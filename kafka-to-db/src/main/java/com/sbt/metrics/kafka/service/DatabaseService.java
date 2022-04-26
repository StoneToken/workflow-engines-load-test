package com.sbt.metrics.kafka.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;

/**
 public interface KogitoProcessInstance extends ProcessInstance, KogitoEventListener {

 int STATE_PENDING = 0;
 int STATE_ACTIVE = 1;
 int STATE_COMPLETED = 2;
 int STATE_ABORTED = 3;
 int STATE_SUSPENDED = 4;
 int STATE_ERROR = 5;

 int SLA_NA = 0;
 int SLA_PENDING = 1;
 int SLA_MET = 2;
 int SLA_VIOLATED = 3;
 int SLA_ABORTED = 4;
 */
public class DatabaseService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseService.class);
    private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");

    private long counter = 0L;
    private long counterProcess = 0L;
    private long counterNode = 0L;

    private String preparedStatementProcessSQL = "insert into ProcessInstance (\n" +
            "id,\n" +
            "parentInstanceId,\n" +
            "rootInstanceId,\n" +
            "processId,\n" +
            "rootProcessId,\n" +
            "processName,\n" +
            "startTime,\n" +
            "endTime,\n" +
            "state,\n" +
            "error,\n" +
            "businessKey)\n" +
            "values (?,?,?,?,?,?,?,?,?,?,?)\n" +
            "on conflict (id) do update set endtime = excluded.endtime, state = excluded.state, error = excluded.error";

    private String preparedStatementNodeSQL = "insert into NodeInstance (\n" +
            "processInstanceId,\n" +
            "id,\n" +
            "nodeId,\n" +
            "nodeName,\n" +
            "nodeType,\n" +
            "startTime,\n" +
            "endTime)\n" +
            "values (?,?,?,?,?,?,?)\n" +
            "on conflict (id) do update set endtime = excluded.endtime";
//            "on conflict do nothing";

    private Connection connection;

    private PreparedStatement preparedStatementProcess;
    private PreparedStatement preparedStatementNode;


    /**
     * Инициализация
     */
    public DatabaseService(
            String databaseDriverClassName,
            String databaseJdbcUrl,
            String databaseUserName,
            String databasePassword
    ) {

        if (databaseJdbcUrl != null && !databaseJdbcUrl.isEmpty()) {
            try {
                DriverManager.registerDriver((Driver) Class.forName(databaseDriverClassName).newInstance());
            } catch (Exception e) {
                LOGGER.error("SQL Ошибка при работе с драйвером: {}\n", databaseDriverClassName, e);
                System.exit(0);
            }

            try {
                connection = DriverManager.getConnection(databaseJdbcUrl, databaseUserName, getStringDecrypt(databasePassword));
            } catch (Exception e) {
                LOGGER.error("Ошибка при создании Connection \n", e);
                System.exit(0);
            }

            try {
                preparedStatementProcess = connection.prepareStatement(preparedStatementProcessSQL);
                preparedStatementNode = connection.prepareStatement(preparedStatementNodeSQL);
            } catch (SQLException throwables) {
                LOGGER.error("Ошибка при инициализации PreparedStatement", throwables);
                System.exit(0);
            }

            createTables();
        }
    }

    /**
     * Создание таблиц
     */
    private void createTables() {
        Statement statement;
        try {
            statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        } catch (Exception e) {
            LOGGER.error("Ошибка при создании Statement\n", e);
            return;
        }

        String sql = "create table IF NOT EXISTS ProcessInstance(\n" +
                "id varchar(64) not null constraint process_id unique,\n" +
                "parentInstanceId varchar(64),\n" +
                "rootInstanceId varchar(64),\n" +
                "processId varchar(64) not null,\n" +
                "rootProcessId varchar(64),\n" +
                "processName varchar(255) not null,\n" +
                "startTime timestamp(6) not null,\n" +
                "endTime timestamp(6),\n" +
                "state int,\n" +
                "error varchar(255),\n" +
                "businesskey varchar(100),\n" +
                "cdate timestamp default CURRENT_TIMESTAMP not null)";
//                "create index processinstance_starttime_idx on processinstance (starttime);\n" +
//                "create index processinstance_endtime_idx on processinstance (endtime);\n" +
//                "create index processinstance_processName_idx on processinstance (processName)";
        try {
            statement.execute(sql);
        } catch (SQLException throwables) {
            LOGGER.error("Ошибка при создании таблицы {}", sql, throwables);
        }

        sql = "create table IF NOT EXISTS NodeInstance(\n" +
                "processInstanceId varchar(64) not null,\n" +
                "id varchar(64) not null constraint node_id unique,\n" +
                "nodeId varchar(64),\n" +
                "nodeName varchar(255),\n" +
                "nodeType varchar(100),\n" +
                "startTime timestamp(6) not null,\n" +
                "endTime timestamp(6))";
//                "create index nodeinstance_endtime_idx on nodeinstance (endtime)";
        try {
            statement.execute(sql);
        } catch (SQLException throwables) {
            LOGGER.error("Ошибка при создании таблицы {}", sql, throwables);
        }
        try {
            statement.close();
        } catch (SQLException throwables) {
            LOGGER.error("", throwables);
        }
    }


    /**
     * Сохранение событий по процессу
     */
    public void add(JSONObject jsonObject) {
        LOGGER.debug("jsonObject: {}", jsonObject);
        insertProcessInstance(jsonObject);
    }

    private void insertProcessInstance(JSONObject jsonObject) {
        counter++;
        counterProcess++;

        if (counter % 1000 == 0) {
            try {
                LOGGER.info("{} {}", counter, jsonObject.getString("time"));
            } catch (JSONException e) {
                LOGGER.error("Ошибка при получении значения time из {}", jsonObject);
            }
        }

        if (connection == null) return;

        long startTime = 0L;
        long endTime = 0L;
        try {
            startTime = sdf.parse(jsonObject.getJSONObject("data").getString("startDate")).getTime();
        } catch (ParseException | JSONException e) {
        }
        try {
            endTime = sdf.parse(jsonObject.getJSONObject("data").getString("endDate")).getTime();
        } catch (ParseException | JSONException e) {
        }

        try {
            preparedStatementProcess.setString(1, jsonObject.getJSONObject("data").getString("id"));
            preparedStatementProcess.setString(2, jsonObject.getJSONObject("data").getString("parentInstanceId"));
            preparedStatementProcess.setString(3, jsonObject.getJSONObject("data").getString("rootInstanceId"));
            preparedStatementProcess.setString(4, jsonObject.getJSONObject("data").getString("processId"));
            preparedStatementProcess.setString(5, jsonObject.getJSONObject("data").getString("rootProcessId"));
            preparedStatementProcess.setString(6, jsonObject.getJSONObject("data").getString("processName"));
            preparedStatementProcess.setTimestamp(7, new Timestamp(startTime));
            preparedStatementProcess.setTimestamp(8, endTime > 0L ? new Timestamp(endTime) : null);
            preparedStatementProcess.setInt(9, jsonObject.getJSONObject("data").getInt("state"));
            preparedStatementProcess.setString(10, jsonObject.getJSONObject("data").getString("error"));
            preparedStatementProcess.setString(11, jsonObject.getJSONObject("data").getString("businessKey"));
            preparedStatementProcess.addBatch();
            if (counterProcess >= 300) {
                preparedStatementProcess.executeBatch();
                counterProcess = 0;
            }

        } catch (Exception e) {
            LOGGER.error("Ошибка при сохранение данных {}", jsonObject, e);
        }
        insertNodeInstance(jsonObject);
    }


    private void insertNodeInstance(JSONObject jsonObject) {
        if (connection == null) return;

        JSONArray jsonArray;
        try {
            jsonArray = jsonObject.getJSONObject("data").getJSONArray("nodeInstances");
            LOGGER.debug("nodeInstances: {}", jsonArray);
        } catch (JSONException e) {
            LOGGER.error("Ошибка при обработке данных {}", jsonObject);
            return;
        }

        for (int o = 0; o < jsonArray.length(); o++) {
            counterNode++;
            long startTime = 0L;
            long endTime = 0L;
            try {
                startTime = sdf.parse(jsonArray.getJSONObject(o).getString("triggerTime")).getTime();
            } catch (ParseException | JSONException e) {
            }
            try {
                endTime = sdf.parse(jsonArray.getJSONObject(o).getString("leaveTime")).getTime();
            } catch (ParseException | JSONException e) {
            }

            try {
                LOGGER.debug("{}", jsonArray.getJSONObject(o));
                preparedStatementNode.setString(1, jsonObject.getJSONObject("data").getString("id"));
                preparedStatementNode.setString(2, jsonArray.getJSONObject(o).getString("id"));
                preparedStatementNode.setString(3, jsonArray.getJSONObject(o).getString("nodeId"));
                preparedStatementNode.setString(4, jsonArray.getJSONObject(o).getString("nodeName"));
                preparedStatementNode.setString(5, jsonArray.getJSONObject(o).getString("nodeType"));
                preparedStatementNode.setTimestamp(6, new Timestamp(startTime));
                preparedStatementNode.setTimestamp(7, endTime > 0L ? new Timestamp(endTime) : null);
                preparedStatementNode.addBatch();
                if (counterNode >= 500) {
                    preparedStatementNode.executeBatch();
                    counterNode = 0;
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
    }

    private String getStringDecrypt(String data) {
        try {
            return new String((Base64.getDecoder().decode(data)));
        } catch (Exception e) {
            return "";
        }
    }
}

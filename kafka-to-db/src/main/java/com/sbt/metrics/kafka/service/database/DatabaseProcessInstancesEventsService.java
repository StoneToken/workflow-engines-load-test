package com.sbt.metrics.kafka.service.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * public interface KogitoProcessInstance extends ProcessInstance, KogitoEventListener {
 * <p>
 * int STATE_PENDING = 0;
 * int STATE_ACTIVE = 1;
 * int STATE_COMPLETED = 2;
 * int STATE_ABORTED = 3;
 * int STATE_SUSPENDED = 4;
 * int STATE_ERROR = 5;
 * <p>
 * int SLA_NA = 0;
 * int SLA_PENDING = 1;
 * int SLA_MET = 2;
 * int SLA_VIOLATED = 3;
 * int SLA_ABORTED = 4;
 */
public class DatabaseProcessInstancesEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProcessInstancesEventsService.class);
    private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS+00:00");

    private final long reconnectTime = 5 * 60000L; // интервал для переподключения к БД (ms)
    private final long insertTime = 1 * 60000L; // интервал для выполнения executeBatch (ms)

    private String databaseDriverClassName;
    private String databaseJdbcUrl;
    private String databaseUserName;
    private String databasePassword;
    private String databaseSchema;
    private int databaseSizeLimit;

    private long counter = 0L;
    private long counterProcess = 0L;
    private long counterNode = 0L;
    private long lastConnectTime = 0L;
    private long lastInsertTime = 0L;

    private String preparedStatementProcessSQL;
    private String preparedStatementNodeSQL;

    private Connection connection;

    private PreparedStatement preparedStatementProcess;
    private PreparedStatement preparedStatementNode;


    /**
     * Инициализация
     */
    public DatabaseProcessInstancesEventsService(
            String databaseDriverClassName,
            String databaseJdbcUrl,
            String databaseUserName,
            String databasePassword,
            String databaseSchema,
            int databaseSizeLimit
    ) {

        if (databaseJdbcUrl == null || databaseJdbcUrl.isEmpty()) {
            LOGGER.error("DB connection parameters are not set");
            System.exit(0);
        }
        this.databaseDriverClassName = databaseDriverClassName;
        this.databaseSchema = databaseSchema + (databaseSchema.isEmpty() ? "" : ".");
        this.databaseJdbcUrl = databaseJdbcUrl;
        this.databaseUserName = databaseUserName;
        this.databasePassword = databasePassword;
        this.databaseSizeLimit = databaseSizeLimit;

        preparedStatementProcessSQL = "insert into " + this.databaseSchema + "ProcessInstance (\n" +
                "id,\n" +
                "parentInstanceId,\n" +
                "rootInstanceId,\n" +
                "rootProcessId,\n" +
                "processId,\n" +
                "processName,\n" +
                "startTime,\n" +
                "endTime,\n" +
                "state,\n" +
                "businessKey,\n" +
                "error)\n" +
                "values (?,?,?,?,?,?,?,?,?,?,?)\n" +
                "on conflict (id) do update set endtime = EXCLUDED.endtime, state = EXCLUDED.state, error = EXCLUDED.error where EXCLUDED.endtime is not null";

        preparedStatementNodeSQL = "insert into " + this.databaseSchema + "NodeInstance (\n" +
                "processInstanceId,\n" +
                "id,\n" +
                "nodeId,\n" +
                "nodeName,\n" +
                "nodeType,\n" +
                "startTime,\n" +
                "endTime)\n" +
                "values (?,?,?,?,?,?,?)\n" +
                "on conflict (id) do update set endtime = EXCLUDED.endtime where EXCLUDED.endtime is not null";
//            "on conflict do nothing";

        connect();
        createTables();
    }

    public void connect() {

        if ((System.currentTimeMillis() - lastInsertTime) > insertTime) { // executeBatch
            if (counterProcess > 0) {
                try {
                    preparedStatementProcess.executeBatch();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                }
                counterProcess = 0;
            }
            if (counterNode > 0) {
                try {
                    preparedStatementNode.executeBatch();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                }
                counterNode = 0;
            }
        }

        if ((System.currentTimeMillis() - lastInsertTime) > reconnectTime &&
                (System.currentTimeMillis() - lastConnectTime) > reconnectTime) {

            lastConnectTime = System.currentTimeMillis();

            if (connection != null) {
                LOGGER.info("Reconnecting to the database...");
                if (preparedStatementProcess != null) {
                    try {
                        preparedStatementProcess.close();
                    } catch (SQLException throwables) {
                        LOGGER.error("", throwables);
                    }
                }
                if (preparedStatementNode != null) {
                    try {
                        preparedStatementNode.close();
                    } catch (SQLException throwables) {
                        LOGGER.error("", throwables);
                    }
                }
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                }
            } else {
                LOGGER.info("Connecting to the database...");
            }

            try {
                DriverManager.registerDriver((Driver) Class.forName(databaseDriverClassName).newInstance());
            } catch (Exception e) {
                LOGGER.error("SQL error when working with the driver: {}\n", databaseDriverClassName, e);
                System.exit(0);
            }

            try {
                connection = DriverManager.getConnection(databaseJdbcUrl, databaseUserName, getStringDecrypt(databasePassword));
            } catch (Exception e) {
                LOGGER.error("Error creating Connection\n", e);
                System.exit(0);
            }

            try {
                preparedStatementProcess = connection.prepareStatement(preparedStatementProcessSQL);
                preparedStatementNode = connection.prepareStatement(preparedStatementNodeSQL);
            } catch (SQLException throwables) {
                LOGGER.error("Error when initializing the PreparedStatement\n", throwables);
                System.exit(0);
            }

            checkDbTableSpace(); // проверка переполнения БД
        }
    }

    /**
     * Создание таблиц
     */
    private void createTables() {
        String sql = "create table IF NOT EXISTS " + databaseSchema + "ProcessInstance(\n" +
                "id varchar(64) not null constraint process_id unique,\n" +
                "parentInstanceId varchar(64),\n" +
                "rootInstanceId varchar(64),\n" +
                "rootProcessId varchar(64),\n" +
                "processId varchar(64) not null,\n" +
                "processName varchar(255) not null,\n" +
                "startTime timestamp(6) not null,\n" +
                "endTime timestamp(6),\n" +
                "state int,\n" +
                "businessKey varchar(100),\n" +
                "error varchar(255),\n" +
                "cdate timestamp default CURRENT_TIMESTAMP not null);\n" +
                "create index if not exists processinstance_starttime_idx on processinstance (starttime);\n" +
                "create index if not exists processinstance_endtime_idx on processinstance (endtime)";

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            statement.execute(sql);
        } catch (SQLException throwables) {
            LOGGER.error("Error creating the table {}", sql, throwables);
        }


        sql = "create table IF NOT EXISTS " + databaseSchema + "NodeInstance(\n" +
                "id varchar(64) not null constraint node_id unique,\n" +
                "processInstanceId varchar(64) not null,\n" +
                "nodeId varchar(64),\n" +
                "nodeName varchar(255),\n" +
                "nodeType varchar(100),\n" +
                "startTime timestamp(6) not null,\n" +
                "endTime timestamp(6));\n" +
                "create index if not exists nodeinstance_processinstanceid_idx on nodeinstance (processinstanceid);\n" +
                "create index if not exists nodeinstance_endtime_idx on nodeinstance (endtime)";

        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            statement.execute(sql);
        } catch (SQLException throwables) {
            LOGGER.error("Error creating the table {}", sql, throwables);
        }
    }

    /**
     * Сохранение событий по процессу
     */
    public void add(JSONObject jsonObject) {
        LOGGER.debug("jsonObject: {}", jsonObject);
        lastInsertTime = System.currentTimeMillis();
        insertProcessInstance(jsonObject);
    }

    private void insertProcessInstance(JSONObject jsonObject) {
        counter++;
        counterProcess++;

        if (counter % 1000 == 0) {
            try {
                LOGGER.info("{} {}", counter, jsonObject.getString("time"));
            } catch (JSONException e) {
                LOGGER.error("Error when getting the <time> value from {}", jsonObject);
            }
        }

        if (connection == null) return;

        String id = "";
        try {
            id = jsonObject.getJSONObject("data").getString("id");
        } catch (JSONException e) {
        }

        long startTime = 0L;
        long endTime = 0L;
        try {
            startTime = getDateTime(jsonObject.getJSONObject("data").getString("startDate"), id);
            endTime = getDateTime(jsonObject.getJSONObject("data").getString("endDate"), id);
        } catch (Exception e) {
            LOGGER.error("", e);
            return;
        }
        if (startTime < 0L || endTime < 0L) return;

        try {
            preparedStatementProcess.setString(1, id);
            preparedStatementProcess.setString(2, strToNull(jsonObject.getJSONObject("data").getString("parentInstanceId")));
            preparedStatementProcess.setString(3, strToNull(jsonObject.getJSONObject("data").getString("rootInstanceId")));
            preparedStatementProcess.setString(4, strToNull(jsonObject.getJSONObject("data").getString("rootProcessId")));
            preparedStatementProcess.setString(5, jsonObject.getJSONObject("data").getString("processId"));
            preparedStatementProcess.setString(6, substring(jsonObject.getJSONObject("data").getString("processName"), 255));
            preparedStatementProcess.setTimestamp(7, new Timestamp(startTime));
            preparedStatementProcess.setTimestamp(8, endTime > 0L ? new Timestamp(endTime) : null);
            preparedStatementProcess.setInt(9, jsonObject.getJSONObject("data").getInt("state"));
            preparedStatementProcess.setString(10, substring(jsonObject.getJSONObject("data").getString("businessKey"), 100));
            preparedStatementProcess.setString(11, substring(jsonObject.getJSONObject("data").getString("error"), 255));
            preparedStatementProcess.addBatch();
            if (counterProcess >= 300) {
                try {
                    preparedStatementProcess.executeBatch();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                    connect();
                }
                counterProcess = 0;
            }
        } catch (Exception e) {
            LOGGER.error("Error when saving data {}", jsonObject, e);
        }
        insertNodeInstance(jsonObject, id);
    }


    private void insertNodeInstance(JSONObject jsonObject, String processInstanceId) {
        if (connection == null) return;

        JSONArray jsonArray;
        try {
            jsonArray = jsonObject.getJSONObject("data").getJSONArray("nodeInstances");
            LOGGER.debug("nodeInstances: {}", jsonArray);
        } catch (JSONException e) {
            LOGGER.error("Error in data processing {}", jsonObject);
            return;
        }

        for (int o = 0; o < jsonArray.length(); o++) {
            counterNode++;
            long startTime = 0L;
            long endTime = 0L;
            try {
                startTime = getDateTime(jsonArray.getJSONObject(o).getString("triggerTime"), processInstanceId);
                endTime = getDateTime(jsonArray.getJSONObject(o).getString("leaveTime"), processInstanceId);
            } catch (Exception e) {
                LOGGER.error("", e);
                return;
            }
            LOGGER.debug("startTime: {} endTime: {}", sdf.format(startTime), sdf.format(endTime));
            if (startTime < 0L || endTime < 0L) return;

            try {
                LOGGER.debug("{}", jsonArray.getJSONObject(o));
                preparedStatementNode.setString(1, processInstanceId);
                preparedStatementNode.setString(2, jsonArray.getJSONObject(o).getString("id"));
                preparedStatementNode.setString(3, jsonArray.getJSONObject(o).getString("nodeId"));
                preparedStatementNode.setString(4, substring(jsonArray.getJSONObject(o).getString("nodeName"), 255));
                preparedStatementNode.setString(5, substring(jsonArray.getJSONObject(o).getString("nodeType"), 100));
                preparedStatementNode.setTimestamp(6, new Timestamp(startTime));
                preparedStatementNode.setTimestamp(7, endTime > 0L ? new Timestamp(endTime) : null);
                preparedStatementNode.addBatch();
                if (counterNode >= 300) {
                    try {
                        preparedStatementNode.executeBatch();
                    } catch (SQLException throwables) {
                        LOGGER.error("", throwables);
                    }
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

    private String strToNull(String str) {
        if (str == null || str.equalsIgnoreCase("null")) return null;
        return str;
    }

    private String substring(String data, int length) {
        data = strToNull(data);
        return (data == null || data.length() <= length) ? data : data.substring(0, length);
    }

    private boolean checkDbTableSpace() {
        boolean res = true;
        String sql = "select pg_database_size((select current_database())) / 1024/ 1024 as size";
        try (Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet != null) {
                while (resultSet.next()) {
                    int size = resultSet.getInt("size");
                    if (size > databaseSizeLimit) {
                        LOGGER.error("TableSpace DB: {} > {}", size, databaseSizeLimit);
                        System.exit(0);
                    }
                    if (size > databaseSizeLimit * 0.9) {
                        LOGGER.warn("TableSpace DB: {} > 0.9 * {}", size, databaseSizeLimit);
                    }
                }
            }
        } catch (SQLException throwables) {
            LOGGER.error("{}", sql, throwables);
        }

        return res;
    }

    public void sleepMilliseconds(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOGGER.error("TimeUnit.MILLISECONDS.sleep", e);
        }
    }

    private long getDateTime(String timeStr, String id) {
        long time = 0L;
        if (timeStr == null || timeStr.isEmpty() || timeStr.equalsIgnoreCase("null")) {
            return time;
        }
        if (timeStr.length() != 29) {
            LOGGER.warn("Date is not correct {}", timeStr);
            return time;
        }
        try {
            time = sdf.parse(timeStr).getTime();
        } catch (Exception e) {
            try {
                sleepMilliseconds(1000);
                time = sdf.parse(timeStr).getTime();
                if (time < 0L) {
                    sleepMilliseconds(1000);
                    time = sdf.parse(timeStr).getTime();
                }
            } catch (Exception e2) {
                LOGGER.warn("date {} {}", id, timeStr, e);
                return -1L;
            }
        }
        return time;
    }
}

package com.sbt.metrics.kafka.service.database;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class DatabaseJobsEventsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseJobsEventsService.class);

    private final DateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private final DateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'");
    private final DateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SS'Z'");
    private final DateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

    private final long reconnectTime = 5 * 60000L; // интервал для переподключения к БД (ms)
    private final long insertTime = 1 * 60000L; // интервал для выполнения executeBatch (ms)

    private String databaseDriverClassName;
    private String databaseJdbcUrl;
    private String databaseUserName;
    private String databasePassword;
    private String databaseSchema;

    private long counter = 0L;
    private long counterJobs = 0L;
    private long lastConnectTime = 0L;
    private long lastInsertTime = 0L;

    private String preparedStatementJobsSQL;

    private Connection connection;

    private PreparedStatement preparedStatementJobs;


    /**
     * Инициализация
     */
    public DatabaseJobsEventsService(
            String databaseDriverClassName,
            String databaseJdbcUrl,
            String databaseUserName,
            String databasePassword,
            String databaseSchema
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

        preparedStatementJobsSQL = "insert into " + this.databaseSchema + "Jobs (\n" +
                "id,\n" +
                "processInstanceId,\n" +
                "nodeInstanceId,\n" +
                "status,\n" +
                "\"time\",\n" +
                "lastUpdate,\n" +
                "expirationTime)\n" +
                "values (?,?,?,?,?,?,?)\n" +
                "on conflict do nothing";

        connect();
        createTables();
    }

    public void connect() {
        if ((System.currentTimeMillis() - lastInsertTime) > insertTime) {
            if (counterJobs > 0) { // executeBatch
                try {
                    preparedStatementJobs.executeBatch();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                }
                counterJobs = 0;
            }
        }

        if ((System.currentTimeMillis() - lastInsertTime) > reconnectTime &&
                (System.currentTimeMillis() - lastConnectTime) > reconnectTime) {
            lastConnectTime = System.currentTimeMillis();

            if (connection != null) {
                LOGGER.info("Reconnecting to the database...");
                if (preparedStatementJobs != null) {
                    try {
                        preparedStatementJobs.close();
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
                preparedStatementJobs = connection.prepareStatement(preparedStatementJobsSQL);
            } catch (SQLException throwables) {
                LOGGER.error("Error when initializing the PreparedStatement\n", throwables);
                System.exit(0);
            }
        }
    }

    /**
     * Создание таблиц
     */
    private void createTables() {
        String sql = "create table IF NOT EXISTS " + databaseSchema + "Jobs(\n" +
                "id varchar(64) not null,\n" +
                "processInstanceId varchar(64) not null,\n" +
                "nodeInstanceId varchar(64),\n" +
                "status varchar(50),\n" +
                "\"time\" timestamp(6) not null,\n" +
                "lastUpdate timestamp(6),\n" +
                "expirationTime timestamp(6),\n" +
                "cdate timestamp default CURRENT_TIMESTAMP not null);\n" +
                "create index if not exists jobs_processinstanceid_nodeinstanceid_idx on jobs (processinstanceid, nodeinstanceid)";

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
        insertJobs(jsonObject);
    }

    private void insertJobs(JSONObject jsonObject) {
        counter++;
        counterJobs++;

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

        long time = 0L;
        long lastUpdate = 0L;
        long expirationTime = 0L;
        try {
            time = getDateTime(jsonObject.getString("time"), id);
            lastUpdate = getDateTime(jsonObject.getJSONObject("data").getString("lastUpdate"), id);
            expirationTime = getDateTime(jsonObject.getJSONObject("data").getString("expirationTime"), id);
        } catch (Exception e) {
            LOGGER.error("", e);
            return;
        }
        if (time < 0L || lastUpdate < 0L || expirationTime < 0L) return;

        try {
            preparedStatementJobs.setString(1, id);
            preparedStatementJobs.setString(2, strToNull(jsonObject.getJSONObject("data").getString("processInstanceId")));
            preparedStatementJobs.setString(3, strToNull(jsonObject.getJSONObject("data").getString("nodeInstanceId")));
            preparedStatementJobs.setString(4, strToNull(jsonObject.getJSONObject("data").getString("status")));
            preparedStatementJobs.setTimestamp(5, new Timestamp(time));
            preparedStatementJobs.setTimestamp(6, lastUpdate > 0L ? new Timestamp(lastUpdate) : null);
            preparedStatementJobs.setTimestamp(7, expirationTime > 0L ? new Timestamp(expirationTime) : null);
            preparedStatementJobs.addBatch();
            if (counterJobs >= 300) {
                try {
                    preparedStatementJobs.executeBatch();
                } catch (SQLException throwables) {
                    LOGGER.error("", throwables);
                }
                counterJobs = 0;
            }

        } catch (Exception e) {
            LOGGER.error("Error when saving data {}", jsonObject, e);
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

    public void sleepMilliseconds(long milliseconds) {
        try {
            TimeUnit.MILLISECONDS.sleep(milliseconds);
        } catch (InterruptedException e) {
            LOGGER.error("TimeUnit.MILLISECONDS.sleep", e);
        }
    }

    private long getDateTime(String timeStr, String id) {
        long time = 0L;
        DateFormat sdf = sdf3;
        String timeStr1 = timeStr;
        if (timeStr == null || timeStr.isEmpty() || timeStr.equalsIgnoreCase("null")) {
            return time;
        }

        if (timeStr.length() < 20) {
            LOGGER.warn("Date is not correct {}", timeStr);
            return time;
        } else if (timeStr.length() > 23) {
            timeStr = timeStr.substring(0, 23) + 'Z';
        } else if (timeStr.length() == 23) {
            sdf = sdf2;
            timeStr = timeStr.substring(0, 22) + 'Z';
        } else if (timeStr.length() == 22){
            sdf = sdf1;
            timeStr = timeStr.substring(0, 21) + 'Z';
        } else {
            sdf = sdf0;
            timeStr = timeStr.substring(0, 19) + 'Z';
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
                LOGGER.warn("time | {} | {} | {} | {} | {}", id, timeStr1, timeStr, sdf3.format(time), time, e2);
                return -1L;
            }
        }
        return time;
    }

}
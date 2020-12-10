package db;

import models.handlers.GrammarHandler;
import models.handlers.StringsHandler;
import models.handlers.TimeHandler;
import org.apache.commons.text.StringEscapeUtils;
import selections.Conclusion;
import selections.ExecutionInfo;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class Db {

    private int diagnosticianId;
    private int areaId;
    private int contrastId;
    private int patientId;
    private ResultSet result;
    private Statement statement;

    private Db() throws SQLException {
        // подключусь к базе данных
        // opening database connection to MySQL server
        mConnection = DriverManager.getConnection(url, user, password);
    }

    private static Db instance;

    public static Db getInstance() throws SQLException {
        if (instance == null) {
            instance = new Db();
        }
        return instance;
    }

    private static final HashMap<String, Integer> diagnosticiansCache = new HashMap<>();
    private static final HashMap<String, Integer>areasCache = new HashMap<>();
    private static final HashMap<String, Integer>contrastsCache = new HashMap<>();
    private static final String url = "jdbc:mysql://localhost:3306/rdcnn_base?serverTimezone=UTC&characterEncoding=utf8";
    private static final String user = "root";
    private static final String password = "root";
    private final Connection mConnection;
    private String query;

    /**
     * Возвращает заготовку для запроса
     *
     * @return Statement
     */
    private Statement getStatement() throws SQLException {
        return mConnection.createStatement();
    }

    public void handleConclusion(Conclusion conclusion) throws SQLException {
        try {
            // нужно занести данные в таблицу, предварительно убедившись, что их там нет
            if (conclusion != null && conclusion.isFilled()) {
                // данные о пациенте
                patientId = insertPatientData(conclusion);
                // данные о враче
                // сначала поищу в кеше, если там нет- запрошу значение в БД и положу в кеш
                if(diagnosticiansCache.containsKey(conclusion.getDiagnostician())){
                    diagnosticianId = diagnosticiansCache.get(conclusion.getDiagnostician());
                    //System.out.println("fill diagnostician from cache");
                }
                else{
                    diagnosticianId = insertDiagnosticianData(conclusion);
                    diagnosticiansCache.put(conclusion.getDiagnostician(), diagnosticianId);
                }

                if(areasCache.containsKey(conclusion.getExecutionArea())){
                    areaId = areasCache.get(conclusion.getExecutionArea());
                    //System.out.println("fill area from cache");
                }
                else{
                    areaId = insertScannedAreaData(conclusion);
                    areasCache.put(conclusion.getExecutionArea(), areaId);
                }
                if(contrastsCache.containsKey(conclusion.getContrast())){
                    //System.out.println("fill contrast from cache");
                    contrastId = contrastsCache.get(conclusion.getContrast());
                }
                else{
                    contrastId = insertContrastData(conclusion);
                    contrastsCache.put(conclusion.getContrast(), contrastId);
                }
                // И, наконец, внесу данные о самом обследовании
                insertExecution(
                        patientId,
                        diagnosticianId,
                        areaId,
                        contrastId,
                        conclusion
                );
            }
        } catch (SQLException e) {
            System.out.println("can't insert data( " + e.getMessage());
            throw e;
        }
    }

    private void insertExecution(int patientId, int diagnosticianId, int areaId, int contrastId, Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "INSERT INTO `rdcnn_base`.`executions` (`patient`, `diagnostician`, `execution_area`, `contrast`, `execution_number`, `execution_date`, `text`, `md5`, `change_time`, `file_name`, `path`) VALUES ('%d', '%d', '%d', '%d', '%s', '%s', '%s', '%s', '%d', '%s', '%s');", patientId, diagnosticianId, areaId, contrastId, conclusion.getExecutionNumber(), conclusion.getExecutionDate(), StringsHandler.escapeQuotes(conclusion.getText()), conclusion.getMd5(), conclusion.getChangeTime(), conclusion.getFileName(), StringEscapeUtils.escapeHtml4(conclusion.getPath()));
        executeUpdateQuery(query);
    }

    private int insertContrastData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertContrast('%s');", conclusion.getContrast());
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private int insertScannedAreaData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertExecutionArea('%s', '%s');", conclusion.getExecutionArea(), conclusion.getPath());
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private int insertDiagnosticianData(Conclusion conclusion) throws SQLException {
        // если пациент ещё не зарегистрирован- зарегистрирую его
        query = String.format(Locale.ENGLISH, "CALL insertDiagnostician('%s');", conclusion.getDiagnostician());
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }


    private int insertPatientData(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "CALL insertPatient('%s', '%s', '%s');", conclusion.getPersonals(), conclusion.getBirthDate(), conclusion.getSex());
        result = executeQuery(query);
        result.next();
        return result.getInt(1);
    }

    private ResultSet executeQuery(String query) throws SQLException {
        statement = getStatement();
        return statement.executeQuery(query);
    }


    private void executeUpdateQuery(String query) throws SQLException {
        statement = getStatement();
        statement.executeUpdate(query);
    }

    public void clearData() throws SQLException {
        executeUpdateQuery("SET FOREIGN_KEY_CHECKS = 0;");
        executeUpdateQuery("TRUNCATE TABLE `rdcnn_base`.`contrasts`;");
        executeUpdateQuery("TRUNCATE TABLE `rdcnn_base`.`diagnosticians`;");
        executeUpdateQuery("TRUNCATE TABLE `rdcnn_base`.`execution_areas`;");
        executeUpdateQuery("TRUNCATE TABLE `rdcnn_base`.`executions`;");
        executeUpdateQuery("TRUNCATE TABLE `rdcnn_base`.`patients`;");
        executeUpdateQuery("SET FOREIGN_KEY_CHECKS = 1;");

    }

    public void disableForeignChecks(){
        try {
            executeUpdateQuery("SET FOREIGN_KEY_CHECKS = 0;");
        } catch (SQLException throwables) {
            System.out.println("error with disabling foreign checks");
            throwables.printStackTrace();
        }
    }
    public void enableForeignChecks(){
        System.out.println("enabling foreign checks");
        try {
            executeUpdateQuery("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException throwables) {
            System.out.println("error with enabling foreign checks");
            throwables.printStackTrace();
        }
    }

    /**
     * @param f <p>Файл</p>
     * @return <p>true, если файл уже в базе, false, если нет</p>
     * @throws IOException  <p>Ошибка ввода-вывода</p>
     * @throws SQLException <p>Ошибка базы данных</p>
     */
    public boolean checkFile(File f) throws IOException, SQLException {
        // проверю, зарегистрирован ли в базе файл, ориентируясь на путь к файлу и дату его изменения
        query = String.format(Locale.ENGLISH, "SELECT COUNT(id) FROM rdcnn_base.`executions` WHERE rdcnn_base.`executions`.`path` = '%s' AND rdcnn_base.`executions`.`change_time` = '%d';", StringsHandler.addSlashes(f.getCanonicalPath()), f.lastModified());
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1) > 0;
        }
        return false;
    }

    /**
     * Выполнение запроса обследований из главного окна программы
     *
     * @param request <p>Сформированный запрос</p>
     * @return <p>Набор найденных обследований</p>
     * @throws SQLException <p></p>
     */
    public ArrayList<ExecutionInfo> request(SearchRequest request) throws SQLException {
        ResultSet result = executeQuery(request.request());
        // перегоню полученный результат в значение
        ArrayList<ExecutionInfo> response = new ArrayList<>();
        ExecutionInfo value;
        while (result.next()) {
            value = ExecutionInfo.newBuilder()
                    .setExecutionNumber(result.getString(1))
                    .setExecutionDate(GrammarHandler.normalizeDateFromDb(result.getString(2)))
                    .setChangeDate(TimeHandler.timestampToDateTime(result.getLong(3)))
                    .setPatientId(result.getInt(4))
                    .setPath(result.getString(5))
                    .setDiagnosticianName(result.getString(6))
                    .setExecutionArea(result.getString(7))
                    .setBirthDate(GrammarHandler.normalizeDateFromDb(result.getString(8)))
                    .setPersonals(result.getString(9))
                    .setContrast(result.getString(10))
                    .build();
            response.add(value);
        }
        return response;
    }

    public ArrayList<String> getDoctors() throws SQLException {
        query = "SELECT diagnostician_name FROM diagnosticians ORDER BY diagnostician_name";
        ResultSet result = executeQuery(query);
        ArrayList<String> answer = new ArrayList<>();
        while (result.next()) {
            answer.add(result.getString(1));
        }
        return answer;
    }

    public ArrayList<String> getExecutionAreas() throws SQLException {
        query = "SELECT area_name FROM execution_areas ORDER BY area_name";
        ResultSet result = executeQuery(query);
        ArrayList<String> answer = new ArrayList<>();
        while (result.next()) {
            answer.add(result.getString(1));
        }
        return answer;
    }

    public String getExecutionAreaPath(String executionArea) throws SQLException {
        query = "SELECT path FROM execution_areas WHERE area_name = \"" + executionArea + "\";";
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getString(1);
        }
        return null;
    }

    public boolean isDatabaseEmpty() throws SQLException {
        // проверю наличие строк в базе
        query = "SELECT EXISTS (SELECT 1 FROM rdcnn_base.`executions`);";
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1) == 0;
        }
        return true;
    }

    public void enableIndexes() throws SQLException {
        System.out.println("enabling indexes");
//        query = "ALTER TABLE `rdcnn_base`.`patients` ADD KEY `x_patient_name` (`personals`);";
//        executeUpdateQuery(query);
        query = "ALTER TABLE `rdcnn_base`.`executions` ADD FULLTEXT INDEX `x_execution_text` (`text`);";
        executeUpdateQuery(query);
        query = "ALTER TABLE `rdcnn_base`.`executions` ADD KEY `x_path` (`path`);";
        executeUpdateQuery(query);
        query = "ALTER TABLE `rdcnn_base`.`executions` ADD KEY `x_change_time` (`change_time`);";
        executeUpdateQuery(query);
        System.out.println("indexes enabled");
    }
    public void disableIndexes() throws SQLException {
//        query = "CALL dropIndexes()";
//        executeUpdateQuery(query);
        System.out.println("indexes disabled");
    }
}

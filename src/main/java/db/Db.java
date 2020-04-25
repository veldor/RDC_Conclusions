package db;

import models.handlers.StringsHandler;
import org.apache.commons.text.StringEscapeUtils;
import selections.Conclusion;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Locale;

public class Db {

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
        try{
            // нужно занести данные в таблицу, предварительно убедившись, что их там нет
            if (conclusion != null && conclusion.isFilled()) {
                // данные о пациенте
                int patientId = insertPatientData(conclusion);
                // данные о враче
                int diagnosticianId = insertDiagnosticianData(conclusion);
                // данные об области обследования
                int areaId = insertScannedAreaData(conclusion);
                // данные о контрастном веществе
                int contrastId = insertContrastData(conclusion);
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
            System.out.println(conclusion.getText());
            throw e;
        }
    }

    private void insertExecution(int patientId, int diagnosticianId, int areaId, int contrastId, Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "INSERT INTO `rdcnn_base`.`executions` (`patient`, `diagnostician`, `execution_area`, `contrast`, `execution_number`, `execution_date`, `text`, `md5`, `change_time`, `file_name`, `path`) VALUES ('%d', '%d', '%d', '%d', '%s', '%s', '%s', '%s', '%d', '%s', '%s');", patientId, diagnosticianId, areaId, contrastId, conclusion.getExecutionNumber(), conclusion.getExecutionDate(), StringsHandler.escapeQuotes(conclusion.getText()), conclusion.getMd5(), conclusion.getChangeTime(), conclusion.getFileName(), StringEscapeUtils.escapeHtml4(conclusion.getPath()));
        executeUpdateQuery(query);
    }

    private int insertContrastData(Conclusion conclusion) throws SQLException {
        int result = getContrast(conclusion);
        if (result < 0) {
            // если пациент ещё не зарегистрирован- зарегистрирую его
            query = String.format(Locale.ENGLISH, " INSERT INTO `rdcnn_base`.`contrasts` (`value`) VALUES ('%s');", conclusion.getContrast());
            executeUpdateQuery(query);
            result = getContrast(conclusion);
        }
        return result;
    }

    private int insertScannedAreaData(Conclusion conclusion) throws SQLException {
        int result = getArea(conclusion);
        if (result < 0) {
            // если пациент ещё не зарегистрирован- зарегистрирую его
            query = String.format(Locale.ENGLISH, " INSERT INTO `rdcnn_base`.`execution_areas` (`area_name`) VALUES ('%s');", conclusion.getExecutionArea());
            executeUpdateQuery(query);
            result = getArea(conclusion);
        }
        return result;
    }

    private int insertDiagnosticianData(Conclusion conclusion) throws SQLException {
        int result = getDiagnostician(conclusion);
        if (result < 0) {
            // если пациент ещё не зарегистрирован- зарегистрирую его
            query = String.format(Locale.ENGLISH, " INSERT INTO `rdcnn_base`.`diagnosticians` (`diagnostician_name`) VALUES ('%s');", conclusion.getDiagnostician());
            executeUpdateQuery(query);
            result = getDiagnostician(conclusion);
        }
        return result;
    }


    private int insertPatientData(Conclusion conclusion) throws SQLException {
        int result = getPatient(conclusion);
        if (result < 0) {
            // если пациент ещё не зарегистрирован- зарегистрирую его
            query = String.format(Locale.ENGLISH, " INSERT INTO `rdcnn_base`.`patients` (`personals`, `birth_date`, `sex`) VALUES ('%s', '%s', '%s');", conclusion.getPersonals(), conclusion.getBirthDate(), conclusion.getSex());
            executeUpdateQuery(query);
            result = getPatient(conclusion);
        }
        return result;
    }


    /**
     * Проверю наличие пациента в базе
     *
     * @param conclusion <p>Данные об обследованиии</p>
     * @return <p>true в случае наличия пациента и false в случае отсутствия</p>
     * @throws SQLException <p>database errors</p>
     */
    private int getPatient(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "SELECT id FROM patients WHERE patients.personals = '%s' AND patients.`sex` = '%s' AND patients.`birth_date` = '%s';", conclusion.getPersonals(), conclusion.getSex(), conclusion.getBirthDate());
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1);
        }
        return -1;
    }

    private int getDiagnostician(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "SELECT id FROM diagnosticians WHERE diagnosticians.diagnostician_name = '%s';", conclusion.getDiagnostician());
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1);
        }
        return -1;
    }

    private int getArea(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "SELECT id FROM execution_areas WHERE execution_areas.area_name = '%s';", conclusion.getExecutionArea());
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1);
        }
        return -1;
    }

    private int getContrast(Conclusion conclusion) throws SQLException {
        query = String.format(Locale.ENGLISH, "SELECT id FROM contrasts WHERE contrasts.value = '%s';", conclusion.getContrast());
        ResultSet result = executeQuery(query);
        if (result.next()) {
            return result.getInt(1);
        }
        return -1;
    }

    private ResultSet executeQuery(String query) throws SQLException {
        Statement statement = getStatement();
        return statement.executeQuery(query);
    }


    private void executeUpdateQuery(String query) throws SQLException {
        Statement statement = getStatement();
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

    /**
     * @param f <p>Файл</p>
     * @return <p>true, если файл уже в базе, false, если нет</p>
     * @throws IOException <p>Ошибка ввода-вывода</p>
     * @throws SQLException <p>Ошибка базы данных</p>
     */
    public boolean checkFile(File f) throws IOException, SQLException {
        // проверю, зарегистрирован ли в базе файл, ориентируясь на путь к файлу и дату его изменения
        query = String.format(Locale.ENGLISH, "SELECT COUNT(id) FROM rdcnn_base.`executions` WHERE rdcnn_base.`executions`.`path` = '%s' AND rdcnn_base.`executions`.`change_time` = '%d';", StringsHandler.addSlashes(f.getCanonicalPath()), f.lastModified());
        ResultSet result = executeQuery(query);
        if(result.next()){
            return result.getInt(1) > 0;
        }
        return false;
    }
}

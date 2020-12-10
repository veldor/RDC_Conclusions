package selections;

import models.Settings;
import models.handlers.FilesHandler;
import ui_controllers.MainController;

import java.io.IOException;
import java.sql.SQLException;

public class ExecutionInfo {

    private String path;

    private ExecutionInfo() {
    }

    public static ExecutionInfo.Builder newBuilder() {
        return new ExecutionInfo().new Builder();
    }


    private String diangostician;
    private int patientId;
    private String executionNumber;
    private String personals;
    private String birthDate;
    private String executionArea;
    private String executionDate;
    private String contrast;
    private String changeDate;

    public String getExecutionNumber() {
        return executionNumber;
    }

    public String getPersonals() {
        return personals;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getExecutionArea() {
        return executionArea;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getContrast() {
        return contrast;
    }

    public String getChangeDate() {
        return changeDate;
    }

    public void openFile() throws IOException {
        FilesHandler.openFile(path);
    }

    public void openContainsFolder() throws IOException {
        FilesHandler.openContainingFolder(path);
    }

    public void useAsTemplate(ExecutionInfo rowData) throws IOException {
        // скопирую файл во временную папку и открою для редактирования
        FilesHandler.useAsTemplate(rowData.path);
    }

    public void showAllByPatient(MainController mainController) throws SQLException {
        // вызову метод поиска данных по id пациента
        mainController.searchPatientExecutions(patientId);
    }

    public class Builder {
        private Builder() {
        }

        public ExecutionInfo build() {
            return ExecutionInfo.this;
        }

        public Builder setPersonals(String parameter) {
            ExecutionInfo.this.personals = parameter;
            return this;
        }
        public Builder setExecutionNumber(String parameter) {
            ExecutionInfo.this.executionNumber = parameter;
            return this;
        }
        public Builder setBirthDate(String parameter) {
            ExecutionInfo.this.birthDate = parameter;
            return this;
        }
        public Builder setExecutionArea(String parameter) {
            ExecutionInfo.this.executionArea = parameter;
            return this;
        }
        public Builder setExecutionDate(String parameter) {
            ExecutionInfo.this.executionDate = parameter;
            return this;
        }
        public Builder setContrast(String parameter) {
            ExecutionInfo.this.contrast = parameter;
            return this;
        }
        public Builder setChangeDate(String parameter) {
            ExecutionInfo.this.changeDate = parameter;
            return this;
        }
        public Builder setPath(String parameter) {
            ExecutionInfo.this.path = parameter;
            return this;
        }
        public Builder setDiagnosticianName(String parameter) {
            ExecutionInfo.this.diangostician = parameter;
            return this;
        }

        public Builder setPatientId(int parameter) {
            ExecutionInfo.this.patientId = parameter;
            return this;
        }
    }
}

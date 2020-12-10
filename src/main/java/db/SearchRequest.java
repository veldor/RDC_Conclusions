package db;


import com.mysql.cj.util.DnsSrv;
import models.handlers.GrammarHandler;
import models.handlers.StringsHandler;
import ui_controllers.MainController;

import java.text.ParseException;
import java.util.Locale;

public class SearchRequest {



    private SearchRequest(){}

    private String doctorName;
    private String text;
    private String executionDate;
    private String executionDateFinish;
    private String executionId;
    private String personals;
    private String executionArea;

    private int patientId = -1;


    public static SearchRequest.Builder newBuilder() {
        return new SearchRequest().new Builder();
    }

    public String request() {
        boolean haveParameter = false;
        StringBuilder sb = new StringBuilder();
        String baseRequest = " SELECT * FROM search_execution_info";
        sb.append(baseRequest);
        if(patientId > 0){
            // запрос всех обследований пациента, он без дополнительных параметров
            return String.format(Locale.ENGLISH, " SELECT * FROM search_execution_info WHERE patient_id = '%d';", patientId);
        }
        if(executionId != null && !executionId.isEmpty()){
            // запрос по номеру обследования
            sb.append(" WHERE search_execution_info.`execution_number` = \"");
            sb.append(executionId);
            sb.append("\"");
            haveParameter = true;
        }
        if(personals != null && !personals.isEmpty()){
            if(haveParameter){
                sb.append(" AND search_execution_info.personals LIKE \"%");
                sb.append(personals);
                sb.append("%\"");
            }
            else{
                haveParameter = true;
                sb.append(" WHERE search_execution_info.personals LIKE \"%");
                sb.append(personals);
                sb.append("%\"");
            }
        }
        if(text != null && !text.isEmpty()){
            if(haveParameter){
                sb.append(" AND MATCH (search_execution_info.text) AGAINST ('");
                sb.append(text);
                sb.append("')");
            }
            else{
                haveParameter = true;
                sb.append(" WHERE MATCH (search_execution_info.text) AGAINST ('");
                sb.append(text);
                sb.append("')");
            }
        }
        if(doctorName != null && !doctorName.isEmpty()){
            if(haveParameter){
                sb.append(" AND search_execution_info.diagnostician_name = \"");
                sb.append(doctorName);
                sb.append("\"");
            }
            else{
                haveParameter = true;
                sb.append(" WHERE search_execution_info.diagnostician_name = \"");
                sb.append(doctorName);
                sb.append("\"");
            }
        }
        if(executionArea != null && !executionArea.isEmpty()){
            if(haveParameter){
                sb.append(" AND search_execution_info.area_name = \"");
                sb.append(executionArea);
                sb.append("\"");
            }
            else{
                haveParameter = true;
                sb.append(" WHERE search_execution_info.area_name = \"");
                sb.append(executionArea);
                sb.append("\"");
            }
        }
        if(executionDate != null && !executionDate.isEmpty()){
            if(executionDateFinish.isEmpty() || executionDate.equals(executionDateFinish)){
                // если не заполнено значение конца периода или он совпадает с началом- ищу обследования за день
                if(haveParameter){
                    sb.append(" AND search_execution_info.execution_date = \"");
                    sb.append(executionDate);
                    sb.append("\"");
                }
                else{
                    haveParameter = true;
                    sb.append(" WHERE search_execution_info.execution_date = \"");
                    sb.append(executionDate);
                    sb.append("\"");
                }
            }
            else{
                // ищу обследования за период
                if(haveParameter){
                    sb.append(" AND search_execution_info.execution_date BETWEEN \"");
                    sb.append(executionDate);
                    sb.append("\"");
                    sb.append(" AND  \"");
                    sb.append(executionDateFinish);
                    sb.append("\"");
                }
                else{
                    haveParameter = true;
                    sb.append(" WHERE search_execution_info.execution_date BETWEEN  \"");
                    sb.append(executionDate);
                    sb.append("\"");
                    sb.append(" AND \"");
                    sb.append(executionDateFinish);
                    sb.append("\"");
                }
            }
        }
        if(haveParameter){
            sb.append(";");
            System.out.println(sb.toString());
            return sb.toString();
        }
        throw new IllegalArgumentException("Не переданы аргументы поиска");
    }

    public boolean haveParameters() {
        return
                (executionId != null && !executionId.isEmpty()) ||
                (personals != null && !personals.isEmpty())||
                (executionDate != null && !executionDate.isEmpty())||
                (text != null && !text.isEmpty())
                ;
    }


    public class Builder {

        private Builder() {
        }

        public SearchRequest build() {
            return SearchRequest.this;
        }

        public SearchRequest.Builder setExecutionId(String parameter) {
            // подготовлю запрос- заменю русскую А на английскую
            SearchRequest.this.executionId = StringsHandler.handleId(parameter);
            return this;
        }

        public SearchRequest.Builder setPatientId(int id) {
            patientId = id;
            return this;
        }

        public Builder setPersonals(String text) {
            SearchRequest.this.personals = text;
            return this;
        }

        public Builder setExecutionDate(String date){
            // дата проверена и отформатирована в контроллере
            SearchRequest.this.executionDate = date;
            return this;
        }

        public Builder setExecutionDateFinish(String date){
            // дата проверена и отформатирована в контроллере
            SearchRequest.this.executionDateFinish = date;
            return this;
        }

        public Builder setTest(String text) {
            SearchRequest.this.text = text;
            return this;
        }

        public Builder setDoctor(String value) {
            if(value != null && !value.isEmpty() && !value.equals("--")){
                SearchRequest.this.doctorName = value;
            }
            return this;
        }

        public Builder setExecutionArea(String value) {
            System.out.println("value is " + value);
            if(value != null && !value.isEmpty() && !value.equals("--")){
                SearchRequest.this.executionArea = value;
            }
            return this;
        }
    }
}

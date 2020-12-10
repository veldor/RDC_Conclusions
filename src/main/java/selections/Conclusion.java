package selections;

import models.handlers.FilesHandler;
import models.handlers.GrammarHandler;
import models.handlers.StringsHandler;

import java.text.ParseException;

public class Conclusion {

    private Conclusion() {
    }

    private String personals;
    private String birthDate;
    private String sex;
    private String executionArea;
    private String isContrast;
    private String executionNumber;
    private String executionDate;
    private String diagnostician;
    private String conclusionText;
    private String md5;
    private long changeTime = -1;
    private String fileName;
    private String path;


    public static Builder newBuilder(boolean skipWholenessCheck) {
        return new Conclusion().new Builder(skipWholenessCheck);
    }

    public boolean isFilled() {
        return
                personals != null && !personals.isEmpty() &&
                        birthDate != null && !birthDate.isEmpty() &&
                        sex != null && !sex.isEmpty() &&
                        executionArea != null && !executionArea.isEmpty() &&
                        isContrast != null && !isContrast.isEmpty() &&
                        executionNumber != null && !executionNumber.isEmpty() &&
                        executionDate != null && !executionDate.isEmpty() &&
                        diagnostician != null && !diagnostician.isEmpty() &&
                        conclusionText != null && !conclusionText.isEmpty() &&
                        md5 != null &&
                        changeTime != -1 &&
                        fileName != null &&
                        path != null;
    }

    /**
     * personals getter
     *
     * @return <p>personals</p>
     */
    public String getPersonals() {
        return personals;
    }


    /**
     * gender getter
     *
     * @return <p>Sex in enum male|female|unknown</p>
     */
    public String getSex() {
        return sex;
    }

    /**
     * birthDate getter
     *
     * @return <p>Birth date in format 01.01.01</p>
     */
    public String getBirthDate() {
        return birthDate;
    }

    public String getDiagnostician() {
        return diagnostician;
    }

    public String getExecutionArea() {
        return executionArea;
    }

    public String getContrast() {
        return isContrast;
    }

    public String getExecutionNumber() {
        return executionNumber;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getText() {
        return conclusionText;
    }

    public String getMd5() {
        return md5;
    }

    public long getChangeTime() {
        return changeTime;
    }

    public String getFileName() {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    public String getSkipperParameter() {
        if (personals.isEmpty()) {
            return "Не заполнены Ф.И.О. пациента";
        }
        if (birthDate.isEmpty()) {
            return "Не заполнена дата рождения пациента";
        }
        if (sex.isEmpty()) {
            return "Не заполнен пол пациента";
        }
        if (executionArea.isEmpty()) {
            return "Не заполнена область обследования пациента";
        }
        if (isContrast.isEmpty()) {
            return "Не заполнены сведения о контрастном веществе";
        }
        if (executionNumber.isEmpty()) {
            return "Не заполнен номер обследования";
        }
        if (executionDate.isEmpty()) {
            return "Не заполнена дата обследования";
        }
        if (diagnostician.isEmpty()) {
            return "Не заполнены сведения о докторе";
        }
        if (conclusionText.isEmpty()) {
            return "Нет текста заключения";
        }
        return "Не заполнено что-то, а я не понял, что :( Дайте знать разработчику";
    }

    public class Builder {

        private final boolean mSkipCheck;

        private Builder(boolean skipWholenessCheck) {
            mSkipCheck = skipWholenessCheck;
        }

        public Conclusion build() {
            if (!mSkipCheck) {
                // проверю, все ли параметры заполнены
                if (Conclusion.this.personals == null) {
                    throw new IllegalArgumentException("Не заполнены персональные данные");
                }
                if (Conclusion.this.birthDate == null) {
                    throw new IllegalArgumentException("Не заполнена дата рождения");
                }
                if (Conclusion.this.sex == null) {
                    throw new IllegalArgumentException("Не заполнен пол");
                }
                if (Conclusion.this.executionArea == null) {
                    throw new IllegalArgumentException("Не заполнена область обследования");
                }
                if (Conclusion.this.isContrast == null) {
                    throw new IllegalArgumentException("Не заполнены сведения о контрастном веществе");
                }
                if (Conclusion.this.executionNumber == null) {
                    throw new IllegalArgumentException("Не заполнен номер обследования");
                }
                if (Conclusion.this.executionDate == null) {
                    throw new IllegalArgumentException("Не заполнена дата обследования");
                }
                if (Conclusion.this.diagnostician == null) {
                    throw new IllegalArgumentException("Не заполнено имя врача");
                }
                if (Conclusion.this.conclusionText == null) {
                    throw new IllegalArgumentException("Не заполнен тект заключения");
                }
                if (Conclusion.this.md5 == null) {
                    throw new IllegalArgumentException("Не удалось посчитать md5");
                }
                if (Conclusion.this.changeTime < 0) {
                    throw new IllegalArgumentException("Неверное время последнего изменения файла");
                }
                if (Conclusion.this.fileName == null) {
                    throw new IllegalArgumentException("Пустое имя файла");
                }
                if (Conclusion.this.path == null) {
                    throw new IllegalArgumentException("Пуст путь к файлу");
                }
            }

            return Conclusion.this;
        }

        public void setPersonals(String parameter) {
            Conclusion.this.personals = parameter;
        }

        public void setBirthDate(String parameter) throws ParseException {
            if (mSkipCheck) {
                try {
                    Conclusion.this.birthDate = GrammarHandler.normalizeDateForDb(parameter);
                } catch (Exception e) {
                    System.out.println("Неверно введена дата рождения: " + parameter);
                }
            } else {
                Conclusion.this.birthDate = GrammarHandler.normalizeDateForDb(parameter);
            }
        }

        public void setSex(String parameter) {
            // тут всё непросто. Сначала обрежу лишнее
            parameter = StringsHandler.clearText(parameter);
            if (parameter.length() > 0) {
                if ("муж".equals(parameter)) {
                    Conclusion.this.sex = "male";
                } else if ("жен".equals(parameter)) {
                    Conclusion.this.sex = "female";
                } else {
                    Conclusion.this.sex = "unknown";
                }
            }
        }

        public void setExecutionArea(String parameter) {
            Conclusion.this.executionArea = StringsHandler.clearWhitespaces(parameter.toLowerCase().replaceAll("\\.", ""));
        }

        public void setContrast(String parameter) {
            Conclusion.this.isContrast = StringsHandler.clearWhitespaces(parameter);
        }

        public void setExecutionNumber(String parameter) {
            Conclusion.this.executionNumber = StringsHandler.handleId(StringsHandler.clearWhitespaces(parameter));
        }

        public void setExecutionDate(String parameter) throws ParseException {
            // тут буду перегонять дату в формат mysql
            if (mSkipCheck) {
                try {
                    Conclusion.this.executionDate = GrammarHandler.normalizeDateForDb(parameter);
                } catch (Exception e) {
                    System.out.println("Неверно введена дата обследования: " + parameter);
                }
            } else {
                Conclusion.this.executionDate = GrammarHandler.normalizeDateForDb(parameter);
            }
        }

        public boolean setDiagnostician(String parameter) {
            if(parameter != null && !parameter.isEmpty()){
                Conclusion.this.diagnostician = parameter;
                return true;
            }
            return false;
        }

        public Builder setText(String text) {
            Conclusion.this.conclusionText = text;
            return this;
        }

        public Builder setMd5(String text) {
            Conclusion.this.md5 = text;
            return this;
        }

        public void setChangeTime(long parameter) {
            Conclusion.this.changeTime = parameter;
        }

        public Builder setFileName(String parameter) {
            Conclusion.this.fileName = parameter;
            return this;
        }

        public Builder setPath(String parameter) {
            Conclusion.this.path = StringsHandler.addSlashes(parameter);
            return this;
        }

        /**
         * Проверю заполненность критичных данных.
         */
        public void checkParameterFilling() {
            // сначала- некритичные данные: пол, наличие контраста
            if (Conclusion.this.sex == null || Conclusion.this.sex.length() == 0) {
                Conclusion.this.sex = "unknown";
                FilesHandler.addToLog("В описании не обнаружена ссылка на пол: ", Conclusion.this.path);
            }
            if (Conclusion.this.isContrast == null || Conclusion.this.isContrast.length() == 0) {
                Conclusion.this.isContrast = "Не проводилось";
                FilesHandler.addToLog("В описании не обнаружена ссылка на введение контрастного вещества: ", Conclusion.this.path);
            }
        }
    }
}
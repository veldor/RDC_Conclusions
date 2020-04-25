package selections;

import models.handlers.StringsHandler;

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


    public static Builder newBuilder() {
        return new Conclusion().new Builder();
    }

    public boolean isFilled() {
        return personals != null && birthDate != null && sex != null && executionArea != null && isContrast != null && executionNumber != null && executionDate != null && diagnostician != null && conclusionText != null && md5 != null && changeTime != -1 && fileName != null && path != null;
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
        return StringsHandler.clearDate(birthDate);
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

    public class Builder {
        private Builder() {
        }

        public Conclusion build() {
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
                System.out.println(Conclusion.this.conclusionText);
                throw new IllegalArgumentException("Не заполнен номер обследования");
            }
            if (Conclusion.this.executionDate == null) {
                System.out.println(Conclusion.this.conclusionText);
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
            return Conclusion.this;
        }

        public void setPersonals(String parameter) {
            Conclusion.this.personals = parameter;
        }

        public void setBirthDate(String parameter) {
            // проверю, что дата рождения в правильном формате
            parameter = StringsHandler.clearDate(parameter);
            if(parameter.length() > 10){
                if(parameter.endsWith("г.")){
                    // обрежу год и пропробую ещё раз
                    parameter = parameter.substring(0, parameter.length() - 2).trim();
                    if(parameter.length() <= 10){
                        Conclusion.this.birthDate = parameter;
                        return;
                    }
                }
                System.out.println(Conclusion.this.conclusionText);
                throw new IllegalArgumentException("Некорректная дата рождения");
            }
            Conclusion.this.birthDate = parameter;
        }

        public void setSex(String parameter) {
            // тут всё непросто. Сначала обрежу лишнее
            parameter = StringsHandler.clearText(parameter);
            if (parameter.length() > 0) {
                if ("муж".equals(parameter)) {
                    //System.out.println("it's male");
                    Conclusion.this.sex = "male";
                } else if ("жен".equals(parameter)) {
                    //System.out.println("it's female");
                    Conclusion.this.sex = "female";
                } else {
                    throw new IllegalArgumentException("Пол не соответствует шаблону");
                }
            } else {
                throw new IllegalArgumentException("Пол не соответствует шаблону");
            }
        }

        public void setExecutionArea(String parameter) {
            Conclusion.this.executionArea = StringsHandler.clearWhitespaces(parameter.toLowerCase().replaceAll("\\.", ""));
        }

        public void setContrast(String parameter) {
            Conclusion.this.isContrast = StringsHandler.clearWhitespaces(parameter);
        }

        public void setExecutionNumber(String parameter) {
            Conclusion.this.executionNumber = StringsHandler.clearWhitespaces(parameter);
        }

        public void setExecutionDate(String parameter) {
            Conclusion.this.executionDate = parameter;
        }

        public void setDiagnostician(String parameter) {
            Conclusion.this.diagnostician = parameter;
        }

        public Builder setText(String text) {
            Conclusion.this.conclusionText = text;
            return this;
        }

        public Builder setMd5(String text) {
            Conclusion.this.md5 = text;
            return this;
        }

        public Builder setChangeTime(long parameter) {
            Conclusion.this.changeTime = parameter;
            return this;
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
                System.out.println("В описании не обнаружена ссылка на пол");
            }
            if (Conclusion.this.isContrast == null || Conclusion.this.isContrast.length() == 0) {
                Conclusion.this.isContrast = "Не проводилось";
                System.out.println("В описании не обнаружена ссылка на введение контрастного вещества");
            }
        }
    }
}
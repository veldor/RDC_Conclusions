package parser;

import models.handlers.StringsHandler;
import selections.Conclusion;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class InitialParser {


    private final Pattern sSignPattern;
    String patternString = "(\\d{1,2}[/|.]\\d{1,2}[/|.]\\d{2,4}).*[врач]?.+\\s([а-я]{2,})";
    LinesHandler mLinesHandler;
    Conclusion.Builder conclusionBuilder;
    boolean isDocFound;
    boolean isSexFound;

    InitialParser(){
        mLinesHandler = new LinesHandler();
        sSignPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    }

    class LinesHandler {

        private String mText;
        private boolean spendConclusion = false;

        public void loadTest(String text) {
            mText = text;
        }

        public void parse() {
            if (mText == null) {
                return;
            }
            // разобью текст по переносам строк
            String[] mStrings = mText.split("\n");
            for (String s : mStrings) {
                if (s.length() == 0) {
                    continue;
                }
                // разберу строку
                if (s.startsWith(WordParser.PERSONALS_START)) {
                    // строка соответствует шаблону персональных данных
                    conclusionBuilder.setPersonals(StringsHandler.getStringFrom(s, WordParser.PERSONAL_PREFIX_LENGTH));
                } else if (s.startsWith(WordParser.BIRTH_DATE_START)) {
                    // строка соответствует шаблону даты рождения
                    conclusionBuilder.setBirthDate(StringsHandler.getStringFrom(s, WordParser.BIRTH_DATE_PREFIX_LENGTH));
                } else if (!isSexFound && s.startsWith(WordParser.SEX_START)) {
                    conclusionBuilder.setSex(StringsHandler.getStringFrom(s, WordParser.SEX_PREFIX_LENGTH));
                } else if (s.startsWith(WordParser.CONTRAST_START)) {
                    // строка соответствует шаблону контраста
                    conclusionBuilder.setContrast(StringsHandler.getStringFrom(s, WordParser.CONTRAST_PREFIX_LENGTH));
                } else if (s.startsWith(WordParser.EXECUTION_AREA_START)) {
                    // строка соответствует шаблону области обследования
                    conclusionBuilder.setExecutionArea(StringsHandler.getStringFrom(s, WordParser.EXECUTION_AREA_PREFIX_LENGTH));
                } else if (s.startsWith(WordParser.EXECUTION_NUMBER_START)) {
                    // строка соответствует шаблону номера обследования
                    conclusionBuilder.setExecutionNumber(StringsHandler.superTrim(StringsHandler.getStringFrom(s, WordParser.EXECUTION_NUMBER_PREFIX_LENGTH)));
                } else if (s.trim().length() > 10 && s.trim().substring(0, 10).toLowerCase().equals(WordParser.CONCLUSION_START)) {
                    //System.out.println("found conclusion");
                    // если дошли до заключения- отмечу, что оно найдено, для проверки на врача
                    spendConclusion = true;
                } else if (spendConclusion) {
                    Matcher matcher = sSignPattern.matcher(s);
                    if (matcher.find()) {
                        // первая найденная группа- дата, тут всё просто, она уже отформатирована
                        conclusionBuilder.setExecutionDate(matcher.group(1));
                        // с доктором чуть сложнее- нам нужна только фамилия
                        if (!isDocFound) {
                            conclusionBuilder.setDiagnostician(matcher.group(2));
                        }
                    }
                }
            }
        }
    }
}

package models.handlers;

public class StringsHandler {

    private static String trim(String s) {
        return s.trim();
    }

    public static String getStringFrom(String s, int prefixLength) {
        return trim(s.substring(prefixLength));
    }


    /**
     * Удалю всё, кроме букв и переведу в нижний регистр
     *
     * @param s <p>Грязная строка</p>
     * @return <p>Чистая строка</p>
     */
    public static String clearText(String s) {
        return s.toLowerCase().replaceAll("[^а-я]", "");
    }

    /**
     * Верну преобразованную дату
     *
     * @param date <p>dirty date</p>
     * @return <p>clear date in format 01.01.2001</p>
     */
    public static String clearDate(String date) {
        return dropWhitespaces(date.replace("-", ".").replace("/", ".").trim());
    }

    private static String dropWhitespaces(String s) {
        return s.replaceAll("\\s", "");
    }

    public static String clearParagraph(String text) {
        return text.replace("FORMTEXT", "").replaceAll("\\p{C}", "").trim();
    }

    public static String superTrim(String s) {
        return s.replaceAll("\\s", "");
    }

    public static String cutDoc(String s) {
        if (s != null && s.length() > 0) {
            if (s.startsWith("к.м.н")) {
                return trim(s.substring(s.indexOf(" "), s.indexOf(" ", 7)));
            }
            return s.substring(0, s.indexOf(" "));
        }
        return null;
    }

    public static String clearWhitespaces(String s) {
        if(s != null){
            return s.replaceAll("\\s", " ").replaceAll("\\s+", " ").trim();
        }
        return null;
    }

    public static String addSlashes(String s) {
        return s.replace("\\", "\\\\");
    }

    public static String escapeQuotes(String text) {
        return text.replace("'", "\\'");
    }
}
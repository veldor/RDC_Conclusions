package parser;

import models.handlers.StringsHandler;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.xmlbeans.SimpleValue;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import selections.Conclusion;
import utils.Checksum;

import javax.xml.namespace.QName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DocxParser extends InitialParser{
    private static DocxParser instance;

    public static DocxParser getInstance() {
        if (instance == null) {
            instance = new DocxParser();
        }
        return instance;
    }

    private DocxParser() {
    }

    public Conclusion parse(File f) throws IOException {
        conclusionBuilder = Conclusion.newBuilder();
        conclusionBuilder.setFileName(f.getName()).setPath(f.getAbsolutePath()).setMd5(Checksum.checksum(f)).setChangeTime(f.lastModified());
        BufferedInputStream mInputStream = new BufferedInputStream(new FileInputStream(f));
        XWPFDocument mDocument = new XWPFDocument(mInputStream);
        XWPFWordExtractor extractor = new XWPFWordExtractor(mDocument);
        String value = extractor.getText();
        conclusionBuilder.setText(value);
        fillDataFromForms(mDocument);
        System.out.println(value);
        mLinesHandler.loadTest(value);
        mLinesHandler.parse();
        conclusionBuilder.checkParameterFilling();
        return conclusionBuilder.build();
    }


    /**
     * Берёт данные о поле пациента и враче из тела файла
     *
     * @param doc <p>Документ</p>
     */
    private void fillDataFromForms(XWPFDocument doc) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            // пройдусь по найденным параграфам
            for (XWPFRun run : paragraph.getRuns()) {
                // получу начало объекта через курсор
                XmlCursor cursor = run.getCTR().newCursor();
                cursor.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:fldChar/@w:fldCharType");
                while (cursor.hasNextSelection()) {
                    cursor.toNextSelection();
                    XmlObject obj = cursor.getObject();
                    if ("begin".equals(((SimpleValue) obj).getStringValue())) {
                        // если элемент начинается с "begin"
                        cursor.toParent();
                        obj = cursor.getObject();
                        if (obj != null) {
                            XmlObject[] forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:name/@w:val");
                            if (forms.length > 0) {
                                // проверю имя найденной формы
                                String formName = ((SimpleValue) forms[0]).getStringValue();
                                if (formName != null) {
                                    if (formName.equals("r_patsex")) {
                                        // проверю выбранное значение
                                        forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:calcOnExit/@w:val");
                                        if (forms.length > 0) {
                                            String selection = ((SimpleValue) forms[0]).getStringValue();
                                            // теперь, если уж полезли в дебри, получу пол
                                            forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:listEntry");
                                            if (forms.length > 0) {
                                                // получу выбранный элемент
                                                XmlObject element = forms[Integer.parseInt(selection)];
                                                System.out.println(element.toString());
                                                String formValue = ((SimpleValue) element.selectAttribute(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"))).getStringValue();
                                                // ура, нашёл пол пациента
                                                conclusionBuilder.setSex(formValue);
                                                isSexFound = true;
                                            }
                                        }
                                    } else if(formName.equals("")) {
                                        forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:result/@w:val");
                                        if(forms.length > 0){
                                            String selection = ((SimpleValue) forms[0]).getStringValue();
                                            forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:listEntry");
                                            if(forms.length > 0){
                                                // получу выбранный элемент
                                                XmlObject element = forms[Integer.parseInt(selection)];
                                                String formValue = ((SimpleValue) element.selectAttribute(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"))).getStringValue();
                                                // ура, нашёл доктора
                                                conclusionBuilder.setDiagnostician(StringsHandler.cutDoc(formValue));
                                                isDocFound = true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}

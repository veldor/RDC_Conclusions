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
import java.text.ParseException;

public class DocxParser extends InitialParser {
    private FileInputStream fis;
    private BufferedInputStream mInputStream;
    private XWPFWordExtractor extractor;
    private XWPFDocument mDocument;

    public Conclusion parse(File f, boolean skipWholenessCheck) throws IOException, ParseException {
        sKokuninFormat = false;
        isDocFound = false;
        isSexFound = false;
        spendConclusion = false;
        try{
            conclusionBuilder = Conclusion.newBuilder(skipWholenessCheck);
            conclusionBuilder.setFileName(f.getName()).setPath(f.getAbsolutePath()).setMd5(Checksum.checksum(f)).setChangeTime(f.lastModified());
            fis = new FileInputStream(f);
            mInputStream = new BufferedInputStream(fis);
            mDocument = new XWPFDocument(mInputStream);
            extractor = new XWPFWordExtractor(mDocument);
            String value = extractor.getText();
            conclusionBuilder.setText(value);
            fillDataFromForms(mDocument);
            mLinesHandler.loadText(value);
            mLinesHandler.parse();
            if (!skipWholenessCheck) {
                conclusionBuilder.checkParameterFilling();
            }
        }
        finally {
            if(fis != null){
                fis.close();
            }
            if(mInputStream != null){
                mInputStream.close();
            }
            if(extractor != null){
                extractor.close();
            }
            if(mDocument != null){
                mDocument.close();
            }
        }
        return conclusionBuilder.build();
    }


    /**
     * Берёт данные о поле пациента и враче из тела файла
     *
     * @param doc <p>Документ</p>
     */
    private void fillDataFromForms(XWPFDocument doc) {
        isDocFound = false;
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
                                    String selection;
                                    String formValue;
                                    XmlObject element;
                                    if (formName.equals("r_patsex")) {
                                        // проверю выбранное значение
                                        forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:calcOnExit/@w:val");
                                        if (forms.length > 0) {
                                            selection = ((SimpleValue) forms[0]).getStringValue();
                                            // теперь, если уж полезли в дебри, получу пол
                                            forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:listEntry");
                                            if (forms.length > 0) {
                                                // получу выбранный элемент
                                                element = forms[Integer.parseInt(selection)];
                                                formValue = ((SimpleValue) element.selectAttribute(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"))).getStringValue();
                                                // ура, нашёл пол пациента
                                                conclusionBuilder.setSex(formValue);
                                                isSexFound = true;
                                            }
                                        }
                                    } else if (formName.equals("")) {
                                        forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:result/@w:val");
                                        if (forms.length > 0) {
                                            selection = ((SimpleValue) forms[0]).getStringValue();
                                            forms = obj.selectPath("declare namespace w='http://schemas.openxmlformats.org/wordprocessingml/2006/main' .//w:ffData/w:ddList/w:listEntry");
                                            if (forms.length > 0) {
                                                // получу выбранный элемент
                                                element = forms[Integer.parseInt(selection)];
                                                formValue = ((SimpleValue) element.selectAttribute(new QName("http://schemas.openxmlformats.org/wordprocessingml/2006/main", "val"))).getStringValue();
                                                // ура, нашёл доктора
                                                if(formValue != null && !formValue.isEmpty() && conclusionBuilder.setDiagnostician(StringsHandler.cutDoc(formValue))){
                                                    isDocFound = true;
                                                }
                                            }
                                        }
                                        if(!isDocFound){
                                            conclusionBuilder.setDiagnostician(InitialParser.DOC_NOT_SELECTED);
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

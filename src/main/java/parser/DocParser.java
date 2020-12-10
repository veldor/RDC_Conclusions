package parser;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import models.handlers.StringsHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import selections.Conclusion;
import utils.Checksum;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;

public class DocParser extends InitialParser {

    private final SAXParserFactory factory;
    private final SAXParser mParser;
    private final XMLHandler mHandler;
    Document doc;
    private FileInputStream fis;
    private String mText;
    private StringReader stringReader;
    private InputSource inputSource;
    private String value;

    DocParser() throws ParserConfigurationException, SAXException {
        factory = SAXParserFactory.newInstance();
        mParser = factory.newSAXParser();
        mHandler = new XMLHandler();
        mLinesHandler = new LinesHandler();
    }

    public Conclusion parse(File f, boolean skipWholenessCheck) throws Exception {
        sKokuninFormat = false;
        isDocFound = false;
        isSexFound = false;
        spendConclusion = false;
        try{
            conclusionBuilder = Conclusion.newBuilder(skipWholenessCheck);
            conclusionBuilder.setFileName(f.getName()).setPath(f.getAbsolutePath()).setMd5(Checksum.checksum(f)).setChangeTime(f.lastModified());
            fis = new FileInputStream(f);
            doc = new Document(fis);
            mText = doc.toString(SaveFormat.HTML);
            stringReader = new StringReader(mText);
            inputSource = new InputSource(stringReader);
            mText = doc.toString(SaveFormat.TEXT);
            conclusionBuilder.setText(mText);
            mLinesHandler.loadText(mText);
            // обработаю XML, найдя при этом пол пациента и имя врача, так как они в select
            // и схлопываются во время поиска по тексту
            mParser.parse(inputSource, mHandler);
            // теперь найду остальные параметры прохождением по строкам
            mLinesHandler.parse();
            if(!skipWholenessCheck){
                conclusionBuilder.checkParameterFilling();
            }
        }
        finally {
            if(fis != null){
                fis.close();
            }
            if(stringReader != null){
                stringReader.close();
            }

        }
        return conclusionBuilder.build();
    }

    private class XMLHandler extends DefaultHandler {

        private boolean mSelectFound = false;
        private boolean mOptionFound = false;
        private boolean mDocFound = false;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if (qName.equals("select")) {
                mSelectFound = true;
                // если имя пустое- значит, найден врач
                if (attributes.getValue("name").equals("")) {
                    mDocFound = true;
                }
            }
            if (mSelectFound && qName.equals("option") && attributes.getValue("selected") != null) {
                mOptionFound = true;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            // Тут будет логика реакции на конец элемента
            if (qName.equals("select")) {
                if(mDocFound && ! mOptionFound){
                    DocParser.this.conclusionBuilder.setDiagnostician(DOC_NOT_SELECTED);
                    DocParser.this.isDocFound = true;
                }
                mSelectFound = false;
                mDocFound = false;
            }
            if (qName.equals("option")) {
                mOptionFound = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            if (mOptionFound) {
                value = new String(ch, start, length);
                if (mDocFound) {
                    // внесу данные о докторе
                    DocParser.this.conclusionBuilder.setDiagnostician(StringsHandler.cutDoc(value));
                    DocParser.this.isDocFound = true;
                } else {
                    DocParser.this.conclusionBuilder.setSex(value);
                    DocParser.this.isSexFound = true;
                }
            }
        }
    }
}

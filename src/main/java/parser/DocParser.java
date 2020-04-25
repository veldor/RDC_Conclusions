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

public class DocParser  extends InitialParser{

    private final SAXParser mParser;
    private final XMLHandler mHandler;
    Document doc;

    private static DocParser instance;

    public static DocParser getInstance() throws ParserConfigurationException, SAXException {
        if (instance == null) {
            instance = new DocParser();
        }
        return instance;
    }

    private DocParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        mParser = factory.newSAXParser();
        mHandler = new XMLHandler();
        mLinesHandler = new LinesHandler();
    }

    public Conclusion parse(File f) throws Exception {
        isDocFound = false;
        isSexFound = false;
        conclusionBuilder = Conclusion.newBuilder();
        conclusionBuilder.setFileName(f.getName()).setPath(f.getAbsolutePath()).setMd5(Checksum.checksum(f)).setChangeTime(f.lastModified());
        doc = new Document(new FileInputStream(f));
        String mText = doc.toString(SaveFormat.HTML);
        // обработаю XML, найдя при этом пол пациента и имя врача, так как они в select
        // и схлопываются во время поиска по тексту
        mParser.parse(new InputSource(new StringReader(mText)), mHandler);
        mText = doc.toString(SaveFormat.TEXT);
        conclusionBuilder.setText(mText);
        // теперь найду остальные параметры прохождением по строкам
        mLinesHandler.loadTest(mText);
        mLinesHandler.parse();
        conclusionBuilder.checkParameterFilling();
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
                if (mDocFound) {
                    // внесу данные о докторе
                    DocParser.this.conclusionBuilder.setDiagnostician(StringsHandler.cutDoc(new String(ch, start, length)));
                    DocParser.this.isDocFound = true;
                } else {
                    DocParser.this.conclusionBuilder.setSex(new String(ch, start, length));
                    DocParser.this.isSexFound = true;
                }
            }
        }
    }
}

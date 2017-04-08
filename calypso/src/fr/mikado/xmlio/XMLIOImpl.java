package fr.mikado.xmlio;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class XMLIOImpl implements XMLIOInterface {

    @Override
    public Document loadDocument(String filename) {
        SAXBuilder sax = new SAXBuilder();
        Document doc;
        try {
            doc = sax.build(new File(filename));
        } catch (JDOMException e) {
            Logger.getGlobal().severe("Error while parsing file \""+filename+"\"");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Logger.getGlobal().severe("Cannot read file \""+filename+"\"");
            return null;
        }
        return doc;
    }

    @Override
    public boolean writeDocument(Document doc, String filename) {
        XMLOutputter outputter = new XMLOutputter();
        outputter.setFormat(Format.getPrettyFormat());
        try {
            outputter.output(doc, new FileWriter(new File(filename)));
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}

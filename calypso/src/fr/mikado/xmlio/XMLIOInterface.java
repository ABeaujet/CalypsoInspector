package fr.mikado.xmlio;

import org.jdom2.Document;

import java.io.IOException;

public interface XMLIOInterface {
    public Document loadDocument(String filename) throws IOException;
    public boolean writeDocument(Document doc, String filename) throws IOException;
}

package fr.mikado.calypso;

import fr.mikado.xmlio.XMLIOImpl;
import fr.mikado.xmlio.XMLIOInterface;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.*;
import java.util.ArrayList;

import static fr.mikado.calypso.CalypsoFile.CalypsoFileType.EF;

public class CalypsoRawDump {

    public static class CalypsoFileDump implements Serializable {
        private String fileName;
        private ArrayList<CalypsoFileDump> children;
        private ArrayList<byte[]> records;
        public CalypsoFileDump(){
            this.records = new ArrayList<>();
            this.children = new ArrayList<>();
        }
        public CalypsoFileDump(Element e){
            this.fileName = e.getAttributeValue("filename");
            this.records = new ArrayList<>();
            this.children = new ArrayList<>();

            this.fromXMLElement(this, e);
        }
        public void fromXMLElement(CalypsoFileDump fd, Element e){
            this.fileName = e.getAttributeValue("filename");
            if(!e.getChildren().isEmpty()) {
                for (Element re : e.getChildren("record"))
                    fd.records.add(BitArray.hex2Bytes(re.getValue()));
                for(Element fe : e.getChildren("file")) {
                    CalypsoFileDump child = new CalypsoFileDump();
                    child.fromXMLElement(child, fe);
                    fd.children.add(child);
                }
            }
        }
        private Element getXmlElement(){
            Element e = new Element("file");
            e.setAttribute("filename", this.fileName);
            if(this.children.isEmpty()){ // add the record contents
                for(byte[] rec : this.records) {
                    Element recElem = new Element("record");
                    StringBuilder sb = new StringBuilder();
                    for(byte b : rec)
                        sb.append(String.format("%02x", b));
                    recElem.addContent(sb.toString());
                    e.addContent(recElem);
                }
            }else { // add the embedded files
                for (CalypsoFileDump fd : this.children)
                    e.addContent(fd.getXmlElement());
            }
            return e;
        }
    }

    protected final CalypsoEnvironment env;
    protected ArrayList<CalypsoFileDump> dump;

    public CalypsoRawDump(CalypsoEnvironment env){
        this.env = env;
        getRawDump();
    }

    public CalypsoFileDump getFileDump(CalypsoFile f){
        CalypsoFileDump fd = new CalypsoFileDump();
        fd.fileName = f.getDescription();
        if(f.getType() == EF)
            fd.records = f.getRawRecords();
        else
            for(CalypsoFile ff : f.getChildren())
                fd.children.add(getFileDump(ff));
        return fd;
    }

    public ArrayList<CalypsoFileDump> getRawDump(){
        this.dump = new ArrayList<>();

        for(CalypsoFile f : this.env.getFiles())
            this.dump.add(getFileDump(f));

        return this.dump;
    }

    public Document getXMLDump(){
        Element root = new Element("calypsoDump");

        ArrayList<CalypsoFileDump> dump = this.getRawDump();
        for(CalypsoFileDump fd : dump)
            root.addContent(fd.getXmlElement());
        return new Document(root);
    }

    public void writeXML(XMLIOInterface xmlio, String filename) throws IOException {
        xmlio.writeDocument(this.getXMLDump(), filename);
    }

    public void loadXML(XMLIOInterface xmlio, String filename) throws IOException {
        Document dump = xmlio.loadDocument(filename);
        Element root = dump.getRootElement();

        this.dump = new ArrayList<>();
        for(Element f : root.getChildren("file"))
            this.dump.add(new CalypsoFileDump(f));
    }

    private String indent(int level){
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<level;i++)
            sb.append(" ");
        return sb.toString();
    }

    private void debugPrintFile(CalypsoFileDump fd, int i){
        System.out.println(indent(i) + "File " + fd.fileName + " :");
        for(byte[] bytes : fd.records)
            System.out.println(indent(i+4) + "Record : " + BitArray.bytes2Hex(bytes));
        for(CalypsoFileDump child : fd.children)
            debugPrintFile(child, i+4);
    }

    public void debugPrint(){
        for(CalypsoFileDump fd : this.dump)
            debugPrintFile(fd, 0);
    }

}






































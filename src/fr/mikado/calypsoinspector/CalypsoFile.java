package fr.mikado.calypsoinspector;

import com.sun.istack.internal.Nullable;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * This class describes a Calypso File.
 * A file has a structure (fields) which describes how each records for this file are structured.
 * CalypsoFile structures can (and should) be loaded from an XML file.
 */
public class CalypsoFile {
    public String getDescription() {
        return description;
    }

    public enum CalypsoFileType{
        MF, DF, EF
    }

    private String identifier;
    private String description;
    private CalypsoFile parent;
    private ArrayList<CalypsoFile> children;
    private CalypsoFileType type;

    private ArrayList<CalypsoRecordField> fields;
    private ArrayList<CalypsoRecord> records;

    /**
     * Creates a Calypso File from an XML node.
     * @param e An XML node
     * @param env Current Calypso Environment (nullable)
     */
    public CalypsoFile(Element e, @Nullable CalypsoEnvironment env){
        this.children = new ArrayList<>();
        this.records = new ArrayList<>();
        this.identifier = e.getAttributeValue("identifier");
        this.description = e.getAttributeValue("description");
        this.type = (e.getAttributeValue("type").equals("DF") ? CalypsoFileType.DF : CalypsoFileType.EF);

        if(this.type == CalypsoFileType.EF) {
            this.fields = new ArrayList<>();
            for (Element ee : e.getChildren())
                this.fields.add(new CalypsoRecordField(ee, env));
        }
    }

    public String getFullPath(){
        if(this.parent != null)
            return this.parent.getFullPath() + "/" + this.identifier;
        return this.identifier;
    }

    public void addChild(CalypsoFile cf){
        this.children.add(cf);
        cf.setParent(this);
    }

    public void setParent(CalypsoFile f){
        this.parent = f;
    }

    /**
     * Creates a new record for this file, from the byte buffer received in the READ RECORD APDU response.
     * @param buffer Byte buffer in the READ RECORD APDU response.
     */
    public void newRecord(byte[] buffer){
        CalypsoRecord r = new CalypsoRecord(this.fields, this);
        r.fillRecord(buffer);
        this.records.add(r);
    }

    public static String nesting(int level){
        StringBuilder r = new StringBuilder();
        for (int j = 0; j < level; j++)
            r.append("    ");
        return r.toString();
    }

    public void dumpStructure(int n){
        System.out.println(nesting(n)+"File " + this.getFullPath() + " : " + this.description);
        if(this.type == CalypsoFileType.EF)
            for(CalypsoRecordField f : this.fields)
                f.print(n+1);
        else
            for(CalypsoFile c : this.children)
                c.dumpStructure(n+1);
    }

    public void dump(int n){
        System.out.println(nesting(n)+"File " + this.getFullPath() + " " + this.description + " :");
        if(this.type == CalypsoFileType.DF || this.type == CalypsoFileType.MF)
            for (CalypsoFile c : this.children)
                c.dump(n+1);
        else
            for(int i =0;i<this.records.size();i++){
                System.out.println(nesting(n+1)+"Record #"+i);
                this.records.get(i).print(n+1);
            }
    }

    public String getIdentifier(){
        return this.identifier;
    }
    public CalypsoFileType getType(){
        return this.type;
    }
    public ArrayList<CalypsoFile> getChildren(){
        return this.children;
    }
    public ArrayList<CalypsoRecord> getRecords() {
        return records;
    }

}

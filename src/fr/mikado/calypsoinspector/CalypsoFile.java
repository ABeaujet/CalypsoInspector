package fr.mikado.calypsoinspector;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.logging.Logger;

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

    public CalypsoFile(String identifier, CalypsoFile parent){
        this.identifier = identifier;
        this.parent = parent;
    }

    public CalypsoFile(Element e, CalypsoEnvironment env){
        this.children = new ArrayList<>();
        this.records = new ArrayList<>();
        this.identifier = e.getAttributeValue("identifier");
        this.description = e.getAttributeValue("description");
        this.type = (e.getAttributeValue("type").equals("DF") ? CalypsoFileType.DF : CalypsoFileType.EF);
        //Logger.getGlobal().info("Creating file : " + identifier);

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

    public void newRecord(byte[] buffer){
        /*
        System.out.println("RECORD FOR FILE " + this.getFullPath() + " (" + this.getDescription() + ") :");
        System.out.println(CalypsoCard.bytes2Hex(buffer));
        System.out.println("END RECORD.");
        */
        CalypsoRecord r = new CalypsoRecord(this.fields, this.description, this.description);
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

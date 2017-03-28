package fr.mikado.calypsoinspector;

import com.sun.istack.internal.Nullable;
import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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

    private Integer LFI;
    private Integer SFI;
    private String description;
    private CalypsoFile parent;
    private ArrayList<CalypsoFile> children;
    private CalypsoFileType type;

    private HashMap<String, ArrayList<CalypsoRecordField>> fileMappings; // available mappings, by name
    private HashMap<Integer, ArrayList<CalypsoRecordField>> mappings; // mapping for each record, ordered
    private ArrayList<CalypsoRecordField> activeMapping; // mapping for the next record
    private ArrayList<CalypsoRecord> records;
    private boolean isDefaultMappingLoaded;

    /**
     * Creates a Calypso File from an XML node.
     * @param e An XML node
     * @param env Current Calypso Environment (nullable)
     */
    public CalypsoFile(Element e, @Nullable CalypsoEnvironment env){
        this.children = new ArrayList<>();
        this.records = new ArrayList<>();

        this.fileMappings = new HashMap<>();
        this.mappings = new HashMap<>();
        this.activeMapping = null;
        this.isDefaultMappingLoaded = false;

        String LFIStr = e.getAttributeValue("LFI");
        if(LFIStr != null)
            this.LFI = Integer.parseInt(LFIStr, 16);
        String SFIStr = e.getAttributeValue("SFI");
        if(SFIStr != null)
            this.SFI = Integer.parseInt(SFIStr, 16);

        this.description = e.getAttributeValue("description");
        this.type = (e.getAttributeValue("type").equals("DF") ? CalypsoFileType.DF : CalypsoFileType.EF);

        String mappingsAttr = e.getAttributeValue("multipleMappings");
        if(mappingsAttr != null && (mappingsAttr.equals("yes") || mappingsAttr.equals("true")))
            for(Element ee : e.getChildren()) {
                ArrayList<CalypsoRecordField> mapping = parseFileStructure(ee, env);
                this.fileMappings.put(ee.getAttributeValue("name"), mapping);
                String isDefaultMappingStr = ee.getAttributeValue("default");
                if (isDefaultMappingStr != null && (isDefaultMappingStr.equals("yes") || isDefaultMappingStr.equals("true"))) {
                    this.activeMapping = mapping;
                    isDefaultMappingLoaded = true;
                }
            }
        else
            if (this.type == CalypsoFileType.EF)
                this.activeMapping = parseFileStructure(e, env);
    }

    private ArrayList<CalypsoRecordField> parseFileStructure(Element e, CalypsoEnvironment env){
        ArrayList<CalypsoRecordField> fields = new ArrayList<>();
        for (Element ee : e.getChildren())
            fields.add(new CalypsoRecordField(ee, env));
        return fields;
    }

    public String getFullPath(){
        if(this.parent != null)
            return this.parent.getFullPath() + "/" + this.getIdentifier();
        return this.getIdentifier();
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
        if(this.mappings.size() > this.records.size())
            this.activeMapping = new ArrayList<ArrayList>(this.mappings.values()).get(this.records.size());

        CalypsoRecord r = new CalypsoRecord(this.activeMapping, this);
        r.fillRecord(buffer);
        for(Map.Entry<String, ArrayList<CalypsoRecordField>> entry : this.fileMappings.entrySet())
            if(entry.getValue() == this.activeMapping) {
                r.setMappingName(entry.getKey());
                break;
            }
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
            for(Map.Entry<String, ArrayList<CalypsoRecordField>> mapping : this.fileMappings.entrySet()) {
                System.out.println(nesting(n+1) + "File mapping \"" + mapping.getKey() + "\" :");
                for (CalypsoRecordField f : mapping.getValue())
                    f.print(n + 2);
            }
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
        if(this.LFI != null)
            return Integer.toHexString(this.LFI);
        if(this.SFI != null)
        return Integer.toHexString(this.SFI);
        return "NO_ID";
    }
    public boolean isSFIAddressable(){
        return this.SFI != null;
    }
    public Integer getSFI(){
        return this.SFI;
    }
    public Integer getLFI(){
        return this.LFI;
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

    public boolean addFileMapping(int index, String mappingName){
        ArrayList<CalypsoRecordField> mapping = this.fileMappings.get(mappingName);
        if(mapping == null)
            return false;
        if(isDefaultMappingLoaded) {
            this.mappings.clear();
            isDefaultMappingLoaded = false;
        }
        this.mappings.put(index, mapping);
        return true;
    }
}
































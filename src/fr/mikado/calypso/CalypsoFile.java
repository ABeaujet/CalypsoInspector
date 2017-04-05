package fr.mikado.calypso;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    public class MappingTuple<X, Y> {
        public X recordId;
        public Y mappingName;
        public MappingTuple(X recordId, Y mappingName){
            this.recordId = recordId;
            this.mappingName = mappingName;
        }
    }

    private ArrayList<Integer> LFIs;
    private Integer SFI;
    private String description;
    private CalypsoFile parent;
    private ArrayList<CalypsoFile> children;
    private CalypsoFileType type;

    private HashMap<String, ArrayList<CalypsoRecordField>> fileMappings; // available mappings, by name
    private ArrayList<MappingTuple<Integer, String>> mappings; // mapping names for each record, card ordered is preserved
    private ArrayList<CalypsoRecordField> activeMapping; // mapping for the next record
    private ArrayList<CalypsoRecord> records;
    private boolean isDefaultMappingLoaded;

    /**
     * Creates a Calypso File from an XML node.
     * @param e An XML node
     * @param env Current Calypso Environment
     */
    public CalypsoFile(Element e, CalypsoEnvironment env){
        this.children = new ArrayList<>();
        this.records = new ArrayList<>();
        this.LFIs = new ArrayList<>();

        this.fileMappings = new HashMap<>();
        this.mappings = new ArrayList<>();
        this.activeMapping = null;
        this.isDefaultMappingLoaded = false;

        String LFIStr = e.getAttributeValue("LFI");
        if(LFIStr != null)
            for(String LFIe : LFIStr.split(";"))
                    this.LFIs.add(Integer.parseInt(LFIe, 16));

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
        String activeMappingName = "";
        if(this.mappings.size() > 0)
            if(this.mappings.size() > this.records.size()){
                // pour HashMap<Integer, String> : activeMappingName = this.mappings.get(new ArrayList<>(this.mappings.keySet()).get(this.records.size()));
                MappingTuple<Integer, String> mapping = this.mappings.get(this.records.size());
                activeMappingName = mapping.mappingName;

                this.activeMapping = this.fileMappings.get(activeMappingName);
            }
        if(this.activeMapping == null){
            System.out.println("Oops.");
        }

        CalypsoRecord r = new CalypsoRecord(this.activeMapping, this);
        r.fillRecord(buffer);
        r.setMappingName(activeMappingName);
        if(!r.isEmpty())
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
                    f.print(n + 2, true);
            }
        else
            for(CalypsoFile c : this.children)
                c.dumpStructure(n+1);
    }

    public void dump(int n, boolean debug){
        System.out.println(nesting(n)+"File " + this.getFullPath() + " " + this.description + " :");
        if(this.type == CalypsoFileType.DF || this.type == CalypsoFileType.MF)
            for (CalypsoFile c : this.children)
                c.dump(n+1, debug);
        else
            if(this.records.size() == 0)
                System.out.println(nesting(n+1)+"(empty)");
            else
                for(int i =0;i<this.records.size();i++){
                    System.out.println(nesting(n+1)+"Record #"+i);
                    this.records.get(i).print(n+1, debug);
                }
    }

    public String getIdentifier(){
        if(this.LFIs.size() > 0) {
            StringBuilder sb = new StringBuilder();
            boolean firstInLine = true;
            for(Integer LFI : this.LFIs) {
                sb.append( (firstInLine ? "":",") + Integer.toHexString(LFI));
                firstInLine = false;
            }
            return sb.toString();
        }
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
    public ArrayList<Integer> getLFIs(){
        return this.LFIs;
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
        //this.mappings.put(index, mappingName);
        this.mappings.add(new MappingTuple<Integer, String>(index, mappingName));
        return true;
    }
}
































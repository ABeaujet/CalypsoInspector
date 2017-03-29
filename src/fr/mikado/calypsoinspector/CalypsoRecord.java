package fr.mikado.calypsoinspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * This class describes a Calypso Record.
 * A record is basically an array of record fields that are filled from a byte buffer.
 * Send read record APDU, get the response byte buffer, fill the records fields using fillRecord(byte[] buffer);
 */
public class CalypsoRecord {
    BitArray bits;
    CalypsoFile parent;
    int id; // record ID (index in file.getRecords())
    String mappingName;
    ArrayList<CalypsoRecordField> fields;
    HashMap<String, CalypsoRecordField> fieldsByName;

    public CalypsoRecord(ArrayList<CalypsoRecordField> fields, CalypsoFile parent){
        this.parent = parent;
        this.id = parent.getRecords().size();
        this.fields = new ArrayList<>();
        this.fieldsByName = new HashMap<>();
        for(CalypsoRecordField r : fields) {
            CalypsoRecordField field = new CalypsoRecordField(r);
            field.setParentRecord(this);
            this.fields.add(field);
            this.fieldsByName.put(field.getDescription(), field);
        }
    }

    public boolean fillRecord(byte[] buffer){
        int offset = 0; // en bits !
        this.bits = new BitArray(buffer);
        try{
            for(CalypsoRecordField f : this.fields)
                offset += f.fill(buffer, offset);
        }catch(IndexOutOfBoundsException e){
            Logger.getGlobal().severe("Attempting to parse data outside of record !");
            return false;
        }
        return true;
    }

    public void print(int n, boolean debug){
        if(debug) {
            System.out.println(CalypsoFile.nesting(n) + "Raw contents : " + this.getBitsAsHex());
            if (this.mappingName != null)
                System.out.println(CalypsoFile.nesting(n) + "Mapping name : " + this.getMappingName());
        }
        for(CalypsoRecordField f : this.fields)
            f.print(n+1, debug);
    }

    public CalypsoRecordField getRecordField(String description){
        return this.fieldsByName.containsKey(description) ? this.fieldsByName.get(description) : null;
    }

    public ArrayList<CalypsoRecordField> getFields(){
        return this.fields;
    }

    public String getBitsAsHex(){
        return this.bits.toHex();
    }

    public BitArray getBits(){
        return this.bits;
    }

    public CalypsoFile getParent(){
        return this.parent;
    }

    public int getId(){
        return this.id;
    }

    public void setMappingName(String mappingName){
        this.mappingName = mappingName;
    }

    public String getMappingName(){
        return this.mappingName;
    }

    public boolean isEmpty(){
        for(CalypsoRecordField rf : this.fields)
            if(rf.isFilled())
                return false;
        return true;
    }
}


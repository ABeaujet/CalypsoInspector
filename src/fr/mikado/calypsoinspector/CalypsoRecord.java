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
        //try{
            for(CalypsoRecordField f : this.fields) {
                System.out.println(f.getDescription() + " - Offset : " + offset);
                offset += f.fill(buffer, offset);
            }
        /*
        }catch(IndexOutOfBoundsException e){
            Logger.getGlobal().severe("Attempting to parse data outside of record !");
            return false;
        }*/
        return true;
    }

    public void print(int n){
        System.out.println(CalypsoFile.nesting(n) + "Raw contents : " + this.getBitsAsHex());
        for(CalypsoRecordField f : this.fields)
            f.print(n+1);
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

}


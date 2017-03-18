package fr.mikado.calypsoinspector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CalypsoRecord {
    ArrayList<CalypsoRecordField> fields;
    HashMap<String, CalypsoRecordField> fieldsByName;

    public CalypsoRecord(ArrayList<CalypsoRecordField> fields, String name, String description){
        this.fields = new ArrayList<>();
        this.fieldsByName = new HashMap<>();
        for(CalypsoRecordField r : fields) {
            CalypsoRecordField field = new CalypsoRecordField(r);
            this.fields.add(field);
            this.fieldsByName.put(field.getDescription(), field);
        }
    }

    public boolean fillRecord(byte[] buffer){
        int offset = 0; // en bits !
        try{
            for(CalypsoRecordField f : this.fields)
                offset += f.fill(buffer, offset);
        }catch(IndexOutOfBoundsException e){
            Logger.getGlobal().severe("Attempting to parse data outside of record !");
            return false;
        }
        return true;
    }

    public void print(int n){
        for(CalypsoRecordField f : this.fields)
            f.print(n+1);
    }

    public CalypsoRecordField getRecordField(String description){
        return this.fieldsByName.containsKey(description) ? this.fieldsByName.get(description) : null;
    }

}


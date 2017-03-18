package fr.mikado.calypsoinspector;

import org.jdom2.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static fr.mikado.calypsoinspector.CalypsoRecordField.FieldType.*;

public class CalypsoRecordField {

    enum FieldType {
        Bitmap, Pointer, Date, Time, DateTime, Amount, Number, NetworkId, BcdDate, String, Repeat, Route, Stop, Vehicle, Direction, PayMethod, Undefined;
        public static String[] n = {"Bitmap", "Pointer", "Date", "Time", "DateTime", "Amount", "Number", "NetworkId", "BcdDate", "String", "Repeat", "Route", "Stop", "Vehicle", "Direction", "PayMethod", "Undefined"};
        public static int c= 0;
        private int cc= 0;
        FieldType(){ this.cc = FieldType.getC(); }
        static int getC(){ return c++;}
        public String getTypeName(){
            return n[this.cc];
        }
    }

    private static HashMap<Integer, String> payMethods;
    static{ payMethods = new HashMap<>();
            payMethods.put(128, "Débit PME");
            payMethods.put(144, "Espèce");
            payMethods.put(160, "Chèque Mobilités");
            payMethods.put(164, "Chèque");
            payMethods.put(165, "Chèque vacances");
            payMethods.put(179, "Carte de paiement");
            payMethods.put(183, "Télépaiement");
            payMethods.put(208, "Télérèglement");
            payMethods.put(215, "Bon de caisse");
            payMethods.put(217, "Bon de réduction");
    }

    private CalypsoEnvironment env;
    private String description;
    private int length;
    private FieldType type;
    private ArrayList<CalypsoRecordField> subfields;
    private HashMap<String, CalypsoRecordField> subfieldsByName;
    private String convertedValue;
    private boolean filled;

    public CalypsoRecordField(String description, int size, CalypsoRecordField.FieldType type, CalypsoEnvironment env){
        this.description = description;
        this.length = size;
        this.subfields = new ArrayList<>();
        this.subfieldsByName= new HashMap<>();
        this.type = type;
        this.env = env;
    }

    // copy ctor
    public CalypsoRecordField (CalypsoRecordField f){
        this.filled = false;
        this.env = f.env;
        this.length = f.length;
        this.type = f.type;
        this.convertedValue = "";
        this.description = f.description;
        this.subfields = new ArrayList<>();
        this.subfieldsByName = new HashMap<>();
        for(CalypsoRecordField ff : f.subfields) {
            CalypsoRecordField subfield = new CalypsoRecordField(ff);
            this.subfields.add(subfield);
            this.subfieldsByName.put(ff.getDescription(), subfield);
        }
    }

    public FieldType getFieldTypeFromString(String fieldType){
        String s = fieldType.toLowerCase();
        switch (s) {
            case "pointer":
                return Pointer;
            case "bitmap":
                return Bitmap;
            case "bcddate":
                return BcdDate;
            case "date":
                return Date;
            case "time":
                return Time;
            case "datetime":
                return DateTime;
            case "amount":
                return Amount;
            case "number":
                return Number;
            case "networkid":
                return NetworkId;
            case "string":
                return String;
            case "repeat":
                return Repeat;
            case "route":
                return Route;
            case "stop":
                return Stop;
            case "vehicle":
                return Vehicle;
            case "direction":
                return Direction;
            default:
                return Undefined;
        }
    }

    public CalypsoRecordField(Element e, CalypsoEnvironment env){
        this.description = e.getAttributeValue("description");
        this.type = this.getFieldTypeFromString(e.getAttributeValue("type"));
        this.length = Integer.parseInt(e.getAttributeValue("length"));
        this.subfields = new ArrayList<>();
        this.subfieldsByName = new HashMap<>();
        this.env = env;
        for(Element ee : e.getChildren()) {
            CalypsoRecordField subfield = new CalypsoRecordField(ee, env);
            this.subfields.add(subfield);
            this.subfieldsByName.put(subfield.getDescription(), new CalypsoRecordField(ee, env));
        }
    }

    /**
     *
     * @param buffer Array of bits
     * @param offset Offset in bits
     * @return amount of bits consumed
     * @throws IndexOutOfBoundsException
     */
    public int fill(byte[] buffer, int offset) throws IndexOutOfBoundsException{
        BitArray bits = new BitArray(buffer, offset, this.length);
        int consumed = this.length;
        this.filled = true;

        switch (this.type) {
            case Bitmap:
                BitArray bitmapMask = bits;
                this.convertedValue = bitmapMask.toString();
                for (int i = 0; i < this.length; i++)
                    if (bitmapMask.get(i))
                        consumed += this.subfields.get(i).fill(buffer, offset + consumed);
                break;
            case Repeat:
                int count = bits.getInt();
                this.convertedValue = "" + count;

                for (int i = 0; i < count - 1; i++) // on ajoute les répétitions des champs.
                    this.subfields.add(new CalypsoRecordField(this.subfields.get(0)));

                for (int i = 0; i < count; i++)
                    consumed += this.subfields.get(i).fill(buffer, offset + consumed);
                break;
            case String:
                this.convertedValue = new String(bits.getChars());
                break;
            case Date:
                int timestamp = 852073200 + bits.getInt() * 24 * 3600;
                java.util.Date date = new Date((long) timestamp * 1000);
                this.convertedValue = new SimpleDateFormat("dd/MM/yyyy").format(date);
                break;
            case Time:
                int minutes = bits.getInt();
                this.convertedValue = ""+minutes/60+":"+minutes%60;
                break;
            case BcdDate:
                assert (this.length == 32);
                StringBuilder sb = new StringBuilder();
                String rawDate = bits.toHex();
                sb.append(rawDate.substring(9, 11));
                sb.append('/');
                sb.append(rawDate.substring(6, 8));
                sb.append('/');
                sb.append(rawDate.substring(0, 2));
                sb.append(rawDate.substring(3, 5));
                this.convertedValue = sb.toString();
                break;
            case PayMethod:
                int methNo = bits.getInt();
                if (payMethods.containsKey(methNo))
                    this.convertedValue = payMethods.get(methNo);
                else
                    this.convertedValue = "Méthode de paiement inconnue !";
                break;
            case Number:
                if (this.length < 8) { // < et non <= car types signés.
                    this.convertedValue = "" +  (int) bits.getBytes()[0];
                } else if (this.length < 32) {
                    this.convertedValue = "" + (int) bits.getChars()[0];
                } else if (this.length < 64) {
                    this.convertedValue = "" + bits.getLong();
                } else {
                    System.out.println("INTEGER TOO BIG !");
                    this.convertedValue = "XXXXXXXXXXXX";
                }
                break;
            case NetworkId:
                /* Buggy...
                assert(this.length == 24);
                bits = bits.getFlipped();
                int countryID = bits.get(0, 12).getInt();
                int regionID = bits.get(12, 12).getInt();
                this.convertedValue = ""+countryID+":"+regionID;*/
                this.convertedValue = bits.toHex();
                break;
            case Stop:
                if(this.env.isTopologyConfigured())
                    this.convertedValue = this.env.getStopName(bits.getInt());
                else
                    this.convertedValue = bits.toHex();
                break;
            case Route:
                if(this.env.isTopologyConfigured())
                    this.convertedValue = this.env.getRouteName(bits.getInt()).replace("/--/", "<>"); // pas de chevrons dans le xml...
                else
                    this.convertedValue = bits.toHex();
                break;
            default:  // et donc Undefined
                this.convertedValue = bits.toHex();
                break;
        }
        return consumed;
    }

    public static String bits2String(byte[] bs, int start, int count){
        StringBuilder sb = new StringBuilder();
        for (int src = start; src < (start+count); src++)
            sb.append(((bs[src/8] >> (7-src%8))&0x01) == 1 ? '1' : '0');
        return sb.toString();
    }

    public long getBits(byte[] bs, int offset, int shift, int count){
        long mask = 0x7fffffffffffffffL;
        long res = 0x0L;

        return res;
    }

    public static String nesting(int level){
        StringBuilder r = new StringBuilder();
        for (int j = 0; j < level; j++)
            r.append("    ");
        return r.toString();
    }

    public void print(int n){
        if(!this.filled)
            return;
        System.out.println(nesting(n)+"> "+this.description);
        System.out.println(nesting(n)+"   - Type : "+this.type.getTypeName());
        System.out.println(nesting(n)+"   - Size : "+this.length);
        System.out.println(nesting(n)+"   - Converted value : "+this.convertedValue);
        if(this.type == Bitmap || this.type == Repeat) {
            System.out.println(nesting(n) + "   - Subfields :");
            for (CalypsoRecordField f : this.subfields)
                f.print(n+1);
        }
    }
    public void print(){
        this.print(0);
    }
    public java.lang.String getConvertedValue() {
        return convertedValue;
    }

    public java.lang.String getDescription() {
        return description;
    }
    public CalypsoRecordField getSubfield(String description){
        return this.subfieldsByName.containsKey(description) ? this.subfieldsByName.get(description) : null;
    }
}

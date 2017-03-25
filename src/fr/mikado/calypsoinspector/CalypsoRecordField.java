package fr.mikado.calypsoinspector;

import com.sun.istack.internal.Nullable;
import org.jdom2.Document;
import org.jdom2.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static fr.mikado.calypsoinspector.CalypsoRecordField.FieldType.*;

/**
 * This class describes a field inside a calypso record.
 * A Calypso Record is basically an array of fields.
 */
public class CalypsoRecordField {

    enum FieldType {
        Bitmap, Pointer, Date, Time, DateTime, Amount, Number, NetworkId, BcdDate, String, Repeat, Route, Stop, Vehicle, Direction, PayMethod, YesNo, Undefined;
        public static String[] n = {"Bitmap", "Pointer", "Date", "Time", "DateTime", "Amount", "Number", "NetworkId", "BcdDate", "String", "Repeat", "Route", "Stop", "Vehicle", "Direction", "PayMethod", "YesNo", "Undefined"};
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

    private static HashMap<Integer, String> countryCodes;

    static{
        countryCodes = new HashMap<>();
        Document cc = CalypsoEnvironment.openDocument("CountryCodes.xml");
        if(cc != null) {
            if (!cc.getRootElement().getName().equals("countryCodes")) {
                System.out.println("CountryCodes.xml does not contain any country codes !");
            } else {
                for (Element e : cc.getRootElement().getChildren())
                    countryCodes.put(Integer.parseInt(e.getAttributeValue("id")), e.getAttributeValue("name"));
            }
        }
    }

    private CalypsoEnvironment env;
    private CalypsoRecord parentRecord;
    private String description;
    private int length;
    private FieldType type;
    private ArrayList<CalypsoRecordField> subfields;
    private HashMap<String, CalypsoRecordField> subfieldsByName;
    private String convertedValue;
    private BitArray bits;
    private boolean filled;

    /**
     * Manually add a Record Field (without XML)
     * @param description field description
     * @param size Size in bits
     * @param type Data type of the field
     * @param env Current Calypso Environment (can be null)
     */
    public CalypsoRecordField(String description, int size, CalypsoRecordField.FieldType type, @Nullable CalypsoEnvironment env){
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
            case "yesno":
                return YesNo;
            default:
                return Undefined;
        }
    }

    /**
     * Creates a Record field using an XML node.
     * @param e XML DOM node
     * @param env Current Calypso Environment (can be null)
     */
    public CalypsoRecordField(Element e, @Nullable CalypsoEnvironment env){
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
     * Fills the current record field using a bit buffer from the file read.
     * @param buffer Array of bits
     * @param offset Offset in bits
     * @return amount of bits consumed
     * @throws IndexOutOfBoundsException
     */
    public int fill(byte[] buffer, int offset) throws IndexOutOfBoundsException{
        this.bits = new BitArray(buffer, offset, this.length);
        int consumed = this.length;
        this.filled = true;

        switch (this.type) {
            case Bitmap:
                this.convertedValue = this.bits.toString();
                for (int i = 0; i < this.length; i++)
                    if (this.bits.get(i))
                        consumed += this.subfields.get(i).fill(buffer, offset + consumed);
                break;
            case Repeat:
                int count = this.bits.getInt();
                this.convertedValue = "" + count;

                for (int i = 0; i < count - 1; i++) // on ajoute les répétitions des champs.
                    this.subfields.add(new CalypsoRecordField(this.subfields.get(0)));

                for (int i = 0; i < count; i++)
                    consumed += this.subfields.get(i).fill(buffer, offset + consumed);
                break;
            case String:
                this.convertedValue = new String(this.bits.getChars());
                break;
            case Date:
                int timestamp = 852073200 + this.bits.getInt() * 24 * 3600;
                java.util.Date date = new Date((long) timestamp * 1000);
                this.convertedValue = new SimpleDateFormat("dd/MM/yyyy").format(date);
                break;
            case Time:
                int minutes = this.bits.getInt();
                this.convertedValue = ""+(minutes/60)%24+":"+minutes%60;
                break;
            case BcdDate:
                assert (this.length == 32);
                StringBuilder sb = new StringBuilder();
                String rawDate = this.bits.toHex();
                sb.append(rawDate.substring(9, 11));
                sb.append('/');
                sb.append(rawDate.substring(6, 8));
                sb.append('/');
                sb.append(rawDate.substring(0, 2));
                sb.append(rawDate.substring(3, 5));
                this.convertedValue = sb.toString();
                break;
            case PayMethod:
                int methNo = this.bits.getInt();
                if (payMethods.containsKey(methNo))
                    this.convertedValue = payMethods.get(methNo);
                else
                    this.convertedValue = "Méthode de paiement inconnue !";
                break;
            case Number:
                if (this.length < 8) { // < et non <= car types signés.
                    this.convertedValue = "" +  (int) this.bits.getBytes()[0];
                } else if (this.length < 32) {
                    this.convertedValue = "" + (int) this.bits.getChars()[0];
                } else if (this.length < 64) {
                    this.convertedValue = "" + this.bits.getLong();
                } else {
                    System.out.println("INTEGER TOO BIG !");
                    this.convertedValue = "XXXXXXXXXXXX";
                }
                break;
            case NetworkId:
                // Network ID might be BCD encoded in 2000/2001 Env>EnvNetworkId
                String net = this.bits.toHex();
                net = net.replace(" ", "");
                int country = Integer.parseInt(net.substring(0, 3));
                if(env != null && country != this.env.getCountryId() && country != 0)
                    System.out.println("The environment configuration does not suit this card ! Wrong country id.");
                int netId = Integer.parseInt(net.substring(4, 6));
                if(env != null && netId != this.env.getNetworkId() && netId != 0)
                    System.out.println("The environment configuration does not suit this card ! Wrong network id.");

                this.convertedValue = (countryCodes.containsKey(country) ? countryCodes.get(country) : ""+country) + " - " + netId;
                break;
            case Stop:
                if(this.env != null && this.env.isTopologyConfigured())
                    this.convertedValue = this.env.getStopName(this.bits.getInt());
                else
                    this.convertedValue = this.bits.toHex();
                break;
            case Route:
                if(this.env != null && this.env.isTopologyConfigured())
                    this.convertedValue = this.env.getRouteName(this.bits.getInt()).replace("/--/", "<>"); // pas de chevrons dans le xml...
                else
                    this.convertedValue = this.bits.toHex();
                break;
            case YesNo:
                this.convertedValue = this.bits.getInt() == 0 ? "Yes":"No";
                break;
            default:  // et donc Undefined
                this.convertedValue = this.bits.toHex();
                break;
        }
        return consumed;
    }

    public BitArray getBits(){
        return this.bits;
    }

    public static String nesting(int level){
        StringBuilder r = new StringBuilder();
        for (int j = 0; j < level; j++)
            r.append("    ");
        return r.toString();
    }

    /**
     * Prints the contents of the record field
     * @param n Indent level
     */
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
    public void setParentRecord(CalypsoRecord r){
        this.parentRecord = r;
    }
    public java.lang.String getDescription() {
        return description;
    }
    public ArrayList<CalypsoRecordField> getSubfields(){
        return this.subfields;
    }
    public CalypsoRecordField getSubfield(String description){
        return this.subfieldsByName.containsKey(description) ? this.subfieldsByName.get(description) : null;
    }
    public CalypsoRecord getParentRecord(){
        return this.parentRecord;
    }
}

package fr.mikado.calypso;

import com.sun.istack.internal.Nullable;
import org.jdom2.Document;
import org.jdom2.Element;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

import static fr.mikado.calypso.CalypsoRecordField.FieldType.*;


/**
 * This class describes a field inside a calypso record.
 * A Calypso Record is basically an array of fields.
 */
public class CalypsoRecordField {

    enum FieldType {
        Bitmap, Pointer, Date, Time, DateTime, Amount, Number, NetworkId, BcdDate, String, Repeat, Route, Stop, Vehicle, Direction, PayMethod, YesNo, Name, Gender, ContractStatus, ContractType, ContractTariff, ContractPointer, Profile, EventCode, CardStatus, Undefined;
        public static String[] n = {"Bitmap", "Pointer", "Date", "Time", "DateTime", "Amount", "Number", "NetworkId", "BcdDate", "String", "Repeat", "Route", "Stop", "Vehicle", "Direction", "PayMethod", "YesNo", "Name", "Gender", "ContractStatus", "ContractType", "ContractTariff", "ContractPointer", "Profile", "EventCode", "CardStatus", "Undefined"};
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

    private static HashMap<Integer, String> contractStatuses;
    static{
        contractStatuses = new HashMap<>();
        contractStatuses.put(0,"Never validated");
        contractStatuses.put(1,"Used once");
        contractStatuses.put(2,"Validated");
        contractStatuses.put(3,"Renewment notification sent");
        contractStatuses.put(4,"Punched");
        contractStatuses.put(5,"Cancelled");
        contractStatuses.put(6,"Interrupted");
        contractStatuses.put(7,"Status OK");
        contractStatuses.put(13,"Not available for validation");
        contractStatuses.put(14,"Free entry");
        contractStatuses.put(15,"Active");
        contractStatuses.put(16,"Pre-allocated");
        contractStatuses.put(17,"Completed and to be removed");
        contractStatuses.put(18,"Completed and cannot be removed");
        contractStatuses.put(19,"Blocked");
        contractStatuses.put(20,"Data group encrypted flag");
        contractStatuses.put(21,"Data group anonymous flag");
        contractStatuses.put(33,"Pending");
        contractStatuses.put(63,"Suspended");
        contractStatuses.put(88,"Disabled");
        contractStatuses.put(125,"Suspended contract");
        contractStatuses.put(126,"Invalid");
        contractStatuses.put(127,"Invalid et reimbursed");
        contractStatuses.put(255,"Deletable");
    }

    private static String[] eventTypes = {"Non spécifié", "Entrée", "Sortie", "Contrôle", "Correspondance entrante", "Correspondance sortante", "Correspondance"};
    private static String[] vehicleTypes = {"Non spécifié", "Bus urbain", "Bus interrurbain", "Métro", "Tramway", "Train", "Parking"};
    private static String[] cardStatuses = {"Anonyme", "Declarative", "Personnalisée", "Codage spécifique"};

    private CalypsoEnvironment env;
    private CalypsoRecord parentRecord;
    private CalypsoRecordField parentField;
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
    public CalypsoRecordField(String description, int size, CalypsoRecordField.FieldType type, CalypsoRecordField parentField, @Nullable CalypsoEnvironment env){
        this.description = description;
        this.length = size;
        this.subfields = new ArrayList<>();
        this.subfieldsByName= new HashMap<>();
        this.type = type;
        this.env = env;
        this.parentField = parentField;
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
            subfield.setParentField(this);
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
            case "name":
                return Name;
            case "gender":
                return Gender;
            case "contractstatus":
                return ContractStatus;
            case "contracttype":
                return ContractType;
            case "contracttariff":
                return ContractTariff;
            case "contractpointer":
                return ContractPointer;
            case "profile":
                return Profile;
            case "eventcode":
                return EventCode;
            case "cardstatus":
                return CardStatus;
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
        if(this.length == 0)
            return 0;

        this.bits = new BitArray(buffer, offset, this.length);
        if(this.bits.isNull()) // if bitmap is empty, do nothing more
            return this.length;

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
            case Pointer:
                break;
            case Date:
                int timestamp = 852073200 + this.bits.getInt() * 24 * 3600; // or maybe 852091200 ?
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
            case DateTime:
                break;
            case Amount:
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
                    this.convertedValue = this.env.getRouteName(this.bits.getInt()).replace("/--/", "<>");
                else
                    this.convertedValue = this.bits.toHex();
                break;
            case YesNo:
                this.convertedValue = this.bits.getInt() == 0 ? "Yes":"No";
                break;
            case Name:
                StringBuilder nameSB = new StringBuilder();
                for(int i = 0;i<bits.getSize();i+=5){
                    int car = bits.get(i, 5).getInt();
                    switch(car){
                        case 0x1B:
                            nameSB.append(' ');
                            break;
                        case 0x1C:
                            nameSB.append('´');
                            break;
                        case 0x1D:
                            nameSB.append('`');
                            break;
                        case 0x1E:
                            nameSB.append('^');
                            break;
                        case 0x1F:
                            nameSB.append('\'');
                            break;
                        case 0x00:
                            nameSB.append('-');
                            break;
                        default:
                            nameSB.append('A' + car);
                            break;
                    }
                    this.convertedValue = nameSB.toString();
                }
                this.convertedValue = this.bits.getInt() == 0 ? "Yes":"No";
                break;
            case Gender:
                switch(bits.getInt()){
                    case 1:
                        this.convertedValue = "M.";
                        break;
                    case 2:
                        this.convertedValue = "Mme.";
                        break;
                    case 3:
                        this.convertedValue = "Réservé";
                        break;
                    default:
                        this.convertedValue = "Inconnu";
                }
                break;
            case ContractStatus:
                int cStatus = bits.getInt();
                if(contractStatuses.containsKey(cStatus))
                    this.convertedValue = contractStatuses.get(bits.getInt());
                else
                    this.convertedValue = ""+bits.getInt();
                break;
            case ContractType:
                this.convertedValue = null;
                if(env.areFaresConfigured())
                    this.convertedValue = this.env.getFareName(bits.getInt());
                if(this.convertedValue == null)
                    this.convertedValue = bits.toHex();
                break;
            case ContractTariff:
                int exploitant = bits.get( 0, 4).getInt();
                int type       = bits.get( 4, 8).getInt();
                int priorite   = bits.get(12, 4).getInt();
                this.convertedValue = "" + exploitant + " " + Integer.toHexString(type) + "h " + priorite;
                break;
            case ContractPointer:
                int pointer = bits.getInt();
                this.env.contractPointers.add(pointer);
                Collections.sort(this.env.contractPointers);
                this.convertedValue = ""+Integer.toHexString(pointer);

                CalypsoRecordField tariff = this.getParentField().getSubfield("BestContractsTariff");
                if(tariff != null) {
                    String mappingName = tariff.getConvertedValue().split(" ")[1];
                    CalypsoFile contracts = this.env.getFile("Contracts");
                    contracts.addFileMapping(pointer, mappingName);
                    this.convertedValue = Integer.toHexString(pointer) + " -> " + mappingName;
                }
                break;
            case Vehicle:
                switch(bits.getInt()){
                    case 0:
                        this.convertedValue = "unspecified";
                        break;
                    case 1:
                        this.convertedValue = "bus urbain";
                        break;
                    case 2:
                        this.convertedValue = "bus interurbain";
                        break;
                    case 3:
                        this.convertedValue = "metro";
                        break;
                    case 4:
                        this.convertedValue = "tramway";
                        break;
                    case 5:
                        this.convertedValue = "train";
                        break;
                    case 6:
                        this.convertedValue = "TGV";
                        break;
                    case 7:
                        this.convertedValue = "busTrain";
                        break;
                    default:
                        this.convertedValue = "unknown";
                }
                break;
            case Profile:
                if(this.env.areProfilesConfigured())
                    this.convertedValue = this.env.getProfileName(bits.getInt());
                else
                    this.convertedValue = ""+bits.getInt();
                break;
            case EventCode:
                int vehicleType = (bits.getInt() >> 4) & 0xf;
                int eventType = bits.getInt() & 0xf;
                this.convertedValue = "";
                if(eventType < eventTypes.length)
                    this.convertedValue = eventTypes[eventType];
                this.convertedValue += ":";
                if(eventType < vehicleTypes.length)
                    this.convertedValue += vehicleTypes[vehicleType];
                break;
            case CardStatus:
                int status = bits.getInt();
                if(status < cardStatuses.length)
                    this.convertedValue = cardStatuses[status];
                else
                    this.convertedValue = bits.toHex() + "h ???";
                break;
            case Undefined:
            default:
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
    public void print(int n, boolean debug){
        if(!this.filled)
            return;
        System.out.println(nesting(n)+"> "+this.description);
        if(debug) {
            System.out.println(nesting(n) + "   - Type : " + this.type.getTypeName());
            System.out.println(nesting(n) + "   - Size : " + this.length);
        }
        if(this.type != Bitmap && this.type != Repeat)
            System.out.println(nesting(n) + "   - Converted value : " + this.convertedValue);
        if(this.type == Bitmap || this.type == Repeat) {
            System.out.println(nesting(n) + "   - Subfields :");
            for (CalypsoRecordField f : this.subfields)
                f.print(n+1, debug);
        }
    }
    public void print(){
        this.print(0, true);
    }
    public java.lang.String getConvertedValue() {
        return convertedValue;
    }
    public void setParentRecord(CalypsoRecord r){
        this.parentRecord = r;
        for(CalypsoRecordField sub : this.subfields)
            sub.setParentRecord(r);
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
    public void setParentField(CalypsoRecordField field){
        this.parentField = field;
    }
    public CalypsoRecordField getParentField(){
        return this.parentField;
    }
    public CalypsoRecord getParentRecord(){
        return this.parentRecord;
    }
    public boolean isFilled(){
        return this.filled;
    }
}

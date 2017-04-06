package fr.mikado.calypsoinspector;

import fr.mikado.calypso.CalypsoEnvironment;
import fr.mikado.calypso.CalypsoFile;
import fr.mikado.calypso.CalypsoRecord;
import fr.mikado.calypso.CalypsoRecordField;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by alexis on 06/04/17.
 */
public class CalypsoDump {

    /**
     * Prints out the contents of the event log of the card. (last trips)
     */
    public static void dumpTrips(CalypsoEnvironment env) {
        CalypsoFile events = env.getFile("Events");
        CalypsoFile contracts = env.getFile("Contracts");

        if (events != null) {
            for(CalypsoRecord rec : events.getRecords()){
                // basic event details
                String date = rec.getRecordField("Event Date").getConvertedValue();
                String time = rec.getRecordField("Event Time").getConvertedValue();
                CalypsoRecordField event = rec.getRecordField("Event");
                String stop = event.getSubfield("EventLocationId").getConvertedValue();
                String route = event.getSubfield("EventRouteNumber").getConvertedValue();
                String direction = event.getSubfield("EventData").getSubfield("EventDataRouteDirection").getConvertedValue();

                String fare = "<Contracts not loaded>";
                if(contracts != null) {
                    // which contract for this event ?
                    fare = "Error while decoding contract pointer. - Dog, probably.";
                    int farePointer = event.getSubfield("EventContractPointer").getBits().getInt();
                    int contractIndex = env.getContractIndex(farePointer);
                    if (contractIndex >= 0) {
                        CalypsoRecord contract = contracts.getRecords().get(contractIndex);
                        CalypsoRecordField contractBitmap = contract.getRecordField("PublicTransportContractBitmap");
                        CalypsoRecordField contractType = contractBitmap.getSubfield("ContractType");
                        fare = contractType.getConvertedValue();
                    }
                }

                System.out.println("Event :");
                System.out.println(" Date    : " + date + " " + time);
                System.out.println(" Arrêt   : " + stop);
                System.out.println(" Ligne   : " + route);
                System.out.println(" Sens    : " + direction);
                System.out.println(" Contrat : " + fare +"\n");
            }
        }
    }

    public static void dumpProfiles(CalypsoEnvironment env){
        CalypsoFile envHolder = env.getFile("Environment, Holder");
        CalypsoRecord envHolderRec = envHolder.getRecords().get(0);
        CalypsoRecordField holderProfiles = envHolderRec.getRecordField("Holder Bitmap").getSubfield("Holder Profiles(0..4)");

        for(CalypsoRecordField f : holderProfiles.getSubfields()) {
            if(!f.isFilled()) {
                System.out.println("No profiles.");
                break;
            }
            System.out.println("Profile :");
            System.out.println(" Label        : " + f.getSubfield("Profile Number").getConvertedValue());
            System.out.println(" Profile date : " + f.getSubfield("Profile Date").getConvertedValue());
        }
        System.out.print("\n");
    }

    public static void dumpContracts(CalypsoEnvironment env) throws ParseException {
        String fare = "";
        DateFormat format = new SimpleDateFormat("dd/MM/yyy", Locale.FRANCE);
        Date now = new Date();

        CalypsoFile contracts = env.getFile("Contracts");
        if(contracts != null) {
            // which contract for this event ?
            for(CalypsoRecord contract : contracts.getRecords()) {
                CalypsoRecordField contractBitmap = contract.getRecordField("PublicTransportContractBitmap");
                CalypsoRecordField contractType = contractBitmap.getSubfield("ContractType");
                CalypsoRecordField contractStatus= contractBitmap.getSubfield("ContractStatus");
                CalypsoRecordField contractValidity = contractBitmap.getSubfield("ContractValidityInfo");
                CalypsoRecordField contractStart = contractValidity.getSubfield("ContractValidityStartDate");
                CalypsoRecordField contractEnd = contractValidity.getSubfield("ContractValidityEndDate");
                String contractEndStr = contractEnd.getConvertedValue();
                System.out.println("Contract :");
                System.out.println("   Label      : " + contractType.getConvertedValue());
                System.out.println("   Start date : " + contractStart.getConvertedValue());
                System.out.print("   End date   : " + contractEndStr);
                if(contractEndStr.length() > 0 && now.after(format.parse(contractEndStr)))
                    System.out.print(" (EXPIRED)");
                System.out.print("\n");
                System.out.println("   Status     : " + contractStatus.getConvertedValue());
            }
        }else
            System.out.println("<Contracts not loaded>");
        System.out.print("\n");
    }
}

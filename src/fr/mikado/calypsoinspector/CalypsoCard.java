package fr.mikado.calypsoinspector;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.logging.Logger;

import static fr.mikado.calypsoinspector.CalypsoFile.CalypsoFileType.EF;

/**
 * This class describes a Calypso Card from a Card and a CalypsoEnvironment.
 * the structure of the card is contained in the Calypso Environment.
 */
public class CalypsoCard {
    private CalypsoEnvironment env;
    private Card card;

    public CalypsoCard(Card card, CalypsoEnvironment env){
        this.card = card;
        this.env = env;
    }

    /**
     * @return The serial number printed on the card.
     */
    public long getCardNumber(){
        BitArray atr = new BitArray(card.getATR().getBytes(), 40, 32);
        return atr.getLong();
    }

    public String getROMVersion() throws CardException {
        CommandAPDU c = new CommandAPDU(new byte[]{(byte)0x00, (byte)0x10, 0x00, 0x00, 0x00});
        ResponseAPDU r;
        try {
            r = this.card.getBasicChannel().transmit(c);
        } catch (CardException e) {
            System.out.println("Error while getting response APDU while asking for chip properties.");
            throw(e);
        }

        return new BitArray(r.getBytes(), 55*8, 8).toHex().toUpperCase();
    }

    /**
     * @return The version of the Celego chip inside.
     * @throws CardException
     */
    public String getChipVersion() throws CardException {
        CommandAPDU c = new CommandAPDU(new byte[]{(byte)0x00, (byte)0x10, 0x00, 0x00, 0x00});
        ResponseAPDU r;
        try {
            r = this.card.getBasicChannel().transmit(c);
        } catch (CardException e) {
            System.out.println("Error while getting response APDU while asking for chip properties.");
            throw(e);
        }
        return new BitArray(r.getBytes(), 54*8, 8).toHex().toLowerCase();
    }

    /**
     * Reads the contents of the file using the structure information contained in the Calypso Environment.
     */
    public void read(){
        env.getFiles().forEach(this::readFile);
    }

    /**
     * Reads a record in the selected file.
     * @param recordId Id of the record
     * @return Reponse APDU from the READ RECORD (94 B2) command.
     * @throws CardException
     */
    private ResponseAPDU readRecord(int recordId) throws CardException {
        return this.card.getBasicChannel().transmit(new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xB2, (byte) recordId, 0x04, 0x1D}));
    }

    private boolean selectFile(CalypsoFile f){
        int id = Integer.parseInt(f.getIdentifier(), 16);
        return selectFileFromLFI(id);
    }

    /**
     * Selects a file on the card using LFI.
     * @param LFI Long File Identifier
     * @return Reponse APDU from the SELECT FILE (94 A4) command.
     * @throws CardException
     */
    private boolean selectFileFromLFI(int LFI){
        CommandAPDU c = new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, (byte) ((LFI>>8)&0xff), (byte) (LFI&0xff), 0x00});
        ResponseAPDU r;

        try {
            r = this.card.getBasicChannel().transmit(c);
        } catch (CardException e) {
            System.out.println("Cannot select file LFI:"+LFI+" : " + e.getMessage());
            return false;
        }
        if(r.getSW() != 0x9000)
            System.out.println("Error while selecting file LFI:"+LFI+" : " + SWDecoder.decode(r.getSW()));
        return r.getSW() == 0x9000;
    }

    /**
     * Reads the contents of a CalypsoFile on the card.
     * @param f CalypsoFile
     */
    public void readFile(CalypsoFile f){
        if(!this.selectFile(f)){
            Logger.getGlobal().warning("Could not select file "+f.getIdentifier()+" ! Skipping...");
            return;
        }

        ResponseAPDU responseAPDU;
        if(f.getType() == EF) {
            for (int i = 1; ; i++) {
                try {
                    responseAPDU = this.readRecord(i);
                    if (responseAPDU.getSW() == 0x6A83)
                        break;
                } catch (CardException e) {
                    Logger.getGlobal().warning("Could not read record #"+i+" for file "+f.getIdentifier());
                    continue;
                }
                if (responseAPDU.getSW() != 0x9000) {
                    Logger.getGlobal().warning("Could not read record #"+i+" for file "+f.getIdentifier() + "\n" +
                                                SWDecoder.decode(responseAPDU.getSW()) + " Last read record : " + i);
                    break;
                }
                f.newRecord(responseAPDU.getData());
            }
        }else
            f.getChildren().forEach(this::readFile);
    }

    public void disconnect(){
        try {
            this.card.disconnect(false);
        } catch (CardException e) {
            Logger.getGlobal().warning("Error while disconnecting the card. Don't care.");
        }
    }

    /**
     * Prints all the data from the CalypsoCard.
     * @throws CardException
     */
    public void dump() throws CardException {
        System.out.println("Calypso card Country="+this.env.getCountryId()+" Network="+this.env.getNetworkId());
        System.out.println("Calypso card number #"+this.getCardNumber());
        String chipVer = this.getChipVersion();
        System.out.println("Calypso celego chip : version "+chipVer+" (" + (chipVer.equals("3c ") ? "" : "non ") + "compatible NFC Android)");
        System.out.println("ROM version : " + this.getROMVersion());
        System.out.println("ATR :"+ BitArray.bytes2Hex(this.card.getATR().getBytes()));
        System.out.println("Contents :");
        for(CalypsoFile f : this.env.getFiles())
            f.dump(1);
    }

    /**
     * Prints the structure of the CalypsoCard.
     */
    public void dumpTree() {
        System.out.println("Calypso card Country="+ this.env.getCountryId() +" Network="+ this.env.getNetworkId());
        for(CalypsoFile f : this.env.getFiles())
            f.dumpStructure(1);
    }

    /**
     * Prints out the contents of the event log of the card. (last trips)
     */
    public void dumpTrips() {
        CalypsoFile events = null;
        for(CalypsoFile f : this.env.getFiles())
            for (CalypsoFile ff : f.getChildren())
                if (ff.getDescription().equals("Events")) {
                    events = ff;
                    break;
                }
        if (events != null) {
            for(CalypsoRecord rec : events.getRecords()){
                String date = rec.getRecordField("Event Date").getConvertedValue();
                String time = rec.getRecordField("Event Time").getConvertedValue();
                CalypsoRecordField event = rec.getRecordField("Event");
                String stop = event.getSubfield("EventLocationId").getConvertedValue();
                String route = event.getSubfield("EventRouteNumber").getConvertedValue();
                String direction = event.getSubfield("EventData").getSubfield("EventDataRouteDirection").getConvertedValue();
                System.out.println("Event :");
                System.out.println(" Date : "  + date + " " + time);
                System.out.println(" Arrêt : " + stop);
                System.out.println(" Ligne : " + route);
                System.out.println(" Sens : "  + direction +"\n");
            }
        }
    }
}

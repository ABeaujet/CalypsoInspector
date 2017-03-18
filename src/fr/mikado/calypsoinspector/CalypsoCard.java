package fr.mikado.calypsoinspector;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import java.util.ArrayList;
import java.util.logging.Logger;

import static fr.mikado.calypsoinspector.CalypsoFile.CalypsoFileType.EF;

public class CalypsoCard {
    private CalypsoEnvironment env;
    private Card card;

    public CalypsoCard(Card card, CalypsoEnvironment env){
        this.card = card;
        this.env = env;
    }

    public long getCardNumber(){
        BitArray atr = new BitArray(card.getATR().getBytes(), 40, 32);
        return atr.getLong();
    }

    public String getROMVersion(){
        CommandAPDU c = new CommandAPDU(new byte[]{(byte)0x00, (byte)0x10, 0x00, 0x00, 0x00});
        ResponseAPDU r = null;
        try {
            r = this.card.getBasicChannel().transmit(c);
        } catch (CardException e) {
            e.printStackTrace();
        }

        return new BitArray(r.getBytes(), 55*8, 8).toHex().toUpperCase();
    }

    public String getChipVersion(){
        CommandAPDU c = new CommandAPDU(new byte[]{(byte)0x00, (byte)0x10, 0x00, 0x00, 0x00});
        ResponseAPDU r = null;
        try {
            r = this.card.getBasicChannel().transmit(c);
        } catch (CardException e) {
            e.printStackTrace();
        }
        return new BitArray(r.getBytes(), 54*8, 8).toHex().toLowerCase();
    }

    public void read(){
        for(CalypsoFile f : env.getFiles())
            this.readFile(f);
    }

    private ResponseAPDU readRecord(int recordId) throws CardException {
        CommandAPDU c = new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xB2, (byte) recordId, 0x04, 0x1D});
        //System.out.println("Read record #"+recordId+" "+c.toString()+" "+ bytes2Hex(c.getBytes()));
        return this.card.getBasicChannel().transmit(new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xB2, (byte) recordId, 0x04, 0x1D}));
    }

    private boolean selectFile(CalypsoFile f) throws CardException {
        int id = Integer.parseInt(f.getIdentifier(), 16);

        CommandAPDU c = new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, (byte) ((id>>8)&0xff), (byte) (id&0xff), 0x00});

        ResponseAPDU r = this.card.getBasicChannel().transmit(c);
        //Logger.getGlobal().info(r.toString());
        return r.getSW() == 0x9000;
    }

    public void readFile(CalypsoFile f){
        try {
            this.selectFile(f);
        } catch (CardException e) {
            Logger.getGlobal().warning("Could not select file "+f.getIdentifier()+" ! Skipping...");
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
                    Logger.getGlobal().warning("Could not read record #"+i+" for file "+f.getIdentifier());
                    Logger.getGlobal().warning(responseAPDU.toString() + " Last read record : " + i);
                    continue;
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
            Logger.getGlobal().warning("Error while disconnecting the card. Don't card.");
        }
    }

    public void dump() {
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

    public void dumpTree() {
        System.out.println("Calypso card Country="+this.env.getCountryId()+" Network="+this.env.getNetworkId());
        for(CalypsoFile f : this.env.getFiles())
            f.dumpStructure(1);
    }

    public void dumpTrips() {
        CalypsoFile events = null;
        for(CalypsoFile f : this.env.getFiles())
            for (CalypsoFile ff : f.getChildren())
                if (ff.getDescription().equals("Events")) {
                    events = ff;
                    break;
                }
        for(CalypsoRecord rec : events.getRecords()){
            String date = rec.getRecordField("Event Date").getConvertedValue();
            String time = rec.getRecordField("Event Time").getConvertedValue();
            CalypsoRecordField event = rec.getRecordField("Event");
            String stop = event.getSubfield("EventLocationId").getConvertedValue();
            String route = event.getSubfield("EventRouteNumber").getConvertedValue();
            System.out.println("Event :");
            System.out.println(" Date : " + date + " " + time);
            System.out.println(" Arrêt : " + stop);
            System.out.println(" Ligne : " + route +"\n");
        }
    }
}

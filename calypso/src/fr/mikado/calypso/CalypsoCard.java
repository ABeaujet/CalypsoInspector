package fr.mikado.calypso;

import fr.mikado.isodep.CardException;
import fr.mikado.isodep.CommandAPDU;
import fr.mikado.isodep.IsoDepInterface;
import fr.mikado.isodep.ResponseAPDU;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Logger;

import static fr.mikado.calypso.CalypsoFile.CalypsoFileType.EF;

/**
 * This class describes a Calypso Card from a Card and a CalypsoEnvironment.
 * the structure of the card is contained in the Calypso Environment.
 */
public class CalypsoCard {
    private CalypsoEnvironment env;
    private IsoDepInterface card;

    public CalypsoCard(IsoDepInterface card, CalypsoEnvironment env){
        this.card = card;
        this.env = env;
    }

    /**
     * @return The serial number printed on the card.
     */
    public long getCardNumber(){
        BitArray atr = new BitArray(card.getATR(), 40, 32);
        return atr.getLong();
    }

    public String getROMVersion() throws CardException {
        CommandAPDU c = new CommandAPDU(new byte[]{(byte)0x00, (byte)0x10, 0x00, 0x00, 0x00});
        ResponseAPDU r;
        try {
            r = this.card.transmit(c);
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
            r = this.card.transmit(c);
        } catch (CardException e) {
            System.out.println("Error while getting response APDU while asking for chip properties.");
            throw(e);
        }
        return new BitArray(r.getBytes(), 54*8, 8).toHex().toLowerCase();
    }

    /**
     * Reads the contents of the file using the structure information contained in the Calypso Environment.
     */
    public void read() throws CardException {
        for(CalypsoFile f : env.getFiles())
            for(Integer LFI : f.getLFIs())
                this.readFile(f, LFI);
    }

    /**
     * Reads a record in a file selected by its SFI
     * @param SFI Short File Identifier
     * @param recordId Id of the record
     * @return Response APDU from the READ RECORD (94 B2) command.
     * @throws CardException
     */
    private ResponseAPDU readRecordSFI(int SFI, int recordId) throws CardException {
        return this.card.transmit(new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xB2, (byte) recordId, (byte)(0x04 + (SFI<<3)), 0x00}));
    }

    /**
     * Reads a record in a file previsouly selected by its LFI
     * @param recordId Id of the record
     * @return Response APDU from the READ RECORD (94 B2) command.
     * @throws CardException
     */
    private ResponseAPDU readRecordLFI(int recordId) throws CardException {
        return this.card.transmit(new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xB2, (byte) recordId, 0x04, 0x00}));
    }

    private boolean selectFile(CalypsoFile f, int LFI){
        return selectFileFromLFI(LFI);
    }

    /**
     * Selects a file on the card using LFI.
     * @param LFI Long File Identifier
     * @return Reponse APDU from the SELECT FILE (94 A4) command.
     */
    private boolean selectFileFromLFI(int LFI){
        CommandAPDU c = new CommandAPDU(new byte[]{(byte) 0x94, (byte) 0xA4, 0x00, 0x00, 0x02, (byte) ((LFI>>8)&0xff), (byte) (LFI&0xff), 0x00});
        ResponseAPDU r;

        try {
            r = this.card.transmit(c);
        } catch (CardException e) {
            System.out.println("Cannot select file LFI:"+Integer.toHexString(LFI)+"h : " + e.getMessage());
            return false;
        }
        if(r.getSW() != 0x9000)
            System.out.println("Error while selecting file LFI:"+Integer.toHexString(LFI)+"h : " + SWDecoder.decode(r.getSW()));
        return r.getSW() == 0x9000;
    }

    /**
     * Reads the contents of a CalypsoFile on the card.
     * @param f CalypsoFile
     * @param LFI Which LFI to use for this file
     */
    public void readFile(CalypsoFile f, Integer LFI) throws CardException {
        if(!f.isSFIAddressable())
            if(!this.selectFile(f, LFI))
                return;

        ResponseAPDU responseAPDU;
        if(f.getType() == EF) {
            for (int i = 1; ; i++) {
                if(f.isSFIAddressable())
                    responseAPDU = this.readRecordSFI(f.getSFI(), i);
                else
                    responseAPDU = this.readRecordLFI(i);
                if (responseAPDU.getSW() == 0x6A83)
                    break;
                if (responseAPDU.getSW() != 0x9000) {
                    Logger.getGlobal().warning("Could not read record #"+i+" for file "+f.getIdentifier() + "\n" +
                                                SWDecoder.decode(responseAPDU.getSW()) + " Last read record : " + i);
                    break;
                }
                f.newRecord(responseAPDU.getData());
            }
        }else
            for(CalypsoFile child : f.getChildren())
                if(child.isSFIAddressable())
                    this.readFile(child, null);
                else
                    for(Integer LFIe : child.getLFIs())
                        this.readFile(child, LFIe);
    }

    public void disconnect(){
        try {
            this.card.disconnect();
        } catch (CardException e) {
            Logger.getGlobal().warning("Error while disconnecting the card. Don't care.");
        }
    }

    /**
     * Prints all the data from the CalypsoCard.
     * @throws CardException
     */
    public void dump(boolean debug) throws CardException {
        System.out.println("Calypso card Country=" + this.env.getCountryId() + " Network=" + this.env.getNetworkId());
        //System.out.println("Calypso card number #" + this.getCardNumber());
        if(debug) {
            String chipVer = this.getChipVersion();
            System.out.println("Calypso celego chip : version " + chipVer + " (" + (chipVer.equals("3c ") ? "" : "NOT ") + "Android NFC compatible)");
            System.out.println("ROM version : " + this.getROMVersion());
            System.out.println("ATR :" + BitArray.bytes2Hex(this.card.getATR()));
        }
        System.out.println("Contents :");
        for(CalypsoFile f : this.env.getFiles())
            f.dump(1, debug);
    }

    /**
     * Prints the structure of the CalypsoCard.
     */
    public void dumpTree() {
        System.out.println("Calypso card Country="+ this.env.getCountryId() +" Network="+ this.env.getNetworkId());
        for(CalypsoFile f : this.env.getFiles())
            f.dumpStructure(1);
    }

    public CalypsoEnvironment getEnvironment(){
        return this.env;
    }
}












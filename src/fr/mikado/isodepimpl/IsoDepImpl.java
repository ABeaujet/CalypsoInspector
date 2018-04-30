package fr.mikado.isodepimpl;

import fr.mikado.isodep.CardException;
import fr.mikado.isodep.CommandAPDU;
import fr.mikado.isodep.IsoDepInterface;
import fr.mikado.isodep.ResponseAPDU;

import javax.smartcardio.*;

/**
 * Created by alexis on 05/04/17.
 */
public class IsoDepImpl implements IsoDepInterface {

    private Card card;

    public IsoDepImpl(Card card){
        this.card = card;
    }

    @Override
    public ResponseAPDU transmit(CommandAPDU cAPDU) throws CardException {
        javax.smartcardio.CommandAPDU c = new javax.smartcardio.CommandAPDU(cAPDU.getBytes());
        javax.smartcardio.ResponseAPDU r;
        try {
            r = card.getBasicChannel().transmit(c);
        } catch (javax.smartcardio.CardException e) {
            throw new CardException(e.getMessage());
        }
        byte[] rb = r.getBytes();
        return new ResponseAPDU(rb);
    }

    @Override
    public byte[] getATR() {
        return this.card.getATR().getBytes();
    }

    @Override
    public void disconnect() throws CardException {
        try {
            this.card.disconnect(false);
        } catch (javax.smartcardio.CardException e) {
            throw new CardException(e.getMessage());
        }
    }

    public static IsoDepImpl getDefaultCard() throws Exception {
        CardTerminal term = null;
        try {
            term = TerminalFactory.getDefault().terminals().list().get(0);
        } catch (Exception e) {
            System.out.println("No terminal plugged... : " + e.getMessage());
        }
        if(term == null)
            throw new CardException("No terminal plugged.");

        System.out.println("Waiting for card...");
        term.waitForCardPresent(0);
        System.out.println("Card found !");

        return new IsoDepImpl(term.connect("T=1"));
    }
}




































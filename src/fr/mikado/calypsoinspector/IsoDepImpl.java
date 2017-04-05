package fr.mikado.calypsoinspector;

import fr.mikado.isodep.CardException;
import fr.mikado.isodep.CommandAPDU;
import fr.mikado.isodep.IsoDepInterface;
import fr.mikado.isodep.ResponseAPDU;

import javax.smartcardio.Card;

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
}




































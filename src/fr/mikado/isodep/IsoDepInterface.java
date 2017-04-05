package fr.mikado.isodep;

/**
 * Created by alexis on 05/04/17.
 */
public interface IsoDepInterface {
    ResponseAPDU transmit(CommandAPDU cAPDU) throws CardException;
    byte[] getATR();
    void disconnect() throws CardException;
}

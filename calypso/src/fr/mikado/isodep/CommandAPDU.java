package fr.mikado.isodep;

/**
 * Created by alexis on 05/04/17.
 */
public class CommandAPDU {
    private final byte[] apdu;

    public CommandAPDU(byte[] cApdu){
        this.apdu = cApdu;
    }

    public byte[] getBytes(){
        return this.apdu;
    }
}

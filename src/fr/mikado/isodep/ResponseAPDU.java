package fr.mikado.isodep;

import java.util.Arrays;

/**
 * Created by alexis on 05/04/17.
 */
public class ResponseAPDU {
    private final byte[] apdu;

    public ResponseAPDU(byte[] rAdpu){
        this.apdu = rAdpu;
    }

    public String toString(){
        return "ResponseAPDU: " + this.apdu.length + " bytes, SW=" + Integer.toHexString(getSW());
    }

    public byte[] getBytes(){
        return this.apdu.clone();
    }

    public int getSW1(){
        return this.apdu[apdu.length - 2] & 0xff;
    }

    public int getSW2(){
        return this.apdu[apdu.length - 1] & 0xff;
    }

    public int getSW(){
        int apdulen = this.apdu.length;
        if(this.apdu.length<2)
            return -1;
        return (this.getSW1() << 8) | this.getSW2();
    }

    public byte[] getData(){
        if(this.apdu.length <= 2)
            return null;
        return Arrays.copyOfRange(this.apdu, 0, this.apdu.length-2);
    }
}

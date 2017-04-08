package fr.mikado.calypso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Simple Bit manipulation class.
 * (may have some bugs not discovered in the unit tests...)
 */
public class BitArray {
    private ArrayList<Boolean> bits;
    private static char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public BitArray(int size){
        this.bits = new ArrayList<>(Collections.nCopies(size, false));
    }

    public BitArray(ArrayList<Boolean> bits){
        this.bits = new ArrayList<>(Collections.nCopies(bits.size(), false));
        for (int i = 0; i < bits.size(); i++)
            this.bits.set(i, bits.get(i));
    }

    /**
     * Crée un BitArray à partir des bits dans un char[]
     * @param bs Le MSB est dans la première case de bs !
     */
    public BitArray(byte[] bs){
        this.bits = new ArrayList<>(Collections.nCopies(bs.length*8, false));
        for (int i = 0; i < bs.length*8; i++)
            this.set(bs.length*8-i-1, ((bs[i / 8] >> ((7-i%8)) & 0x01) == 1));
    }

    /**
     * Crée un BitArray à partir d'une range de bits dans un char[]
     * @param bs Le MSB est à gauche ! MSB = bs[start]>>7 & 0x01
     * @param start Index du premier bit
     * @param count Nombre de bits
     */
    public BitArray(byte[] bs, int start, int count){
        this.bits = new ArrayList<>(Collections.nCopies(count, false));
        for (int i = 0; i < count; i++) {
            int bit = (bs[(start+i) / 8] >>(7-(start+i)%8)) & 0x01;
            this.set(count-i-1, bit == 1);
        }
    }
    /**
     * Crée un BitArray à partir d'un long
     * @param bits bits
     * @param count Nombre de bits dans le long (en partant du LSB)
     */
    public BitArray(long bits, int count){
        assert(count <= 64);
        this.bits = new ArrayList<>(Collections.nCopies(count, false));
        for (int i = 0; i < count; i++)
            this.set(count-i-1, ((bits>>(count-1-i))&0x01L) == 1);
    }

    public static byte[] hex2Bytes(String hexBytes){
        if(hexBytes.length()%2 != 0)
            throw new NumberFormatException("Hex byte string has to contain an even number of characters");
        byte[] bytes = new byte[hexBytes.length()/2];
        char[] chars = hexBytes.toCharArray();
        String hexCharsStr = new String(hexChars);
        for(int i = 0;i<hexBytes.length();i+=2){
            bytes[i/2]  = (byte) (hexCharsStr.indexOf(chars[i]) << 4);
            bytes[i/2] |= (byte) (hexCharsStr.indexOf(chars[i+1]));
        }
        return bytes;
    }

    public static String bytes2Hex(byte[] bs, int offset, int len){
        byte[] t = new byte[len];
        for (int i = offset; i < len; i++)
            t[i-offset] = bs[i];
        return bytes2Hex(t);
    }

    public static String byte2Hex(byte b){
        StringBuilder sb = new StringBuilder();
        sb.append(hexChars[(b>>4)&0xf]);
        sb.append(hexChars[b&0xf]);
        return sb.toString();
    }

    public static String bytes2Hex(byte[] bs){
        StringBuilder sb = new StringBuilder();
        for(byte b : bs) {
            sb.append(byte2Hex(b));
            sb.append(' ');
        }
        return sb.toString();
    }

    public void set(int index, boolean val){
        this.bits.set(index, val);
    }

    public Boolean get(int index){
        return this.bits.get(index);
    }

    public BitArray get(int i, int li){
        BitArray res = new BitArray(new ArrayList<>(Collections.nCopies(li, false)));
        for(int j = i;j<(i+li);j++)
            res.set(j-i, this.bits.get(j));
        return res;
    }

    public BitArray rshift(){
        return this.get(1, this.bits.size()-1);
    }

    public BitArray rshift(int shift){
        return this.get(shift, Math.max(shift, this.bits.size()));
    }

    public BitArray lshift(){
        BitArray result = new BitArray(this.bits.size()+1);
        result.set(0, false);
        for (int i = 0; i < this.bits.size(); i++)
            result.set(i+1, this.bits.get(i));
        return result;
    }
    public BitArray lshift(int shift){
        if(shift == 0)
            return this;
        for (int i = 0; i < shift; i++)
            this.lshift();
        return this;
    }

    public BitArray getFlipped(){
        ArrayList<Boolean> nv = new ArrayList<>(Collections.nCopies(this.bits.size(), false));
        for(int i = 0;i<this.bits.size();i++)
            nv.set(i, this.bits.get(this.bits.size() - i - 1));
        return new BitArray(nv);
    }

    public static byte[] shiftByteArray(byte[] bs, int bitcount){
        byte[] res = new byte[bs.length + (bitcount%8 == 0 ? 1 : 0)]; // si le tableau est pile plein, on ajoute une case à droite pour préserver les bits outshiftés.

        for(int i =0;i<bitcount;i++){
            byte bit = (byte)(bs[i/8]>>(7-i%8) & 0x01);
            res[(i+1)/8] += bit<<(7-(i+1)%8);
        }

        return res;
    }

    public char[] getChars(){
        char[] res = new char[(this.bits.size()+7)/8];
        for (int i = 0; i < this.bits.size(); i++)
            res[res.length-i/8-1] += (char) ((this.bits.get(i) ? 1 :0) << (i % 8));
        return res;
    }
    public byte[] getBytes(){
        byte[] res = new byte[(this.bits.size()+7)/8];
        for (int i = 0; i < this.bits.size(); i++)
            res[res.length-i/8-1] += (char) ((this.bits.get(i) ? 1 :0) << (i % 8));
        return res;
    }

    public String toHex(){
        StringBuilder sb = new StringBuilder();
        byte[] bytes = this.getBytes();
        for (byte b : bytes) {
            sb.append(byte2Hex(b));
            sb.append(" ");
        }
        return sb.toString();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        for(Boolean b : this.bits)
            sb.append(b ? '1': '0');
        sb.reverse();

        return sb.toString();
    }

    public int[] getInts(){
        int[] res = new int[(this.bits.size()+31)/32];

        for(int i = 0;i<this.bits.size();i++)
            res[i/32] = (res[i/32] << 1) + (this.get(this.bits.size() - i - 1) ? 1 : 0);

        return res;
    }

    public int getInt(){
        int res = 0;

        for(int i = 0;i<32;i++) {
            boolean bit = (32-i-1) >= this.bits.size() ? false : this.get(32-i-1);
            res = (res << 1) + (bit ? 1 : 0);
        }

        return res;
    }

    public long[] getLongs(){
        long[] res = new long[(this.bits.size()+63)/64];

        for(int i = 0;i<this.bits.size();i++)
            res[i/64] = (res[i/64] << 1) + (this.get(this.bits.size() - i - 1) ? 1 : 0);

        return res;
    }

    public long getLong(){
        long res = 0;

        for(int i = 0;i<64;i++) {
            boolean bit = (64 - i - 1) >= this.bits.size() ? false : this.get(64 - i - 1);
            res = (res << 1) + (bit ? 1 : 0);
        }

        return res;
    }

    public int getSize(){
        return this.bits.size();
    }

    public boolean equals(BitArray b){
        if(b == null)
            return false;
        if(this.bits.size() != b.bits.size())
            return false;
        for(int i = 0;i<this.getSize();i++)
            if(this.bits.get(i) != b.bits.get(i))
                return false;
        return true;
    }

    /**
     * Searches a bit pattern in the bit array.
     * @param needle The pattern to look for
     * @return The offset if >=0, -1 if not found
     */
    public int find(BitArray needle){
        if(this.getSize() < needle.getSize())
            return -1;
        for (int i = 0; i < this.getSize() - needle.getSize(); i++) {
            BitArray toCompare = this.getFlipped().get(i, needle.getSize()).getFlipped();
            if (needle.equals(toCompare))
                return i;
        }
        return -1;
    }

    /**
     * Searches a bit pattern in the bit array.
     * @param needle The pattern to look for
     * @param needleSize Size of the pattern
     * @return The offset if >=0, -1 if not found
     */
    public int find(long needle, int needleSize){
        BitArray needleBits = new BitArray(needle, needleSize);
        return this.find(needleBits);
    }

    /**
     * Checks if all bits are zeroes
     * @return All bits are zeroes
     */
    public boolean isNull(){
        for(Boolean b : this.bits)
            if(b)
                return false;
        return true;
    }
}

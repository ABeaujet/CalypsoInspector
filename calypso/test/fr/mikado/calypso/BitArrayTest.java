package fr.mikado.calypso;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by alexis on 15/03/17.
 */
public class BitArrayTest {

    @org.junit.Test
    public void testSet() throws Exception {
        BitArray test      = new BitArray(new byte[]{1, 3, 3, 4});
        BitArray expected = new BitArray(new byte[]{1, 2, 3, 4});

        test.set(16, false);
        assertArrayEquals(expected.getBytes(), test.getBytes());
    }

    @org.junit.Test
    public void testGet() throws Exception {
        BitArray test = new BitArray(new byte[]{1, 3, 3, 4, 6, 7});
        assertEquals(test.get(0), true);
    }

    @org.junit.Test
    public void testRshift() throws Exception {
        BitArray test =     new BitArray(new byte[]{2, 4, 6, 8, 13,            15});
        BitArray expected = new BitArray(new byte[]{1, 2, 3, 4,  6, (byte)(7+128)});

        assertArrayEquals(expected.getBytes(), test.rshift().getBytes());
    }

    @org.junit.Test
    public void testLshift() throws Exception {
        BitArray test =     new BitArray(new byte[]{   (byte)0x80, 2,  6, (byte)(7+128)});
        BitArray expected = new BitArray(new byte[]{1,          0, 4, 13,           14});

        assertArrayEquals(expected.getBytes(), test.lshift().getBytes());

    }

    @org.junit.Test
    public void testShiftByteArray() throws Exception {

    }

    @org.junit.Test
    public void testGetChars() throws Exception {
        BitArray test = new BitArray(new byte[]{'a', 'b', 'c', 'd'});
        char[] expected = new char[]{'a', 'b', 'c', 'd'};

        char[] result = test.getChars();
        assertArrayEquals(expected, result);
    }

    @org.junit.Test
    public void testGetBytes() throws Exception {
        BitArray test = new BitArray(new byte[]{'a', 'b', 'c', 'd'});
        byte[] expected = new byte[]{0x61, 0x62, 0x63, 0x64};

        byte[] result = test.getBytes();
        assertArrayEquals(expected, result);
    }

    @org.junit.Test
    public void testToHex() throws Exception {
        String expected = "61 62 63 64 ";
        BitArray test = new BitArray(new byte[]{'a', 'b', 'c', 'd'});

        assertEquals(expected, test.toHex());
    }

    @Test
    public void testGetInt() throws Exception {
        BitArray test = new BitArray(new byte[]{0x0c, 0x00, 0x00});
        int expected = 0x000c0000;

        int result = test.getInt();
        assertEquals(expected, result);
    }

    @Test
    public void testGetLong() throws Exception {
        BitArray test = new BitArray(new byte[]{0x0c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});
        long expected = 0x000c000000000000L;

        long result = test.getLong();
        assertEquals(expected, result);
    }

    @Test
    public void testConstructFromLong() throws Exception{
        BitArray expected = new BitArray(new byte[]{0x01, 0x02}, 7, 9);
        BitArray test = new BitArray(258L, 9);

        assertTrue(expected.equals(test));
    }

    @Test
    public void testEquals() throws Exception{
        BitArray val1 = new BitArray(new byte[]{0x01, 0x02}, 7, 9);
        BitArray val2 = new BitArray(new byte[]{0x01, 0x02}, 7, 9);

        assertTrue(val1.equals(val2));
    }

    @Test
    public void testHex2Bytes() throws Exception{
        String hex = "00ab000af88000";

        byte[] expected = new byte[]{0x00, (byte)0xab, 0x00, 0x0a, (byte)0xf8, (byte)0x80, 0x00};
        byte[] test = BitArray.hex2Bytes(hex);

        assertArrayEquals(expected, test);
    }
}
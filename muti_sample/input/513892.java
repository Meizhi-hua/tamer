public class BitwiseStreamsTest extends AndroidTestCase {
    private final static String LOG_TAG = "BitwiseStreamsTest";
    @SmallTest
    public void testOne() throws Exception {
        int offset = 3;
        byte[] inBuf = HexDump.hexStringToByteArray("FFDD");
        BitwiseOutputStream outStream = new BitwiseOutputStream(30);
        outStream.skip(offset);
        for (int i = 0; i < inBuf.length; i++) outStream.write(8, inBuf[i]);
        byte[] outBuf = outStream.toByteArray();
        BitwiseInputStream inStream = new BitwiseInputStream(outBuf);
        byte[] inBufDup = new byte[inBuf.length];
        inStream.skip(offset);
        for (int i = 0; i < inBufDup.length; i++) inBufDup[i] = (byte)inStream.read(8);
        assertEquals(HexDump.toHexString(inBuf), HexDump.toHexString(inBufDup));
    }
    @SmallTest
    public void testTwo() throws Exception {
        int offset = 3;
        byte[] inBuf = HexDump.hexStringToByteArray("11d4f29c0e9ad3c36e72584e064d9b53");
        BitwiseOutputStream outStream = new BitwiseOutputStream(30);
        outStream.skip(offset);
        for (int i = 0; i < inBuf.length; i++) outStream.write(8, inBuf[i]);
        BitwiseInputStream inStream = new BitwiseInputStream(outStream.toByteArray());
        inStream.skip(offset);
        byte[] inBufDup = new byte[inBuf.length];
        for (int i = 0; i < inBufDup.length; i++) inBufDup[i] = (byte)inStream.read(8);
        assertEquals(HexDump.toHexString(inBuf), HexDump.toHexString(inBufDup));
    }
    @SmallTest
    public void testThree() throws Exception {
        int offset = 4;
        byte[] inBuf = HexDump.hexStringToByteArray("00031040900112488ea794e0");
        BitwiseOutputStream outStream = new BitwiseOutputStream(30);
        outStream.skip(offset);
        for (int i = 0; i < inBuf.length; i++) outStream.write(8, inBuf[i]);
        BitwiseInputStream inStream = new BitwiseInputStream(outStream.toByteArray());
        inStream.skip(offset);
        byte[] inBufDup = new byte[inBuf.length];
        for (int i = 0; i < inBufDup.length; i++) inBufDup[i] = (byte)inStream.read(8);
        assertEquals(HexDump.toHexString(inBuf), HexDump.toHexString(inBufDup));
    }
    @SmallTest
    public void testFour() throws Exception {
        int offset = 7;
        byte[] inBuf = HexDump.hexStringToByteArray("00031040900112488ea794e0");
        BitwiseOutputStream outStream = new BitwiseOutputStream(30);
        outStream.skip(offset);
        for (int i = 0; i < inBuf.length; i++) {
            outStream.write(5, inBuf[i] >>> 3);
            outStream.write(3, inBuf[i] & 0x07);
        }
        BitwiseInputStream inStream = new BitwiseInputStream(outStream.toByteArray());
        inStream.skip(offset);
        byte[] inBufDup = new byte[inBuf.length];
        for (int i = 0; i < inBufDup.length; i++) inBufDup[i] = (byte)inStream.read(8);
        assertEquals(HexDump.toHexString(inBuf), HexDump.toHexString(inBufDup));
    }
    @SmallTest
    public void testFive() throws Exception {
        Random random = new Random();
        int iterations = 10000;
        int[] sizeArr = new int[iterations];
        int[] valueArr = new int[iterations];
        BitwiseOutputStream outStream = new BitwiseOutputStream(iterations * 4);
        for (int i = 0; i < iterations; i++) {
            int x = random.nextInt();
            int size = (x & 0x07) + 1;
            int value = x & (-1 >>> (32 - size));
            sizeArr[i] = size;
            valueArr[i] = value;
            outStream.write(size, value);
        }
        BitwiseInputStream inStream = new BitwiseInputStream(outStream.toByteArray());
        for (int i = 0; i < iterations; i++) {
            assertEquals(valueArr[i], inStream.read(sizeArr[i]));
        }
    }
    @SmallTest
    public void testSix() throws Exception {
        int num_runs = 10;
        long start = android.os.SystemClock.elapsedRealtime();
        for (int run = 0; run < num_runs; run++) {
            int offset = run % 8;
            byte[] inBuf = HexDump.hexStringToByteArray("00031040900112488ea794e0");
            BitwiseOutputStream outStream = new BitwiseOutputStream(30);
            outStream.skip(offset);
            for (int i = 0; i < inBuf.length; i++) {
                outStream.write(5, inBuf[i] >>> 3);
                outStream.write(3, inBuf[i] & 0x07);
            }
            BitwiseInputStream inStream = new BitwiseInputStream(outStream.toByteArray());
            inStream.skip(offset);
            byte[] inBufDup = new byte[inBuf.length];
            for (int i = 0; i < inBufDup.length; i++) inBufDup[i] = (byte)inStream.read(8);
            assertEquals(HexDump.toHexString(inBuf), HexDump.toHexString(inBufDup));
        }
        long end = android.os.SystemClock.elapsedRealtime();
        Log.d(LOG_TAG, "repeated encode-decode took " + (end - start) + " ms");
    }
}

@TestTargetClass(GZIPInputStream.class)
public class GZIPInputStreamTest extends junit.framework.TestCase {
    File resources;
    class TestGZIPInputStream extends GZIPInputStream {
        TestGZIPInputStream(InputStream in) throws IOException {
            super(in);
        }
        TestGZIPInputStream(InputStream in, int size) throws IOException {
            super(in, size);
        }
        Checksum getChecksum() {
            return crc;
        }
        boolean endofInput() {
            return eos;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPInputStream",
        args = {java.io.InputStream.class}
    )
    public void test_ConstructorLjava_io_InputStream() {
        try {
            Support_Resources.copyFile(resources, "GZIPInputStream",
                    "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString()
                    + "/GZIPInputStream/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream());
            assertNotNull("the constructor for GZIPInputStream is null", inGZIP);
            assertEquals("the CRC value of the inputStream is not zero", 0,
                    inGZIP.getChecksum().getValue());
            inGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to open the input file");
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "GZIPInputStream",
        args = {java.io.InputStream.class, int.class}
    )
    public void test_ConstructorLjava_io_InputStreamI() {
        try {
            Support_Resources.copyFile(resources, "GZIPInputStream",
                    "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString()
                    + "/GZIPInputStream/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream(), 200);
            assertNotNull("the constructor for GZIPInputStream is null", inGZIP);
            assertEquals("the CRC value of the inputStream is not zero", 0,
                    inGZIP.getChecksum().getValue());
            inGZIP.close();
        } catch (IOException e) {
            fail("an IO error occured while trying to open the input file");
        }
        try {
            Support_Resources.copyFile(resources, "GZIPInputStream",
                    "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString()
                    + "/GZIPInputStream/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream(), 0);
            fail("Expected IllegalArgumentException");
        } catch (IOException e) {
            fail("an IO error occured while trying to open the input file");
        } catch (IllegalArgumentException ee) {
        }
        try {
            Support_Resources.copyFile(resources, "GZIPInputStream",
                    "hyts_gInput.txt.gz");
            final URL gInput = new File(resources.toString()
                    + "/GZIPInputStream/hyts_gInput.txt.gz").toURL();
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream(), -1);
            fail("Expected IllegalArgumentException");
        } catch (IOException e) {
            fail("an IO error occured while trying to open the input file");
        } catch (IllegalArgumentException ee) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "read",
        args = {byte[].class, int.class, int.class}
    )
    public void test_read$BII() throws IOException {
        byte orgBuf[] = {'3', '5', '2', 'r', 'g', 'e', 'f', 'd', 'e', 'w'};
        byte outBuf[] = new byte[100];
        int result = 0;
        Support_Resources.copyFile(resources, "GZIPInputStream",
                "hyts_gInput.txt.gz");
        String resPath = resources.toString();
        if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\') {
            resPath = resPath.substring(1);
        }
        final URL gInput = new URL("file:/" + resPath
                + "/GZIPInputStream/hyts_gInput.txt.gz");
        TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                .openConnection().getInputStream());
        while (!(inGZIP.endofInput())) {
            result += inGZIP.read(outBuf, result, outBuf.length - result);
        }
        assertEquals(
                "the checkSum value of the compressed and decompressed data does not equal",
                2074883667L, inGZIP.getChecksum().getValue());
        for (int i = 0; i < orgBuf.length; i++) {
            assertTrue(
                    "the decompressed data does not equal the original data decompressed",
                    orgBuf[i] == outBuf[i]);
        }
        int r = 0;
        try {
            inGZIP.read(outBuf, 100, 1);
        } catch (IndexOutOfBoundsException e) {
            r = 1;
        }
        inGZIP.close();
        byte[] test = new byte[507];
        for (int i = 0; i < 256; i++) {
            test[i] = (byte) i;
        }
        for (int i = 256; i < test.length; i++) {
            test[i] = (byte) (256 - i);
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        GZIPOutputStream out = new GZIPOutputStream(bout);
        out.write(test);
        out.close();
        byte[] comp = bout.toByteArray();
        GZIPInputStream gin2 = new GZIPInputStream(new ByteArrayInputStream(
                comp), 512);
        int total = 0;
        while ((result = gin2.read(test)) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertTrue("Incorrectly decompressed", total == test.length);
        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512);
        total = 0;
        while ((result = gin2.read(new byte[200])) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertTrue("Incorrectly decompressed", total == test.length);
        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 516);
        total = 0;
        while ((result = gin2.read(new byte[200])) != -1) {
            total += result;
        }
        assertEquals("Should return -1", -1, gin2.read());
        gin2.close();
        assertTrue("Incorrectly decompressed", total == test.length);
        comp[40] = 0;
        gin2 = new GZIPInputStream(new ByteArrayInputStream(comp), 512);
        boolean exception = false;
        try {
            while (gin2.read(test) != -1) {
                ;
            }
        } catch (IOException e) {
            exception = true;
        }
        assertTrue("Exception expected", exception);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPOutputStream zipout = new GZIPOutputStream(baos);
        zipout.write(test);
        zipout.close();
        outBuf = new byte[530];
        GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(baos
                .toByteArray()));
        try {
            in.read(outBuf, 530, 1);
            fail("Test failed IOOBE was not thrown");
        } catch (IndexOutOfBoundsException e) {
        }
        while (true) {
            result = in.read(outBuf, 0, 5);
            if (result == -1) {
                break;
            }
        }
        result = -10;
        result = in.read(null, 100, 1);
        result = in.read(outBuf, -100, 1);
        result = in.read(outBuf, -1, 1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "close",
        args = {}
    )
    public void test_close() {
        byte outBuf[] = new byte[100];
        try {
            int result = 0;
            Support_Resources.copyFile(resources, "GZIPInputStream",
                    "hyts_gInput.txt.gz");
            String resPath = resources.toString();
            if (resPath.charAt(0) == '/' || resPath.charAt(0) == '\\') {
                resPath = resPath.substring(1);
            }
            final URL gInput = new URL("file:/" + resPath
                    + "/GZIPInputStream/hyts_gInput.txt.gz");
            TestGZIPInputStream inGZIP = new TestGZIPInputStream(gInput
                    .openConnection().getInputStream());
            while (!(inGZIP.endofInput())) {
                result += inGZIP.read(outBuf, result, outBuf.length - result);
            }
            assertEquals(
                    "the checkSum value of the compressed and decompressed data does not equal",
                    2074883667L, inGZIP.getChecksum().getValue());
            inGZIP.close();
            int r = 0;
            try {
                inGZIP.read(outBuf, 0, 1);
            } catch (IOException e) {
                r = 1;
            }
            assertEquals(
                    "GZIPInputStream can still be used after close is called",
                    1, r);
        } catch (IOException e) {
            e.printStackTrace();
            fail("unexpected: " + e);
        }
    }
    @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "read",
            args = {byte[].class}
    )
    public void test_read() throws IOException {
        GZIPInputStream gis = null;
        int result = 0;
        byte[] buffer = new byte[] {1,2,3,4,5,6,7,8,9,10};
        File f = new File(resources.getAbsolutePath() + "test.gz");
        FileOutputStream out = new FileOutputStream(f);
        GZIPOutputStream gout = new GZIPOutputStream(out);
        for(int i = 0; i < 10; i++) {
            gout.write(buffer);
        }
        gout.finish();
        out.write(1);
        out.close();
        gis = new GZIPInputStream(new FileInputStream(f));
        buffer = new byte[100];
        gis.read(buffer);
        result = gis.read();
        gis.close();
        f.delete();
        assertEquals("Incorrect value returned at the end of the file", -1, result);
    }
	@Override
    protected void setUp() {
        resources = Support_Resources.createTempFolder();
    }
    @Override
    protected void tearDown() {
    }
}

public class SubbufferLongLongBoolean {
    static float[] testarray;
    static byte[] test_byte_array;
    static File test_file;
    static AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
    static void setUp() throws Exception {
        testarray = new float[1024];
        for (int i = 0; i < 1024; i++) {
            double ii = i / 1024.0;
            ii = ii * ii;
            testarray[i] = (float)Math.sin(10*ii*2*Math.PI);
            testarray[i] += (float)Math.sin(1.731 + 2*ii*2*Math.PI);
            testarray[i] += (float)Math.sin(0.231 + 6.3*ii*2*Math.PI);
            testarray[i] *= 0.3;
        }
        test_byte_array = new byte[testarray.length*2];
        AudioFloatConverter.getConverter(format).toByteArray(testarray, test_byte_array);
        test_file = File.createTempFile("test", ".raw");
        FileOutputStream fos = new FileOutputStream(test_file);
        fos.write(test_byte_array);
    }
    static void tearDown() throws Exception {
        if(!test_file.delete())
            test_file.deleteOnExit();
    }
    public static void main(String[] args) throws Exception {
        try
        {
            setUp();
            for (int i = 0; i < 2; i++) {
                ModelByteBuffer buff;
                if(i == 0)
                    buff = new ModelByteBuffer(test_file);
                else
                    buff = new ModelByteBuffer(test_byte_array);
                ModelByteBuffer buff2 = buff.subbuffer(10,21,true);
                if(buff2.getRoot() != buff2);
                if(buff2.capacity() != 11);
                if(i == 0)
                {
                    if(buff2.getFilePointer() != buff.getFilePointer()+10)
                        throw new RuntimeException("buff2.getFilePointer() incorrect!");
                }
                else
                {
                    if(buff2.arrayOffset() != 10)
                        throw new RuntimeException("buff2.arrayOffset() not 10!");
                }
            }
        }
        finally
        {
            tearDown();
        }
    }
}
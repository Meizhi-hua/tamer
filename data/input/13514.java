public class ReadWritePrimitives {
    public static void main(String args[]) throws IOException {
        long start, finish;
        start = System.currentTimeMillis();
        testShort();
        finish = System.currentTimeMillis();
        start = System.currentTimeMillis();
        testChar();
        finish = System.currentTimeMillis();
        start = System.currentTimeMillis();
        testInt();
        finish = System.currentTimeMillis();
        start = System.currentTimeMillis();
        testLong();
        finish = System.currentTimeMillis();
    }
    private static void testShort() throws IOException {
        File fh = new File(System.getProperty("test.dir", "."),
                          "x.ReadWriteGenerated");
        RandomAccessFile f = new RandomAccessFile(fh,"rw");
        for(int i = 0; i < 10000; i++){
            f.writeShort((short)i);
        }
        f.writeShort((short)65535);
        f.close();
        f = new RandomAccessFile(fh,"r");
        for(int i = 0; i < 10000; i++) {
            short r = f.readShort();
            if (r != ((short)i)) {
                System.err.println("An error occurred. Read:" + r
                                   + " i:" + ((short)i));
                throw new IOException("Bad read from a writeShort");
            }
        }
        short rmax = f.readShort();
        if (rmax != ((short)65535)) {
            System.err.println("An error occurred. Read:" + rmax);
            throw new IOException("Bad read from a writeShort");
        }
        f.close();
    }
    private static void testChar() throws IOException {
        File fh = new File(System.getProperty("test.dir", "."),
                          "x.ReadWriteGenerated");
        RandomAccessFile f = new RandomAccessFile(fh,"rw");
        for(int i = 0; i < 10000; i++){
            f.writeChar((char)i);
        }
        f.close();
        f = new RandomAccessFile(fh,"r");
        for(int i = 0; i < 10000; i++) {
            char r = f.readChar();
            if (r != ((char)i)){
                System.err.println("An error occurred. Read:" + r
                                   + " i:" + ((char) i));
                throw new IOException("Bad read from a writeChar");
            }
        }
        f.close();
    }
    private static void testInt() throws IOException {
        File fh = new File(System.getProperty("test.dir", "."),
                          "x.ReadWriteGenerated");
        RandomAccessFile f = new RandomAccessFile(fh,"rw");
        for(int i = 0; i < 10000; i++){
            f.writeInt((short)i);
        }
        f.writeInt(Integer.MAX_VALUE);
        f.close();
        f = new RandomAccessFile(fh, "r");
        for(int i = 0; i < 10000; i++) {
            int r = f.readInt();
            if (r != i){
                System.err.println("An error occurred. Read:" + r
                                   + " i:" + i);
                throw new IOException("Bad read from a writeInt");
            }
        }
        int rmax = f.readInt();
        if (rmax != Integer.MAX_VALUE){
            System.err.println("An error occurred. Read:" + rmax);
            throw new IOException("Bad read from a writeInt");
        }
        f.close();
    }
    private static void testLong() throws IOException {
        File fh = new File(System.getProperty("test.dir", "."),
                          "x.ReadWriteGenerated");
        RandomAccessFile f = new RandomAccessFile(fh,"rw");
        for(int i = 0; i < 10000; i++){
            f.writeLong(123456789L * (long)i);
        }
        f.close();
        f = new RandomAccessFile(fh,"r");
        for(int i = 0; i < 10000; i++){
            long r = f.readLong();
            if (r != (((long) i) * 123456789L) ) {
                System.err.println("An error occurred. Read:" + r
                                   + " i" + ((long) i));
                throw new IOException("Bad read from a writeInt");
            }
        }
        f.close();
    }
}

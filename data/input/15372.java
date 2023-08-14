class Traffic
{
    private InputStream         in;
    private OutputStream        out;
    static private byte fixedSeed [] = { 1, 2, 3, 4};
    private SecureRandom        prng;
    private boolean             compareRandom = true;
    Traffic (InputStream in, OutputStream out)
    {
        this.in = in;
        this.out = out;
        try {
            prng = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        prng.setSeed(fixedSeed);
    }
    public void setPRNG (SecureRandom prng)
    {
        this.prng = prng;
        compareRandom = false;
    }
    public void initiate (int n)
    throws IOException
    {
        if (n == 0)
            initiateConst ();
        else if (n < 0)
            System.out.println ("** ERROR:  initiate forever ??");
        else
            for ( ; n > 0; n -= 1) {
                initiateRandom ();
            }
    }
    public void respond (int n)
    throws IOException
    {
        if (n == 0)
            respondConst ();
        else if (n < 0)                 
            while (true)
                respondRandom ();
        else
            while (n-- > 0)
                respondRandom ();
    }
    private static final int MAX_BLOCKSIZE = 8 * 2;
    private void writeConstData (int n)
    throws IOException
    {
        if (n <= 0)
            return;
        byte buf [] = new byte [n];
        for (int i = 0; i < n; i++)
            buf [i] = (byte) i;
        out.write (buf);
    }
    private void readConstData (int n)
    throws IOException
    {
        if (n <= 0)
            return;
        byte buf [] = new byte [n];
        in.read (buf);
        for (int i = 0; i < n; i++)
            if (buf [i] != (byte) i)
                throw new IOException ("const data was incorrect, "
                    + "n = " + n + ", i = " + i);
    }
    private void initiateConst ()
    throws IOException
    {
        for (int i = 1; i <= MAX_BLOCKSIZE; i++) {
            writeConstData (i);
            readConstData (i);
        }
    }
    private void respondConst ()
    throws IOException
    {
        for (int i = 1; i <= MAX_BLOCKSIZE; i++) {
            readConstData (i);
            writeConstData (i);
        }
    }
    private static final int MAX_RECORDSIZE = 16384 * 2;
    private int nextRecordSize ()
    {
        double  d = prng.nextGaussian ();
        int     n;
        if ((prng.nextInt () % 3)  == 0) {
            n = (int) (d * 2048);
            n += 15 * 1024;
        } else {
            n = (int) (d * 4096);
            n += 1024;
        }
        if (n < 0)
            return nextRecordSize ();
        else if (n > MAX_RECORDSIZE)
            return MAX_RECORDSIZE;
        else
            return n;
    }
    private void writeRandomData ()
    throws IOException
    {
        int n = nextRecordSize ();
        byte buf [] = new byte [n];
        prng.nextBytes (buf);
        writeInt (n);
        out.write (buf);
    }
    private void readRandomData ()
    throws IOException
    {
        int     n = readInt ();
        byte    actual [] = new byte [n];
        readFully (actual);
        if (compareRandom) {
            byte        expected [];
            if (n != nextRecordSize ())
                throw new IOException ("wrong record size");
            expected = new byte [n];
            prng.nextBytes (expected);
            for (int i = 0; i < n; i++)
                if (actual [i] != expected [i])
                    throw new IOException ("random data was incorrect, "
                        + "n = " + n + ", i = " + i);
        }
    }
    private void initiateRandom ()
    throws IOException
    {
        writeRandomData ();
        readRandomData ();
    }
    private void respondRandom ()
    throws IOException
    {
        readRandomData ();
        writeRandomData ();
    }
    private void readFully (byte buf [])
    throws IOException
    {
        int len = buf.length;
        int offset = 0;
        int value;
        while (len > 0) {
            value = in.read (buf, offset, len);
            if (value == -1)
                throw new EOFException ("read buffer");
            offset += value;
            len -= value;
        }
    }
    private int readInt ()
    throws IOException
    {
        int b0, b1, b2, b3;
        int n;
        b0 = in.read ();
        b1 = in.read ();
        b2 = in.read ();
        b3 = in.read ();
        if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0)
            throw new EOFException ();
        n =  (b3 & 0x0ff);
        n |= (b2 & 0x0ff) << 8;
        n |= (b1 & 0x0ff) << 16;
        n |= (b0 & 0x0ff) << 24;
        return n;
    }
    private void writeInt (int n)
    throws IOException
    {
        int b0, b1, b2, b3;
        b3 = n & 0x0ff;
        n >>= 8;
        b2 = n & 0x0ff;
        n >>= 8;
        b1 = n & 0x0ff;
        n >>= 8;
        b0 = n & 0x0ff;
        out.write (b0);
        out.write (b1);
        out.write (b2);
        out.write (b3);
    }
}

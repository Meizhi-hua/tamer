public class JIS0213 {
    static Pattern sjis0213 = Pattern.compile("0x(\\p{XDigit}++)\\s++U\\+(\\p{XDigit}++)(?:\\+(\\p{XDigit}++))?\\s++#.*");
    static void genClass(String argv[]) throws IOException
    {
        InputStream in = new FileInputStream(argv[0]) ;
        OutputStream out = new FileOutputStream(argv[1]);
        int[] sb = new int[0x100];                         
        int[] db = new int[0x10000];                       
        int[] indexC2B = new int[256];
        Entry[] supp = new Entry[0x10000];
        Entry[] comp = new Entry[0x100];
        int suppTotal = 0;
        int compTotal = 0;
        int b1Min1 = 0x81;
        int b1Max1 = 0x9f;
        int b1Min2 = 0xe0;
        int b1Max2 = 0xfc;
        int b2Min = 0x40;
        int b2Max = 0xfe;
        for (int i = 0; i < 0x80; i++) sb[i] = i;
        for (int i = 0x80; i < 0x100; i++) sb[i] = UNMAPPABLE_DECODING;
        for (int i = 0; i < 0x10000; i++) db[i] = UNMAPPABLE_DECODING;
        try {
            Parser p = new Parser(in, sjis0213);
            Entry  e = null;
            while ((e = p.next()) != null) {
                if (e.cp2 != 0) {
                    comp[compTotal++] = e;
                } else {
                    if (e.cp <= 0xffff) {
                        if (e.bs <= 0xff)
                            sb[e.bs] = e.cp;
                        else
                            db[e.bs] = e.cp;
                        indexC2B[e.cp>>8] = 1;
                    } else {
                        supp[suppTotal++] = e;
                    }
                }
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writeINDEXC2B(baos, indexC2B);
            writeSINGLEBYTE(baos, sb);
            writeDOUBLEBYTE1(baos, db, b1Min1, b1Max1, b2Min, b2Max);
            writeDOUBLEBYTE2(baos, db, b1Min2, b1Max2, b2Min, b2Max);
            writeSUPPLEMENT(baos, supp, suppTotal);
            writeCOMPOSITE(baos, comp, compTotal);
            writeSIZE(out, baos.size());
            baos.writeTo(out);
            out.close();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    static Comparator<Entry> comparatorCP =
        new Comparator<Entry>() {
            public int compare(Entry m1, Entry m2) {
                return m1.cp - m2.cp;
            }
            public boolean equals(Object obj) {
                return this == obj;
            }
    };
    private final static int MAP_SINGLEBYTE      = 0x1; 
    private final static int MAP_DOUBLEBYTE1     = 0x2; 
    private final static int MAP_DOUBLEBYTE2     = 0x3; 
    private final static int MAP_SUPPLEMENT      = 0x5; 
    private final static int MAP_SUPPLEMENT_C2B  = 0x6; 
    private final static int MAP_COMPOSITE       = 0x7; 
    private final static int MAP_INDEXC2B        = 0x8; 
    private static final void writeShort(OutputStream out, int data)
        throws IOException
    {
        out.write((data >>> 8) & 0xFF);
        out.write((data      ) & 0xFF);
    }
    private static final void writeShortArray(OutputStream out,
                                              int type,
                                              int[] array,
                                              int off,
                                              int size)   
        throws IOException
    {
        writeShort(out, type);
        writeShort(out, size);
        for (int i = off; i < size; i++) {
            writeShort(out, array[off+i]);
        }
    }
    private static final void writeSIZE(OutputStream out, int data)
        throws IOException
    {
        out.write((data >>> 24) & 0xFF);
        out.write((data >>> 16) & 0xFF);
        out.write((data >>>  8) & 0xFF);
        out.write((data       ) & 0xFF);
    }
    private static void writeINDEXC2B(OutputStream out, int[] indexC2B)
        throws IOException
    {
        writeShort(out, MAP_INDEXC2B);
        writeShort(out, indexC2B.length);
        int off = 0;
        for (int i = 0; i < indexC2B.length; i++) {
            if (indexC2B[i] != 0) {
                writeShort(out, off);
                off += 256;
            } else {
                writeShort(out, -1);
            }
        }
    }
    private static void writeSINGLEBYTE(OutputStream out, int[] sb)
        throws IOException
    {
        writeShortArray(out, MAP_SINGLEBYTE, sb, 0, 256);
    }
    private static void writeDOUBLEBYTE(OutputStream out,
                                        int type,
                                        int[] db,
                                        int b1Min, int b1Max,
                                        int b2Min, int b2Max)
        throws IOException
    {
        writeShort(out, type);
        writeShort(out, b1Min);
        writeShort(out, b1Max);
        writeShort(out, b2Min);
        writeShort(out, b2Max);
        writeShort(out, (b1Max - b1Min + 1) * (b2Max - b2Min + 1));
        for (int b1 = b1Min; b1 <= b1Max; b1++) {
            for (int b2 = b2Min; b2 <= b2Max; b2++) {
                writeShort(out, db[b1 * 256 + b2]);
            }
        }
    }
    private static void writeDOUBLEBYTE1(OutputStream out,
                                        int[] db,
                                        int b1Min, int b1Max,
                                        int b2Min, int b2Max)
        throws IOException
    {
        writeDOUBLEBYTE(out, MAP_DOUBLEBYTE1, db, b1Min, b1Max, b2Min, b2Max);
    }
    private static void writeDOUBLEBYTE2(OutputStream out,
                                        int[] db,
                                        int b1Min, int b1Max,
                                        int b2Min, int b2Max)
        throws IOException
    {
        writeDOUBLEBYTE(out, MAP_DOUBLEBYTE2, db, b1Min, b1Max, b2Min, b2Max);
    }
    private static void writeSUPPLEMENT(OutputStream out, Entry[] supp, int size)
        throws IOException
    {
        writeShort(out, MAP_SUPPLEMENT);
        writeShort(out, size * 2);
        for (int i = 0; i < size; i++) {
            writeShort(out, supp[i].bs);
        }
        for (int i = 0; i < size; i++) {
            writeShort(out, supp[i].cp);
        }
        writeShort(out, MAP_SUPPLEMENT_C2B);
        writeShort(out, size*2);
        Arrays.sort(supp, 0, size, comparatorCP);
        for (int i = 0; i < size; i++) {
            writeShort(out, supp[i].cp);
        }
        for (int i = 0; i < size; i++) {
            writeShort(out, supp[i].bs);
        }
    }
    private static void writeCOMPOSITE(OutputStream out, Entry[] comp, int size)
        throws IOException
    {
        writeShort(out, MAP_COMPOSITE);
        writeShort(out, size*3);
        for (int i = 0; i < size; i++) {
            writeShort(out, (char)comp[i].bs);
            writeShort(out, (char)comp[i].cp);
            writeShort(out, (char)comp[i].cp2);
        }
    }
}

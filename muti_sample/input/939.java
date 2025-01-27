class CharacterName {
    private static SoftReference<byte[]> refStrPool;
    private static int[][] lookup;
    private static synchronized byte[] initNamePool() {
        byte[] strPool = null;
        if (refStrPool != null && (strPool = refStrPool.get()) != null)
            return strPool;
        DataInputStream dis = null;
        try {
            dis = new DataInputStream(new InflaterInputStream(
                AccessController.doPrivileged(new PrivilegedAction<InputStream>()
                {
                    public InputStream run() {
                        return getClass().getResourceAsStream("uniName.dat");
                    }
                })));
            lookup = new int[(Character.MAX_CODE_POINT + 1) >> 8][];
            int total = dis.readInt();
            int cpEnd = dis.readInt();
            byte ba[] = new byte[cpEnd];
            dis.readFully(ba);
            int nameOff = 0;
            int cpOff = 0;
            int cp = 0;
            do {
                int len = ba[cpOff++] & 0xff;
                if (len == 0) {
                    len = ba[cpOff++] & 0xff;
                    cp = ((ba[cpOff++] & 0xff) << 16) |
                         ((ba[cpOff++] & 0xff) <<  8) |
                         ((ba[cpOff++] & 0xff));
                }  else {
                    cp++;
                }
                int hi = cp >> 8;
                if (lookup[hi] == null) {
                    lookup[hi] = new int[0x100];
                }
                lookup[hi][cp&0xff] = (nameOff << 8) | len;
                nameOff += len;
            } while (cpOff < cpEnd);
            strPool = new byte[total - cpEnd];
            dis.readFully(strPool);
            refStrPool = new SoftReference<>(strPool);
        } catch (Exception x) {
            throw new InternalError(x.getMessage());
        } finally {
            try {
                if (dis != null)
                    dis.close();
            } catch (Exception xx) {}
        }
        return strPool;
    }
    public static String get(int cp) {
        byte[] strPool = null;
        if (refStrPool == null || (strPool = refStrPool.get()) == null)
            strPool = initNamePool();
        int off = 0;
        if (lookup[cp>>8] == null ||
            (off = lookup[cp>>8][cp&0xff]) == 0)
            return null;
        return new String(strPool, 0, off >>> 8, off & 0xff);  
    }
}

abstract class OutputStreamTests extends OutputTests {
    private static Group streamRoot;
    private static Group streamTestRoot;
    public static void init() {
        streamRoot = new Group(outputRoot, "stream",
                               "Image Stream Benchmarks");
        streamTestRoot = new Group(streamRoot, "tests",
                                   "ImageOutputStream Tests");
        new IOSConstruct();
        new IOSWrite();
        new IOSWriteByteArray();
        new IOSWriteBit();
        new IOSWriteByte();
        new IOSWriteShort();
        new IOSWriteInt();
        new IOSWriteFloat();
        new IOSWriteLong();
        new IOSWriteDouble();
    }
    protected OutputStreamTests(Group parent,
                                String nodeName, String description)
    {
        super(parent, nodeName, description);
        addDependency(generalDestRoot);
        addDependencies(imageioGeneralOptRoot, true);
    }
    public void cleanupTest(TestEnvironment env, Object ctx) {
        Context iioctx = (Context)ctx;
        iioctx.cleanup(env);
    }
    private static class Context extends OutputTests.Context {
        ImageOutputStream outputStream;
        int scanlineStride; 
        int length; 
        byte[] byteBuf;
        Context(TestEnvironment env, Result result) {
            super(env, result);
            scanlineStride = size * 4;
            length = (scanlineStride * size) + 4;
            byteBuf = new byte[scanlineStride];
            initOutput();
            try {
                outputStream = createImageOutputStream();
            } catch (IOException e) {
                System.err.println("Error creating ImageOutputStream");
            }
        }
        void cleanup(TestEnvironment env) {
            super.cleanup(env);
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    System.err.println("error closing stream");
                }
                outputStream = null;
            }
        }
    }
    private static class IOSConstruct extends OutputStreamTests {
        public IOSConstruct() {
            super(streamTestRoot,
                  "construct",
                  "Construct");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("stream");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            try {
                do {
                    ImageOutputStream ios = octx.createImageOutputStream();
                    ios.close();
                    octx.closeOriginalStream();
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static class IOSWrite extends OutputStreamTests {
        public IOSWrite() {
            super(streamTestRoot,
                  "write",
                  "write()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos >= length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.write(0);
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteByteArray extends OutputStreamTests {
        public IOSWriteByteArray() {
            super(streamTestRoot,
                  "writeByteArray",
                  "write(byte[]) (one \"scanline\" at a time)");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(ctx.scanlineStride);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final byte[] buf = octx.byteBuf;
            final int scanlineStride = octx.scanlineStride;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + scanlineStride > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.write(buf);
                    pos += scanlineStride;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteBit extends OutputStreamTests {
        public IOSWriteBit() {
            super(streamTestRoot,
                  "writeBit",
                  "writeBit()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("bit");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length * 8; 
            int pos = 0; 
            try {
                ios.mark();
                do {
                    if (pos >= length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeBit(0);
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteByte extends OutputStreamTests {
        public IOSWriteByte() {
            super(streamTestRoot,
                  "writeByte",
                  "writeByte()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(1);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos >= length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeByte(0);
                    pos++;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteShort extends OutputStreamTests {
        public IOSWriteShort() {
            super(streamTestRoot,
                  "writeShort",
                  "writeShort()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(2);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + 2 > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeShort(0);
                    pos += 2;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteInt extends OutputStreamTests {
        public IOSWriteInt() {
            super(streamTestRoot,
                  "writeInt",
                  "writeInt()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(4);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + 4 > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeInt(0);
                    pos += 4;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteFloat extends OutputStreamTests {
        public IOSWriteFloat() {
            super(streamTestRoot,
                  "writeFloat",
                  "writeFloat()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(4);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + 4 > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeFloat(0.0f);
                    pos += 4;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteLong extends OutputStreamTests {
        public IOSWriteLong() {
            super(streamTestRoot,
                  "writeLong",
                  "writeLong()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(8);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + 8 > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeLong(0L);
                    pos += 8;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
    private static class IOSWriteDouble extends OutputStreamTests {
        public IOSWriteDouble() {
            super(streamTestRoot,
                  "writeDouble",
                  "writeDouble()");
        }
        public Object initTest(TestEnvironment env, Result result) {
            Context ctx = new Context(env, result);
            result.setUnits(8);
            result.setUnitName("byte");
            return ctx;
        }
        public void runTest(Object ctx, int numReps) {
            final Context octx = (Context)ctx;
            final ImageOutputStream ios = octx.outputStream;
            final int length = octx.length;
            int pos = 0;
            try {
                ios.mark();
                do {
                    if (pos + 8 > length) {
                        ios.reset();
                        ios.mark();
                        pos = 0;
                    }
                    ios.writeDouble(0.0);
                    pos += 8;
                } while (--numReps >= 0);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try { ios.reset(); } catch (IOException e) {}
            }
        }
    }
}

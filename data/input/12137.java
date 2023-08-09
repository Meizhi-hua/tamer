public class LotsOfWrites {
    static final Random rand = new Random();
    static class Writer implements CompletionHandler<Integer,ByteBuffer> {
        private final File file;
        private final long size;
        private final CountDownLatch latch;
        private final AsynchronousFileChannel channel;
        private volatile long position;
        private volatile byte nextByte;
        private long updatePosition(long nwrote) {
            position += nwrote;
            return position;
        }
        private ByteBuffer genNextBuffer() {
            int n = Math.min(8192 + rand.nextInt(8192), (int)(size - position));
            ByteBuffer buf = ByteBuffer.allocate(n);
            for (int i=0; i<n; i++) {
                buf.put(nextByte++);
            }
            buf.flip();
            return buf;
        }
        private void done() {
            try {
                channel.close();
            } catch (IOException ignore) { }
            latch.countDown();
        }
        Writer(File file, long size, CountDownLatch latch) throws IOException {
            this.file = file;
            this.size = size;
            this.latch = latch;
            this.channel = AsynchronousFileChannel.open(file.toPath(), WRITE);
        }
        File file() {
            return file;
        }
        long size() {
            return size;
        }
        void start() {
            ByteBuffer buf = genNextBuffer();
            channel.write(buf, 0L, buf, this);
        }
        @Override
        public void completed(Integer nwrote, ByteBuffer buf) {
            long pos = updatePosition(nwrote);
            if (!buf.hasRemaining()) {
                if (position >= size) {
                    done();
                    return;
                }
                buf = genNextBuffer();
            }
            channel.write(buf, pos, buf, this);
        }
        @Override
        public void failed(Throwable exc, ByteBuffer buf) {
            exc.printStackTrace();
            done();
        }
    }
    public static void main(String[] args) throws Exception {
        int count = 20 + rand.nextInt(16);
        Writer[] writers = new Writer[count];
        CountDownLatch latch = new CountDownLatch(count);
        for (int i=0; i<count; i++) {
            long size = 512*1024 + rand.nextInt(512*1024);
            File blah = File.createTempFile("blah", null);
            blah.deleteOnExit();
            Writer writer = new Writer(blah, size, latch);
            writers[i] = writer;
            writer.start();
        }
        latch.await();
        boolean failed = false;
        byte[] buf = new byte[8192];
        for (int i=0; i<count ;i++) {
            Writer writer = writers[i];
            FileInputStream in = new FileInputStream(writer.file());
            try {
                long size = 0L;
                byte expected = 0;
                int nread = in.read(buf);
                while (nread > 0) {
                    for (int j=0; j<nread; j++) {
                        if (buf[j] != expected) {
                            System.err.println("Unexpected contents");
                            failed = true;
                            break;
                        }
                        expected++;
                    }
                    if (failed)
                        break;
                    size += nread;
                    nread = in.read(buf);
                }
                if (!failed && size != writer.size()) {
                    System.err.println("Unexpected size");
                    failed = true;
                }
                if (failed)
                    break;
            } finally {
                in.close();
            }
        }
        for (int i=0; i<count; i++) {
            writers[i].file().delete();
        }
        if (failed)
            throw new RuntimeException("Test failed");
    }
}

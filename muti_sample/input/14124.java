public class ResponseCacheStream implements HttpCallback {
    void okReply (HttpTransaction req) throws IOException {
        req.setResponseEntityBody ("Hello, This is the response body. Let's make it as long as possible since we need to test the cache mechanism.");
        req.sendResponse (200, "Ok");
            System.out.println ("Server: sent response");
        req.orderlyClose();
    }
    public void request (HttpTransaction req) {
        try {
            okReply (req);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    static class MyCacheRequest extends CacheRequest {
        private OutputStream buf = null;
        public MyCacheRequest(OutputStream out) {
            buf = out;
        }
        public OutputStream getBody() throws IOException {
            return buf;
        }
        public void abort() {
        }
    }
    static class MyResponseCache extends ResponseCache {
        private ByteArrayOutputStream buf = new ByteArrayOutputStream(1024);
        public MyResponseCache() {
        }
        public CacheRequest put(URI uri, URLConnection conn) throws IOException {
            return new MyCacheRequest(buf);
        }
        public CacheResponse get(URI uri, String rqstMethod, Map<String, List<String>> rqstHeaders) throws IOException {
            return null;
        }
        public byte[] getBuffer() {
            return buf.toByteArray();
        }
    }
    static HttpServer server;
    public static void main(String[] args) throws Exception {
        MyResponseCache cache = new MyResponseCache();
        try {
            ResponseCache.setDefault(cache);
            server = new HttpServer (new ResponseCacheStream());
            System.out.println ("Server: listening on port: " + server.getLocalPort());
            URL url = new URL ("http:
            System.out.println ("Client: connecting to " + url);
            HttpURLConnection urlc = (HttpURLConnection)url.openConnection();
            InputStream is = urlc.getInputStream();
            System.out.println("is is " + is.getClass() + ". And markSupported: " + is.markSupported());
            if (is.markSupported()) {
                byte[] b = new byte[1024];
                byte[] b2 = new byte[32];
                int len;
                int count;
                is.mark(10);
                len = is.read(b, 0, 10);
                is.reset();
                len = 0;
                count = 0;
                do {
                    len = is.read(b, count, 40 - count);
                    if (len > 0)
                        count += len;
                } while (len > 0);
                is.mark(20);
                len = is.read(b2, 0, 20);
                is.reset();
                len = is.read(b, count, 10);
                count += len;
                is.mark(20);
                len = is.read(b2, 0, 20);
                is.reset();
                do {
                    len = is.read(b, count, 1024 - count);
                    if (len > 0)
                        count += len;
                } while (len > 0);
                is.close();
                String s1 = new String(b, 0 , count);
                String s2 = new String(cache.getBuffer(), 0 , count);
                if (! s1.equals(s2))
                    throw new RuntimeException("cache got corrupted!");
            }
        } catch (Exception e) {
            if (server != null) {
                server.terminate();
            }
            throw e;
        }
        server.terminate();
    }
}

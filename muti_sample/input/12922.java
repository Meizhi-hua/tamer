public class Test7a extends Test {
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpsServer server = HttpsServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        SSLContext ssl = new SimpleSSLContext(System.getProperty("test.src")).get();
        server.setHttpsConfigurator(new HttpsConfigurator (ssl));
        server.setExecutor (executor);
        server.start ();
        URL url = new URL ("https:
        System.out.print ("Test7a: " );
        HttpsURLConnection urlc = (HttpsURLConnection)url.openConnection ();
        urlc.setDoOutput (true);
        urlc.setRequestMethod ("POST");
        urlc.setChunkedStreamingMode (16 * 1024); 
        urlc.setHostnameVerifier (new DummyVerifier());
        urlc.setSSLSocketFactory (ssl.getSocketFactory());
        OutputStream os = new BufferedOutputStream (urlc.getOutputStream(), 8000);
        for (int i=0; i<SIZE; i++) {
            os.write (i % 100);
        }
        os.close();
        int resp = urlc.getResponseCode();
        if (resp != 200) {
            throw new RuntimeException ("test failed response code");
        }
        if (error) {
            throw new RuntimeException ("test failed error");
        }
        delay();
        server.stop(2);
        executor.shutdown();
        System.out.println ("OK");
    }
    public static boolean error = false;
    final static int SIZE = 999999;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            int c, count=0;
            while ((c=is.read ()) != -1) {
                if (c != (count % 100)) {
                    error = true;
                    break;
                }
                count ++;
            }
            if (count != SIZE) {
                error = true;
            }
            is.close();
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
}

public class Test8 extends Test {
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        URL url = new URL ("http:
        System.out.print ("Test8: " );
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
        urlc.setDoOutput (true);
        urlc.setRequestMethod ("POST");
        OutputStream os = new BufferedOutputStream (urlc.getOutputStream());
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
            if (!t.getRequestMethod().equals ("POST")) {
                error = true;
            }
            is.close();
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
}

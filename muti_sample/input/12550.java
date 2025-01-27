public class B6526158 {
    final static int SIZE = 60 * 1024;
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        URL url = new URL ("http:
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
        urlc.setDoOutput (true);
        try {
            OutputStream os = new BufferedOutputStream (urlc.getOutputStream());
            for (int i=0; i< SIZE; i++) {
                os.write (i);
            }
            os.close();
            InputStream is = urlc.getInputStream();
            int c = 0;
            while (is.read()!= -1) {
                c ++;
            }
            is.close();
        } finally {
            server.stop(2);
            executor.shutdown();
        }
        if (error) {
            throw new RuntimeException ("Test failed");
        }
    }
    public static boolean error = false;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                error = true;
            }
            t.sendResponseHeaders (200, -1);
            t.close();
        }
    }
}

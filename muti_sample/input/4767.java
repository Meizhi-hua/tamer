public class Test14 extends Test {
    static final String test_input = "Hello world";
    static final String test_output = "Ifmmp!xpsme";
    static class OffsetOutputStream extends FilterOutputStream {
        OffsetOutputStream (OutputStream os) {
            super (os);
        }
        public void write (int b) throws IOException {
            super.write (b+1);
        }
    }
    static class OffsetFilter extends Filter {
        public String description() {
            return "Translates outgoing data";
        }
        public void destroy(HttpContext c) {}
        public void init(HttpContext c) {}
        public void doFilter (HttpExchange exchange, Filter.Chain chain)
        throws IOException {
            exchange.setStreams (null, new OffsetOutputStream(
                exchange.getResponseBody()
            ));
            chain.doFilter (exchange);
        }
    }
    public static void main (String[] args) throws Exception {
        Handler handler = new Handler();
        InetSocketAddress addr = new InetSocketAddress (0);
        HttpServer server = HttpServer.create (addr, 0);
        HttpContext ctx = server.createContext ("/test", handler);
        File logfile = new File (
            System.getProperty ("test.classes")+ "/log.txt"
        );
        ctx.getFilters().add (new OffsetFilter());
        ctx.getFilters().add (new LogFilter(logfile));
        if (ctx.getFilters().size() != 2) {
            throw new RuntimeException ("wrong filter list size");
        }
        ExecutorService executor = Executors.newCachedThreadPool();
        server.setExecutor (executor);
        server.start ();
        URL url = new URL ("http:
        System.out.print ("Test14: " );
        HttpURLConnection urlc = (HttpURLConnection)url.openConnection ();
        InputStream is = urlc.getInputStream();
        int x = 0;
        String output="";
        while ((x=is.read())!= -1) {
            output = output + (char)x;
        }
        error = !output.equals (test_output);
        server.stop(2);
        executor.shutdown();
        if (error ) {
            throw new RuntimeException ("test failed error");
        }
        System.out.println ("OK");
    }
    public static boolean error = false;
    static class Handler implements HttpHandler {
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            while (is.read () != -1) ;
            is.close();
            String response = test_input;
            t.sendResponseHeaders (200, response.length());
            OutputStream os = t.getResponseBody();
            os.write (response.getBytes());
            t.close();
        }
    }
}

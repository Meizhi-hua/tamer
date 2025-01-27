public class TestAvailable
{
    com.sun.net.httpserver.HttpServer httpServer;
    ExecutorService executorService;
    public static void main(String[] args)
    {
        new TestAvailable();
    }
    public TestAvailable()
    {
        try {
            startHttpServer();
            doClient();
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }
    void doClient() {
        try {
            InetSocketAddress address = httpServer.getAddress();
            URL url = new URL("http:
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            uc.setDoOutput(true);
            uc.setRequestMethod("POST");
            uc.setChunkedStreamingMode(0);
            OutputStream os = uc.getOutputStream();
            for (int i=0; i< (128 * 1024); i++)
                os.write('X');
            os.close();
            InputStream is = uc.getInputStream();
            int avail = 0;
            while (avail == 0) {
                try { Thread.sleep(2000); } catch (Exception e) {}
                avail = is.available();
            }
            try { Thread.sleep(2000); } catch (Exception e) {}
            int nextAvail =  is.available();
            is.close();
            if (nextAvail > avail) {
                throw new RuntimeException
                        ("Failed: calling available multiple times should not return more data");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            httpServer.stop(1);
            executorService.shutdown();
        }
    }
    public void startHttpServer() throws IOException {
        httpServer = com.sun.net.httpserver.HttpServer.create(new InetSocketAddress(0), 0);
        HttpContext ctx = httpServer.createContext("/testAvailable/", new MyHandler());
        executorService = Executors.newCachedThreadPool();
        httpServer.setExecutor(executorService);
        httpServer.start();
    }
    class MyHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            byte[] ba = new byte[1024];
            while (is.read(ba) != -1);
            is.close();
            t.sendResponseHeaders(200, 0);
            OutputStream os = t.getResponseBody();
            for (int i=0; i< (128 * 1024); i++)
                os.write('X');
            os.close();
            t.close();
        }
    }
}

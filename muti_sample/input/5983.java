public class FileServerHandler implements HttpHandler {
        public static void main (String[] args) throws Exception {
            if (args.length != 3) {
                System.out.println ("usage: java FileServerHandler rootDir port logfilename");
                System.exit(1);
            }
            String rootDir = args[0];
            int port = Integer.parseInt (args[1]);
            String logfile = args[2];
            HttpServer server = HttpServer.create (new InetSocketAddress (8000), 0);
            HttpHandler h = new FileServerHandler (rootDir);
            HttpContext c = server.createContext ("/", h);
            c.getFilters().add (new LogFilter (new File (logfile)));
            server.setExecutor (Executors.newCachedThreadPool());
            server.start ();
        }
        String docroot;
        FileServerHandler (String docroot) {
            this.docroot = docroot;
        }
        int invocation = 1;
        public void handle (HttpExchange t)
            throws IOException
        {
            InputStream is = t.getRequestBody();
            Headers map = t.getRequestHeaders();
            Headers rmap = t.getResponseHeaders();
            URI uri = t.getRequestURI();
            String path = uri.getPath();
            while (is.read () != -1) ;
            is.close();
            File f = new File (docroot, path);
            if (!f.exists()) {
                notfound (t, path);
                return;
            }
            String fixedrequest = map.getFirst ("XFixed");
            String method = t.getRequestMethod();
            if (method.equals ("HEAD")) {
                rmap.set ("Content-Length", Long.toString (f.length()));
                t.sendResponseHeaders (200, -1);
                t.close();
            } else if (!method.equals("GET")) {
                t.sendResponseHeaders (405, -1);
                t.close();
                return;
            }
            if (path.endsWith (".html") || path.endsWith (".htm")) {
                rmap.set ("Content-Type", "text/html");
            } else {
                rmap.set ("Content-Type", "text/plain");
            }
            if (f.isDirectory()) {
                if (!path.endsWith ("/")) {
                    moved (t);
                    return;
                }
                rmap.set ("Content-Type", "text/html");
                t.sendResponseHeaders (200, 0);
                String[] list = f.list();
                OutputStream os = t.getResponseBody();
                PrintStream p = new PrintStream (os);
                p.println ("<h2>Directory listing for: " + path+ "</h2>");
                p.println ("<ul>");
                for (int i=0; i<list.length; i++) {
                    p.println ("<li><a href=\""+list[i]+"\">"+list[i]+"</a></li>");
                }
                p.println ("</ul><p><hr>");
                p.flush();
                p.close();
            } else {
                int clen;
                if (fixedrequest != null) {
                    clen = (int) f.length();
                } else {
                    clen = 0;
                }
                t.sendResponseHeaders (200, clen);
                OutputStream os = t.getResponseBody();
                FileInputStream fis = new FileInputStream (f);
                int count = 0;
                try {
                byte[] buf = new byte [16 * 1024];
                int len;
                while ((len=fis.read (buf)) != -1) {
                    os.write (buf, 0, len);
                    count += len;
                }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                fis.close();
                os.close();
            }
        }
        void moved (HttpExchange t) throws IOException {
            Headers req = t.getRequestHeaders();
            Headers map = t.getResponseHeaders();
            URI uri = t.getRequestURI();
            String host = req.getFirst ("Host");
            String location = "http:
            map.set ("Content-Type", "text/html");
            map.set ("Location", location);
            t.sendResponseHeaders (301, -1);
            t.close();
        }
        void notfound (HttpExchange t, String p) throws IOException {
            t.getResponseHeaders().set ("Content-Type", "text/html");
            t.sendResponseHeaders (404, 0);
            OutputStream os = t.getResponseBody();
            String s = "<h2>File not found</h2>";
            s = s + p + "<p>";
            os.write (s.getBytes());
            os.close();
            t.close();
        }
    }

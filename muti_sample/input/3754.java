public class BasicTest3 {
    static class BasicServer3 extends Thread {
        ServerSocket server;
        Socket s;
        InputStream is;
        OutputStream os;
        static final String realm = "wallyworld";
        String reply1 = "HTTP/1.1 401 Unauthorized\r\n"+
            "WWW-Authenticate: Basic realm=\""+realm+"\"\r\n\r\n";
        String reply2 = "HTTP/1.1 200 OK\r\n"+
            "Date: Mon, 15 Jan 2001 12:18:21 GMT\r\n" +
            "Server: Apache/1.3.14 (Unix)\r\n" +
            "Connection: close\r\n" +
            "Content-Type: text/html; charset=iso-8859-1\r\n" +
            "Content-Length: 10\r\n\r\n";
        BasicServer3 (ServerSocket s) {
            server = s;
        }
        void readAll (Socket s) throws IOException {
            byte[] buf = new byte [128];
            InputStream is = s.getInputStream ();
            s.setSoTimeout(1000);
            try {
                while (is.read(buf) > 0) ;
            } catch (SocketTimeoutException x) { }
        }
        public void run () {
            try {
                System.out.println ("Server 1: accept");
                s = server.accept ();
                System.out.println ("accepted");
                os = s.getOutputStream();
                os.write (reply1.getBytes());
                readAll (s);
                s.close ();
                System.out.println ("Server 2: accept");
                s = server.accept ();
                System.out.println ("accepted");
                os = s.getOutputStream();
                readAll (s);
                os.write ((reply2+"HelloWorld").getBytes());
            }
            catch (Exception e) {
                System.out.println (e);
            }
            finished ();
        }
        public synchronized void finished () {
            notifyAll();
        }
    }
    static class MyAuthenticator3 extends Authenticator {
        PasswordAuthentication pw;
        MyAuthenticator3 () {
            super ();
            pw = new PasswordAuthentication ("user", "passwordNotCheckedAnyway".toCharArray());
        }
        public PasswordAuthentication getPasswordAuthentication ()
            {
            System.out.println ("Auth called");
            return pw;
        }
        public void checkPW () {
            if (!new String (pw.getPassword()).equals ("passwordNotCheckedAnyway")) {
                throw new RuntimeException ("Password was \"" + new String (pw.getPassword()) + "\"");
            }
        }
    }
    static void read (InputStream is) throws IOException {
        int c;
        System.out.println ("reading");
        while ((c=is.read()) != -1) {
            System.out.write (c);
        }
        System.out.println ("");
        System.out.println ("finished reading");
    }
    public static void main (String args[]) throws Exception {
        MyAuthenticator3 auth = new MyAuthenticator3 ();
        Authenticator.setDefault (auth);
        ServerSocket ss = new ServerSocket (0);
        int port = ss.getLocalPort ();
        BasicServer3 server = new BasicServer3 (ss);
        synchronized (server) {
            server.start();
            System.out.println ("client 1");
            URL url = new URL ("http:
            URLConnection urlc = url.openConnection ();
            InputStream is = urlc.getInputStream ();
            read (is);
            is.close ();
            auth.checkPW ();
        }
    }
}

public class BasicTest4 {
    static class BasicServer extends Thread {
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
        BasicServer (ServerSocket s) {
            server = s;
        }
        static boolean checkFor (InputStream in, char[] seq) throws IOException {
            System.out.println ("checkfor");
            try {
                int i=0, count=0;
                while (true) {
                    int c = in.read();
                    if (c == -1)
                        return false;
                    count++;
                    if (c == seq[i]) {
                        i++;
                        if (i == seq.length)
                            return true;
                        continue;
                    } else {
                        i = 0;
                    }
                }
            }
            catch (SocketTimeoutException e) {
                return false;
            }
        }
        boolean success = false;
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
                readAll (s);
                System.out.println ("accepted");
                os = s.getOutputStream();
                os.write (reply1.getBytes());
                s.close ();
                System.out.println ("Server 2: accept");
                s = server.accept ();
                readAll (s);
                System.out.println ("accepted");
                os = s.getOutputStream();
                os.write ((reply2+"HelloWorld").getBytes());
                s.close ();
                System.out.println ("Server 3: accept");
                s = server.accept ();
                readAll (s);
                System.out.println ("accepted");
                os = s.getOutputStream();
                os.write (reply1.getBytes());
                s.close ();
                System.out.println ("Server 4: accept");
                s = server.accept ();
                readAll (s);
                System.out.println ("accepted");
                os = s.getOutputStream();
                os.write ((reply2+"HelloAgain").getBytes());
                s.close ();
                System.out.println ("Server 5: accept");
                s = server.accept ();
                s.setSoTimeout (1000);
                System.out.println ("accepted");
                InputStream is = s.getInputStream ();
                success = checkFor (is, "Authorization".toCharArray());
                System.out.println ("checkfor returned " + success);
                readAll (s);
                os = s.getOutputStream();
                os.write (reply2.getBytes());
                s.close ();
                if (success)
                    return;
                System.out.println ("Server 6: accept");
                s = server.accept ();
                System.out.println ("accepted");
                os = s.getOutputStream();
                readAll (s);
                os.write ((reply2+"HelloAgain").getBytes());
                s.close ();
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
    static class MyAuthenticator extends Authenticator {
        MyAuthenticator () {
            super ();
        }
        public PasswordAuthentication getPasswordAuthentication ()
            {
            System.out.println ("Auth called");
            return (new PasswordAuthentication ("user", "passwordNotCheckedAnyway".toCharArray()));
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
        MyAuthenticator auth = new MyAuthenticator ();
        Authenticator.setDefault (auth);
        ServerSocket ss = new ServerSocket (0);
        int port = ss.getLocalPort ();
        BasicServer server = new BasicServer (ss);
        synchronized (server) {
            server.start();
            System.out.println ("client 1");
            URL url = new URL ("http:
            URLConnection urlc = url.openConnection ();
            InputStream is = urlc.getInputStream ();
            read (is);
            System.out.println ("client 2");
            url = new URL ("http:
            urlc = url.openConnection ();
            is = urlc.getInputStream ();
            System.out.println ("client 3");
            url = new URL ("http:
            urlc = url.openConnection ();
            is = urlc.getInputStream ();
            read (is);
            server.wait ();
            if (!server.success) {
                throw new RuntimeException ("3rd request did not use pre-emptive authorization");
            }
        }
    }
}

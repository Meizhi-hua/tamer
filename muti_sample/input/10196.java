public class FtpURL {
    private class FtpServer extends Thread {
        private ServerSocket    server;
        private int port;
        private boolean done = false;
        private boolean portEnabled = true;
        private boolean pasvEnabled = true;
        private String username;
        private String password;
        private String cwd;
        private String filename;
        private String type;
        private boolean list = false;
        private class FtpServerHandler {
            BufferedReader in;
            PrintWriter out;
            Socket client;
            private final int ERROR = 0;
            private final int USER = 1;
            private final int PASS = 2;
            private final int CWD = 3;
            private final int CDUP = 4;
            private final int PWD = 5;
            private final int TYPE = 6;
            private final int NOOP = 7;
            private final int RETR = 8;
            private final int PASV = 9;
            private final int PORT = 10;
            private final int LIST = 11;
            private final int REIN = 12;
            private final int QUIT = 13;
            private final int STOR = 14;
            private final int NLST = 15;
            private final int RNFR = 16;
            private final int RNTO = 17;
            String[] cmds = { "USER", "PASS", "CWD", "CDUP", "PWD", "TYPE",
                              "NOOP", "RETR", "PASV", "PORT", "LIST", "REIN",
                              "QUIT", "STOR", "NLST", "RNFR", "RNTO" };
            private String arg = null;
            private ServerSocket pasv = null;
            private int data_port = 0;
            private InetAddress data_addr = null;
            private int parseCmd(String cmd) {
                if (cmd == null || cmd.length() < 3)
                    return ERROR;
                int blank = cmd.indexOf(' ');
                if (blank < 0)
                    blank = cmd.length();
                if (blank < 3)
                    return ERROR;
                String s = cmd.substring(0, blank);
                if (cmd.length() > blank+1)
                    arg = cmd.substring(blank+1, cmd.length());
                else
                    arg = null;
                for (int i = 0; i < cmds.length; i++) {
                    if (s.equalsIgnoreCase(cmds[i]))
                        return i+1;
                }
                return ERROR;
            }
            public FtpServerHandler(Socket cl) {
                client = cl;
            }
            protected boolean isPasvSet() {
                if (pasv != null && !pasvEnabled) {
                    try {
                        pasv.close();
                    } catch (IOException ex) {
                    }
                    pasv = null;
                }
                if (pasvEnabled && pasv != null)
                    return true;
                return false;
            }
            protected OutputStream getOutDataStream() {
                try {
                    if (isPasvSet()) {
                        Socket s = pasv.accept();
                        return s.getOutputStream();
                    }
                    if (data_addr != null) {
                        Socket s = new Socket(data_addr, data_port);
                        data_addr = null;
                        data_port = 0;
                        return s.getOutputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            protected InputStream getInDataStream() {
                try {
                    if (isPasvSet()) {
                        Socket s = pasv.accept();
                        return s.getInputStream();
                    }
                    if (data_addr != null) {
                        Socket s = new Socket(data_addr, data_port);
                        data_addr = null;
                        data_port = 0;
                        return s.getInputStream();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
            public void run() {
                boolean done = false;
                String str;
                int res;
                boolean logged = false;
                boolean waitpass = false;
                try {
                    in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                    out = new PrintWriter(client.getOutputStream(), true);
                    out.println("220 tatooine FTP server (SunOS 5.8) ready.");
                } catch (Exception ex) {
                    return;
                }
                synchronized (FtpServer.this) {
                  while (!done) {
                    try {
                        str = in.readLine();
                        res = parseCmd(str);
                        if ((res > PASS && res != QUIT) && !logged) {
                            out.println("530 Not logged in.");
                            continue;
                        }
                        switch (res) {
                        case ERROR:
                            out.println("500 '" + str + "': command not understood.");
                            break;
                        case USER:
                            if (!logged && !waitpass) {
                                username = str.substring(5);
                                password = null;
                                cwd = null;
                                if ("user2".equals(username)) {
                                    out.println("230 Guest login ok, access restrictions apply.");
                                    logged = true;
                                } else {
                                    out.println("331 Password required for " + arg);
                                    waitpass = true;
                                }
                            } else {
                                out.println("503 Bad sequence of commands.");
                            }
                            break;
                        case PASS:
                            if (!logged && waitpass) {
                                out.println("230 Guest login ok, access restrictions apply.");
                                password = str.substring(5);
                                logged = true;
                                waitpass = false;
                            } else
                                out.println("503 Bad sequence of commands.");
                            break;
                        case QUIT:
                            out.println("221 Goodbye.");
                            out.flush();
                            out.close();
                            if (pasv != null)
                                pasv.close();
                            done = true;
                            break;
                        case TYPE:
                            out.println("200 Type set to " + arg + ".");
                            type = arg;
                            break;
                        case CWD:
                            out.println("250 CWD command successful.");
                            if (cwd == null)
                                cwd = str.substring(4);
                            else
                                cwd = cwd + "/" + str.substring(4);
                            break;
                        case CDUP:
                            out.println("250 CWD command successful.");
                            break;
                        case PWD:
                            out.println("257 \"" + cwd + "\" is current directory");
                            break;
                        case PASV:
                            if (!pasvEnabled) {
                                out.println("500 PASV is disabled, use PORT instead.");
                                continue;
                            }
                            try {
                                if (pasv == null)
                                    pasv = new ServerSocket(0);
                                int port = pasv.getLocalPort();
                                out.println("227 Entering Passive Mode (127,0,0,1," +
                                            (port >> 8) + "," + (port & 0xff) +")");
                            } catch (IOException ssex) {
                                out.println("425 Can't build data connection: Connection refused.");
                            }
                            break;
                        case PORT:
                            if (!portEnabled) {
                                out.println("500 PORT is disabled, use PASV instead");
                                continue;
                            }
                            StringBuffer host;
                            int i=0, j=4;
                            while (j>0) {
                                i = arg.indexOf(',', i+1);
                                if (i < 0)
                                    break;
                                j--;
                            }
                            if (j != 0) {
                                out.println("500 '" + arg + "': command not understood.");
                                continue;
                            }
                            try {
                                host = new StringBuffer(arg.substring(0,i));
                                for (j=0; j < host.length(); j++)
                                    if (host.charAt(j) == ',')
                                        host.setCharAt(j, '.');
                                String ports = arg.substring(i+1);
                                i = ports.indexOf(',');
                                data_port = Integer.parseInt(ports.substring(0,i)) << 8;
                                data_port += (Integer.parseInt(ports.substring(i+1)));
                                data_addr = InetAddress.getByName(host.toString());
                                out.println("200 Command okay.");
                            } catch (Exception ex3) {
                                data_port = 0;
                                data_addr = null;
                                out.println("500 '" + arg + "': command not understood.");
                            }
                            break;
                        case RETR:
                            {
                                filename = str.substring(5);
                                OutputStream dout = getOutDataStream();
                                if (dout != null) {
                                    out.println("200 Command okay.");
                                    PrintWriter pout = new PrintWriter(new BufferedOutputStream(dout));
                                    pout.println("Hello World!");
                                    pout.flush();
                                    pout.close();
                                    list = false;
                                } else
                                    out.println("425 Can't build data connection: Connection refused.");
                            }
                            break;
                        case NLST:
                            filename = arg;
                        case LIST:
                            {
                                OutputStream dout = getOutDataStream();
                                if (dout != null) {
                                    out.println("200 Command okay.");
                                    PrintWriter pout = new PrintWriter(new BufferedOutputStream(dout));
                                    pout.println("total 130");
                                    pout.println("drwxrwxrwt   7 sys      sys          577 May 12 03:30 .");
                                    pout.println("drwxr-xr-x  39 root     root        1024 Mar 27 12:55 ..");
                                    pout.println("drwxrwxr-x   2 root     root         176 Apr 10 12:02 .X11-pipe");
                                    pout.println("drwxrwxr-x   2 root     root         176 Apr 10 12:02 .X11-unix");
                                    pout.println("drwxrwxrwx   2 root     root         179 Mar 30 15:09 .pcmcia");
                                    pout.println("drwxrwxrwx   2 jladen   staff        117 Mar 30 18:18 .removable");
                                    pout.println("drwxrwxrwt   2 root     root         327 Mar 30 15:08 .rpc_door");
                                    pout.println("-rw-r--r--   1 root     other         21 May  5 16:59 hello2.txt");
                                    pout.println("-rw-rw-r--   1 root     sys         5968 Mar 30 15:08 ps_data");
                                    pout.flush();
                                    pout.close();
                                    list = true;
                                    try {
                                        FtpServer.this.wait ();
                                    } catch (Exception e) {}
                                } else
                                    out.println("425 Can't build data connection: Connection refused.");
                            }
                            break;
                        case STOR:
                            {
                                InputStream is = getInDataStream();
                                if (is != null) {
                                    out.println("200 Command okay.");
                                    BufferedInputStream din = new BufferedInputStream(is);
                                    int val;
                                    do {
                                        val = din.read();
                                    } while (val != -1);
                                    din.close();
                                } else
                                    out.println("425 Can't build data connection: Connection refused.");
                            }
                            break;
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                        try {
                            out.close();
                        } catch (Exception ex2) {
                        }
                        done = true;
                    }
                  }
                }
            }
        }
        public FtpServer(int port) {
            this.port = port;
            try {
              server = new ServerSocket(port);
            } catch (IOException e) {
            }
        }
        public FtpServer() {
            this(21);
        }
        public int getPort() {
            if (server != null)
                return server.getLocalPort();
            return 0;
        }
        synchronized public void terminate() {
            done = true;
        }
        synchronized public void setPortEnabled(boolean ok) {
            portEnabled = ok;
        }
        synchronized public void setPasvEnabled(boolean ok) {
            pasvEnabled = ok;
        }
        String getUsername() {
            return username;
        }
        String getPassword() {
            return password;
        }
        String pwd() {
            return cwd;
        }
        String getFilename() {
            return filename;
        }
        String getType() {
            return type;
        }
        synchronized boolean getList() {
            notify ();
            return list;
        }
        public void run() {
            try {
                Socket client;
                for (int i=0; i<2; i++) {
                    client = server.accept();
                    (new FtpServerHandler(client)).run();
                }
            } catch(Exception e) {
            } finally {
                try { server.close(); } catch (IOException unused) {}
            }
        }
    }
    public static void main(String[] args) throws Exception {
        FtpURL test = new FtpURL();
    }
    public FtpURL() throws Exception {
        FtpServer server = new FtpServer(0);
        BufferedReader in = null;
        try {
            server.start();
            int port = server.getPort();
            URL url = new URL("ftp:
            URLConnection con = url.openConnection();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String s;
            do {
                s = in.readLine();
            } while (s != null);
            if (!("user".equals(server.getUsername())))
                throw new RuntimeException("Inccorect username received");
            if (!("password".equals(server.getPassword())))
                throw new RuntimeException("Inccorect password received");
            if (!("/etc".equals(server.pwd())))
                throw new RuntimeException("Inccorect directory received");
            if (!("motd".equals(server.getFilename())))
                throw new RuntimeException("Inccorect username received");
            if (!("A".equals(server.getType())))
                throw new RuntimeException("Incorrect type received");
            in.close();
            port = server.getPort();
            url = new URL("ftp:
            con = url.openConnection();
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            do {
                s = in.readLine();
            } while (s != null);
            if (!server.getList())
                throw new RuntimeException(";type=d didn't generate a NLST");
            if (server.getPassword() != null)
                throw new RuntimeException("password should be null!");
            if (! "bin".equals(server.getFilename()))
                throw new RuntimeException("Incorrect filename received");
            if (! "/usr".equals(server.pwd()))
                throw new RuntimeException("Incorrect pwd received");
        } catch (Exception e) {
            throw new RuntimeException("FTP support error: " + e.getMessage());
        } finally {
            try { in.close(); } catch (IOException unused) {}
            server.terminate();
            server.server.close();
        }
    }
}

public class SmtpClient extends TransferProtocolClient {
    String mailhost;
    SmtpPrintStream message;
    public void closeServer() throws IOException {
        if (serverIsOpen()) {
            closeMessage();
            issueCommand("QUIT\r\n", 221);
            super.closeServer();
        }
    }
    void issueCommand(String cmd, int expect) throws IOException {
        sendServer(cmd);
        int reply;
        while ((reply = readServerResponse()) != expect)
            if (reply != 220) {
                throw new SmtpProtocolException(getResponseString());
            }
    }
    private void toCanonical(String s) throws IOException {
        if (s.startsWith("<"))
            issueCommand("rcpt to: " + s + "\r\n", 250);
        else
            issueCommand("rcpt to: <" + s + ">\r\n", 250);
    }
    public void to(String s) throws IOException {
        int st = 0;
        int limit = s.length();
        int pos = 0;
        int lastnonsp = 0;
        int parendepth = 0;
        boolean ignore = false;
        while (pos < limit) {
            int c = s.charAt(pos);
            if (parendepth > 0) {
                if (c == '(')
                    parendepth++;
                else if (c == ')')
                    parendepth--;
                if (parendepth == 0)
                    if (lastnonsp > st)
                        ignore = true;
                    else
                        st = pos + 1;
            } else if (c == '(')
                parendepth++;
            else if (c == '<')
                st = lastnonsp = pos + 1;
            else if (c == '>')
                ignore = true;
            else if (c == ',') {
                if (lastnonsp > st)
                    toCanonical(s.substring(st, lastnonsp));
                st = pos + 1;
                ignore = false;
            } else {
                if (c > ' ' && !ignore)
                    lastnonsp = pos + 1;
                else if (st == pos)
                    st++;
            }
            pos++;
        }
        if (lastnonsp > st)
            toCanonical(s.substring(st, lastnonsp));
    }
    public void from(String s) throws IOException {
        if (s.startsWith("<"))
            issueCommand("mail from: " + s + "\r\n", 250);
        else
            issueCommand("mail from: <" + s + ">\r\n", 250);
    }
    private void openServer(String host) throws IOException {
        mailhost = host;
        openServer(mailhost, 25);
        issueCommand("helo "+InetAddress.getLocalHost().getHostName()+"\r\n", 250);
    }
    public PrintStream startMessage() throws IOException {
        issueCommand("data\r\n", 354);
        try {
            message = new SmtpPrintStream(serverOutput, this);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(encoding+" encoding not found");
        }
        return message;
    }
    void closeMessage() throws IOException {
        if (message != null)
            message.close();
    }
    public SmtpClient (String host) throws IOException {
        super();
        if (host != null) {
            try {
                openServer(host);
                mailhost = host;
                return;
            } catch(Exception e) {
            }
        }
        try {
            String s;
            mailhost = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction("mail.host"));
            if (mailhost != null) {
                openServer(mailhost);
                return;
            }
        } catch(Exception e) {
        }
        try {
            mailhost = "localhost";
            openServer(mailhost);
        } catch(Exception e) {
            mailhost = "mailhost";
            openServer(mailhost);
        }
    }
    public SmtpClient () throws IOException {
        this(null);
    }
    public SmtpClient(int to) throws IOException {
        super();
        setConnectTimeout(to);
        try {
            String s;
            mailhost = java.security.AccessController.doPrivileged(
                    new sun.security.action.GetPropertyAction("mail.host"));
            if (mailhost != null) {
                openServer(mailhost);
                return;
            }
        } catch(Exception e) {
        }
        try {
            mailhost = "localhost";
            openServer(mailhost);
        } catch(Exception e) {
            mailhost = "mailhost";
            openServer(mailhost);
        }
    }
    public String getMailHost() {
        return mailhost;
    }
    String getEncoding () {
        return encoding;
    }
}
class SmtpPrintStream extends java.io.PrintStream {
    private SmtpClient target;
    private int lastc = '\n';
    SmtpPrintStream (OutputStream fos, SmtpClient cl) throws UnsupportedEncodingException {
        super(fos, false, cl.getEncoding());
        target = cl;
    }
    public void close() {
        if (target == null)
            return;
        if (lastc != '\n') {
            write('\n');
        }
        try {
            target.issueCommand(".\r\n", 250);
            target.message = null;
            out = null;
            target = null;
        } catch (IOException e) {
        }
    }
    public void write(int b) {
        try {
            if (lastc == '\n' && b == '.') {
                out.write('.');
            }
            if (b == '\n' && lastc != '\r') {
                out.write('\r');
            }
            out.write(b);
            lastc = b;
        } catch (IOException e) {
        }
    }
    public void write(byte b[], int off, int len) {
        try {
            int lc = lastc;
            while (--len >= 0) {
                int c = b[off++];
                if (lc == '\n' && c == '.')
                    out.write('.');
                if (c == '\n' && lc != '\r') {
                    out.write('\r');
                }
                out.write(c);
                lc = c;
            }
            lastc = lc;
        } catch (IOException e) {
        }
    }
    public void print(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            write(s.charAt(i));
        }
    }
}
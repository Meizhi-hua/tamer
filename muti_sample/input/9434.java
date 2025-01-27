class BasicAuthentication extends AuthenticationInfo {
    private static final long serialVersionUID = 100L;
    String auth;
    public BasicAuthentication(boolean isProxy, String host, int port,
                               String realm, PasswordAuthentication pw) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
              AuthScheme.BASIC, host, port, realm);
        String plain = pw.getUserName() + ":";
        byte[] nameBytes = null;
        try {
            nameBytes = plain.getBytes("ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i=0; i<passwd.length; i++)
            passwdBytes[i] = (byte)passwd[i];
        byte[] concat = new byte[nameBytes.length + passwdBytes.length];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length,
                         passwdBytes.length);
        this.auth = "Basic " + (new BasicBASE64Encoder()).encode(concat);
        this.pw = pw;
    }
    public BasicAuthentication(boolean isProxy, String host, int port,
                               String realm, String auth) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
              AuthScheme.BASIC, host, port, realm);
        this.auth = "Basic " + auth;
    }
    public BasicAuthentication(boolean isProxy, URL url, String realm,
                                   PasswordAuthentication pw) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
              AuthScheme.BASIC, url, realm);
        String plain = pw.getUserName() + ":";
        byte[] nameBytes = null;
        try {
            nameBytes = plain.getBytes("ISO-8859-1");
        } catch (java.io.UnsupportedEncodingException uee) {
            assert false;
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i=0; i<passwd.length; i++)
            passwdBytes[i] = (byte)passwd[i];
        byte[] concat = new byte[nameBytes.length + passwdBytes.length];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length,
                         passwdBytes.length);
        this.auth = "Basic " + (new BasicBASE64Encoder()).encode(concat);
        this.pw = pw;
    }
    public BasicAuthentication(boolean isProxy, URL url, String realm,
                                   String auth) {
        super(isProxy ? PROXY_AUTHENTICATION : SERVER_AUTHENTICATION,
              AuthScheme.BASIC, url, realm);
        this.auth = "Basic " + auth;
    }
    @Override
    public boolean supportsPreemptiveAuthorization() {
        return true;
    }
    @Override
    public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        conn.setAuthenticationProperty(getHeaderName(), getHeaderValue(null,null));
        return true;
    }
    @Override
    public String getHeaderValue(URL url, String method) {
        return auth;
    }
    @Override
    public boolean isAuthorizationStale (String header) {
        return false;
    }
    static String getRootPath(String npath, String opath) {
        int index = 0;
        int toindex;
        try {
            npath = new URI (npath).normalize().getPath();
            opath = new URI (opath).normalize().getPath();
        } catch (URISyntaxException e) {
        }
        while (index < opath.length()) {
            toindex = opath.indexOf('/', index+1);
            if (toindex != -1 && opath.regionMatches(0, npath, 0, toindex+1))
                index = toindex;
            else
                return opath.substring(0, index+1);
        }
        return npath;
    }
    private class BasicBASE64Encoder extends BASE64Encoder {
        @Override
        protected int bytesPerLine() {
            return (10000);
        }
    }
}

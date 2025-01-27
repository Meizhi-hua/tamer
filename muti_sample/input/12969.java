public class AliasFileParser {
    private static final String ALIAS = "alias";
    private static final boolean DEBUG = false;
    private URL inputfile;
    private StreamTokenizer st;
    private Token currentToken;
    AliasFileParser(URL inputfile) {
        this.inputfile = inputfile;
    }
    private class Token {
        public String sval;
        public int ttype;
        public Token(int ttype, String sval) {
            this.ttype = ttype;
            this.sval = sval;
        }
    }
    private void logln(String s) {
        if (DEBUG) {
            System.err.println(s);
        }
    }
    private void nextToken() throws IOException {
        st.nextToken();
        currentToken = new Token(st.ttype, st.sval);
        logln("Read token: type = " + currentToken.ttype
              + " string = " + currentToken.sval);
    }
    private void match(int ttype, String token)
                 throws IOException, SyntaxException {
        if ((currentToken.ttype == ttype)
                && (currentToken.sval.compareTo(token) == 0)) {
            logln("matched type: " + ttype + " and token = "
                  + currentToken.sval);
            nextToken();
        } else {
            throw new SyntaxException(st.lineno());
        }
    }
    private void match(int ttype) throws IOException, SyntaxException {
        if (currentToken.ttype == ttype) {
            logln("matched type: " + ttype + ", token = " + currentToken.sval);
            nextToken();
        } else {
            throw new SyntaxException(st.lineno());
        }
    }
    private void match(String token) throws IOException, SyntaxException {
        match(StreamTokenizer.TT_WORD, token);
    }
    public void parse(Map<String, ArrayList<String>> map) throws SyntaxException, IOException {
        if (inputfile == null) {
            return;
        }
        BufferedReader r = new BufferedReader(
                new InputStreamReader(inputfile.openStream()));
        st = new StreamTokenizer(r);
        st.slashSlashComments(true);
        st.slashStarComments(true);
        st.wordChars('_','_');
        nextToken();
        while (currentToken.ttype != StreamTokenizer.TT_EOF) {
            if ((currentToken.ttype != StreamTokenizer.TT_WORD)
                    || (currentToken.sval.compareTo(ALIAS) != 0)) {
                nextToken();
                continue;
            }
            match(ALIAS);
            String name = currentToken.sval;
            match(StreamTokenizer.TT_WORD);
            ArrayList<String> aliases = new ArrayList<String>();
            do {
                aliases.add(currentToken.sval);
                match(StreamTokenizer.TT_WORD);
            } while ((currentToken.ttype != StreamTokenizer.TT_EOF)
                     && (currentToken.sval.compareTo(ALIAS) != 0));
            logln("adding map entry for " + name + " values = " + aliases);
            map.put(name, aliases);
        }
    }
}

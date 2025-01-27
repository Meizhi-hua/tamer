public class ContentTypeParser implements ContentTypeParserConstants {
        private String type;
        private String subtype;
        private ArrayList paramNames = new ArrayList();
        private ArrayList paramValues = new ArrayList();
        public String getType() { return type; }
        public String getSubType() { return subtype; }
        public ArrayList getParamNames() { return paramNames; }
        public ArrayList getParamValues() { return paramValues; }
    public static void main(String args[]) throws ParseException {
        while (true) {
            try {
                ContentTypeParser parser = new ContentTypeParser(System.in);
                parser.parseLine();
            } catch (Exception x) {
                x.printStackTrace();
                return;
            }
        }
    }
  final public void parseLine() throws ParseException {
    parse();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 1:
      jj_consume_token(1);
      break;
    default:
      jj_la1[0] = jj_gen;
      ;
    }
    jj_consume_token(2);
  }
  final public void parseAll() throws ParseException {
    parse();
    jj_consume_token(0);
  }
  final public void parse() throws ParseException {
        Token type;
        Token subtype;
    type = jj_consume_token(ATOKEN);
    jj_consume_token(3);
    subtype = jj_consume_token(ATOKEN);
                this.type = type.image;
                this.subtype = subtype.image;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case 4:
        ;
        break;
      default:
        jj_la1[1] = jj_gen;
        break label_1;
      }
      jj_consume_token(4);
      parameter();
    }
  }
  final public void parameter() throws ParseException {
        Token attrib;
        String val;
    attrib = jj_consume_token(ATOKEN);
    jj_consume_token(5);
    val = value();
                paramNames.add(attrib.image);
                paramValues.add(val);
  }
  final public String value() throws ParseException {
 Token t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ATOKEN:
      t = jj_consume_token(ATOKEN);
      break;
    case QUOTEDSTRING:
      t = jj_consume_token(QUOTEDSTRING);
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
          {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }
  public ContentTypeParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[3];
  static private int[] jj_la1_0;
  static {
      jj_la1_0();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x2,0x10,0x280000,};
   }
  public ContentTypeParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public ContentTypeParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new ContentTypeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  public ContentTypeParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new ContentTypeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  public ContentTypeParser(ContentTypeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  public void ReInit(ContentTypeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 3; i++) jj_la1[i] = -1;
  }
  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }
  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }
  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }
  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }
  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;
  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[24];
    for (int i = 0; i < 24; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 3; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 24; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }
  final public void enable_tracing() {
  }
  final public void disable_tracing() {
  }
}

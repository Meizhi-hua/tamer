public class DateTimeParser implements DateTimeParserConstants {
    private static final boolean ignoreMilitaryZoneOffset = true;
    public static void main(String args[]) throws ParseException {
                while (true) {
                    try {
                                DateTimeParser parser = new DateTimeParser(System.in);
                        parser.parseLine();
                    } catch (Exception x) {
                                x.printStackTrace();
                                return;
                    }
                }
    }
    private static int parseDigits(Token token) {
        return Integer.parseInt(token.image, 10);
    }
    private static int getMilitaryZoneOffset(char c) {
        if (ignoreMilitaryZoneOffset)
            return 0;
        c = Character.toUpperCase(c);
        switch (c) {
            case 'A': return 1;
            case 'B': return 2;
            case 'C': return 3;
            case 'D': return 4;
            case 'E': return 5;
            case 'F': return 6;
            case 'G': return 7;
            case 'H': return 8;
            case 'I': return 9;
            case 'K': return 10;
            case 'L': return 11;
            case 'M': return 12;
            case 'N': return -1;
            case 'O': return -2;
            case 'P': return -3;
            case 'Q': return -4;
            case 'R': return -5;
            case 'S': return -6;
            case 'T': return -7;
            case 'U': return -8;
            case 'V': return -9;
            case 'W': return -10;
            case 'X': return -11;
            case 'Y': return -12;
            case 'Z': return 0;
            default: return 0;
        }
    }
    private static class Time {
        private int hour;
        private int minute;
        private int second;
        private int zone;
        public Time(int hour, int minute, int second, int zone) {
            this.hour = hour;
            this.minute = minute;
            this.second = second;
            this.zone = zone;
        }
        public int getHour() { return hour; }
        public int getMinute() { return minute; }
        public int getSecond() { return second; }
        public int getZone() { return zone; }
    }
    private static class Date {
        private String year;
        private int month;
        private int day;
        public Date(String year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }
        public String getYear() { return year; }
        public int getMonth() { return month; }
        public int getDay() { return day; }
    }
  final public DateTime parseLine() throws ParseException {
 DateTime dt;
    dt = date_time();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 1:
      jj_consume_token(1);
      break;
    default:
      jj_la1[0] = jj_gen;
      ;
    }
    jj_consume_token(2);
          {if (true) return dt;}
    throw new Error("Missing return statement in function");
  }
  final public DateTime parseAll() throws ParseException {
 DateTime dt;
    dt = date_time();
    jj_consume_token(0);
          {if (true) return dt;}
    throw new Error("Missing return statement in function");
  }
  final public DateTime date_time() throws ParseException {
 Date d; Time t;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 4:
    case 5:
    case 6:
    case 7:
    case 8:
    case 9:
    case 10:
      day_of_week();
      jj_consume_token(3);
      break;
    default:
      jj_la1[1] = jj_gen;
      ;
    }
    d = date();
    t = time();
            {if (true) return new DateTime(
                    d.getYear(),
                    d.getMonth(),
                    d.getDay(),
                    t.getHour(),
                    t.getMinute(),
                    t.getSecond(),
                    t.getZone());}    
    throw new Error("Missing return statement in function");
  }
  final public String day_of_week() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 4:
      jj_consume_token(4);
      break;
    case 5:
      jj_consume_token(5);
      break;
    case 6:
      jj_consume_token(6);
      break;
    case 7:
      jj_consume_token(7);
      break;
    case 8:
      jj_consume_token(8);
      break;
    case 9:
      jj_consume_token(9);
      break;
    case 10:
      jj_consume_token(10);
      break;
    default:
      jj_la1[2] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return token.image;}
    throw new Error("Missing return statement in function");
  }
  final public Date date() throws ParseException {
 int d, m; String y;
    d = day();
    m = month();
    y = year();
      {if (true) return new Date(y, m, d);}
    throw new Error("Missing return statement in function");
  }
  final public int day() throws ParseException {
 Token t;
    t = jj_consume_token(DIGITS);
                 {if (true) return parseDigits(t);}
    throw new Error("Missing return statement in function");
  }
  final public int month() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 11:
      jj_consume_token(11);
            {if (true) return 1;}
      break;
    case 12:
      jj_consume_token(12);
            {if (true) return 2;}
      break;
    case 13:
      jj_consume_token(13);
            {if (true) return 3;}
      break;
    case 14:
      jj_consume_token(14);
            {if (true) return 4;}
      break;
    case 15:
      jj_consume_token(15);
            {if (true) return 5;}
      break;
    case 16:
      jj_consume_token(16);
            {if (true) return 6;}
      break;
    case 17:
      jj_consume_token(17);
            {if (true) return 7;}
      break;
    case 18:
      jj_consume_token(18);
            {if (true) return 8;}
      break;
    case 19:
      jj_consume_token(19);
            {if (true) return 9;}
      break;
    case 20:
      jj_consume_token(20);
            {if (true) return 10;}
      break;
    case 21:
      jj_consume_token(21);
            {if (true) return 11;}
      break;
    case 22:
      jj_consume_token(22);
            {if (true) return 12;}
      break;
    default:
      jj_la1[3] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    throw new Error("Missing return statement in function");
  }
  final public String year() throws ParseException {
 Token t;
    t = jj_consume_token(DIGITS);
                 {if (true) return t.image;}
    throw new Error("Missing return statement in function");
  }
  final public Time time() throws ParseException {
 int h, m, s=0, z;
    h = hour();
    jj_consume_token(23);
    m = minute();
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 23:
      jj_consume_token(23);
      s = second();
      break;
    default:
      jj_la1[4] = jj_gen;
      ;
    }
    z = zone();
      {if (true) return new Time(h, m, s, z);}
    throw new Error("Missing return statement in function");
  }
  final public int hour() throws ParseException {
 Token t;
    t = jj_consume_token(DIGITS);
                 {if (true) return parseDigits(t);}
    throw new Error("Missing return statement in function");
  }
  final public int minute() throws ParseException {
 Token t;
    t = jj_consume_token(DIGITS);
                 {if (true) return parseDigits(t);}
    throw new Error("Missing return statement in function");
  }
  final public int second() throws ParseException {
 Token t;
    t = jj_consume_token(DIGITS);
                 {if (true) return parseDigits(t);}
    throw new Error("Missing return statement in function");
  }
  final public int zone() throws ParseException {
  Token t, u; int z;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case OFFSETDIR:
      t = jj_consume_token(OFFSETDIR);
      u = jj_consume_token(DIGITS);
                                              z=parseDigits(u)*(t.image.equals("-") ? -1 : 1);
      break;
    case 25:
    case 26:
    case 27:
    case 28:
    case 29:
    case 30:
    case 31:
    case 32:
    case 33:
    case 34:
    case MILITARY_ZONE:
      z = obs_zone();
      break;
    default:
      jj_la1[5] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return z;}
    throw new Error("Missing return statement in function");
  }
  final public int obs_zone() throws ParseException {
 Token t; int z;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case 25:
      jj_consume_token(25);
            z=0;
      break;
    case 26:
      jj_consume_token(26);
            z=0;
      break;
    case 27:
      jj_consume_token(27);
            z=-5;
      break;
    case 28:
      jj_consume_token(28);
            z=-4;
      break;
    case 29:
      jj_consume_token(29);
            z=-6;
      break;
    case 30:
      jj_consume_token(30);
            z=-5;
      break;
    case 31:
      jj_consume_token(31);
            z=-7;
      break;
    case 32:
      jj_consume_token(32);
            z=-6;
      break;
    case 33:
      jj_consume_token(33);
            z=-8;
      break;
    case 34:
      jj_consume_token(34);
            z=-7;
      break;
    case MILITARY_ZONE:
      t = jj_consume_token(MILITARY_ZONE);
                                                             z=getMilitaryZoneOffset(t.image.charAt(0));
      break;
    default:
      jj_la1[6] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return z * 100;}
    throw new Error("Missing return statement in function");
  }
  public DateTimeParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[7];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x2,0x7f0,0x7f0,0x7ff800,0x800000,0xff000000,0xfe000000,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x0,0x0,0x0,0x0,0xf,0xf,};
   }
  public DateTimeParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public DateTimeParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new DateTimeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
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
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }
  public DateTimeParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new DateTimeParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }
  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }
  public DateTimeParser(DateTimeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
  }
  public void ReInit(DateTimeParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 7; i++) jj_la1[i] = -1;
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
    boolean[] la1tokens = new boolean[49];
    for (int i = 0; i < 49; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 7; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 49; i++) {
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

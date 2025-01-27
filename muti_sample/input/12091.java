public class MS50221 extends ISO2022_JP
{
    public MS50221() {
        super("x-windows-50221",
              ExtendedCharsets.aliasesFor("x-windows-50221"));
    }
    public String historicalName() {
        return "MS50221";
    }
    public boolean contains(Charset cs) {
      return super.contains(cs) ||
             (cs instanceof JIS_X_0212) ||
             (cs instanceof MS50221);
    }
    protected short[] getDecIndex1() {
        return JIS_X_0208_MS5022X_Decoder.index1;
    }
    protected String[] getDecIndex2() {
        return JIS_X_0208_MS5022X_Decoder.index2;
    }
    protected DoubleByteDecoder get0212Decoder() {
        return new JIS_X_0212_MS5022X_Decoder(this);
    }
    protected short[] getEncIndex1() {
        return JIS_X_0208_MS5022X_Encoder.index1;
    }
    protected String[] getEncIndex2() {
        return JIS_X_0208_MS5022X_Encoder.index2;
    }
    protected DoubleByteEncoder get0212Encoder() {
        return new JIS_X_0212_MS5022X_Encoder(this);
    }
    protected boolean doSBKANA() {
        return true;
    }
}

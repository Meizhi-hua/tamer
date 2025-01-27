public abstract class SimpleEUCEncoder
    extends CharsetEncoder
{
    protected short  index1[];
    protected String index2;
    protected String index2a;
    protected String index2b;
    protected String index2c;
    protected int    mask1;
    protected int    mask2;
    protected int    shift;
    private byte[] outputByte = new byte[4];
    private final Surrogate.Parser sgp = new Surrogate.Parser();
    protected SimpleEUCEncoder(Charset cs)
    {
        super(cs, 3.0f, 4.0f);
    }
    public boolean canEncode(char ch) {
       int    index;
       String theChars;
       index = index1[((ch & mask1) >> shift)] + (ch & mask2);
       if (index < 7500)
         theChars = index2;
       else
         if (index < 15000) {
           index = index - 7500;
           theChars = index2a;
         }
         else
           if (index < 22500){
             index = index - 15000;
             theChars = index2b;
           }
           else {
             index = index - 22500;
             theChars = index2c;
           }
       if (theChars.charAt(2*index) != '\u0000' ||
                    theChars.charAt(2*index + 1) != '\u0000')
         return (true);
       return( ch == '\u0000');
    }
    private CoderResult encodeArrayLoop(CharBuffer src, ByteBuffer dst) {
        char[] sa = src.array();
        int sp = src.arrayOffset() + src.position();
        int sl = src.arrayOffset() + src.limit();
        assert (sp <= sl);
        sp = (sp <= sl ? sp : sl);
        byte[] da = dst.array();
        int dp = dst.arrayOffset() + dst.position();
        int dl = dst.arrayOffset() + dst.limit();
        assert (dp <= dl);
        dp = (dp <= dl ? dp : dl);
        int     index;
        int     spaceNeeded;
        int     i;
        try {
            while (sp < sl) {
                boolean allZeroes = true;
                char inputChar = sa[sp];
                if (Character.isSurrogate(inputChar)) {
                    if (sgp.parse(inputChar, sa, sp, sl) < 0)
                        return sgp.error();
                    return sgp.unmappableResult();
                }
                if (inputChar >= '\uFFFE')
                    return CoderResult.unmappableForLength(1);
                String theChars;
                char   aChar;
                index = index1[((inputChar & mask1) >> shift)] + (inputChar & mask2);
                if (index < 7500)
                    theChars = index2;
                else if (index < 15000) {
                     index = index - 7500;
                     theChars = index2a;
                } else if (index < 22500){
                    index = index - 15000;
                    theChars = index2b;
                }
                else {
                    index = index - 22500;
                    theChars = index2c;
                }
                aChar = theChars.charAt(2*index);
                outputByte[0] = (byte)((aChar & 0xff00)>>8);
                outputByte[1] = (byte)(aChar & 0x00ff);
                aChar = theChars.charAt(2*index + 1);
                outputByte[2] = (byte)((aChar & 0xff00)>>8);
                outputByte[3] = (byte)(aChar & 0x00ff);
            for (i = 0; i < outputByte.length; i++) {
                if (outputByte[i] != 0x00) {
                allZeroes = false;
                break;
                }
            }
            if (allZeroes && inputChar != '\u0000') {
                return CoderResult.unmappableForLength(1);
            }
            int oindex = 0;
            for (spaceNeeded = outputByte.length;
                 spaceNeeded > 1; spaceNeeded--){
                if (outputByte[oindex++] != 0x00 )
                    break;
            }
            if (dp + spaceNeeded > dl)
                return CoderResult.OVERFLOW;
            for (i = outputByte.length - spaceNeeded;
                 i < outputByte.length; i++) {
                    da[dp++] = outputByte[i];
            }
            sp++;
        }
        return CoderResult.UNDERFLOW;
        } finally {
            src.position(sp - src.arrayOffset());
            dst.position(dp - dst.arrayOffset());
        }
    }
    private CoderResult encodeBufferLoop(CharBuffer src, ByteBuffer dst) {
        int     index;
        int     spaceNeeded;
        int     i;
        int mark = src.position();
        try {
            while (src.hasRemaining()) {
                char inputChar = src.get();
                boolean allZeroes = true;
                if (Character.isSurrogate(inputChar)) {
                    if (sgp.parse(inputChar, src) < 0)
                        return sgp.error();
                    return sgp.unmappableResult();
                }
                if (inputChar >= '\uFFFE')
                    return CoderResult.unmappableForLength(1);
                String theChars;
                char   aChar;
                index = index1[((inputChar & mask1) >> shift)] + (inputChar & mask2);
                if (index < 7500)
                    theChars = index2;
                else if (index < 15000) {
                     index = index - 7500;
                     theChars = index2a;
                } else if (index < 22500){
                    index = index - 15000;
                    theChars = index2b;
                }
                else {
                    index = index - 22500;
                    theChars = index2c;
                }
                aChar = theChars.charAt(2*index);
                outputByte[0] = (byte)((aChar & 0xff00)>>8);
                outputByte[1] = (byte)(aChar & 0x00ff);
                aChar = theChars.charAt(2*index + 1);
                outputByte[2] = (byte)((aChar & 0xff00)>>8);
                outputByte[3] = (byte)(aChar & 0x00ff);
            for (i = 0; i < outputByte.length; i++) {
                if (outputByte[i] != 0x00) {
                allZeroes = false;
                break;
                }
            }
            if (allZeroes && inputChar != '\u0000') {
                return CoderResult.unmappableForLength(1);
            }
            int oindex = 0;
            for (spaceNeeded = outputByte.length;
                 spaceNeeded > 1; spaceNeeded--){
                if (outputByte[oindex++] != 0x00 )
                    break;
            }
            if (dst.remaining() < spaceNeeded)
                return CoderResult.OVERFLOW;
            for (i = outputByte.length - spaceNeeded;
                 i < outputByte.length; i++) {
                    dst.put(outputByte[i]);
            }
            mark++;
            }
            return CoderResult.UNDERFLOW;
        } finally {
            src.position(mark);
        }
    }
    protected CoderResult encodeLoop(CharBuffer src, ByteBuffer dst) {
        if (true && src.hasArray() && dst.hasArray())
            return encodeArrayLoop(src, dst);
        else
            return encodeBufferLoop(src, dst);
    }
    public byte encode(char inputChar) {
        return (byte)index2.charAt(index1[(inputChar & mask1) >> shift] +
                (inputChar & mask2));
    }
}

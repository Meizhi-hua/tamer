public class MessageDigestTestSHA224 extends MessageDigestTest {
    public MessageDigestTestSHA224() {
        super("SHA-224");
        super.source1 = "abc";
        super.source2 = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq";
        super.expected1 = singleblock;
        super.expected2 = multiblock;
        super.expected3 = longmessage;
    }
    private static final String singleblock = "23097d223405d8228642a477bda255b32aadbce4bda0b3f7e36c9da7";
    private static final String multiblock = "75388b16512776cc5dba5da1fd890150b0c6455cb4f58b1952522525";
    private static final String longmessage = "20794655980c91d8bbb4c1ea97618a4bf03f42581948b2ee4ee7ad67";
}

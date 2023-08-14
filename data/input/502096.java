public class ClientHello extends Message {
    final byte[] client_version;
    final byte[] random = new byte[32];
    final byte[] session_id;
    final CipherSuite[] cipher_suites;
    final byte[] compression_methods;
    public ClientHello(SecureRandom sr, byte[] version, byte[] ses_id,
            CipherSuite[] cipher_suite) {
        client_version = version;
        long gmt_unix_time = System.currentTimeMillis()/1000;
        sr.nextBytes(random);
        random[0] = (byte) (gmt_unix_time & 0xFF000000 >>> 24);
        random[1] = (byte) (gmt_unix_time & 0xFF0000 >>> 16);
        random[2] = (byte) (gmt_unix_time & 0xFF00 >>> 8);
        random[3] = (byte) (gmt_unix_time & 0xFF);
        session_id = ses_id;
        this.cipher_suites = cipher_suite;
        compression_methods = new byte[] { 0 }; 
        length = 38 + session_id.length + (this.cipher_suites.length << 1)
                + compression_methods.length;
    }
    public ClientHello(HandshakeIODataStream in, int length) throws IOException {
        client_version = new byte[2];
        client_version[0] = (byte) in.readUint8();
        client_version[1] = (byte) in.readUint8();
        in.read(random, 0, 32);
        int size = in.read();
        session_id = new byte[size];
        in.read(session_id, 0, size);
        int l = in.readUint16();
        if ((l & 0x01) == 0x01) { 
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect ClientHello");
        }
        size = l >> 1;
        cipher_suites = new CipherSuite[size];
        for (int i = 0; i < size; i++) {
            byte b0 = (byte) in.read();
            byte b1 = (byte) in.read();
            cipher_suites[i] = CipherSuite.getByCode(b0, b1);
        }
        size = in.read();
        compression_methods = new byte[size];
        in.read(compression_methods, 0, size);
        this.length = 38 + session_id.length + (cipher_suites.length << 1)
                + compression_methods.length;
        if (this.length > length) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect ClientHello");
        }
        if (this.length < length) {
            in.skip(length - this.length);
            this.length = length;
        }
    }
    public ClientHello(HandshakeIODataStream in) throws IOException {
        if (in.readUint8() != 1) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect V2ClientHello");
        }
        client_version = new byte[2];
        client_version[0] = (byte) in.readUint8();
        client_version[1] = (byte) in.readUint8();
        int cipher_spec_length = in.readUint16();
        if (in.readUint16() != 0) { 
            fatalAlert(AlertProtocol.DECODE_ERROR,
                    "DECODE ERROR: incorrect V2ClientHello, cannot be used for resuming");
        }
        int challenge_length = in.readUint16();
        if (challenge_length < 16) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect V2ClientHello, short challenge data");
        }
        session_id = new byte[0];
        cipher_suites = new CipherSuite[cipher_spec_length/3];
        for (int i = 0; i < cipher_suites.length; i++) {
            byte b0 = (byte) in.read();
            byte b1 = (byte) in.read();
            byte b2 = (byte) in.read();
            cipher_suites[i] = CipherSuite.getByCode(b0, b1, b2);
        }
        compression_methods = new byte[] { 0 }; 
        if (challenge_length < 32) {
            Arrays.fill(random, 0, 32 - challenge_length, (byte)0);
            System.arraycopy(in.read(challenge_length), 0, random, 32 - challenge_length, challenge_length);            
        } else if (challenge_length == 32) {
            System.arraycopy(in.read(32), 0, random, 0, 32);            
        } else {
            System.arraycopy(in.read(challenge_length), challenge_length - 32, random, 0, 32);
        }
        if (in.available() > 0) {
            fatalAlert(AlertProtocol.DECODE_ERROR, "DECODE ERROR: incorrect V2ClientHello, extra data");
        }
        this.length = 38 + session_id.length + (cipher_suites.length << 1)
                + compression_methods.length;    
    }
    @Override
    public void send(HandshakeIODataStream out) {
        out.write(client_version);
        out.write(random);
        out.writeUint8(session_id.length);
        out.write(session_id);
        int size = cipher_suites.length << 1;
        out.writeUint16(size);
        for (int i = 0; i < cipher_suites.length; i++) {
            out.write(cipher_suites[i].toBytes());
        }
        out.writeUint8(compression_methods.length);
        for (int i = 0; i < compression_methods.length; i++) {
            out.write(compression_methods[i]);
        }
    }
    public byte[] getRandom() {
        return random;
    }
    @Override
    public int getType() {
        return Handshake.CLIENT_HELLO;
    }
}

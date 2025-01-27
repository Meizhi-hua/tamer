public class TestTransmit extends Utils {
    private final static String CMD_MARKER = "C-APDU: ";
    private final static String RES_MARKER = "R-APDU: ";
    public static void main(String[] args) throws Exception {
        CardTerminal terminal = getTerminal(args);
        Card card = terminal.connect("T=0");
        CardChannel channel = card.getBasicChannel();
        BufferedReader reader = new BufferedReader(new FileReader("apdu.log"));
        byte[] command = null;
        while (true) {
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            if (line.startsWith(CMD_MARKER)) {
                System.out.println(line);
                line = line.substring(CMD_MARKER.length());
                command = parse(line);
            } else if (line.startsWith(RES_MARKER)) {
                System.out.println(line);
                line = line.substring(RES_MARKER.length());
                Bytes response = parseWildcard(line);
                CommandAPDU capdu = new CommandAPDU(command);
                ResponseAPDU rapdu = channel.transmit(capdu);
                byte[] received = rapdu.getBytes();
                if (received.length != response.bytes.length) {
                    throw new Exception("Length mismatch: " + toString(received));
                }
                for (int i = 0; i < received.length; i++) {
                    byte mask = response.mask[i];
                    if ((received[i] & response.mask[i]) != response.bytes[i]) {
                        throw new Exception("Mismatch: " + toString(received));
                    }
                }
            } 
        }
        card.disconnect(false);
        System.out.println("OK.");
    }
    private static class Bytes {
        final byte[] bytes;
        final byte[] mask;
        Bytes(byte[] bytes, byte[] mask) {
            this.bytes = bytes;
            this.mask = mask;
        }
    }
    private static Bytes parseWildcard(String s) {
        try {
            int n = s.length();
            ByteArrayOutputStream out = new ByteArrayOutputStream(n >> 1);
            ByteArrayOutputStream mask = new ByteArrayOutputStream(n >> 1);
            StringReader r = new StringReader(s);
            while (true) {
                int b1 = nextNibble(r);
                if (b1 < 0) {
                    if (b1 == -1) {
                        break;
                    }
                    int b2 = nextNibble(r);
                    if (b2 != -2) {
                        throw new RuntimeException("Invalid wildcard: " + s);
                    }
                    out.write(0);
                    mask.write(0);
                    continue;
                }
                int b2 = nextNibble(r);
                if (b2 < 0) {
                    throw new RuntimeException("Invalid string " + s);
                }
                int b = (b1 << 4) | b2;
                out.write(b);
                mask.write(0xff);
            }
            byte[] b = out.toByteArray();
            byte[] m = mask.toByteArray();
            return new Bytes(b, m);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private static int nextNibble(StringReader r) throws IOException {
        while (true) {
            int ch = r.read();
            if (ch == -1) {
                return -1;
            } else if ((ch >= '0') && (ch <= '9')) {
                return ch - '0';
            } else if ((ch >= 'a') && (ch <= 'f')) {
                return ch - 'a' + 10;
            } else if ((ch >= 'A') && (ch <= 'F')) {
                return ch - 'A' + 10;
            } else if (ch == 'X') {
                return -2;
            }
        }
    }
}

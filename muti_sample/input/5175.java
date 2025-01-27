public abstract class NetClient {
    public static NetClient getInstance(String protocol, String hostname, int port,
            int timeout) throws IOException {
        if (protocol.equals("TCP")) {
            return new TCPClient(hostname, port, timeout);
        } else {
            return new UDPClient(hostname, port, timeout);
        }
    }
    abstract public void send(byte[] data) throws IOException;
    abstract public byte[] receive() throws IOException;
    abstract public void close() throws IOException;
}
class TCPClient extends NetClient {
    private Socket tcpSocket;
    private BufferedOutputStream out;
    private BufferedInputStream in;
    TCPClient(String hostname, int port, int timeout)
            throws IOException {
        tcpSocket = new Socket();
        tcpSocket.connect(new InetSocketAddress(hostname, port), timeout);
        out = new BufferedOutputStream(tcpSocket.getOutputStream());
        in = new BufferedInputStream(tcpSocket.getInputStream());
        tcpSocket.setSoTimeout(timeout);
    }
    @Override
    public void send(byte[] data) throws IOException {
        byte[] lenField = new byte[4];
        intToNetworkByteOrder(data.length, lenField, 0, 4);
        out.write(lenField);
        out.write(data);
        out.flush();
    }
    @Override
    public byte[] receive() throws IOException {
        byte[] lenField = new byte[4];
        int count = readFully(lenField, 4);
        if (count != 4) {
            if (Krb5.DEBUG) {
                System.out.println(
                    ">>>DEBUG: TCPClient could not read length field");
            }
            return null;
        }
        int len = networkByteOrderToInt(lenField, 0, 4);
        if (Krb5.DEBUG) {
            System.out.println(
                ">>>DEBUG: TCPClient reading " + len + " bytes");
        }
        if (len <= 0) {
            if (Krb5.DEBUG) {
                System.out.println(
                    ">>>DEBUG: TCPClient zero or negative length field: "+len);
            }
            return null;
        }
        byte data[] = new byte[len];
        count = readFully(data, len);
        if (count != len) {
            if (Krb5.DEBUG) {
                System.out.println(
                    ">>>DEBUG: TCPClient could not read complete packet (" +
                    len + "/" + count + ")");
            }
            return null;
        } else {
            return data;
        }
    }
    @Override
    public void close() throws IOException {
        tcpSocket.close();
    }
    private int readFully(byte[] inBuf, int total) throws IOException {
        int count, pos = 0;
        while (total > 0) {
            count = in.read(inBuf, pos, total);
            if (count == -1) {
                return (pos == 0? -1 : pos);
            }
            pos += count;
            total -= count;
        }
        return pos;
    }
    private static int networkByteOrderToInt(byte[] buf, int start,
        int count) {
        if (count > 4) {
            throw new IllegalArgumentException(
                "Cannot handle more than 4 bytes");
        }
        int answer = 0;
        for (int i = 0; i < count; i++) {
            answer <<= 8;
            answer |= ((int)buf[start+i] & 0xff);
        }
        return answer;
    }
    private static void intToNetworkByteOrder(int num, byte[] buf,
        int start, int count) {
        if (count > 4) {
            throw new IllegalArgumentException(
                "Cannot handle more than 4 bytes");
        }
        for (int i = count-1; i >= 0; i--) {
            buf[start+i] = (byte)(num & 0xff);
            num >>>= 8;
        }
    }
}
class UDPClient extends NetClient {
    InetAddress iaddr;
    int iport;
    int bufSize = 65507;
    DatagramSocket dgSocket;
    DatagramPacket dgPacketIn;
    UDPClient(String hostname, int port, int timeout)
        throws UnknownHostException, SocketException {
        iaddr = InetAddress.getByName(hostname);
        iport = port;
        dgSocket = new DatagramSocket();
        dgSocket.setSoTimeout(timeout);
    }
    @Override
    public void send(byte[] data) throws IOException {
        DatagramPacket dgPacketOut = new DatagramPacket(data, data.length,
                                                        iaddr, iport);
        dgSocket.send(dgPacketOut);
    }
    @Override
    public byte[] receive() throws IOException {
        byte ibuf[] = new byte[bufSize];
        dgPacketIn = new DatagramPacket(ibuf, ibuf.length);
        try {
            dgSocket.receive(dgPacketIn);
        }
        catch (SocketException e) {
            dgSocket.receive(dgPacketIn);
        }
        byte[] data = new byte[dgPacketIn.getLength()];
        System.arraycopy(dgPacketIn.getData(), 0, data, 0,
                         dgPacketIn.getLength());
        return data;
    }
    @Override
    public void close() {
        dgSocket.close();
    }
}

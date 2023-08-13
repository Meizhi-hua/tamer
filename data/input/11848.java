public class NotBound {
    static void checkBound(DatagramChannel dc) throws IOException {
        if (dc.getLocalAddress() == null)
            throw new RuntimeException("Not bound??");
    }
    static void wakeupWhenBound(final DatagramChannel dc) {
        Runnable wakeupTask = new Runnable() {
            public void run() {
                try {
                    InetSocketAddress local;
                    do {
                        Thread.sleep(50);
                        local = (InetSocketAddress)dc.getLocalAddress();
                    } while (local == null);
                    DatagramChannel sender = DatagramChannel.open();
                    try {
                        ByteBuffer bb = ByteBuffer.wrap("hello".getBytes());
                        InetAddress lh = InetAddress.getLocalHost();
                        SocketAddress target =
                            new InetSocketAddress(lh, local.getPort());
                        sender.send(bb, target);
                    } finally {
                        sender.close();
                    }
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }};
        new Thread(wakeupTask).start();
    }
    public static void main(String[] args) throws IOException {
        DatagramChannel dc;
        dc = DatagramChannel.open();
        try {
            DatagramChannel peer = DatagramChannel.open()
                .bind(new InetSocketAddress(0));
            int peerPort = ((InetSocketAddress)(peer.getLocalAddress())).getPort();
            try {
                dc.connect(new InetSocketAddress(InetAddress.getLocalHost(), peerPort));
                checkBound(dc);
            } finally {
                peer.close();
            }
        } finally {
            dc.close();
        }
        dc = DatagramChannel.open();
        try {
            ByteBuffer bb = ByteBuffer.wrap("ignore this".getBytes());
            SocketAddress target =
                new InetSocketAddress(InetAddress.getLocalHost(), 5000);
            dc.send(bb, target);
            checkBound(dc);
        } finally {
            dc.close();
        }
        dc = DatagramChannel.open();
        try {
            ByteBuffer bb = ByteBuffer.allocateDirect(128);
            wakeupWhenBound(dc);
            SocketAddress sender = dc.receive(bb);
            if (sender == null)
                throw new RuntimeException("Sender should not be null");
            checkBound(dc);
        } finally {
            dc.close();
        }
        dc = DatagramChannel.open();
        try {
            dc.configureBlocking(false);
            ByteBuffer bb = ByteBuffer.allocateDirect(128);
            SocketAddress sender = dc.receive(bb);
            if (sender != null)
                throw new RuntimeException("Sender should be null");
            checkBound(dc);
        } finally {
            dc.close();
        }
    }
}

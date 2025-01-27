public abstract class SelectorProviderImpl
    extends SelectorProvider
{
    public DatagramChannel openDatagramChannel() throws IOException {
        return new DatagramChannelImpl(this);
    }
    public DatagramChannel openDatagramChannel(ProtocolFamily family) throws IOException {
        return new DatagramChannelImpl(this, family);
    }
    public Pipe openPipe() throws IOException {
        return new PipeImpl(this);
    }
    public abstract AbstractSelector openSelector() throws IOException;
    public ServerSocketChannel openServerSocketChannel() throws IOException {
        return new ServerSocketChannelImpl(this);
    }
    public SocketChannel openSocketChannel() throws IOException {
        return new SocketChannelImpl(this);
    }
}

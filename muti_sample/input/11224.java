class MembershipKeyImpl
    extends MembershipKey
{
    private final MulticastChannel ch;
    private final InetAddress group;
    private final NetworkInterface interf;
    private final InetAddress source;
    private volatile boolean valid = true;
    private Object stateLock = new Object();
    private HashSet<InetAddress> blockedSet;
    private MembershipKeyImpl(MulticastChannel ch,
                              InetAddress group,
                              NetworkInterface interf,
                              InetAddress source)
    {
        this.ch = ch;
        this.group = group;
        this.interf = interf;
        this.source = source;
    }
    static class Type4 extends MembershipKeyImpl {
        private final int groupAddress;
        private final int interfAddress;
        private final int sourceAddress;
        Type4(MulticastChannel ch,
              InetAddress group,
              NetworkInterface interf,
              InetAddress source,
              int groupAddress,
              int interfAddress,
              int sourceAddress)
        {
            super(ch, group, interf, source);
            this.groupAddress = groupAddress;
            this.interfAddress = interfAddress;
            this.sourceAddress = sourceAddress;
        }
        int groupAddress() {
            return groupAddress;
        }
        int interfaceAddress() {
            return interfAddress;
        }
        int source() {
            return sourceAddress;
        }
    }
    static class Type6 extends MembershipKeyImpl {
        private final byte[] groupAddress;
        private final int index;
        private final byte[] sourceAddress;
        Type6(MulticastChannel ch,
              InetAddress group,
              NetworkInterface interf,
              InetAddress source,
              byte[] groupAddress,
              int index,
              byte[] sourceAddress)
        {
            super(ch, group, interf, source);
            this.groupAddress = groupAddress;
            this.index = index;
            this.sourceAddress = sourceAddress;
        }
        byte[] groupAddress() {
            return groupAddress;
        }
        int index() {
            return index;
        }
        byte[] source() {
            return sourceAddress;
        }
    }
    public boolean isValid() {
        return valid;
    }
    void invalidate() {
        valid = false;
    }
    public void drop() {
        ((DatagramChannelImpl)ch).drop(this);
    }
    @Override
    public MulticastChannel channel() {
        return ch;
    }
    @Override
    public InetAddress group() {
        return group;
    }
    @Override
    public NetworkInterface networkInterface() {
        return interf;
    }
    @Override
    public InetAddress sourceAddress() {
        return source;
    }
    @Override
    public MembershipKey block(InetAddress toBlock)
        throws IOException
    {
        if (source != null)
            throw new IllegalStateException("key is source-specific");
        synchronized (stateLock) {
            if ((blockedSet != null) && blockedSet.contains(toBlock)) {
                return this;
            }
            ((DatagramChannelImpl)ch).block(this, toBlock);
            if (blockedSet == null)
                blockedSet = new HashSet<InetAddress>();
            blockedSet.add(toBlock);
        }
        return this;
    }
    @Override
    public MembershipKey unblock(InetAddress toUnblock) {
        synchronized (stateLock) {
            if ((blockedSet == null) || !blockedSet.contains(toUnblock))
                throw new IllegalStateException("not blocked");
            ((DatagramChannelImpl)ch).unblock(this, toUnblock);
            blockedSet.remove(toUnblock);
        }
        return this;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        sb.append('<');
        sb.append(group.getHostAddress());
        sb.append(',');
        sb.append(interf.getName());
        if (source != null) {
            sb.append(',');
            sb.append(source.getHostAddress());
        }
        sb.append('>');
        return sb.toString();
    }
}

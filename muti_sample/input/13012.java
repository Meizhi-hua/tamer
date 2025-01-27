public class SocketOrChannelContactInfoImpl
    extends CorbaContactInfoBase
    implements SocketInfo
{
    protected boolean isHashCodeCached = false;
    protected int cachedHashCode;
    protected String socketType;
    protected String hostname;
    protected int    port;
    protected SocketOrChannelContactInfoImpl()
    {
    }
    protected SocketOrChannelContactInfoImpl(
        ORB orb,
        CorbaContactInfoList contactInfoList)
    {
        this.orb = orb;
        this.contactInfoList = contactInfoList;
    }
    public SocketOrChannelContactInfoImpl(
        ORB orb,
        CorbaContactInfoList contactInfoList,
        String socketType,
        String hostname,
        int port)
    {
        this(orb, contactInfoList);
        this.socketType = socketType;
        this.hostname = hostname;
        this.port     = port;
    }
    public SocketOrChannelContactInfoImpl(
        ORB orb,
        CorbaContactInfoList contactInfoList,
        IOR effectiveTargetIOR,
        short addressingDisposition,
        String socketType,
        String hostname,
        int port)
    {
        this(orb, contactInfoList, socketType, hostname, port);
        this.effectiveTargetIOR = effectiveTargetIOR;
        this.addressingDisposition = addressingDisposition;
    }
    public boolean isConnectionBased()
    {
        return true;
    }
    public boolean shouldCacheConnection()
    {
        return true;
    }
    public String getConnectionCacheType()
    {
        return CorbaTransportManager.SOCKET_OR_CHANNEL_CONNECTION_CACHE;
    }
    public Connection createConnection()
    {
        Connection connection =
            new SocketOrChannelConnectionImpl(orb, this,
                                              socketType, hostname, port);
        return connection;
    }
    public String getMonitoringName()
    {
        return "SocketConnections";
    }
    public String getType()
    {
        return socketType;
    }
    public String getHost()
    {
        return hostname;
    }
    public int getPort()
    {
        return port;
    }
    public int hashCode()
    {
        if (! isHashCodeCached) {
            cachedHashCode = socketType.hashCode() ^ hostname.hashCode() ^ port;
            isHashCodeCached = true;
        }
        return cachedHashCode;
    }
    public boolean equals(Object obj)
    {
        if (obj == null) {
            return false;
        } else if (!(obj instanceof SocketOrChannelContactInfoImpl)) {
            return false;
        }
        SocketOrChannelContactInfoImpl other =
            (SocketOrChannelContactInfoImpl) obj;
        if (port != other.port) {
            return false;
        }
        if (!hostname.equals(other.hostname)) {
            return false;
        }
        if (socketType == null) {
            if (other.socketType != null) {
                return false;
            }
        } else if (!socketType.equals(other.socketType)) {
            return false;
        }
        return true;
    }
    public String toString()
    {
        return
            "SocketOrChannelContactInfoImpl["
            + socketType + " "
            + hostname + " "
            + port
            + "]";
    }
    protected void dprint(String msg)
    {
        ORBUtility.dprint("SocketOrChannelContactInfoImpl", msg);
    }
}

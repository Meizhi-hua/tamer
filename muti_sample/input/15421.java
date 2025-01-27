public abstract class InputStreamHook extends ObjectInputStream
{
    static final OMGSystemException omgWrapper =
        OMGSystemException.get( CORBALogDomains.RPC_ENCODING ) ;
    static final UtilSystemException utilWrapper =
        UtilSystemException.get( CORBALogDomains.RPC_ENCODING ) ;
    private class HookGetFields extends ObjectInputStream.GetField {
        private Map fields = null;
        HookGetFields(Map fields){
            this.fields = fields;
        }
        public java.io.ObjectStreamClass getObjectStreamClass() {
            return null;
        }
        public boolean defaulted(String name)
            throws IOException, IllegalArgumentException  {
            return (!fields.containsKey(name));
        }
        public boolean get(String name, boolean defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Boolean)fields.get(name)).booleanValue();
        }
        public char get(String name, char defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Character)fields.get(name)).charValue();
        }
        public byte get(String name, byte defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Byte)fields.get(name)).byteValue();
        }
        public short get(String name, short defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Short)fields.get(name)).shortValue();
        }
        public int get(String name, int defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Integer)fields.get(name)).intValue();
        }
        public long get(String name, long defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Long)fields.get(name)).longValue();
        }
        public float get(String name, float defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return ((Float)fields.get(name)).floatValue();
        }
        public double get(String name, double defvalue)
            throws IOException, IllegalArgumentException  {
            if (defaulted(name))
                return defvalue;
            else return ((Double)fields.get(name)).doubleValue();
        }
        public Object get(String name, Object defvalue)
            throws IOException, IllegalArgumentException {
            if (defaulted(name))
                return defvalue;
            else return fields.get(name);
        }
        public String toString(){
            return fields.toString();
        }
    }
    public InputStreamHook()
        throws IOException {
        super();
    }
    public void defaultReadObject()
        throws IOException, ClassNotFoundException, NotActiveException
    {
        readObjectState.beginDefaultReadObject(this);
        defaultReadObjectDelegate();
        readObjectState.endDefaultReadObject(this);
    }
    public abstract void defaultReadObjectDelegate();
    abstract void readFields(java.util.Map fieldToValueMap)
        throws java.io.InvalidClassException, java.io.StreamCorruptedException,
               ClassNotFoundException, java.io.IOException;
    public ObjectInputStream.GetField readFields()
        throws IOException, ClassNotFoundException, NotActiveException {
        HashMap fieldValueMap = new HashMap();
        readFields(fieldValueMap);
        readObjectState.endDefaultReadObject(this);
        return new HookGetFields(fieldValueMap);
    }
    protected void setState(ReadObjectState newState) {
        readObjectState = newState;
    }
    protected abstract byte getStreamFormatVersion();
    protected abstract org.omg.CORBA_2_3.portable.InputStream getOrbStream();
    protected static class ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject) throws IOException {}
        public void endUnmarshalCustomValue(InputStreamHook stream) throws IOException {}
        public void beginDefaultReadObject(InputStreamHook stream) throws IOException {}
        public void endDefaultReadObject(InputStreamHook stream) throws IOException {}
        public void readData(InputStreamHook stream) throws IOException {}
    }
    protected ReadObjectState readObjectState = DEFAULT_STATE;
    protected static final ReadObjectState DEFAULT_STATE = new DefaultState();
    protected static final ReadObjectState IN_READ_OBJECT_OPT_DATA
        = new InReadObjectOptionalDataState();
    protected static final ReadObjectState IN_READ_OBJECT_NO_MORE_OPT_DATA
        = new InReadObjectNoMoreOptionalDataState();
    protected static final ReadObjectState IN_READ_OBJECT_DEFAULTS_SENT
        = new InReadObjectDefaultsSentState();
    protected static final ReadObjectState NO_READ_OBJECT_DEFAULTS_SENT
        = new NoReadObjectDefaultsSentState();
    protected static final ReadObjectState IN_READ_OBJECT_REMOTE_NOT_CUSTOM_MARSHALED
        = new InReadObjectRemoteDidNotUseWriteObjectState();
    protected static final ReadObjectState IN_READ_OBJECT_PAST_DEFAULTS_REMOTE_NOT_CUSTOM
        = new InReadObjectPastDefaultsRemoteDidNotUseWOState();
    protected static class DefaultState extends ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
            throws IOException {
            if (hasReadObject) {
                if (calledDefaultWriteObject)
                    stream.setState(IN_READ_OBJECT_DEFAULTS_SENT);
                else {
                    try {
                        if (stream.getStreamFormatVersion() == 2)
                            ((ValueInputStream)stream.getOrbStream()).start_value();
                    } catch( Exception e ) {
                    }
                    stream.setState(IN_READ_OBJECT_OPT_DATA);
                }
            } else {
                if (calledDefaultWriteObject)
                    stream.setState(NO_READ_OBJECT_DEFAULTS_SENT);
                else
                    throw new StreamCorruptedException("No default data sent");
            }
        }
    }
    protected static class InReadObjectRemoteDidNotUseWriteObjectState extends ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
        {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }
        public void endDefaultReadObject(InputStreamHook stream) {
            stream.setState(IN_READ_OBJECT_PAST_DEFAULTS_REMOTE_NOT_CUSTOM);
        }
        public void readData(InputStreamHook stream) {
            stream.throwOptionalDataIncompatibleException();
        }
    }
    protected static class InReadObjectPastDefaultsRemoteDidNotUseWOState extends ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
        {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }
        public void beginDefaultReadObject(InputStreamHook stream) throws IOException
        {
            throw new StreamCorruptedException("Default data already read");
        }
        public void readData(InputStreamHook stream) {
            stream.throwOptionalDataIncompatibleException();
        }
    }
    protected void throwOptionalDataIncompatibleException()
    {
        throw omgWrapper.rmiiiopOptionalDataIncompatible2() ;
    }
    protected static class InReadObjectDefaultsSentState extends ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject) {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }
        public void endUnmarshalCustomValue(InputStreamHook stream) {
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).start_value();
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }
            stream.setState(DEFAULT_STATE);
        }
        public void endDefaultReadObject(InputStreamHook stream) throws IOException {
            if (stream.getStreamFormatVersion() == 2)
                ((ValueInputStream)stream.getOrbStream()).start_value();
            stream.setState(IN_READ_OBJECT_OPT_DATA);
        }
        public void readData(InputStreamHook stream) throws IOException {
            org.omg.CORBA.ORB orb = stream.getOrbStream().orb();
            if ((orb == null) ||
                    !(orb instanceof com.sun.corba.se.spi.orb.ORB)) {
                throw new StreamCorruptedException(
                                     "Default data must be read first");
            }
            ORBVersion clientOrbVersion =
                ((com.sun.corba.se.spi.orb.ORB)orb).getORBVersion();
            if ((ORBVersionFactory.getPEORB().compareTo(clientOrbVersion) <= 0) ||
                    (clientOrbVersion.equals(ORBVersionFactory.getFOREIGN()))) {
                throw new StreamCorruptedException("Default data must be read first");
            }
        }
    }
    protected static class InReadObjectOptionalDataState extends ReadObjectState {
        public void beginUnmarshalCustomValue(InputStreamHook stream,
                                              boolean calledDefaultWriteObject,
                                              boolean hasReadObject)
        {
            throw utilWrapper.badBeginUnmarshalCustomValue() ;
        }
        public void endUnmarshalCustomValue(InputStreamHook stream) throws IOException
        {
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }
            stream.setState(DEFAULT_STATE);
        }
        public void beginDefaultReadObject(InputStreamHook stream) throws IOException
        {
            throw new StreamCorruptedException("Default data not sent or already read/passed");
        }
    }
    protected static class InReadObjectNoMoreOptionalDataState
        extends InReadObjectOptionalDataState {
        public void readData(InputStreamHook stream) throws IOException {
            stream.throwOptionalDataIncompatibleException();
        }
    }
    protected static class NoReadObjectDefaultsSentState extends ReadObjectState {
        public void endUnmarshalCustomValue(InputStreamHook stream) throws IOException {
            if (stream.getStreamFormatVersion() == 2) {
                ((ValueInputStream)stream.getOrbStream()).start_value();
                ((ValueInputStream)stream.getOrbStream()).end_value();
            }
            stream.setState(DEFAULT_STATE);
        }
    }
}

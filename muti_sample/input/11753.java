public class ParserTable {
    private static String MY_CLASS_NAME = ParserTable.class.getName() ;
    private static ParserTable myInstance = new ParserTable() ;
    private ORBUtilSystemException wrapper ;
    public static ParserTable get()
    {
        return myInstance ;
    }
    private ParserData[] parserData ;
    public ParserData[] getParserData()
    {
        return parserData ;
    }
    private ParserTable() {
        wrapper = ORBUtilSystemException.get( CORBALogDomains.ORB_LIFECYCLE ) ;
        String codeSetTestString =
            OSFCodeSetRegistry.ISO_8859_1_VALUE + "," +
            OSFCodeSetRegistry.UTF_16_VALUE + "," +
            OSFCodeSetRegistry.ISO_646_VALUE ;
        String[] debugTestData = { "subcontract", "poa", "transport" } ;
        USLPort[] USLPorts = { new USLPort( "FOO", 2701 ), new USLPort( "BAR", 3333 ) } ;
        ReadTimeouts readTimeouts =
               TransportDefault.makeReadTimeoutsFactory().create(
                    ORBConstants.TRANSPORT_TCP_INITIAL_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_MAX_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_GIOP_HEADER_MAX_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_TIME_TO_WAIT_BACKOFF_FACTOR);
        ORBInitializer[] TestORBInitializers =
            { null,
              new TestORBInitializer1(),
              new TestORBInitializer2() }  ;
        StringPair[] TestORBInitData = {
            new StringPair( "foo.bar.blech.NonExistent", "dummy" ),
            new StringPair( MY_CLASS_NAME + "$TestORBInitializer1", "dummy" ),
            new StringPair( MY_CLASS_NAME + "$TestORBInitializer2", "dummy" ) } ;
        Acceptor[] TestAcceptors =
            { new TestAcceptor2(),
              new TestAcceptor1(),
              null }  ;
        StringPair[] TestAcceptorData = {
            new StringPair( "foo.bar.blech.NonExistent", "dummy" ),
            new StringPair( MY_CLASS_NAME + "$TestAcceptor1", "dummy" ),
            new StringPair( MY_CLASS_NAME + "$TestAcceptor2", "dummy" ) } ;
        StringPair[] TestORBInitRefData =
            { new StringPair( "Foo", "ior:930492049394" ),
              new StringPair( "Bar", "ior:3453465785633576" ) } ;
        URL testServicesURL = null ;
        String testServicesString = "corbaloc::camelot/NameService" ;
        try {
            testServicesURL = new URL( testServicesString )  ;
        } catch (Exception exc) {
        }
        ParserData[] pd = {
            ParserDataFactory.make( ORBConstants.DEBUG_PROPERTY,
                OperationFactory.listAction( ",", OperationFactory.stringAction()),
                "debugFlags", new String[0],
                debugTestData, "subcontract,poa,transport" ),
            ParserDataFactory.make( ORBConstants.INITIAL_HOST_PROPERTY,
                OperationFactory.stringAction(),
                "ORBInitialHost", "",
                "Foo", "Foo" ),
            ParserDataFactory.make( ORBConstants.INITIAL_PORT_PROPERTY,
                OperationFactory.integerAction(),
                "ORBInitialPort", new Integer( ORBConstants.DEFAULT_INITIAL_PORT ),
                new Integer( 27314 ), "27314" ),
            ParserDataFactory.make( ORBConstants.SERVER_HOST_PROPERTY,
                OperationFactory.stringAction(),
                "ORBServerHost", "",
                "camelot", "camelot" ),
            ParserDataFactory.make( ORBConstants.SERVER_PORT_PROPERTY,
                OperationFactory.integerAction(),
                "ORBServerPort", new Integer( 0 ),
                new Integer( 38143 ), "38143" ),
            ParserDataFactory.make( ORBConstants.LISTEN_ON_ALL_INTERFACES,
                OperationFactory.stringAction(),
                "listenOnAllInterfaces", ORBConstants.LISTEN_ON_ALL_INTERFACES,
                "foo", "foo" ),
            ParserDataFactory.make( ORBConstants.ORB_ID_PROPERTY,
                OperationFactory.stringAction(),
                "orbId", "",
                "foo", "foo" ),
            ParserDataFactory.make( ORBConstants.OLD_ORB_ID_PROPERTY,
                OperationFactory.stringAction(),
                "orbId", "",
                "foo", "foo" ),
            ParserDataFactory.make( ORBConstants.ORB_SERVER_ID_PROPERTY,
                OperationFactory.integerAction(),
                "persistentServerId", new Integer(-1),
                new Integer( 1234), "1234" ),
            ParserDataFactory.make(
                ORBConstants.ORB_SERVER_ID_PROPERTY,
                OperationFactory.setFlagAction(),
                "persistentServerIdInitialized", Boolean.FALSE,
                Boolean.TRUE, "1234" ),
            ParserDataFactory.make(
                ORBConstants.ORB_SERVER_ID_PROPERTY,
                OperationFactory.setFlagAction(),
                "orbServerIdPropertySpecified", Boolean.FALSE,
                Boolean.TRUE, "1234" ),
            ParserDataFactory.make( ORBConstants.HIGH_WATER_MARK_PROPERTY,
                OperationFactory.integerAction(),
                "highWaterMark", new Integer( 240 ),
                new Integer( 3745 ), "3745" ),
            ParserDataFactory.make( ORBConstants.LOW_WATER_MARK_PROPERTY,
                OperationFactory.integerAction(),
                "lowWaterMark", new Integer( 100 ),
                new Integer( 12 ), "12" ),
            ParserDataFactory.make( ORBConstants.NUMBER_TO_RECLAIM_PROPERTY,
                OperationFactory.integerAction(),
                "numberToReclaim", new Integer( 5 ),
                new Integer( 231 ), "231" ),
            ParserDataFactory.make( ORBConstants.GIOP_VERSION,
                makeGVOperation(),
                "giopVersion", GIOPVersion.DEFAULT_VERSION,
                new GIOPVersion( 2, 3 ), "2.3" ),
            ParserDataFactory.make( ORBConstants.GIOP_FRAGMENT_SIZE,
                makeFSOperation(), "giopFragmentSize",
                new Integer( ORBConstants.GIOP_DEFAULT_FRAGMENT_SIZE ),
                new Integer( 65536 ), "65536" ),
            ParserDataFactory.make( ORBConstants.GIOP_BUFFER_SIZE,
                OperationFactory.integerAction(),
                "giopBufferSize", new Integer( ORBConstants.GIOP_DEFAULT_BUFFER_SIZE ),
                new Integer( 234000 ), "234000" ),
            ParserDataFactory.make( ORBConstants.GIOP_11_BUFFMGR,
                makeBMGROperation(),
                "giop11BuffMgr", new Integer( ORBConstants.DEFAULT_GIOP_11_BUFFMGR ),
                new Integer( 1 ), "CLCT" ),
            ParserDataFactory.make( ORBConstants.GIOP_12_BUFFMGR,
                makeBMGROperation(),
                "giop12BuffMgr", new Integer( ORBConstants.DEFAULT_GIOP_12_BUFFMGR ),
                new Integer( 0 ), "GROW" ),
            ParserDataFactory.make( ORBConstants.GIOP_TARGET_ADDRESSING,
                OperationFactory.compose( OperationFactory.integerRangeAction( 0, 3 ),
                    OperationFactory.convertIntegerToShort() ),
                "giopTargetAddressPreference",
                new Short( ORBConstants.ADDR_DISP_HANDLE_ALL ),
                new Short( (short)2 ), "2" ),
            ParserDataFactory.make( ORBConstants.GIOP_TARGET_ADDRESSING,
                makeADOperation(),
                "giopAddressDisposition", new Short( KeyAddr.value ),
                new Short( (short)2 ), "2" ),
            ParserDataFactory.make( ORBConstants.ALWAYS_SEND_CODESET_CTX_PROPERTY,
                OperationFactory.booleanAction(),
                "alwaysSendCodeSetCtx", Boolean.TRUE,
                Boolean.FALSE, "false"),
            ParserDataFactory.make( ORBConstants.USE_BOMS,
                OperationFactory.booleanAction(),
                "useByteOrderMarkers",
                    Boolean.valueOf( ORBConstants.DEFAULT_USE_BYTE_ORDER_MARKERS ),
                Boolean.FALSE, "false" ),
            ParserDataFactory.make( ORBConstants.USE_BOMS_IN_ENCAPS,
                OperationFactory.booleanAction(),
                "useByteOrderMarkersInEncaps",
                    Boolean.valueOf( ORBConstants.DEFAULT_USE_BYTE_ORDER_MARKERS_IN_ENCAPS ),
                Boolean.FALSE, "false" ),
            ParserDataFactory.make( ORBConstants.CHAR_CODESETS,
                makeCSOperation(),
                "charData", CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.getCharComponent(),
                CodeSetComponentInfo.createFromString( codeSetTestString ), codeSetTestString ),
            ParserDataFactory.make( ORBConstants.WCHAR_CODESETS,
                makeCSOperation(),
                "wcharData", CodeSetComponentInfo.JAVASOFT_DEFAULT_CODESETS.getWCharComponent(),
                CodeSetComponentInfo.createFromString( codeSetTestString ), codeSetTestString ),
            ParserDataFactory.make( ORBConstants.ALLOW_LOCAL_OPTIMIZATION,
                OperationFactory.booleanAction(),
                "allowLocalOptimization", Boolean.FALSE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.LEGACY_SOCKET_FACTORY_CLASS_PROPERTY,
                makeLegacySocketFactoryOperation(),
                "legacySocketFactory", null,
                new TestLegacyORBSocketFactory(),
                MY_CLASS_NAME + "$TestLegacyORBSocketFactory" ),
            ParserDataFactory.make( ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY,
                makeSocketFactoryOperation(),
                "socketFactory", new DefaultSocketFactoryImpl(),
                new TestORBSocketFactory(),
                MY_CLASS_NAME + "$TestORBSocketFactory" ),
            ParserDataFactory.make( ORBConstants.LISTEN_SOCKET_PROPERTY,
                makeUSLOperation() ,
                "userSpecifiedListenPorts", new USLPort[0],
                USLPorts, "FOO:2701,BAR:3333" ),
            ParserDataFactory.make( ORBConstants.IOR_TO_SOCKET_INFO_CLASS_PROPERTY,
                makeIORToSocketInfoOperation(),
                "iorToSocketInfo", new DefaultIORToSocketInfoImpl(),
                new TestIORToSocketInfo(),
                MY_CLASS_NAME + "$TestIORToSocketInfo" ),
            ParserDataFactory.make( ORBConstants.IIOP_PRIMARY_TO_CONTACT_INFO_CLASS_PROPERTY,
                makeIIOPPrimaryToContactInfoOperation(),
                "iiopPrimaryToContactInfo", null,
                new TestIIOPPrimaryToContactInfo(),
                MY_CLASS_NAME + "$TestIIOPPrimaryToContactInfo" ),
            ParserDataFactory.make( ORBConstants.CONTACT_INFO_LIST_FACTORY_CLASS_PROPERTY,
                makeContactInfoListFactoryOperation(),
                "corbaContactInfoListFactory", null,
                new TestContactInfoListFactory(),
                MY_CLASS_NAME + "$TestContactInfoListFactory" ),
            ParserDataFactory.make( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                OperationFactory.integerAction(),
                "persistentServerPort", new Integer( 0 ),
                new Integer( 2743 ), "2743" ),
            ParserDataFactory.make( ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY,
                OperationFactory.setFlagAction(),
                "persistentPortInitialized", Boolean.FALSE,
                Boolean.TRUE, "2743" ),
            ParserDataFactory.make( ORBConstants.SERVER_ID_PROPERTY,
                OperationFactory.integerAction(),
                "persistentServerId", new Integer( 0 ),
                new Integer( 294 ), "294" ),
            ParserDataFactory.make( ORBConstants.SERVER_ID_PROPERTY,
                OperationFactory.setFlagAction(),
                "persistentServerIdInitialized", Boolean.FALSE,
                Boolean.TRUE, "294" ),
            ParserDataFactory.make( ORBConstants.SERVER_ID_PROPERTY,
                OperationFactory.setFlagAction(),
                "orbServerIdPropertySpecified", Boolean.FALSE,
                Boolean.TRUE, "294" ),
            ParserDataFactory.make( ORBConstants.ACTIVATED_PROPERTY,
                OperationFactory.booleanAction(),
                "serverIsORBActivated", Boolean.FALSE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.BAD_SERVER_ID_HANDLER_CLASS_PROPERTY,
                OperationFactory.classAction(),
                "badServerIdHandlerClass", null,
                TestBadServerIdHandler.class, MY_CLASS_NAME + "$TestBadServerIdHandler" ),
            ParserDataFactory.make( ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX,
                makeROIOperation(),
                "orbInitializers", new ORBInitializer[0],
                TestORBInitializers, TestORBInitData, ORBInitializer.class ),
            ParserDataFactory.make( ORBConstants.ACCEPTOR_CLASS_PREFIX_PROPERTY,
                makeAcceptorInstantiationOperation(),
                "acceptors", new Acceptor[0],
                TestAcceptors, TestAcceptorData, Acceptor.class ),
            ParserDataFactory.make( ORBConstants.ACCEPTOR_SOCKET_TYPE_PROPERTY,
                OperationFactory.stringAction(),
                "acceptorSocketType", ORBConstants.SOCKETCHANNEL,
                "foo", "foo" ),
            ParserDataFactory.make( ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY,
                OperationFactory.booleanAction(),
                "acceptorSocketUseSelectThreadToWait", Boolean.TRUE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.ACCEPTOR_SOCKET_USE_WORKER_THREAD_FOR_EVENT_PROPERTY,
                OperationFactory.booleanAction(),
                "acceptorSocketUseWorkerThreadForEvent", Boolean.TRUE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.CONNECTION_SOCKET_TYPE_PROPERTY,
                OperationFactory.stringAction(),
                "connectionSocketType", ORBConstants.SOCKETCHANNEL,
                "foo", "foo" ),
            ParserDataFactory.make( ORBConstants.USE_NIO_SELECT_TO_WAIT_PROPERTY,
                OperationFactory.booleanAction(),
                "connectionSocketUseSelectThreadToWait", Boolean.TRUE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.CONNECTION_SOCKET_USE_WORKER_THREAD_FOR_EVENT_PROPERTY,
                OperationFactory.booleanAction(),
                "connectionSocketUseWorkerThreadForEvent", Boolean.TRUE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make( ORBConstants.DISABLE_DIRECT_BYTE_BUFFER_USE_PROPERTY,
                OperationFactory.booleanAction(),
                "disableDirectByteBufferUse", Boolean.FALSE,
                Boolean.TRUE, "true" ),
            ParserDataFactory.make(ORBConstants.TRANSPORT_TCP_READ_TIMEOUTS_PROPERTY,
                makeTTCPRTOperation(),
                "readTimeouts",  TransportDefault.makeReadTimeoutsFactory().create(
                    ORBConstants.TRANSPORT_TCP_INITIAL_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_MAX_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_GIOP_HEADER_MAX_TIME_TO_WAIT,
                    ORBConstants.TRANSPORT_TCP_TIME_TO_WAIT_BACKOFF_FACTOR),
                readTimeouts, "100:3000:300:20" ),
            ParserDataFactory.make(
                ORBConstants.ENABLE_JAVA_SERIALIZATION_PROPERTY,
                OperationFactory.booleanAction(),
                "enableJavaSerialization", Boolean.FALSE,
                Boolean.FALSE, "false"),
            ParserDataFactory.make(
                ORBConstants.USE_REP_ID,
                OperationFactory.booleanAction(),
                "useRepId", Boolean.TRUE,
                Boolean.TRUE, "true"),
            ParserDataFactory.make( ORBConstants.ORB_INIT_REF_PROPERTY,
                OperationFactory.identityAction(),
                "orbInitialReferences", new StringPair[0],
                TestORBInitRefData, TestORBInitRefData, StringPair.class )
        } ;
        parserData = pd ;
    }
    public final class TestBadServerIdHandler implements BadServerIdHandler
    {
        public boolean equals( Object other )
        {
            return other instanceof TestBadServerIdHandler ;
        }
        public void handle( ObjectKey objectKey )
        {
        }
    }
    private Operation makeTTCPRTOperation()
    {
        Operation[] fourIop = { OperationFactory.integerAction(),
                                OperationFactory.integerAction(),
                                OperationFactory.integerAction(),
                                OperationFactory.integerAction() } ;
        Operation op2 = OperationFactory.sequenceAction( ":", fourIop ) ;
        Operation rtOp = new Operation() {
            public Object operate(Object value)
            {
                Object[] values = (Object[])value ;
                Integer initialTime = (Integer)(values[0]) ;
                Integer maxGIOPHdrTime = (Integer)(values[1]) ;
                Integer maxGIOPBodyTime = (Integer)(values[2]) ;
                Integer backoffPercent = (Integer)(values[3]) ;
                return TransportDefault.makeReadTimeoutsFactory().create(
                                                   initialTime.intValue(),
                                                   maxGIOPHdrTime.intValue(),
                                                   maxGIOPBodyTime.intValue(),
                                                   backoffPercent.intValue());
            }
        } ;
        Operation ttcprtOp = OperationFactory.compose(op2, rtOp);
        return ttcprtOp;
    }
    private Operation makeUSLOperation()
    {
        Operation[] siop = { OperationFactory.stringAction(),
            OperationFactory.integerAction() } ;
        Operation op2 = OperationFactory.sequenceAction( ":", siop ) ;
        Operation uslop = new Operation() {
            public Object operate( Object value )
            {
                Object[] values = (Object[])value ;
                String type = (String)(values[0]) ;
                Integer port = (Integer)(values[1]) ;
                return new USLPort( type, port.intValue() ) ;
            }
        } ;
        Operation op3 = OperationFactory.compose( op2, uslop ) ;
        Operation listenop = OperationFactory.listAction( ",", op3 ) ;
        return listenop ;
    }
    public static final class TestLegacyORBSocketFactory
        implements com.sun.corba.se.spi.legacy.connection.ORBSocketFactory
    {
        public boolean equals( Object other )
        {
            return other instanceof TestLegacyORBSocketFactory ;
        }
        public ServerSocket createServerSocket( String type, int port )
        {
            return null ;
        }
        public SocketInfo getEndPointInfo( org.omg.CORBA.ORB orb,
            IOR ior, SocketInfo socketInfo )
        {
            return null ;
        }
        public Socket createSocket( SocketInfo socketInfo )
        {
            return null ;
        }
    }
    public static final class TestORBSocketFactory
        implements com.sun.corba.se.spi.transport.ORBSocketFactory
    {
        public boolean equals( Object other )
        {
            return other instanceof TestORBSocketFactory ;
        }
        public void setORB(ORB orb)
        {
        }
        public ServerSocket createServerSocket( String type, InetSocketAddress a )
        {
            return null ;
        }
        public Socket createSocket( String type, InetSocketAddress a )
        {
            return null ;
        }
        public void setAcceptedSocketOptions(Acceptor acceptor,
                                             ServerSocket serverSocket,
                                             Socket socket)
        {
        }
    }
    public static final class TestIORToSocketInfo
        implements IORToSocketInfo
    {
        public boolean equals( Object other )
        {
            return other instanceof TestIORToSocketInfo;
        }
        public List getSocketInfo(IOR ior)
        {
            return null;
        }
    }
    public static final class TestIIOPPrimaryToContactInfo
        implements IIOPPrimaryToContactInfo
    {
        public void reset(ContactInfo primary)
        {
        }
        public boolean hasNext(ContactInfo primary,
                               ContactInfo previous,
                               List contactInfos)
        {
            return true;
        }
        public ContactInfo next(ContactInfo primary,
                                ContactInfo previous,
                                List contactInfos)
        {
            return null;
        }
    }
    public static final class TestContactInfoListFactory
        implements CorbaContactInfoListFactory
    {
        public boolean equals( Object other )
        {
            return other instanceof TestContactInfoListFactory;
        }
        public void setORB(ORB orb) { }
        public CorbaContactInfoList create( IOR ior ) { return null; }
    }
    private Operation makeMapOperation( final Map map )
    {
        return new Operation() {
            public Object operate( Object value )
            {
                return map.get( value ) ;
            }
        } ;
    }
    private Operation makeBMGROperation()
    {
        Map map = new HashMap() ;
        map.put( "GROW", new Integer(0) ) ;
        map.put( "CLCT", new Integer(1) ) ;
        map.put( "STRM", new Integer(2) ) ;
        return makeMapOperation( map ) ;
    }
    private Operation makeLegacySocketFactoryOperation()
    {
        Operation sfop = new Operation() {
            public Object operate( Object value )
            {
                String param = (String)value ;
                try {
                    Class legacySocketFactoryClass =
                        ORBClassLoader.loadClass(param);
                    if (com.sun.corba.se.spi.legacy.connection.ORBSocketFactory.class.isAssignableFrom(legacySocketFactoryClass)) {
                        return legacySocketFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalSocketFactoryType( legacySocketFactoryClass.toString() ) ;
                    }
                } catch (Exception ex) {
                    throw wrapper.badCustomSocketFactory( ex, param ) ;
                }
            }
        } ;
        return sfop ;
    }
    private Operation makeSocketFactoryOperation()
    {
        Operation sfop = new Operation() {
            public Object operate( Object value )
            {
                String param = (String)value ;
                try {
                    Class socketFactoryClass = ORBClassLoader.loadClass(param);
                    if (com.sun.corba.se.spi.transport.ORBSocketFactory.class.isAssignableFrom(socketFactoryClass)) {
                        return socketFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalSocketFactoryType( socketFactoryClass.toString() ) ;
                    }
                } catch (Exception ex) {
                    throw wrapper.badCustomSocketFactory( ex, param ) ;
                }
            }
        } ;
        return sfop ;
    }
    private Operation makeIORToSocketInfoOperation()
    {
        Operation op = new Operation() {
            public Object operate( Object value )
            {
                String param = (String)value ;
                try {
                    Class iorToSocketInfoClass = ORBClassLoader.loadClass(param);
                    if (IORToSocketInfo.class.isAssignableFrom(iorToSocketInfoClass)) {
                        return iorToSocketInfoClass.newInstance();
                    } else {
                        throw wrapper.illegalIorToSocketInfoType( iorToSocketInfoClass.toString() ) ;
                    }
                } catch (Exception ex) {
                    throw wrapper.badCustomIorToSocketInfo( ex, param ) ;
                }
            }
        } ;
        return op ;
    }
    private Operation makeIIOPPrimaryToContactInfoOperation()
    {
        Operation op = new Operation() {
            public Object operate( Object value )
            {
                String param = (String)value ;
                try {
                    Class iiopPrimaryToContactInfoClass = ORBClassLoader.loadClass(param);
                    if (IIOPPrimaryToContactInfo.class.isAssignableFrom(iiopPrimaryToContactInfoClass)) {
                        return iiopPrimaryToContactInfoClass.newInstance();
                    } else {
                        throw wrapper.illegalIiopPrimaryToContactInfoType( iiopPrimaryToContactInfoClass.toString() ) ;
                    }
                } catch (Exception ex) {
                    throw wrapper.badCustomIiopPrimaryToContactInfo( ex, param ) ;
                }
            }
        } ;
        return op ;
    }
    private Operation makeContactInfoListFactoryOperation()
    {
        Operation op = new Operation() {
            public Object operate( Object value )
            {
                String param = (String)value ;
                try {
                    Class contactInfoListFactoryClass =
                        ORBClassLoader.loadClass(param);
                    if (CorbaContactInfoListFactory.class.isAssignableFrom(
                        contactInfoListFactoryClass)) {
                        return contactInfoListFactoryClass.newInstance();
                    } else {
                        throw wrapper.illegalContactInfoListFactoryType(
                            contactInfoListFactoryClass.toString() ) ;
                    }
                } catch (Exception ex) {
                    throw wrapper.badContactInfoListFactory( ex, param ) ;
                }
            }
        } ;
        return op ;
    }
    private Operation makeCSOperation()
    {
        Operation csop = new Operation() {
            public Object operate( Object value )
            {
                String val = (String)value ;
                return CodeSetComponentInfo.createFromString( val ) ;
            }
        } ;
        return csop ;
    }
    private Operation makeADOperation()
    {
        Operation admap = new Operation() {
            private Integer[] map = {
                new Integer( KeyAddr.value ),
                new Integer( ProfileAddr.value ),
                new Integer( ReferenceAddr.value ),
                new Integer( KeyAddr.value ) } ;
            public Object operate( Object value )
            {
                int val = ((Integer)value).intValue() ;
                return map[val] ;
            }
        } ;
        Operation rangeop = OperationFactory.integerRangeAction( 0, 3 ) ;
        Operation op1 = OperationFactory.compose( rangeop, admap ) ;
        Operation result = OperationFactory.compose( op1, OperationFactory.convertIntegerToShort() ) ;
        return result ;
    }
    private Operation makeFSOperation() {
        Operation fschecker = new Operation() {
            public Object operate( Object value )
            {
                int giopFragmentSize = ((Integer)value).intValue() ;
                if (giopFragmentSize < ORBConstants.GIOP_FRAGMENT_MINIMUM_SIZE){
                    throw wrapper.fragmentSizeMinimum( new Integer( giopFragmentSize ),
                        new Integer( ORBConstants.GIOP_FRAGMENT_MINIMUM_SIZE ) ) ;
                }
                if (giopFragmentSize % ORBConstants.GIOP_FRAGMENT_DIVISOR != 0)
                    throw wrapper.fragmentSizeDiv( new Integer( giopFragmentSize ),
                            new Integer( ORBConstants.GIOP_FRAGMENT_DIVISOR ) ) ;
                return value ;
            }
        } ;
        Operation result = OperationFactory.compose( OperationFactory.integerAction(),
            fschecker ) ;
        return result ;
    }
    private Operation makeGVOperation() {
        Operation gvHelper = OperationFactory.listAction( ".",
            OperationFactory.integerAction() ) ;
        Operation gvMain = new Operation() {
            public Object operate( Object value )
            {
                Object[] nums = (Object[])value ;
                int major = ((Integer)(nums[0])).intValue() ;
                int minor = ((Integer)(nums[1])).intValue() ;
                return new GIOPVersion( major, minor ) ;
            }
        } ;
        Operation result = OperationFactory.compose( gvHelper, gvMain );
        return result ;
    }
    public static final class TestORBInitializer1 extends org.omg.CORBA.LocalObject
        implements ORBInitializer
    {
        public boolean equals( Object other )
        {
            return other instanceof TestORBInitializer1 ;
        }
        public void pre_init( ORBInitInfo info )
        {
        }
        public void post_init( ORBInitInfo info )
        {
        }
    }
    public static final class TestORBInitializer2 extends org.omg.CORBA.LocalObject
        implements ORBInitializer
    {
        public boolean equals( Object other )
        {
            return other instanceof TestORBInitializer2 ;
        }
        public void pre_init( ORBInitInfo info )
        {
        }
        public void post_init( ORBInitInfo info )
        {
        }
    }
    private Operation makeROIOperation() {
        Operation clsop = OperationFactory.classAction() ;
        Operation indexOp = OperationFactory.suffixAction() ;
        Operation op1 = OperationFactory.compose( indexOp, clsop ) ;
        Operation mop = OperationFactory.maskErrorAction( op1 ) ;
        Operation mkinst = new Operation() {
            public Object operate( Object value )
            {
                final Class initClass = (Class)value ;
                if (initClass == null)
                    return null ;
                if( org.omg.PortableInterceptor.ORBInitializer.class.isAssignableFrom(
                    initClass ) ) {
                    ORBInitializer initializer = null ;
                    try {
                        initializer = (ORBInitializer)AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                                public Object run()
                                    throws InstantiationException, IllegalAccessException
                                {
                                    return initClass.newInstance() ;
                                }
                            }
                        ) ;
                    } catch (PrivilegedActionException exc) {
                        throw wrapper.orbInitializerFailure( exc.getException(),
                            initClass.getName() ) ;
                    } catch (Exception exc) {
                        throw wrapper.orbInitializerFailure( exc, initClass.getName() ) ;
                    }
                    return initializer ;
                } else {
                    throw wrapper.orbInitializerType( initClass.getName() ) ;
                }
            }
        } ;
        Operation result = OperationFactory.compose( mop, mkinst ) ;
        return result ;
    }
    public static final class TestAcceptor1
        implements Acceptor
    {
        public boolean equals( Object other )
        {
            return other instanceof TestAcceptor1 ;
        }
        public boolean initialize() { return true; }
        public boolean initialized() { return true; }
        public String getConnectionCacheType() { return "FOO"; }
        public void setConnectionCache(InboundConnectionCache connectionCache){}
        public InboundConnectionCache getConnectionCache() { return null; }
        public boolean shouldRegisterAcceptEvent() { return true; }
        public void setUseSelectThreadForConnections(boolean x) { }
        public boolean shouldUseSelectThreadForConnections() { return true; }
        public void setUseWorkerThreadForConnections(boolean x) { }
        public boolean shouldUseWorkerThreadForConnections() { return true; }
        public void accept() { }
        public void close() { }
        public EventHandler getEventHandler() { return null; }
        public MessageMediator createMessageMediator(
            Broker xbroker, Connection xconnection) { return null; }
        public MessageMediator finishCreatingMessageMediator(
            Broker xbroker, Connection xconnection,
            MessageMediator messageMediator) { return null; }
        public InputObject createInputObject(
            Broker broker, MessageMediator messageMediator) { return null; }
        public OutputObject createOutputObject(
            Broker broker, MessageMediator messageMediator) { return null; }
    }
    public static final class TestAcceptor2
        implements Acceptor
    {
        public boolean equals( Object other )
        {
            return other instanceof TestAcceptor2 ;
        }
        public boolean initialize() { return true; }
        public boolean initialized() { return true; }
        public String getConnectionCacheType() { return "FOO"; }
        public void setConnectionCache(InboundConnectionCache connectionCache){}
        public InboundConnectionCache getConnectionCache() { return null; }
        public boolean shouldRegisterAcceptEvent() { return true; }
        public void setUseSelectThreadForConnections(boolean x) { }
        public boolean shouldUseSelectThreadForConnections() { return true; }
        public void setUseWorkerThreadForConnections(boolean x) { }
        public boolean shouldUseWorkerThreadForConnections() { return true; }
        public void accept() { }
        public void close() { }
        public EventHandler getEventHandler() { return null; }
        public MessageMediator createMessageMediator(
            Broker xbroker, Connection xconnection) { return null; }
        public MessageMediator finishCreatingMessageMediator(
            Broker xbroker, Connection xconnection,
            MessageMediator messageMediator) { return null; }
        public InputObject createInputObject(
            Broker broker, MessageMediator messageMediator) { return null; }
        public OutputObject createOutputObject(
            Broker broker, MessageMediator messageMediator) { return null; }
    }
    private Operation makeAcceptorInstantiationOperation() {
        Operation clsop = OperationFactory.classAction() ;
        Operation indexOp = OperationFactory.suffixAction() ;
        Operation op1 = OperationFactory.compose( indexOp, clsop ) ;
        Operation mop = OperationFactory.maskErrorAction( op1 ) ;
        Operation mkinst = new Operation() {
            public Object operate( Object value )
            {
                final Class initClass = (Class)value ;
                if (initClass == null)
                    return null ;
                if( Acceptor.class.isAssignableFrom( initClass ) ) {
                    Acceptor acceptor = null ;
                    try {
                        acceptor = (Acceptor)AccessController.doPrivileged(
                            new PrivilegedExceptionAction() {
                                public Object run()
                                    throws InstantiationException, IllegalAccessException
                                {
                                    return initClass.newInstance() ;
                                }
                            }
                        ) ;
                    } catch (PrivilegedActionException exc) {
                        throw wrapper.acceptorInstantiationFailure( exc.getException(),
                            initClass.getName() ) ;
                    } catch (Exception exc) {
                        throw wrapper.acceptorInstantiationFailure( exc, initClass.getName() ) ;
                    }
                    return acceptor ;
                } else {
                    throw wrapper.acceptorInstantiationTypeFailure( initClass.getName() ) ;
                }
            }
        } ;
        Operation result = OperationFactory.compose( mop, mkinst ) ;
        return result ;
    }
    private Operation makeInitRefOperation() {
        return new Operation() {
            public Object operate( Object value )
            {
                String[] values = (String[])value ;
                if (values.length != 2)
                    throw wrapper.orbInitialreferenceSyntax() ;
                return values[0] + "=" + values[1] ;
            }
        } ;
    }
}

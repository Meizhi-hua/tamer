public abstract class OperationFactory {
    private OperationFactory() {}
    private static String getString( Object obj )
    {
        if (obj instanceof String)
            return (String)obj ;
        else
            throw new Error( "String expected" ) ;
    }
    private static Object[] getObjectArray( Object obj )
    {
        if (obj instanceof Object[])
            return (Object[])obj ;
        else
            throw new Error( "Object[] expected" ) ;
    }
    private static StringPair getStringPair( Object obj )
    {
        if (obj instanceof StringPair)
            return (StringPair)obj ;
        else
            throw new Error( "StringPair expected" ) ;
    }
    private static abstract class OperationBase implements Operation{
        public boolean equals( Object obj )
        {
            if (this==obj)
                return true ;
            if (!(obj instanceof OperationBase))
                return false ;
            OperationBase other = (OperationBase)obj ;
            return toString().equals( other.toString() ) ;
        }
        public int hashCode()
        {
            return toString().hashCode() ;
        }
    }
    private static class MaskErrorAction extends OperationBase
    {
        private Operation op ;
        public MaskErrorAction( Operation op )
        {
            this.op = op ;
        }
        public Object operate( Object arg )
        {
            try {
                return op.operate( arg ) ;
            } catch (java.lang.Exception exc) {
                return null ;
            }
        }
        public String toString()
        {
            return "maskErrorAction(" + op + ")" ;
        }
    }
    public static Operation maskErrorAction( Operation op )
    {
        return new MaskErrorAction( op ) ;
    }
    private static class IndexAction extends OperationBase
    {
        private int index ;
        public IndexAction( int index )
        {
            this.index = index ;
        }
        public Object operate( Object value )
        {
            return getObjectArray( value )[ index ] ;
        }
        public String toString()
        {
            return "indexAction(" + index + ")" ;
        }
    }
    public static Operation indexAction( int index )
    {
        return new IndexAction( index ) ;
    }
    private static class SuffixAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return getStringPair( value ).getFirst() ;
        }
        public String toString() { return "suffixAction" ; }
    }
    private static Operation suffixActionImpl = new SuffixAction() ;
    private static class ValueAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return getStringPair( value ).getSecond() ;
        }
        public String toString() { return "valueAction" ; }
    }
    private static Operation valueActionImpl = new ValueAction() ;
    private static class IdentityAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return value ;
        }
        public String toString() { return "identityAction" ; }
    }
    private static Operation identityActionImpl = new IdentityAction() ;
    private static class BooleanAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return new Boolean( getString( value ) ) ;
        }
        public String toString() { return "booleanAction" ; }
    }
    private static Operation booleanActionImpl = new BooleanAction() ;
    private static class IntegerAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return new Integer( getString( value ) ) ;
        }
        public String toString() { return "integerAction" ; }
    }
    private static Operation integerActionImpl = new IntegerAction() ;
    private static class StringAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return value ;
        }
        public String toString() { return "stringAction" ; }
    }
    private static Operation stringActionImpl = new StringAction() ;
    private static class ClassAction extends OperationBase
    {
        public Object operate( Object value )
        {
            String className = getString( value ) ;
            try {
                Class result = ORBClassLoader.loadClass( className ) ;
                return result ;
            } catch (Exception exc) {
                ORBUtilSystemException wrapper = ORBUtilSystemException.get(
                    CORBALogDomains.ORB_LIFECYCLE ) ;
                throw wrapper.couldNotLoadClass( exc, className ) ;
            }
        }
        public String toString() { return "classAction" ; }
    }
    private static Operation classActionImpl = new ClassAction() ;
    private static class SetFlagAction extends OperationBase
    {
        public Object operate( Object value )
        {
            return Boolean.TRUE ;
        }
        public String toString() { return "setFlagAction" ; }
    }
    private static Operation setFlagActionImpl = new SetFlagAction() ;
    private static class URLAction extends OperationBase
    {
        public Object operate( Object value )
        {
            String val = (String)value ;
            try {
                return new URL( val ) ;
            } catch (MalformedURLException exc) {
                ORBUtilSystemException wrapper = ORBUtilSystemException.get(
                    CORBALogDomains.ORB_LIFECYCLE ) ;
                throw wrapper.badUrl( exc, val ) ;
            }
        }
        public String toString() { return "URLAction" ; }
    }
    private static Operation URLActionImpl = new URLAction() ;
    public static Operation identityAction()
    {
        return identityActionImpl ;
    }
    public static Operation suffixAction()
    {
        return suffixActionImpl ;
    }
    public static Operation valueAction()
    {
        return valueActionImpl ;
    }
    public static Operation booleanAction()
    {
        return booleanActionImpl ;
    }
    public static Operation integerAction()
    {
        return integerActionImpl ;
    }
    public static Operation stringAction()
    {
        return stringActionImpl ;
    }
    public static Operation classAction()
    {
        return classActionImpl ;
    }
    public static Operation setFlagAction()
    {
        return setFlagActionImpl ;
    }
    public static Operation URLAction()
    {
        return URLActionImpl ;
    }
    private static class IntegerRangeAction extends OperationBase
    {
        private int min ;
        private int max ;
        IntegerRangeAction( int min, int max )
        {
            this.min = min ;
            this.max = max ;
        }
        public Object operate( Object value )
        {
            int result = Integer.parseInt( getString( value ) ) ;
            if ((result >= min) && (result <= max))
                return new Integer( result ) ;
            else
                throw new IllegalArgumentException(
                    "Property value " + result + " is not in the range " +
                    min + " to " + max ) ;
        }
        public String toString() {
            return "integerRangeAction(" + min + "," + max + ")" ;
        }
    }
    public static Operation integerRangeAction( int min, int max )
    {
        return new IntegerRangeAction( min, max ) ;
    }
    private static class ListAction extends OperationBase {
        private String sep ;
        private Operation act ;
        ListAction( String sep, Operation act )
        {
            this.sep = sep ;
            this.act = act ;
        }
        public Object operate( Object value )
        {
            StringTokenizer st = new StringTokenizer( getString( value ),
                sep ) ;
            int length = st.countTokens() ;
            Object result = null ;
            int ctr = 0 ;
            while (st.hasMoreTokens()) {
                String next = st.nextToken() ;
                Object val = act.operate( next ) ;
                if (result == null)
                    result = Array.newInstance( val.getClass(), length ) ;
                Array.set( result, ctr++, val ) ;
            }
            return result ;
        }
        public String toString() {
            return "listAction(separator=\"" + sep +
                "\",action=" + act + ")" ;
        }
    }
    public static Operation listAction( String sep, Operation act )
    {
        return new ListAction( sep, act ) ;
    }
    private static class SequenceAction extends OperationBase
    {
        private String sep ;
        private Operation[] actions ;
        SequenceAction( String sep, Operation[] actions )
        {
            this.sep = sep ;
            this.actions = actions ;
        }
        public Object operate( Object value )
        {
            StringTokenizer st = new StringTokenizer( getString( value ),
                sep ) ;
            int numTokens = st.countTokens() ;
            if (numTokens != actions.length)
                throw new Error(
                    "Number of tokens and number of actions do not match" ) ;
            int ctr = 0 ;
            Object[] result = new Object[ numTokens ] ;
            while (st.hasMoreTokens()) {
                Operation act = actions[ctr] ;
                String next = st.nextToken() ;
                result[ctr++] = act.operate( next ) ;
            }
            return result ;
        }
        public String toString() {
            return "sequenceAction(separator=\"" + sep +
                "\",actions=" +
                Arrays.toString(actions) + ")" ;
        }
    }
    public static Operation sequenceAction( String sep,
        Operation[] actions )
    {
        return new SequenceAction( sep, actions ) ;
    }
    private static class ComposeAction extends OperationBase
    {
        private Operation op1 ;
        private Operation op2 ;
        ComposeAction( Operation op1, Operation op2 )
        {
            this.op1 = op1 ;
            this.op2 = op2 ;
        }
        public Object operate( Object value )
        {
            return op2.operate( op1.operate( value ) ) ;
        }
        public String toString() {
            return "composition(" + op1 + "," + op2 + ")" ;
        }
    }
    public static Operation compose( Operation op1, Operation op2 )
    {
        return new ComposeAction( op1, op2 ) ;
    }
    private static class MapAction extends OperationBase
    {
        Operation op ;
        MapAction( Operation op )
        {
            this.op = op ;
        }
        public Object operate( Object value )
        {
            Object[] values = (Object[])value ;
            Object[] result = new Object[ values.length ] ;
            for (int ctr=0; ctr<values.length; ctr++ )
                result[ctr] = op.operate( values[ctr] ) ;
            return result ;
        }
        public String toString() {
            return "mapAction(" + op + ")" ;
        }
    }
    public static Operation mapAction( Operation op )
    {
        return new MapAction( op ) ;
    }
    private static class MapSequenceAction extends OperationBase
    {
        private Operation[] op ;
        public MapSequenceAction( Operation[] op )
        {
            this.op = op ;
        }
        public Object operate( Object value )
        {
            Object[] values = (Object[])value ;
            Object[] result = new Object[ values.length ] ;
            for (int ctr=0; ctr<values.length; ctr++ )
                result[ctr] = op[ctr].operate( values[ctr] ) ;
            return result ;
        }
        public String toString() {
            return "mapSequenceAction(" +
                Arrays.toString(op) + ")" ;
        }
    }
    public static Operation mapSequenceAction( Operation[] op )
    {
        return new MapSequenceAction( op ) ;
    }
    private static class ConvertIntegerToShort extends OperationBase
    {
        public Object operate( Object value )
        {
            Integer val = (Integer)value ;
            return new Short( val.shortValue() ) ;
        }
        public String toString() {
            return "ConvertIntegerToShort" ;
        }
    }
    private static Operation convertIntegerToShortImpl = new ConvertIntegerToShort() ;
    public static Operation convertIntegerToShort()
    {
        return convertIntegerToShortImpl ;
    }
}

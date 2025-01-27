public class NamespaceSupport2
    extends org.xml.sax.helpers.NamespaceSupport
{
    private Context2 currentContext; 
    public final static String XMLNS =
        "http:
    public NamespaceSupport2 ()
    {
        reset();
    }
    public void reset ()
    {
        currentContext = new Context2(null);
        currentContext.declarePrefix("xml", XMLNS);
    }
    public void pushContext ()
    {
        Context2 parentContext=currentContext;
        currentContext = parentContext.getChild();
        if (currentContext == null){
                currentContext = new Context2(parentContext);
            }
        else{
            currentContext.setParent(parentContext);
        }
    }
    public void popContext ()
    {
        Context2 parentContext=currentContext.getParent();
        if(parentContext==null)
            throw new EmptyStackException();
        else
            currentContext = parentContext;
    }
    public boolean declarePrefix (String prefix, String uri)
    {
        if (prefix.equals("xml") || prefix.equals("xmlns")) {
            return false;
        } else {
            currentContext.declarePrefix(prefix, uri);
            return true;
        }
    }
    public String [] processName (String qName, String[] parts,
                                  boolean isAttribute)
    {
        String[] name=currentContext.processName(qName, isAttribute);
        if(name==null)
            return null;
        System.arraycopy(name,0,parts,0,3);
        return parts;
    }
    public String getURI (String prefix)
    {
        return currentContext.getURI(prefix);
    }
    public Enumeration getPrefixes ()
    {
        return currentContext.getPrefixes();
    }
    public String getPrefix (String uri)
    {
        return currentContext.getPrefix(uri);
    }
    public Enumeration getPrefixes (String uri)
    {
        return new PrefixForUriEnumerator(this,uri,getPrefixes());       
    }
    public Enumeration getDeclaredPrefixes ()
    {
        return currentContext.getDeclaredPrefixes();
    }
}
class PrefixForUriEnumerator implements Enumeration
{
    private Enumeration allPrefixes;
    private String uri;
    private String lookahead=null;
    private NamespaceSupport2 nsup;
    PrefixForUriEnumerator(NamespaceSupport2 nsup,String uri, Enumeration allPrefixes)
    {
	this.nsup=nsup;
        this.uri=uri;
        this.allPrefixes=allPrefixes;
    }
    public boolean hasMoreElements()
    {
        if(lookahead!=null)
            return true;
        while(allPrefixes.hasMoreElements())
            {
                String prefix=(String)allPrefixes.nextElement();
                if(uri.equals(nsup.getURI(prefix)))
                    {
                        lookahead=prefix;
                        return true;
                    }
            }
        return false;
    }
    public Object nextElement()
    {
        if(hasMoreElements())
            {
                String tmp=lookahead;
                lookahead=null;
                return tmp;
            }
        else
            throw new java.util.NoSuchElementException();
    }
}
final class Context2 {
    private final static Enumeration EMPTY_ENUMERATION =
        new Vector().elements();
    Hashtable prefixTable;
    Hashtable uriTable;
    Hashtable elementNameTable;
    Hashtable attributeNameTable;
    String defaultNS = null;
    private Vector declarations = null;
    private boolean tablesDirty = false;
    private Context2 parent = null;
    private Context2 child = null;
    Context2 (Context2 parent)
    {
        if(parent==null)
            {
                prefixTable = new Hashtable();
                uriTable = new Hashtable();
                elementNameTable=null; 
                attributeNameTable=null; 
            }
        else
            setParent(parent);
    }
    Context2 getChild()
    {
        return child;
    }
    Context2 getParent()
    {
        return parent;
    }
    void setParent (Context2 parent)
    {
        this.parent = parent;
        parent.child = this;        
        declarations = null;
        prefixTable = parent.prefixTable;
        uriTable = parent.uriTable;
        elementNameTable = parent.elementNameTable;
        attributeNameTable = parent.attributeNameTable;
        defaultNS = parent.defaultNS;
        tablesDirty = false;
    }
    void declarePrefix (String prefix, String uri)
    {
        if (!tablesDirty) {
            copyTables();
        }
        if (declarations == null) {
            declarations = new Vector();
        }
        prefix = prefix.intern();
        uri = uri.intern();
        if ("".equals(prefix)) {
            if ("".equals(uri)) {
                defaultNS = null;
            } else {
                defaultNS = uri;
            }
        } else {
            prefixTable.put(prefix, uri);
            uriTable.put(uri, prefix); 
        }
        declarations.addElement(prefix);
    }
    String [] processName (String qName, boolean isAttribute)
    {
        String name[];
        Hashtable table;
        if (isAttribute) {
            if(elementNameTable==null)
                elementNameTable=new Hashtable();
            table = elementNameTable;
        } else {
            if(attributeNameTable==null)
                attributeNameTable=new Hashtable();
            table = attributeNameTable;
        }
        name = (String[])table.get(qName);
        if (name != null) {
            return name;
        }
        name = new String[3];
        int index = qName.indexOf(':');
        if (index == -1) {
            if (isAttribute || defaultNS == null) {
                name[0] = "";
            } else {
                name[0] = defaultNS;
            }
            name[1] = qName.intern();
            name[2] = name[1];
        }
        else {
            String prefix = qName.substring(0, index);
            String local = qName.substring(index+1);
            String uri;
            if ("".equals(prefix)) {
                uri = defaultNS;
            } else {
                uri = (String)prefixTable.get(prefix);
            }
            if (uri == null) {
                return null;
            }
            name[0] = uri;
            name[1] = local.intern();
            name[2] = qName.intern();
        }
        table.put(name[2], name);
        tablesDirty = true;
        return name;
    }
    String getURI (String prefix)
    {
        if ("".equals(prefix)) {
            return defaultNS;
        } else if (prefixTable == null) {
            return null;
        } else {
            return (String)prefixTable.get(prefix);
        }
    }
    String getPrefix (String uri)
    {
        if (uriTable == null) {
            return null;
        } else {
            return (String)uriTable.get(uri);
        }
    }
    Enumeration getDeclaredPrefixes ()
    {
        if (declarations == null) {
            return EMPTY_ENUMERATION;
        } else {
            return declarations.elements();
        }
    }
    Enumeration getPrefixes ()
    {
        if (prefixTable == null) {
            return EMPTY_ENUMERATION;
        } else {
            return prefixTable.keys();
        }
    }
    private void copyTables ()
    {
        prefixTable = (Hashtable)prefixTable.clone();
        uriTable = (Hashtable)uriTable.clone();
        if(elementNameTable!=null)
            elementNameTable=new Hashtable(); 
        if(attributeNameTable!=null)
            attributeNameTable=new Hashtable(); 
        tablesDirty = true;
    }
}

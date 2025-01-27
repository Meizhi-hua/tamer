public class XMLReaderManager {
    private static final String NAMESPACES_FEATURE =
                             "http:
    private static final String NAMESPACE_PREFIXES_FEATURE =
                             "http:
    private static final XMLReaderManager m_singletonManager =
                                                     new XMLReaderManager();
    private static SAXParserFactory m_parserFactory;
    private ThreadLocal m_readers;
    private Hashtable m_inUse;
    private XMLReaderManager() {
    }
    public static XMLReaderManager getInstance() {
        return m_singletonManager;
    }
    public synchronized XMLReader getXMLReader() throws SAXException {
        XMLReader reader;
        boolean readerInUse;
        if (m_readers == null) {
            m_readers = new ThreadLocal();
        }
        if (m_inUse == null) {
            m_inUse = new Hashtable();
        }
        reader = (XMLReader) m_readers.get();
        boolean threadHasReader = (reader != null);
        if (!threadHasReader || m_inUse.get(reader) == Boolean.TRUE) {
            try {
                try {
                    reader = XMLReaderFactory.createXMLReader();
                } catch (Exception e) {
                   try {
                        if (m_parserFactory == null) {
                            m_parserFactory = SAXParserFactory.newInstance();
                            m_parserFactory.setNamespaceAware(true);
                        }
                        reader = m_parserFactory.newSAXParser().getXMLReader();
                   } catch (ParserConfigurationException pce) {
                       throw pce;   
                   }
                }
                try {
                    reader.setFeature(NAMESPACES_FEATURE, true);
                    reader.setFeature(NAMESPACE_PREFIXES_FEATURE, false);
                } catch (SAXException se) {
                }
            } catch (ParserConfigurationException ex) {
                throw new SAXException(ex);
            } catch (FactoryConfigurationError ex1) {
                throw new SAXException(ex1.toString());
            } catch (NoSuchMethodError ex2) {
            } catch (AbstractMethodError ame) {
            }
            if (!threadHasReader) {
                m_readers.set(reader);
                m_inUse.put(reader, Boolean.TRUE);
            }
        } else {
            m_inUse.put(reader, Boolean.TRUE);
        }
        return reader;
    }
    public synchronized void releaseXMLReader(XMLReader reader) {
        if (m_readers.get() == reader && reader != null) {
            m_inUse.remove(reader);
        }
    }
}

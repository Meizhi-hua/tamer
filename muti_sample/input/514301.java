public class XercesHTMLDocumentBuilderFactory
    extends DOMTestDocumentBuilderFactory {
  private SAXParserFactory factory;
  private Constructor htmlBuilderConstructor;
  private Method getHTMLDocumentMethod;
  private DOMImplementation domImpl;
  private static final Class[] NO_CLASSES = new Class[0];
  private static final Object[] NO_OBJECTS = new Object[0];
  public XercesHTMLDocumentBuilderFactory(DocumentBuilderSetting[] settings) throws
      DOMTestIncompatibleException {
    super(settings);
    try {
      ClassLoader classLoader = ClassLoader.getSystemClassLoader();
      Class htmlBuilderClass =
          classLoader.loadClass("org.apache.html.dom.HTMLBuilder");
      htmlBuilderConstructor =
          htmlBuilderClass.getConstructor(NO_CLASSES);
      getHTMLDocumentMethod =
          htmlBuilderClass.getMethod("getHTMLDocument", NO_CLASSES);
      Class htmlDOMImpl =
          classLoader.loadClass(
          "org.apache.html.dom.HTMLDOMImplementationImpl");
      Method method =
          htmlDOMImpl.getMethod("getHTMLDOMImplementation", NO_CLASSES);
      domImpl = (DOMImplementation) method.invoke(null, NO_OBJECTS);
      Class saxFactoryClass =
          classLoader.loadClass(
          "org.apache.xerces.jaxp.SAXParserFactoryImpl");
      factory = (SAXParserFactory) saxFactoryClass.newInstance();
    }
    catch (InvocationTargetException ex) {
      throw new DOMTestIncompatibleException(
          ex.getTargetException(),
          null);
    }
    catch (Exception ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
    if (settings != null) {
      for (int i = 0; i < settings.length; i++) {
      }
    }
    try {
      factory.newSAXParser();
    }
    catch (ParserConfigurationException ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
    catch (SAXException ex) {
      throw new DOMTestIncompatibleException(ex, null);
    }
  }
  public DOMTestDocumentBuilderFactory newInstance(DocumentBuilderSetting[]
      newSettings) throws DOMTestIncompatibleException {
    if (newSettings == null) {
      return this;
    }
    DocumentBuilderSetting[] mergedSettings = mergeSettings(newSettings);
    return new XercesHTMLDocumentBuilderFactory(mergedSettings);
  }
  private class HTMLHandler
      extends DefaultHandler
      implements AttributeList {
    private final DocumentHandler htmlBuilder;
    private final Method getHTMLDocumentMethod;
    private Attributes currentAttributes;
    public HTMLHandler(
        Constructor htmlBuilderConstructor,
        Method getHTMLDocumentMethod) throws Exception {
      htmlBuilder =
          (DocumentHandler) htmlBuilderConstructor.newInstance(
          new Object[0]);
      this.getHTMLDocumentMethod = getHTMLDocumentMethod;
    }
    public void startDocument() throws SAXException {
      htmlBuilder.startDocument();
    }
    public void endDocument() throws SAXException {
      htmlBuilder.endDocument();
    }
    public void startElement(
        String uri,
        String localName,
        String qName,
        Attributes attributes) throws SAXException {
      currentAttributes = attributes;
      htmlBuilder.startElement(qName, this);
    }
    public void endElement(String uri, String localName, String qName) throws
        SAXException {
      htmlBuilder.endElement(qName);
    }
    public void characters(char ch[], int start, int length) throws
        SAXException {
      htmlBuilder.characters(ch, start, length);
    }
    public void ignorableWhitespace(char ch[], int start, int length) throws
        SAXException {
      htmlBuilder.ignorableWhitespace(ch, start, length);
    }
    public void processingInstruction(String target, String data) throws
        SAXException {
      htmlBuilder.processingInstruction(target, data);
    }
    public Document getHTMLDocument() throws Exception {
      return (Document) getHTMLDocumentMethod.invoke(
          htmlBuilder,
          NO_OBJECTS);
    }
    public int getLength() {
      return currentAttributes.getLength();
    }
    public String getName(int i) {
      return currentAttributes.getQName(i);
    }
    public String getType(int i) {
      return currentAttributes.getType(i);
    }
    public String getValue(int i) {
      return currentAttributes.getValue(i);
    }
    public String getType(String name) {
      return currentAttributes.getType(name);
    }
    public String getValue(String name) {
      return currentAttributes.getValue(name);
    }
  }
  public Document load(java.net.URL url) throws DOMTestLoadException {
    Document doc = null;
    try {
      SAXParser parser = factory.newSAXParser();
      HTMLHandler handler =
          new HTMLHandler(htmlBuilderConstructor, getHTMLDocumentMethod);
      parser.parse(url.toString(), handler);
      doc = handler.getHTMLDocument();
    }
    catch (Exception ex) {
      throw new DOMTestLoadException(ex);
    }
    return doc;
  }
  public DOMImplementation getDOMImplementation() {
    return domImpl;
  }
  public boolean hasFeature(String feature, String version) {
    return domImpl.hasFeature(feature, version);
  }
  public boolean isCoalescing() {
    return false;
  }
  public boolean isExpandEntityReferences() {
    return false;
  }
  public boolean isIgnoringElementContentWhitespace() {
    return false;
  }
  public boolean isNamespaceAware() {
    return factory.isNamespaceAware();
  }
  public boolean isValidating() {
    return factory.isValidating();
  }
  public static DocumentBuilderSetting[] getConfiguration1() {
    return new DocumentBuilderSetting[] {
        DocumentBuilderSetting.notCoalescing,
        DocumentBuilderSetting.notExpandEntityReferences,
        DocumentBuilderSetting.notIgnoringElementContentWhitespace,
        DocumentBuilderSetting.notNamespaceAware,
        DocumentBuilderSetting.notValidating};
  }
  public static DocumentBuilderSetting[] getConfiguration2() {
    return new DocumentBuilderSetting[] {
        DocumentBuilderSetting.notCoalescing,
        DocumentBuilderSetting.expandEntityReferences,
        DocumentBuilderSetting.ignoringElementContentWhitespace,
        DocumentBuilderSetting.namespaceAware,
        DocumentBuilderSetting.validating};
  }
}

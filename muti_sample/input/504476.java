public final class isSupported09 extends DOMTestCase {
   public isSupported09(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node rootNode;
      boolean state;
      doc = (Document) load("staff", false);
      rootNode = doc.getDocumentElement();
      state = rootNode.isSupported("XML", "1.0");
      assertTrue("throw_True", state);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(isSupported09.class, args);
   }
}
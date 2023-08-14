public final class hc_notationssetnameditem1 extends DOMTestCase {
   public hc_notationssetnameditem1(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
      if (factory.hasFeature("XML", null) != true) {
         throw org.w3c.domts.DOMTestIncompatibleException.incompatibleFeature("XML", null);
      }
    String contentType = getContentType();
    preload(contentType, "hc_staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      NamedNodeMap notations;
      DocumentType docType;
      Node retval;
      Element elem;
      doc = (Document) load("hc_staff", true);
      docType = doc.getDoctype();
      if (
    !(("text/html".equals(getContentType())))
) {
          assertNotNull("docTypeNotNull", docType);
      notations = docType.getNotations();
      assertNotNull("notationsNotNull", notations);
      elem = doc.createElement("br");
      try {
      retval = notations.setNamedItem(elem);
      fail("throw_HIER_OR_NO_MOD_ERR");
      } catch (DOMException ex) {
           switch (ex.code) {
      case 3 : 
       break;
      case 7 : 
       break;
          default:
          throw ex;
          }
      } 
}
    }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(hc_notationssetnameditem1.class, args);
   }
}
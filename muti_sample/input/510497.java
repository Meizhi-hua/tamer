public final class nodereplacechildnomodificationallowederrEE extends DOMTestCase {
   public nodereplacechildnomodificationallowederrEE(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      Node entRef;
      Node entText;
      Node createdNode;
      Node replacedChild;
      doc = (Document) load("staff", true);
      entRef = doc.createEntityReference("ent4");
      assertNotNull("createdEntRefNotNull", entRef);
      entText = entRef.getFirstChild();
      createdNode = doc.createElement("newChild");
      {
         boolean success = false;
         try {
            replacedChild = entRef.replaceChild(createdNode, entText);
          } catch (DOMException ex) {
            success = (ex.code == DOMException.NO_MODIFICATION_ALLOWED_ERR);
         }
         assertTrue("throw_NO_MODIFICATION_ALLOWED_ERR", success);
      }
}
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodereplacechildnomodificationallowederrEE.class, args);
   }
}
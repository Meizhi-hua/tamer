public final class documentcreatedocumentfragment extends DOMTestCase {
   public documentcreatedocumentfragment(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", true);
    }
   public void runTest() throws Throwable {
      Document doc;
      DocumentFragment newDocFragment;
      NodeList children;
      int length;
      String newDocFragmentName;
      int newDocFragmentType;
      String newDocFragmentValue;
      doc = (Document) load("staff", true);
      newDocFragment = doc.createDocumentFragment();
      children = newDocFragment.getChildNodes();
      length = (int) children.getLength();
      assertEquals("length", 0, length);
      newDocFragmentName = newDocFragment.getNodeName();
      assertEquals("name", "#document-fragment", newDocFragmentName);
      newDocFragmentType = (int) newDocFragment.getNodeType();
      assertEquals("type", 11, newDocFragmentType);
      newDocFragmentValue = newDocFragment.getNodeValue();
      assertNull("value", newDocFragmentValue);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(documentcreatedocumentfragment.class, args);
   }
}
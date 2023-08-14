public final class nodegetfirstchildnull extends DOMTestCase {
   public nodegetfirstchildnull(final DOMTestDocumentBuilderFactory factory)  throws org.w3c.domts.DOMTestIncompatibleException {
      super(factory);
    String contentType = getContentType();
    preload(contentType, "staff", false);
    }
   public void runTest() throws Throwable {
      Document doc;
      NodeList elementList;
      Node employeeNode;
      NodeList employeeList;
      Node secondChildNode;
      Node textNode;
      Node noChildNode;
      doc = (Document) load("staff", false);
      elementList = doc.getElementsByTagName("employee");
      employeeNode = elementList.item(0);
      employeeList = employeeNode.getChildNodes();
      secondChildNode = employeeList.item(1);
      textNode = secondChildNode.getFirstChild();
      noChildNode = textNode.getFirstChild();
      assertNull("nodeGetFirstChildNullAssert1", noChildNode);
      }
   public String getTargetURI() {
      return "http:
   }
   public static void main(final String[] args) {
        DOMTestCase.doMain(nodegetfirstchildnull.class, args);
   }
}
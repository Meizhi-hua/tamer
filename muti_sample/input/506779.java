public class TestDefaultParserAltConfig extends TestSuite {
  public static TestSuite suite() throws Exception
  {
    Class testClass = ClassLoader.getSystemClassLoader().loadClass("org.w3c.domts.level1.core.alltests");
    Constructor testConstructor = testClass.getConstructor(new Class[] { DOMTestDocumentBuilderFactory.class });
    DOMTestDocumentBuilderFactory factory =
        new JAXPDOMTestDocumentBuilderFactory(null,
          JAXPDOMTestDocumentBuilderFactory.getConfiguration2());
    Object test = testConstructor.newInstance(new Object[] { factory });
    return new JUnitTestSuiteAdapter((DOMTestSuite) test);
  }
}

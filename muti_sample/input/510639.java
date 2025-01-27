public class XsltXPathConformanceTestSuite {
    private static final String defaultCatalogFile
            = "/home/dalvik-prebuild/OASIS/XSLT-Conformance-TC/TESTS/catalog.xml";
    private static final Comparator<Attr> orderByName = new Comparator<Attr>() {
        public int compare(Attr a, Attr b) {
            int result = compareNullsFirst(a.getNamespaceURI(), b.getNamespaceURI());
            return result == 0 ? result
                    : compareNullsFirst(a.getName(), b.getName());
        }
        <T extends Comparable<T>> int compareNullsFirst(T a, T b) {
            return (a == b) ? 0
                    : (a == null) ? -1
                    : (b == null) ? 1
                    : a.compareTo(b);
        }
    };
    private final DocumentBuilder documentBuilder;
    private final TransformerFactory transformerFactory;
    private final XmlPullParserFactory xmlPullParserFactory;
    public XsltXPathConformanceTestSuite()
            throws ParserConfigurationException, XmlPullParserException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setCoalescing(true);
        documentBuilder = factory.newDocumentBuilder();
        transformerFactory = TransformerFactory.newInstance();
        xmlPullParserFactory = XmlPullParserFactory.newInstance();
    }
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: XsltXPathConformanceTestSuite <catalog-xml>");
            System.out.println();
            System.out.println("  catalog-xml: an XML file describing an OASIS test suite");
            System.out.println("               such as: " + defaultCatalogFile);
            return;
        }
        File catalogXml = new File(args[0]);
        TestRunner.run(suite(catalogXml));
    }
    public static Test suite() throws Exception {
        return suite(new File(defaultCatalogFile));
    }
    public static Test suite(File catalogXml) throws Exception {
        XsltXPathConformanceTestSuite suite = new XsltXPathConformanceTestSuite();
        Document document = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(catalogXml);
        Element testSuiteElement = document.getDocumentElement();
        TestSuite result = new TestSuite();
        for (Element testCatalog : elementsOf(testSuiteElement.getElementsByTagName("test-catalog"))) {
            Element majorPathElement = (Element) testCatalog.getElementsByTagName("major-path").item(0);
            String majorPath = majorPathElement.getTextContent();
            File base = new File(catalogXml.getParentFile(), majorPath);
            for (Element testCaseElement : elementsOf(testCatalog.getElementsByTagName("test-case"))) {
                result.addTest(suite.create(base, testCaseElement));
            }
        }
        return result;
    }
    private TestCase create(File base, Element testCaseElement) {
        Element filePathElement = (Element) testCaseElement.getElementsByTagName("file-path").item(0);
        Element purposeElement = (Element) testCaseElement.getElementsByTagName("purpose").item(0);
        Element specCitationElement = (Element) testCaseElement.getElementsByTagName("spec-citation").item(0);
        Element scenarioElement = (Element) testCaseElement.getElementsByTagName("scenario").item(0);
        String category = testCaseElement.getAttribute("category");
        String id = testCaseElement.getAttribute("id");
        String name = category + "." + id;
        String purpose = purposeElement != null ? purposeElement.getTextContent() : "";
        String spec = "place=" + specCitationElement.getAttribute("place")
                + " type" + specCitationElement.getAttribute("type")
                + " version=" + specCitationElement.getAttribute("version")
                + " spec=" + specCitationElement.getAttribute("spec");
        String operation = scenarioElement.getAttribute("operation");
        Element principalDataElement = null;
        Element principalStylesheetElement = null;
        Element principalElement = null;
        for (Element element : elementsOf(scenarioElement.getChildNodes())) {
            String role = element.getAttribute("role");
            if (role.equals("principal-data")) {
                principalDataElement = element;
            } else if (role.equals("principal-stylesheet")) {
                principalStylesheetElement = element;
            } else if (role.equals("principal")) {
                principalElement = element;
            } else if (!role.equals("supplemental-stylesheet")
                    && !role.equals("supplemental-data")) {
                return new MisspecifiedTest("Unexpected element at " + name);
            }
        }
        String testDirectory = filePathElement.getTextContent();
        File inBase = new File(base, testDirectory);
        File outBase = new File(new File(base, "REF_OUT"), testDirectory);
        if (principalDataElement == null || principalStylesheetElement == null) {
            return new MisspecifiedTest("Expected <scenario> to have "
                    + "principal=data and principal-stylesheet elements at " + name);
        }
        try {
            File principalData = findFile(inBase, principalDataElement.getTextContent());
            File principalStylesheet = findFile(inBase, principalStylesheetElement.getTextContent());
            final File principal;
            final String compareAs;
            if (!operation.equals("execution-error")) {
                if (principalElement == null) {
                    return new MisspecifiedTest("Expected <scenario> to have principal element at " + name);
                }
                principal = findFile(outBase, principalElement.getTextContent());
                compareAs = principalElement.getAttribute("compare");
            } else {
                principal = null;
                compareAs = null;
            }
            return new XsltTest(category, id, purpose, spec, principalData,
                    principalStylesheet, principal, operation, compareAs);
        } catch (FileNotFoundException e) {
            return new MisspecifiedTest(e.getMessage() + " at " + name);
        }
    }
    private File findFile(File directory, String name) throws FileNotFoundException {
        File file = new File(directory, name);
        if (file.exists()) {
            return file;
        }
        for (String child : directory.list()) {
            if (child.equalsIgnoreCase(name)) {
                return new File(directory, child);
            }
        }
        throw new FileNotFoundException("Missing file: " + file);
    }
    public class MisspecifiedTest extends TestCase {
        private final String message;
        MisspecifiedTest(String message) {
            super("test");
            this.message = message;
        }
        public void test() {
            fail(message);
        }
    }
    public class XsltTest extends TestCase {
        private final String category;
        private final String id;
        private final String purpose;
        private final String spec;
        private final File principalData;
        private final File principalStylesheet;
        private final File principal;
        private final String operation;
        private final String compareAs;
        XsltTest(String category, String id, String purpose, String spec,
                File principalData, File principalStylesheet, File principal,
                String operation, String compareAs) {
            super("test");
            this.category = category;
            this.id = id;
            this.purpose = purpose;
            this.spec = spec;
            this.principalData = principalData;
            this.principalStylesheet = principalStylesheet;
            this.principal = principal;
            this.operation = operation;
            this.compareAs = compareAs;
        }
        XsltTest(File principalData, File principalStylesheet, File principal) {
            this("standalone", "test", "", "",
                    principalData, principalStylesheet, principal, "standard", "XML");
        }
        public void test() throws Exception {
            if (purpose != null) {
                System.out.println("Purpose: " + purpose);
            }
            if (spec != null) {
                System.out.println("Spec: " + spec);
            }
            Result result;
            if ("XML".equals(compareAs)) {
                DOMResult domResult = new DOMResult();
                domResult.setNode(documentBuilder.newDocument().createElementNS("", "result"));
                result = domResult;
            } else {
                result = new StreamResult(new StringWriter());
            }
            ErrorRecorder errorRecorder = new ErrorRecorder();
            transformerFactory.setErrorListener(errorRecorder);
            Transformer transformer;
            try {
                Source xslt = new StreamSource(principalStylesheet);
                transformer = transformerFactory.newTransformer(xslt);
                if (errorRecorder.error == null) {
                    transformer.setErrorListener(errorRecorder);
                    transformer.transform(new StreamSource(principalData), result);
                }
            } catch (TransformerConfigurationException e) {
                errorRecorder.fatalError(e);
            }
            if (operation.equals("standard")) {
                if (errorRecorder.error != null) {
                    throw errorRecorder.error;
                }
            } else if (operation.equals("execution-error")) {
                if (errorRecorder.error != null) {
                    return;
                }
                fail("Expected " + operation + ", but transform completed normally." 
                        + " (Warning=" + errorRecorder.warning + ")");
            } else {
                throw new UnsupportedOperationException("Unexpected operation: " + operation);
            }
            if ("XML".equals(compareAs)) {
                assertNodesAreEquivalent(principal, ((DOMResult) result).getNode());
            } else {
                throw new UnsupportedOperationException("Cannot compare as " + compareAs);
            }
        }
        @Override public String getName() {
            return category + "." + id;
        }
    }
    private void assertNodesAreEquivalent(File expected, Node actual)
            throws ParserConfigurationException, IOException, SAXException,
            XmlPullParserException {
        Node expectedNode = fileToResultNode(expected);
        String expectedString = nodeToNormalizedString(expectedNode);
        String actualString = nodeToNormalizedString(actual);
        Assert.assertEquals("Expected XML to match file " + expected,
                expectedString, actualString);
    }
    private Node fileToResultNode(File file) throws IOException, SAXException {
        String rawContents = fileToString(file);
        String fragment = rawContents;
        if (fragment.startsWith("<?xml")) {
            int declarationEnd = fragment.indexOf("?>");
            fragment = fragment.substring(declarationEnd + 2);
        }
        try {
            fragment = "<result>" + fragment + "</result>";
            return documentBuilder.parse(new InputSource(new StringReader(fragment)))
                    .getDocumentElement();
        } catch (SAXParseException e) {
            Error error = new AssertionFailedError(
                    "Failed to parse XML: " + file + "\n" + rawContents);
            error.initCause(e);
            throw error;
        }
    }
    private String nodeToNormalizedString(Node node)
            throws XmlPullParserException, IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer xmlSerializer = xmlPullParserFactory.newSerializer();
        xmlSerializer.setFeature("http:
        xmlSerializer.setOutput(writer);
        emitNode(xmlSerializer, node);
        xmlSerializer.flush();
        return writer.toString();
    }
    private void emitNode(XmlSerializer serializer, Node node) throws IOException {
        if (node == null) {
            throw new UnsupportedOperationException("Cannot emit null nodes");
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element element = (Element) node;
            serializer.startTag(element.getNamespaceURI(), element.getLocalName());
            emitAttributes(serializer, element);
            emitChildren(serializer, element);
            serializer.endTag(element.getNamespaceURI(), element.getLocalName());
        } else if (node.getNodeType() == Node.TEXT_NODE
                || node.getNodeType() == Node.CDATA_SECTION_NODE) {
            String trimmed = node.getTextContent().trim();
            if (trimmed.length() > 0) {
                serializer.text(trimmed);
            }
        } else if (node.getNodeType() == Node.DOCUMENT_NODE) {
            Document document = (Document) node;
            serializer.startDocument("UTF-8", true);
            emitNode(serializer, document.getDocumentElement());
            serializer.endDocument();
        } else if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
            ProcessingInstruction processingInstruction = (ProcessingInstruction) node;
            String data = processingInstruction.getData();
            String target = processingInstruction.getTarget();
            serializer.processingInstruction(target + " " + data);
        } else if (node.getNodeType() == Node.COMMENT_NODE) {
        } else if (node.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
            EntityReference entityReference = (EntityReference) node;
            serializer.entityRef(entityReference.getNodeName());
        } else {
            throw new UnsupportedOperationException(
                    "Cannot emit " + node + " of type " + node.getNodeType());
        }
    }
    private void emitAttributes(XmlSerializer serializer, Node node)
            throws IOException {
        NamedNodeMap map = node.getAttributes();
        if (map == null) {
            return;
        }
        List<Attr> attributes = new ArrayList<Attr>();
        for (int i = 0; i < map.getLength(); i++) {
            attributes.add((Attr) map.item(i));
        }
        Collections.sort(attributes, orderByName);
        for (Attr attr : attributes) {
            if ("xmlns".equals(attr.getPrefix()) || "xmlns".equals(attr.getLocalName())) {
            } else {
                serializer.attribute(attr.getNamespaceURI(), attr.getLocalName(), attr.getValue());
            }
        }
    }
    private void emitChildren(XmlSerializer serializer, Node node)
            throws IOException {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            emitNode(serializer, childNodes.item(i));
        }
    }
    private static List<Element> elementsOf(NodeList nodeList) {
        List<Element> result = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element) {
                result.add((Element) node);
            }
        }
        return result;
    }
    private String fileToString(File file) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file), 1024);
        Reader reader;
        in.mark(3);
        int byte1 = in.read();
        int byte2 = in.read();
        if (byte1 == 0xFF && byte2 == 0xFE) {
            reader = new InputStreamReader(in, "UTF-16LE");
        } else if (byte1 == 0xFF && byte2 == 0xFF) {
            reader = new InputStreamReader(in, "UTF-16BE");
        } else {
            int byte3 = in.read();
            if (byte1 == 0xEF && byte2 == 0xBB && byte3 == 0xBF) {
                reader = new InputStreamReader(in, "UTF-8");
            } else {
                in.reset();
                reader = new InputStreamReader(in);
            }
        }
        StringWriter out = new StringWriter();
        char[] buffer = new char[1024];
        int count;
        while ((count = reader.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        return out.toString();
    }
    static class ErrorRecorder implements ErrorListener {
        Exception warning;
        Exception error;
        public void warning(TransformerException exception) {
            if (this.warning == null) {
                this.warning = exception;
            }
        }
        public void error(TransformerException exception) {
            if (this.error == null) {
                this.error = exception;
            }
        }
        public void fatalError(TransformerException exception) {
            if (this.error == null) {
                this.error = exception;
            }
        }
    }
}

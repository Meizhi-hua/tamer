@TestTargetClass(Xml.class)
public class XmlTest extends AndroidTestCase {
    private static final String STR_INVALIDATE_EN_CODING = "invalidateEnCoding";
    private static final String STR_N2 = "-2";
    private static final String STR_N1 = "-1";
    private static final String STR_LAYOUT_HEIGHT = "layout_height";
    private static final String STR_LAYOUT_WIDTH = "layout_width";
    private static final String STR_ISO_8859_1 = "ISO-8859-1";
    private static final String STR_US_ASCII = "US-ASCII";
    private static final String STR_UTF_16 = "UTF-16";
    private static final String STR_UTF_8 = "UTF-8";
    private static final String STR_END_DOCUMENT = "endDocument";
    private static final String STR_1_NUMBER = "1";
    private static final String STR_0_NUMBER = "0";
    private static final String STR_EMPTY = "";
    private static final String STR_END_ELEMENT = "endElement";
    private static final String STR_ABC = "abc";
    private static final String ATT_NAME = "name";
    private static final String TAG_SON = "Son";
    private static final String TAG_TEST = "Test";
    private static final String STR_START_ELEMENT = "startElement:";
    private static final String STR_START_DOCUMENT = "startDocument";
    private static final String STR_SET_DOCUMENT_LOCATOR = "setDocumentLocator:Locator[publicId: "
               + "null, systemId: null, line: 1, column: 0]";
    private static final String STR_START_PREFIX_MAPPING_TAG = "startPrefixMapping:";
    private static final String STR_SKIPPED_ENTITY_TAG = "skippedEntity:";
    private static final String STR_SET_DOCUMENT_LOCATOR_TAG = "setDocumentLocator:";
    private static final String STR_PROCESSING_INSTRUCTION_TAG = "processingInstruction:";
    private static final String STR_IGNORABLE_WHITESPACE_TAG = "ignorableWhitespace:";
    private static final String STR_END_PREFIX_MAPPING_TAG = "endPrefixMapping:";
    private static final String STR_LENGTH_TAG = "length:";
    private static final String STR_START_TAG = "start:";
    private static final String STR_CHARACTERS_TAG = "characters:";
    final String sourceStr = "<" + TAG_TEST + "><" + TAG_SON + " " + ATT_NAME + "=\"" + STR_ABC
            + "\"/></" + TAG_TEST + ">";
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor(s) of Xml.",
        method = "Xml",
        args = {}
    )
    public void testConstructor() {
        new Xml();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test parse(String xml, ContentHandler contentHandler).",
        method = "parse",
        args = {java.lang.String.class, org.xml.sax.ContentHandler.class}
    )
    public void testParseStringContentHandler() {
        final String xmlStr = "<Test><Son name=\"abc\"/></Test>";
        DefaultContentHandler dc = new DefaultContentHandler();
        try {
            Xml.parse(xmlStr, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        }
    }
    class DefaultContentHandler implements ContentHandler {
        public Vector<String> mVec = new Vector<String>();
        public void characters(char[] ch, int start, int length) throws SAXException {
            mVec.add(STR_CHARACTERS_TAG + new String(ch));
            mVec.add(STR_START_TAG + start);
            mVec.add(STR_LENGTH_TAG + length);
        }
        public void endDocument() throws SAXException {
            mVec.add(STR_END_DOCUMENT);
        }
        public void endElement(String uri, String localName, String name) throws SAXException {
            mVec.add(STR_END_ELEMENT);
            mVec.add(uri);
            mVec.add(localName);
            mVec.add(name);
        }
        public void endPrefixMapping(String prefix) throws SAXException {
            mVec.add(STR_END_PREFIX_MAPPING_TAG + prefix);
        }
        public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
            mVec.add(STR_IGNORABLE_WHITESPACE_TAG + new String(ch));
            mVec.add(STR_START_TAG + start);
            mVec.add(STR_LENGTH_TAG + length);
        }
        public void processingInstruction(String target, String data) throws SAXException {
            mVec.add(STR_PROCESSING_INSTRUCTION_TAG + target);
            mVec.add(data);
        }
        public void setDocumentLocator(Locator locator) {
            mVec.add(STR_SET_DOCUMENT_LOCATOR_TAG + locator);
        }
        public void skippedEntity(String name) throws SAXException {
            mVec.add(STR_SKIPPED_ENTITY_TAG + name);
        }
        public void startDocument() throws SAXException {
            mVec.add(STR_START_DOCUMENT);
        }
        public void startElement(String uri, String localName, String name, Attributes atts)
                throws SAXException {
            mVec.add(STR_START_ELEMENT + uri);
            mVec.add(localName);
            mVec.add(name);
            mVec.add(atts.getLength() + STR_EMPTY);
            for (int i = 0; i < atts.getLength(); i++) {
                mVec.add(atts.getLocalName(i));
                mVec.add(atts.getValue(i));
            }
        }
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            mVec.add(STR_START_PREFIX_MAPPING_TAG + prefix);
            mVec.add(uri);
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test parse(Reader in, ContentHandler contentHandler).",
        method = "parse",
        args = {java.io.Reader.class, org.xml.sax.ContentHandler.class}
    )
    public void testParseReaderContentHander() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        for (int i = 0; i < sourceStr.length(); i++) {
            try {
                dout.write(sourceStr.charAt(i));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        try {
            dout.flush();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(
                bout.toByteArray())));
        DefaultContentHandler dc = new DefaultContentHandler();
        try {
            Xml.parse(reader, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test parse(InputStream in, Encoding encoding, ContentHandler contentHandler).",
        method = "parse",
        args = {java.io.InputStream.class, android.util.Xml.Encoding.class, 
                org.xml.sax.ContentHandler.class}
    )
    public void testParseInputStreamEncodingContentHandler() {
        DefaultContentHandler dc = new DefaultContentHandler();
        try {
            Xml.parse(new ByteArrayInputStream(sourceStr.getBytes(STR_US_ASCII)),
                            Xml.Encoding.US_ASCII, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        dc = new DefaultContentHandler();
        try {
            Xml.parse(new ByteArrayInputStream(sourceStr.getBytes(STR_UTF_8)),
                            Xml.Encoding.UTF_8, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        dc = new DefaultContentHandler();
        try {
            Xml.parse(new ByteArrayInputStream(sourceStr.getBytes(STR_UTF_16)),
                            Xml.Encoding.UTF_16, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        dc = new DefaultContentHandler();
        try {
            Xml.parse(new ByteArrayInputStream(sourceStr.getBytes(STR_ISO_8859_1)),
                            Xml.Encoding.ISO_8859_1, dc);
            assertEquals(STR_SET_DOCUMENT_LOCATOR, dc.mVec.elementAt(0));
            assertEquals(STR_START_DOCUMENT, dc.mVec.elementAt(1));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(2));
            assertEquals(TAG_TEST, dc.mVec.elementAt(3));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(4));
            assertEquals(STR_0_NUMBER, dc.mVec.elementAt(5));
            assertEquals(STR_START_ELEMENT, dc.mVec.elementAt(6));
            assertEquals(TAG_SON, dc.mVec.elementAt(7));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(8));
            assertEquals(STR_1_NUMBER, dc.mVec.elementAt(9));
            assertEquals(ATT_NAME, dc.mVec.elementAt(10));
            assertEquals(STR_ABC, dc.mVec.elementAt(11));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(12));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(13));
            assertEquals(TAG_SON, dc.mVec.elementAt(14));
            assertEqualsOrEmpty(TAG_SON, dc.mVec.elementAt(15));
            assertEquals(STR_END_ELEMENT, dc.mVec.elementAt(16));
            assertEquals(STR_EMPTY, dc.mVec.elementAt(17));
            assertEquals(TAG_TEST, dc.mVec.elementAt(18));
            assertEqualsOrEmpty(TAG_TEST, dc.mVec.elementAt(19));
            assertEquals(STR_END_DOCUMENT, dc.mVec.elementAt(20));
        } catch (SAXException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test newSerializer() and newPullParser().",
            method = "newSerializer",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test newSerializer() and newPullParser().",
            method = "newPullParser",
            args = {}
        )
    })
    public void testNewSerializer() {
        XmlSerializer xs = Xml.newSerializer();
        assertNotNull(xs);
        XmlPullParser xp = Xml.newPullParser();
        assertNotNull(xp);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test findEncodingByName(String encodingName).",
        method = "findEncodingByName",
        args = {java.lang.String.class}
    )
    public void testFindEncodingByName() {
        try {
            assertEquals(Xml.Encoding.UTF_8, Xml.findEncodingByName(STR_UTF_8));
            assertEquals(Xml.Encoding.UTF_16, Xml.findEncodingByName(STR_UTF_16));
            assertEquals(Xml.Encoding.US_ASCII, Xml.findEncodingByName(STR_US_ASCII));
            assertEquals(Xml.Encoding.ISO_8859_1, Xml.findEncodingByName(STR_ISO_8859_1));
        } catch (UnsupportedEncodingException e) {
            fail(e.getMessage());
        }
        try {
            Xml.findEncodingByName(STR_INVALIDATE_EN_CODING);
            fail("should throw out exception");
        } catch (UnsupportedEncodingException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test asAttributeSet(XmlPullParser parser).",
        method = "asAttributeSet",
        args = {org.xmlpull.v1.XmlPullParser.class}
    )
    public void testAsAttributeSet() {
        XmlResourceParser xp = getContext().getResources().getLayout(
                com.android.cts.stub.R.layout.xml_test);
        int eventType = -1;
        try {
            eventType = xp.getEventType();
        } catch (XmlPullParserException e) {
            fail(e.getMessage());
        }
        while (eventType != XmlResourceParser.START_TAG
                && eventType != XmlResourceParser.END_DOCUMENT) {
            try {
                eventType = xp.next();
            } catch (XmlPullParserException e) {
                fail(e.getMessage());
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        if (eventType == XmlResourceParser.START_TAG) {
            AttributeSet set = Xml.asAttributeSet(xp);
            assertEquals(2, set.getAttributeCount());
            assertEquals(STR_LAYOUT_WIDTH, set.getAttributeName(0));
            assertEquals(STR_N1, set.getAttributeValue(0));
            assertEquals(STR_LAYOUT_HEIGHT, set.getAttributeName(1));
            assertEquals(STR_N2, set.getAttributeValue(1));
        } else {
            fail("XML parser didn't find the start element of the specified xml file.");
        }
    }
    private void assertEqualsOrEmpty(String expected, String actual) {
        if (actual.length() != 0) {
            assertEquals(expected, actual);
        }
    }
}

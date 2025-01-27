public final class ValueResourceParser extends DefaultHandler {
    private final static String NODE_RESOURCES = "resources";
    private final static String NODE_ITEM = "item";
    private final static String ATTR_NAME = "name";
    private final static String ATTR_TYPE = "type";
    private final static String ATTR_PARENT = "parent";
    private final static String RES_STYLE = "style";
    private final static String RES_ATTR = "attr";
    private final static String DEFAULT_NS_PREFIX = "android:";
    private final static int DEFAULT_NS_PREFIX_LEN = DEFAULT_NS_PREFIX.length();
    public interface IValueResourceRepository {
        void addResourceValue(String resType, ResourceValue value);
    }
    private boolean inResources = false;
    private int mDepth = 0;
    private StyleResourceValue mCurrentStyle = null;
    private ResourceValue mCurrentValue = null;
    private IValueResourceRepository mRepository;
    private final boolean mIsFramework;
    public ValueResourceParser(IValueResourceRepository repository, boolean isFramework) {
        mRepository = repository;
        mIsFramework = isFramework;
    }
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (mCurrentValue != null) {
            mCurrentValue.setValue(trimXmlWhitespaces(mCurrentValue.getValue()));
        }
        if (inResources && qName.equals(NODE_RESOURCES)) {
            inResources = false;
        } else if (mDepth == 2) {
            mCurrentValue = null;
            mCurrentStyle = null;
        } else if (mDepth == 3) {
            mCurrentValue = null;
        }
        mDepth--;
        super.endElement(uri, localName, qName);
    }
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        try {
            mDepth++;
            if (inResources == false && mDepth == 1) {
                if (qName.equals(NODE_RESOURCES)) {
                    inResources = true;
                }
            } else if (mDepth == 2 && inResources == true) {
                String type;
                if (NODE_ITEM.equals(qName)) {
                    type = attributes.getValue(ATTR_TYPE);
                } else {
                    type = qName;
                }
                if (type != null) {
                    if (RES_ATTR.equals(type) == false) {
                        String name = attributes.getValue(ATTR_NAME);
                        if (name != null) {
                            if (RES_STYLE.equals(type)) {
                                String parent = attributes.getValue(ATTR_PARENT);
                                mCurrentStyle = new StyleResourceValue(type, name, parent, mIsFramework);
                                mRepository.addResourceValue(type, mCurrentStyle);
                            } else {
                                mCurrentValue = new ResourceValue(type, name, mIsFramework);
                                mRepository.addResourceValue(type, mCurrentValue);
                            }
                        }
                    }
                }
            } else if (mDepth == 3 && mCurrentStyle != null) {
                String name = attributes.getValue(ATTR_NAME);
                if (name != null) {
                    if (name.startsWith(DEFAULT_NS_PREFIX)) {
                        name = name.substring(DEFAULT_NS_PREFIX_LEN);
                    }
                    mCurrentValue = new ResourceValue(null, name, mIsFramework);
                    mCurrentStyle.addItem(mCurrentValue);
                }
            }
        } finally {
            super.startElement(uri, localName, qName, attributes);
        }
    }
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (mCurrentValue != null) {
            String value = mCurrentValue.getValue();
            if (value == null) {
                mCurrentValue.setValue(new String(ch, start, length));
            } else {
                mCurrentValue.setValue(value + new String(ch, start, length));
            }
        }
    }
    public static String trimXmlWhitespaces(String value) {
        if (value == null) {
            return null;
        }
        int index;
        while ((index = value.indexOf('\n')) != -1) {
            int left = index - 1;
            while (left >= 0) {
                if (Character.isWhitespace(value.charAt(left))) {
                    left--;
                } else {
                    break;
                }
            }
            int right = index + 1;
            int count = value.length();
            while (right < count) {
                if (Character.isWhitespace(value.charAt(right))) {
                    right++;
                } else {
                    break;
                }
            }
            String leftString = null;
            if (left >= 0) {
                leftString = value.substring(0, left + 1);
            }
            String rightString = null;
            if (right < count) {
                rightString = value.substring(right);
            }
            if (leftString != null) {
                value = leftString;
                if (rightString != null) {
                    value += " " + rightString;
                }
            } else {
                value = rightString != null ? rightString : "";
            }
        }
        int length = value.length();
        char[] buffer = value.toCharArray();
        for (int i = 0 ; i < length ; i++) {
            if (buffer[i] == '\\' && i + 1 < length) {
                if (buffer[i+1] == 'u') {
                    if (i + 5 < length) {
                        int unicodeChar = Integer.parseInt(new String(buffer, i+2, 4), 16);
                        buffer[i] = (char)unicodeChar;
                        if (i + 6 < buffer.length) {
                            System.arraycopy(buffer, i+6, buffer, i+1, length - i - 6);
                        }
                        length -= 5;
                    }
                } else {
                    if (buffer[i+1] == 'n') {
                        buffer[i+1] = '\n';
                    }
                    System.arraycopy(buffer, i+1, buffer, i, length - i - 1);
                    length--;
                }
            }
        }
        return new String(buffer, 0, length);
    }
}

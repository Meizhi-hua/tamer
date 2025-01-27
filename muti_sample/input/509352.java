public class AliasActivity extends Activity {
    public final String ALIAS_META_DATA = "android.app.alias";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        XmlResourceParser parser = null;
        try {
            ActivityInfo ai = getPackageManager().getActivityInfo(
                    getComponentName(), PackageManager.GET_META_DATA);
            parser = ai.loadXmlMetaData(getPackageManager(),
                    ALIAS_META_DATA);
            if (parser == null) {
                throw new RuntimeException("Alias requires a meta-data field "
                        + ALIAS_META_DATA);
            }
            Intent intent = parseAlias(parser);
            if (intent == null) {
                throw new RuntimeException(
                        "No <intent> tag found in alias description");
            }
            startActivity(intent);
            finish();
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException("Error parsing alias", e);
        } catch (XmlPullParserException e) {
            throw new RuntimeException("Error parsing alias", e);
        } catch (IOException e) {
            throw new RuntimeException("Error parsing alias", e);
        } finally {
            if (parser != null) parser.close();
        }
    }
    private Intent parseAlias(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        AttributeSet attrs = Xml.asAttributeSet(parser);
        Intent intent = null;
        int type;
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
                && type != XmlPullParser.START_TAG) {
        }
        String nodeName = parser.getName();
        if (!"alias".equals(nodeName)) {
            throw new RuntimeException(
                    "Alias meta-data must start with <alias> tag; found"
                    + nodeName + " at " + parser.getPositionDescription());
        }
        int outerDepth = parser.getDepth();
        while ((type=parser.next()) != XmlPullParser.END_DOCUMENT
               && (type != XmlPullParser.END_TAG || parser.getDepth() > outerDepth)) {
            if (type == XmlPullParser.END_TAG || type == XmlPullParser.TEXT) {
                continue;
            }
            nodeName = parser.getName();
            if ("intent".equals(nodeName)) {
                Intent gotIntent = Intent.parseIntent(getResources(), parser, attrs);
                if (intent == null) intent = gotIntent;
            } else {
                XmlUtils.skipCurrentTag(parser);
            }
        }
        return intent;
    }
}

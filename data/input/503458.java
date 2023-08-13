public class SpnOverride {
    private HashMap<String, String> CarrierSpnMap;
    static final String LOG_TAG = "GSM";
    static final String PARTNER_SPN_OVERRIDE_PATH ="etc/spn-conf.xml";
    SpnOverride () {
        CarrierSpnMap = new HashMap<String, String>();
        loadSpnOverrides();
    }
    boolean containsCarrier(String carrier) {
        return CarrierSpnMap.containsKey(carrier);
    }
    String getSpn(String carrier) {
        return CarrierSpnMap.get(carrier);
    }
    private void loadSpnOverrides() {
        FileReader spnReader;
        final File spnFile = new File(Environment.getRootDirectory(),
                PARTNER_SPN_OVERRIDE_PATH);
        try {
            spnReader = new FileReader(spnFile);
        } catch (FileNotFoundException e) {
            Log.w(LOG_TAG, "Can't open " +
                    Environment.getRootDirectory() + "/" + PARTNER_SPN_OVERRIDE_PATH);
            return;
        }
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(spnReader);
            XmlUtils.beginDocument(parser, "spnOverrides");
            while (true) {
                XmlUtils.nextElement(parser);
                String name = parser.getName();
                if (!"spnOverride".equals(name)) {
                    break;
                }
                String numeric = parser.getAttributeValue(null, "numeric");
                String data    = parser.getAttributeValue(null, "spn");
                CarrierSpnMap.put(numeric, data);
            }
        } catch (XmlPullParserException e) {
            Log.w(LOG_TAG, "Exception in spn-conf parser " + e);
        } catch (IOException e) {
            Log.w(LOG_TAG, "Exception in spn-conf parser " + e);
        }
    }
}

public class ProvisionParser extends Parser {
    private EasSyncService mService;
    PolicySet mPolicySet = null;
    String mPolicyKey = null;
    boolean mRemoteWipe = false;
    boolean mIsSupportable = true;
    public ProvisionParser(InputStream in, EasSyncService service) throws IOException {
        super(in);
        mService = service;
    }
    public PolicySet getPolicySet() {
        return mPolicySet;
    }
    public String getPolicyKey() {
        return mPolicyKey;
    }
    public boolean getRemoteWipe() {
        return mRemoteWipe;
    }
    public boolean hasSupportablePolicySet() {
        return (mPolicySet != null) && mIsSupportable;
    }
    private void parseProvisionDocWbxml() throws IOException {
        int minPasswordLength = 0;
        int passwordMode = PolicySet.PASSWORD_MODE_NONE;
        int maxPasswordFails = 0;
        int maxScreenLockTime = 0;
        boolean supported = true;
        while (nextTag(Tags.PROVISION_EAS_PROVISION_DOC) != END) {
            switch (tag) {
                case Tags.PROVISION_DEVICE_PASSWORD_ENABLED:
                    if (getValueInt() == 1) {
                        if (passwordMode == PolicySet.PASSWORD_MODE_NONE) {
                            passwordMode = PolicySet.PASSWORD_MODE_SIMPLE;
                        }
                    }
                    break;
                case Tags.PROVISION_MIN_DEVICE_PASSWORD_LENGTH:
                    minPasswordLength = getValueInt();
                    break;
                case Tags.PROVISION_ALPHA_DEVICE_PASSWORD_ENABLED:
                    if (getValueInt() == 1) {
                        passwordMode = PolicySet.PASSWORD_MODE_STRONG;
                    }
                    break;
                case Tags.PROVISION_MAX_INACTIVITY_TIME_DEVICE_LOCK:
                    maxScreenLockTime = getValueInt();
                    break;
                case Tags.PROVISION_MAX_DEVICE_PASSWORD_FAILED_ATTEMPTS:
                    maxPasswordFails = getValueInt();
                    break;
                case Tags.PROVISION_ALLOW_SIMPLE_DEVICE_PASSWORD:
                    getValue();
                    break;
                case Tags.PROVISION_ATTACHMENTS_ENABLED:
                    if (getValueInt() == 0) {
                       supported = false;
                    }
                    break;
                case Tags.PROVISION_DEVICE_ENCRYPTION_ENABLED:
                case Tags.PROVISION_PASSWORD_RECOVERY_ENABLED:
                case Tags.PROVISION_DEVICE_PASSWORD_EXPIRATION:
                case Tags.PROVISION_DEVICE_PASSWORD_HISTORY:
                case Tags.PROVISION_MAX_ATTACHMENT_SIZE:
                    if (getValueInt() == 1) {
                        supported = false;
                    }
                    break;
                default:
                    skipTag();
            }
            if (!supported) {
                log("Policy not supported: " + tag);
                mIsSupportable = false;
            }
        }
        mPolicySet = new SecurityPolicy.PolicySet(minPasswordLength, passwordMode,
                    maxPasswordFails, maxScreenLockTime, true);
    }
    class ShadowPolicySet {
        int mMinPasswordLength = 0;
        int mPasswordMode = PolicySet.PASSWORD_MODE_NONE;
        int mMaxPasswordFails = 0;
        int mMaxScreenLockTime = 0;
    }
    public void parseProvisionDocXml(String doc) throws IOException {
        ShadowPolicySet sps = new ShadowPolicySet();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new ByteArrayInputStream(doc.getBytes()), "UTF-8");
            int type = parser.getEventType();
            if (type == XmlPullParser.START_DOCUMENT) {
                type = parser.next();
                if (type == XmlPullParser.START_TAG) {
                    String tagName = parser.getName();
                    if (tagName.equals("wap-provisioningdoc")) {
                        parseWapProvisioningDoc(parser, sps);
                    }
                }
            }
        } catch (XmlPullParserException e) {
           throw new IOException();
        }
        mPolicySet = new PolicySet(sps.mMinPasswordLength, sps.mPasswordMode, sps.mMaxPasswordFails,
                sps.mMaxScreenLockTime, true);
    }
    boolean parseSecurityPolicy(XmlPullParser parser, ShadowPolicySet sps)
            throws XmlPullParserException, IOException {
        boolean passwordRequired = true;
        while (true) {
            int type = parser.nextTag();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("characteristic")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String tagName = parser.getName();
                if (tagName.equals("parm")) {
                    String name = parser.getAttributeValue(null, "name");
                    if (name.equals("4131")) {
                        String value = parser.getAttributeValue(null, "value");
                        if (value.equals("1")) {
                            passwordRequired = false;
                        }
                    }
                }
            }
        }
        return passwordRequired;
    }
    void parseCharacteristic(XmlPullParser parser, ShadowPolicySet sps)
            throws XmlPullParserException, IOException {
        boolean enforceInactivityTimer = true;
        while (true) {
            int type = parser.nextTag();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("characteristic")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                if (parser.getName().equals("parm")) {
                    String name = parser.getAttributeValue(null, "name");
                    String value = parser.getAttributeValue(null, "value");
                    if (name.equals("AEFrequencyValue")) {
                        if (enforceInactivityTimer) {
                            if (value.equals("0")) {
                                sps.mMaxScreenLockTime = 1;
                            } else {
                                sps.mMaxScreenLockTime = 60*Integer.parseInt(value);
                            }
                        }
                    } else if (name.equals("AEFrequencyType")) {
                        if (value.equals("0")) {
                            enforceInactivityTimer = false;
                        }
                    } else if (name.equals("DeviceWipeThreshold")) {
                        sps.mMaxPasswordFails = Integer.parseInt(value);
                    } else if (name.equals("CodewordFrequency")) {
                    } else if (name.equals("MinimumPasswordLength")) {
                        sps.mMinPasswordLength = Integer.parseInt(value);
                    } else if (name.equals("PasswordComplexity")) {
                        if (value.equals("0")) {
                            sps.mPasswordMode = PolicySet.PASSWORD_MODE_STRONG;
                        } else {
                            sps.mPasswordMode = PolicySet.PASSWORD_MODE_SIMPLE;
                        }
                    }
                }
            }
        }
    }
    void parseRegistry(XmlPullParser parser, ShadowPolicySet sps)
            throws XmlPullParserException, IOException {
      while (true) {
          int type = parser.nextTag();
          if (type == XmlPullParser.END_TAG && parser.getName().equals("characteristic")) {
              break;
          } else if (type == XmlPullParser.START_TAG) {
              String name = parser.getName();
              if (name.equals("characteristic")) {
                  parseCharacteristic(parser, sps);
              }
          }
      }
    }
    void parseWapProvisioningDoc(XmlPullParser parser, ShadowPolicySet sps)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.nextTag();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("wap-provisioningdoc")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("characteristic")) {
                    String atype = parser.getAttributeValue(null, "type");
                    if (atype.equals("SecurityPolicy")) {
                        if (!parseSecurityPolicy(parser, sps)) {
                            return;
                        }
                    } else if (atype.equals("Registry")) {
                        parseRegistry(parser, sps);
                        return;
                    }
                }
            }
        }
    }
    public void parseProvisionData() throws IOException {
        while (nextTag(Tags.PROVISION_DATA) != END) {
            if (tag == Tags.PROVISION_EAS_PROVISION_DOC) {
                parseProvisionDocWbxml();
            } else {
                skipTag();
            }
        }
    }
    public void parsePolicy() throws IOException {
        String policyType = null;
        while (nextTag(Tags.PROVISION_POLICY) != END) {
            switch (tag) {
                case Tags.PROVISION_POLICY_TYPE:
                    policyType = getValue();
                    mService.userLog("Policy type: ", policyType);
                    break;
                case Tags.PROVISION_POLICY_KEY:
                    mPolicyKey = getValue();
                    break;
                case Tags.PROVISION_STATUS:
                    mService.userLog("Policy status: ", getValue());
                    break;
                case Tags.PROVISION_DATA:
                    if (policyType.equalsIgnoreCase(EasSyncService.EAS_2_POLICY_TYPE)) {
                        parseProvisionDocXml(getValue());
                    } else {
                        parseProvisionData();
                    }
                    break;
                default:
                    skipTag();
            }
        }
    }
    public void parsePolicies() throws IOException {
        while (nextTag(Tags.PROVISION_POLICIES) != END) {
            if (tag == Tags.PROVISION_POLICY) {
                parsePolicy();
            } else {
                skipTag();
            }
        }
    }
    @Override
    public boolean parse() throws IOException {
        boolean res = false;
        if (nextTag(START_DOCUMENT) != Tags.PROVISION_PROVISION) {
            throw new IOException();
        }
        while (nextTag(START_DOCUMENT) != END_DOCUMENT) {
            switch (tag) {
                case Tags.PROVISION_STATUS:
                    int status = getValueInt();
                    mService.userLog("Provision status: ", status);
                    res = (status == 1);
                    break;
                case Tags.PROVISION_POLICIES:
                    parsePolicies();
                    break;
                case Tags.PROVISION_REMOTE_WIPE:
                    mRemoteWipe = true;
                    break;
                default:
                    skipTag();
            }
        }
        return res;
    }
}
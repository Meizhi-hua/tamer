public class GSMPhone extends PhoneBase {
    static final String LOG_TAG = "GSM";
    private static final boolean LOCAL_DEBUG = true;
    public static final String CIPHERING_KEY = "ciphering_key";
    public static final String VM_NUMBER = "vm_number_key";
    public static final String VM_SIM_IMSI = "vm_sim_imsi_key";
    GsmCallTracker mCT;
    GsmServiceStateTracker mSST;
    GsmSMSDispatcher mSMS;
    SIMRecords mSIMRecords;
    SimCard mSimCard;
    StkService mStkService;
    ArrayList <GsmMmiCode> mPendingMMIs = new ArrayList<GsmMmiCode>();
    SimPhoneBookInterfaceManager mSimPhoneBookIntManager;
    SimSmsInterfaceManager mSimSmsIntManager;
    PhoneSubInfo mSubInfo;
    Registrant mPostDialHandler;
    RegistrantList mSsnRegistrants = new RegistrantList();
    Thread debugPortThread;
    ServerSocket debugSocket;
    private int mReportedRadioResets;
    private int mReportedAttemptedConnects;
    private int mReportedSuccessfulConnects;
    private String mImei;
    private String mImeiSv;
    private String mVmNumber;
    public
    GSMPhone (Context context, CommandsInterface ci, PhoneNotifier notifier) {
        this(context,ci,notifier, false);
    }
    public
    GSMPhone (Context context, CommandsInterface ci, PhoneNotifier notifier, boolean unitTestMode) {
        super(notifier, context, ci, unitTestMode);
        if (ci instanceof SimulatedRadioControl) {
            mSimulatedRadioControl = (SimulatedRadioControl) ci;
        }
        mCM.setPhoneType(Phone.PHONE_TYPE_GSM);
        mCT = new GsmCallTracker(this);
        mSST = new GsmServiceStateTracker (this);
        mSMS = new GsmSMSDispatcher(this);
        mIccFileHandler = new SIMFileHandler(this);
        mSIMRecords = new SIMRecords(this);
        mDataConnection = new GsmDataConnectionTracker (this);
        mSimCard = new SimCard(this);
        if (!unitTestMode) {
            mSimPhoneBookIntManager = new SimPhoneBookInterfaceManager(this);
            mSimSmsIntManager = new SimSmsInterfaceManager(this);
            mSubInfo = new PhoneSubInfo(this);
        }
        mStkService = StkService.getInstance(mCM, mSIMRecords, mContext,
                (SIMFileHandler)mIccFileHandler, mSimCard);
        mCM.registerForAvailable(this, EVENT_RADIO_AVAILABLE, null);
        mSIMRecords.registerForRecordsLoaded(this, EVENT_SIM_RECORDS_LOADED, null);
        mCM.registerForOffOrNotAvailable(this, EVENT_RADIO_OFF_OR_NOT_AVAILABLE, null);
        mCM.registerForOn(this, EVENT_RADIO_ON, null);
        mCM.setOnUSSD(this, EVENT_USSD, null);
        mCM.setOnSuppServiceNotification(this, EVENT_SSN, null);
        mSST.registerForNetworkAttach(this, EVENT_REGISTERED_TO_NETWORK, null);
        if (false) {
            try {
                debugSocket = new ServerSocket();
                debugSocket.setReuseAddress(true);
                debugSocket.bind (new InetSocketAddress("127.0.0.1", 6666));
                debugPortThread
                    = new Thread(
                        new Runnable() {
                            public void run() {
                                for(;;) {
                                    try {
                                        Socket sock;
                                        sock = debugSocket.accept();
                                        Log.i(LOG_TAG, "New connection; resetting radio");
                                        mCM.resetRadio(null);
                                        sock.close();
                                    } catch (IOException ex) {
                                        Log.w(LOG_TAG,
                                            "Exception accepting socket", ex);
                                    }
                                }
                            }
                        },
                        "GSMPhone debug");
                debugPortThread.start();
            } catch (IOException ex) {
                Log.w(LOG_TAG, "Failure to open com.android.internal.telephony.debug socket", ex);
            }
        }
        SystemProperties.set(TelephonyProperties.CURRENT_ACTIVE_PHONE,
                new Integer(Phone.PHONE_TYPE_GSM).toString());
    }
    public void dispose() {
        synchronized(PhoneProxy.lockForRadioTechnologyChange) {
            super.dispose();
            mCM.unregisterForAvailable(this); 
            mSIMRecords.unregisterForRecordsLoaded(this); 
            mCM.unregisterForOffOrNotAvailable(this); 
            mCM.unregisterForOn(this); 
            mSST.unregisterForNetworkAttach(this); 
            mCM.unSetOnUSSD(this);
            mCM.unSetOnSuppServiceNotification(this);
            mPendingMMIs.clear();
            mStkService.dispose();
            mCT.dispose();
            mDataConnection.dispose();
            mSST.dispose();
            mIccFileHandler.dispose(); 
            mSIMRecords.dispose();
            mSimCard.dispose();
            mSimPhoneBookIntManager.dispose();
            mSimSmsIntManager.dispose();
            mSubInfo.dispose();
        }
    }
    public void removeReferences() {
            this.mSimulatedRadioControl = null;
            this.mStkService = null;
            this.mSimPhoneBookIntManager = null;
            this.mSimSmsIntManager = null;
            this.mSMS = null;
            this.mSubInfo = null;
            this.mSIMRecords = null;
            this.mIccFileHandler = null;
            this.mSimCard = null;
            this.mDataConnection = null;
            this.mCT = null;
            this.mSST = null;
    }
    protected void finalize() {
        if(LOCAL_DEBUG) Log.d(LOG_TAG, "GSMPhone finalized");
    }
    public ServiceState
    getServiceState() {
        return mSST.ss;
    }
    public CellLocation getCellLocation() {
        return mSST.cellLoc;
    }
    public Phone.State getState() {
        return mCT.state;
    }
    public String getPhoneName() {
        return "GSM";
    }
    public int getPhoneType() {
        return Phone.PHONE_TYPE_GSM;
    }
    public SignalStrength getSignalStrength() {
        return mSST.mSignalStrength;
    }
    public boolean getMessageWaitingIndicator() {
        return mSIMRecords.getVoiceMessageWaiting();
    }
    public boolean getCallForwardingIndicator() {
        return mSIMRecords.getVoiceCallForwardingFlag();
    }
    public List<? extends MmiCode>
    getPendingMmiCodes() {
        return mPendingMMIs;
    }
    public DataState getDataConnectionState() {
        DataState ret = DataState.DISCONNECTED;
        if (mSST == null) {
            ret = DataState.DISCONNECTED;
        } else if (mSST.getCurrentGprsState()
                != ServiceState.STATE_IN_SERVICE) {
            ret = DataState.DISCONNECTED;
        } else { 
            switch (mDataConnection.getState()) {
                case FAILED:
                case IDLE:
                    ret = DataState.DISCONNECTED;
                break;
                case CONNECTED:
                case DISCONNECTING:
                    if ( mCT.state != Phone.State.IDLE
                            && !mSST.isConcurrentVoiceAndData()) {
                        ret = DataState.SUSPENDED;
                    } else {
                        ret = DataState.CONNECTED;
                    }
                break;
                case INITING:
                case CONNECTING:
                case SCANNING:
                    ret = DataState.CONNECTING;
                break;
            }
        }
        return ret;
    }
    public DataActivityState getDataActivityState() {
        DataActivityState ret = DataActivityState.NONE;
        if (mSST.getCurrentGprsState() == ServiceState.STATE_IN_SERVICE) {
            switch (mDataConnection.getActivity()) {
                case DATAIN:
                    ret = DataActivityState.DATAIN;
                break;
                case DATAOUT:
                    ret = DataActivityState.DATAOUT;
                break;
                case DATAINANDOUT:
                    ret = DataActivityState.DATAINANDOUT;
                break;
            }
        }
        return ret;
    }
     void notifyPhoneStateChanged() {
        mNotifier.notifyPhoneState(this);
    }
     void notifyPreciseCallStateChanged() {
        super.notifyPreciseCallStateChangedP();
    }
     void
    notifyNewRingingConnection(Connection c) {
        super.notifyNewRingingConnectionP(c);
    }
     void
    notifyDisconnect(Connection cn) {
        mDisconnectRegistrants.notifyResult(cn);
    }
    void notifyUnknownConnection() {
        mUnknownConnectionRegistrants.notifyResult(this);
    }
    void notifySuppServiceFailed(SuppService code) {
        mSuppServiceFailedRegistrants.notifyResult(code);
    }
     void
    notifyServiceStateChanged(ServiceState ss) {
        super.notifyServiceStateChangedP(ss);
    }
    void notifyLocationChanged() {
        mNotifier.notifyCellLocation(this);
    }
     void
    notifySignalStrength() {
        mNotifier.notifySignalStrength(this);
    }
     void
    notifyDataConnectionFailed(String reason) {
        mNotifier.notifyDataConnectionFailed(this, reason);
    }
     void
    updateMessageWaitingIndicator(boolean mwi) {
        mSIMRecords.setVoiceMessageWaiting(1, mwi ? -1 : 0);
    }
    public void
    notifyCallForwardingIndicator() {
        mNotifier.notifyCallForwardingChanged(this);
    }
    public final void
    setSystemProperty(String property, String value) {
        super.setSystemProperty(property, value);
    }
    public void registerForSuppServiceNotification(
            Handler h, int what, Object obj) {
        mSsnRegistrants.addUnique(h, what, obj);
        if (mSsnRegistrants.size() == 1) mCM.setSuppServiceNotifications(true, null);
    }
    public void unregisterForSuppServiceNotification(Handler h) {
        mSsnRegistrants.remove(h);
        if (mSsnRegistrants.size() == 0) mCM.setSuppServiceNotifications(false, null);
    }
    public void
    acceptCall() throws CallStateException {
        mCT.acceptCall();
    }
    public void
    rejectCall() throws CallStateException {
        mCT.rejectCall();
    }
    public void
    switchHoldingAndActive() throws CallStateException {
        mCT.switchWaitingOrHoldingAndActive();
    }
    public boolean canConference() {
        return mCT.canConference();
    }
    public boolean canDial() {
        return mCT.canDial();
    }
    public void conference() throws CallStateException {
        mCT.conference();
    }
    public void clearDisconnected() {
        mCT.clearDisconnected();
    }
    public boolean canTransfer() {
        return mCT.canTransfer();
    }
    public void explicitCallTransfer() throws CallStateException {
        mCT.explicitCallTransfer();
    }
    public GsmCall
    getForegroundCall() {
        return mCT.foregroundCall;
    }
    public GsmCall
    getBackgroundCall() {
        return mCT.backgroundCall;
    }
    public GsmCall
    getRingingCall() {
        return mCT.ringingCall;
    }
    private boolean handleCallDeflectionIncallSupplementaryService(
            String dialString) throws CallStateException {
        if (dialString.length() > 1) {
            return false;
        }
        if (getRingingCall().getState() != GsmCall.State.IDLE) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG, "MmiCode 0: rejectCall");
            try {
                mCT.rejectCall();
            } catch (CallStateException e) {
                if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "reject failed", e);
                notifySuppServiceFailed(Phone.SuppService.REJECT);
            }
        } else if (getBackgroundCall().getState() != GsmCall.State.IDLE) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "MmiCode 0: hangupWaitingOrBackground");
            mCT.hangupWaitingOrBackground();
        }
        return true;
    }
    private boolean handleCallWaitingIncallSupplementaryService(
            String dialString) throws CallStateException {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCall call = (GsmCall) getForegroundCall();
        try {
            if (len > 1) {
                char ch = dialString.charAt(1);
                int callIndex = ch - '0';
                if (callIndex >= 1 && callIndex <= GsmCallTracker.MAX_CONNECTIONS) {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG,
                            "MmiCode 1: hangupConnectionByIndex " +
                            callIndex);
                    mCT.hangupConnectionByIndex(call, callIndex);
                }
            } else {
                if (call.getState() != GsmCall.State.IDLE) {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG,
                            "MmiCode 1: hangup foreground");
                    mCT.hangup(call);
                } else {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG,
                            "MmiCode 1: switchWaitingOrHoldingAndActive");
                    mCT.switchWaitingOrHoldingAndActive();
                }
            }
        } catch (CallStateException e) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG,
                "hangup failed", e);
            notifySuppServiceFailed(Phone.SuppService.HANGUP);
        }
        return true;
    }
    private boolean handleCallHoldIncallSupplementaryService(String dialString)
            throws CallStateException {
        int len = dialString.length();
        if (len > 2) {
            return false;
        }
        GsmCall call = (GsmCall) getForegroundCall();
        if (len > 1) {
            try {
                char ch = dialString.charAt(1);
                int callIndex = ch - '0';
                GsmConnection conn = mCT.getConnectionByIndex(call, callIndex);
                if (conn != null && callIndex >= 1 && callIndex <= GsmCallTracker.MAX_CONNECTIONS) {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG, "MmiCode 2: separate call "+
                            callIndex);
                    mCT.separate(conn);
                } else {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG, "separate: invalid call index "+
                            callIndex);
                    notifySuppServiceFailed(Phone.SuppService.SEPARATE);
                }
            } catch (CallStateException e) {
                if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "separate failed", e);
                notifySuppServiceFailed(Phone.SuppService.SEPARATE);
            }
        } else {
            try {
                if (getRingingCall().getState() != GsmCall.State.IDLE) {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "MmiCode 2: accept ringing call");
                    mCT.acceptCall();
                } else {
                    if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "MmiCode 2: switchWaitingOrHoldingAndActive");
                    mCT.switchWaitingOrHoldingAndActive();
                }
            } catch (CallStateException e) {
                if (LOCAL_DEBUG) Log.d(LOG_TAG,
                    "switch failed", e);
                notifySuppServiceFailed(Phone.SuppService.SWITCH);
            }
        }
        return true;
    }
    private boolean handleMultipartyIncallSupplementaryService(
            String dialString) throws CallStateException {
        if (dialString.length() > 1) {
            return false;
        }
        if (LOCAL_DEBUG) Log.d(LOG_TAG, "MmiCode 3: merge calls");
        try {
            conference();
        } catch (CallStateException e) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG,
                "conference failed", e);
            notifySuppServiceFailed(Phone.SuppService.CONFERENCE);
        }
        return true;
    }
    private boolean handleEctIncallSupplementaryService(String dialString)
            throws CallStateException {
        int len = dialString.length();
        if (len != 1) {
            return false;
        }
        if (LOCAL_DEBUG) Log.d(LOG_TAG, "MmiCode 4: explicit call transfer");
        try {
            explicitCallTransfer();
        } catch (CallStateException e) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG,
                "transfer failed", e);
            notifySuppServiceFailed(Phone.SuppService.TRANSFER);
        }
        return true;
    }
    private boolean handleCcbsIncallSupplementaryService(String dialString)
            throws CallStateException {
        if (dialString.length() > 1) {
            return false;
        }
        Log.i(LOG_TAG, "MmiCode 5: CCBS not supported!");
        notifySuppServiceFailed(Phone.SuppService.UNKNOWN);
        return true;
    }
    public boolean handleInCallMmiCommands(String dialString)
            throws CallStateException {
        if (!isInCall()) {
            return false;
        }
        if (TextUtils.isEmpty(dialString)) {
            return false;
        }
        boolean result = false;
        char ch = dialString.charAt(0);
        switch (ch) {
            case '0':
                result = handleCallDeflectionIncallSupplementaryService(
                        dialString);
                break;
            case '1':
                result = handleCallWaitingIncallSupplementaryService(
                        dialString);
                break;
            case '2':
                result = handleCallHoldIncallSupplementaryService(dialString);
                break;
            case '3':
                result = handleMultipartyIncallSupplementaryService(dialString);
                break;
            case '4':
                result = handleEctIncallSupplementaryService(dialString);
                break;
            case '5':
                result = handleCcbsIncallSupplementaryService(dialString);
                break;
            default:
                break;
        }
        return result;
    }
    boolean isInCall() {
        GsmCall.State foregroundCallState = getForegroundCall().getState();
        GsmCall.State backgroundCallState = getBackgroundCall().getState();
        GsmCall.State ringingCallState = getRingingCall().getState();
       return (foregroundCallState.isAlive() ||
                backgroundCallState.isAlive() ||
                ringingCallState.isAlive());
    }
    public Connection
    dial (String dialString) throws CallStateException {
        String newDialString = PhoneNumberUtils.stripSeparators(dialString);
        if (handleInCallMmiCommands(newDialString)) {
            return null;
        }
        String networkPortion = PhoneNumberUtils.extractNetworkPortionAlt(newDialString);
        GsmMmiCode mmi = GsmMmiCode.newFromDialString(networkPortion, this);
        if (LOCAL_DEBUG) Log.d(LOG_TAG,
                               "dialing w/ mmi '" + mmi + "'...");
        if (mmi == null) {
            return mCT.dial(newDialString);
        } else if (mmi.isTemporaryModeCLIR()) {
            return mCT.dial(mmi.dialingNumber, mmi.getCLIRMode());
        } else {
            mPendingMMIs.add(mmi);
            mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.processCode();
            return null;
        }
    }
    public boolean handlePinMmi(String dialString) {
        GsmMmiCode mmi = GsmMmiCode.newFromDialString(dialString, this);
        if (mmi != null && mmi.isPinCommand()) {
            mPendingMMIs.add(mmi);
            mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
            mmi.processCode();
            return true;
        }
        return false;
    }
    public void sendUssdResponse(String ussdMessge) {
        GsmMmiCode mmi = GsmMmiCode.newFromUssdUserInput(ussdMessge, this);
        mPendingMMIs.add(mmi);
        mMmiRegistrants.notifyRegistrants(new AsyncResult(null, mmi, null));
        mmi.sendUssd(ussdMessge);
    }
    public void
    sendDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Log.e(LOG_TAG,
                    "sendDtmf called with invalid character '" + c + "'");
        } else {
            if (mCT.state ==  Phone.State.OFFHOOK) {
                mCM.sendDtmf(c, null);
            }
        }
    }
    public void
    startDtmf(char c) {
        if (!PhoneNumberUtils.is12Key(c)) {
            Log.e(LOG_TAG,
                "startDtmf called with invalid character '" + c + "'");
        } else {
            mCM.startDtmf(c, null);
        }
    }
    public void
    stopDtmf() {
        mCM.stopDtmf(null);
    }
    public void
    sendBurstDtmf(String dtmfString) {
        Log.e(LOG_TAG, "[GSMPhone] sendBurstDtmf() is a CDMA method");
    }
    public void
    setRadioPower(boolean power) {
        mSST.setRadioPower(power);
    }
    private void storeVoiceMailNumber(String number) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(VM_NUMBER, number);
        editor.commit();
        setVmSimImsi(getSubscriberId());
    }
    public String getVoiceMailNumber() {
        String number = mSIMRecords.getVoiceMailNumber();
        if (TextUtils.isEmpty(number)) {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
            number = sp.getString(VM_NUMBER, null);
        }
        return number;
    }
    private String getVmSimImsi() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        return sp.getString(VM_SIM_IMSI, null);
    }
    private void setVmSimImsi(String imsi) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(VM_SIM_IMSI, imsi);
        editor.commit();
    }
    public String getVoiceMailAlphaTag() {
        String ret;
        ret = mSIMRecords.getVoiceMailAlphaTag();
        if (ret == null || ret.length() == 0) {
            return mContext.getText(
                com.android.internal.R.string.defaultVoiceMailAlphaTag).toString();
        }
        return ret;
    }
    public String getDeviceId() {
        return mImei;
    }
    public String getDeviceSvn() {
        return mImeiSv;
    }
    public String getEsn() {
        Log.e(LOG_TAG, "[GSMPhone] getEsn() is a CDMA method");
        return "0";
    }
    public String getMeid() {
        Log.e(LOG_TAG, "[GSMPhone] getMeid() is a CDMA method");
        return "0";
    }
    public String getSubscriberId() {
        return mSIMRecords.imsi;
    }
    public String getIccSerialNumber() {
        return mSIMRecords.iccid;
    }
    public String getLine1Number() {
        return mSIMRecords.getMsisdnNumber();
    }
    public String getLine1AlphaTag() {
        return mSIMRecords.getMsisdnAlphaTag();
    }
    public void setLine1Number(String alphaTag, String number, Message onComplete) {
        mSIMRecords.setMsisdnNumber(alphaTag, number, onComplete);
    }
    public void setVoiceMailNumber(String alphaTag,
                            String voiceMailNumber,
                            Message onComplete) {
        Message resp;
        mVmNumber = voiceMailNumber;
        resp = obtainMessage(EVENT_SET_VM_NUMBER_DONE, 0, 0, onComplete);
        mSIMRecords.setVoiceMailNumber(alphaTag, mVmNumber, resp);
    }
    private boolean isValidCommandInterfaceCFReason (int commandInterfaceCFReason) {
        switch (commandInterfaceCFReason) {
        case CF_REASON_UNCONDITIONAL:
        case CF_REASON_BUSY:
        case CF_REASON_NO_REPLY:
        case CF_REASON_NOT_REACHABLE:
        case CF_REASON_ALL:
        case CF_REASON_ALL_CONDITIONAL:
            return true;
        default:
            return false;
        }
    }
    private boolean isValidCommandInterfaceCFAction (int commandInterfaceCFAction) {
        switch (commandInterfaceCFAction) {
        case CF_ACTION_DISABLE:
        case CF_ACTION_ENABLE:
        case CF_ACTION_REGISTRATION:
        case CF_ACTION_ERASURE:
            return true;
        default:
            return false;
        }
    }
    protected  boolean isCfEnable(int action) {
        return (action == CF_ACTION_ENABLE) || (action == CF_ACTION_REGISTRATION);
    }
    public void getCallForwardingOption(int commandInterfaceCFReason, Message onComplete) {
        if (isValidCommandInterfaceCFReason(commandInterfaceCFReason)) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG, "requesting call forwarding query.");
            Message resp;
            if (commandInterfaceCFReason == CF_REASON_UNCONDITIONAL) {
                resp = obtainMessage(EVENT_GET_CALL_FORWARD_DONE, onComplete);
            } else {
                resp = onComplete;
            }
            mCM.queryCallForwardStatus(commandInterfaceCFReason,0,null,resp);
        }
    }
    public void setCallForwardingOption(int commandInterfaceCFAction,
            int commandInterfaceCFReason,
            String dialingNumber,
            int timerSeconds,
            Message onComplete) {
        if (    (isValidCommandInterfaceCFAction(commandInterfaceCFAction)) &&
                (isValidCommandInterfaceCFReason(commandInterfaceCFReason))) {
            Message resp;
            if (commandInterfaceCFReason == CF_REASON_UNCONDITIONAL) {
                resp = obtainMessage(EVENT_SET_CALL_FORWARD_DONE,
                        isCfEnable(commandInterfaceCFAction) ? 1 : 0, 0, onComplete);
            } else {
                resp = onComplete;
            }
            mCM.setCallForward(commandInterfaceCFAction,
                    commandInterfaceCFReason,
                    CommandsInterface.SERVICE_CLASS_VOICE,
                    dialingNumber,
                    timerSeconds,
                    resp);
        }
    }
    public void getOutgoingCallerIdDisplay(Message onComplete) {
        mCM.getCLIR(onComplete);
    }
    public void setOutgoingCallerIdDisplay(int commandInterfaceCLIRMode,
                                           Message onComplete) {
        mCM.setCLIR(commandInterfaceCLIRMode,
                obtainMessage(EVENT_SET_CLIR_COMPLETE, commandInterfaceCLIRMode, 0, onComplete));
    }
    public void getCallWaiting(Message onComplete) {
        mCM.queryCallWaiting(CommandsInterface.SERVICE_CLASS_VOICE, onComplete);
    }
    public void setCallWaiting(boolean enable, Message onComplete) {
        mCM.setCallWaiting(enable, CommandsInterface.SERVICE_CLASS_VOICE, onComplete);
    }
    public boolean
    getIccRecordsLoaded() {
        return mSIMRecords.getRecordsLoaded();
    }
    public IccCard getIccCard() {
        return mSimCard;
    }
    public void
    getAvailableNetworks(Message response) {
        mCM.getAvailableNetworks(response);
    }
    private static class NetworkSelectMessage {
        public Message message;
        public String operatorNumeric;
        public String operatorAlphaLong;
    }
    public void
    setNetworkSelectionModeAutomatic(Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = "";
        nsm.operatorAlphaLong = "";
        Message msg = obtainMessage(EVENT_SET_NETWORK_AUTOMATIC_COMPLETE, nsm);
        if (LOCAL_DEBUG)
            Log.d(LOG_TAG, "wrapping and sending message to connect automatically");
        mCM.setNetworkSelectionModeAutomatic(msg);
    }
    public void
    selectNetworkManually(com.android.internal.telephony.gsm.NetworkInfo network,
            Message response) {
        NetworkSelectMessage nsm = new NetworkSelectMessage();
        nsm.message = response;
        nsm.operatorNumeric = network.operatorNumeric;
        nsm.operatorAlphaLong = network.operatorAlphaLong;
        Message msg = obtainMessage(EVENT_SET_NETWORK_MANUAL_COMPLETE, nsm);
        mCM.setNetworkSelectionModeManual(network.operatorNumeric, msg);
    }
    public void
    getNeighboringCids(Message response) {
        mCM.getNeighboringCids(response);
    }
    public void setOnPostDialCharacter(Handler h, int what, Object obj) {
        mPostDialHandler = new Registrant(h, what, obj);
    }
    public void setMute(boolean muted) {
        mCT.setMute(muted);
    }
    public boolean getMute() {
        return mCT.getMute();
    }
    public void getDataCallList(Message response) {
        mCM.getDataCallList(response);
    }
    public List<DataConnection> getCurrentDataConnectionList () {
        return mDataConnection.getAllDataConnections();
    }
    public void updateServiceLocation() {
        mSST.enableSingleLocationUpdate();
    }
    public void enableLocationUpdates() {
        mSST.enableLocationUpdates();
    }
    public void disableLocationUpdates() {
        mSST.disableLocationUpdates();
    }
    public boolean getDataRoamingEnabled() {
        return mDataConnection.getDataOnRoamingEnabled();
    }
    public void setDataRoamingEnabled(boolean enable) {
        mDataConnection.setDataOnRoamingEnabled(enable);
    }
    public boolean enableDataConnectivity() {
        return mDataConnection.setDataEnabled(true);
    }
    public boolean disableDataConnectivity() {
        return mDataConnection.setDataEnabled(false);
    }
    public boolean isDataConnectivityPossible() {
        boolean noData = mDataConnection.getDataEnabled() &&
            getDataConnectionState() == DataState.DISCONNECTED;
        return !noData && getIccCard().getState() == SimCard.State.READY &&
                getServiceState().getState() == ServiceState.STATE_IN_SERVICE &&
            (mDataConnection.getDataOnRoamingEnabled() || !getServiceState().getRoaming());
    }
     void
    onMMIDone(GsmMmiCode mmi) {
        if (mPendingMMIs.remove(mmi) || mmi.isUssdRequest()) {
            mMmiCompleteRegistrants.notifyRegistrants(
                new AsyncResult(null, mmi, null));
        }
    }
    private void
    onNetworkInitiatedUssd(GsmMmiCode mmi) {
        mMmiCompleteRegistrants.notifyRegistrants(
            new AsyncResult(null, mmi, null));
    }
    private void
    onIncomingUSSD (int ussdMode, String ussdMessage) {
        boolean isUssdError;
        boolean isUssdRequest;
        isUssdRequest
            = (ussdMode == CommandsInterface.USSD_MODE_REQUEST);
        isUssdError
            = (ussdMode != CommandsInterface.USSD_MODE_NOTIFY
                && ussdMode != CommandsInterface.USSD_MODE_REQUEST);
        GsmMmiCode found = null;
        for (int i = 0, s = mPendingMMIs.size() ; i < s; i++) {
            if(mPendingMMIs.get(i).isPendingUSSD()) {
                found = mPendingMMIs.get(i);
                break;
            }
        }
        if (found != null) {
            if (isUssdError) {
                found.onUssdFinishedError();
            } else {
                found.onUssdFinished(ussdMessage, isUssdRequest);
            }
        } else { 
            if (!isUssdError && ussdMessage != null) {
                GsmMmiCode mmi;
                mmi = GsmMmiCode.newNetworkInitiatedUssd(ussdMessage,
                                                   isUssdRequest,
                                                   GSMPhone.this);
                onNetworkInitiatedUssd(mmi);
            }
        }
    }
    protected  void syncClirSetting() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        int clirSetting = sp.getInt(CLIR_KEY, -1);
        if (clirSetting >= 0) {
            mCM.setCLIR(clirSetting, null);
        }
    }
    @Override
    public void handleMessage (Message msg) {
        AsyncResult ar;
        Message onComplete;
        switch (msg.what) {
            case EVENT_RADIO_AVAILABLE: {
                mCM.getBasebandVersion(
                        obtainMessage(EVENT_GET_BASEBAND_VERSION_DONE));
                mCM.getIMEI(obtainMessage(EVENT_GET_IMEI_DONE));
                mCM.getIMEISV(obtainMessage(EVENT_GET_IMEISV_DONE));
            }
            break;
            case EVENT_RADIO_ON:
            break;
            case EVENT_REGISTERED_TO_NETWORK:
                syncClirSetting();
                break;
            case EVENT_SIM_RECORDS_LOADED:
                updateCurrentCarrierInProvider();
                String imsi = getVmSimImsi();
                if (imsi != null && !getSubscriberId().equals(imsi)) {
                    storeVoiceMailNumber(null);
                    setVmSimImsi(null);
                }
            break;
            case EVENT_GET_BASEBAND_VERSION_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    break;
                }
                if (LOCAL_DEBUG) Log.d(LOG_TAG, "Baseband version: " + ar.result);
                setSystemProperty(PROPERTY_BASEBAND_VERSION, (String)ar.result);
            break;
            case EVENT_GET_IMEI_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    break;
                }
                mImei = (String)ar.result;
            break;
            case EVENT_GET_IMEISV_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception != null) {
                    break;
                }
                mImeiSv = (String)ar.result;
            break;
            case EVENT_USSD:
                ar = (AsyncResult)msg.obj;
                String[] ussdResult = (String[]) ar.result;
                if (ussdResult.length > 1) {
                    try {
                        onIncomingUSSD(Integer.parseInt(ussdResult[0]), ussdResult[1]);
                    } catch (NumberFormatException e) {
                        Log.w(LOG_TAG, "error parsing USSD");
                    }
                }
            break;
            case EVENT_RADIO_OFF_OR_NOT_AVAILABLE:
                for (int i = 0, s = mPendingMMIs.size() ; i < s; i++) {
                    if (mPendingMMIs.get(i).isPendingUSSD()) {
                        mPendingMMIs.get(i).onUssdFinishedError();
                    }
                }
            break;
            case EVENT_SSN:
                ar = (AsyncResult)msg.obj;
                SuppServiceNotification not = (SuppServiceNotification) ar.result;
                mSsnRegistrants.notifyRegistrants(ar);
            break;
            case EVENT_SET_CALL_FORWARD_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    mSIMRecords.setVoiceCallForwardingFlag(1, msg.arg1 == 1);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
                break;
            case EVENT_SET_VM_NUMBER_DONE:
                ar = (AsyncResult)msg.obj;
                if (IccVmNotSupportedException.class.isInstance(ar.exception)) {
                    storeVoiceMailNumber(mVmNumber);
                    ar.exception = null;
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
                break;
            case EVENT_GET_CALL_FORWARD_DONE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    handleCfuQueryResult((CallForwardInfo[])ar.result);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
                break;
            case EVENT_SET_NETWORK_MANUAL_COMPLETE:
            case EVENT_SET_NETWORK_AUTOMATIC_COMPLETE:
                handleSetSelectNetwork((AsyncResult) msg.obj);
                break;
            case EVENT_SET_CLIR_COMPLETE:
                ar = (AsyncResult)msg.obj;
                if (ar.exception == null) {
                    saveClirSetting(msg.arg1);
                }
                onComplete = (Message) ar.userObj;
                if (onComplete != null) {
                    AsyncResult.forMessage(onComplete, ar.result, ar.exception);
                    onComplete.sendToTarget();
                }
                break;
             default:
                 super.handleMessage(msg);
        }
    }
    boolean updateCurrentCarrierInProvider() {
        if (mSIMRecords != null) {
            try {
                Uri uri = Uri.withAppendedPath(Telephony.Carriers.CONTENT_URI, "current");
                ContentValues map = new ContentValues();
                map.put(Telephony.Carriers.NUMERIC, mSIMRecords.getSIMOperatorNumeric());
                mContext.getContentResolver().insert(uri, map);
                return true;
            } catch (SQLException e) {
                Log.e(LOG_TAG, "Can't store current operator", e);
            }
        }
        return false;
    }
    private void handleSetSelectNetwork(AsyncResult ar) {
        if (!(ar.userObj instanceof NetworkSelectMessage)) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG, "unexpected result from user object.");
            return;
        }
        NetworkSelectMessage nsm = (NetworkSelectMessage) ar.userObj;
        if (nsm.message != null) {
            if (LOCAL_DEBUG) Log.d(LOG_TAG, "sending original message to recipient");
            AsyncResult.forMessage(nsm.message, ar.result, ar.exception);
            nsm.message.sendToTarget();
        }
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(NETWORK_SELECTION_KEY, nsm.operatorNumeric);
        editor.putString(NETWORK_SELECTION_NAME_KEY, nsm.operatorAlphaLong);
        if (! editor.commit()) {
            Log.e(LOG_TAG, "failed to commit network selection preference");
        }
    }
    public void saveClirSetting(int commandInterfaceCLIRMode) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(CLIR_KEY, commandInterfaceCLIRMode);
        if (! editor.commit()) {
            Log.e(LOG_TAG, "failed to commit CLIR preference");
        }
    }
    private void handleCfuQueryResult(CallForwardInfo[] infos) {
        if (infos == null || infos.length == 0) {
            mSIMRecords.setVoiceCallForwardingFlag(1, false);
        } else {
            for (int i = 0, s = infos.length; i < s; i++) {
                if ((infos[i].serviceClass & SERVICE_CLASS_VOICE) != 0) {
                    mSIMRecords.setVoiceCallForwardingFlag(1, (infos[i].status == 1));
                    break;
                }
            }
        }
    }
    public PhoneSubInfo getPhoneSubInfo(){
        return mSubInfo;
    }
    public IccSmsInterfaceManager getIccSmsInterfaceManager(){
        return mSimSmsIntManager;
    }
    public IccPhoneBookInterfaceManager getIccPhoneBookInterfaceManager(){
        return mSimPhoneBookIntManager;
    }
    public IccFileHandler getIccFileHandler(){
        return this.mIccFileHandler;
    }
    public void activateCellBroadcastSms(int activate, Message response) {
        Log.e(LOG_TAG, "Error! This functionality is not implemented for GSM.");
    }
    public void getCellBroadcastSmsConfig(Message response) {
        Log.e(LOG_TAG, "Error! This functionality is not implemented for GSM.");
    }
    public void setCellBroadcastSmsConfig(int[] configValuesArray, Message response){
        Log.e(LOG_TAG, "Error! This functionality is not implemented for GSM.");
    }
}

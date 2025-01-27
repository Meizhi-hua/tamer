public class BluetoothPbapObexServer extends ServerRequestHandler {
    private static final String TAG = "BluetoothPbapObexServer";
    private static final boolean D = BluetoothPbapService.DEBUG;
    private static final boolean V = BluetoothPbapService.VERBOSE;
    private static final int UUID_LENGTH = 16;
    private static final int VCARD_NAME_SUFFIX_LENGTH = 5;
    private static final byte[] PBAP_TARGET = new byte[] {
            0x79, 0x61, 0x35, (byte)0xf0, (byte)0xf0, (byte)0xc5, 0x11, (byte)0xd8, 0x09, 0x66,
            0x08, 0x00, 0x20, 0x0c, (byte)0x9a, 0x66
    };
    private static final String[] LEGAL_PATH = {
            "/telecom", "/telecom/pb", "/telecom/ich", "/telecom/och", "/telecom/mch",
            "/telecom/cch"
    };
    @SuppressWarnings("unused")
    private static final String[] LEGAL_PATH_WITH_SIM = {
            "/telecom", "/telecom/pb", "/telecom/ich", "/telecom/och", "/telecom/mch",
            "/telecom/cch", "/SIM1", "/SIM1/telecom", "/SIM1/telecom/ich", "/SIM1/telecom/och",
            "/SIM1/telecom/mch", "/SIM1/telecom/cch", "/SIM1/telecom/pb"
    };
    private static final String SIM1 = "SIM1";
    private static final String MCH = "mch";
    private static final String ICH = "ich";
    private static final String OCH = "och";
    private static final String CCH = "cch";
    private static final String PB = "pb";
    private static final String ICH_PATH = "/telecom/ich";
    private static final String OCH_PATH = "/telecom/och";
    private static final String MCH_PATH = "/telecom/mch";
    private static final String CCH_PATH = "/telecom/cch";
    private static final String PB_PATH = "/telecom/pb";
    private static final String TYPE_LISTING = "x-bt/vcard-listing";
    private static final String TYPE_VCARD = "x-bt/vcard";
    private static final int NEED_SEND_BODY = -1;
    private static final String TYPE_PB = "x-bt/phonebook";
    private boolean mNeedPhonebookSize = false;
    private boolean mNeedNewMissedCallsNum = false;
    private int mMissedCallSize = 0;
    private String mCurrentPath = "";
    private long mConnectionId;
    private Handler mCallback = null;
    private Context mContext;
    private BluetoothPbapVcardManager mVcardManager;
    private int mOrderBy  = ORDER_BY_INDEXED;
    public static int ORDER_BY_INDEXED = 0;
    public static int ORDER_BY_ALPHABETICAL = 1;
    public static boolean sIsAborted = false;
    public static class ContentType {
        public static final int PHONEBOOK = 1;
        public static final int INCOMING_CALL_HISTORY = 2;
        public static final int OUTGOING_CALL_HISTORY = 3;
        public static final int MISSED_CALL_HISTORY = 4;
        public static final int COMBINED_CALL_HISTORY = 5;
    }
    public BluetoothPbapObexServer(Handler callback, Context context) {
        super();
        mConnectionId = -1;
        mCallback = callback;
        mContext = context;
        mVcardManager = new BluetoothPbapVcardManager(mContext);
        mMissedCallSize = mVcardManager.getPhonebookSize(ContentType.MISSED_CALL_HISTORY);
        if (D) Log.d(TAG, "Initialize mMissedCallSize=" + mMissedCallSize);
    }
    @Override
    public int onConnect(final HeaderSet request, HeaderSet reply) {
        if (V) logHeader(request);
        try {
            byte[] uuid = (byte[])request.getHeader(HeaderSet.TARGET);
            if (uuid == null) {
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
            if (D) Log.d(TAG, "onConnect(): uuid=" + Arrays.toString(uuid));
            if (uuid.length != UUID_LENGTH) {
                Log.w(TAG, "Wrong UUID length");
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
            for (int i = 0; i < UUID_LENGTH; i++) {
                if (uuid[i] != PBAP_TARGET[i]) {
                    Log.w(TAG, "Wrong UUID");
                    return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
                }
            }
            reply.setHeader(HeaderSet.WHO, uuid);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        try {
            byte[] remote = (byte[])request.getHeader(HeaderSet.WHO);
            if (remote != null) {
                if (D) Log.d(TAG, "onConnect(): remote=" + Arrays.toString(remote));
                reply.setHeader(HeaderSet.TARGET, remote);
            }
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        if (V) Log.v(TAG, "onConnect(): uuid is ok, will send out " +
                "MSG_SESSION_ESTABLISHED msg.");
        Message msg = Message.obtain(mCallback);
        msg.what = BluetoothPbapService.MSG_SESSION_ESTABLISHED;
        msg.sendToTarget();
        return ResponseCodes.OBEX_HTTP_OK;
    }
    @Override
    public void onDisconnect(final HeaderSet req, final HeaderSet resp) {
        if (D) Log.d(TAG, "onDisconnect(): enter");
        if (V) logHeader(req);
        resp.responseCode = ResponseCodes.OBEX_HTTP_OK;
        if (mCallback != null) {
            Message msg = Message.obtain(mCallback);
            msg.what = BluetoothPbapService.MSG_SESSION_DISCONNECTED;
            msg.sendToTarget();
            if (V) Log.v(TAG, "onDisconnect(): msg MSG_SESSION_DISCONNECTED sent out.");
        }
    }
    @Override
    public int onAbort(HeaderSet request, HeaderSet reply) {
        if (D) Log.d(TAG, "onAbort(): enter.");
        sIsAborted = true;
        return ResponseCodes.OBEX_HTTP_OK;
    }
    @Override
    public int onPut(final Operation op) {
        if (D) Log.d(TAG, "onPut(): not support PUT request.");
        return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
    }
    @Override
    public int onSetPath(final HeaderSet request, final HeaderSet reply, final boolean backup,
            final boolean create) {
        if (V) logHeader(request);
        if (D) Log.d(TAG, "before setPath, mCurrentPath ==  " + mCurrentPath);
        String current_path_tmp = mCurrentPath;
        String tmp_path = null;
        try {
            tmp_path = (String)request.getHeader(HeaderSet.NAME);
        } catch (IOException e) {
            Log.e(TAG, "Get name header fail");
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        if (D) Log.d(TAG, "backup=" + backup + " create=" + create + " name=" + tmp_path);
        if (backup) {
            if (current_path_tmp.length() != 0) {
                current_path_tmp = current_path_tmp.substring(0,
                        current_path_tmp.lastIndexOf("/"));
            }
        } else {
            if (tmp_path == null) {
                current_path_tmp = "";
            } else {
                current_path_tmp = current_path_tmp + "/" + tmp_path;
            }
        }
        if ((current_path_tmp.length() != 0) && (!isLegalPath(current_path_tmp))) {
            if (create) {
                Log.w(TAG, "path create is forbidden!");
                return ResponseCodes.OBEX_HTTP_FORBIDDEN;
            } else {
                Log.w(TAG, "path is not legal");
                return ResponseCodes.OBEX_HTTP_NOT_FOUND;
            }
        }
        mCurrentPath = current_path_tmp;
        if (V) Log.v(TAG, "after setPath, mCurrentPath ==  " + mCurrentPath);
        return ResponseCodes.OBEX_HTTP_OK;
    }
    @Override
    public void onClose() {
        if (mCallback != null) {
            Message msg = Message.obtain(mCallback);
            msg.what = BluetoothPbapService.MSG_SERVERSESSION_CLOSE;
            msg.sendToTarget();
            if (D) Log.d(TAG, "onClose(): msg MSG_SERVERSESSION_CLOSE sent out.");
        }
    }
    @Override
    public int onGet(Operation op) {
        sIsAborted = false;
        HeaderSet request = null;
        HeaderSet reply = new HeaderSet();
        String type = "";
        String name = "";
        byte[] appParam = null;
        AppParamValue appParamValue = new AppParamValue();
        try {
            request = op.getReceivedHeader();
            type = (String)request.getHeader(HeaderSet.TYPE);
            name = (String)request.getHeader(HeaderSet.NAME);
            appParam = (byte[])request.getHeader(HeaderSet.APPLICATION_PARAMETER);
        } catch (IOException e) {
            Log.e(TAG, "request headers error");
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        if (V) logHeader(request);
        if (D) Log.d(TAG, "OnGet type is " + type + "; name is " + name);
        if (type == null) {
            return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
        }
        boolean validName = true;
        if (TextUtils.isEmpty(name)) {
            validName = false;
        }
        if (!validName || (validName && type.equals(TYPE_VCARD))) {
            if (D) Log.d(TAG, "Guess what carkit actually want from current path (" +
                    mCurrentPath + ")");
            if (mCurrentPath.equals(PB_PATH)) {
                appParamValue.needTag = ContentType.PHONEBOOK;
            } else if (mCurrentPath.equals(ICH_PATH)) {
                appParamValue.needTag = ContentType.INCOMING_CALL_HISTORY;
            } else if (mCurrentPath.equals(OCH_PATH)) {
                appParamValue.needTag = ContentType.OUTGOING_CALL_HISTORY;
            } else if (mCurrentPath.equals(MCH_PATH)) {
                appParamValue.needTag = ContentType.MISSED_CALL_HISTORY;
                mNeedNewMissedCallsNum = true;
            } else if (mCurrentPath.equals(CCH_PATH)) {
                appParamValue.needTag = ContentType.COMBINED_CALL_HISTORY;
            } else {
                Log.w(TAG, "mCurrentpath is not valid path!!!");
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
            if (D) Log.v(TAG, "onGet(): appParamValue.needTag=" + appParamValue.needTag);
        } else {
            if (name.contains(SIM1.subSequence(0, SIM1.length()))) {
                Log.w(TAG, "Not support access SIM card info!");
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
            if (name.contains(PB.subSequence(0, PB.length()))) {
                appParamValue.needTag = ContentType.PHONEBOOK;
                if (D) Log.v(TAG, "download phonebook request");
            } else if (name.contains(ICH.subSequence(0, ICH.length()))) {
                appParamValue.needTag = ContentType.INCOMING_CALL_HISTORY;
                if (D) Log.v(TAG, "download incoming calls request");
            } else if (name.contains(OCH.subSequence(0, OCH.length()))) {
                appParamValue.needTag = ContentType.OUTGOING_CALL_HISTORY;
                if (D) Log.v(TAG, "download outgoing calls request");
            } else if (name.contains(MCH.subSequence(0, MCH.length()))) {
                appParamValue.needTag = ContentType.MISSED_CALL_HISTORY;
                mNeedNewMissedCallsNum = true;
                if (D) Log.v(TAG, "download missed calls request");
            } else if (name.contains(CCH.subSequence(0, CCH.length()))) {
                appParamValue.needTag = ContentType.COMBINED_CALL_HISTORY;
                if (D) Log.v(TAG, "download combined calls request");
            } else {
                Log.w(TAG, "Input name doesn't contain valid info!!!");
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
        }
        if (!parseApplicationParameter(appParam, appParamValue)) {
            return ResponseCodes.OBEX_HTTP_BAD_REQUEST;
        }
        if (type.equals(TYPE_LISTING)) {
            return pullVcardListing(appParam, appParamValue, reply, op);
        }
        else if (type.equals(TYPE_VCARD)) {
            return pullVcardEntry(appParam, appParamValue, op, name, mCurrentPath);
        }
        else if (type.equals(TYPE_PB)) {
            return pullPhonebook(appParam, appParamValue, reply, op, name);
        } else {
            Log.w(TAG, "unknown type request!!!");
            return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
        }
    }
    private final boolean isLegalPath(final String str) {
        if (str.length() == 0) {
            return true;
        }
        for (int i = 0; i < LEGAL_PATH.length; i++) {
            if (str.equals(LEGAL_PATH[i])) {
                return true;
            }
        }
        return false;
    }
    private class AppParamValue {
        public int maxListCount;
        public int listStartOffset;
        public String searchValue;
        public String searchAttr;
        public String order;
        public int needTag;
        public boolean vcard21;
        public AppParamValue() {
            maxListCount = 0;
            listStartOffset = 0;
            searchValue = "";
            searchAttr = "";
            order = "";
            needTag = 0x00;
            vcard21 = true;
        }
        public void dump() {
            Log.i(TAG, "maxListCount=" + maxListCount + " listStartOffset=" + listStartOffset
                    + " searchValue=" + searchValue + " searchAttr=" + searchAttr + " needTag="
                    + needTag + " vcard21=" + vcard21 + " order=" + order);
        }
    }
    private final boolean parseApplicationParameter(final byte[] appParam,
            AppParamValue appParamValue) {
        int i = 0;
        boolean parseOk = true;
        while (i < appParam.length) {
            switch (appParam[i]) {
                case ApplicationParameter.TRIPLET_TAGID.FILTER_TAGID:
                    i += 2; 
                    i += ApplicationParameter.TRIPLET_LENGTH.FILTER_LENGTH;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.ORDER_TAGID:
                    i += 2; 
                    appParamValue.order = Byte.toString(appParam[i]);
                    i += ApplicationParameter.TRIPLET_LENGTH.ORDER_LENGTH;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.SEARCH_VALUE_TAGID:
                    i += 1; 
                    for (int k = 1; k <= appParam[i]; k++) {
                        appParamValue.searchValue += Byte.toString(appParam[i + k]);
                    }
                    i += appParam[i];
                    i += 1;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.SEARCH_ATTRIBUTE_TAGID:
                    i += 2;
                    appParamValue.searchAttr = Byte.toString(appParam[i]);
                    i += ApplicationParameter.TRIPLET_LENGTH.SEARCH_ATTRIBUTE_LENGTH;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.MAXLISTCOUNT_TAGID:
                    i += 2;
                    if (appParam[i] == 0 && appParam[i + 1] == 0) {
                        mNeedPhonebookSize = true;
                    } else {
                        int highValue = appParam[i] & 0xff;
                        int lowValue = appParam[i + 1] & 0xff;
                        appParamValue.maxListCount = highValue * 256 + lowValue;
                    }
                    i += ApplicationParameter.TRIPLET_LENGTH.MAXLISTCOUNT_LENGTH;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.LISTSTARTOFFSET_TAGID:
                    i += 2;
                    int highValue = appParam[i] & 0xff;
                    int lowValue = appParam[i + 1] & 0xff;
                    appParamValue.listStartOffset = highValue * 256 + lowValue;
                    i += ApplicationParameter.TRIPLET_LENGTH.LISTSTARTOFFSET_LENGTH;
                    break;
                case ApplicationParameter.TRIPLET_TAGID.FORMAT_TAGID:
                    i += 2;
                    if (appParam[i] != 0) {
                        appParamValue.vcard21 = false;
                    }
                    i += ApplicationParameter.TRIPLET_LENGTH.FORMAT_LENGTH;
                    break;
                default:
                    parseOk = false;
                    Log.e(TAG, "Parse Application Parameter error");
                    break;
            }
        }
        if (D) appParamValue.dump();
        return parseOk;
    }
    private final int sendVcardListingXml(final int type, Operation op,
            final int maxListCount, final int listStartOffset, final String searchValue,
            String searchAttr) {
        StringBuilder result = new StringBuilder();
        int itemsFound = 0;
        result.append("<?xml version=\"1.0\"?>");
        result.append("<!DOCTYPE vcard-listing SYSTEM \"vcard-listing.dtd\">");
        result.append("<vCard-listing version=\"1.0\">");
        if (type == ContentType.PHONEBOOK) {
            if (searchAttr.equals("0")) {
                ArrayList<String> nameList = mVcardManager.getPhonebookNameList(mOrderBy );
                int requestSize = nameList.size() >= maxListCount ? maxListCount : nameList.size();
                int startPoint = listStartOffset;
                int endPoint = startPoint + requestSize;
                if (endPoint > nameList.size()) {
                    endPoint = nameList.size();
                }
                if (D) Log.d(TAG, "search by name, size=" + requestSize + " offset=" +
                        listStartOffset + " searchValue=" + searchValue);
                if (searchValue == null || searchValue.trim().length() == 0) {
                    for (int j = startPoint; j < endPoint; j++) {
                        result.append("<card handle=\"" + j + ".vcf\" name=\"" + nameList.get(j)
                                + "\"" + "/>");
                        itemsFound++;
                    }
                } else {
                    for (int j = startPoint; j < endPoint; j++) {
                        if (nameList.get(j).startsWith(searchValue.trim())) {
                            itemsFound++;
                            result.append("<card handle=\"" + j + ".vcf\" name=\""
                                    + nameList.get(j) + "\"" + "/>");
                        }
                    }
                }
            }
            else if (searchAttr.equals("1")) {
                ArrayList<String> numberList = mVcardManager.getPhonebookNumberList();
                int requestSize = numberList.size() >= maxListCount ? maxListCount : numberList
                        .size();
                int startPoint = listStartOffset;
                int endPoint = startPoint + requestSize;
                if (endPoint > numberList.size()) {
                    endPoint = numberList.size();
                }
                if (D) Log.d(TAG, "search by number, size=" + requestSize + " offset="
                            + listStartOffset + " searchValue=" + searchValue);
                if (searchValue == null || searchValue.trim().length() == 0) {
                    for (int j = startPoint; j < endPoint; j++) {
                        result.append("<card handle=\"" + j + ".vcf\" number=\""
                                + numberList.get(j) + "\"" + "/>");
                        itemsFound++;
                    }
                } else {
                    for (int j = startPoint; j < endPoint; j++) {
                        if (numberList.get(j).startsWith(searchValue.trim())) {
                            itemsFound++;
                            result.append("<card handle=\"" + j + ".vcf\" number=\""
                                    + numberList.get(j) + "\"" + "/>");
                        }
                    }
                }
            }
            else {
                return ResponseCodes.OBEX_HTTP_PRECON_FAILED;
            }
        }
        else {
            ArrayList<String> nameList = mVcardManager.loadCallHistoryList(type);
            int requestSize = nameList.size() >= maxListCount ? maxListCount : nameList.size();
            int startPoint = listStartOffset;
            int endPoint = startPoint + requestSize;
            if (endPoint > nameList.size()) {
                endPoint = nameList.size();
            }
            if (D) Log.d(TAG, "call log list, size=" + requestSize + " offset=" + listStartOffset);
            for (int j = startPoint; j < endPoint; j++) {
                result.append("<card handle=\"" + (j + 1) + ".vcf\" name=\"" + nameList.get(j)
                        + "\"" + "/>");
                itemsFound++;
            }
        }
        result.append("</vCard-listing>");
        if (V) Log.v(TAG, "itemsFound =" + itemsFound);
        return pushBytes(op, result.toString());
    }
    private final int pushHeader(final Operation op, final HeaderSet reply) {
        OutputStream outputStream = null;
        if (D) Log.d(TAG, "Push Header");
        if (D) Log.d(TAG, reply.toString());
        int pushResult = ResponseCodes.OBEX_HTTP_OK;
        try {
            op.sendHeaders(reply);
            outputStream = op.openOutputStream();
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            pushResult = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        } finally {
            if (!closeStream(outputStream, op)) {
                pushResult = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            }
        }
        return pushResult;
    }
    private final int pushBytes(Operation op, final String vcardString) {
        if (vcardString == null) {
            Log.w(TAG, "vcardString is null!");
            return ResponseCodes.OBEX_HTTP_OK;
        }
        int vcardStringLen = vcardString.length();
        if (D) Log.d(TAG, "Send Data: len=" + vcardStringLen);
        OutputStream outputStream = null;
        int pushResult = ResponseCodes.OBEX_HTTP_OK;
        try {
            outputStream = op.openOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "open outputstrem failed" + e.toString());
            return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        int position = 0;
        long timestamp = 0;
        int outputBufferSize = op.getMaxPacketSize();
        if (V) Log.v(TAG, "outputBufferSize = " + outputBufferSize);
        while (position != vcardStringLen) {
            if (sIsAborted) {
                ((ServerOperation)op).isAborted = true;
                sIsAborted = false;
                break;
            }
            if (V) timestamp = System.currentTimeMillis();
            int readLength = outputBufferSize;
            if (vcardStringLen - position < outputBufferSize) {
                readLength = vcardStringLen - position;
            }
            String subStr = vcardString.substring(position, position + readLength);
            try {
                outputStream.write(subStr.getBytes(), 0, readLength);
            } catch (IOException e) {
                Log.e(TAG, "write outputstrem failed" + e.toString());
                pushResult = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
                break;
            }
            if (V) {
                Log.v(TAG, "Sending vcard String position = " + position + " readLength "
                        + readLength + " bytes took " + (System.currentTimeMillis() - timestamp)
                        + " ms");
            }
            position += readLength;
        }
        if (V) Log.v(TAG, "Send Data complete!");
        if (!closeStream(outputStream, op)) {
            pushResult = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
        return pushResult;
    }
    private final int handleAppParaForResponse(AppParamValue appParamValue, int size,
            HeaderSet reply, Operation op) {
        byte[] misnum = new byte[1];
        ApplicationParameter ap = new ApplicationParameter();
        if (mNeedPhonebookSize) {
            if (V) Log.v(TAG, "Need Phonebook size in response header.");
            mNeedPhonebookSize = false;
            byte[] pbsize = new byte[2];
            pbsize[0] = (byte)((size / 256) & 0xff);
            pbsize[1] = (byte)((size % 256) & 0xff);
            ap.addAPPHeader(ApplicationParameter.TRIPLET_TAGID.PHONEBOOKSIZE_TAGID,
                    ApplicationParameter.TRIPLET_LENGTH.PHONEBOOKSIZE_LENGTH, pbsize);
            if (mNeedNewMissedCallsNum) {
                int nmnum = size - mMissedCallSize;
                mMissedCallSize = size;
                nmnum = nmnum > 0 ? nmnum : 0;
                misnum[0] = (byte)nmnum;
                ap.addAPPHeader(ApplicationParameter.TRIPLET_TAGID.NEWMISSEDCALLS_TAGID,
                        ApplicationParameter.TRIPLET_LENGTH.NEWMISSEDCALLS_LENGTH, misnum);
                if (D) Log.d(TAG, "handleAppParaForResponse(): mNeedNewMissedCallsNum=true,  num= "
                            + nmnum);
            }
            reply.setHeader(HeaderSet.APPLICATION_PARAMETER, ap.getAPPparam());
            if (D) Log.d(TAG, "Send back Phonebook size only, without body info! Size= " + size);
            return pushHeader(op, reply);
        }
        if (mNeedNewMissedCallsNum) {
            if (V) Log.v(TAG, "Need new missed call num in response header.");
            mNeedNewMissedCallsNum = false;
            int nmnum = size - mMissedCallSize;
            mMissedCallSize = size;
            nmnum = nmnum > 0 ? nmnum : 0;
            misnum[0] = (byte)nmnum;
            ap.addAPPHeader(ApplicationParameter.TRIPLET_TAGID.NEWMISSEDCALLS_TAGID,
                    ApplicationParameter.TRIPLET_LENGTH.NEWMISSEDCALLS_LENGTH, misnum);
            reply.setHeader(HeaderSet.APPLICATION_PARAMETER, ap.getAPPparam());
            if (D) Log.d(TAG, "handleAppParaForResponse(): mNeedNewMissedCallsNum=true,  num= "
                        + nmnum);
            try {
                op.sendHeaders(reply);
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
            }
        }
        return NEED_SEND_BODY;
    }
    private final int pullVcardListing(byte[] appParam, AppParamValue appParamValue,
            HeaderSet reply, Operation op) {
        String searchAttr = appParamValue.searchAttr.trim();
        if (searchAttr == null || searchAttr.length() == 0) {
            appParamValue.searchAttr = "0";
            if (D) Log.d(TAG, "searchAttr is not set by PCE, assume search by name by default");
        } else if (!searchAttr.equals("0") && !searchAttr.equals("1")) {
            Log.w(TAG, "search attr not supported");
            if (searchAttr.equals("2")) {
                Log.w(TAG, "do not support search by sound");
                return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
            }
            return ResponseCodes.OBEX_HTTP_PRECON_FAILED;
        } else {
            Log.i(TAG, "searchAttr is valid: " + searchAttr);
        }
        int size = mVcardManager.getPhonebookSize(appParamValue.needTag);
        int needSendBody = handleAppParaForResponse(appParamValue, size, reply, op);
        if (needSendBody != NEED_SEND_BODY) {
            return needSendBody;
        }
        if (size == 0) {
            if (V) Log.v(TAG, "PhonebookSize is 0, return.");
            return ResponseCodes.OBEX_HTTP_OK;
        }
        String orderPara = appParamValue.order.trim();
        if (TextUtils.isEmpty(orderPara)) {
            appParamValue.order = "0";
            if (D) Log.d(TAG, "Order parameter is not set by PCE. " +
                       "Assume order by 'Indexed' by default");
        } else if (!orderPara.equals("0") && !orderPara.equals("1")) {
            if (V) Log.v(TAG, "Order parameter is not supported: " + appParamValue.order);
            if (orderPara.equals("2")) {
                Log.w(TAG, "Do not support order by sound");
                return ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED;
            }
            return ResponseCodes.OBEX_HTTP_PRECON_FAILED;
        } else {
            Log.i(TAG, "Order parameter is valid: " + orderPara);
        }
        if (orderPara.equals("0")) {
            mOrderBy = ORDER_BY_INDEXED;
        } else if (orderPara.equals("1")) {
            mOrderBy = ORDER_BY_ALPHABETICAL;
        }
        int sendResult = sendVcardListingXml(appParamValue.needTag, op, appParamValue.maxListCount,
                appParamValue.listStartOffset, appParamValue.searchValue,
                appParamValue.searchAttr);
        return sendResult;
    }
    private final int pullVcardEntry(byte[] appParam, AppParamValue appParamValue,
            Operation op, final String name, final String current_path) {
        if (name == null || name.length() < VCARD_NAME_SUFFIX_LENGTH) {
            if (D) Log.d(TAG, "Name is Null, or the length of name < 5 !");
            return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
        }
        String strIndex = name.substring(0, name.length() - VCARD_NAME_SUFFIX_LENGTH + 1);
        int intIndex = 0;
        if (strIndex.trim().length() != 0) {
            try {
                intIndex = Integer.parseInt(strIndex);
            } catch (NumberFormatException e) {
                Log.e(TAG, "catch number format exception " + e.toString());
                return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
            }
        }
        int size = mVcardManager.getPhonebookSize(appParamValue.needTag);
        if (size == 0) {
            if (V) Log.v(TAG, "PhonebookSize is 0, return.");
            return ResponseCodes.OBEX_HTTP_OK;
        }
        boolean vcard21 = appParamValue.vcard21;
        if (appParamValue.needTag == 0) {
            Log.w(TAG, "wrong path!");
            return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
        } else if (appParamValue.needTag == ContentType.PHONEBOOK) {
            if (intIndex < 0 || intIndex >= size) {
                Log.w(TAG, "The requested vcard is not acceptable! name= " + name);
                return ResponseCodes.OBEX_HTTP_OK;
            } else if (intIndex == 0) {
                String ownerVcard = mVcardManager.getOwnerPhoneNumberVcard(vcard21);
                return pushBytes(op, ownerVcard);
            } else {
                return mVcardManager.composeAndSendPhonebookOneVcard(op, intIndex, vcard21, null,
                        mOrderBy );
            }
        } else {
            if (intIndex <= 0 || intIndex > size) {
                Log.w(TAG, "The requested vcard is not acceptable! name= " + name);
                return ResponseCodes.OBEX_HTTP_OK;
            }
            if (intIndex >= 1) {
                return mVcardManager.composeAndSendCallLogVcards(appParamValue.needTag, op,
                        intIndex, intIndex, vcard21);
            }
        }
        return ResponseCodes.OBEX_HTTP_OK;
    }
    private final int pullPhonebook(byte[] appParam, AppParamValue appParamValue, HeaderSet reply,
            Operation op, final String name) {
        if (name != null) {
            int dotIndex = name.indexOf(".");
            String vcf = "vcf";
            if (dotIndex >= 0 && dotIndex <= name.length()) {
                if (name.regionMatches(dotIndex + 1, vcf, 0, vcf.length()) == false) {
                    Log.w(TAG, "name is not .vcf");
                    return ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE;
                }
            }
        } 
        int pbSize = mVcardManager.getPhonebookSize(appParamValue.needTag);
        int needSendBody = handleAppParaForResponse(appParamValue, pbSize, reply, op);
        if (needSendBody != NEED_SEND_BODY) {
            return needSendBody;
        }
        if (pbSize == 0) {
            if (V) Log.v(TAG, "PhonebookSize is 0, return.");
            return ResponseCodes.OBEX_HTTP_OK;
        }
        int requestSize = pbSize >= appParamValue.maxListCount ? appParamValue.maxListCount
                : pbSize;
        int startPoint = appParamValue.listStartOffset;
        if (startPoint < 0 || startPoint >= pbSize) {
            Log.w(TAG, "listStartOffset is not correct! " + startPoint);
            return ResponseCodes.OBEX_HTTP_OK;
        }
        int endPoint = startPoint + requestSize - 1;
        if (endPoint > pbSize - 1) {
            endPoint = pbSize - 1;
        }
        if (D) Log.d(TAG, "pullPhonebook(): requestSize=" + requestSize + " startPoint=" +
                startPoint + " endPoint=" + endPoint);
        String result = null;
        boolean vcard21 = appParamValue.vcard21;
        if (appParamValue.needTag == BluetoothPbapObexServer.ContentType.PHONEBOOK) {
            if (startPoint == 0) {
                String ownerVcard = mVcardManager.getOwnerPhoneNumberVcard(vcard21);
                if (endPoint == 0) {
                    return pushBytes(op, ownerVcard);
                } else {
                    return mVcardManager.composeAndSendPhonebookVcards(op, 1, endPoint, vcard21,
                            ownerVcard);
                }
            } else {
                return mVcardManager.composeAndSendPhonebookVcards(op, startPoint, endPoint,
                        vcard21, null);
            }
        } else {
            return mVcardManager.composeAndSendCallLogVcards(appParamValue.needTag, op,
                    startPoint + 1, endPoint + 1, vcard21);
        }
    }
    public static boolean closeStream(final OutputStream out, final Operation op) {
        boolean returnvalue = true;
        try {
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "outputStream close failed" + e.toString());
            returnvalue = false;
        }
        try {
            if (op != null) {
                op.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "operation close failed" + e.toString());
            returnvalue = false;
        }
        return returnvalue;
    }
    public final void onAuthenticationFailure(final byte[] userName) {
    }
    public static final String createSelectionPara(final int type) {
        String selection = null;
        switch (type) {
            case ContentType.INCOMING_CALL_HISTORY:
                selection = Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE;
                break;
            case ContentType.OUTGOING_CALL_HISTORY:
                selection = Calls.TYPE + "=" + CallLog.Calls.OUTGOING_TYPE;
                break;
            case ContentType.MISSED_CALL_HISTORY:
                selection = Calls.TYPE + "=" + CallLog.Calls.MISSED_TYPE;
                break;
            default:
                break;
        }
        if (V) Log.v(TAG, "Call log selection: " + selection);
        return selection;
    }
    public static final void logHeader(HeaderSet hs) {
        Log.v(TAG, "Dumping HeaderSet " + hs.toString());
        try {
            Log.v(TAG, "COUNT : " + hs.getHeader(HeaderSet.COUNT));
            Log.v(TAG, "NAME : " + hs.getHeader(HeaderSet.NAME));
            Log.v(TAG, "TYPE : " + hs.getHeader(HeaderSet.TYPE));
            Log.v(TAG, "LENGTH : " + hs.getHeader(HeaderSet.LENGTH));
            Log.v(TAG, "TIME_ISO_8601 : " + hs.getHeader(HeaderSet.TIME_ISO_8601));
            Log.v(TAG, "TIME_4_BYTE : " + hs.getHeader(HeaderSet.TIME_4_BYTE));
            Log.v(TAG, "DESCRIPTION : " + hs.getHeader(HeaderSet.DESCRIPTION));
            Log.v(TAG, "TARGET : " + hs.getHeader(HeaderSet.TARGET));
            Log.v(TAG, "HTTP : " + hs.getHeader(HeaderSet.HTTP));
            Log.v(TAG, "WHO : " + hs.getHeader(HeaderSet.WHO));
            Log.v(TAG, "OBJECT_CLASS : " + hs.getHeader(HeaderSet.OBJECT_CLASS));
            Log.v(TAG, "APPLICATION_PARAMETER : " + hs.getHeader(HeaderSet.APPLICATION_PARAMETER));
        } catch (IOException e) {
            Log.e(TAG, "dump HeaderSet error " + e);
        }
    }
}

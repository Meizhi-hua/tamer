public class UsimPhoneBookManager extends Handler implements IccConstants {
    private static final String LOG_TAG = "GSM";
    private static final boolean DBG = true;
    private PbrFile mPbrFile;
    private Boolean mIsPbrPresent;
    private PhoneBase mPhone;
    private AdnRecordCache mAdnCache;
    private Object mLock = new Object();
    private ArrayList<AdnRecord> mPhoneBookRecords;
    private boolean mEmailPresentInIap = false;
    private int mEmailTagNumberInIap = 0;
    private ArrayList<byte[]> mIapFileRecord;
    private ArrayList<byte[]> mEmailFileRecord;
    private Map<Integer, ArrayList<String>> mEmailsForAdnRec;
    private static final int EVENT_PBR_LOAD_DONE = 1;
    private static final int EVENT_USIM_ADN_LOAD_DONE = 2;
    private static final int EVENT_IAP_LOAD_DONE = 3;
    private static final int EVENT_EMAIL_LOAD_DONE = 4;
    private static final int USIM_TYPE1_TAG   = 0xA8;
    private static final int USIM_TYPE2_TAG   = 0xA9;
    private static final int USIM_TYPE3_TAG   = 0xAA;
    private static final int USIM_EFADN_TAG   = 0xC0;
    private static final int USIM_EFIAP_TAG   = 0xC1;
    private static final int USIM_EFEXT1_TAG  = 0xC2;
    private static final int USIM_EFSNE_TAG   = 0xC3;
    private static final int USIM_EFANR_TAG   = 0xC4;
    private static final int USIM_EFPBC_TAG   = 0xC5;
    private static final int USIM_EFGRP_TAG   = 0xC6;
    private static final int USIM_EFAAS_TAG   = 0xC7;
    private static final int USIM_EFGSD_TAG   = 0xC8;
    private static final int USIM_EFUID_TAG   = 0xC9;
    private static final int USIM_EFEMAIL_TAG = 0xCA;
    private static final int USIM_EFCCP1_TAG  = 0xCB;
    public UsimPhoneBookManager(PhoneBase phone, AdnRecordCache cache) {
        mPhone = phone;
        mPhoneBookRecords = new ArrayList<AdnRecord>();
        mPbrFile = null;
        mIsPbrPresent = true;
        mAdnCache = cache;
    }
    public void reset() {
        mPhoneBookRecords.clear();
        mIapFileRecord = null;
        mEmailFileRecord = null;
        mPbrFile = null;
        mIsPbrPresent = true;
    }
    public ArrayList<AdnRecord> loadEfFilesFromUsim() {
        synchronized (mLock) {
            if (!mPhoneBookRecords.isEmpty()) return mPhoneBookRecords;
            if (!mIsPbrPresent) return null;
            if (mPbrFile == null) {
                readPbrFileAndWait();
            }
            if (mPbrFile == null) return null;
            int numRecs = mPbrFile.mFileIds.size();
            for (int i = 0; i < numRecs; i++) {
                readAdnFileAndWait(i);
                readEmailFileAndWait(i);
            }
        }
        return mPhoneBookRecords;
    }
    private void readPbrFileAndWait() {
        mPhone.getIccFileHandler().loadEFLinearFixedAll(EF_PBR, obtainMessage(EVENT_PBR_LOAD_DONE));
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }
    private void readEmailFileAndWait(int recNum) {
        Map <Integer,Integer> fileIds;
        fileIds = mPbrFile.mFileIds.get(recNum);
        if (fileIds == null) return;
        if (fileIds.containsKey(USIM_EFEMAIL_TAG)) {
            int efid = fileIds.get(USIM_EFEMAIL_TAG);
            if (mEmailPresentInIap) {
                readIapFileAndWait(fileIds.get(USIM_EFIAP_TAG));
                if (mIapFileRecord == null) {
                    Log.e(LOG_TAG, "Error: IAP file is empty");
                    return;
                }
            }
            mPhone.getIccFileHandler().loadEFLinearFixedAll(fileIds.get(USIM_EFEMAIL_TAG),
                    obtainMessage(EVENT_EMAIL_LOAD_DONE));
            try {
                mLock.wait();
            } catch (InterruptedException e) {
                Log.e(LOG_TAG, "Interrupted Exception in readEmailFileAndWait");
            }
            if (mEmailFileRecord == null) {
                Log.e(LOG_TAG, "Error: Email file is empty");
                return;
            }
            updatePhoneAdnRecord();
        }
    }
    private void readIapFileAndWait(int efid) {
        mPhone.getIccFileHandler().loadEFLinearFixedAll(efid, obtainMessage(EVENT_IAP_LOAD_DONE));
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readIapFileAndWait");
        }
    }
    private void updatePhoneAdnRecord() {
        if (mEmailFileRecord == null) return;
        int numAdnRecs = mPhoneBookRecords.size();
        if (mIapFileRecord != null) {
            for (int i = 0; i < numAdnRecs; i++) {
                byte[] record = null;
                try {
                    record = mIapFileRecord.get(i);
                } catch (IndexOutOfBoundsException e) {
                    Log.e(LOG_TAG, "Error: Improper ICC card: No IAP record for ADN, continuing");
                    break;
                }
                int recNum = record[mEmailTagNumberInIap];
                if (recNum != -1) {
                    String[] emails = new String[1];
                    emails[0] = readEmailRecord(recNum - 1);
                    AdnRecord rec = mPhoneBookRecords.get(i);
                    if (rec != null) {
                        rec.setEmails(emails);
                    } else {
                        rec = new AdnRecord("", "", emails);
                    }
                    mPhoneBookRecords.set(i, rec);
                }
            }
        }
        int len = mPhoneBookRecords.size();
        if (mEmailsForAdnRec == null) {
            parseType1EmailFile(len);
        }
        for (int i = 0; i < numAdnRecs; i++) {
            ArrayList<String> emailList = null;
            try {
                emailList = mEmailsForAdnRec.get(i);
            } catch (IndexOutOfBoundsException e) {
                break;
            }
            if (emailList == null) continue;
            AdnRecord rec = mPhoneBookRecords.get(i);
            String[] emails = new String[emailList.size()];
            System.arraycopy(emailList.toArray(), 0, emails, 0, emailList.size());
            rec.setEmails(emails);
            mPhoneBookRecords.set(i, rec);
        }
    }
    void parseType1EmailFile(int numRecs) {
        mEmailsForAdnRec = new HashMap<Integer, ArrayList<String>>();
        byte[] emailRec = null;
        for (int i = 0; i < numRecs; i++) {
            try {
                emailRec = mEmailFileRecord.get(i);
            } catch (IndexOutOfBoundsException e) {
                Log.e(LOG_TAG, "Error: Improper ICC card: No email record for ADN, continuing");
                break;
            }
            int adnRecNum = emailRec[emailRec.length - 1];
            if (adnRecNum == -1) {
                continue;
            }
            String email = readEmailRecord(i);
            if (email == null || email.equals("")) {
                continue;
            }
            ArrayList<String> val = mEmailsForAdnRec.get(adnRecNum - 1);
            if (val == null) {
                val = new ArrayList<String>();
            }
            val.add(email);
            mEmailsForAdnRec.put(adnRecNum - 1, val);
        }
    }
    private String readEmailRecord(int recNum) {
        byte[] emailRec = null;
        try {
            emailRec = mEmailFileRecord.get(recNum);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
        String email = IccUtils.adnStringFieldToString(emailRec, 0, emailRec.length - 2);
        return email;
    }
    private void readAdnFileAndWait(int recNum) {
        Map <Integer,Integer> fileIds;
        fileIds = mPbrFile.mFileIds.get(recNum);
        if (fileIds == null || fileIds.isEmpty()) return;
        mAdnCache.requestLoadAllAdnLike(fileIds.get(USIM_EFADN_TAG),
            fileIds.get(USIM_EFEXT1_TAG), obtainMessage(EVENT_USIM_ADN_LOAD_DONE));
        try {
            mLock.wait();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted Exception in readAdnFileAndWait");
        }
    }
    private void createPbrFile(ArrayList<byte[]> records) {
        if (records == null) {
            mPbrFile = null;
            mIsPbrPresent = false;
            return;
        }
        mPbrFile = new PbrFile(records);
    }
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        switch(msg.what) {
        case EVENT_PBR_LOAD_DONE:
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                createPbrFile((ArrayList<byte[]>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_USIM_ADN_LOAD_DONE:
            log("Loading USIM ADN records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                mPhoneBookRecords.addAll((ArrayList<AdnRecord>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_IAP_LOAD_DONE:
            log("Loading USIM IAP records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                mIapFileRecord = ((ArrayList<byte[]>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        case EVENT_EMAIL_LOAD_DONE:
            log("Loading USIM Email records done");
            ar = (AsyncResult) msg.obj;
            if (ar.exception == null) {
                mEmailFileRecord = ((ArrayList<byte[]>)ar.result);
            }
            synchronized (mLock) {
                mLock.notify();
            }
            break;
        }
    }
    private class PbrFile {
        HashMap<Integer,Map<Integer,Integer>> mFileIds;
        PbrFile(ArrayList<byte[]> records) {
            mFileIds = new HashMap<Integer, Map<Integer, Integer>>();
            SimTlv recTlv;
            int recNum = 0;
            for (byte[] record: records) {
                recTlv = new SimTlv(record, 0, record.length);
                parseTag(recTlv, recNum);
                recNum ++;
            }
        }
        void parseTag(SimTlv tlv, int recNum) {
            SimTlv tlvEf;
            int tag;
            byte[] data;
            Map<Integer, Integer> val = new HashMap<Integer, Integer>();
            do {
                tag = tlv.getTag();
                switch(tag) {
                case USIM_TYPE1_TAG: 
                case USIM_TYPE3_TAG: 
                case USIM_TYPE2_TAG: 
                    data = tlv.getData();
                    tlvEf = new SimTlv(data, 0, data.length);
                    parseEf(tlvEf, val, tag);
                    break;
                }
            } while (tlv.nextObject());
            mFileIds.put(recNum, val);
        }
        void parseEf(SimTlv tlv, Map<Integer, Integer> val, int parentTag) {
            int tag;
            byte[] data;
            int tagNumberWithinParentTag = 0;
            do {
                tag = tlv.getTag();
                if (parentTag == USIM_TYPE2_TAG && tag == USIM_EFEMAIL_TAG) {
                    mEmailPresentInIap = true;
                    mEmailTagNumberInIap = tagNumberWithinParentTag;
                }
                switch(tag) {
                    case USIM_EFEMAIL_TAG:
                    case USIM_EFADN_TAG:
                    case USIM_EFEXT1_TAG:
                    case USIM_EFANR_TAG:
                    case USIM_EFPBC_TAG:
                    case USIM_EFGRP_TAG:
                    case USIM_EFAAS_TAG:
                    case USIM_EFGSD_TAG:
                    case USIM_EFUID_TAG:
                    case USIM_EFCCP1_TAG:
                    case USIM_EFIAP_TAG:
                    case USIM_EFSNE_TAG:
                        data = tlv.getData();
                        int efid = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
                        val.put(tag, efid);
                        break;
                }
                tagNumberWithinParentTag ++;
            } while(tlv.nextObject());
        }
    }
    private void log(String msg) {
        if(DBG) Log.d(LOG_TAG, msg);
    }
}

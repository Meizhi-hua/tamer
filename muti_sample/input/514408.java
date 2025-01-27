public class BluetoothOppManager {
    private static final String TAG = "BluetoothOppManager";
    private static final boolean D = Constants.DEBUG;
    private static final boolean V = Constants.VERBOSE;
    private static BluetoothOppManager INSTANCE;
    private static Object INSTANCE_LOCK = new Object();
    private boolean mInitialized;
    private Context mContext;
    private BluetoothAdapter mAdapter;
    private String mMimeTypeOfSendigFile;
    private String mUriOfSendingFile;
    private String mMimeTypeOfSendigFiles;
    private ArrayList<Uri> mUrisOfSendingFiles;
    private static final String OPP_PREFERENCE_FILE = "OPPMGR";
    private static final String SENDING_FLAG = "SENDINGFLAG";
    private static final String MIME_TYPE = "MIMETYPE";
    private static final String FILE_URI = "FILE_URI";
    private static final String MIME_TYPE_MULTIPLE = "MIMETYPE_MULTIPLE";
    private static final String FILE_URIS = "FILE_URIS";
    private static final String MULTIPLE_FLAG = "MULTIPLE_FLAG";
    private static final String ARRAYLIST_ITEM_SEPERATOR = ";";
    private static final int ALLOWED_INSERT_SHARE_THREAD_NUMBER = 3;
    public boolean mSendingFlag;
    public boolean mMultipleFlag;
    private int mfileNumInBatch;
    private int mInsertShareThreadNum = 0;
    public static BluetoothOppManager getInstance(Context context) {
        synchronized (INSTANCE_LOCK) {
            if (INSTANCE == null) {
                INSTANCE = new BluetoothOppManager();
            }
            INSTANCE.init(context);
            return INSTANCE;
        }
    }
    private boolean init(Context context) {
        if (mInitialized)
            return true;
        mInitialized = true;
        mContext = context;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mAdapter == null) {
            if (V) Log.v(TAG, "BLUETOOTH_SERVICE is not started! ");
        }
        restoreApplicationData();
        return true;
    }
    private void restoreApplicationData() {
        SharedPreferences settings = mContext.getSharedPreferences(OPP_PREFERENCE_FILE, 0);
        mSendingFlag = settings.getBoolean(SENDING_FLAG, false);
        mMimeTypeOfSendigFile = settings.getString(MIME_TYPE, null);
        mUriOfSendingFile = settings.getString(FILE_URI, null);
        mMimeTypeOfSendigFiles = settings.getString(MIME_TYPE_MULTIPLE, null);
        mMultipleFlag = settings.getBoolean(MULTIPLE_FLAG, false);
        if (V) Log.v(TAG, "restoreApplicationData! " + mSendingFlag + mMultipleFlag
                    + mMimeTypeOfSendigFile + mUriOfSendingFile);
        String strUris = settings.getString(FILE_URIS, null);
        mUrisOfSendingFiles = new ArrayList<Uri>();
        if (strUris != null) {
            String[] splitUri = strUris.split(ARRAYLIST_ITEM_SEPERATOR);
            for (int i = 0; i < splitUri.length; i++) {
                mUrisOfSendingFiles.add(Uri.parse(splitUri[i]));
                if (V) Log.v(TAG, "Uri in batch:  " + Uri.parse(splitUri[i]));
            }
        }
        mContext.getSharedPreferences(OPP_PREFERENCE_FILE, 0).edit().clear().commit();
    }
    private void storeApplicationData() {
        SharedPreferences.Editor editor = mContext.getSharedPreferences(OPP_PREFERENCE_FILE, 0)
                .edit();
        editor.putBoolean(SENDING_FLAG, mSendingFlag).commit();
        editor.putBoolean(MULTIPLE_FLAG, mMultipleFlag).commit();
        if (mMultipleFlag) {
            editor.putString(MIME_TYPE_MULTIPLE, mMimeTypeOfSendigFiles).commit();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, count = mUrisOfSendingFiles.size(); i < count; i++) {
                Uri uriContent = mUrisOfSendingFiles.get(i);
                sb.append(uriContent);
                sb.append(ARRAYLIST_ITEM_SEPERATOR);
            }
            String strUris = sb.toString();
            editor.putString(FILE_URIS, strUris).commit();
            editor.remove(MIME_TYPE).commit();
            editor.remove(FILE_URI).commit();
        } else {
            editor.putString(MIME_TYPE, mMimeTypeOfSendigFile).commit();
            editor.putString(FILE_URI, mUriOfSendingFile).commit();
            editor.remove(MIME_TYPE_MULTIPLE).commit();
            editor.remove(FILE_URIS).commit();
        }
        if (V) Log.v(TAG, "Application data stored to SharedPreference! ");
    }
    public void saveSendingFileInfo(String mimeType, String uri) {
        synchronized (BluetoothOppManager.this) {
            mMultipleFlag = false;
            mMimeTypeOfSendigFile = mimeType;
            mUriOfSendingFile = uri;
            storeApplicationData();
        }
    }
    public void saveSendingFileInfo(String mimeType, ArrayList<Uri> uris) {
        synchronized (BluetoothOppManager.this) {
            mMultipleFlag = true;
            mMimeTypeOfSendigFiles = mimeType;
            mUrisOfSendingFiles = uris;
            storeApplicationData();
        }
    }
    public boolean isEnabled() {
        if (mAdapter != null) {
            return mAdapter.isEnabled();
        } else {
            if (V) Log.v(TAG, "BLUETOOTH_SERVICE is not available! ");
            return false;
        }
    }
    public void enableBluetooth() {
        if (mAdapter != null) {
            mAdapter.enable();
        }
    }
    public void disableBluetooth() {
        if (mAdapter != null) {
            mAdapter.disable();
        }
    }
    public String getDeviceName(BluetoothDevice device) {
        String deviceName;
        deviceName = BluetoothOppPreference.getInstance(mContext).getName(device);
        if (deviceName == null && mAdapter != null) {
            deviceName = device.getName();
        }
        if (deviceName == null) {
            deviceName = mContext.getString(R.string.unknown_device);
        }
        return deviceName;
    }
    public int getBatchSize() {
        synchronized (BluetoothOppManager.this) {
            return mfileNumInBatch;
        }
    }
    public void startTransfer(BluetoothDevice device) {
        if (V) Log.v(TAG, "Active InsertShareThread number is : " + mInsertShareThreadNum);
        InsertShareInfoThread insertThread;
        synchronized (BluetoothOppManager.this) {
            if (mInsertShareThreadNum > ALLOWED_INSERT_SHARE_THREAD_NUMBER) {
                Log.e(TAG, "Too many shares user triggered concurrently!");
                Intent in = new Intent(mContext, BluetoothOppBtErrorActivity.class);
                in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                in.putExtra("title", mContext.getString(R.string.enabling_progress_title));
                in.putExtra("content", mContext.getString(R.string.ErrorTooManyRequests));
                mContext.startActivity(in);
                return;
            }
            insertThread = new InsertShareInfoThread(device, mMultipleFlag, mMimeTypeOfSendigFile,
                    mUriOfSendingFile, mMimeTypeOfSendigFiles, mUrisOfSendingFiles);
            if (mMultipleFlag) {
                mfileNumInBatch = mUrisOfSendingFiles.size();
            }
        }
        insertThread.start();
    }
    private class InsertShareInfoThread extends Thread {
        private final BluetoothDevice mRemoteDevice;
        private final String mTypeOfSingleFile;
        private final String mUri;
        private final String mTypeOfMultipleFiles;
        private final ArrayList<Uri> mUris;
        private final boolean mIsMultiple;
        public InsertShareInfoThread(BluetoothDevice device, boolean multiple,
                String typeOfSingleFile, String uri, String typeOfMultipleFiles, ArrayList<Uri> uris) {
            super("Insert ShareInfo Thread");
            this.mRemoteDevice = device;
            this.mIsMultiple = multiple;
            this.mTypeOfSingleFile = typeOfSingleFile;
            this.mUri = uri;
            this.mTypeOfMultipleFiles = typeOfMultipleFiles;
            this.mUris = uris;
            synchronized (BluetoothOppManager.this) {
                mInsertShareThreadNum++;
            }
            if (V) Log.v(TAG, "Thread id is: " + this.getId());
        }
        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            if (mRemoteDevice == null) {
                Log.e(TAG, "Target bt device is null!");
                return;
            }
            if (mIsMultiple) {
                insertMultipleShare();
            } else {
                insertSingleShare();
            }
            synchronized (BluetoothOppManager.this) {
                mInsertShareThreadNum--;
            }
        }
        private void insertMultipleShare() {
            int count = mUris.size();
            Long ts = System.currentTimeMillis();
            for (int i = 0; i < count; i++) {
                Uri fileUri = mUris.get(i);
                ContentResolver contentResolver = mContext.getContentResolver();
                String contentType = contentResolver.getType(fileUri);
                if (V) Log.v(TAG, "Got mimetype: " + contentType + "  Got uri: " + fileUri);
                if (TextUtils.isEmpty(contentType)) {
                    contentType = mTypeOfMultipleFiles;
                }
                ContentValues values = new ContentValues();
                values.put(BluetoothShare.URI, fileUri.toString());
                values.put(BluetoothShare.MIMETYPE, contentType);
                values.put(BluetoothShare.DESTINATION, mRemoteDevice.getAddress());
                values.put(BluetoothShare.TIMESTAMP, ts);
                final Uri contentUri = mContext.getContentResolver().insert(
                        BluetoothShare.CONTENT_URI, values);
                if (V) Log.v(TAG, "Insert contentUri: " + contentUri + "  to device: "
                            + getDeviceName(mRemoteDevice));
            }
        }
        private void insertSingleShare() {
            ContentValues values = new ContentValues();
            values.put(BluetoothShare.URI, mUri);
            values.put(BluetoothShare.MIMETYPE, mTypeOfSingleFile);
            values.put(BluetoothShare.DESTINATION, mRemoteDevice.getAddress());
            final Uri contentUri = mContext.getContentResolver().insert(BluetoothShare.CONTENT_URI,
                    values);
            if (V) Log.v(TAG, "Insert contentUri: " + contentUri + "  to device: "
                                + getDeviceName(mRemoteDevice));
        }
    }
}

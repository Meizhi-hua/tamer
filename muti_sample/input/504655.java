public class Photographs extends Activity {
    private static final String LOG_TAG = "Wallpaper";
    static final int PHOTO_PICKED = 1;
    static final int CROP_DONE = 2;
    static final int SHOW_PROGRESS = 0;
    static final int FINISH = 1;
    static final String DO_LAUNCH_ICICLE = "do_launch";
    static final String TEMP_FILE_PATH_ICICLE = "temp_file_path";
    private App mApp = null; 
    private ProgressDialog mProgressDialog = null;
    private boolean mDoLaunch = true;
    private File mTempFile;
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case SHOW_PROGRESS: {
                CharSequence c = getText(Res.string.wallpaper);
                mProgressDialog = ProgressDialog.show(Photographs.this, "", c, true, false);
                break;
            }
            case FINISH: {
                closeProgressDialog();
                setResult(RESULT_OK);
                finish();
                break;
            }
            }
        }
    };
    static class SetWallpaperThread extends Thread {
        private final Bitmap mBitmap;
        private final Handler mHandler;
        private final Context mContext;
        private final File mFile;
        public SetWallpaperThread(Bitmap bitmap, Handler handler, Context context, File file) {
            mBitmap = bitmap;
            mHandler = handler;
            mContext = context;
            mFile = file;
        }
        @Override
        public void run() {
            try {
                mContext.setWallpaper(mBitmap);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Failed to set wallpaper.", e);
            } finally {
                mHandler.sendEmptyMessage(FINISH);
                mFile.delete();
            }
        }
    }
    private synchronized void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mApp = new App(Photographs.this);
        if (icicle != null) {
            mDoLaunch = icicle.getBoolean(DO_LAUNCH_ICICLE);
            mTempFile = new File(icicle.getString(TEMP_FILE_PATH_ICICLE));
        }
    }
    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        icicle.putBoolean(DO_LAUNCH_ICICLE, mDoLaunch);
        icicle.putString(TEMP_FILE_PATH_ICICLE, mTempFile.getAbsolutePath());
    }
    @Override
    protected void onPause() {
        closeProgressDialog();
        super.onPause();
        mApp.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mApp.onResume();
        if (!mDoLaunch) {
            return;
        }
        Uri imageToUse = getIntent().getData();
        if (imageToUse != null) {
            Intent intent = new Intent();
            intent.setClass(this, CropImage.class);
            intent.setData(imageToUse);
            formatIntent(intent);
            startActivityForResult(intent, CROP_DONE);
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
            intent.setType("image/*");
            intent.putExtra("crop", "true");
            formatIntent(intent);
            startActivityForResult(intent, PHOTO_PICKED);
        }
    }
    @Override
    protected void onDestroy() {
    	mApp.shutdown();
    	super.onDestroy();
    }   
    protected void formatIntent(Intent intent) {
        mTempFile = getFileStreamPath("temp-wallpaper");
        mTempFile.getParentFile().mkdirs();
        int width = getWallpaperDesiredMinimumWidth();
        int height = getWallpaperDesiredMinimumHeight();
        intent.putExtra("outputX", width);
        intent.putExtra("outputY", height);
        intent.putExtra("aspectX", width);
        intent.putExtra("aspectY", height);
        intent.putExtra("scale", true);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTempFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.name());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == PHOTO_PICKED || requestCode == CROP_DONE) && (resultCode == RESULT_OK) && (data != null)) {
            try {
                InputStream s = new FileInputStream(mTempFile);
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(s);
                    if (bitmap == null) {
                        Log.e(LOG_TAG, "Failed to set wallpaper. " + "Couldn't get bitmap for path " + mTempFile);
                    } else {
                        mHandler.sendEmptyMessage(SHOW_PROGRESS);
                        new SetWallpaperThread(bitmap, mHandler, this, mTempFile).start();
                    }
                    mDoLaunch = false;
                } finally {
                    Util.closeSilently(s);
                }
            } catch (FileNotFoundException ex) {
                Log.e(LOG_TAG, "file not found: " + mTempFile, ex);
            }
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}

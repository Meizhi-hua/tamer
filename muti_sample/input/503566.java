public class AudioModel extends MediaModel {
    private static final String TAG = MediaModel.TAG;
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private final HashMap<String, String> mExtras;
    public AudioModel(Context context, Uri uri) throws MmsException {
        this(context, null, null, uri);
        initModelFromUri(uri);
        checkContentRestriction();
    }
    public AudioModel(Context context, String contentType, String src, Uri uri) throws MmsException {
        super(context, SmilHelper.ELEMENT_TAG_AUDIO, contentType, src, uri);
        mExtras = new HashMap<String, String>();
    }
    public AudioModel(Context context, String contentType, String src,
            DrmWrapper wrapper) throws IOException {
        super(context, SmilHelper.ELEMENT_TAG_AUDIO, contentType, src, wrapper);
        mExtras = new HashMap<String, String>();
    }
    private void initModelFromUri(Uri uri) throws MmsException {
        ContentResolver cr = mContext.getContentResolver();
        Cursor c = SqliteWrapper.query(mContext, cr, uri, null, null, null, null);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    String path;
                    boolean isFromMms = isMmsUri(uri);
                    if (isFromMms) {
                        path = c.getString(c.getColumnIndexOrThrow(Part._DATA));
                        mContentType = c.getString(c.getColumnIndexOrThrow(Part.CONTENT_TYPE));
                    } else {
                        path = c.getString(c.getColumnIndexOrThrow(Audio.Media.DATA));
                        mContentType = c.getString(c.getColumnIndexOrThrow(
                                Audio.Media.MIME_TYPE));
                        String album = c.getString(c.getColumnIndexOrThrow("album"));
                        if (!TextUtils.isEmpty(album)) {
                            mExtras.put("album", album);
                        }
                        String artist = c.getString(c.getColumnIndexOrThrow("artist"));
                        if (!TextUtils.isEmpty(artist)) {
                            mExtras.put("artist", artist);
                        }
                    }
                    mSrc = path.substring(path.lastIndexOf('/') + 1);
                    if (TextUtils.isEmpty(mContentType)) {
                        throw new MmsException("Type of media is unknown.");
                    }
                    if (LOCAL_LOGV) {
                        Log.v(TAG, "New AudioModel created:"
                                + " mSrc=" + mSrc
                                + " mContentType=" + mContentType
                                + " mUri=" + uri
                                + " mExtras=" + mExtras);
                    }
                } else {
                    throw new MmsException("Nothing found: " + uri);
                }
            } finally {
                c.close();
            }
        } else {
            throw new MmsException("Bad URI: " + uri);
        }
        initMediaDuration();
    }
    public void stop() {
        appendAction(MediaAction.STOP);
        notifyModelChanged(false);
    }
    public void handleEvent(Event evt) {
        String evtType = evt.getType();
        if (LOCAL_LOGV) {
            Log.v(TAG, "Handling event: " + evtType + " on " + this);
        }
        MediaAction action = MediaAction.NO_ACTIVE_ACTION;
        if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_START_EVENT)) {
            action = MediaAction.START;
            pauseMusicPlayer();
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_END_EVENT)) {
            action = MediaAction.STOP;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_PAUSE_EVENT)) {
            action = MediaAction.PAUSE;
        } else if (evtType.equals(SmilMediaElementImpl.SMIL_MEDIA_SEEK_EVENT)) {
            action = MediaAction.SEEK;
            mSeekTo = ((EventImpl) evt).getSeekTo();
        }
        appendAction(action);
        notifyModelChanged(false);
    }
    public Map<String, ?> getExtras() {
        return mExtras;
    }
    protected void checkContentRestriction() throws ContentRestrictionException {
        ContentRestriction cr = ContentRestrictionFactory.getContentRestriction();
        cr.checkAudioContentType(mContentType);
    }
    @Override
    protected boolean isPlayable() {
        return true;
    }
}

public class SlideshowAttachmentView extends LinearLayout implements
        SlideViewInterface {
    private static final String TAG = "SlideshowAttachmentView";
    private ImageView mImageView;
    private TextView mTextView;
    public SlideshowAttachmentView(Context context) {
        super(context);
    }
    public SlideshowAttachmentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        mImageView = (ImageView) findViewById(R.id.slideshow_image);
        mTextView = (TextView) findViewById(R.id.slideshow_text);
    }
    public void startAudio() {
    }
    public void startVideo() {
    }
    public void setAudio(Uri audio, String name, Map<String, ?> extras) {
    }
    public void setImage(String name, Bitmap bitmap) {
        if (null == bitmap) {
            try {
                bitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_missing_thumbnail_picture);
            } catch (java.lang.OutOfMemoryError e) {
            }
        }
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);      
        }
    }
    public void setImageRegionFit(String fit) {
    }
    public void setImageVisibility(boolean visible) {
        mImageView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
    public void setText(String name, String text) {
        mTextView.setText(text);
    }
    public void setTextVisibility(boolean visible) {
        mTextView.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }
    public void setVideo(String name, Uri video) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(mContext, video);
            mImageView.setImageBitmap(mp.getFrameAt(1000));
        } catch (IOException e) {
            Log.e(TAG, "Unexpected IOException.", e);
        } finally {
            mp.release();
        }
    }
    public void setVideoVisibility(boolean visible) {
    }
    public void stopAudio() {
    }
    public void stopVideo() {
    }
    public void reset() {
        mImageView.setImageURI(null);
        mTextView.setText("");
    }
    public void setVisibility(boolean visible) {
    }
    public void pauseAudio() {
    }
    public void pauseVideo() {
    }
    public void seekAudio(int seekTo) {
    }
    public void seekVideo(int seekTo) {
    }
}

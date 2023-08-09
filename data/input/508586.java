public class AttachmentEditor extends LinearLayout {
    private static final String TAG = "AttachmentEditor";
    static final int MSG_EDIT_SLIDESHOW   = 1;
    static final int MSG_SEND_SLIDESHOW   = 2;
    static final int MSG_PLAY_SLIDESHOW   = 3;
    static final int MSG_REPLACE_IMAGE    = 4;
    static final int MSG_REPLACE_VIDEO    = 5;
    static final int MSG_REPLACE_AUDIO    = 6;
    static final int MSG_PLAY_VIDEO       = 7;
    static final int MSG_PLAY_AUDIO       = 8;
    static final int MSG_VIEW_IMAGE       = 9;
    static final int MSG_REMOVE_ATTACHMENT = 10;
    private final Context mContext;
    private Handler mHandler;
    private SlideViewInterface mView;
    private SlideshowModel mSlideshow;
    private Presenter mPresenter;
    private boolean mCanSend;
    private Button mSendButton;
    public AttachmentEditor(Context context, AttributeSet attr) {
        super(context, attr);
        mContext = context;
    }
    public void update(WorkingMessage msg) {
        hideView();
        mView = null;
        if (!msg.hasAttachment()) {
            return;
        }
        mSlideshow = msg.getSlideshow();
        mView = createView();
        if ((mPresenter == null) || !mSlideshow.equals(mPresenter.getModel())) {
            mPresenter = PresenterFactory.getPresenter(
                    "MmsThumbnailPresenter", mContext, mView, mSlideshow);
        } else {
            mPresenter.setView(mView);
        }
        mPresenter.present();
    }
    public void setHandler(Handler handler) {
        mHandler = handler;
    }
    public void setCanSend(boolean enable) {
        if (mCanSend != enable) {
            mCanSend = enable;
            updateSendButton();
        }
    }
    private void updateSendButton() {
        if (null != mSendButton) {
            mSendButton.setEnabled(mCanSend);
            mSendButton.setFocusable(mCanSend);
        }
    }
    public void hideView() {
        if (mView != null) {
            ((View)mView).setVisibility(View.GONE);
        }
    }
    private View getStubView(int stubId, int viewId) {
        View view = findViewById(viewId);
        if (view == null) {
            ViewStub stub = (ViewStub) findViewById(stubId);
            view = stub.inflate();
        }
        return view;
    }
    private class MessageOnClick implements OnClickListener {
        private int mWhat;
        public MessageOnClick(int what) {
            mWhat = what;
        }
        public void onClick(View v) {
            Message msg = Message.obtain(mHandler, mWhat);
            msg.sendToTarget();
        }
    }
    private SlideViewInterface createView() {
        boolean inPortrait = inPortraitMode();
        if (mSlideshow.size() > 1) {
            return createSlideshowView(inPortrait);
        }
        SlideModel slide = mSlideshow.get(0);
        if (slide.hasImage()) {
            return createMediaView(
                    inPortrait ? R.id.image_attachment_view_portrait_stub :
                        R.id.image_attachment_view_landscape_stub,
                    inPortrait ? R.id.image_attachment_view_portrait :
                        R.id.image_attachment_view_landscape,
                    R.id.view_image_button, R.id.replace_image_button, R.id.remove_image_button,
                    MSG_VIEW_IMAGE, MSG_REPLACE_IMAGE, MSG_REMOVE_ATTACHMENT);
        } else if (slide.hasVideo()) {
            return createMediaView(
                    inPortrait ? R.id.video_attachment_view_portrait_stub :
                        R.id.video_attachment_view_landscape_stub,
                    inPortrait ? R.id.video_attachment_view_portrait :
                        R.id.video_attachment_view_landscape,
                    R.id.view_video_button, R.id.replace_video_button, R.id.remove_video_button,
                    MSG_PLAY_VIDEO, MSG_REPLACE_VIDEO, MSG_REMOVE_ATTACHMENT);
        } else if (slide.hasAudio()) {
            return createMediaView(
                    inPortrait ? R.id.audio_attachment_view_portrait_stub :
                        R.id.audio_attachment_view_landscape_stub,
                    inPortrait ? R.id.audio_attachment_view_portrait :
                        R.id.audio_attachment_view_landscape,
                    R.id.play_audio_button, R.id.replace_audio_button, R.id.remove_audio_button,
                    MSG_PLAY_AUDIO, MSG_REPLACE_AUDIO, MSG_REMOVE_ATTACHMENT);
        } else {
            throw new IllegalArgumentException();
        }
    }
    private boolean inPortraitMode() {
        final Configuration configuration = mContext.getResources().getConfiguration();
        return configuration.orientation == Configuration.ORIENTATION_PORTRAIT;
    }
    private SlideViewInterface createMediaView(
            int stub_view_id, int real_view_id,
            int view_button_id, int replace_button_id, int remove_button_id,
            int view_message, int replace_message, int remove_message) {
        LinearLayout view = (LinearLayout)getStubView(stub_view_id, real_view_id);
        view.setVisibility(View.VISIBLE);
        Button viewButton = (Button) view.findViewById(view_button_id);
        Button replaceButton = (Button) view.findViewById(replace_button_id);
        Button removeButton = (Button) view.findViewById(remove_button_id);
        viewButton.setOnClickListener(new MessageOnClick(view_message));
        replaceButton.setOnClickListener(new MessageOnClick(replace_message));
        removeButton.setOnClickListener(new MessageOnClick(remove_message));
        return (SlideViewInterface) view;
    }
    private SlideViewInterface createSlideshowView(boolean inPortrait) {
        LinearLayout view =(LinearLayout) getStubView(inPortrait ?
                R.id.slideshow_attachment_view_portrait_stub :
                R.id.slideshow_attachment_view_landscape_stub,
                inPortrait ? R.id.slideshow_attachment_view_portrait :
                    R.id.slideshow_attachment_view_landscape);
        view.setVisibility(View.VISIBLE);
        Button editBtn = (Button) view.findViewById(R.id.edit_slideshow_button);
        mSendButton = (Button) view.findViewById(R.id.send_slideshow_button);
        updateSendButton();
        final ImageButton playBtn = (ImageButton) view.findViewById(
                R.id.play_slideshow_button);
        editBtn.setOnClickListener(new MessageOnClick(MSG_EDIT_SLIDESHOW));
        mSendButton.setOnClickListener(new MessageOnClick(MSG_SEND_SLIDESHOW));
        playBtn.setOnClickListener(new MessageOnClick(MSG_PLAY_SLIDESHOW));
        return (SlideViewInterface) view;
    }
}

public class SlideshowPresenter extends Presenter {
    private static final String TAG = "SlideshowPresenter";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    protected int mLocation;
    protected final int mSlideNumber;
    protected float mWidthTransformRatio;
    protected float mHeightTransformRatio;
    protected final Handler mHandler = new Handler();
    public SlideshowPresenter(Context context, ViewInterface view, Model model) {
        super(context, view, model);
        mLocation = 0;
        mSlideNumber = ((SlideshowModel) mModel).size();
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setOnSizeChangedListener(
                    mViewSizeChangedListener);
        }
    }
    private final OnSizeChangedListener mViewSizeChangedListener =
        new OnSizeChangedListener() {
        public void onSizeChanged(int width, int height) {
            LayoutModel layout = ((SlideshowModel) mModel).getLayout();
            mWidthTransformRatio = getWidthTransformRatio(
                    width, layout.getLayoutWidth());
            mHeightTransformRatio = getHeightTransformRatio(
                    height, layout.getLayoutHeight());
            float ratio = mWidthTransformRatio > mHeightTransformRatio ?
                    mWidthTransformRatio : mHeightTransformRatio;
            mWidthTransformRatio = ratio;
            mHeightTransformRatio = ratio;
            if (LOCAL_LOGV) {
                Log.v(TAG, "ratio_w = " + mWidthTransformRatio
                        + ", ratio_h = " + mHeightTransformRatio);
            }
        }
    };
    private float getWidthTransformRatio(int width, int layoutWidth) {
        if (width > 0) {
            return (float) layoutWidth / (float) width;
        }
        return 1.0f;
    }
    private float getHeightTransformRatio(int height, int layoutHeight) {
        if (height > 0) {
            return (float) layoutHeight / (float) height;
        }
        return 1.0f;
    }
    private int transformWidth(int width) {
        return (int) (width / mWidthTransformRatio);
    }
    private int transformHeight(int height) {
        return (int) (height / mHeightTransformRatio);
    }
    @Override
    public void present() {
        presentSlide((SlideViewInterface) mView, ((SlideshowModel) mModel).get(mLocation));
    }
    protected void presentSlide(SlideViewInterface view, SlideModel model) {
        view.reset();
        try {
            for (MediaModel media : model) {
                if (media instanceof RegionMediaModel) {
                    presentRegionMedia(view, (RegionMediaModel) media, true);
                } else if (media.isAudio()) {
                    presentAudio(view, (AudioModel) media, true);
                }
            }
        } catch (DrmException e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(mContext,
                    mContext.getString(R.string.insufficient_drm_rights),
                    Toast.LENGTH_SHORT).show();
        }
    }
    protected void presentRegionMedia(SlideViewInterface view,
            RegionMediaModel rMedia, boolean dataChanged)
            throws DrmException {
        RegionModel r = (rMedia).getRegion();
        if (rMedia.isText()) {
            presentText(view, (TextModel) rMedia, r, dataChanged);
        } else if (rMedia.isImage()) {
            presentImage(view, (ImageModel) rMedia, r, dataChanged);
        } else if (rMedia.isVideo()) {
            presentVideo(view, (VideoModel) rMedia, r, dataChanged);
        }
    }
    protected void presentAudio(SlideViewInterface view, AudioModel audio,
            boolean dataChanged) throws DrmException {
        if (dataChanged) {
            view.setAudio(audio.getUriWithDrmCheck(), audio.getSrc(), audio.getExtras());
        }
        MediaAction action = audio.getCurrentAction();
        if (action == MediaAction.START) {
            view.startAudio();
        } else if (action == MediaAction.PAUSE) {
            view.pauseAudio();
        } else if (action == MediaAction.STOP) {
            view.stopAudio();
        } else if (action == MediaAction.SEEK) {
            view.seekAudio(audio.getSeekTo());
        }
    }
    protected void presentText(SlideViewInterface view, TextModel text,
            RegionModel r, boolean dataChanged) {
        if (dataChanged) {
            view.setText(text.getSrc(), text.getText());
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setTextRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformWidth(r.getWidth()),
                    transformHeight(r.getHeight()));
        }
        view.setTextVisibility(text.isVisible());
    }
    protected void presentImage(SlideViewInterface view, ImageModel image,
            RegionModel r, boolean dataChanged) throws DrmException {
        if (dataChanged) {
            view.setImage(image.getSrc(), image.getBitmapWithDrmCheck());
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setImageRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformWidth(r.getWidth()),
                    transformHeight(r.getHeight()));
        }
        view.setImageRegionFit(r.getFit());
        view.setImageVisibility(image.isVisible());
    }
    protected void presentVideo(SlideViewInterface view, VideoModel video,
            RegionModel r, boolean dataChanged) throws DrmException {
        if (dataChanged) {
            view.setVideo(video.getSrc(), video.getUriWithDrmCheck());
        }
        if (view instanceof AdaptableSlideViewInterface) {
            ((AdaptableSlideViewInterface) view).setVideoRegion(
                    transformWidth(r.getLeft()),
                    transformHeight(r.getTop()),
                    transformWidth(r.getWidth()),
                    transformHeight(r.getHeight()));
        }
        view.setVideoVisibility(video.isVisible());
        MediaAction action = video.getCurrentAction();
        if (action == MediaAction.START) {
            view.startVideo();
        } else if (action == MediaAction.PAUSE) {
            view.pauseVideo();
        } else if (action == MediaAction.STOP) {
            view.stopVideo();
        } else if (action == MediaAction.SEEK) {
            view.seekVideo(video.getSeekTo());
        }
    }
    public void setLocation(int location) {
        mLocation = location;
    }
    public int getLocation() {
        return mLocation;
    }
    public void goBackward() {
        if (mLocation > 0) {
            mLocation--;
        }
    }
    public void goForward() {
        if (mLocation < (mSlideNumber - 1)) {
            mLocation++;
        }
    }
    public void onModelChanged(final Model model, final boolean dataChanged) {
        final SlideViewInterface view = (SlideViewInterface) mView;
        if (model instanceof SlideshowModel) {
        } else if (model instanceof SlideModel) {
            if (((SlideModel) model).isVisible()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        presentSlide(view, (SlideModel) model);
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    public void run() {
                        goForward();
                    }
                });
            }
        } else if (model instanceof MediaModel) {
            if (model instanceof RegionMediaModel) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            presentRegionMedia(view, (RegionMediaModel) model, dataChanged);
                        } catch (DrmException e) {
                            Log.e(TAG, e.getMessage(), e);
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.insufficient_drm_rights),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else if (((MediaModel) model).isAudio()) {
                mHandler.post(new Runnable() {
                    public void run() {
                        try {
                            presentAudio(view, (AudioModel) model, dataChanged);
                        } catch (DrmException e) {
                            Log.e(TAG, e.getMessage(), e);
                            Toast.makeText(mContext,
                                    mContext.getString(R.string.insufficient_drm_rights),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } else if (model instanceof RegionModel) {
        }
    }
}

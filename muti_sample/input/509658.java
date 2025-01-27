public class MediaPlayerDemo_Audio extends Activity {
    private static final String TAG = "MediaPlayerDemo";
    private MediaPlayer mMediaPlayer;
    private static final String MEDIA = "media";
    private static final int LOCAL_AUDIO = 1;
    private static final int STREAM_AUDIO = 2;
    private static final int RESOURCES_AUDIO = 3;
    private static final int LOCAL_VIDEO = 4;
    private static final int STREAM_VIDEO = 5;
    private String path;
    private TextView tx;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        tx = new TextView(this);
        setContentView(tx);
        Bundle extras = getIntent().getExtras();
        playAudio(extras.getInt(MEDIA));
    }
    private void playAudio(Integer media) {
        try {
            switch (media) {
                case LOCAL_AUDIO:
                    path = "";
                    if (path == "") {
                        Toast
                                .makeText(
                                        MediaPlayerDemo_Audio.this,
                                        "Please edit MediaPlayer_Audio Activity, "
                                                + "and set the path variable to your audio file path."
                                                + " Your audio file must be stored on sdcard.",
                                        Toast.LENGTH_LONG).show();
                    }
                    mMediaPlayer = new MediaPlayer();
                    mMediaPlayer.setDataSource(path);
                    mMediaPlayer.prepare();
                    mMediaPlayer.start();
                    break;
                case RESOURCES_AUDIO:
                    mMediaPlayer = MediaPlayer.create(this, R.raw.test_cbr);
                    mMediaPlayer.start();
            }
            tx.setText("Playing audio...");
        } catch (Exception e) {
            Log.e(TAG, "error: " + e.getMessage(), e);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}

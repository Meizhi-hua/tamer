public class ChooseLockPatternExample extends Activity implements View.OnClickListener {
    private static final long START_DELAY = 1000;
    protected static final String TAG = "Settings";
    private View mNextButton;
    private View mSkipButton;
    private View mImageView;
    private AnimationDrawable mAnimation;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        public void run() {
            startAnimation(mAnimation);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.choose_lock_pattern_example);
        initViews();
    }
    @Override
    protected void onResume() {
        super.onResume();
        mHandler.postDelayed(mRunnable, START_DELAY);
    }
    @Override
    protected void onPause() {
        super.onPause();
        stopAnimation(mAnimation);
    }
    public void onClick(View v) {
        if (v == mSkipButton) {
            setResult(ChooseLockPattern.RESULT_FINISHED);
            finish();
        } else if (v == mNextButton) {
            stopAnimation(mAnimation);
            Intent intent = new Intent(this, ChooseLockPattern.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
            startActivity(intent);
            finish();
        }
    }
    private void initViews() {
        mNextButton = findViewById(R.id.next_button);
        mNextButton.setOnClickListener(this);
        mSkipButton = findViewById(R.id.skip_button);
        mSkipButton.setOnClickListener(this);
        mImageView = (ImageView) findViewById(R.id.lock_anim);
        mImageView.setBackgroundResource(R.drawable.lock_anim);
        mImageView.setOnClickListener(this);
        mAnimation = (AnimationDrawable) mImageView.getBackground();
    }
    protected void startAnimation(final AnimationDrawable animation) {
        if (animation != null && !animation.isRunning()) {
            animation.run();
        }
    }
    protected void stopAnimation(final AnimationDrawable animation) {
        if (animation != null && animation.isRunning()) animation.stop();
    }
}

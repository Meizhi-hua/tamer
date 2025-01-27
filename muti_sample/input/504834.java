public class IconMerger extends LinearLayout {
    StatusBarService service;
    StatusBarIcon moreIcon;
    public IconMerger(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        final int maxWidth = r - l;
        final int N = getChildCount();
        int i;
        int fitRight = -1;
        for (i=N-1; i>=0; i--) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                fitRight = child.getRight();
                break;
            }
        }
        View moreView = null;
        int fitLeft = -1;
        int startIndex = -1;
        for (i=0; i<N; i++) {
            final View child = getChildAt(i);
            if (com.android.internal.R.drawable.stat_notify_more == child.getId()) {
                moreView = child;
                startIndex = i+1;
            }
            else if (child.getVisibility() != GONE) {
                fitLeft = child.getLeft();
                break;
            }
        }
        if (moreView == null || startIndex < 0) {
            throw new RuntimeException("Status Bar / IconMerger moreView == null");
        }
        int adjust = 0;
        if (fitRight - fitLeft <= maxWidth) {
            adjust = fitLeft - moreView.getLeft();
            fitLeft -= adjust;
            fitRight -= adjust;
            moreView.layout(0, moreView.getTop(), 0, moreView.getBottom());
        }
        int extra = fitRight - r;
        int shift = -1;
        int breakingPoint = fitLeft + extra + adjust;
        int number = 0;
        for (i=startIndex; i<N; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                int childLeft = child.getLeft();
                int childRight = child.getRight();
                if (childLeft < breakingPoint) {
                    child.layout(0, child.getTop(), 0, child.getBottom());
                    int n = this.service.getIconNumberForView(child);
                    if (n == 0) {
                        number += 1;
                    } else if (n > 0) {
                        number += n;
                    }
                } else {
                    if (shift < 0) {
                        shift = childLeft - fitLeft;
                    }
                    child.layout(childLeft-shift, child.getTop(),
                                    childRight-shift, child.getBottom());
                }
            }
        }
        if (false) {
            this.moreIcon.update(number);
        } else {
            mBugWorkaroundNumber = number;
            mBugWorkaroundHandler.post(mBugWorkaroundRunnable);
        }
    }
    private int mBugWorkaroundNumber;
    private Handler mBugWorkaroundHandler = new Handler();
    private Runnable mBugWorkaroundRunnable = new Runnable() {
        public void run() {
            IconMerger.this.moreIcon.update(mBugWorkaroundNumber);
            IconMerger.this.moreIcon.view.invalidate();
        }
    };
}

public class TickerView extends TextSwitcher
{
    Ticker mTicker;
    public TickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mTicker.reflowText();
    }
}

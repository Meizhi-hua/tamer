public class MonthActivity extends Activity implements ViewSwitcher.ViewFactory,
        Navigator, AnimationListener {
    private static final int INITIAL_HEAP_SIZE = 4 * 1024 * 1024;
    private Animation mInAnimationPast;
    private Animation mInAnimationFuture;
    private Animation mOutAnimationPast;
    private Animation mOutAnimationFuture;
    private ViewSwitcher mSwitcher;
    private Time mTime;
    private ContentResolver mContentResolver;
    EventLoader mEventLoader;
    private int mStartDay;
    private ProgressBar mProgressBar;
    private static final int DAY_OF_WEEK_LABEL_IDS[] = {
        R.id.day0, R.id.day1, R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6
    };
    private static final int DAY_OF_WEEK_KINDS[] = {
        Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY
    };
    protected void startProgressSpinner() {
        mProgressBar.setVisibility(View.VISIBLE);
    }
    protected void stopProgressSpinner() {
        mProgressBar.setVisibility(View.GONE);
    }
    public View makeView() {
        MonthView mv = new MonthView(this, this);
        mv.setLayoutParams(new ViewSwitcher.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mv.setSelectedTime(mTime);
        return mv;
    }
    public void goTo(Time time, boolean animate) {
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(Utils.formatMonthYear(this, time));
        MonthView current = (MonthView) mSwitcher.getCurrentView();
        current.dismissPopup();
        Time currentTime = current.getTime();
        if (animate) {
            int currentMonth = currentTime.month + currentTime.year * 12;
            int nextMonth = time.month + time.year * 12;
            if (nextMonth < currentMonth) {
                mSwitcher.setInAnimation(mInAnimationPast);
                mSwitcher.setOutAnimation(mOutAnimationPast);
            } else {
                mSwitcher.setInAnimation(mInAnimationFuture);
                mSwitcher.setOutAnimation(mOutAnimationFuture);
            }
        }
        MonthView next = (MonthView) mSwitcher.getNextView();
        next.setSelectionMode(current.getSelectionMode());
        next.setSelectedTime(time);
        next.reloadEvents();
        next.animationStarted();
        mSwitcher.showNext();
        next.requestFocus();
        mTime = time;
    }
    public void goToToday() {
        Time now = new Time();
        now.set(System.currentTimeMillis());
        now.minute = 0;
        now.second = 0;
        now.normalize(false);
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(Utils.formatMonthYear(this, now));
        mTime = now;
        MonthView view = (MonthView) mSwitcher.getCurrentView();
        view.setSelectedTime(now);
        view.reloadEvents();
    }
    public long getSelectedTime() {
        MonthView mv = (MonthView) mSwitcher.getCurrentView();
        return mv.getSelectedTimeInMillis();
    }
    public boolean getAllDay() {
        return false;
    }
    int getStartDay() {
        return mStartDay;
    }
    void eventsChanged() {
        MonthView view = (MonthView) mSwitcher.getCurrentView();
        view.reloadEvents();
    }
    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_TIME_CHANGED)
                    || action.equals(Intent.ACTION_DATE_CHANGED)
                    || action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                eventsChanged();
            }
        }
    };
    private ContentObserver mObserver = new ContentObserver(new Handler())
    {
        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }
        @Override
        public void onChange(boolean selfChange) {
            eventsChanged();
        }
    };
    public void onAnimationStart(Animation animation) {
    }
    public void onAnimationEnd(Animation animation) {
        MonthView monthView = (MonthView) mSwitcher.getCurrentView();
        monthView.animationFinished();
    }
    public void onAnimationRepeat(Animation animation) {
    }
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        VMRuntime.getRuntime().setMinimumHeapSize(INITIAL_HEAP_SIZE);
        setContentView(R.layout.month_activity);
        mContentResolver = getContentResolver();
        long time;
        if (icicle != null) {
            time = icicle.getLong(EVENT_BEGIN_TIME);
        } else {
            time = Utils.timeFromIntentInMillis(getIntent());
        }
        mTime = new Time();
        mTime.set(time);
        mTime.normalize(true);
        mStartDay = Calendar.getInstance().getFirstDayOfWeek();
        int diff = mStartDay - Calendar.SUNDAY - 1;
        final int startDay = Utils.getFirstDayOfWeek();
        final int sundayColor = getResources().getColor(R.color.sunday_text_color);
        final int saturdayColor = getResources().getColor(R.color.saturday_text_color);
        for (int day = 0; day < 7; day++) {
            final String dayString = DateUtils.getDayOfWeekString(
                    (DAY_OF_WEEK_KINDS[day] + diff) % 7 + 1, DateUtils.LENGTH_MEDIUM);
            final TextView label = (TextView) findViewById(DAY_OF_WEEK_LABEL_IDS[day]);
            label.setText(dayString);
            if (Utils.isSunday(day, startDay)) {
                label.setTextColor(sundayColor);
            } else if (Utils.isSaturday(day, startDay)) {
                label.setTextColor(saturdayColor);
            }
        }
        TextView title = (TextView) findViewById(R.id.title);
        title.setText(Utils.formatMonthYear(this, mTime));
        mEventLoader = new EventLoader(this);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
        mSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.getCurrentView().requestFocus();
        mInAnimationPast = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        mOutAnimationPast = AnimationUtils.loadAnimation(this, R.anim.slide_down_out);
        mInAnimationFuture = AnimationUtils.loadAnimation(this, R.anim.slide_up_in);
        mOutAnimationFuture = AnimationUtils.loadAnimation(this, R.anim.slide_up_out);
        mInAnimationPast.setAnimationListener(this);
        mInAnimationFuture.setAnimationListener(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        long timeMillis = Utils.timeFromIntentInMillis(intent);
        if (timeMillis > 0) {
            Time time = new Time();
            time.set(timeMillis);
            goTo(time, false);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            mEventLoader.stopBackgroundThread();
        }
        mContentResolver.unregisterContentObserver(mObserver);
        unregisterReceiver(mIntentReceiver);
        MonthView view = (MonthView) mSwitcher.getCurrentView();
        view.dismissPopup();
        view = (MonthView) mSwitcher.getNextView();
        view.dismissPopup();
        mEventLoader.stopBackgroundThread();
        Utils.setDefaultView(this, CalendarApplication.MONTH_VIEW_ID);
    }
    @Override
    protected void onResume() {
        super.onResume();
        mEventLoader.startBackgroundThread();
        eventsChanged();
        MonthView view1 = (MonthView) mSwitcher.getCurrentView();
        MonthView view2 = (MonthView) mSwitcher.getNextView();
        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(this);
        String str = prefs.getString(CalendarPreferenceActivity.KEY_DETAILED_VIEW,
                CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW);
        view1.setDetailedView(str);
        view2.setDetailedView(str);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        registerReceiver(mIntentReceiver, filter);
        mContentResolver.registerContentObserver(Events.CONTENT_URI,
                true, mObserver);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(EVENT_BEGIN_TIME, mTime.toMillis(true));
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuHelper.onPrepareOptionsMenu(this, menu);
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuHelper.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        MenuHelper.onOptionsItemSelected(this, item, this);
        return super.onOptionsItemSelected(item);
    }
}

public class AgendaWindowAdapter extends BaseAdapter {
    static final boolean BASICLOG = false;
    static final boolean DEBUGLOG = false;
    private static String TAG = "AgendaWindowAdapter";
    private static final String AGENDA_SORT_ORDER = "startDay ASC, begin ASC, title ASC";
    public static final int INDEX_TITLE = 1;
    public static final int INDEX_EVENT_LOCATION = 2;
    public static final int INDEX_ALL_DAY = 3;
    public static final int INDEX_HAS_ALARM = 4;
    public static final int INDEX_COLOR = 5;
    public static final int INDEX_RRULE = 6;
    public static final int INDEX_BEGIN = 7;
    public static final int INDEX_END = 8;
    public static final int INDEX_EVENT_ID = 9;
    public static final int INDEX_START_DAY = 10;
    public static final int INDEX_END_DAY = 11;
    public static final int INDEX_SELF_ATTENDEE_STATUS = 12;
    private static final String[] PROJECTION = new String[] {
            Instances._ID, 
            Instances.TITLE, 
            Instances.EVENT_LOCATION, 
            Instances.ALL_DAY, 
            Instances.HAS_ALARM, 
            Instances.COLOR, 
            Instances.RRULE, 
            Instances.BEGIN, 
            Instances.END, 
            Instances.EVENT_ID, 
            Instances.START_DAY, 
            Instances.END_DAY, 
            Instances.SELF_ATTENDEE_STATUS, 
    };
    private static final int OFF_BY_ONE_BUG = 1;
    private static final int MAX_NUM_OF_ADAPTERS = 5;
    private static final int IDEAL_NUM_OF_EVENTS = 50;
    private static final int MIN_QUERY_DURATION = 7; 
    private static final int MAX_QUERY_DURATION = 60; 
    private static final int PREFETCH_BOUNDARY = 1;
    private static final int RETRIES_ON_NO_DATA = 0;
    private Context mContext;
    private QueryHandler mQueryHandler;
    private AgendaListView mAgendaListView;
    private int mRowCount; 
    private int mEmptyCursorCount;
    private DayAdapterInfo mLastUsedInfo; 
    private LinkedList<DayAdapterInfo> mAdapterInfos = new LinkedList<DayAdapterInfo>();
    private ConcurrentLinkedQueue<QuerySpec> mQueryQueue = new ConcurrentLinkedQueue<QuerySpec>();
    private TextView mHeaderView;
    private TextView mFooterView;
    private boolean mDoneSettingUpHeaderFooter = false;
    private int mOlderRequests;
    private int mOlderRequestsProcessed;
    private int mNewerRequests;
    private int mNewerRequestsProcessed;
    private Formatter mFormatter;
    private StringBuilder mStringBuilder;
    private boolean mShuttingDown;
    private boolean mHideDeclined;
    private static final int QUERY_TYPE_OLDER = 0; 
    private static final int QUERY_TYPE_NEWER = 1; 
    private static final int QUERY_TYPE_CLEAN = 2; 
    private static class QuerySpec {
        long queryStartMillis;
        Time goToTime;
        int start;
        int end;
        int queryType;
        public QuerySpec(int queryType) {
            this.queryType = queryType;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + end;
            result = prime * result + (int) (queryStartMillis ^ (queryStartMillis >>> 32));
            result = prime * result + queryType;
            result = prime * result + start;
            if (goToTime != null) {
                long goToTimeMillis = goToTime.toMillis(false);
                result = prime * result + (int) (goToTimeMillis ^ (goToTimeMillis >>> 32));
            }
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            QuerySpec other = (QuerySpec) obj;
            if (end != other.end || queryStartMillis != other.queryStartMillis
                    || queryType != other.queryType || start != other.start) {
                return false;
            }
            if (goToTime != null) {
                if (goToTime.toMillis(false) != other.goToTime.toMillis(false)) {
                    return false;
                }
            } else {
                if (other.goToTime != null) {
                    return false;
                }
            }
            return true;
        }
    }
    static class EventInfo {
        long begin;
        long end;
        long id;
    }
    static class DayAdapterInfo {
        Cursor cursor;
        AgendaByDayAdapter dayAdapter;
        int start; 
        int end; 
        int offset; 
        int size; 
        public DayAdapterInfo(Context context) {
            dayAdapter = new AgendaByDayAdapter(context);
        }
        @Override
        public String toString() {
            Time time = new Time();
            StringBuilder sb = new StringBuilder();
            time.setJulianDay(start);
            time.normalize(false);
            sb.append("Start:").append(time.toString());
            time.setJulianDay(end);
            time.normalize(false);
            sb.append(" End:").append(time.toString());
            sb.append(" Offset:").append(offset);
            sb.append(" Size:").append(size);
            return sb.toString();
        }
    }
    public AgendaWindowAdapter(AgendaActivity agendaActivity,
            AgendaListView agendaListView) {
        mContext = agendaActivity;
        mAgendaListView = agendaListView;
        mQueryHandler = new QueryHandler(agendaActivity.getContentResolver());
        mStringBuilder = new StringBuilder(50);
        mFormatter = new Formatter(mStringBuilder, Locale.getDefault());
        LayoutInflater inflater = (LayoutInflater) agendaActivity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mHeaderView = (TextView)inflater.inflate(R.layout.agenda_header_footer, null);
        mFooterView = (TextView)inflater.inflate(R.layout.agenda_header_footer, null);
        mHeaderView.setText(R.string.loading);
        mAgendaListView.addHeaderView(mHeaderView);
    }
    @Override
    public int getViewTypeCount() {
        return AgendaByDayAdapter.TYPE_LAST;
    }
    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }
    @Override
    public int getItemViewType(int position) {
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info != null) {
            return info.dayAdapter.getItemViewType(position - info.offset);
        } else {
            return -1;
        }
    }
    @Override
    public boolean isEnabled(int position) {
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info != null) {
            return info.dayAdapter.isEnabled(position - info.offset);
        } else {
            return false;
        }
    }
    public int getCount() {
        return mRowCount;
    }
    public Object getItem(int position) {
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info != null) {
            return info.dayAdapter.getItem(position - info.offset);
        } else {
            return null;
        }
    }
    @Override
    public boolean hasStableIds() {
        return true;
    }
    public long getItemId(int position) {
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info != null) {
            return ((position - info.offset) << 20) + info.start ;
        } else {
            return -1;
        }
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= (mRowCount - PREFETCH_BOUNDARY)
                && mNewerRequests <= mNewerRequestsProcessed) {
            if (DEBUGLOG) Log.e(TAG, "queryForNewerEvents: ");
            mNewerRequests++;
            queueQuery(new QuerySpec(QUERY_TYPE_NEWER));
        }
        if (position < PREFETCH_BOUNDARY
                && mOlderRequests <= mOlderRequestsProcessed) {
            if (DEBUGLOG) Log.e(TAG, "queryForOlderEvents: ");
            mOlderRequests++;
            queueQuery(new QuerySpec(QUERY_TYPE_OLDER));
        }
        View v;
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info != null) {
            v = info.dayAdapter.getView(position - info.offset, convertView,
                    parent);
        } else {
            Log.e(TAG, "BUG: getAdapterInfoByPosition returned null!!! " + position);
            TextView tv = new TextView(mContext);
            tv.setText("Bug! " + position);
            v = tv;
        }
        if (DEBUGLOG) {
            Log.e(TAG, "getView " + position + " = " + getViewTitle(v));
        }
        return v;
    }
    private int findDayPositionNearestTime(Time time) {
        if (DEBUGLOG) Log.e(TAG, "findDayPositionNearestTime " + time);
        DayAdapterInfo info = getAdapterInfoByTime(time);
        if (info != null) {
            return info.offset + info.dayAdapter.findDayPositionNearestTime(time);
        } else {
            return -1;
        }
    }
    private DayAdapterInfo getAdapterInfoByPosition(int position) {
        synchronized (mAdapterInfos) {
            if (mLastUsedInfo != null && mLastUsedInfo.offset <= position
                    && position < (mLastUsedInfo.offset + mLastUsedInfo.size)) {
                return mLastUsedInfo;
            }
            for (DayAdapterInfo info : mAdapterInfos) {
                if (info.offset <= position
                        && position < (info.offset + info.size)) {
                    mLastUsedInfo = info;
                    return info;
                }
            }
        }
        return null;
    }
    private DayAdapterInfo getAdapterInfoByTime(Time time) {
        if (DEBUGLOG) Log.e(TAG, "getAdapterInfoByTime " + time.toString());
        Time tmpTime = new Time(time);
        long timeInMillis = tmpTime.normalize(true);
        int day = Time.getJulianDay(timeInMillis, tmpTime.gmtoff);
        synchronized (mAdapterInfos) {
            for (DayAdapterInfo info : mAdapterInfos) {
                if (info.start <= day && day < info.end) {
                    return info;
                }
            }
        }
        return null;
    }
    public EventInfo getEventByPosition(int position) {
        if (DEBUGLOG) Log.e(TAG, "getEventByPosition " + position);
        EventInfo event = new EventInfo();
        position -= OFF_BY_ONE_BUG;
        DayAdapterInfo info = getAdapterInfoByPosition(position);
        if (info == null) {
            return null;
        }
        position = info.dayAdapter.getCursorPosition(position - info.offset);
        if (position == Integer.MIN_VALUE) {
            return null;
        }
        boolean isDayHeader = false;
        if (position < 0) {
            position = -position;
            isDayHeader = true;
        }
        if (position < info.cursor.getCount()) {
            info.cursor.moveToPosition(position);
            event.begin = info.cursor.getLong(AgendaWindowAdapter.INDEX_BEGIN);
            boolean allDay = info.cursor.getInt(AgendaWindowAdapter.INDEX_ALL_DAY) != 0;
            if (allDay) { 
                Time time = new Time();
                time.setJulianDay(Time.getJulianDay(event.begin, 0));
                event.begin = time.toMillis(false );
            } else if (isDayHeader) { 
                Time time = new Time();
                time.set(event.begin);
                time.hour = 0;
                time.minute = 0;
                time.second = 0;
                event.begin = time.toMillis(false );
            }
            if (!isDayHeader) {
                event.end = info.cursor.getLong(AgendaWindowAdapter.INDEX_END);
                event.id = info.cursor.getLong(AgendaWindowAdapter.INDEX_EVENT_ID);
            }
            return event;
        }
        return null;
    }
    public void refresh(Time goToTime, boolean forced) {
        if (DEBUGLOG) {
            Log.e(TAG, "refresh " + goToTime.toString() + (forced ? " forced" : " not forced"));
        }
        int startDay = Time.getJulianDay(goToTime.toMillis(false), goToTime.gmtoff);
        if (!forced && isInRange(startDay, startDay)) {
            mAgendaListView.setSelection(findDayPositionNearestTime(goToTime) + OFF_BY_ONE_BUG);
            return;
        }
        int endDay = startDay + MIN_QUERY_DURATION;
        queueQuery(startDay, endDay, goToTime, QUERY_TYPE_CLEAN);
    }
    public void close() {
        mShuttingDown = true;
        pruneAdapterInfo(QUERY_TYPE_CLEAN);
        if (mQueryHandler != null) {
            mQueryHandler.cancelOperation(0);
        }
    }
    private DayAdapterInfo pruneAdapterInfo(int queryType) {
        synchronized (mAdapterInfos) {
            DayAdapterInfo recycleMe = null;
            if (!mAdapterInfos.isEmpty()) {
                if (mAdapterInfos.size() >= MAX_NUM_OF_ADAPTERS) {
                    if (queryType == QUERY_TYPE_NEWER) {
                        recycleMe = mAdapterInfos.removeFirst();
                    } else if (queryType == QUERY_TYPE_OLDER) {
                        recycleMe = mAdapterInfos.removeLast();
                        recycleMe.size = 0;
                    }
                    if (recycleMe != null) {
                        if (recycleMe.cursor != null) {
                            recycleMe.cursor.close();
                        }
                        return recycleMe;
                    }
                }
                if (mRowCount == 0 || queryType == QUERY_TYPE_CLEAN) {
                    mRowCount = 0;
                    int deletedRows = 0;
                    DayAdapterInfo info;
                    do {
                        info = mAdapterInfos.poll();
                        if (info != null) {
                            info.cursor.close();
                            deletedRows += info.size;
                            recycleMe = info;
                        }
                    } while (info != null);
                    if (recycleMe != null) {
                        recycleMe.cursor = null;
                        recycleMe.size = deletedRows;
                    }
                }
            }
            return recycleMe;
        }
    }
    private String buildQuerySelection() {
        if (mHideDeclined) {
            return Calendars.SELECTED + "=1 AND "
                    + Instances.SELF_ATTENDEE_STATUS + "!="
                    + Attendees.ATTENDEE_STATUS_DECLINED;
        } else {
            return Calendars.SELECTED + "=1";
        }
    }
    private Uri buildQueryUri(int start, int end) {
        StringBuilder path = new StringBuilder();
        path.append(start);
        path.append('/');
        path.append(end);
        Uri uri = Uri.withAppendedPath(Instances.CONTENT_BY_DAY_URI, path.toString());
        return uri;
    }
    private boolean isInRange(int start, int end) {
        synchronized (mAdapterInfos) {
            if (mAdapterInfos.isEmpty()) {
                return false;
            }
            return mAdapterInfos.getFirst().start <= start && end <= mAdapterInfos.getLast().end;
        }
    }
    private int calculateQueryDuration(int start, int end) {
        int queryDuration = MAX_QUERY_DURATION;
        if (mRowCount != 0) {
            queryDuration = IDEAL_NUM_OF_EVENTS * (end - start + 1) / mRowCount;
        }
        if (queryDuration > MAX_QUERY_DURATION) {
            queryDuration = MAX_QUERY_DURATION;
        } else if (queryDuration < MIN_QUERY_DURATION) {
            queryDuration = MIN_QUERY_DURATION;
        }
        return queryDuration;
    }
    private boolean queueQuery(int start, int end, Time goToTime, int queryType) {
        QuerySpec queryData = new QuerySpec(queryType);
        queryData.goToTime = goToTime;
        queryData.start = start;
        queryData.end = end;
        return queueQuery(queryData);
    }
    private boolean queueQuery(QuerySpec queryData) {
        Boolean queuedQuery;
        synchronized (mQueryQueue) {
            queuedQuery = false;
            Boolean doQueryNow = mQueryQueue.isEmpty();
            mQueryQueue.add(queryData);
            queuedQuery = true;
            if (doQueryNow) {
                doQuery(queryData);
            }
        }
        return queuedQuery;
    }
    private void doQuery(QuerySpec queryData) {
        if (!mAdapterInfos.isEmpty()) {
            int start = mAdapterInfos.getFirst().start;
            int end = mAdapterInfos.getLast().end;
            int queryDuration = calculateQueryDuration(start, end);
            switch(queryData.queryType) {
                case QUERY_TYPE_OLDER:
                    queryData.end = start - 1;
                    queryData.start = queryData.end - queryDuration;
                    break;
                case QUERY_TYPE_NEWER:
                    queryData.start = end + 1;
                    queryData.end = queryData.start + queryDuration;
                    break;
            }
        }
        if (BASICLOG) {
            Time time = new Time();
            time.setJulianDay(queryData.start);
            Time time2 = new Time();
            time2.setJulianDay(queryData.end);
            Log.v(TAG, "startQuery: " + time.toString() + " to "
                    + time2.toString() + " then go to " + queryData.goToTime);
        }
        mQueryHandler.cancelOperation(0);
        if (BASICLOG) queryData.queryStartMillis = System.nanoTime();
        mQueryHandler.startQuery(0, queryData, buildQueryUri(
                queryData.start, queryData.end), PROJECTION,
                buildQuerySelection(), null, AGENDA_SORT_ORDER);
    }
    private String formatDateString(int julianDay) {
        Time time = new Time();
        time.setJulianDay(julianDay);
        long millis = time.toMillis(false);
        mStringBuilder.setLength(0);
        return DateUtils.formatDateRange(mContext, mFormatter, millis, millis,
                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_ABBREV_MONTH).toString();
    }
    private void updateHeaderFooter(final int start, final int end) {
        mHeaderView.setText(mContext.getString(R.string.show_older_events,
                formatDateString(start)));
        mFooterView.setText(mContext.getString(R.string.show_newer_events,
                formatDateString(end)));
    }
    private class QueryHandler extends AsyncQueryHandler {
        public QueryHandler(ContentResolver cr) {
            super(cr);
        }
        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            QuerySpec data = (QuerySpec)cookie;
            if (BASICLOG) {
                long queryEndMillis = System.nanoTime();
                Log.e(TAG, "Query time(ms): "
                        + (queryEndMillis - data.queryStartMillis) / 1000000
                        + " Count: " + cursor.getCount());
            }
            if (mShuttingDown) {
                cursor.close();
                return;
            }
            int cursorSize = cursor.getCount();
            if (cursorSize > 0 || mAdapterInfos.isEmpty() || data.queryType == QUERY_TYPE_CLEAN) {
                final int listPositionOffset = processNewCursor(data, cursor);
                if (data.goToTime == null) { 
                    notifyDataSetChanged();
                    if (listPositionOffset != 0) {
                        mAgendaListView.shiftSelection(listPositionOffset);
                    }
                } else { 
                    final Time goToTime = data.goToTime;
                    notifyDataSetChanged();
                    int newPosition = findDayPositionNearestTime(goToTime);
                    if (newPosition >= 0) {
                        mAgendaListView.setSelection(newPosition + OFF_BY_ONE_BUG);
                    }
                    if (DEBUGLOG) {
                        Log.e(TAG, "Setting listview to " +
                                "findDayPositionNearestTime: " + (newPosition + OFF_BY_ONE_BUG));
                    }
                }
            } else {
                cursor.close();
            }
            if (!mDoneSettingUpHeaderFooter) {
                OnClickListener headerFooterOnClickListener = new OnClickListener() {
                    public void onClick(View v) {
                        if (v == mHeaderView) {
                            queueQuery(new QuerySpec(QUERY_TYPE_OLDER));
                        } else {
                            queueQuery(new QuerySpec(QUERY_TYPE_NEWER));
                        }
                    }};
                mHeaderView.setOnClickListener(headerFooterOnClickListener);
                mFooterView.setOnClickListener(headerFooterOnClickListener);
                mAgendaListView.addFooterView(mFooterView);
                mDoneSettingUpHeaderFooter = true;
            }
            synchronized (mQueryQueue) {
                int totalAgendaRangeStart = -1;
                int totalAgendaRangeEnd = -1;
                if (cursorSize != 0) {
                    QuerySpec x = mQueryQueue.poll();
                    if (BASICLOG && !x.equals(data)) {
                        Log.e(TAG, "onQueryComplete - cookie != head of queue");
                    }
                    mEmptyCursorCount = 0;
                    if (data.queryType == QUERY_TYPE_NEWER) {
                        mNewerRequestsProcessed++;
                    } else if (data.queryType == QUERY_TYPE_OLDER) {
                        mOlderRequestsProcessed++;
                    }
                    totalAgendaRangeStart = mAdapterInfos.getFirst().start;
                    totalAgendaRangeEnd = mAdapterInfos.getLast().end;
                } else { 
                    QuerySpec querySpec = mQueryQueue.peek();
                    if (!mAdapterInfos.isEmpty()) {
                        DayAdapterInfo first = mAdapterInfos.getFirst();
                        DayAdapterInfo last = mAdapterInfos.getLast();
                        if (first.start - 1 <= querySpec.end && querySpec.start < first.start) {
                            first.start = querySpec.start;
                        }
                        if (querySpec.start <= last.end + 1 && last.end < querySpec.end) {
                            last.end = querySpec.end;
                        }
                        totalAgendaRangeStart = first.start;
                        totalAgendaRangeEnd = last.end;
                    } else {
                        totalAgendaRangeStart = querySpec.start;
                        totalAgendaRangeEnd = querySpec.end;
                    }
                    switch (querySpec.queryType) {
                        case QUERY_TYPE_OLDER:
                            totalAgendaRangeStart = querySpec.start;
                            querySpec.start -= MAX_QUERY_DURATION;
                            break;
                        case QUERY_TYPE_NEWER:
                            totalAgendaRangeEnd = querySpec.end;
                            querySpec.end += MAX_QUERY_DURATION;
                            break;
                        case QUERY_TYPE_CLEAN:
                            totalAgendaRangeStart = querySpec.start;
                            totalAgendaRangeEnd = querySpec.end;
                            querySpec.start -= MAX_QUERY_DURATION / 2;
                            querySpec.end += MAX_QUERY_DURATION / 2;
                            break;
                    }
                    if (++mEmptyCursorCount > RETRIES_ON_NO_DATA) {
                        mQueryQueue.poll();
                    }
                }
                updateHeaderFooter(totalAgendaRangeStart, totalAgendaRangeEnd);
                Iterator<QuerySpec> it = mQueryQueue.iterator();
                while (it.hasNext()) {
                    QuerySpec queryData = it.next();
                    if (!isInRange(queryData.start, queryData.end)) {
                        if (DEBUGLOG) Log.e(TAG, "Query accepted. QueueSize:" + mQueryQueue.size());
                        doQuery(queryData);
                        break;
                    } else {
                        it.remove();
                        if (DEBUGLOG) Log.e(TAG, "Query rejected. QueueSize:" + mQueryQueue.size());
                    }
                }
            }
            if (BASICLOG) {
                for (DayAdapterInfo info3 : mAdapterInfos) {
                    Log.e(TAG, "> " + info3.toString());
                }
            }
        }
        private int processNewCursor(QuerySpec data, Cursor cursor) {
            synchronized (mAdapterInfos) {
                DayAdapterInfo info = pruneAdapterInfo(data.queryType);
                int listPositionOffset = 0;
                if (info == null) {
                    info = new DayAdapterInfo(mContext);
                } else {
                    if (DEBUGLOG)
                        Log.e(TAG, "processNewCursor listPositionOffsetA="
                                + -info.size);
                    listPositionOffset = -info.size;
                }
                info.start = data.start;
                info.end = data.end;
                info.cursor = cursor;
                info.dayAdapter.changeCursor(info);
                info.size = info.dayAdapter.getCount();
                if (mAdapterInfos.isEmpty()
                        || data.end <= mAdapterInfos.getFirst().start) {
                    mAdapterInfos.addFirst(info);
                    listPositionOffset += info.size;
                } else if (BASICLOG && data.start < mAdapterInfos.getLast().end) {
                    mAdapterInfos.addLast(info);
                    for (DayAdapterInfo info2 : mAdapterInfos) {
                        Log.e("========== BUG ==", info2.toString());
                    }
                } else {
                    mAdapterInfos.addLast(info);
                }
                mRowCount = 0;
                for (DayAdapterInfo info3 : mAdapterInfos) {
                    info3.offset = mRowCount;
                    mRowCount += info3.size;
                }
                mLastUsedInfo = null;
                return listPositionOffset;
            }
        }
    }
    static String getViewTitle(View x) {
        String title = "";
        if (x != null) {
            Object yy = x.getTag();
            if (yy instanceof AgendaAdapter.ViewHolder) {
                TextView tv = ((AgendaAdapter.ViewHolder) yy).title;
                if (tv != null) {
                    title = (String) tv.getText();
                }
            } else if (yy != null) {
                TextView dateView = ((AgendaByDayAdapter.ViewHolder) yy).dateView;
                if (dateView != null) {
                    title = (String) dateView.getText();
                }
            }
        }
        return title;
    }
    public void setHideDeclinedEvents(boolean hideDeclined) {
        mHideDeclined = hideDeclined;
    }
}

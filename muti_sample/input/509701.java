public class TestAppWidgetProvider extends BroadcastReceiver {
    static final String TAG = "TestAppWidgetProvider";
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "intent=" + intent);
        if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            Log.d(TAG, "ENABLED");
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
            Log.d(TAG, "DISABLED");
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
            Log.d(TAG, "UPDATE");
            Bundle extras = intent.getExtras();
            int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            AppWidgetManager gm = AppWidgetManager.getInstance(context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.test_appwidget);
            views.setTextViewText(R.id.oh_hai_text, "hai: " + SystemClock.elapsedRealtime());
            if (false) {
                gm.updateAppWidget(appWidgetIds, views);
            } else {
                gm.updateAppWidget(new ComponentName("com.android.tests.appwidgetprovider",
                            "com.android.tests.appwidgetprovider.TestAppWidgetProvider"), views);
            }
        }
    }
}

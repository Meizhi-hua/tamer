public class LayoutAnimation1 extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadApps();
        setContentView(R.layout.layout_animation_1);
        GridView grid = (GridView) findViewById(R.id.grid);
        grid.setAdapter(new LayoutAnimation1.AppsAdapter());
    }
    private List<ResolveInfo> mApps;
    private void loadApps() {
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
    }
    public class AppsAdapter extends BaseAdapter {
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(LayoutAnimation1.this);
            ResolveInfo info = mApps.get(position % mApps.size());
            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            final int w = (int) (36 * getResources().getDisplayMetrics().density + 0.5f);
            i.setLayoutParams(new GridView.LayoutParams(w, w));
            return i;
        }
        public final int getCount() {
            return Math.min(32, mApps.size());
        }
        public final Object getItem(int position) {
            return mApps.get(position % mApps.size());
        }
        public final long getItemId(int position) {
            return position;
        }
    }
}

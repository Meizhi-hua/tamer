public class AddAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();
    public static final int ITEM_SHORTCUT = 0;
    public static final int ITEM_APPWIDGET = 1;
    public static final int ITEM_LIVE_FOLDER = 2;
    public static final int ITEM_WALLPAPER = 3;
    public class ListItem {
        public final CharSequence text;
        public final Drawable image;
        public final int actionTag;
        public ListItem(Resources res, int textResourceId, int imageResourceId, int actionTag) {
            text = res.getString(textResourceId);
            if (imageResourceId != -1) {
                image = res.getDrawable(imageResourceId);
            } else {
                image = null;
            }
            this.actionTag = actionTag;
        }
    }
    public AddAdapter(Launcher launcher) {
        super();
        mInflater = (LayoutInflater) launcher.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources res = launcher.getResources();
        mItems.add(new ListItem(res, R.string.group_shortcuts,
                R.drawable.ic_launcher_shortcut, ITEM_SHORTCUT));
        mItems.add(new ListItem(res, R.string.group_widgets,
                R.drawable.ic_launcher_appwidget, ITEM_APPWIDGET));
        mItems.add(new ListItem(res, R.string.group_live_folders,
                R.drawable.ic_launcher_folder, ITEM_LIVE_FOLDER));
        mItems.add(new ListItem(res, R.string.group_wallpapers,
                R.drawable.ic_launcher_wallpaper, ITEM_WALLPAPER));
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.add_list_item, parent, false);
        }
        TextView textView = (TextView) convertView;
        textView.setTag(item);
        textView.setText(item.text);
        textView.setCompoundDrawablesWithIntrinsicBounds(item.image, null, null, null);
        return convertView;
    }
    public int getCount() {
        return mItems.size();
    }
    public Object getItem(int position) {
        return mItems.get(position);
    }
    public long getItemId(int position) {
        return position;
    }
}

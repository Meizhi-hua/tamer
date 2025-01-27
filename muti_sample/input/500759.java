public class InflatedExpandableListView extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inflated_expandablelistview);
        ExpandableListView elv = (ExpandableListView) findViewById(R.id.elv);
        elv.setAdapter(new MyExpandableListAdapter());
    }
    public class MyExpandableListAdapter extends BaseExpandableListAdapter {
        private String[] groups = { "People Names", "Dog Names", "Cat Names", "Fish Names" };
        private String[][] children = {
                { "Arnold", "Barry", "Chuck", "David" },
                { "Ace", "Bandit", "Cha-Cha", "Deuce" },
                { "Fluffy", "Snuggles" },
                { "Goldy", "Bubbles" }
        };
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }
        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }
        public TextView getGenericView() {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 64);
            TextView textView = new TextView(InflatedExpandableListView.this);
            textView.setLayoutParams(lp);
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            textView.setPadding(36, 0, 0, 0);
            return textView;
        }
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                View convertView, ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getChild(groupPosition, childPosition).toString());
            return textView;
        }
        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }
        public int getGroupCount() {
            return groups.length;
        }
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                ViewGroup parent) {
            TextView textView = getGenericView();
            textView.setText(getGroup(groupPosition).toString());
            return textView;
        }
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
        public boolean hasStableIds() {
            return true;
        }
    }
}

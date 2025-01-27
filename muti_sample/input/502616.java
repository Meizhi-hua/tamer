public class AddColumn extends Activity {
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.add_column_in_table);
        final Button addRowButton = (Button) findViewById(R.id.add_row_button);
        addRowButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final TableLayout table = (TableLayout) findViewById(R.id.table);
                final TableRow newRow = new TableRow(AddColumn.this);
                for (int i = 0; i < 4; i++) {
                    final TextView view = new TextView(AddColumn.this);
                    view.setText("Column " + (i + 1));
                    view.setPadding(3, 3, 3, 3);
                    newRow.addView(view, new TableRow.LayoutParams());
                }
                table.addView(newRow, new TableLayout.LayoutParams());
                newRow.requestLayout();
            }
        });
    }
}

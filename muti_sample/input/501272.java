public class DialogActivity extends Activity {
    private static final int DIALOG_WITHOUT_EDITTEXT = 0;
    private static final int DIALOG_WITH_EDITTEXT = 1;
    private LinearLayout mLayout;
    private LayoutInflater mInflater;
    private Button mButton1;
    private Button mButton2;
    private EditText mEditText;
    @Override
    protected void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        mLayout = new LinearLayout(this);
        mLayout.setOrientation(LinearLayout.VERTICAL);
        mLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mButton1 = new Button(this);
        mButton1.setText("Dialog WITHOUT EditText");
        mButton1.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                showDialog(DIALOG_WITHOUT_EDITTEXT);
            }
        });
        mButton2 = new Button(this);
        mButton2.setText("Dialog WITH EditText");
        mButton2.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v) 
            {
                showDialog(DIALOG_WITH_EDITTEXT);
            }
        });
        mEditText = new EditText(this);
        mLayout.addView(mEditText);
        mLayout.addView(mButton1);
        mLayout.addView(mButton2);
        setContentView(mLayout);
    }
    @Override
    protected Dialog onCreateDialog(int id) 
    {
        switch (id) 
        {
            case DIALOG_WITHOUT_EDITTEXT:
                return createDialog(false);
            case DIALOG_WITH_EDITTEXT:
                return createDialog(true);
        }
        return super.onCreateDialog(id);
    }
    protected Dialog createDialog(boolean bEditText) 
    {
        LinearLayout layout;        
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        if(bEditText)
        {
            EditText editText;
            editText = new EditText(this);
            layout.addView(editText);
        }
        Dialog d = new Dialog(this);
        d.setTitle("The DIALOG!!!");
        d.setCancelable(true);
        d.setContentView(layout);
        return d;
    }
 }

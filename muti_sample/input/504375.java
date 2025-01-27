public class ListMenuItemView extends LinearLayout implements MenuView.ItemView {
    private MenuItemImpl mItemData; 
    private ImageView mIconView;
    private RadioButton mRadioButton;
    private TextView mTitleView;
    private CheckBox mCheckBox;
    private TextView mShortcutView;
    private Drawable mBackground;
    private int mTextAppearance;
    private Context mTextAppearanceContext;
    private int mMenuType;
    public ListMenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        TypedArray a =
            context.obtainStyledAttributes(
                attrs, com.android.internal.R.styleable.MenuView, defStyle, 0);
        mBackground = a.getDrawable(com.android.internal.R.styleable.MenuView_itemBackground);
        mTextAppearance = a.getResourceId(com.android.internal.R.styleable.
                                          MenuView_itemTextAppearance, -1);
        mTextAppearanceContext = context;
        a.recycle();
    }
    public ListMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setBackgroundDrawable(mBackground);
        mTitleView = (TextView) findViewById(com.android.internal.R.id.title);
        if (mTextAppearance != -1) {
            mTitleView.setTextAppearance(mTextAppearanceContext,
                                         mTextAppearance);
        }
        mShortcutView = (TextView) findViewById(com.android.internal.R.id.shortcut);
    }
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;
        mMenuType = menuType;
        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);
        setTitle(itemData.getTitleForItemView(this));
        setCheckable(itemData.isCheckable());
        setShortcut(itemData.shouldShowShortcut(), itemData.getShortcut());
        setIcon(itemData.getIcon());
        setEnabled(itemData.isEnabled());
    }
    public void setTitle(CharSequence title) {
        if (title != null) {
            mTitleView.setText(title);
            if (mTitleView.getVisibility() != VISIBLE) mTitleView.setVisibility(VISIBLE);
        } else {
            if (mTitleView.getVisibility() != GONE) mTitleView.setVisibility(GONE);
        }
    }
    public MenuItemImpl getItemData() {
        return mItemData;
    }
    public void setCheckable(boolean checkable) {
        if (!checkable && mRadioButton == null && mCheckBox == null) {
            return;
        }
        if (mRadioButton == null) {
            insertRadioButton();
        }
        if (mCheckBox == null) {
            insertCheckBox();
        }
        final CompoundButton compoundButton;
        final CompoundButton otherCompoundButton; 
        if (mItemData.isExclusiveCheckable()) {
            compoundButton = mRadioButton;
            otherCompoundButton = mCheckBox;
        } else {
            compoundButton = mCheckBox;
            otherCompoundButton = mRadioButton;
        }
        if (checkable) {
            compoundButton.setChecked(mItemData.isChecked());
            final int newVisibility = checkable ? VISIBLE : GONE;
            if (compoundButton.getVisibility() != newVisibility) {
                compoundButton.setVisibility(newVisibility);
            }
            if (otherCompoundButton.getVisibility() != GONE) {
                otherCompoundButton.setVisibility(GONE);
            }
        } else {
            mCheckBox.setVisibility(GONE);
            mRadioButton.setVisibility(GONE);
        }
    }
    public void setChecked(boolean checked) {
        CompoundButton compoundButton;
        if (mItemData.isExclusiveCheckable()) {
            if (mRadioButton == null) {
                insertRadioButton();
            }
            compoundButton = mRadioButton;
        } else {
            if (mCheckBox == null) {
                insertCheckBox();
            }
            compoundButton = mCheckBox;
        }
        compoundButton.setChecked(checked);
    }
    public void setShortcut(boolean showShortcut, char shortcutKey) {
        final int newVisibility = (showShortcut && mItemData.shouldShowShortcut())
                ? VISIBLE : GONE;
        if (newVisibility == VISIBLE) {
            mShortcutView.setText(mItemData.getShortcutLabel());
        }
        if (mShortcutView.getVisibility() != newVisibility) {
            mShortcutView.setVisibility(newVisibility);
        }
    }
    public void setIcon(Drawable icon) {
        if (!mItemData.shouldShowIcon(mMenuType)) {
            return;
        }
        if (mIconView == null && icon == null) {
            return;
        }
        if (mIconView == null) {
            insertIconView();
        }
        if (icon != null) {
            mIconView.setImageDrawable(icon);
            if (mIconView.getVisibility() != VISIBLE) {
                mIconView.setVisibility(VISIBLE);
            }
        } else {
            mIconView.setVisibility(GONE);
        }
    }
    private void insertIconView() {
        LayoutInflater inflater = mItemData.getLayoutInflater(mMenuType);
        mIconView = (ImageView) inflater.inflate(com.android.internal.R.layout.list_menu_item_icon,
                this, false);
        addView(mIconView, 0);
    }
    private void insertRadioButton() {
        LayoutInflater inflater = mItemData.getLayoutInflater(mMenuType);
        mRadioButton =
                (RadioButton) inflater.inflate(com.android.internal.R.layout.list_menu_item_radio,
                this, false);
        addView(mRadioButton);
    }
    private void insertCheckBox() {
        LayoutInflater inflater = mItemData.getLayoutInflater(mMenuType);
        mCheckBox =
                (CheckBox) inflater.inflate(com.android.internal.R.layout.list_menu_item_checkbox,
                this, false);
        addView(mCheckBox);
    }
    public boolean prefersCondensedTitle() {
        return false;
    }
    public boolean showsIcon() {
        return false;
    }
}

public class UsagePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private BooleanFieldEditor mOptInCheckBox;
    public UsagePreferencePage() {
    }
    public void init(IWorkbench workbench) {
    }
    @Override
    protected Control createContents(Composite parent) {
        Composite top = new Composite(parent, SWT.NONE);
        top.setLayout(new GridLayout(1, false));
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Link text = new Link(top, SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 200;
        text.setLayoutData(gd);
        text.setText(SdkStatsService.BODY_TEXT);
        text.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                SdkStatsService.openUrl(event.text);
            }
        });
        mOptInCheckBox = new BooleanFieldEditor(SdkStatsService.PING_OPT_IN,
                SdkStatsService.CHECKBOX_TEXT, top);
        mOptInCheckBox.setPage(this);
        mOptInCheckBox.setPreferenceStore(SdkStatsService.getPreferenceStore());
        mOptInCheckBox.load();
        return top;
    }
    @Override
    public boolean performCancel() {
        mOptInCheckBox.load();
        return super.performCancel();
    }
    @Override
    protected void performDefaults() {
        mOptInCheckBox.loadDefault();
        super.performDefaults();
    }
    @Override
    public boolean performOk() {
        save();
        return super.performOk();
    }
    @Override
    protected void performApply() {
        save();
        super.performApply();
    }
    private void save() {
        try {
            PreferenceStore store = SdkStatsService.getPreferenceStore();
            if (store !=  null) {
                store.setValue(SdkStatsService.PING_OPT_IN, mOptInCheckBox.getBooleanValue());
                store.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

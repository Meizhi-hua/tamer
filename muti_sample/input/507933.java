final class AvdDetailsDialog extends Dialog {
    private static Point sLastSize;
    private Shell mDialogShell;
    private final AvdInfo mAvdInfo;
    private Composite mRootComposite;
    public AvdDetailsDialog(Shell shell, AvdInfo avdInfo) {
        super(shell, SWT.APPLICATION_MODAL);
        mAvdInfo = avdInfo;
        setText("AVD details");
    }
    public void open() {
        createContents();
        positionShell();            
        mDialogShell.open();
        mDialogShell.layout();
        Display display = getParent().getDisplay();
        while (!mDialogShell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        if (!mDialogShell.isDisposed()) {
            sLastSize = mDialogShell.getSize();
            mDialogShell.close();
        }
    }
    private void createContents() {
        mDialogShell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.RESIZE);
        mDialogShell.setLayout(new GridLayout(1, false));
        mDialogShell.setSize(450, 300);
        mDialogShell.setText(getText());
        mRootComposite = new Composite(mDialogShell, SWT.NONE);
        mRootComposite.setLayout(new GridLayout(2, false));
        mRootComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout gl;
        Composite c = new Composite(mRootComposite, SWT.NONE);
        c.setLayout(gl = new GridLayout(2, false));
        gl.marginHeight = gl.marginWidth = 0;
        c.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (mAvdInfo != null) {
            displayValue(c, "Name:", mAvdInfo.getName());
            displayValue(c, "Path:", mAvdInfo.getPath());
            if (mAvdInfo.getStatus() != AvdStatus.OK) {
                displayValue(c, "Error:", mAvdInfo.getErrorMessage());
            } else {
                IAndroidTarget target = mAvdInfo.getTarget();
                AndroidVersion version = target.getVersion();
                displayValue(c, "Target:", String.format("%s (API level %s)",
                        target.getName(), version.getApiString()));
                Map<String, String> properties = mAvdInfo.getProperties();
                if (properties != null) {
                    String skin = properties.get(AvdManager.AVD_INI_SKIN_NAME);
                    if (skin != null) {
                        displayValue(c, "Skin:", skin);
                    }
                    String sdcard = properties.get(AvdManager.AVD_INI_SDCARD_SIZE);
                    if (sdcard == null) {
                        sdcard = properties.get(AvdManager.AVD_INI_SDCARD_PATH);
                    }
                    if (sdcard != null) {
                        displayValue(c, "SD Card:", sdcard);
                    }
                    HashMap<String, String> copy = new HashMap<String, String>(properties);
                    copy.remove(AvdManager.AVD_INI_SKIN_NAME);
                    copy.remove(AvdManager.AVD_INI_SKIN_PATH);
                    copy.remove(AvdManager.AVD_INI_SDCARD_SIZE);
                    copy.remove(AvdManager.AVD_INI_SDCARD_PATH);
                    copy.remove(AvdManager.AVD_INI_IMAGES_1);
                    copy.remove(AvdManager.AVD_INI_IMAGES_2);
                    if (copy.size() > 0) {
                        Label l = new Label(mRootComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
                        l.setLayoutData(new GridData(
                                GridData.FILL, GridData.CENTER, false, false, 2, 1));
                        c = new Composite(mRootComposite, SWT.NONE);
                        c.setLayout(gl = new GridLayout(2, false));
                        gl.marginHeight = gl.marginWidth = 0;
                        c.setLayoutData(new GridData(GridData.FILL_BOTH));
                        Set<String> keys = copy.keySet();
                        for (String key : keys) {
                            displayValue(c, key + ":", copy.get(key));
                        }
                    }
                }
            }
        }
    }
    private void displayValue(Composite parent, String label, String value) {
        Label l = new Label(parent, SWT.NONE);
        l.setText(label);
        l.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        l = new Label(parent, SWT.NONE);
        l.setText(value);
        l.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
    }
    private void positionShell() {
        Shell child = mDialogShell;
        Shell parent = getParent();
        if (child != null && parent != null) {
            Rectangle parentArea = parent.getClientArea();
            Point parentLoc = parent.getLocation();
            int px = parentLoc.x;
            int py = parentLoc.y;
            int pw = parentArea.width;
            int ph = parentArea.height;
            Point childSize = sLastSize != null ? sLastSize : child.getSize();
            int cw = childSize.x;
            int ch = childSize.y;
            child.setLocation(px + (pw - cw) / 2, py + (ph - ch) / 2);
            child.setSize(cw, ch);
        }
    }
}

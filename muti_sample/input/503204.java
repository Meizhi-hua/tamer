public class ConfigEditDialog extends GridDialog {
    private static final Pattern FLOAT_PATTERN = Pattern.compile("\\d*(\\.\\d?)?");
    private final FolderConfiguration mConfig = new FolderConfiguration();
    private ConfigurationSelector mConfigSelector;
    private Composite mStatusComposite;
    private Label mStatusLabel;
    private Label mStatusImage;
    private Image mError;
    private String mDeviceName;
    private String mConfigName;
    private float mXDpi = Float.NaN;
    private float mYDpi = Float.NaN;
    public ConfigEditDialog(Shell parentShell, FolderConfiguration config) {
        super(parentShell, 1, false);
        mConfig.set(config);
    }
    public void setDeviceName(String name) {
        mDeviceName = name;
    }
    public String getDeviceName() {
        return mDeviceName;
    }
    public void setXDpi(float xdpi) {
        mXDpi = xdpi;
    }
    public float getXDpi() {
        return mXDpi;
    }
    public void setYDpi(float ydpi) {
        mYDpi = ydpi;
    }
    public float getYDpi() {
        return mYDpi;
    }
    public void setConfigName(String name) {
        mConfigName = name;
    }
    public String getConfigName() {
        return mConfigName;
    }
    public void setConfig(FolderConfiguration config) {
        mConfig.set(config);
    }
    public void getConfig(FolderConfiguration config) {
        config.set(mConfig);
    }
    @Override
    public void createDialogContent(Composite parent) {
        mError = IconFactory.getInstance().getIcon("error"); 
        Group deviceGroup = new Group(parent, SWT.NONE);
        deviceGroup.setText("Device");
        deviceGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        deviceGroup.setLayout(new GridLayout(2, false));
        Label l = new Label(deviceGroup, SWT.None);
        l.setText("Name");
        final Text deviceNameText = new Text(deviceGroup, SWT.BORDER);
        deviceNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (mDeviceName != null) {
            deviceNameText.setText(mDeviceName);
        }
        deviceNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mDeviceName = deviceNameText.getText().trim();
                validateOk();
            }
        });
        VerifyListener floatVerifier = new VerifyListener() {
            public void verifyText(VerifyEvent event) {
                String text = ((Text)event.widget).getText();
                text = text.substring(0, event.start) + event.text + text.substring(event.end);
                event.doit = FLOAT_PATTERN.matcher(text).matches();
            }
        };
        l = new Label(deviceGroup, SWT.None);
        l.setText("x dpi");
        final Text deviceXDpiText = new Text(deviceGroup, SWT.BORDER);
        deviceXDpiText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (Float.isNaN(mXDpi) == false) {
            deviceXDpiText.setText(String.format("%.1f", mXDpi)); 
        }
        deviceXDpiText.addVerifyListener(floatVerifier);
        deviceXDpiText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String value = deviceXDpiText.getText();
                if (value.length() == 0) {
                    mXDpi = Float.NaN;
                } else {
                    mXDpi = Float.parseFloat(value);
                }
            }
        });
        l = new Label(deviceGroup, SWT.None);
        l.setText("y dpi");
        final Text deviceYDpiText = new Text(deviceGroup, SWT.BORDER);
        deviceYDpiText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (Float.isNaN(mYDpi) == false) {
            deviceYDpiText.setText(String.format("%.1f", mYDpi)); 
        }
        deviceYDpiText.addVerifyListener(floatVerifier);
        deviceYDpiText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                String value = deviceYDpiText.getText();
                if (value.length() == 0) {
                    mYDpi = Float.NaN;
                } else {
                    mYDpi = Float.parseFloat(value);
                }
            }
        });
        Group configGroup = new Group(parent, SWT.NONE);
        configGroup.setText("Configuration");
        configGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
        configGroup.setLayout(new GridLayout(2, false));
        l = new Label(configGroup, SWT.None);
        l.setText("Name");
        final Text configNameText = new Text(configGroup, SWT.BORDER);
        configNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (mConfigName != null) {
            configNameText.setText(mConfigName);
        }
        configNameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                mConfigName = configNameText.getText().trim();
                validateOk();
            }
        });
        mConfigSelector = new ConfigurationSelector(configGroup, true );
        mConfigSelector.setQualifierFilter(new IQualifierFilter() {
            public boolean accept(ResourceQualifier qualifier) {
                if (qualifier instanceof LanguageQualifier ||
                        qualifier instanceof RegionQualifier ||
                        qualifier instanceof VersionQualifier) {
                    return false;
                }
                return true;
            }
        });
        mConfigSelector.setConfiguration(mConfig);
        GridData gd;
        mConfigSelector.setLayoutData(gd = new GridData(GridData.FILL_HORIZONTAL));
        gd.horizontalSpan = 2;
        gd.widthHint = ConfigurationSelector.WIDTH_HINT;
        gd.heightHint = ConfigurationSelector.HEIGHT_HINT;
        mConfigSelector.setOnChangeListener(new Runnable() {
            public void run() {
                if (mConfigSelector.getState() == ConfigurationState.OK) {
                    mConfigSelector.getConfiguration(mConfig);
                }
                validateOk();
            }
        });
        mStatusComposite = new Composite(parent, SWT.NONE);
        mStatusComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout gl = new GridLayout(2, false);
        mStatusComposite.setLayout(gl);
        gl.marginHeight = gl.marginWidth = 0;
        mStatusImage = new Label(mStatusComposite, SWT.NONE);
        mStatusLabel = new Label(mStatusComposite, SWT.NONE);
        mStatusLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        resetStatus();
    }
    @Override
    protected Control createContents(Composite parent) {
        Control c = super.createContents(parent);
        validateOk();
        return c;
    }
    private void resetStatus() {
        String displayString = Dialog.shortenText(
                String.format("Config: %1$s", mConfig.toString()),
                mStatusLabel);
        mStatusLabel.setText(displayString);
    }
    private void setError(String text) {
        String displayString = Dialog.shortenText(text, mStatusLabel);
        mStatusLabel.setText(displayString);
        mStatusImage.setImage(mError);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }
    private void validateOk() {
        if (mDeviceName == null || mDeviceName.length() == 0) {
            setError("Device name must not be empty");
            return;
        }
        if (mConfigName == null || mConfigName.length() == 0) {
            setError("Configuration name must not be empty");
            return;
        }
        ConfigurationState state = mConfigSelector.getState();
        switch (state) {
            case INVALID_CONFIG:
                ResourceQualifier invalidQualifier = mConfigSelector.getInvalidQualifier();
                setError(String.format(
                        "Invalid Configuration: %1$s has no filter set.",
                        invalidQualifier.getName()));
                return;
            case REGION_WITHOUT_LANGUAGE:
                setError("The Region qualifier requires the Language qualifier.");
                return;
        }
        mStatusImage.setImage(null);
        resetStatus();
        getButton(IDialogConstants.OK_ID).setEnabled(true);
        mStatusComposite.layout();
    }
}

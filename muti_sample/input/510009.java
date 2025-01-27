public class MainLaunchConfigTab extends AbstractLaunchConfigurationTab {
    public static final String LAUNCH_TAB_IMAGE = "mainLaunchTab.png"; 
    protected static final String EMPTY_STRING = ""; 
    protected Text mProjText;
    private Button mProjButton;
    private Combo mActivityCombo;
    private final ArrayList<Activity> mActivities = new ArrayList<Activity>();
    private WidgetListener mListener = new WidgetListener();
    private Button mDefaultActionButton;
    private Button mActivityActionButton;
    private Button mDoNothingActionButton;
    private int mLaunchAction = LaunchConfigDelegate.DEFAULT_LAUNCH_ACTION;
    private ProjectChooserHelper mProjectChooserHelper;
    private class WidgetListener implements ModifyListener, SelectionListener {
        public void modifyText(ModifyEvent e) {
            IProject project = checkParameters();
            loadActivities(project);
            setDirty(true);
        }
        public void widgetDefaultSelected(SelectionEvent e) {
        }
        public void widgetSelected(SelectionEvent e) {
            Object source = e.getSource();
            if (source == mProjButton) {
                handleProjectButtonSelected();
            } else {
                checkParameters();
            }
        }
    }
    public MainLaunchConfigTab() {
    }
    public void createControl(Composite parent) {
        mProjectChooserHelper = new ProjectChooserHelper(parent.getShell(),
                new NonLibraryProjectOnlyFilter());
        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        GridLayout topLayout = new GridLayout();
        topLayout.verticalSpacing = 0;
        comp.setLayout(topLayout);
        comp.setFont(font);
        createProjectEditor(comp);
        createVerticalSpacer(comp, 1);
        Group group = new Group(comp, SWT.NONE);
        group.setText("Launch Action:");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setFont(font);
        mDefaultActionButton = new Button(group, SWT.RADIO);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mDefaultActionButton.setLayoutData(gd);
        mDefaultActionButton.setText("Launch Default Activity");
        mDefaultActionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mDefaultActionButton.getSelection() == true) {
                    mLaunchAction = LaunchConfigDelegate.ACTION_DEFAULT;
                    mActivityCombo.setEnabled(false);
                    checkParameters();
                }
            }
        });
        mActivityActionButton = new Button(group, SWT.RADIO);
        mActivityActionButton.setText("Launch:");
        mActivityActionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mActivityActionButton.getSelection() == true) {
                    mLaunchAction = LaunchConfigDelegate.ACTION_ACTIVITY;
                    mActivityCombo.setEnabled(true);
                    checkParameters();
                }
            }
        });
        mActivityCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mActivityCombo.setLayoutData(gd);
        mActivityCombo.clearSelection();
        mActivityCombo.setEnabled(false);
        mActivityCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                checkParameters();
            }
        });
        mDoNothingActionButton = new Button(group, SWT.RADIO);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        mDoNothingActionButton.setLayoutData(gd);
        mDoNothingActionButton.setText("Do Nothing");
        mDoNothingActionButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (mDoNothingActionButton.getSelection() == true) {
                    mLaunchAction = LaunchConfigDelegate.ACTION_DO_NOTHING;
                    mActivityCombo.setEnabled(false);
                    checkParameters();
                }
            }
        });
    }
    public String getName() {
        return "Android";
    }
    @Override
    public Image getImage() {
        return AdtPlugin.getImageLoader().loadImage(LAUNCH_TAB_IMAGE, null);
    }
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, mProjText.getText());
        configuration.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, true);
        configuration.setAttribute(LaunchConfigDelegate.ATTR_LAUNCH_ACTION, mLaunchAction);
        int selection = mActivityCombo.getSelectionIndex();
        if (mActivities != null && selection >=0 && selection < mActivities.size()) {
            configuration.setAttribute(LaunchConfigDelegate.ATTR_ACTIVITY,
                    mActivities.get(selection).getName());
        }
        mapResources(configuration);
    }
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(LaunchConfigDelegate.ATTR_LAUNCH_ACTION,
                LaunchConfigDelegate.DEFAULT_LAUNCH_ACTION);
    }
    protected void createProjectEditor(Composite parent) {
        Font font = parent.getFont();
        Group group = new Group(parent, SWT.NONE);
        group.setText("Project:");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        group.setLayoutData(gd);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setFont(font);
        mProjText = new Text(group, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        mProjText.setLayoutData(gd);
        mProjText.setFont(font);
        mProjText.addModifyListener(mListener);
        mProjButton = createPushButton(group, "Browse...", null);
        mProjButton.addSelectionListener(mListener);
    }
    protected WidgetListener getDefaultListener() {
        return mListener;
    }
    protected IJavaProject getJavaProject(IJavaModel javaModel) {
        String projectName = mProjText.getText().trim();
        if (projectName.length() < 1) {
            return null;
        }
        return javaModel.getJavaProject(projectName);
    }
    protected void handleProjectButtonSelected() {
        IJavaProject javaProject = mProjectChooserHelper.chooseJavaProject(
                mProjText.getText().trim(),
                "Please select a project to launch");
        if (javaProject == null) {
            return;
        }
        String projectName = javaProject.getElementName();
        mProjText.setText(projectName);
        IProject project = javaProject.getProject();
        loadActivities(project);
    }
    public void initializeFrom(ILaunchConfiguration config) {
        String projectName = EMPTY_STRING;
        try {
            projectName = config.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
                    EMPTY_STRING);
        }
        catch (CoreException ce) {
        }
        mProjText.setText(projectName);
        IProject proj = mProjectChooserHelper.getAndroidProject(projectName);
        loadActivities(proj);
        mLaunchAction = LaunchConfigDelegate.DEFAULT_LAUNCH_ACTION;
        try {
            mLaunchAction = config.getAttribute(LaunchConfigDelegate.ATTR_LAUNCH_ACTION,
                    mLaunchAction);
        } catch (CoreException e) {
        }
        mDefaultActionButton.setSelection(mLaunchAction == LaunchConfigDelegate.ACTION_DEFAULT);
        mActivityActionButton.setSelection(mLaunchAction == LaunchConfigDelegate.ACTION_ACTIVITY);
        mDoNothingActionButton.setSelection(
                mLaunchAction == LaunchConfigDelegate.ACTION_DO_NOTHING);
        String activityName = EMPTY_STRING;
        try {
            activityName = config.getAttribute(LaunchConfigDelegate.ATTR_ACTIVITY, EMPTY_STRING);
        }
        catch (CoreException ce) {
        }
        if (mLaunchAction != LaunchConfigDelegate.ACTION_ACTIVITY) {
            mActivityCombo.setEnabled(false);
            mActivityCombo.clearSelection();
        } else {
            mActivityCombo.setEnabled(true);
            if (activityName == null || activityName.equals(EMPTY_STRING)) {
                mActivityCombo.clearSelection();
            } else if (mActivities != null && mActivities.size() > 0) {
                boolean found = false;
                for (int i = 0 ; i < mActivities.size() ; i++) {
                    if (activityName.equals(mActivities.get(i).getName())) {
                        found = true;
                        mActivityCombo.select(i);
                        break;
                    }
                }
                if (found == false) {
                    mActivityCombo.clearSelection();
                }
            }
        }
    }
    protected void mapResources(ILaunchConfigurationWorkingCopy config) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IJavaModel javaModel = JavaCore.create(workspaceRoot);
        IJavaProject javaProject = getJavaProject(javaModel);
        IResource[] resources = null;
        if (javaProject != null) {
            resources = AndroidLaunchController.getResourcesToMap(javaProject.getProject());
        }
        config.setMappedResources(resources);
    }
    private void loadActivities(IProject project) {
        if (project != null) {
            try {
                AndroidManifestParser manifestParser = AndroidManifestParser.parse(
                        BaseProjectHelper.getJavaProject(project), null ,
                        true , false );
                if (manifestParser != null) {
                    Activity[] activities = manifestParser.getActivities();
                    mActivities.clear();
                    mActivityCombo.removeAll();
                    for (Activity activity : activities) {
                        if (activity.isExported() && activity.hasAction()) {
                            mActivities.add(activity);
                            mActivityCombo.add(activity.getName());
                        }
                    }
                    if (mActivities.size() > 0) {
                        if (mLaunchAction == LaunchConfigDelegate.ACTION_ACTIVITY) {
                            mActivityCombo.setEnabled(true);
                        }
                    } else {
                        mActivityCombo.setEnabled(false);
                    }
                    mActivityCombo.clearSelection();
                    return;
                }
            } catch (CoreException e) {
            }
        }
        mActivityCombo.removeAll();
        mActivities.clear();
    }
    private IProject checkParameters() {
        try {
            String text = mProjText.getText();
            if (text.length() == 0) {
                setErrorMessage("Project Name is required!");
            } else if (text.matches("[a-zA-Z0-9_ \\.-]+") == false) {
                setErrorMessage("Project name contains unsupported characters!");
            } else {
                IJavaProject[] projects = mProjectChooserHelper.getAndroidProjects(null);
                IProject found = null;
                for (IJavaProject javaProject : projects) {
                    if (javaProject.getProject().getName().equals(text)) {
                        found = javaProject.getProject();
                        break;
                    }
                }
                if (found != null) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(String.format("There is no android project named '%1$s'",
                            text));
                }
                return found;
            }
        } finally {
            updateLaunchConfigurationDialog();
        }
        return null;
    }
}

public final class NewFilterAction extends CallableSystemAction {
    public NewFilterAction() {
        putValue(Action.SHORT_DESCRIPTION, "Create new filter");
    }
    public void performAction() {
        FilterTopComponent.findInstance().newFilter();
    }
    public String getName() {
        return NbBundle.getMessage(SaveFilterSettingsAction.class, "CTL_NewFilterAction");
    }
    @Override
    protected void initialize() {
        super.initialize();
    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    @Override
    protected boolean asynchronous() {
        return false;
    }
    @Override
    protected String iconResource() {
        return "com/sun/hotspot/igv/filterwindow/images/plus.gif";
    }
}

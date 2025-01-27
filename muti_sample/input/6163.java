public class ShowAction extends DelegateAction {
    public static final String VALUE_COMMAND = "show-command";
    public static final String VALUE_NAME = "Show Objects";
    public static final String VALUE_SMALL_ICON = "general/Zoom16.gif";
    public static final Integer VALUE_MNEMONIC = new Integer('S');
    public static final String VALUE_SHORT_DESCRIPTION = "Show Objects of this selected type";
    public static final String VALUE_LONG_DESCRIPTION = VALUE_SHORT_DESCRIPTION;
    public ShowAction() {
        super(VALUE_NAME, ActionManager.getIcon(VALUE_SMALL_ICON));
        putValue(Action.ACTION_COMMAND_KEY, VALUE_COMMAND);
        putValue(Action.SHORT_DESCRIPTION, VALUE_SHORT_DESCRIPTION);
        putValue(Action.LONG_DESCRIPTION, VALUE_LONG_DESCRIPTION);
        putValue(Action.MNEMONIC_KEY, VALUE_MNEMONIC);
    }
}

public abstract class XOperations extends JPanel implements ActionListener {
    public final static String OPERATION_INVOCATION_EVENT =
            "jam.xoperations.invoke.result";
    private java.util.List<NotificationListener> notificationListenersList;
    private Hashtable<JButton, OperationEntry> operationEntryTable;
    private XMBean mbean;
    private MBeanInfo mbeanInfo;
    private MBeansTab mbeansTab;
    public XOperations(MBeansTab mbeansTab) {
        super(new GridLayout(1, 1));
        this.mbeansTab = mbeansTab;
        operationEntryTable = new Hashtable<JButton, OperationEntry>();
        ArrayList<NotificationListener> l =
                new ArrayList<NotificationListener>(1);
        notificationListenersList =
                Collections.synchronizedList(l);
    }
    public void removeOperations() {
        removeAll();
    }
    public void loadOperations(XMBean mbean, MBeanInfo mbeanInfo) {
        this.mbean = mbean;
        this.mbeanInfo = mbeanInfo;
        MBeanOperationInfo operations[] = mbeanInfo.getOperations();
        invalidate();
        Component listeners[] = getComponents();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof JButton) {
                ((JButton) listeners[i]).removeActionListener(this);
            }
        }
        removeAll();
        setLayout(new BorderLayout());
        JButton methodButton;
        JLabel methodLabel;
        JPanel innerPanelLeft, innerPanelRight;
        JPanel outerPanelLeft, outerPanelRight;
        outerPanelLeft = new JPanel(new GridLayout(operations.length, 1));
        outerPanelRight = new JPanel(new GridLayout(operations.length, 1));
        for (int i = 0; i < operations.length; i++) {
            innerPanelLeft = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            innerPanelRight = new JPanel(new FlowLayout(FlowLayout.LEFT));
            String returnType = operations[i].getReturnType();
            if (returnType == null) {
                methodLabel = new JLabel("null", JLabel.RIGHT);
                if (JConsole.isDebug()) {
                    System.err.println(
                            "WARNING: The operation's return type " +
                            "shouldn't be \"null\". Check how the " +
                            "MBeanOperationInfo for the \"" +
                            operations[i].getName() + "\" operation has " +
                            "been defined in the MBean's implementation code.");
                }
            } else {
                methodLabel = new JLabel(
                        Utils.getReadableClassName(returnType), JLabel.RIGHT);
            }
            innerPanelLeft.add(methodLabel);
            if (methodLabel.getText().length() > 20) {
                methodLabel.setText(methodLabel.getText().
                        substring(methodLabel.getText().
                        lastIndexOf(".") + 1,
                        methodLabel.getText().length()));
            }
            methodButton = new JButton(operations[i].getName());
            methodButton.setToolTipText(operations[i].getDescription());
            boolean callable = isCallable(operations[i].getSignature());
            if (callable) {
                methodButton.addActionListener(this);
            } else {
                methodButton.setEnabled(false);
            }
            MBeanParameterInfo[] signature = operations[i].getSignature();
            OperationEntry paramEntry = new OperationEntry(operations[i],
                    callable,
                    methodButton,
                    this);
            operationEntryTable.put(methodButton, paramEntry);
            innerPanelRight.add(methodButton);
            if (signature.length == 0) {
                innerPanelRight.add(new JLabel("( )", JLabel.CENTER));
            } else {
                innerPanelRight.add(paramEntry);
            }
            outerPanelLeft.add(innerPanelLeft, BorderLayout.WEST);
            outerPanelRight.add(innerPanelRight, BorderLayout.CENTER);
        }
        add(outerPanelLeft, BorderLayout.WEST);
        add(outerPanelRight, BorderLayout.CENTER);
        validate();
    }
    private boolean isCallable(MBeanParameterInfo[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (!Utils.isEditableType(signature[i].getType())) {
                return false;
            }
        }
        return true;
    }
    public void actionPerformed(final ActionEvent e) {
        performInvokeRequest((JButton) e.getSource());
    }
    void performInvokeRequest(final JButton button) {
        final OperationEntry entryIf = operationEntryTable.get(button);
        new SwingWorker<Object, Void>() {
            @Override
            public Object doInBackground() throws Exception {
                return mbean.invoke(button.getText(),
                        entryIf.getParameters(), entryIf.getSignature());
            }
            @Override
            protected void done() {
                try {
                    Object result = get();
                    if (entryIf.getReturnType() != null &&
                            !entryIf.getReturnType().equals(Void.TYPE.getName()) &&
                            !entryIf.getReturnType().equals(Void.class.getName())) {
                        fireChangedNotification(OPERATION_INVOCATION_EVENT, button, result);
                    } else {
                        new ThreadDialog(
                                button,
                                Resources.getText("Method successfully invoked"),
                                Resources.getText("Info"),
                                JOptionPane.INFORMATION_MESSAGE).run();
                    }
                } catch (Throwable t) {
                    t = Utils.getActualException(t);
                    if (JConsole.isDebug()) {
                        t.printStackTrace();
                    }
                    new ThreadDialog(
                            button,
                            Resources.getText("Problem invoking") + " " +
                            button.getText() + " : " + t.toString(),
                            Resources.getText("Error"),
                            JOptionPane.ERROR_MESSAGE).run();
                }
            }
        }.execute();
    }
    public void addOperationsListener(NotificationListener nl) {
        notificationListenersList.add(nl);
    }
    public void removeOperationsListener(NotificationListener nl) {
        notificationListenersList.remove(nl);
    }
    private void fireChangedNotification(
            String type, Object source, Object handback) {
        Notification n = new Notification(type, source, 0);
        for (NotificationListener nl : notificationListenersList) {
            nl.handleNotification(n, handback);
        }
    }
    protected abstract MBeanOperationInfo[] updateOperations(MBeanOperationInfo[] operations);
}

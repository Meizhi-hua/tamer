public class CreateMBeanDialog extends InternalDialog
                implements ActionListener {
    JConsole jConsole;
    JComboBox connections;
    JButton createMBeanButton, unregisterMBeanButton, cancelButton;
    private static final String HOTSPOT_MBEAN =
        "sun.management.HotspotInternal";
    private static final String HOTSPOT_MBEAN_OBJECTNAME =
        "sun.management:type=HotspotInternal";
    public CreateMBeanDialog(JConsole jConsole) {
        super(jConsole, "JConsole: Hotspot MBeans", true);
        this.jConsole = jConsole;
        setAccessibleDescription(this,
                                 getText("Hotspot MBeans.dialog.accessibleDescription"));
        Container cp = getContentPane();
        ((JComponent)cp).setBorder(new EmptyBorder(10, 10, 4, 10));
        JPanel centerPanel = new JPanel(new VariableGridLayout(0,
                                                        1,
                                                        4,
                                                        4,
                                                        false,
                                                        true));
        cp.add(centerPanel, BorderLayout.CENTER);
        connections = new JComboBox();
        updateConnections();
        centerPanel.add(new LabeledComponent(Resources.
                                             getText("Manage Hotspot MBeans "+
                                                     "in: "),
                                             connections));
        JPanel bottomPanel = new JPanel(new BorderLayout());
        cp.add(bottomPanel, BorderLayout.SOUTH);
        JPanel buttonPanel = new JPanel();
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        buttonPanel.add(createMBeanButton =
                        new JButton(Resources.getText("Create")));
        buttonPanel.add(unregisterMBeanButton =
                        new JButton(Resources.getText("Unregister")));
        buttonPanel.add(cancelButton =
                        new JButton(Resources.getText("Cancel")));
        statusBar = new JLabel(" ", JLabel.CENTER);
        bottomPanel.add(statusBar, BorderLayout.SOUTH);
        createMBeanButton.addActionListener(this);
        unregisterMBeanButton.addActionListener(this);
        cancelButton.addActionListener(this);
        LabeledComponent.layout(centerPanel);
        pack();
        setLocationRelativeTo(jConsole);
    }
    private void updateConnections() {
        List<VMInternalFrame> frames = jConsole.getInternalFrames();
        TreeSet<ProxyClient> data =
            new TreeSet<ProxyClient>(new Comparator<ProxyClient>() {
            public int compare(ProxyClient o1, ProxyClient o2) {
                return o1.connectionName().compareTo(o2.connectionName());
            }
        });
        if (frames.size() == 0) {
            JComponent cp = (JComponent)jConsole.getContentPane();
            Component comp = ((BorderLayout)cp.getLayout()).
                getLayoutComponent(BorderLayout.CENTER);
            if (comp instanceof VMPanel) {
                VMPanel vmpanel = (VMPanel) comp;
                ProxyClient client = vmpanel.getProxyClient(false);
                if (client != null && client.hasPlatformMXBeans()) {
                    data.add(client);
                }
            }
        } else {
            for (VMInternalFrame f : frames) {
                ProxyClient client = f.getVMPanel().getProxyClient(false);
                if (client != null && client.hasPlatformMXBeans()) {
                    data.add(client);
                }
            }
        }
        connections.invalidate();
        connections.setModel(new DefaultComboBoxModel(data.toArray()));
        connections.validate();
    }
    public void actionPerformed(final ActionEvent ev) {
        setVisible(false);
        statusBar.setText("");
        if (ev.getSource() != cancelButton) {
            new Thread("CreateMBeanDialog.actionPerformed") {
                    public void run() {
                        try {
                            StringBuffer buff = null;
                            Object c = connections.getSelectedItem();
                            if(c == null) return;
                            if(ev.getSource() == createMBeanButton) {
                                MBeanServerConnection connection =
                                    ((ProxyClient) c).
                                    getMBeanServerConnection();
                                connection.createMBean(HOTSPOT_MBEAN, null);
                            } else {
                                if(ev.getSource() == unregisterMBeanButton) {
                                    MBeanServerConnection connection =
                                        ((ProxyClient) c).
                                        getMBeanServerConnection();
                                    connection.unregisterMBean(new
                                        ObjectName(HOTSPOT_MBEAN_OBJECTNAME));
                                }
                            }
                            return;
                        } catch(InstanceAlreadyExistsException e) {
                            statusBar.setText(Resources.
                                              getText("Error: MBeans already "
                                                      + "exist"));
                        } catch(InstanceNotFoundException e) {
                            statusBar.setText(Resources.
                                              getText("Error: MBeans do not "
                                                      + "exist"));
                        } catch(Exception e) {
                            statusBar.setText(e.toString());
                        }
                        setVisible(true);
                    }
                }.start();
        }
    }
    public void setVisible(boolean b) {
        boolean wasVisible = isVisible();
        if(b) {
            setLocationRelativeTo(jConsole);
            invalidate();
            updateConnections();
            validate();
            repaint();
        }
        super.setVisible(b);
        if (b && !wasVisible) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    connections.requestFocus();
                }
            });
        }
    }
}

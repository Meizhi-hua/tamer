public class FileChooserDemo extends JPanel implements ActionListener {
    public static final String NIMBUS_LAF_NAME = "Nimbus";
    private static JFrame frame;
    private final List<SupportedLaF> supportedLaFs =
            new ArrayList<SupportedLaF>();
    private static SupportedLaF nimbusLaF;
    private static class SupportedLaF {
        private final String name;
        private final LookAndFeel laf;
        SupportedLaF(String name, LookAndFeel laf) {
            this.name = name;
            this.laf = laf;
        }
        @Override
        public String toString() {
            return name;
        }
    }
    private JButton showButton;
    private JCheckBox showAllFilesFilterCheckBox;
    private JCheckBox showImageFilesFilterCheckBox;
    private JCheckBox showFullDescriptionCheckBox;
    private JCheckBox useFileViewCheckBox;
    private JCheckBox useFileSystemViewCheckBox;
    private JCheckBox accessoryCheckBox;
    private JCheckBox setHiddenCheckBox;
    private JCheckBox useEmbedInWizardCheckBox;
    private JCheckBox useControlsCheckBox;
    private JCheckBox enableDragCheckBox;
    private JRadioButton singleSelectionRadioButton;
    private JRadioButton multiSelectionRadioButton;
    private JRadioButton openRadioButton;
    private JRadioButton saveRadioButton;
    private JRadioButton customButton;
    private JComboBox lafComboBox;
    private JRadioButton justFilesRadioButton;
    private JRadioButton justDirectoriesRadioButton;
    private JRadioButton bothFilesAndDirectoriesRadioButton;
    private JTextField customField;
    private final ExampleFileView fileView;
    private final ExampleFileSystemView fileSystemView;
    private final static Dimension hpad10 = new Dimension(10, 1);
    private final static Dimension vpad20 = new Dimension(1, 20);
    private final static Dimension vpad7 = new Dimension(1, 7);
    private final static Dimension vpad4 = new Dimension(1, 4);
    private final static Insets insets = new Insets(5, 10, 0, 10);
    private final FilePreviewer previewer;
    private final JFileChooser chooser;
    @SuppressWarnings("LeakingThisInConstructor")
    public FileChooserDemo() {
        UIManager.LookAndFeelInfo[] installedLafs = UIManager.
                getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lafInfo : installedLafs) {
            try {
                Class<?> lnfClass = Class.forName(lafInfo.getClassName());
                LookAndFeel laf = (LookAndFeel) (lnfClass.newInstance());
                if (laf.isSupportedLookAndFeel()) {
                    String name = lafInfo.getName();
                    SupportedLaF supportedLaF = new SupportedLaF(name, laf);
                    supportedLaFs.add(supportedLaF);
                    if (NIMBUS_LAF_NAME.equals(name)) {
                        nimbusLaF = supportedLaF;
                    }
                }
            } catch (Exception ignored) {
            }
        }
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        chooser = new JFileChooser();
        previewer = new FilePreviewer(chooser);
        fileView = new ExampleFileView();
        fileView.putIcon("jpg", new ImageIcon(getClass().getResource(
                "/resources/images/jpgIcon.jpg")));
        fileView.putIcon("gif", new ImageIcon(getClass().getResource(
                "/resources/images/gifIcon.gif")));
        fileSystemView = new ExampleFileSystemView();
        OptionListener optionListener = new OptionListener();
        openRadioButton = new JRadioButton("Open");
        openRadioButton.setSelected(true);
        openRadioButton.addActionListener(optionListener);
        saveRadioButton = new JRadioButton("Save");
        saveRadioButton.addActionListener(optionListener);
        customButton = new JRadioButton("Custom");
        customButton.addActionListener(optionListener);
        customField = new JTextField(8) {
            @Override
            public Dimension getMaximumSize() {
                return new Dimension(getPreferredSize().width,
                        getPreferredSize().height);
            }
        };
        customField.setText("Doit");
        customField.setAlignmentY(JComponent.TOP_ALIGNMENT);
        customField.setEnabled(false);
        customField.addActionListener(optionListener);
        ButtonGroup group1 = new ButtonGroup();
        group1.add(openRadioButton);
        group1.add(saveRadioButton);
        group1.add(customButton);
        showAllFilesFilterCheckBox = new JCheckBox("Show \"All Files\" Filter");
        showAllFilesFilterCheckBox.addActionListener(optionListener);
        showAllFilesFilterCheckBox.setSelected(true);
        showImageFilesFilterCheckBox = new JCheckBox("Show JPG and GIF Filters");
        showImageFilesFilterCheckBox.addActionListener(optionListener);
        showImageFilesFilterCheckBox.setSelected(false);
        accessoryCheckBox = new JCheckBox("Show Preview");
        accessoryCheckBox.addActionListener(optionListener);
        accessoryCheckBox.setSelected(false);
        setHiddenCheckBox = new JCheckBox("Show Hidden Files");
        setHiddenCheckBox.addActionListener(optionListener);
        showFullDescriptionCheckBox = new JCheckBox("With File Extensions");
        showFullDescriptionCheckBox.addActionListener(optionListener);
        showFullDescriptionCheckBox.setSelected(true);
        showFullDescriptionCheckBox.setEnabled(false);
        useFileViewCheckBox = new JCheckBox("Use FileView");
        useFileViewCheckBox.addActionListener(optionListener);
        useFileViewCheckBox.setSelected(false);
        useFileSystemViewCheckBox = new JCheckBox("Use FileSystemView", false);
        useFileSystemViewCheckBox.addActionListener(optionListener);
        useEmbedInWizardCheckBox = new JCheckBox("Embed in Wizard");
        useEmbedInWizardCheckBox.addActionListener(optionListener);
        useEmbedInWizardCheckBox.setSelected(false);
        useControlsCheckBox = new JCheckBox("Show Control Buttons");
        useControlsCheckBox.addActionListener(optionListener);
        useControlsCheckBox.setSelected(true);
        enableDragCheckBox = new JCheckBox("Enable Dragging");
        enableDragCheckBox.addActionListener(optionListener);
        ButtonGroup group3 = new ButtonGroup();
        justFilesRadioButton = new JRadioButton("Just Select Files");
        justFilesRadioButton.setSelected(true);
        group3.add(justFilesRadioButton);
        justFilesRadioButton.addActionListener(optionListener);
        justDirectoriesRadioButton = new JRadioButton("Just Select Directories");
        group3.add(justDirectoriesRadioButton);
        justDirectoriesRadioButton.addActionListener(optionListener);
        bothFilesAndDirectoriesRadioButton = new JRadioButton(
                "Select Files or Directories");
        group3.add(bothFilesAndDirectoriesRadioButton);
        bothFilesAndDirectoriesRadioButton.addActionListener(optionListener);
        singleSelectionRadioButton = new JRadioButton("Single Selection", true);
        singleSelectionRadioButton.addActionListener(optionListener);
        multiSelectionRadioButton = new JRadioButton("Multi Selection");
        multiSelectionRadioButton.addActionListener(optionListener);
        ButtonGroup group4 = new ButtonGroup();
        group4.add(singleSelectionRadioButton);
        group4.add(multiSelectionRadioButton);
        showButton = new JButton("Show FileChooser");
        showButton.addActionListener(this);
        showButton.setMnemonic('s');
        lafComboBox = new JComboBox(supportedLaFs.toArray());
        lafComboBox.setSelectedItem(nimbusLaF);
        lafComboBox.setEditable(false);
        lafComboBox.addActionListener(optionListener);
        JPanel control1 = new InsetPanel(insets);
        control1.setBorder(BorderFactory.createTitledBorder("Dialog Type"));
        control1.setLayout(new BoxLayout(control1, BoxLayout.Y_AXIS));
        control1.add(Box.createRigidArea(vpad20));
        control1.add(openRadioButton);
        control1.add(Box.createRigidArea(vpad7));
        control1.add(saveRadioButton);
        control1.add(Box.createRigidArea(vpad7));
        control1.add(customButton);
        control1.add(Box.createRigidArea(vpad4));
        JPanel fieldWrapper = new JPanel();
        fieldWrapper.setLayout(new BoxLayout(fieldWrapper, BoxLayout.X_AXIS));
        fieldWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        fieldWrapper.add(Box.createRigidArea(hpad10));
        fieldWrapper.add(Box.createRigidArea(hpad10));
        fieldWrapper.add(customField);
        control1.add(fieldWrapper);
        control1.add(Box.createRigidArea(vpad20));
        control1.add(Box.createGlue());
        JPanel control2 = new InsetPanel(insets);
        control2.setBorder(BorderFactory.createTitledBorder("Filter Controls"));
        control2.setLayout(new BoxLayout(control2, BoxLayout.Y_AXIS));
        control2.add(Box.createRigidArea(vpad20));
        control2.add(showAllFilesFilterCheckBox);
        control2.add(Box.createRigidArea(vpad7));
        control2.add(showImageFilesFilterCheckBox);
        control2.add(Box.createRigidArea(vpad4));
        JPanel checkWrapper = new JPanel();
        checkWrapper.setLayout(new BoxLayout(checkWrapper, BoxLayout.X_AXIS));
        checkWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkWrapper.add(Box.createRigidArea(hpad10));
        checkWrapper.add(Box.createRigidArea(hpad10));
        checkWrapper.add(showFullDescriptionCheckBox);
        control2.add(checkWrapper);
        control2.add(Box.createRigidArea(vpad20));
        control2.add(Box.createGlue());
        JPanel control3 = new InsetPanel(insets);
        control3.setBorder(BorderFactory.createTitledBorder("Display Options"));
        control3.setLayout(new BoxLayout(control3, BoxLayout.Y_AXIS));
        control3.add(Box.createRigidArea(vpad20));
        control3.add(setHiddenCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(useFileViewCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(useFileSystemViewCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(accessoryCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(useEmbedInWizardCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(useControlsCheckBox);
        control3.add(Box.createRigidArea(vpad7));
        control3.add(enableDragCheckBox);
        control3.add(Box.createRigidArea(vpad20));
        control3.add(Box.createGlue());
        JPanel control4 = new InsetPanel(insets);
        control4.setBorder(BorderFactory.createTitledBorder(
                "File and Directory Options"));
        control4.setLayout(new BoxLayout(control4, BoxLayout.Y_AXIS));
        control4.add(Box.createRigidArea(vpad20));
        control4.add(justFilesRadioButton);
        control4.add(Box.createRigidArea(vpad7));
        control4.add(justDirectoriesRadioButton);
        control4.add(Box.createRigidArea(vpad7));
        control4.add(bothFilesAndDirectoriesRadioButton);
        control4.add(Box.createRigidArea(vpad20));
        control4.add(singleSelectionRadioButton);
        control4.add(Box.createRigidArea(vpad7));
        control4.add(multiSelectionRadioButton);
        control4.add(Box.createRigidArea(vpad20));
        control4.add(Box.createGlue());
        JPanel panel = new JPanel();
        panel.add(new JLabel("Look and Feel: "));
        panel.add(lafComboBox);
        panel.add(showButton);
        JPanel wrapper = new JPanel();
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
        add(Box.createRigidArea(vpad20));
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(control1);
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(control2);
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(control3);
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(control4);
        wrapper.add(Box.createRigidArea(hpad10));
        wrapper.add(Box.createRigidArea(hpad10));
        add(wrapper);
        add(Box.createRigidArea(vpad20));
        add(panel);
        add(Box.createRigidArea(vpad20));
    }
    public void actionPerformed(ActionEvent e) {
        if (customButton.isSelected()) {
            chooser.setApproveButtonText(customField.getText());
        }
        if (chooser.isMultiSelectionEnabled()) {
            chooser.setSelectedFiles(null);
        } else {
            chooser.setSelectedFile(null);
        }
        JComponent accessory = chooser.getAccessory();
        if (accessory != null) {
            ((FilePreviewer) accessory).loadImage(null);
        }
        if (useEmbedInWizardCheckBox.isSelected()) {
            WizardDialog wizard = new WizardDialog(frame, true);
            wizard.setVisible(true);
            wizard.dispose();
            return;
        }
        int retval = chooser.showDialog(frame, null);
        if (retval == APPROVE_OPTION) {
            JOptionPane.showMessageDialog(frame, getResultString());
        } else if (retval == CANCEL_OPTION) {
            JOptionPane.showMessageDialog(frame,
                    "User cancelled operation. No file was chosen.");
        } else if (retval == ERROR_OPTION) {
            JOptionPane.showMessageDialog(frame,
                    "An error occured. No file was chosen.");
        } else {
            JOptionPane.showMessageDialog(frame, "Unknown operation occured.");
        }
    }
    private void resetFileFilters(boolean enableFilters,
            boolean showExtensionInDescription) {
        chooser.resetChoosableFileFilters();
        if (enableFilters) {
            FileFilter jpgFilter = createFileFilter(
                    "JPEG Compressed Image Files",
                    showExtensionInDescription, "jpg");
            FileFilter gifFilter = createFileFilter("GIF Image Files",
                    showExtensionInDescription, "gif");
            FileFilter bothFilter = createFileFilter("JPEG and GIF Image Files",
                    showExtensionInDescription, "jpg",
                    "gif");
            chooser.addChoosableFileFilter(bothFilter);
            chooser.addChoosableFileFilter(jpgFilter);
            chooser.addChoosableFileFilter(gifFilter);
        }
    }
    private FileFilter createFileFilter(String description,
            boolean showExtensionInDescription, String... extensions) {
        if (showExtensionInDescription) {
            description = createFileNameFilterDescriptionFromExtensions(
                    description, extensions);
        }
        return new FileNameExtensionFilter(description, extensions);
    }
    private String createFileNameFilterDescriptionFromExtensions(
            String description, String[] extensions) {
        String fullDescription = (description == null) ? "(" : description
                + " (";
        fullDescription += "." + extensions[0];
        for (int i = 1; i < extensions.length; i++) {
            fullDescription += ", .";
            fullDescription += extensions[i];
        }
        fullDescription += ")";
        return fullDescription;
    }
    private class WizardDialog extends JDialog implements ActionListener {
        CardLayout cardLayout;
        JPanel cardPanel;
        JLabel messageLabel;
        JButton backButton, nextButton, closeButton;
        @SuppressWarnings("LeakingThisInConstructor")
        WizardDialog(JFrame frame, boolean modal) {
            super(frame, "Embedded JFileChooser Demo", modal);
            cardLayout = new CardLayout();
            cardPanel = new JPanel(cardLayout);
            getContentPane().add(cardPanel, BorderLayout.CENTER);
            messageLabel = new JLabel("", JLabel.CENTER);
            cardPanel.add(chooser, "fileChooser");
            cardPanel.add(messageLabel, "label");
            cardLayout.show(cardPanel, "fileChooser");
            chooser.addActionListener(this);
            JPanel buttonPanel = new JPanel();
            backButton = new JButton("< Back");
            nextButton = new JButton("Next >");
            closeButton = new JButton("Close");
            buttonPanel.add(backButton);
            buttonPanel.add(nextButton);
            buttonPanel.add(closeButton);
            getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            backButton.setEnabled(false);
            getRootPane().setDefaultButton(nextButton);
            backButton.addActionListener(this);
            nextButton.addActionListener(this);
            closeButton.addActionListener(this);
            pack();
            setLocationRelativeTo(frame);
        }
        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            String cmd = evt.getActionCommand();
            if (src == backButton) {
                back();
            } else if (src == nextButton) {
                FileChooserUI ui = chooser.getUI();
                if (ui instanceof BasicFileChooserUI) {
                    ((BasicFileChooserUI) ui).getApproveSelectionAction().
                            actionPerformed(null);
                } else {
                    next();
                }
            } else if (src == closeButton) {
                close();
            } else if (APPROVE_SELECTION.equals(cmd)) {
                next();
            } else if (CANCEL_SELECTION.equals(cmd)) {
                close();
            }
        }
        private void back() {
            backButton.setEnabled(false);
            nextButton.setEnabled(true);
            cardLayout.show(cardPanel, "fileChooser");
            getRootPane().setDefaultButton(nextButton);
            chooser.requestFocus();
        }
        private void next() {
            backButton.setEnabled(true);
            nextButton.setEnabled(false);
            messageLabel.setText(getResultString());
            cardLayout.show(cardPanel, "label");
            getRootPane().setDefaultButton(closeButton);
            closeButton.requestFocus();
        }
        private void close() {
            setVisible(false);
        }
        @Override
        public void dispose() {
            chooser.removeActionListener(this);
            cardPanel.remove(chooser);
            chooser.setVisible(true);
            super.dispose();
        }
    }
    private String getResultString() {
        String resultString;
        String filter;
        if (chooser.getFileFilter() == null) {
            filter = "";
        } else {
            filter = chooser.getFileFilter().getDescription();
        }
        String path = null;
        boolean isDirMode = (chooser.getFileSelectionMode() == DIRECTORIES_ONLY);
        boolean isMulti = chooser.isMultiSelectionEnabled();
        if (isMulti) {
            File[] files = chooser.getSelectedFiles();
            if (files != null && files.length > 0) {
                path = "";
                for (File file : files) {
                    path = path + "<br>" + file.getPath();
                }
            }
        } else {
            File file = chooser.getSelectedFile();
            if (file != null) {
                path = "<br>" + file.getPath();
            }
        }
        if (path != null) {
            path = path.replace(" ", "&nbsp;");
            filter = filter.replace(" ", "&nbsp;");
            resultString =
                    "<html>You chose " + (isMulti ? "these" : "this") + " " + (isDirMode ? (isMulti
                    ? "directories" : "directory")
                    : (isMulti ? "files" : "file")) + ": <code>" + path
                    + "</code><br><br>with filter: <br><code>" + filter;
        } else {
            resultString = "Nothing was chosen";
        }
        return resultString;
    }
    private class OptionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JComponent c = (JComponent) e.getSource();
            boolean selected = false;
            if (c instanceof JToggleButton) {
                selected = ((JToggleButton) c).isSelected();
            }
            if (c == openRadioButton) {
                chooser.setDialogType(OPEN_DIALOG);
                customField.setEnabled(false);
                repaint();
            } else if (c == useEmbedInWizardCheckBox) {
                useControlsCheckBox.setEnabled(!selected);
                useControlsCheckBox.setSelected(!selected);
                chooser.setControlButtonsAreShown(!selected);
            } else if (c == useControlsCheckBox) {
                chooser.setControlButtonsAreShown(selected);
            } else if (c == enableDragCheckBox) {
                chooser.setDragEnabled(selected);
            } else if (c == saveRadioButton) {
                chooser.setDialogType(SAVE_DIALOG);
                customField.setEnabled(false);
                repaint();
            } else if (c == customButton || c == customField) {
                customField.setEnabled(true);
                chooser.setDialogType(CUSTOM_DIALOG);
                repaint();
            } else if (c == showAllFilesFilterCheckBox) {
                chooser.setAcceptAllFileFilterUsed(selected);
            } else if (c == showImageFilesFilterCheckBox) {
                resetFileFilters(selected,
                        showFullDescriptionCheckBox.isSelected());
                showFullDescriptionCheckBox.setEnabled(selected);
            } else if (c == setHiddenCheckBox) {
                chooser.setFileHidingEnabled(!selected);
            } else if (c == accessoryCheckBox) {
                if (selected) {
                    chooser.setAccessory(previewer);
                } else {
                    chooser.setAccessory(null);
                }
            } else if (c == useFileViewCheckBox) {
                if (selected) {
                    chooser.setFileView(fileView);
                } else {
                    chooser.setFileView(null);
                }
            } else if (c == useFileSystemViewCheckBox) {
                if (selected) {
                    chooser.setFileSystemView(fileSystemView);
                } else {
                    chooser.setFileSystemView(FileSystemView.getFileSystemView());
                }
            } else if (c == showFullDescriptionCheckBox) {
                resetFileFilters(showImageFilesFilterCheckBox.isSelected(),
                        selected);
            } else if (c == justFilesRadioButton) {
                chooser.setFileSelectionMode(FILES_ONLY);
            } else if (c == justDirectoriesRadioButton) {
                chooser.setFileSelectionMode(DIRECTORIES_ONLY);
            } else if (c == bothFilesAndDirectoriesRadioButton) {
                chooser.setFileSelectionMode(FILES_AND_DIRECTORIES);
            } else if (c == singleSelectionRadioButton) {
                if (selected) {
                    chooser.setMultiSelectionEnabled(false);
                }
            } else if (c == multiSelectionRadioButton) {
                if (selected) {
                    chooser.setMultiSelectionEnabled(true);
                }
            } else if (c == lafComboBox) {
                SupportedLaF supportedLaF = ((SupportedLaF) lafComboBox.
                        getSelectedItem());
                LookAndFeel laf = supportedLaF.laf;
                try {
                    UIManager.setLookAndFeel(laf);
                    SwingUtilities.updateComponentTreeUI(frame);
                    if (chooser != null) {
                        SwingUtilities.updateComponentTreeUI(chooser);
                    }
                    frame.pack();
                } catch (UnsupportedLookAndFeelException exc) {
                    ((DefaultComboBoxModel) lafComboBox.getModel()).
                            removeElement(supportedLaF);
                }
            }
        }
    }
    private class FilePreviewer extends JComponent implements
            PropertyChangeListener {
        ImageIcon thumbnail = null;
        @SuppressWarnings("LeakingThisInConstructor")
        public FilePreviewer(JFileChooser fc) {
            setPreferredSize(new Dimension(100, 50));
            fc.addPropertyChangeListener(this);
        }
        public void loadImage(File f) {
            if (f == null) {
                thumbnail = null;
            } else {
                ImageIcon tmpIcon = new ImageIcon(f.getPath());
                if (tmpIcon.getIconWidth() > 90) {
                    thumbnail = new ImageIcon(
                            tmpIcon.getImage().getScaledInstance(90, -1,
                            Image.SCALE_DEFAULT));
                } else {
                    thumbnail = tmpIcon;
                }
            }
        }
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
                if (isShowing()) {
                    loadImage((File) e.getNewValue());
                    repaint();
                }
            }
        }
        @Override
        public void paint(Graphics g) {
            if (thumbnail != null) {
                int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
                int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;
                if (y < 0) {
                    y = 0;
                }
                if (x < 5) {
                    x = 5;
                }
                thumbnail.paintIcon(this, g, x, y);
            }
        }
    }
    public static void main(String s[]) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        for (LookAndFeelInfo info : UIManager.
                                getInstalledLookAndFeels()) {
                            if (NIMBUS_LAF_NAME.equals(info.getName())) {
                                UIManager.setLookAndFeel(info.getClassName());
                                break;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    FileChooserDemo panel = new FileChooserDemo();
                    frame = new JFrame("FileChooserDemo");
                    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                    frame.getContentPane().add("Center", panel);
                    frame.pack();
                    frame.setVisible(true);
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(FileChooserDemo.class.getName()).log(Level.SEVERE,
                    null,
                    ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(FileChooserDemo.class.getName()).log(Level.SEVERE,
                    null,
                    ex);
        }
    }
    private static class InsetPanel extends JPanel {
        Insets i;
        InsetPanel(Insets i) {
            this.i = i;
        }
        @Override
        public Insets getInsets() {
            return i;
        }
    }
}

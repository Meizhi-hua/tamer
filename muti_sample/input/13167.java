public class WindowsPlacesBar extends JToolBar
                              implements ActionListener, PropertyChangeListener {
    JFileChooser fc;
    JToggleButton[] buttons;
    ButtonGroup buttonGroup;
    File[] files;
    final Dimension buttonSize;
    public WindowsPlacesBar(JFileChooser fc, boolean isXPStyle) {
        super(JToolBar.VERTICAL);
        this.fc = fc;
        setFloatable(false);
        putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        boolean isXPPlatform = (OSInfo.getOSType() == OSInfo.OSType.WINDOWS &&
                OSInfo.getWindowsVersion().compareTo(OSInfo.WINDOWS_XP) >= 0);
        if (isXPStyle) {
            buttonSize = new Dimension(83, 69);
            putClientProperty("XPStyle.subAppName", "placesbar");
            setBorder(new EmptyBorder(1, 1, 1, 1));
        } else {
            buttonSize = new Dimension(83, isXPPlatform ? 65 : 54);
            setBorder(new BevelBorder(BevelBorder.LOWERED,
                                      UIManager.getColor("ToolBar.highlight"),
                                      UIManager.getColor("ToolBar.background"),
                                      UIManager.getColor("ToolBar.darkShadow"),
                                      UIManager.getColor("ToolBar.shadow")));
        }
        Color bgColor = new Color(UIManager.getColor("ToolBar.shadow").getRGB());
        setBackground(bgColor);
        FileSystemView fsv = fc.getFileSystemView();
        files = AccessController.doPrivileged(new PrivilegedAction<File[]>() {
            public File[] run() {
                return (File[]) ShellFolder.get("fileChooserShortcutPanelFolders");
            }
        });
        buttons = new JToggleButton[files.length];
        buttonGroup = new ButtonGroup();
        for (int i = 0; i < files.length; i++) {
            if (fsv.isFileSystemRoot(files[i])) {
                files[i] = fsv.createFileObject(files[i].getAbsolutePath());
            }
            String folderName = fsv.getSystemDisplayName(files[i]);
            int index = folderName.lastIndexOf(File.separatorChar);
            if (index >= 0 && index < folderName.length() - 1) {
                folderName = folderName.substring(index + 1);
            }
            Icon icon;
            if (files[i] instanceof ShellFolder) {
                ShellFolder sf = (ShellFolder)files[i];
                Image image = sf.getIcon(true);
                if (image == null) {
                    image = (Image) ShellFolder.get("shell32LargeIcon 1");
                }
                icon = image == null ? null : new ImageIcon(image, sf.getFolderType());
            } else {
                icon = fsv.getSystemIcon(files[i]);
            }
            buttons[i] = new JToggleButton(folderName, icon);
            if (isXPPlatform) {
                buttons[i].setText("<html><center>"+folderName+"</center></html>");
            }
            if (isXPStyle) {
                buttons[i].putClientProperty("XPStyle.subAppName", "placesbar");
            } else {
                Color fgColor = new Color(UIManager.getColor("List.selectionForeground").getRGB());
                buttons[i].setContentAreaFilled(false);
                buttons[i].setForeground(fgColor);
            }
            buttons[i].setMargin(new Insets(3, 2, 1, 2));
            buttons[i].setFocusPainted(false);
            buttons[i].setIconTextGap(0);
            buttons[i].setHorizontalTextPosition(JToggleButton.CENTER);
            buttons[i].setVerticalTextPosition(JToggleButton.BOTTOM);
            buttons[i].setAlignmentX(JComponent.CENTER_ALIGNMENT);
            buttons[i].setPreferredSize(buttonSize);
            buttons[i].setMaximumSize(buttonSize);
            buttons[i].addActionListener(this);
            add(buttons[i]);
            if (i < files.length-1 && isXPStyle) {
                add(Box.createRigidArea(new Dimension(1, 1)));
            }
            buttonGroup.add(buttons[i]);
        }
        doDirectoryChanged(fc.getCurrentDirectory());
    }
    protected void doDirectoryChanged(File f) {
        for (int i=0; i<buttons.length; i++) {
            JToggleButton b = buttons[i];
            if (files[i].equals(f)) {
                b.setSelected(true);
                break;
            } else if (b.isSelected()) {
                buttonGroup.remove(b);
                b.setSelected(false);
                buttonGroup.add(b);
            }
        }
    }
    public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (prop == JFileChooser.DIRECTORY_CHANGED_PROPERTY) {
            doDirectoryChanged(fc.getCurrentDirectory());
        }
    }
    public void actionPerformed(ActionEvent e) {
        JToggleButton b = (JToggleButton)e.getSource();
        for (int i=0; i<buttons.length; i++) {
            if (b == buttons[i]) {
                fc.setCurrentDirectory(files[i]);
                break;
            }
        }
    }
    public Dimension getPreferredSize() {
        Dimension min  = super.getMinimumSize();
        Dimension pref = super.getPreferredSize();
        int h = min.height;
        if (buttons != null && buttons.length > 0 && buttons.length < 5) {
            JToggleButton b = buttons[0];
            if (b != null) {
                int bh = 5 * (b.getPreferredSize().height + 1);
                if (bh > h) {
                    h = bh;
                }
            }
        }
        if (h > pref.height) {
            pref = new Dimension(pref.width, h);
        }
        return pref;
    }
}

public final class Font2DTest extends JPanel
    implements ActionListener, ItemListener, ChangeListener {
    private final JFrame parent;
    private final FontPanel fp;
    private final RangeMenu rm;
    private final ChoiceV2 fontMenu;
    private final JTextField sizeField;
    private final ChoiceV2 styleMenu;
    private final ChoiceV2 textMenu;
    private int currentTextChoice = 0;
    private final ChoiceV2 transformMenu;
    private final ChoiceV2 transformMenuG2;
    private final ChoiceV2 methodsMenu;
    private final JComboBox antiAliasMenu;
    private final JComboBox fracMetricsMenu;
    private final JSlider contrastSlider;
    private CheckboxMenuItemV2 displayGridCBMI;
    private CheckboxMenuItemV2 force16ColsCBMI;
    private CheckboxMenuItemV2 showFontInfoCBMI;
    private JDialog userTextDialog;
    private JTextArea userTextArea;
    private JDialog printDialog;
    private JDialog fontInfoDialog;
    private LabelV2 fontInfos[] = new LabelV2[2];
    private JFileChooser filePromptDialog = null;
    private ButtonGroup printCBGroup;
    private JRadioButton printModeCBs[] = new JRadioButton[3];
    private final LabelV2 statusBar;
    private int fontStyles [] = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC};
    private String tFileName;
    private static boolean canDisplayCheck = true;
    public Font2DTest( JFrame f, boolean isApplet ) {
        parent = f;
        rm = new RangeMenu( this, parent );
        fp = new FontPanel( this, parent );
        statusBar = new LabelV2("");
        fontMenu = new ChoiceV2( this, canDisplayCheck );
        sizeField = new JTextField( "12", 3 );
        sizeField.addActionListener( this );
        styleMenu = new ChoiceV2( this );
        textMenu = new ChoiceV2( ); 
        transformMenu = new ChoiceV2( this );
        transformMenuG2 = new ChoiceV2( this );
        methodsMenu = new ChoiceV2( this );
        antiAliasMenu =
            new JComboBox(EnumSet.allOf(FontPanel.AAValues.class).toArray());
        antiAliasMenu.addActionListener(this);
        fracMetricsMenu =
            new JComboBox(EnumSet.allOf(FontPanel.FMValues.class).toArray());
        fracMetricsMenu.addActionListener(this);
        contrastSlider = new JSlider(JSlider.HORIZONTAL, 100, 250,
                                 FontPanel.getDefaultLCDContrast().intValue());
        contrastSlider.setEnabled(false);
        contrastSlider.setMajorTickSpacing(20);
        contrastSlider.setMinorTickSpacing(10);
        contrastSlider.setPaintTicks(true);
        contrastSlider.setPaintLabels(true);
        contrastSlider.addChangeListener(this);
        setupPanel();
        setupMenu( isApplet );
        setupDialog( isApplet );
        if(canDisplayCheck) {
            fireRangeChanged();
        }
    }
    private void setupPanel() {
        GridBagLayout gbl = new GridBagLayout();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new Insets( 2, 0, 2, 2 );
        this.setLayout( gbl );
        addLabeledComponentToGBL( "Font: ", fontMenu, gbl, gbc, this );
        addLabeledComponentToGBL( "Size: ", sizeField, gbl, gbc, this );
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addLabeledComponentToGBL( "Font Transform:",
                                  transformMenu, gbl, gbc, this );
        gbc.gridwidth = 1;
        addLabeledComponentToGBL( "Range: ", rm, gbl, gbc, this );
        addLabeledComponentToGBL( "Style: ", styleMenu, gbl, gbc, this );
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addLabeledComponentToGBL( "Graphics Transform: ",
                                  transformMenuG2, gbl, gbc, this );
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        addLabeledComponentToGBL( "Method: ", methodsMenu, gbl, gbc, this );
        addLabeledComponentToGBL("", null, gbl, gbc, this);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addLabeledComponentToGBL( "Text to use:", textMenu, gbl, gbc, this );
        gbc.weightx=1;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        addLabeledComponentToGBL("LCD contrast: ",
                                  contrastSlider, gbl, gbc, this);
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        addLabeledComponentToGBL("Antialiasing: ",
                                  antiAliasMenu, gbl, gbc, this);
        gbc.anchor = GridBagConstraints.EAST;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        addLabeledComponentToGBL("Fractional metrics: ",
                                  fracMetricsMenu, gbl, gbc, this);
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets( 2, 0, 0, 2 );
        gbc.fill = GridBagConstraints.BOTH;
        gbl.setConstraints( fp, gbc );
        this.add( fp );
        gbc.weighty = 0;
        gbc.insets = new Insets( 0, 2, 0, 0 );
        gbl.setConstraints( statusBar, gbc );
        this.add( statusBar );
    }
    private void addLabeledComponentToGBL( String name,
                                           JComponent c,
                                           GridBagLayout gbl,
                                           GridBagConstraints gbc,
                                           Container target ) {
        LabelV2 l = new LabelV2( name );
        GridBagConstraints gbcLabel = (GridBagConstraints) gbc.clone();
        gbcLabel.insets = new Insets( 2, 2, 2, 0 );
        gbcLabel.gridwidth = 1;
        gbcLabel.weightx = 0;
        if ( c == null )
          c = new JLabel( "" );
        gbl.setConstraints( l, gbcLabel );
        target.add( l );
        gbl.setConstraints( c, gbc );
        target.add( c );
    }
    private void setupMenu( boolean isApplet ) {
        JMenu fileMenu = new JMenu( "File" );
        JMenu optionMenu = new JMenu( "Option" );
        fileMenu.add( new MenuItemV2( "Save Selected Options...", this ));
        fileMenu.add( new MenuItemV2( "Load Options...", this ));
        fileMenu.addSeparator();
        fileMenu.add( new MenuItemV2( "Save as PNG...", this ));
        fileMenu.add( new MenuItemV2( "Load PNG File to Compare...", this ));
        fileMenu.add( new MenuItemV2( "Page Setup...", this ));
        fileMenu.add( new MenuItemV2( "Print...", this ));
        fileMenu.addSeparator();
        if ( !isApplet )
          fileMenu.add( new MenuItemV2( "Exit", this ));
        else
          fileMenu.add( new MenuItemV2( "Close", this ));
        displayGridCBMI = new CheckboxMenuItemV2( "Display Grid", true, this );
        force16ColsCBMI = new CheckboxMenuItemV2( "Force 16 Columns", false, this );
        showFontInfoCBMI = new CheckboxMenuItemV2( "Display Font Info", false, this );
        optionMenu.add( displayGridCBMI );
        optionMenu.add( force16ColsCBMI );
        optionMenu.add( showFontInfoCBMI );
        JMenuBar mb = parent.getJMenuBar();
        if ( mb == null )
          mb = new JMenuBar();
        mb.add( fileMenu );
        mb.add( optionMenu );
        parent.setJMenuBar( mb );
        String fontList[] =
          GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for ( int i = 0; i < fontList.length; i++ )
          fontMenu.addItem( fontList[i] );
        fontMenu.setSelectedItem( "Dialog" );
        styleMenu.addItem( "Plain" );
        styleMenu.addItem( "Bold" );
        styleMenu.addItem( "Italic" );
        styleMenu.addItem( "Bold Italic" );
        transformMenu.addItem( "None" );
        transformMenu.addItem( "Scale" );
        transformMenu.addItem( "Shear" );
        transformMenu.addItem( "Rotate" );
        transformMenuG2.addItem( "None" );
        transformMenuG2.addItem( "Scale" );
        transformMenuG2.addItem( "Shear" );
        transformMenuG2.addItem( "Rotate" );
        methodsMenu.addItem( "drawString" );
        methodsMenu.addItem( "drawChars" );
        methodsMenu.addItem( "drawBytes" );
        methodsMenu.addItem( "drawGlyphVector" );
        methodsMenu.addItem( "TextLayout.draw" );
        methodsMenu.addItem( "GlyphVector.getOutline + draw" );
        methodsMenu.addItem( "TextLayout.getOutline + draw" );
        textMenu.addItem( "Unicode Range" );
        textMenu.addItem( "All Glyphs" );
        textMenu.addItem( "User Text" );
        textMenu.addItem( "Text File" );
        textMenu.addActionListener ( this ); 
    }
    private void setupDialog( boolean isApplet ) {
        if (!isApplet)
                filePromptDialog = new JFileChooser( );
        else
                filePromptDialog = null;
        userTextDialog = new JDialog( parent, "User Text", false );
        JPanel dialogTopPanel = new JPanel();
        JPanel dialogBottomPanel = new JPanel();
        LabelV2 message1 = new LabelV2( "Enter text below and then press update" );
        LabelV2 message2 = new LabelV2( "(Unicode char can be denoted by \\uXXXX)" );
        LabelV2 message3 = new LabelV2( "(Supplementary chars can be denoted by \\UXXXXXX)" );
        userTextArea = new JTextArea( "Font2DTest!" );
        ButtonV2 bUpdate = new ButtonV2( "Update", this );
        userTextArea.setFont( new Font( "dialog", Font.PLAIN, 12 ));
        dialogTopPanel.setLayout( new GridLayout( 3, 1 ));
        dialogTopPanel.add( message1 );
        dialogTopPanel.add( message2 );
        dialogTopPanel.add( message3 );
        dialogBottomPanel.add( bUpdate );
        JScrollPane userTextAreaSP = new JScrollPane(userTextArea);
        userTextAreaSP.setPreferredSize(new Dimension(300, 100));
        userTextDialog.getContentPane().setLayout( new BorderLayout() );
        userTextDialog.getContentPane().add( "North", dialogTopPanel );
        userTextDialog.getContentPane().add( "Center", userTextAreaSP );
        userTextDialog.getContentPane().add( "South", dialogBottomPanel );
        userTextDialog.pack();
        userTextDialog.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                userTextDialog.hide();
            }
        });
        printCBGroup = new ButtonGroup();
        printModeCBs[ fp.ONE_PAGE ] =
          new JRadioButton( "Print one page from currently displayed character/line",
                         true );
        printModeCBs[ fp.CUR_RANGE ] =
          new JRadioButton( "Print all characters in currently selected range",
                         false );
        printModeCBs[ fp.ALL_TEXT ] =
          new JRadioButton( "Print all lines of text",
                         false );
        LabelV2 l =
          new LabelV2( "Note: Page range in native \"Print\" dialog will not affect the result" );
        JPanel buttonPanel = new JPanel();
        printModeCBs[ fp.ALL_TEXT ].setEnabled( false );
        buttonPanel.add( new ButtonV2( "Print", this ));
        buttonPanel.add( new ButtonV2( "Cancel", this ));
        printDialog = new JDialog( parent, "Print...", true );
        printDialog.setResizable( false );
        printDialog.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                printDialog.hide();
            }
        });
        printDialog.getContentPane().setLayout( new GridLayout( printModeCBs.length + 2, 1 ));
        printDialog.getContentPane().add( l );
        for ( int i = 0; i < printModeCBs.length; i++ ) {
            printCBGroup.add( printModeCBs[i] );
            printDialog.getContentPane().add( printModeCBs[i] );
        }
        printDialog.getContentPane().add( buttonPanel );
        printDialog.pack();
        fontInfoDialog = new JDialog( parent, "Font info", false );
        fontInfoDialog.setResizable( false );
        fontInfoDialog.addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                fontInfoDialog.hide();
                showFontInfoCBMI.setState( false );
            }
        });
        JPanel fontInfoPanel = new JPanel();
        fontInfoPanel.setLayout( new GridLayout( fontInfos.length, 1 ));
        for ( int i = 0; i < fontInfos.length; i++ ) {
            fontInfos[i] = new LabelV2("");
            fontInfoPanel.add( fontInfos[i] );
        }
        fontInfoDialog.getContentPane().add( fontInfoPanel );
        userTextDialog.setLocation( 200, 300 );
        fontInfoDialog.setLocation( 0, 400 );
    }
    public void fireRangeChanged() {
        int range[] = rm.getSelectedRange();
        fp.setTextToDraw( fp.RANGE_TEXT, range, null, null );
        if(canDisplayCheck) {
            setupFontList(range[0], range[1]);
        }
        if ( showFontInfoCBMI.getState() )
          fireUpdateFontInfo();
    }
    public void fireChangeStatus( String message, boolean error ) {
        statusBar.setText( message );
        if ( error )
          fp.showingError = true;
        else
          fp.showingError = false;
    }
    public void fireUpdateFontInfo() {
        if ( showFontInfoCBMI.getState() ) {
            String infos[] = fp.getFontInfo();
            for ( int i = 0; i < fontInfos.length; i++ )
              fontInfos[i].setText( infos[i] );
            fontInfoDialog.pack();
        }
    }
    private void setupFontList(int rangeStart, int rangeEnd) {
        int listCount = fontMenu.getItemCount();
        int size = 16;
        try {
            size =  Float.valueOf(sizeField.getText()).intValue();
        }
        catch ( Exception e ) {
            System.out.println("Invalid font size in the size textField. Using default value of 16");
        }
        int style = fontStyles[styleMenu.getSelectedIndex()];
        Font f;
        for (int i = 0; i < listCount; i++) {
            String fontName = (String)fontMenu.getItemAt(i);
            f = new Font(fontName, style, size);
            if ((rm.getSelectedIndex() != RangeMenu.SURROGATES_AREA_INDEX) &&
                canDisplayRange(f, rangeStart, rangeEnd)) {
                fontMenu.setBit(i, true);
            }
            else {
                fontMenu.setBit(i, false);
            }
        }
        fontMenu.repaint();
    }
    protected boolean canDisplayRange(Font font, int rangeStart, int rangeEnd) {
        for (int i = rangeStart; i < rangeEnd; i++) {
            if (font.canDisplay(i)) {
                return true;
            }
        }
        return false;
    }
    private String promptFile( boolean isSave, String initFileName ) {
        int retVal;
        String str;
        if ( filePromptDialog == null)
                return null;
        if ( isSave ) {
            filePromptDialog.setDialogType( JFileChooser.SAVE_DIALOG );
            filePromptDialog.setDialogTitle( "Save..." );
            str = "Save";
        }
        else {
            filePromptDialog.setDialogType( JFileChooser.OPEN_DIALOG );
            filePromptDialog.setDialogTitle( "Load..." );
            str = "Load";
        }
        if (initFileName != null)
                filePromptDialog.setSelectedFile( new File( initFileName ) );
        retVal = filePromptDialog.showDialog( this, str );
        if ( retVal == JFileChooser.APPROVE_OPTION ) {
                File file = filePromptDialog.getSelectedFile();
                String fileName = file.getAbsolutePath();
                if ( fileName != null ) {
                        return fileName;
                }
        }
        return null;
    }
    private String[] parseUserText( String orig ) {
        int length = orig.length();
        StringTokenizer perLine = new StringTokenizer( orig, "\n" );
        String textLines[] = new String[ perLine.countTokens() ];
        int lineNumber = 0;
        while ( perLine.hasMoreElements() ) {
            StringBuffer converted = new StringBuffer();
            String oneLine = perLine.nextToken();
            int lineLength = oneLine.length();
            int prevEscapeEnd = 0;
            int nextEscape = -1;
            do {
                int nextBMPEscape = oneLine.indexOf( "\\u", prevEscapeEnd );
                int nextSupEscape = oneLine.indexOf( "\\U", prevEscapeEnd );
                nextEscape = (nextBMPEscape < 0)
                    ? ((nextSupEscape < 0)
                       ? -1
                       : nextSupEscape)
                    : ((nextSupEscape < 0)
                       ? nextBMPEscape
                       : Math.min(nextBMPEscape, nextSupEscape));
                if ( nextEscape != -1 ) {
                    if ( prevEscapeEnd < nextEscape )
                        converted.append( oneLine.substring( prevEscapeEnd, nextEscape ));
                    prevEscapeEnd = nextEscape + (nextEscape == nextBMPEscape ? 6 : 8);
                    try {
                        String hex = oneLine.substring( nextEscape + 2, prevEscapeEnd );
                        if (nextEscape == nextBMPEscape) {
                            converted.append( (char) Integer.parseInt( hex, 16 ));
                        } else {
                            converted.append( new String( Character.toChars( Integer.parseInt( hex, 16 ))));
                        }
                    }
                    catch ( Exception e ) {
                        int copyLimit = Math.min(lineLength, prevEscapeEnd);
                        converted.append( oneLine.substring( nextEscape, copyLimit ));
                    }
                }
            } while (nextEscape != -1);
            if ( prevEscapeEnd < lineLength )
              converted.append( oneLine.substring( prevEscapeEnd, lineLength ));
            textLines[ lineNumber++ ] = converted.toString();
        }
        return textLines;
    }
    private void readTextFile( String fileName ) {
        try {
            String fileText, textLines[];
            BufferedInputStream bis =
              new BufferedInputStream( new FileInputStream( fileName ));
            int numBytes = bis.available();
            if (numBytes == 0) {
                throw new Exception("Text file " + fileName + " is empty");
            }
            byte byteData[] = new byte[ numBytes ];
            bis.read( byteData, 0, numBytes );
            bis.close();
            if (numBytes >= 2 &&
                (( byteData[0] == (byte) 0xFF && byteData[1] == (byte) 0xFE ) ||
                 ( byteData[0] == (byte) 0xFE && byteData[1] == (byte) 0xFF )))
              fileText = new String( byteData, "UTF-16" );
            else
              fileText = new String( byteData );
            int length = fileText.length();
            StringTokenizer perLine = new StringTokenizer( fileText, "\n" );
            for ( int i = 0; i < length; i++ ) {
                char iTh = fileText.charAt( i );
                if ( iTh == '\r' ) {
                    if ( i < length - 1 && fileText.charAt( i + 1 ) == '\n' )
                      perLine = new StringTokenizer( fileText, "\r\n" );
                    else
                      perLine = new StringTokenizer( fileText, "\r" );
                    break;
                }
                else if ( iTh == '\n' )
                  break;
            }
            int lineNumber = 0, numLines = perLine.countTokens();
            textLines = new String[ numLines ];
            while ( perLine.hasMoreElements() ) {
                String oneLine = perLine.nextToken();
                if ( oneLine == null )
                  oneLine = " ";
                textLines[ lineNumber++ ] = oneLine;
            }
            fp.setTextToDraw( fp.FILE_TEXT, null, null, textLines );
            rm.setEnabled( false );
            methodsMenu.setEnabled( false );
        }
        catch ( Exception ex ) {
            fireChangeStatus( "ERROR: Failed to Read Text File; See Stack Trace", true );
            ex.printStackTrace();
        }
    }
    private void writeCurrentOptions( String fileName ) {
        try {
            String curOptions = fp.getCurrentOptions();
            BufferedOutputStream bos =
              new BufferedOutputStream( new FileOutputStream( fileName ));
            int range[] = rm.getSelectedRange();
            String completeOptions =
              ( "Font2DTest Option File\n" +
                displayGridCBMI.getState() + "\n" +
                force16ColsCBMI.getState() + "\n" +
                showFontInfoCBMI.getState() + "\n" +
                rm.getSelectedItem() + "\n" +
                range[0] + "\n" + range[1] + "\n" + curOptions + tFileName);
            byte toBeWritten[] = completeOptions.getBytes( "UTF-16" );
            bos.write( toBeWritten, 0, toBeWritten.length );
            bos.close();
        }
        catch ( Exception ex ) {
            fireChangeStatus( "ERROR: Failed to Save Options File; See Stack Trace", true );
            ex.printStackTrace();
        }
    }
    private void updateGUI() {
        int selectedText = textMenu.getSelectedIndex();
        if ( selectedText == fp.USER_TEXT )
          userTextDialog.show();
        else
          userTextDialog.hide();
        printModeCBs[ fp.ONE_PAGE ].setSelected( true );
        if ( selectedText == fp.FILE_TEXT || selectedText == fp.USER_TEXT ) {
            if ( selectedText == fp.FILE_TEXT )
                methodsMenu.setSelectedItem("TextLayout.draw");
            methodsMenu.setEnabled( selectedText == fp.USER_TEXT );
            printModeCBs[ fp.CUR_RANGE ].setEnabled( false );
            printModeCBs[ fp.ALL_TEXT ].setEnabled( true );
        }
        else {
            if ( selectedText == fp.ALL_GLYPHS )
                methodsMenu.setSelectedItem("drawGlyphVector");
            methodsMenu.setEnabled( selectedText == fp.RANGE_TEXT );
            printModeCBs[ fp.CUR_RANGE ].setEnabled( true );
            printModeCBs[ fp.ALL_TEXT ].setEnabled( false );
        }
        if ( selectedText == fp.RANGE_TEXT ) {
            fontInfos[1].setVisible( true );
            rm.setEnabled( true );
        }
        else {
            fontInfos[1].setVisible( false );
            rm.setEnabled( false );
        }
    }
    private void loadOptions( String fileName ) {
        try {
            BufferedInputStream bis =
              new BufferedInputStream( new FileInputStream( fileName ));
            int numBytes = bis.available();
            byte byteData[] = new byte[ numBytes ];
            bis.read( byteData, 0, numBytes );
            bis.close();
            if ( numBytes < 2 ||
                (byteData[0] != (byte) 0xFE || byteData[1] != (byte) 0xFF) )
              throw new Exception( "Not a Font2DTest options file" );
            String options = new String( byteData, "UTF-16" );
            StringTokenizer perLine = new StringTokenizer( options, "\n" );
            String title = perLine.nextToken();
            if ( !title.equals( "Font2DTest Option File" ))
              throw new Exception( "Not a Font2DTest options file" );
            boolean displayGridOpt = Boolean.parseBoolean( perLine.nextToken() );
            boolean force16ColsOpt = Boolean.parseBoolean( perLine.nextToken() );
            boolean showFontInfoOpt = Boolean.parseBoolean( perLine.nextToken() );
            String rangeNameOpt = perLine.nextToken();
            int rangeStartOpt = Integer.parseInt( perLine.nextToken() );
            int rangeEndOpt = Integer.parseInt( perLine.nextToken() );
            String fontNameOpt = perLine.nextToken();
            float fontSizeOpt = Float.parseFloat( perLine.nextToken() );
            int fontStyleOpt = Integer.parseInt( perLine.nextToken() );
            int fontTransformOpt = Integer.parseInt( perLine.nextToken() );
            int g2TransformOpt = Integer.parseInt( perLine.nextToken() );
            int textToUseOpt = Integer.parseInt( perLine.nextToken() );
            int drawMethodOpt = Integer.parseInt( perLine.nextToken() );
            int antialiasOpt = Integer.parseInt(perLine.nextToken());
            int fractionalOpt = Integer.parseInt(perLine.nextToken());
            int lcdContrast = Integer.parseInt(perLine.nextToken());
            String userTextOpt[] = { "Font2DTest!" };
            String dialogEntry = "Font2DTest!";
            if (textToUseOpt == fp.USER_TEXT )  {
                int numLines = perLine.countTokens(), lineNumber = 0;
                if ( numLines != 0 ) {
                    userTextOpt = new String[ numLines ];
                    dialogEntry = "";
                    for ( ; perLine.hasMoreElements(); lineNumber++ ) {
                        userTextOpt[ lineNumber ] = perLine.nextToken();
                        dialogEntry += userTextOpt[ lineNumber ] + "\n";
                    }
                }
            }
            displayGridCBMI.setState( displayGridOpt );
            force16ColsCBMI.setState( force16ColsOpt );
            showFontInfoCBMI.setState( showFontInfoOpt );
            rm.setSelectedRange( rangeNameOpt, rangeStartOpt, rangeEndOpt );
            fontMenu.setSelectedItem( fontNameOpt );
            sizeField.setText( String.valueOf( fontSizeOpt ));
            styleMenu.setSelectedIndex( fontStyleOpt );
            transformMenu.setSelectedIndex( fontTransformOpt );
            transformMenuG2.setSelectedIndex( g2TransformOpt );
            textMenu.setSelectedIndex( textToUseOpt );
            methodsMenu.setSelectedIndex( drawMethodOpt );
            antiAliasMenu.setSelectedIndex( antialiasOpt );
            fracMetricsMenu.setSelectedIndex( fractionalOpt );
            contrastSlider.setValue(lcdContrast);
            userTextArea.setText( dialogEntry );
            updateGUI();
            if ( textToUseOpt == fp.FILE_TEXT ) {
              tFileName = perLine.nextToken();
              readTextFile(tFileName );
            }
            fp.loadOptions( displayGridOpt, force16ColsOpt,
                            rangeStartOpt, rangeEndOpt,
                            fontNameOpt, fontSizeOpt,
                            fontStyleOpt, fontTransformOpt, g2TransformOpt,
                            textToUseOpt, drawMethodOpt,
                            antialiasOpt, fractionalOpt,
                            lcdContrast, userTextOpt );
            if ( showFontInfoOpt ) {
                fireUpdateFontInfo();
                fontInfoDialog.show();
            }
            else
              fontInfoDialog.hide();
        }
        catch ( Exception ex ) {
            fireChangeStatus( "ERROR: Failed to Load Options File; See Stack Trace", true );
            ex.printStackTrace();
        }
    }
    private void loadComparisonPNG( String fileName ) {
        try {
            BufferedImage image =
                javax.imageio.ImageIO.read(new File(fileName));
            JFrame f = new JFrame( "Comparison PNG" );
            ImagePanel ip = new ImagePanel( image );
            f.setResizable( false );
            f.getContentPane().add( ip );
            f.addWindowListener( new WindowAdapter() {
                public void windowClosing( WindowEvent e ) {
                    ( (JFrame) e.getSource() ).dispose();
                }
            });
            f.pack();
            f.show();
        }
        catch ( Exception ex ) {
            fireChangeStatus( "ERROR: Failed to Load PNG File; See Stack Trace", true );
            ex.printStackTrace();
        }
    }
    public void actionPerformed( ActionEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JMenuItem ) {
            JMenuItem mi = (JMenuItem) source;
            String itemName = mi.getText();
            if ( itemName.equals( "Save Selected Options..." )) {
                String fileName = promptFile( true, "options.txt" );
                if ( fileName != null )
                  writeCurrentOptions( fileName );
            }
            else if ( itemName.equals( "Load Options..." )) {
                String fileName = promptFile( false, "options.txt" );
                if ( fileName != null )
                  loadOptions( fileName );
            }
            else if ( itemName.equals( "Save as PNG..." )) {
                String fileName = promptFile( true, fontMenu.getSelectedItem() + ".png" );
                if ( fileName != null )
                  fp.doSavePNG( fileName );
            }
            else if ( itemName.equals( "Load PNG File to Compare..." )) {
                String fileName = promptFile( false, null );
                if ( fileName != null )
                  loadComparisonPNG( fileName );
            }
            else if ( itemName.equals( "Page Setup..." ))
              fp.doPageSetup();
            else if ( itemName.equals( "Print..." ))
              printDialog.show();
            else if ( itemName.equals( "Close" ))
              parent.dispose();
            else if ( itemName.equals( "Exit" ))
              System.exit(0);
        }
        else if ( source instanceof JTextField ) {
            JTextField tf = (JTextField) source;
            float sz = 12f;
            try {
                 sz = Float.parseFloat(sizeField.getText());
                 if (sz < 1f || sz > 120f) {
                      sz = 12f;
                      sizeField.setText("12");
                 }
            } catch (Exception se) {
                 sizeField.setText("12");
            }
            if ( tf == sizeField )
              fp.setFontParams( fontMenu.getSelectedItem(),
                                sz,
                                styleMenu.getSelectedIndex(),
                                transformMenu.getSelectedIndex() );
        }
        else if ( source instanceof JButton ) {
            String itemName = ( (JButton) source ).getText();
            if ( itemName.equals( "Print" )) {
                for ( int i = 0; i < printModeCBs.length; i++ )
                  if ( printModeCBs[i].isSelected() ) {
                      printDialog.hide();
                      fp.doPrint( i );
                  }
            }
            else if ( itemName.equals( "Cancel" ))
              printDialog.hide();
            else if ( itemName.equals( "Update" ))
              fp.setTextToDraw( fp.USER_TEXT, null,
                                parseUserText( userTextArea.getText() ), null );
        }
        else if ( source instanceof JComboBox ) {
            JComboBox c = (JComboBox) source;
            if ( c == fontMenu || c == styleMenu || c == transformMenu ) {
                float sz = 12f;
                try {
                    sz = Float.parseFloat(sizeField.getText());
                    if (sz < 1f || sz > 120f) {
                        sz = 12f;
                        sizeField.setText("12");
                    }
                } catch (Exception se) {
                    sizeField.setText("12");
                }
                fp.setFontParams(fontMenu.getSelectedItem(),
                                 sz,
                                 styleMenu.getSelectedIndex(),
                                 transformMenu.getSelectedIndex());
            } else if ( c == methodsMenu )
              fp.setDrawMethod( methodsMenu.getSelectedIndex() );
            else if ( c == textMenu ) {
                if(canDisplayCheck) {
                    fireRangeChanged();
                }
                int selected = textMenu.getSelectedIndex();
                if ( selected == fp.RANGE_TEXT )
                  fp.setTextToDraw( fp.RANGE_TEXT, rm.getSelectedRange(),
                                    null, null );
                else if ( selected == fp.USER_TEXT )
                  fp.setTextToDraw( fp.USER_TEXT, null,
                                    parseUserText( userTextArea.getText() ), null );
                else if ( selected == fp.FILE_TEXT ) {
                    String fileName = promptFile( false, null );
                    if ( fileName != null ) {
                      tFileName = fileName;
                      readTextFile( fileName );
                    } else {
                        c.setSelectedIndex( currentTextChoice );
                        return;
                    }
                }
                else if ( selected == fp.ALL_GLYPHS )
                  fp.setTextToDraw( fp.ALL_GLYPHS, null, null, null );
                updateGUI();
                currentTextChoice = selected;
            }
            else if ( c == transformMenuG2 ) {
                fp.setTransformG2( transformMenuG2.getSelectedIndex() );
            }
            else if (c == antiAliasMenu || c == fracMetricsMenu) {
                if (c == antiAliasMenu) {
                    boolean enabled = FontPanel.AAValues.
                        isLCDMode(antiAliasMenu.getSelectedItem());
                        contrastSlider.setEnabled(enabled);
                }
                fp.setRenderingHints(antiAliasMenu.getSelectedItem(),
                                     fracMetricsMenu.getSelectedItem(),
                                     contrastSlider.getValue());
            }
        }
    }
    public void stateChanged(ChangeEvent e) {
         Object source = e.getSource();
         if (source instanceof JSlider) {
             fp.setRenderingHints(antiAliasMenu.getSelectedItem(),
                                  fracMetricsMenu.getSelectedItem(),
                                  contrastSlider.getValue());
         }
    }
    public void itemStateChanged( ItemEvent e ) {
        Object source = e.getSource();
        if ( source instanceof JCheckBoxMenuItem ) {
            JCheckBoxMenuItem cbmi = (JCheckBoxMenuItem) source;
            if ( cbmi == displayGridCBMI )
              fp.setGridDisplay( displayGridCBMI.getState() );
            else if ( cbmi == force16ColsCBMI )
              fp.setForce16Columns( force16ColsCBMI.getState() );
            else if ( cbmi == showFontInfoCBMI ) {
                if ( showFontInfoCBMI.getState() ) {
                    fireUpdateFontInfo();
                    fontInfoDialog.show();
                }
                else
                  fontInfoDialog.hide();
            }
        }
    }
    private static void printUsage() {
        String usage = "Usage: java -jar Font2DTest.jar [options]\n" +
            "\nwhere options include:\n" +
            "    -dcdc | -disablecandisplaycheck disable canDisplay check for font\n" +
            "    -?    | -help                   print this help message\n" +
            "\nExample :\n" +
            "     To disable canDisplay check on font for ranges\n" +
            "     java -jar Font2DTest.jar -dcdc";
        System.out.println(usage);
        System.exit(0);
    }
    public static void main(String argv[]) {
        if(argv.length > 0) {
            if(argv[0].equalsIgnoreCase("-disablecandisplaycheck") ||
               argv[0].equalsIgnoreCase("-dcdc")) {
                canDisplayCheck = false;
            }
            else {
                printUsage();
            }
        }
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        final JFrame f = new JFrame( "Font2DTest" );
        final Font2DTest f2dt = new Font2DTest( f, false );
        f.addWindowListener( new WindowAdapter() {
            public void windowOpening( WindowEvent e ) { f2dt.repaint(); }
            public void windowClosing( WindowEvent e ) { System.exit(0); }
        });
        f.getContentPane().add( f2dt );
        f.pack();
        f.show();
    }
    private final class ImagePanel extends JPanel {
        private final BufferedImage bi;
        public ImagePanel( BufferedImage image ) {
            bi = image;
        }
        public Dimension getPreferredSize() {
            return new Dimension( bi.getWidth(), bi.getHeight() );
        }
        public void paintComponent( Graphics g ) {
            g.drawImage( bi, 0, 0, this );
        }
    }
    private final class ButtonV2 extends JButton {
        public ButtonV2( String name, ActionListener al ) {
            super( name );
            this.addActionListener( al );
        }
    }
    private final class ChoiceV2 extends JComboBox {
        private BitSet bitSet = null;
        public ChoiceV2() {;}
        public ChoiceV2( ActionListener al ) {
            super();
            this.addActionListener( al );
        }
        public ChoiceV2( ActionListener al, boolean fontChoice) {
            this(al);
            if(fontChoice) {
                setToolTipText("");
                bitSet = new BitSet();
                setRenderer(new ChoiceV2Renderer(this));
            }
        }
        public String getToolTipText() {
            int index = this.getSelectedIndex();
            String fontName = (String) this.getSelectedItem();
            if(fontName != null &&
               (textMenu.getSelectedIndex() == fp.RANGE_TEXT)) {
                if (getBit(index)) {
                    return "Font \"" + fontName + "\" can display some characters in \"" +
                        rm.getSelectedItem() + "\" range";
                }
                else {
                    return "Font \"" + fontName + "\" cannot display any characters in \"" +
                        rm.getSelectedItem() + "\" range";
                }
            }
            return super.getToolTipText();
        }
        public void setBit(int bitIndex, boolean value) {
            bitSet.set(bitIndex, value);
        }
        public boolean getBit(int bitIndex) {
            return bitSet.get(bitIndex);
        }
    }
    private final class ChoiceV2Renderer extends DefaultListCellRenderer {
        private ImageIcon yesImage, blankImage;
        private ChoiceV2 choice = null;
        public ChoiceV2Renderer(ChoiceV2 choice) {
            BufferedImage yes =
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = yes.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                               RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLUE);
            g.drawLine(0, 5, 3, 10);
            g.drawLine(1, 5, 4, 10);
            g.drawLine(3, 10, 10, 0);
            g.drawLine(4, 9, 9, 0);
            g.dispose();
            BufferedImage blank =
                new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
            yesImage = new ImageIcon(yes);
            blankImage = new ImageIcon(blank);
            this.choice = choice;
        }
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean isSelected,
                                                      boolean cellHasFocus) {
            if(textMenu.getSelectedIndex() == fp.RANGE_TEXT) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(index == -1) {
                    index = choice.getSelectedIndex();
                }
                if(choice.getBit(index)) {
                    setIcon(yesImage);
                }
                else {
                    setIcon(blankImage);
                }
            } else {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setIcon(blankImage);
            }
            return this;
        }
    }
    private final class LabelV2 extends JLabel {
        public LabelV2( String name ) {
            super( name );
        }
    }
    private final class MenuItemV2 extends JMenuItem {
        public MenuItemV2( String name, ActionListener al ) {
            super( name );
            this.addActionListener( al );
        }
    }
    private final class CheckboxMenuItemV2 extends JCheckBoxMenuItem {
        public CheckboxMenuItemV2( String name, boolean b, ItemListener il ) {
            super( name, b );
            this.addItemListener( il );
        }
    }
}

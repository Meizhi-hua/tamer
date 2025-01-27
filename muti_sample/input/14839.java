public class Inspector extends SAPanel {
  private JTree tree;
  private SimpleTreeModel model;
  private HistoryComboBox addressField;
  private JLabel statusLabel;
  private JButton            livenessButton;
  private ActionListener     livenessButtonListener;
  private ActionListener     showLivenessListener;
  private static final String computeLivenessText = "Compute Liveness";
  private static final String showLivenessText = "Show Liveness";
  private JLabel liveStatus;
  private LivenessPathList list = null;
  private Oop currentOop = null;
  public Inspector() {
    model = new SimpleTreeModel();
    tree = new JTree(model);
    setLayout(new BorderLayout());
    Box hbox = Box.createHorizontalBox();
    JButton button = new JButton("Previous Oop");
    button.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String text = addressField.getText();
          try {
            VM vm = VM.getVM();
            Address a = vm.getDebugger().parseAddress(text);
            OopHandle handle = a.addOffsetToAsOopHandle(-vm.getAddressSize());
            addressField.setText(handle.toString());
          } catch (Exception ex) {
          }
        }
      });
    hbox.add(button);
    hbox.add(new JLabel("Address / C++ Expression: "));
    addressField = new HistoryComboBox();
    hbox.add(addressField);
    statusLabel = new JLabel();
    hbox.add(statusLabel);
    Box hboxDown = Box.createHorizontalBox();
    hboxDown.add(Box.createGlue());
    livenessButton = new JButton(computeLivenessText);
    livenessButtonListener = new ActionListener() {
          public void actionPerformed(ActionEvent e) {
               if (currentOop != null) {
                  fireComputeLiveness();
               }
               return;
         }
    };
    showLivenessListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      fireShowLiveness();
      }
    };
    livenessButton.addActionListener(livenessButtonListener);
    hboxDown.add(livenessButton);
    hboxDown.add(Box.createGlue());
    liveStatus = new JLabel();
    hboxDown.add(liveStatus);
    hboxDown.add(Box.createGlue());
    add(hbox, BorderLayout.NORTH);
    add(hboxDown, BorderLayout.SOUTH);
    addressField.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          String text = addressField.getText();
          try {
            Address a = VM.getVM().getDebugger().parseAddress(text);
            int max_searches = 1000;
            int searches = 0;
            int offset = 0;
            Oop oop = null;
            if (a != null) {
              OopHandle handle = a.addOffsetToAsOopHandle(0);
              while (searches < max_searches) {
                searches++;
                if (RobustOopDeterminator.oopLooksValid(handle)) {
                  try {
                    oop = VM.getVM().getObjectHeap().newOop(handle);
                    addressField.setText(handle.toString());
                    break;
                  } catch (UnknownOopException ex) {
                  } catch (RuntimeException ex) {
                    ex.printStackTrace();
                  }
                }
                offset -= 4;
                handle = a.addOffsetToAsOopHandle(offset);
              }
            }
            if (oop != currentOop) {
              currentOop = oop;
              liveStatus.setText("");
              list = null;
              if (livenessButton.getText().equals(showLivenessText)) {
                livenessButton.setText(computeLivenessText);
                livenessButton.removeActionListener(showLivenessListener);
                livenessButton.addActionListener(livenessButtonListener);
              }
            }
            if (oop != null) {
              statusLabel.setText("");
              setRoot(new OopTreeNodeAdapter(oop, null));
              return;
            }
            Type t = VM.getVM().getTypeDataBase().guessTypeForAddress(a);
            if (t != null) {
              statusLabel.setText("");
              setRoot(new CTypeTreeNodeAdapter(a, t, null));
              return;
            }
            statusLabel.setText("<bad oop or unknown C++ object " + text + ">");
          }
          catch (NumberFormatException ex) {
              currentOop = null;
              liveStatus.setText("");
              list = null;
              if (livenessButton.getText().equals(showLivenessText)) {
                livenessButton.setText(computeLivenessText);
                livenessButton.removeActionListener(showLivenessListener);
                livenessButton.addActionListener(livenessButtonListener);
              }
            CPPExpressions.CastExpr cast = CPPExpressions.parseCast(text);
            if (cast != null) {
              TypeDataBase db = VM.getVM().getTypeDataBase();
              Type t = db.lookupType(cast.getType());
              if (t == null) {
                statusLabel.setText("<unknown C++ type \"" + cast.getType() + "\">");
              } else {
                try {
                  Address a = VM.getVM().getDebugger().parseAddress(cast.getAddress());
                  statusLabel.setText("");
                  setRoot(new CTypeTreeNodeAdapter(a, t, null));
                } catch (NumberFormatException ex2) {
                  statusLabel.setText("<bad address " + cast.getAddress() + ">");
                }
              }
              return;
            }
            CPPExpressions.StaticFieldExpr stat = CPPExpressions.parseStaticField(text);
            if (stat != null) {
              TypeDataBase db = VM.getVM().getTypeDataBase();
              Type t = db.lookupType(stat.getContainingType());
              if (t == null) {
                statusLabel.setText("<unknown C++ type \"" + stat.getContainingType() + "\">");
              } else {
                sun.jvm.hotspot.types.Field f = t.getField(stat.getFieldName(), true, false);
                if (f == null) {
                  statusLabel.setText("<unknown field \"" + stat.getFieldName() + "\" in type \"" +
                                      stat.getContainingType() + "\">");
                } else if (!f.isStatic()) {
                  statusLabel.setText("<field \"" + stat.getContainingType() + "::" +
                                      stat.getFieldName() + "\" was not static>");
                } else {
                  Type fieldType = f.getType();
                  if (fieldType.isPointerType()) {
                    fieldType = ((PointerType) fieldType).getTargetType();
                    Type typeGuess = db.guessTypeForAddress(f.getAddress());
                    if (typeGuess != null) {
                      fieldType = typeGuess;
                    }
                    statusLabel.setText("");
                    setRoot(new CTypeTreeNodeAdapter(f.getAddress(),
                                                     fieldType,
                                                     new NamedFieldIdentifier(text)));
                  } else {
                    statusLabel.setText("");
                    setRoot(new CTypeTreeNodeAdapter(f.getStaticFieldAddress(),
                                                     f.getType(),
                                                     new NamedFieldIdentifier(text)));
                  }
                }
              }
              return;
            }
            statusLabel.setText("<parse error>");
          }
          catch (AddressException ex) {
            ex.printStackTrace();
            currentOop = null;
            liveStatus.setText("");
            list = null;
            if (livenessButton.getText().equals(showLivenessText)) {
              livenessButton.setText(computeLivenessText);
              livenessButton.removeActionListener(showLivenessListener);
              livenessButton.addActionListener(livenessButtonListener);
            }
            statusLabel.setText("<bad address>");
          }
          catch (Exception ex) {
            ex.printStackTrace();
            currentOop = null;
            liveStatus.setText("");
            list = null;
            if (livenessButton.getText().equals(showLivenessText)) {
              livenessButton.setText(computeLivenessText);
              livenessButton.removeActionListener(showLivenessListener);
              livenessButton.addActionListener(livenessButtonListener);
            }
            statusLabel.setText("<error constructing oop>");
          }
        }
      });
    MouseListener ml = new MouseAdapter() {
        public void mousePressed(MouseEvent e) {
          int selRow = tree.getRowForLocation(e.getX(), e.getY());
          TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
          if(selRow != -1) {
            if (e.getClickCount() == 1 && (e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
              Object node = tree.getLastSelectedPathComponent();
              if (node != null && node instanceof SimpleTreeNode) {
                showInspector((SimpleTreeNode)node);
              }
            }
          }
        }
      };
    tree.addMouseListener(ml);
    JScrollPane scrollPane = new JScrollPane(tree);
    add(scrollPane, BorderLayout.CENTER);
  }
  public Inspector(final SimpleTreeNode root) {
    this();
    SwingUtilities.invokeLater( new Runnable() {
        public void run() {
          if (root instanceof OopTreeNodeAdapter) {
            final Oop oop = ((OopTreeNodeAdapter)root).getOop();
            addressField.setText(oop.getHandle().toString());
          }
          setRoot(root);
        }
      });
  }
  private void setRoot(SimpleTreeNode root) {
    model.setRoot(root);
  }
  private void fireComputeLiveness() {
    final Runnable cutoverButtonRunnable = new Runnable() {
        public void run() {
          list = LivenessAnalysis.computeAllLivenessPaths(currentOop);
          if (list == null) {
            liveStatus.setText("Oop is Dead");
          } else {
            liveStatus.setText("Oop is Alive");
            livenessButton.removeActionListener(livenessButtonListener);
            livenessButton.addActionListener(showLivenessListener);
            livenessButton.setEnabled(true);
            livenessButton.setText(showLivenessText);
          }
        }
      };
    if (VM.getVM().getRevPtrs() != null) {
      cutoverButtonRunnable.run();
    } else {
      final WorkerThread worker = new WorkerThread();
      worker.invokeLater(new Runnable() {
          public void run() {
            try {
              ReversePtrsAnalysis rev = new ReversePtrsAnalysis();
              rev.run();
              cutoverButtonRunnable.run();
            } finally {
              worker.shutdown();
            }
          }
        });
    }
  }
  private void fireShowLiveness() {
    if (list == null) {
      return; 
    }
    for (Iterator iter = listeners.iterator(); iter.hasNext(); ) {
      SAListener listener = (SAListener) iter.next();
      listener.showLiveness(currentOop, list);
    }
  }
}

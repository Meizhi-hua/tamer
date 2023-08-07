public class FrmComprobarPadrones extends javax.swing.JDialog {
    List<Firmante> listaFirmantes;
    JFrame vent;
    Usuario usuario;
    Padron padron;
    Procesoelectoral procesoelectoral;
    public FrmComprobarPadrones(JFrame vent, Usuario usuario) {
        super(vent, true);
        initComponents();
        listaFirmantes = new ArrayList<Firmante>();
        jtxfOrgPolitica.setEnabled(false);
        jtxfProcElectoral.setEnabled(false);
        jtxfCodPadron.setEnabled(false);
        this.vent = vent;
        this.usuario = usuario;
        this.setVisible(true);
    }
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jlblOrgPolitica = new javax.swing.JLabel();
        jtxfProcElectoral = new javax.swing.JTextField();
        jtxfCodPadron = new javax.swing.JTextField();
        jlblProcElectoral = new javax.swing.JLabel();
        jbtnCargarDatos = new javax.swing.JButton();
        jbtnBProcElect = new javax.swing.JButton();
        jlblNombre1 = new javax.swing.JLabel();
        jtxfOrgPolitica = new javax.swing.JTextField();
        jbtnBOrgPol = new javax.swing.JButton();
        jbtnBCodPad = new javax.swing.JButton();
        jbtnCancelar = new javax.swing.JButton();
        jbtnAceptar = new javax.swing.JButton();
        jPBusqueda = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jtblFirmantes = new javax.swing.JTable();
        jToolBar1 = new javax.swing.JToolBar();
        jButton1 = new javax.swing.JButton();
        jbtnBuscar1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(verisoft.GUI.VerisoftApp.class).getContext().getResourceMap(FrmComprobarPadrones.class);
        setTitle(resourceMap.getString("Form.title"));
        setName("Form");
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), resourceMap.getString("jPanel1.border.title")));
        jPanel1.setName("jPanel1");
        jlblOrgPolitica.setText(resourceMap.getString("jlblOrgPolitica.text"));
        jlblOrgPolitica.setName("jlblOrgPolitica");
        jtxfProcElectoral.setName("jtxfProcElectoral");
        jtxfCodPadron.setName("jtxfCodPadron");
        jtxfCodPadron.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jtxfCodPadronActionPerformed(evt);
            }
        });
        jlblProcElectoral.setText(resourceMap.getString("jlblProcElectoral.text"));
        jlblProcElectoral.setName("jlblProcElectoral");
        jbtnCargarDatos.setIcon(resourceMap.getIcon("jbtnCargarDatos.icon"));
        jbtnCargarDatos.setText(resourceMap.getString("jbtnCargarDatos.text"));
        jbtnCargarDatos.setName("jbtnCargarDatos");
        jbtnCargarDatos.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnCargarDatosbtnBuscarClick(evt);
            }
        });
        jbtnCargarDatos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCargarDatosActionPerformed(evt);
            }
        });
        jbtnBProcElect.setIcon(resourceMap.getIcon("jbtnBProcElect.icon"));
        jbtnBProcElect.setText(resourceMap.getString("jbtnBProcElect.text"));
        jbtnBProcElect.setName("jbtnBProcElect");
        jbtnBProcElect.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnBProcElectbtnBuscarClick(evt);
            }
        });
        jbtnBProcElect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnBProcElectActionPerformed(evt);
            }
        });
        jlblNombre1.setText(resourceMap.getString("jlblNombre1.text"));
        jlblNombre1.setName("jlblNombre1");
        jtxfOrgPolitica.setName("jtxfOrgPolitica");
        jbtnBOrgPol.setIcon(resourceMap.getIcon("jbtnBOrgPol.icon"));
        jbtnBOrgPol.setText(resourceMap.getString("jbtnBOrgPol.text"));
        jbtnBOrgPol.setName("jbtnBOrgPol");
        jbtnBOrgPol.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnBOrgPolbtnBuscarClick(evt);
            }
        });
        jbtnBOrgPol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnBOrgPolActionPerformed(evt);
            }
        });
        jbtnBCodPad.setIcon(resourceMap.getIcon("jbtnBCodPad.icon"));
        jbtnBCodPad.setText(resourceMap.getString("jbtnBCodPad.text"));
        jbtnBCodPad.setName("jbtnBCodPad");
        jbtnBCodPad.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnBCodPadbtnBuscarClick(evt);
            }
        });
        jbtnBCodPad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnBCodPadActionPerformed(evt);
            }
        });
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGap(41, 41, 41).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jlblOrgPolitica).addComponent(jlblNombre1).addComponent(jlblProcElectoral)).addGap(30, 30, 30).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addComponent(jtxfProcElectoral, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(26, 26, 26).addComponent(jbtnBProcElect)).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jtxfOrgPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jtxfCodPadron, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 27, Short.MAX_VALUE).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jbtnBOrgPol).addComponent(jbtnBCodPad)))).addGap(206, 206, 206)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap(662, Short.MAX_VALUE).addComponent(jbtnCargarDatos, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(39, 39, 39)));
        jPanel1Layout.setVerticalGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGap(24, 24, 24).addComponent(jlblOrgPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup().addContainerGap().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jtxfProcElectoral, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jbtnBProcElect)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 19, Short.MAX_VALUE).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jtxfOrgPolitica, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jlblNombre1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jbtnBOrgPol)).addGap(13, 13, 13))).addGap(5, 5, 5).addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPanel1Layout.createSequentialGroup().addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jtxfCodPadron, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jlblProcElectoral, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)).addGap(8, 8, 8).addComponent(jbtnCargarDatos, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)).addComponent(jbtnBCodPad)).addContainerGap()));
        jbtnCancelar.setIcon(resourceMap.getIcon("jbtnCancelar.icon"));
        jbtnCancelar.setText(resourceMap.getString("jbtnCancelar.text"));
        jbtnCancelar.setName("jbtnCancelar");
        jbtnCancelar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnCancelarbtnBuscarClick(evt);
            }
        });
        jbtnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnCancelarActionPerformed(evt);
            }
        });
        jbtnAceptar.setIcon(resourceMap.getIcon("jbtnAceptar.icon"));
        jbtnAceptar.setText(resourceMap.getString("jbtnAceptar.text"));
        jbtnAceptar.setName("jbtnAceptar");
        jbtnAceptar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnAceptarbtnBuscarClick(evt);
            }
        });
        jbtnAceptar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnAceptarActionPerformed(evt);
            }
        });
        jPBusqueda.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), resourceMap.getString("jPBusqueda.border.title")));
        jPBusqueda.setName("jPBusqueda");
        jScrollPane1.setName("jScrollPane1");
        jtblFirmantes.setModel(new javax.swing.table.DefaultTableModel(new Object[][] { { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null }, { null, null, null, null, null, null, null, null } }, new String[] { "DNI", "A. Paterno", "A. Materno", "Nombres", "Estado Civil", "Distrito", "Firma", "Huella" }) {
            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false };
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        jtblFirmantes.setName("jtblFirmantes");
        jtblFirmantes.setSurrendersFocusOnKeystroke(true);
        jtblFirmantes.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(jtblFirmantes);
        javax.swing.GroupLayout jPBusquedaLayout = new javax.swing.GroupLayout(jPBusqueda);
        jPBusqueda.setLayout(jPBusquedaLayout);
        jPBusquedaLayout.setHorizontalGroup(jPBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPBusquedaLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 727, Short.MAX_VALUE).addContainerGap()));
        jPBusquedaLayout.setVerticalGroup(jPBusquedaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(jPBusquedaLayout.createSequentialGroup().addContainerGap().addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 190, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(24, Short.MAX_VALUE)));
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1");
        jButton1.setFocusable(false);
        jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton1.setName("jButton1");
        jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton1);
        jbtnBuscar1.setIcon(null);
        jbtnBuscar1.setFocusable(false);
        jbtnBuscar1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jbtnBuscar1.setName("jbtnBuscar1");
        jbtnBuscar1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jbtnBuscar1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jbtnBuscar1btnBuscarClick(evt);
            }
        });
        jbtnBuscar1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jbtnBuscar1ActionPerformed(evt);
            }
        });
        jToolBar1.add(jbtnBuscar1);
        jButton2.setFocusable(false);
        jButton2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton2.setName("jButton2");
        jButton2.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton2);
        jButton3.setIcon(null);
        jButton3.setFocusable(false);
        jButton3.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jButton3.setName("jButton3");
        jButton3.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(jButton3);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(185, 185, 185).addComponent(jbtnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 271, Short.MAX_VALUE).addComponent(jbtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(169, 169, 169)).addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addContainerGap().addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addContainerGap()));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(12, 12, 12).addComponent(jPBusqueda, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(jbtnAceptar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jbtnCancelar, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        jPBusqueda.getAccessibleContext().setAccessibleName(resourceMap.getString("jPBusqueda.AccessibleContext.accessibleName"));
        pack();
    }
    private void jbtnCargarDatosbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    public byte[] convertFileToByte(String pathImagen) {
        File fileImagen = new File(pathImagen);
        byte[] bFile = new byte[(int) fileImagen.length()];
        try {
            FileInputStream fileInputStream = new FileInputStream(fileImagen);
            fileInputStream.read(bFile);
            fileInputStream.close();
            return bFile;
        } catch (Exception e) {
            return null;
        }
    }
    public ImageIcon loadImage(String path) {
        ImageIcon thumbnail = null;
        ImageIcon tmpIcon = new ImageIcon(path);
        if (tmpIcon != null) {
            if (tmpIcon.getIconWidth() > 90) {
                thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(90, -1, Image.SCALE_DEFAULT));
            } else {
                thumbnail = tmpIcon;
            }
        }
        return thumbnail;
    }
    private void cargarTabla(File file) {
        if (listaFirmantes.size() > 0) {
            int i = 0;
            for (Firmante firm : listaFirmantes) {
                jtblFirmantes.setValueAt(firm.getDni(), i, 0);
                jtblFirmantes.setValueAt(firm.getAppaterno(), i, 1);
                jtblFirmantes.setValueAt(firm.getApmaterno(), i, 2);
                jtblFirmantes.setValueAt(firm.getNombres(), i, 3);
                jtblFirmantes.setValueAt(firm.getEstado().getDescripcion(), i, 4);
                jtblFirmantes.setValueAt(firm.getDistrito().getNombre(), i, 5);
                jtblFirmantes.setValueAt(file + "/Firmas/" + firm.getDni(), i, 6);
                jtblFirmantes.setValueAt(file + "/Huellas/" + firm.getDni(), i, 7);
                i++;
            }
        } else {
        }
    }
    private void CargarDatos() {
        final JFileChooser fc = new JFileChooser();
        Padron padron = new PadronBL().getPadronfromCodigo(1);
        fc.setCurrentDirectory(new java.io.File("."));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            StringTokenizer sFirmante;
            Firmante oFirmante;
            byte[] firma;
            byte[] huelladigital;
            File file = fc.getSelectedFile();
            BufferedReader inputStream = null;
            try {
                inputStream = new BufferedReader(new FileReader(file + "/Lista.txt"));
                String linea;
                String dni;
                int numLinea = 1;
                while ((linea = inputStream.readLine()) != null) {
                    sFirmante = new StringTokenizer(linea, "-");
                    oFirmante = new Firmante();
                    oFirmante.setFechaanalisis(new Date());
                    oFirmante.setPadron(padron);
                    dni = sFirmante.nextToken().trim();
                    oFirmante.setDni(Integer.parseInt(dni));
                    oFirmante.setAppaterno(sFirmante.nextToken().trim());
                    oFirmante.setApmaterno(sFirmante.nextToken().trim());
                    oFirmante.setNombres(sFirmante.nextToken().trim());
                    oFirmante.setEstado(new EstadoBL().getEstadofromDescripcion(sFirmante.nextToken().trim()));
                    oFirmante.setDistrito(new DistritoBL().getDistrito(sFirmante.nextToken().trim()));
                    if (sFirmante.hasMoreTokens()) {
                    } else {
                        try {
                            firma = convertFileToByte(file + "/Firmas/" + dni + ".jpg");
                            oFirmante.setFirma(firma);
                        } catch (Exception ex) {
                        }
                        try {
                            huelladigital = convertFileToByte(file + "/Huellas/" + dni + ".jpg");
                            oFirmante.setHuelladigital(huelladigital);
                        } catch (Exception ex) {
                        }
                        listaFirmantes.add(oFirmante);
                    }
                    numLinea++;
                }
            } catch (IOException ex) {
                Logger.getLogger(FrmComprobarPadrones.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ex) {
                        Logger.getLogger(FrmComprobarPadrones.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            cargarTabla(file);
        }
    }
    private void jbtnCargarDatosActionPerformed(java.awt.event.ActionEvent evt) {
        CargarDatos();
    }
    private void jbtnBCodPadbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnBCodPadActionPerformed(java.awt.event.ActionEvent evt) {
    }
    private void jbtnAceptarbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnAceptarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }
    private void jbtnCancelarbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        dispose();
    }
    private void jtxfCodPadronActionPerformed(java.awt.event.ActionEvent evt) {
    }
    private void jbtnBProcElectActionPerformed(java.awt.event.ActionEvent evt) {
        FrmBuscarProcElectoral vent = new FrmBuscarProcElectoral(new JFrame(), usuario, FrmBuscarProcElectoral.SELECCIONAR);
        vent.setVisible(true);
        this.procesoelectoral = vent.getProcElegido();
    }
    private void jbtnBProcElectbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnBOrgPolActionPerformed(java.awt.event.ActionEvent evt) {
    }
    private void jbtnBOrgPolbtnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnBuscar1btnBuscarClick(java.awt.event.MouseEvent evt) {
    }
    private void jbtnBuscar1ActionPerformed(java.awt.event.ActionEvent evt) {
    }
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new FrmComprobarPadrones(new JFrame(), new Usuario()).setVisible(true);
            }
        });
    }
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JPanel jPBusqueda;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JButton jbtnAceptar;
    private javax.swing.JButton jbtnBCodPad;
    private javax.swing.JButton jbtnBOrgPol;
    private javax.swing.JButton jbtnBProcElect;
    private javax.swing.JButton jbtnBuscar1;
    private javax.swing.JButton jbtnCancelar;
    private javax.swing.JButton jbtnCargarDatos;
    private javax.swing.JLabel jlblNombre1;
    private javax.swing.JLabel jlblOrgPolitica;
    private javax.swing.JLabel jlblProcElectoral;
    private javax.swing.JTable jtblFirmantes;
    private javax.swing.JTextField jtxfCodPadron;
    private javax.swing.JTextField jtxfOrgPolitica;
    private javax.swing.JTextField jtxfProcElectoral;
}

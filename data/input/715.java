public class UDGM extends AbstractRadioMedium {
    private static Logger logger = Logger.getLogger(UDGM.class);
    public double SUCCESS_RATIO_TX = 1.0;
    public double SUCCESS_RATIO_RX = 1.0;
    public double TRANSMITTING_RANGE = 50;
    public double INTERFERENCE_RANGE = 100;
    private Simulation simulation;
    private DirectedGraphMedium dgrm;
    private Random random = null;
    public UDGM(Simulation simulation) {
        super(simulation);
        this.simulation = simulation;
        random = simulation.getRandomGenerator();
        dgrm = new DirectedGraphMedium() {
            protected void analyzeEdges() {
                clearEdges();
                for (Radio source : UDGM.this.getRegisteredRadios()) {
                    Position sourcePos = source.getPosition();
                    for (Radio dest : UDGM.this.getRegisteredRadios()) {
                        Position destPos = dest.getPosition();
                        if (source == dest) {
                            continue;
                        }
                        double distance = sourcePos.getDistanceTo(destPos);
                        if (distance < Math.max(TRANSMITTING_RANGE, INTERFERENCE_RANGE)) {
                            addEdge(new DirectedGraphMedium.Edge(source, new DestinationRadio(dest)));
                        }
                    }
                }
                super.analyzeEdges();
            }
        };
        final Observer positionObserver = new Observer() {
            public void update(Observable o, Object arg) {
                dgrm.requestEdgeAnalysis();
            }
        };
        simulation.getEventCentral().addMoteCountListener(new MoteCountListener() {
            public void moteWasAdded(Mote mote) {
                mote.getInterfaces().getPosition().addObserver(positionObserver);
                dgrm.requestEdgeAnalysis();
            }
            public void moteWasRemoved(Mote mote) {
                mote.getInterfaces().getPosition().deleteObserver(positionObserver);
                dgrm.requestEdgeAnalysis();
            }
        });
        for (Mote mote : simulation.getMotes()) {
            mote.getInterfaces().getPosition().addObserver(positionObserver);
        }
        dgrm.requestEdgeAnalysis();
        Visualizer.registerVisualizerSkin(UDGMVisualizerSkin.class);
    }
    public void setTxRange(double r) {
        TRANSMITTING_RANGE = r;
        dgrm.requestEdgeAnalysis();
    }
    public void setInterferenceRange(double r) {
        INTERFERENCE_RANGE = r;
        dgrm.requestEdgeAnalysis();
    }
    public RadioConnection createConnections(Radio sender) {
        RadioConnection newConnection = new RadioConnection(sender);
        if (SUCCESS_RATIO_TX < 1.0 && random.nextDouble() > SUCCESS_RATIO_TX) {
            return newConnection;
        }
        double moteTransmissionRange = TRANSMITTING_RANGE * ((double) sender.getCurrentOutputPowerIndicator() / (double) sender.getOutputPowerIndicatorMax());
        double moteInterferenceRange = INTERFERENCE_RANGE * ((double) sender.getCurrentOutputPowerIndicator() / (double) sender.getOutputPowerIndicatorMax());
        DestinationRadio[] potentialDestinations = dgrm.getPotentialDestinations(sender);
        if (potentialDestinations == null) {
            return newConnection;
        }
        Position senderPos = sender.getPosition();
        for (DestinationRadio dest : potentialDestinations) {
            Radio recv = dest.radio;
            Position recvPos = recv.getPosition();
            if (sender.getChannel() >= 0 && recv.getChannel() >= 0 && sender.getChannel() != recv.getChannel()) {
                continue;
            }
            double distance = senderPos.getDistanceTo(recvPos);
            if (distance <= moteTransmissionRange) {
                if (!recv.isReceiverOn()) {
                    newConnection.addInterfered(recv);
                    recv.interfereAnyReception();
                } else if (recv.isInterfered()) {
                    newConnection.addInterfered(recv);
                } else if (recv.isTransmitting()) {
                    newConnection.addInterfered(recv);
                } else if (recv.isReceiving() || (SUCCESS_RATIO_RX < 1.0 && random.nextDouble() > SUCCESS_RATIO_RX)) {
                    newConnection.addInterfered(recv);
                    recv.interfereAnyReception();
                    for (RadioConnection conn : getActiveConnections()) {
                        if (conn.isDestination(recv)) {
                            conn.addInterfered(recv);
                        }
                    }
                } else {
                    newConnection.addDestination(recv);
                }
            } else if (distance <= moteInterferenceRange) {
                newConnection.addInterfered(recv);
                recv.interfereAnyReception();
            }
        }
        return newConnection;
    }
    public void updateSignalStrengths() {
        for (Radio radio : getRegisteredRadios()) {
            radio.setCurrentSignalStrength(SS_NOTHING);
        }
        RadioConnection[] conns = getActiveConnections();
        for (RadioConnection conn : conns) {
            if (conn.getSource().getCurrentSignalStrength() < SS_STRONG) {
                conn.getSource().setCurrentSignalStrength(SS_STRONG);
            }
            for (Radio dstRadio : conn.getDestinations()) {
                double dist = conn.getSource().getPosition().getDistanceTo(dstRadio.getPosition());
                double maxTxDist = TRANSMITTING_RANGE * ((double) conn.getSource().getCurrentOutputPowerIndicator() / (double) conn.getSource().getOutputPowerIndicatorMax());
                double distFactor = dist / maxTxDist;
                double signalStrength = SS_STRONG + distFactor * (SS_WEAK - SS_STRONG);
                if (dstRadio.getCurrentSignalStrength() < signalStrength) {
                    dstRadio.setCurrentSignalStrength(signalStrength);
                }
            }
        }
        for (RadioConnection conn : conns) {
            for (Radio intfRadio : conn.getInterfered()) {
                double dist = conn.getSource().getPosition().getDistanceTo(intfRadio.getPosition());
                double maxTxDist = TRANSMITTING_RANGE * ((double) conn.getSource().getCurrentOutputPowerIndicator() / (double) conn.getSource().getOutputPowerIndicatorMax());
                double distFactor = dist / maxTxDist;
                if (distFactor < 1) {
                    double signalStrength = SS_STRONG + distFactor * (SS_WEAK - SS_STRONG);
                    if (intfRadio.getCurrentSignalStrength() < signalStrength) {
                        intfRadio.setCurrentSignalStrength(signalStrength);
                    }
                } else {
                    intfRadio.setCurrentSignalStrength(SS_WEAK);
                    if (intfRadio.getCurrentSignalStrength() < SS_WEAK) {
                        intfRadio.setCurrentSignalStrength(SS_WEAK);
                    }
                }
                if (!intfRadio.isInterfered()) {
                    intfRadio.interfereAnyReception();
                }
            }
        }
    }
    public Collection<Element> getConfigXML() {
        ArrayList<Element> config = new ArrayList<Element>();
        Element element;
        element = new Element("transmitting_range");
        element.setText(Double.toString(TRANSMITTING_RANGE));
        config.add(element);
        element = new Element("interference_range");
        element.setText(Double.toString(INTERFERENCE_RANGE));
        config.add(element);
        element = new Element("success_ratio_tx");
        element.setText("" + SUCCESS_RATIO_TX);
        config.add(element);
        element = new Element("success_ratio_rx");
        element.setText("" + SUCCESS_RATIO_RX);
        config.add(element);
        return config;
    }
    public boolean setConfigXML(Collection<Element> configXML, boolean visAvailable) {
        for (Element element : configXML) {
            if (element.getName().equals("transmitting_range")) {
                TRANSMITTING_RANGE = Double.parseDouble(element.getText());
            }
            if (element.getName().equals("interference_range")) {
                INTERFERENCE_RANGE = Double.parseDouble(element.getText());
            }
            if (element.getName().equals("success_ratio")) {
                SUCCESS_RATIO_TX = Double.parseDouble(element.getText());
                logger.warn("Loading old COOJA Config, XML element \"sucess_ratio\" parsed at \"sucess_ratio_tx\"");
            }
            if (element.getName().equals("success_ratio_tx")) {
                SUCCESS_RATIO_TX = Double.parseDouble(element.getText());
            }
            if (element.getName().equals("success_ratio_rx")) {
                SUCCESS_RATIO_RX = Double.parseDouble(element.getText());
            }
        }
        return true;
    }
}

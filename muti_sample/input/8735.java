class ChildSession extends Session {
    private Process process;
    private PrintWriter in;
    private BufferedReader out;
    private BufferedReader err;
    private InputListener input;
    private OutputListener output;
    private OutputListener error;
    public ChildSession(ExecutionManager runtime,
                        String userVMArgs, String cmdLine,
                        InputListener input,
                        OutputListener output,
                        OutputListener error,
                        OutputListener diagnostics) {
        this(runtime, getVM(diagnostics, userVMArgs, cmdLine),
             input, output, error, diagnostics);
    }
    public ChildSession(ExecutionManager runtime,
                        LaunchingConnector connector,
                        Map<String, Connector.Argument> arguments,
                        InputListener input,
                        OutputListener output,
                        OutputListener error,
                        OutputListener diagnostics) {
        this(runtime, generalGetVM(diagnostics, connector, arguments),
             input, output, error, diagnostics);
    }
    private ChildSession(ExecutionManager runtime,
                        VirtualMachine vm,
                        InputListener input,
                        OutputListener output,
                        OutputListener error,
                        OutputListener diagnostics) {
        super(vm, runtime, diagnostics);
        this.input = input;
        this.output = output;
        this.error = error;
    }
    @Override
    public boolean attach() {
        if (!connectToVMProcess()) {
            diagnostics.putString("Could not launch VM");
            return false;
        }
        OutputReader outputReader =
            new OutputReader("output reader", "output",
                             out, output, diagnostics);
        outputReader.setPriority(Thread.MAX_PRIORITY-1);
        outputReader.start();
        OutputReader errorReader =
            new OutputReader("error reader", "error",
                             err, error, diagnostics);
        errorReader.setPriority(Thread.MAX_PRIORITY-1);
        errorReader.start();
        InputWriter inputWriter =
            new InputWriter("input writer", in, input);
        inputWriter.setPriority(Thread.MAX_PRIORITY-1);
        inputWriter.start();
        if (!super.attach()) {
            if (process != null) {
                process.destroy();
                process = null;
            }
            return false;
        }
        return true;
    }
    @Override
    public void detach() {
        super.detach();
        if (process != null) {
            process.destroy();
            process = null;
        }
    }
    static private void dumpStream(OutputListener diagnostics,
                                   InputStream stream) throws IOException {
        BufferedReader in =
            new BufferedReader(new InputStreamReader(stream));
        String line;
        while ((line = in.readLine()) != null) {
            diagnostics.putString(line);
        }
    }
    static private void dumpFailedLaunchInfo(OutputListener diagnostics,
                                             Process process) {
        try {
            dumpStream(diagnostics, process.getErrorStream());
            dumpStream(diagnostics, process.getInputStream());
        } catch (IOException e) {
            diagnostics.putString("Unable to display process output: " +
                                  e.getMessage());
        }
    }
    static private VirtualMachine getVM(OutputListener diagnostics,
                                        String userVMArgs,
                                        String cmdLine) {
        VirtualMachineManager manager = Bootstrap.virtualMachineManager();
        LaunchingConnector connector = manager.defaultConnector();
        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("options").setValue(userVMArgs);
        arguments.get("main").setValue(cmdLine);
        return generalGetVM(diagnostics, connector, arguments);
    }
    static private VirtualMachine generalGetVM(OutputListener diagnostics,
                                               LaunchingConnector connector,
                                               Map<String, Connector.Argument> arguments) {
        VirtualMachine vm = null;
        try {
            diagnostics.putString("Starting child.");
            vm = connector.launch(arguments);
        } catch (IOException ioe) {
            diagnostics.putString("Unable to start child: " + ioe.getMessage());
        } catch (IllegalConnectorArgumentsException icae) {
            diagnostics.putString("Unable to start child: " + icae.getMessage());
        } catch (VMStartException vmse) {
            diagnostics.putString("Unable to start child: " + vmse.getMessage() + '\n');
            dumpFailedLaunchInfo(diagnostics, vmse.process());
        }
        return vm;
    }
    private boolean connectToVMProcess() {
        if (vm == null) {
            return false;
        }
        process = vm.process();
        in = new PrintWriter(new OutputStreamWriter(process.getOutputStream()));
        out = new BufferedReader(new InputStreamReader(process.getInputStream()), 1);
        err = new BufferedReader(new InputStreamReader(process.getErrorStream()), 1);
        return true;
    }
    private static class OutputReader extends Thread {
        private String streamName;
        private BufferedReader stream;
        private OutputListener output;
        private OutputListener diagnostics;
        private boolean running = true;
        private char[] buffer = new char[512];
        OutputReader(String threadName,
                     String streamName,
                     BufferedReader stream,
                     OutputListener output,
                     OutputListener diagnostics) {
            super(threadName);
            this.streamName = streamName;
            this.stream = stream;
            this.output = output;
            this.diagnostics = diagnostics;
        }
        @Override
        public void run() {
            try {
                int count;
                while (running && (count = stream.read(buffer, 0, 512)) != -1) {
                    if (count > 0) {
                        final String chars = new String(buffer, 0, count);
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                output.putString(chars);
                            }
                        });
                    }
                }
            } catch (IOException e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        diagnostics.putString("IO error reading " +
                                              streamName +
                                              " stream of child java interpreter");
                    }
                });
            }
        }
    }
    private static class InputWriter extends Thread {
        private PrintWriter stream;
        private InputListener input;
        private boolean running = true;
        InputWriter(String threadName,
                    PrintWriter stream,
                    InputListener input) {
            super(threadName);
            this.stream = stream;
            this.input = input;
        }
        @Override
        public void run() {
            String line;
            while (running) {
                line = input.getLine();
                stream.println(line);
                stream.flush();
            }
        }
    }
}

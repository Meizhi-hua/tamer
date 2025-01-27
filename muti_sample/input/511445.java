public class Compile {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: Compile [log file] [output file]");
            System.exit(0);
        }
        Root root = new Root();
        List<Record> records = new ArrayList<Record>();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                new FileInputStream(args[0])));
        String line;
        int lineNumber = 0;
        while ((line = in.readLine()) != null) {
            lineNumber++;
            if (line.startsWith("I/PRELOAD")) {
                try {
                    String clipped = line.substring(19);
                    records.add(new Record(clipped, lineNumber));
                } catch (RuntimeException e) {
                    throw new RuntimeException(
                            "Exception while recording line " + lineNumber + ": " + line, e);
                }
            }
        }
        for (Record record : records) {
            root.indexProcess(record);
        }
        for (Record record : records) {
            root.indexClassOperation(record);
        }
        in.close();
        root.toFile(args[1]);
    }
}

public class Applet4 extends Applet {
    String name = "Applet 4 ";
    PrintService defServ = null;
    PrintService[] allServices = null;
    StreamPrintServiceFactory []fact = null;
    public void init() {
        PrintServiceLookup psl = new Applet4PrintServiceLookup();
        PrintServiceLookup.registerServiceProvider(psl);
        defServ = PrintServiceLookup.lookupDefaultPrintService();
        allServices = PrintServiceLookup.lookupPrintServices(null,null);
        fact = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(
              DocFlavor.SERVICE_FORMATTED.PRINTABLE,
              DocFlavor.BYTE_ARRAY.POSTSCRIPT.getMimeType());
    }
    public void paint(Graphics g) {
      g.drawString("This is " + name, 10, 20);
      g.drawString("Default print service="+defServ,10,40);
      g.drawString("Number of print services="+allServices.length,10,60);
      g.drawString("Number of PS stream services="+fact.length,10,80);
      int y=100;
      for (int i=0;i<allServices.length;i++) {
          if (allServices[i] instanceof Applet4PrintService ||
               allServices[i].getName().startsWith("Applet ")) {
               g.drawString("Found service : "+allServices[i].getName(),10,y);
               y+=20;
           }
      }
    }
    public static void main(String[] args) {
        Applet4 applet = new Applet4();
        applet.init();
        Frame f = new Frame("Print Lookup Test");
        f.add(applet);
        f.setSize(300,200);
        f.show();
        }
}

public class PYXScanner implements Scanner {
        public void resetDocumentLocator(String publicid, String systemid) {
        }
	public void scan(Reader r, ScanHandler h) throws IOException, SAXException {
		BufferedReader br = new BufferedReader(r);
		String s;
		char[] buff = null;
		boolean instag = false;
		while ((s = br.readLine()) != null) {
			int size = s.length();
			if (buff == null || buff.length < size) {
				buff = new char[size];
				}
			s.getChars(0, size, buff, 0);
			switch (buff[0]) {
			case '(':
				if (instag) {
					h.stagc(buff, 0, 0);
					instag = false;
					}
				h.gi(buff, 1, size - 1);
				instag = true;
				break;
			case ')':
				if (instag) {
					h.stagc(buff, 0, 0);
					instag = false;
					}
				h.etag(buff, 1, size - 1);
				break;
			case '?':
				if (instag) {
					h.stagc(buff, 0, 0);
					instag = false;
					}
				h.pi(buff, 1, size - 1);
				break;
			case 'A':
				int sp = s.indexOf(' ');
				h.aname(buff, 1, sp - 1);
				h.aval(buff, sp + 1, size - sp - 1);
				break;
			case '-':
				if (instag) {
					h.stagc(buff, 0, 0);
					instag = false;
					}
				if (s.equals("-\\n")) {
					buff[0] = '\n';
					h.pcdata(buff, 0, 1);
					}
				else {
					h.pcdata(buff, 1, size - 1);
					}
				break;
			case 'E':
				if (instag) {
					h.stagc(buff, 0, 0);
					instag = false;
					}
				h.entity(buff, 1, size - 1);
				break;
			default:
				break;
				}
			}
		h.eof(buff, 0, 0);
		}
	public void startCDATA() { }
	public static void main(String[] argv) throws IOException, SAXException {
		Scanner s = new PYXScanner();
		Reader r = new InputStreamReader(System.in, "UTF-8");
		Writer w = new BufferedWriter(new OutputStreamWriter(System.out, "UTF-8"));
		s.scan(r, new PYXWriter(w));
		}
	}

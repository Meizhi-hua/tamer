public class CharacterScript {
    static void fortest(String fmt, Object... o) {
    }
    static void print(String fmt, Object... o) {
        System.out.printf(fmt, o);
    }
    static void debug(String fmt, Object... o) {
    }
    public static void main(String args[]){
        try {
            if (args.length != 1) {
                System.out.println("java CharacterScript script.txt out");
                System.exit(1);
            }
            int i, j;
            BufferedReader sbfr = new BufferedReader(new FileReader(args[0]));
            HashMap<String,Integer> scriptMap = new HashMap<String,Integer>();
            String line = null;
            Matcher m = Pattern.compile("(\\p{XDigit}+)(?:\\.{2}(\\p{XDigit}+))?\\s+;\\s+(\\w+)\\s+#.*").matcher("");
            int prevS = -1;
            int prevE = -1;
            String prevN = null;
            int[][] scripts = new int[1024][3];
            int scriptSize = 0;
            while ((line = sbfr.readLine()) != null) {
                if (line.length() <= 1 || line.charAt(0) == '#') {
                    continue;
                }
                m.reset(line);
                if (m.matches()) {
                    int start = Integer.parseInt(m.group(1), 16);
                    int end = (m.group(2)==null)?start
                              :Integer.parseInt(m.group(2), 16);
                    String name = m.group(3);
                    if (name.equals(prevN) && start == prevE + 1) {
                        prevE = end;
                    } else {
                        if (prevS != -1) {
                            if (scriptMap.get(prevN) == null) {
                                scriptMap.put(prevN, scriptMap.size());
                            }
                            scripts[scriptSize][0] = prevS;
                            scripts[scriptSize][1] = prevE;
                            scripts[scriptSize][2] = scriptMap.get(prevN);
                            scriptSize++;
                        }
                        debug("%x-%x\t%s%n", prevS, prevE, prevN);
                        prevS = start; prevE = end; prevN = name;
                    }
                } else {
                    debug("Warning: Unrecognized line <%s>%n", line);
                }
            }
            if (scriptMap.get(prevN) == null) {
                scriptMap.put(prevN, scriptMap.size());
            }
            scripts[scriptSize][0] = prevS;
            scripts[scriptSize][1] = prevE;
            scripts[scriptSize][2] = scriptMap.get(prevN);
            scriptSize++;
            debug("%x-%x\t%s%n", prevS, prevE, prevN);
            debug("-----------------%n");
            debug("Total scripts=%s%n", scriptMap.size());
            debug("-----------------%n%n");
            String[] names = new String[scriptMap.size()];
            for (String name: scriptMap.keySet()) {
                names[scriptMap.get(name).intValue()] = name;
            }
            for (j = 0; j < scriptSize; j++) {
                for (int cp = scripts[j][0]; cp <= scripts[j][1]; cp++) {
                    String name = names[scripts[j][2]].toUpperCase(Locale.ENGLISH);;
                    if (cp > 0xffff)
                        System.out.printf("%05X    %s%n", cp, name);
                    else
                        System.out.printf("%05X    %s%n", cp, name);
                }
            }
            Arrays.sort(scripts, 0, scriptSize,
                        new Comparator<int[]>() {
                            public int compare(int[] a1, int[] a2) {
                                return a1[0] - a2[0];
                            }
                            public boolean compare(Object obj) {
                                return obj == this;
                            }
                         });
            ArrayList<int[]> list = new ArrayList();
            list.add(scripts[0]);
            int[] last = scripts[0];
            for (i = 1; i < scriptSize; i++) {
                if (scripts[i][0] != (last[1] + 1)) {
                    boolean isNotUnassigned = false;
                    for (int cp = last[1] + 1; cp < scripts[i][0]; cp++) {
                        if (Character.getType(cp) != Character.UNASSIGNED) {
                            isNotUnassigned = true;
                            debug("Warning: [%x] is ASSIGNED but in NON script%n", cp);
                            break;
                        }
                    }
                    if (isNotUnassigned) {
                        int[] a = new int[3];
                        a[0] = last[1] + 1;
                        a[1] = scripts[i][0] - 1;
                        a[2] = -1;  
                        list.add(a);
                    } else {
                        if (last[2] == scripts[i][2]) {
                            last[1] = scripts[i][1];
                            continue;
                        } else {
                            last[1] = scripts[i][0] - 1;
                        }
                    }
                }
                list.add(scripts[i]);
                last = scripts[i];
            }
            for (i = 0; i < list.size(); i++) {
                int[] a = (int[])list.get(i);
                String name = "UNKNOWN";
                if (a[2] != -1)
                    name = names[a[2]].toUpperCase(Locale.US);
                debug("0x%05x, 0x%05x  %s%n", a[0], a[1], name);
            }
            debug("--->total=%d%n", list.size());
            print("public class Scripts {%n%n");
            print("    public static enum UnicodeScript {%n");
            for (i = 0; i < names.length; i++) {
                print("        %n", names[i]);
                print("        %s,%n%n",  names[i].toUpperCase(Locale.US));
            }
            print("        %n        UNKNOWN;%n%n");
            print("        private static final int[] scriptStarts = {%n");
            for (int[] a : list) {
                String name = "UNKNOWN";
                if (a[2] != -1)
                    name = names[a[2]].toUpperCase(Locale.US);
                if (a[0] < 0x10000)
                    print("            0x%04X,   
                          a[0], a[0], a[1], name);
                else
                    print("            0x%05X,  
                          a[0], a[0], a[1], name);
            }
            last = list.get(list.size() -1);
            if (last[1] != Character.MAX_CODE_POINT)
                print("            0x%05X   
                      last[1] + 1, last[1] + 1, Character.MAX_CODE_POINT,
                      "UNKNOWN");
            print("%n        };%n%n");
            print("        private static final UnicodeScript[] scripts = {%n");
            for (int[] a : list) {
                String name = "UNKNOWN";
                if (a[2] != -1)
                    name = names[a[2]].toUpperCase(Locale.US);
                print("            %s,%n", name);
            }
            if (last[1] != Character.MAX_CODE_POINT)
                print("            UNKNOWN%n");
            print("        };%n");
            print("    }%n");
            print("}%n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

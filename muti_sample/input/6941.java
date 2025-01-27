public class FindServiceTags {
    private static String registryDir = System.getProperty("test.classes");
    private static String servicetagDir = System.getProperty("test.src");
    private static String[] files = new String[] {
                                        "servicetag1.properties",
                                        "servicetag2.properties",
                                        "servicetag3.properties",
                                        "servicetag4.properties",
                                        "servicetag5.properties"
                                    };
    private static Registry registry;
    private static Set<ServiceTag> set = new HashSet<ServiceTag>();
    private static Set<String> productUrns = new HashSet<String>();
    private static int expectedUrnCount = 3;
    public static void main(String[] argv) throws Exception {
        try {
            registry = Util.getSvcTagClientRegistry();
            runTest();
        } finally {
            Util.emptyRegistryFile();
        }
        System.out.println("Test passed.");
    }
    public static void runTest() throws Exception {
        for (String filename : files) {
            File f = new File(servicetagDir, filename);
            ServiceTag svcTag = Util.newServiceTag(f);
            ServiceTag st = registry.addServiceTag(svcTag);
            set.add(st);
            productUrns.add(st.getProductURN());
        }
        if (productUrns.size() != expectedUrnCount) {
            throw new RuntimeException("Unexpected number of product URNs = " +
                productUrns.size() + " expected " + expectedUrnCount);
        }
        if (set.size() != files.length) {
            throw new RuntimeException("Unexpected number of service tags = " +
                set.size() + " expected " + files.length);
        }
        String purn = null;
        for (String urn : productUrns) {
            if (purn == null) {
                purn = urn;
            }
            findServiceTags(urn);
        }
        Set<ServiceTag> tags = registry.findServiceTags(purn);
        for (ServiceTag st : tags) {
            System.out.println("Removing service tag " + st.getInstanceURN());
            registry.removeServiceTag(st.getInstanceURN());
        }
        tags = registry.findServiceTags(purn);
        if (tags.size() != 0) {
            throw new RuntimeException("Unexpected service tag count = " +
                tags.size());
        }
    }
    private static void findServiceTags(String productUrn) throws Exception {
        Set<ServiceTag> found = registry.findServiceTags(productUrn);
        Set<ServiceTag> matched = new HashSet<ServiceTag>();
        System.out.println("Finding service tags of product_urn=" +
            productUrn);
        for (ServiceTag st : set) {
            if (st.getProductURN().equals(productUrn)) {
                System.out.println(st.getInstanceURN());
                matched.add(st);
            }
        }
        if (found.size() != matched.size()) {
            throw new RuntimeException("Unmatched service tag count = " +
                found.size() + " expected " + matched.size());
        }
        for (ServiceTag st0 : found) {
            ServiceTag st = null;
            for (ServiceTag st1 : matched) {
                if (Util.matches(st0, st1)) {
                    st = st1;
                    break;
                }
            }
            if (st == null) {
                System.out.println("product_urn=" + st0.getProductURN());
                System.out.println("instance_urn=" + st0.getInstanceURN() );
                throw new RuntimeException(st0.getInstanceURN() +
                    " not expected in the returned list");
            }
        }
    }
}

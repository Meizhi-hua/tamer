public final class GraphicsPrimitiveMgr {
    private static final boolean debugTrace = false;
    private static GraphicsPrimitive primitives[];
    private static GraphicsPrimitive generalPrimitives[];
    private static boolean needssort = true;
    private static native void initIDs(Class GP, Class ST, Class CT,
                                       Class SG2D, Class Color, Class AT,
                                       Class XORComp, Class AlphaComp,
                                       Class Path2D, Class Path2DFloat,
                                       Class SHints);
    private static native void registerNativeLoops();
    static {
        initIDs(GraphicsPrimitive.class,
                SurfaceType.class,
                CompositeType.class,
                SunGraphics2D.class,
                java.awt.Color.class,
                java.awt.geom.AffineTransform.class,
                XORComposite.class,
                java.awt.AlphaComposite.class,
                java.awt.geom.Path2D.class,
                java.awt.geom.Path2D.Float.class,
                sun.awt.SunHints.class);
        CustomComponent.register();
        GeneralRenderer.register();
        registerNativeLoops();
    }
    private static class PrimitiveSpec {
        public int uniqueID;
    }
    private static Comparator primSorter = new Comparator() {
        public int compare(Object o1, Object o2) {
            int id1 = ((GraphicsPrimitive) o1).getUniqueID();
            int id2 = ((GraphicsPrimitive) o2).getUniqueID();
            return (id1 == id2 ? 0 : (id1 < id2 ? -1 : 1));
        }
    };
    private static Comparator primFinder = new Comparator() {
        public int compare(Object o1, Object o2) {
            int id1 = ((GraphicsPrimitive) o1).getUniqueID();
            int id2 = ((PrimitiveSpec) o2).uniqueID;
            return (id1 == id2 ? 0 : (id1 < id2 ? -1 : 1));
        }
    };
    private GraphicsPrimitiveMgr() {
    }
    public synchronized static void register(GraphicsPrimitive[] newPrimitives)
    {
        GraphicsPrimitive[] devCollection = primitives;
        int oldSize = 0;
        int newSize = newPrimitives.length;
        if (debugTrace) {
            writeLog("Registering " + newSize + " primitives");
            for (int i = 0; i < newSize; i++) {
                writeLog(newPrimitives[i].toString());
            }
        }
        if (devCollection != null) {
            oldSize = devCollection.length;
        }
        GraphicsPrimitive[] temp = new GraphicsPrimitive[oldSize + newSize];
        if (devCollection != null) {
            System.arraycopy(devCollection, 0, temp, 0, oldSize);
        }
        System.arraycopy(newPrimitives, 0, temp, oldSize, newSize);
        needssort = true;
        primitives = temp;
    }
    public synchronized static void registerGeneral(GraphicsPrimitive gen) {
        if (generalPrimitives == null) {
            generalPrimitives = new GraphicsPrimitive[] {gen};
            return;
        }
        int len = generalPrimitives.length;
        GraphicsPrimitive[] newGen = new GraphicsPrimitive[len + 1];
        System.arraycopy(generalPrimitives, 0, newGen, 0, len);
        newGen[len] = gen;
        generalPrimitives = newGen;
    }
    public synchronized static GraphicsPrimitive locate(int primTypeID,
                                                        SurfaceType dsttype)
    {
        return locate(primTypeID,
                      SurfaceType.OpaqueColor,
                      CompositeType.Src,
                      dsttype);
    }
    public synchronized static GraphicsPrimitive locate(int primTypeID,
                                                        SurfaceType srctype,
                                                        CompositeType comptype,
                                                        SurfaceType dsttype)
    {
        GraphicsPrimitive prim = locatePrim(primTypeID,
                                            srctype, comptype, dsttype);
        if (prim == null) {
            prim = locateGeneral(primTypeID);
            if (prim != null) {
                prim = prim.makePrimitive(srctype, comptype, dsttype);
                if (prim != null && GraphicsPrimitive.traceflags != 0) {
                    prim = prim.traceWrap();
                }
            }
        }
        return prim;
    }
    public synchronized static GraphicsPrimitive
        locatePrim(int primTypeID,
                   SurfaceType srctype,
                   CompositeType comptype,
                   SurfaceType dsttype)
    {
        SurfaceType src, dst;
        CompositeType cmp;
        GraphicsPrimitive prim;
        PrimitiveSpec spec = new PrimitiveSpec();
        for (dst = dsttype; dst != null; dst = dst.getSuperType()) {
            for (src = srctype; src != null; src = src.getSuperType()) {
                for (cmp = comptype; cmp != null; cmp = cmp.getSuperType()) {
                    spec.uniqueID =
                        GraphicsPrimitive.makeUniqueID(primTypeID, src, cmp, dst);
                    prim = locate(spec);
                    if (prim != null) {
                        return prim;
                    }
                }
            }
        }
        return null;
    }
    private static GraphicsPrimitive locateGeneral(int primTypeID) {
        if (generalPrimitives == null) {
            return null;
        }
        for (int i = 0; i < generalPrimitives.length; i++) {
            GraphicsPrimitive prim = generalPrimitives[i];
            if (prim.getPrimTypeID() == primTypeID) {
                return prim;
            }
        }
        return null;
    }
    private static GraphicsPrimitive locate(PrimitiveSpec spec) {
        if (needssort) {
            if (GraphicsPrimitive.traceflags != 0) {
                for (int i = 0; i < primitives.length; i++) {
                    primitives[i] = primitives[i].traceWrap();
                }
            }
            Arrays.sort(primitives, primSorter);
            needssort = false;
        }
        GraphicsPrimitive[] devCollection = primitives;
        if (devCollection == null) {
            return null;
        }
        int index = Arrays.binarySearch(devCollection, spec, primFinder);
        if (index >= 0) {
            GraphicsPrimitive prim = devCollection[index];
            if (prim instanceof GraphicsPrimitiveProxy) {
                prim = ((GraphicsPrimitiveProxy) prim).instantiate();
                devCollection[index] = prim;
                if (debugTrace) {
                    writeLog("Instantiated graphics primitive " + prim);
                }
            }
            if (debugTrace) {
                writeLog("Lookup found[" + index + "]["+ prim + "]");
            }
            return prim;
        }
        if (debugTrace) {
            writeLog("Lookup found nothing for:");
            writeLog(" " + spec.uniqueID);
        }
        return null;
    }
    private static void writeLog(String str) {
        if (debugTrace) {
            System.err.println(str);
        }
    }
    public static void testPrimitiveInstantiation() {
        testPrimitiveInstantiation(false);
    }
    public static void testPrimitiveInstantiation(boolean verbose) {
        int resolved = 0;
        int unresolved = 0;
        GraphicsPrimitive[] prims = primitives;
        for (int j = 0; j < prims.length; j++) {
            GraphicsPrimitive p = prims[j];
            if (p instanceof GraphicsPrimitiveProxy) {
                GraphicsPrimitive r = ((GraphicsPrimitiveProxy) p).instantiate();
                if (!r.getSignature().equals(p.getSignature()) ||
                    r.getUniqueID() != p.getUniqueID()) {
                    System.out.println("r.getSignature == "+r.getSignature());
                    System.out.println("r.getUniqueID == " + r.getUniqueID());
                    System.out.println("p.getSignature == "+p.getSignature());
                    System.out.println("p.getUniqueID == " + p.getUniqueID());
                    throw new RuntimeException("Primitive " + p
                                               + " returns wrong signature for "
                                               + r.getClass());
                }
                unresolved++;
                p = r;
                if (verbose) {
                    System.out.println(p);
                }
            } else {
                if (verbose) {
                    System.out.println(p + " (not proxied).");
                }
                resolved++;
            }
        }
        System.out.println(resolved+
                           " graphics primitives were not proxied.");
        System.out.println(unresolved+
                           " proxied graphics primitives resolved correctly.");
        System.out.println(resolved+unresolved+
                           " total graphics primitives");
    }
    public static void main(String argv[]) {
        if (needssort) {
            Arrays.sort(primitives, primSorter);
            needssort = false;
        }
        testPrimitiveInstantiation(argv.length > 0);
    }
}

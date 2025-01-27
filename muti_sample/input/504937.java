class LoadClass {
    public static void main(String[] args) {
        System.loadLibrary("android_runtime");
        if (registerNatives() < 0) {
            throw new RuntimeException("Error registering natives.");    
        }
        Debug.startAllocCounting();
        if (args.length > 0) {
            try {
                long start = System.currentTimeMillis();
                Class.forName(args[0]);
                long elapsed = System.currentTimeMillis() - start;
                Log.i("LoadClass", "Loaded " + args[0] + " in " + elapsed
                        + "ms.");
            } catch (ClassNotFoundException e) {
                Log.w("LoadClass", e);
                return;
            }
        }
        System.gc();
        int allocCount = Debug.getGlobalAllocCount();
        int allocSize = Debug.getGlobalAllocSize();
        int freedCount = Debug.getGlobalFreedCount();
        int freedSize = Debug.getGlobalFreedSize();
        long nativeHeapSize = Debug.getNativeHeapSize();
        Debug.stopAllocCounting();
        StringBuilder response = new StringBuilder("DECAFBAD");
        int[] pages = new int[6];
        Debug.MemoryInfo memoryInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(memoryInfo);
        response.append(',').append(memoryInfo.nativeSharedDirty);
        response.append(',').append(memoryInfo.dalvikSharedDirty);
        response.append(',').append(memoryInfo.otherSharedDirty);
        response.append(',').append(memoryInfo.nativePrivateDirty);
        response.append(',').append(memoryInfo.dalvikPrivateDirty);
        response.append(',').append(memoryInfo.otherPrivateDirty);
        response.append(',').append(allocCount);
        response.append(',').append(allocSize);
        response.append(',').append(freedCount);
        response.append(',').append(freedSize);
        response.append(',').append(nativeHeapSize);
        System.out.println(response.toString());
    }
    static native int registerNatives();
}

final class DualPivotQuicksort {
    private DualPivotQuicksort() {}
    private static final int INSERTION_SORT_THRESHOLD = 32;
    private static final int COUNTING_SORT_THRESHOLD_FOR_BYTE = 128;
    private static final int COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR = 32768;
    public static void sort(int[] a) {
        doSort(a, 0, a.length - 1);
    }
    public static void sort(int[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        doSort(a, fromIndex, toIndex - 1);
    }
    private static void doSort(int[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                int ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(int[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        int ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { int t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { int t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { int t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { int t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { int t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { int t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { int t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { int t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { int t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        int pivot1 = ae2; a[e2] = a[left];
        int pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                int ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                int ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                int ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(long[] a) {
        doSort(a, 0, a.length - 1);
    }
    public static void sort(long[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        doSort(a, fromIndex, toIndex - 1);
    }
    private static void doSort(long[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                long ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(long[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        long ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { long t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { long t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { long t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { long t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { long t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { long t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { long t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { long t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { long t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        long pivot1 = ae2; a[e2] = a[left];
        long pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                long ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                long ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                long ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(short[] a) {
        doSort(a, 0, a.length - 1);
    }
    public static void sort(short[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        doSort(a, fromIndex, toIndex - 1);
    }
    private static final int NUM_SHORT_VALUES = 1 << 16;
    private static void doSort(short[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                short ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else if (right-left+1 > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_SHORT_VALUES];
            for (int i = left; i <= right; i++) {
                count[a[i] - Short.MIN_VALUE]++;
            }
            for (int i = 0, k = left; i < count.length && k <= right; i++) {
                short value = (short) (i + Short.MIN_VALUE);
                for (int s = count[i]; s > 0; s--) {
                    a[k++] = value;
               }
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(short[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        short ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { short t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { short t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { short t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { short t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { short t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { short t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { short t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { short t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { short t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        short pivot1 = ae2; a[e2] = a[left];
        short pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                short ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                short ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                short ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(char[] a) {
        doSort(a, 0, a.length - 1);
    }
    public static void sort(char[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        doSort(a, fromIndex, toIndex - 1);
    }
    private static final int NUM_CHAR_VALUES = 1 << 16;
    private static void doSort(char[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                char ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else if (right-left+1 > COUNTING_SORT_THRESHOLD_FOR_SHORT_OR_CHAR) {
            int[] count = new int[NUM_CHAR_VALUES];
            for (int i = left; i <= right; i++) {
                count[a[i]]++;
            }
            for (int i = 0, k = left; i < count.length && k <= right; i++) {
                for (int s = count[i]; s > 0; s--) {
                    a[k++] = (char) i;
               }
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(char[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        char ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { char t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { char t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { char t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { char t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { char t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { char t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { char t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { char t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { char t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        char pivot1 = ae2; a[e2] = a[left];
        char pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                char ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                char ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                char ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(byte[] a) {
        doSort(a, 0, a.length - 1);
    }
    public static void sort(byte[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        doSort(a, fromIndex, toIndex - 1);
    }
    private static final int NUM_BYTE_VALUES = 1 << 8;
    private static void doSort(byte[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                byte ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else if (right - left + 1 > COUNTING_SORT_THRESHOLD_FOR_BYTE) {
            int[] count = new int[NUM_BYTE_VALUES];
            for (int i = left; i <= right; i++) {
                count[a[i] - Byte.MIN_VALUE]++;
            }
            for (int i = 0, k = left; i < count.length && k <= right; i++) {
                byte value = (byte) (i + Byte.MIN_VALUE);
                for (int s = count[i]; s > 0; s--) {
                    a[k++] = value;
               }
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(byte[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        byte ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { byte t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { byte t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { byte t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { byte t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { byte t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { byte t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { byte t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { byte t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { byte t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        byte pivot1 = ae2; a[e2] = a[left];
        byte pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                byte ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                byte ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                byte ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(float[] a) {
        sortNegZeroAndNaN(a, 0, a.length - 1);
    }
    public static void sort(float[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        sortNegZeroAndNaN(a, fromIndex, toIndex - 1);
    }
    private static void sortNegZeroAndNaN(float[] a, int left, int right) {
        final int NEGATIVE_ZERO = Float.floatToIntBits(-0.0f);
        int numNegativeZeros = 0;
        int n = right;
        for (int k = left; k <= n; k++) {
            float ak = a[k];
            if (ak == 0.0f && NEGATIVE_ZERO == Float.floatToIntBits(ak)) {
                a[k] = 0.0f;
                numNegativeZeros++;
            } else if (ak != ak) { 
                a[k--] = a[n];
                a[n--] = Float.NaN;
            }
        }
        doSort(a, left, n);
        if (numNegativeZeros == 0) {
            return;
        }
        int zeroIndex = findAnyZero(a, left, n);
        for (int i = zeroIndex - 1; i >= left && a[i] == 0.0f; i--) {
            zeroIndex = i;
        }
        for (int i = zeroIndex, m = zeroIndex + numNegativeZeros; i < m; i++) {
            a[i] = -0.0f;
        }
    }
    private static int findAnyZero(float[] a, int low, int high) {
        while (true) {
            int middle = (low + high) >>> 1;
            float middleValue = a[middle];
            if (middleValue < 0.0f) {
                low = middle + 1;
            } else if (middleValue > 0.0f) {
                high = middle - 1;
            } else { 
                return middle;
            }
        }
    }
    private static void doSort(float[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                float ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(float[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        float ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { float t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { float t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { float t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { float t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { float t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { float t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { float t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { float t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { float t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        float pivot1 = ae2; a[e2] = a[left];
        float pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                float ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                float ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                float ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    public static void sort(double[] a) {
        sortNegZeroAndNaN(a, 0, a.length - 1);
    }
    public static void sort(double[] a, int fromIndex, int toIndex) {
        rangeCheck(a.length, fromIndex, toIndex);
        sortNegZeroAndNaN(a, fromIndex, toIndex - 1);
    }
    private static void sortNegZeroAndNaN(double[] a, int left, int right) {
        final long NEGATIVE_ZERO = Double.doubleToLongBits(-0.0d);
        int numNegativeZeros = 0;
        int n = right;
        for (int k = left; k <= n; k++) {
            double ak = a[k];
            if (ak == 0.0d && NEGATIVE_ZERO == Double.doubleToLongBits(ak)) {
                a[k] = 0.0d;
                numNegativeZeros++;
            } else if (ak != ak) { 
                a[k--] = a[n];
                a[n--] = Double.NaN;
            }
        }
        doSort(a, left, n);
        if (numNegativeZeros == 0) {
            return;
        }
        int zeroIndex = findAnyZero(a, left, n);
        for (int i = zeroIndex - 1; i >= left && a[i] == 0.0d; i--) {
            zeroIndex = i;
        }
        for (int i = zeroIndex, m = zeroIndex + numNegativeZeros; i < m; i++) {
            a[i] = -0.0d;
        }
    }
    private static int findAnyZero(double[] a, int low, int high) {
        while (true) {
            int middle = (low + high) >>> 1;
            double middleValue = a[middle];
            if (middleValue < 0.0d) {
                low = middle + 1;
            } else if (middleValue > 0.0d) {
                high = middle - 1;
            } else { 
                return middle;
            }
        }
    }
    private static void doSort(double[] a, int left, int right) {
        if (right - left + 1 < INSERTION_SORT_THRESHOLD) {
            for (int i = left + 1; i <= right; i++) {
                double ai = a[i];
                int j;
                for (j = i - 1; j >= left && ai < a[j]; j--) {
                    a[j + 1] = a[j];
                }
                a[j + 1] = ai;
            }
        } else { 
            dualPivotQuicksort(a, left, right);
        }
    }
    private static void dualPivotQuicksort(double[] a, int left, int right) {
        int sixth = (right - left + 1) / 6;
        int e1 = left  + sixth;
        int e5 = right - sixth;
        int e3 = (left + right) >>> 1; 
        int e4 = e3 + sixth;
        int e2 = e3 - sixth;
        double ae1 = a[e1], ae2 = a[e2], ae3 = a[e3], ae4 = a[e4], ae5 = a[e5];
        if (ae1 > ae2) { double t = ae1; ae1 = ae2; ae2 = t; }
        if (ae4 > ae5) { double t = ae4; ae4 = ae5; ae5 = t; }
        if (ae1 > ae3) { double t = ae1; ae1 = ae3; ae3 = t; }
        if (ae2 > ae3) { double t = ae2; ae2 = ae3; ae3 = t; }
        if (ae1 > ae4) { double t = ae1; ae1 = ae4; ae4 = t; }
        if (ae3 > ae4) { double t = ae3; ae3 = ae4; ae4 = t; }
        if (ae2 > ae5) { double t = ae2; ae2 = ae5; ae5 = t; }
        if (ae2 > ae3) { double t = ae2; ae2 = ae3; ae3 = t; }
        if (ae4 > ae5) { double t = ae4; ae4 = ae5; ae5 = t; }
        a[e1] = ae1; a[e3] = ae3; a[e5] = ae5;
        double pivot1 = ae2; a[e2] = a[left];
        double pivot2 = ae4; a[e4] = a[right];
        int less  = left  + 1; 
        int great = right - 1; 
        boolean pivotsDiffer = (pivot1 != pivot2);
        if (pivotsDiffer) {
            outer:
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else if (ak > pivot2) { 
                    while (a[great] > pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = a[great];
                        a[great--] = ak;
                    }
                }
            }
        } else { 
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                if (ak == pivot1) {
                    continue;
                }
                if (ak < pivot1) { 
                    if (k != less) {
                        a[k] = a[less];
                        a[less] = ak;
                    }
                    less++;
                } else { 
                    while (a[great] > pivot1) {
                        great--;
                    }
                    if (a[great] < pivot1) {
                        a[k] = a[less];
                        a[less++] = a[great];
                        a[great--] = ak;
                    } else { 
                        a[k] = pivot1;
                        a[great--] = ak;
                    }
                }
            }
        }
        a[left]  = a[less  - 1]; a[less  - 1] = pivot1;
        a[right] = a[great + 1]; a[great + 1] = pivot2;
        doSort(a, left,   less - 2);
        doSort(a, great + 2, right);
        if (!pivotsDiffer) {
            return;
        }
        if (less < e1 && great > e5) {
            while (a[less] == pivot1) {
                less++;
            }
            while (a[great] == pivot2) {
                great--;
            }
            outer:
            for (int k = less; k <= great; k++) {
                double ak = a[k];
                if (ak == pivot2) { 
                    while (a[great] == pivot2) {
                        if (great-- == k) {
                            break outer;
                        }
                    }
                    if (a[great] == pivot1) {
                        a[k] = a[less];
                        a[less++] = pivot1;
                    } else { 
                        a[k] = a[great];
                    }
                    a[great--] = pivot2;
                } else if (ak == pivot1) { 
                    a[k] = a[less];
                    a[less++] = pivot1;
                }
            }
        }
        doSort(a, less, great);
    }
    private static void rangeCheck(int length, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException(
                "fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > length) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }
}

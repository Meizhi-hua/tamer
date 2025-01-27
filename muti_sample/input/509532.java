public class PackedIntVectorTest extends TestCase {
    public void testBasic() throws Exception {
        for (int width = 0; width < 10; width++) {
            PackedIntVector p = new PackedIntVector(width);
            int[] ins = new int[width];
            for (int height = width * 2; height < width * 4; height++) {
                assertEquals(p.width(), width);
                for (int i = 0; i < height; i++) {
                    int at;
                    if (i % 2 == 0) {
                        at = i;
                    } else {
                        at = p.size() - i;
                    }
                    for (int j = 0; j < width; j++) {
                        ins[j] = i + j;
                    }
                    if (i == height / 2) {
                        p.insertAt(at, null);
                    } else {
                        p.insertAt(at, ins);
                    }
                    assertEquals(p.size(), i + 1);
                    for (int j = 0; j < width; j++) {
                        if (i == height / 2) {
                            assertEquals(0, p.getValue(at, j));
                        } else {
                            assertEquals(p.getValue(at, j), i + j);
                        }
                    }
                }
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        p.setValue(i, j, i * j);
                        assertEquals(p.getValue(i, j), i * j);
                    }
                }
                for (int j = 0; j < width; j++) {
                    p.adjustValuesBelow(j * 2, j, j + 27);
                }
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int expect = i * j;
                        if (i >= j * 2) {
                            expect += j + 27;
                        }
                        assertEquals(p.getValue(i, j), expect);
                    }
                }
                for (int j = 0; j < width; j++) {
                    p.adjustValuesBelow(j, j, j * j + 14);
                }
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        int expect = i * j;
                        if (i >= j * 2) {
                            expect += j + 27;
                        }
                        if (i >= j) {
                            expect += j * j + 14;
                        }
                        assertEquals(p.getValue(i, j), expect);
                    }
                }
                for (int j = 0; j < width; j++) {
                    p.adjustValuesBelow(j * 2, j, -(j + 27));
                    p.adjustValuesBelow(j, j, -(j * j + 14));
                }
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        assertEquals(p.getValue(i, j), i * j);
                    }
                }
                while (p.size() > 0) {
                    int osize = p.size();
                    int del = osize / 3;
                    if (del == 0) {
                        del = 1;
                    }
                    int at = (osize - del) / 2;
                    p.deleteAt(at, del);
                    assertEquals(p.size(), osize - del);
                    for (int i = 0; i < at; i++) {
                        for (int j = 0; j < width; j++) {
                            assertEquals(p.getValue(i, j), i * j);
                        }
                    }
                    for (int i = at; i < p.size(); i++) {
                        for (int j = 0; j < width; j++) {
                            assertEquals(p.getValue(i, j), (i + height - p.size()) * j);
                        }
                    }
                }
                assertEquals(0, p.size());
            }
        }
    }
}

public class SimpleArrays {
    public static void main (String argv[]) throws IOException {
       System.err.println("\nRegression test for testing of " +
           "serialization/deserialization of objects with Arrays types\n");
       FileInputStream istream = null;
       FileOutputStream ostream = null;
       try {
           ostream = new FileOutputStream("piotest2.tmp");
           ObjectOutputStream p = new ObjectOutputStream(ostream);
            byte b[] = { 0, 1};
            p.writeObject((Object)b);
            short s[] = { 0, 1, 2};
            p.writeObject((Object)s);
            char c[] = { 'A', 'B', 'C', 'D'};
            p.writeObject((Object)c);
            int i[] = { 0, 1, 2, 3, 4};
            p.writeObject((Object)i);
            long l[] = { 0, 1, 2, 3, 4, 5};
            p.writeObject((Object)l);
            boolean z[] = new boolean[4];
            z[0] = true;
            z[1] = false;
            z[2] = true;
            z[3] = false;
            p.writeObject(z);
            float f[] = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f};
            p.writeObject((Object)f);
            double d[] = { 1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0d};
            p.writeObject((Object)d);
            String string[] = { "A", "B", "C", "D"};
            p.writeObject((Object) string);
            PrimitivesTest prim[] = new PrimitivesTest[5];
            prim[0] = new PrimitivesTest();
            prim[1] = prim[0];
            prim[2] = new PrimitivesTest();
            prim[3] = prim[2];
            prim[4] = null;
            p.writeObject((Object)prim);
            p.flush();
            istream = new FileInputStream("piotest2.tmp");
            ObjectInputStream q = new ObjectInputStream(istream);
            Object obj;
            byte b_u[] = (byte[])q.readObject();
            short s_u[] = (short[])q.readObject();
            char c_u[] = (char[])q.readObject();
            int i_u[] = (int[])q.readObject();
            long l_u[] = (long[])q.readObject();
            boolean z_u[] = null;
            Object z_obj = null;
            try {
                z_obj = q.readObject();
                z_u = (boolean[])z_obj;
            } catch (ClassCastException e) {
                System.err.println("\nClassCastException " + e.getMessage());
                System.err.println("\nBoolean array read as " +
                    z_obj.getClass().getName());
                System.err.println("\nAn Exception occurred. " +
                    e.getMessage());
                z_u = z;
                throw new Error();
            }
            float f_u[] = (float[])q.readObject();
            double d_u[] = (double[])q.readObject();
            String string_u[] = (String[])q.readObject();
            PrimitivesTest prim_u[] = (PrimitivesTest[])q.readObject();
            if (!ArrayOpsTest.verify(i, i_u)) {
                System.err.println("\nUnpickling of int array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(b, b_u)) {
                System.err.println("\nUnpickling of byte array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(s, s_u)) {
                System.err.println("\nUnpickling of short array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(c, c_u)) {
                System.err.println("\nUnpickling of char array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(l, l_u)) {
                System.err.println("\nUnpickling of long array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(f, f_u)) {
                System.err.println("\nUnpickling of float array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(d, d_u)) {
                System.err.println("\nUnpickling of double array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(z, z_u)) {
                System.err.println("\nUnpickling of boolean array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(string, string_u)) {
                System.err.println("\nUnpickling of String array failed");
                throw new Error();
            }
            if (!ArrayOpsTest.verify(prim, prim_u)) {
                System.err.println("\nUnpickling of PrimitivesTest array " +
                    "failed");
                throw new Error();
            }
            System.err.println("\nTEST PASSED");
       } catch (Exception e) {
           System.err.print("TEST FAILED: ");
           e.printStackTrace();
           System.err.println("\nInput remaining");
           int ch;
           try {
               while ((ch = istream.read()) != -1) {
                   System.err.print("\n " + Integer.toString(ch, 16) + " ");
               }
               System.err.println("\n ");
           } catch (Exception f) {
               throw new Error();
           }
           throw new Error();
       } finally {
           if (istream != null) istream.close();
           if (ostream != null) ostream.close();
       }
    }
}

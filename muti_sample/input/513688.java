    class PublicClassLoader extends ClassLoader {
        public PublicClassLoader() {
            super();
        }
        public PublicClassLoader(ClassLoader cl) {
            super(cl);
        }        
        private byte[] getBytes( String filename ) throws IOException {
            File file = new File( filename );
            long len = file.length();
            byte raw[] = new byte[(int)len];
            FileInputStream fin = new FileInputStream( file );
            int r = fin.read( raw );
            if (r != len)
              throw new IOException( "Can't read all, "+r+" != "+len );
            fin.close();
            return raw;
          }
        public Class<?> loadClass(String name, boolean resolve)
                                            throws ClassNotFoundException {
            Class clazz = findLoadedClass(name);
            String classFileName = name.replace( '.', '/' ) + ".class";
            File classFile = new File(classFileName);
            if (classFile.exists()) {
                try {
                    byte raw[] = getBytes(classFileName);
                    clazz = defineClass( name, raw, 0, raw.length );
                } catch(Exception ioe) {}
            }
            if (clazz == null) {
                Package p = getClass().getPackage();
                InputStream is = getResourceAsStream("/" + classFileName);
                byte[] buf = new byte[512];
                int len;
                try {
                    len = is.read(buf);
                    clazz = defineClass(name, buf, 0, len);                    
                } catch (IOException e) {
                }
            }            
            if (clazz == null) {
                clazz = findSystemClass(name);
            }
            if(clazz == null)
                throw new ClassNotFoundException(name);
            return clazz;
        }
        public Class<?> defineClassTest(byte[] b, int off, int len) {
            return defineClass(b, off, len);
        }
        public Package getPackage(String name) {
            return super.getPackage(name);
        }
        public Package [] getPackages() {
            return super.getPackages();
        }
        public InputStream getResourceAsStream(String name) {
            return getClass().getResourceAsStream("/" + getClass().getPackage().
                    getName().replace(".", "/") + "/" + name);
        }    
    }

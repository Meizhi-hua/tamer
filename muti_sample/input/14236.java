public class WriteOutputStream {
    private static void assertEquals(Object a, Object b) throws Exception
    {
        if(!a.equals(b))
            throw new RuntimeException("assertEquals fails!");
    }
    public static void main(String[] args) throws Exception {
        RIFFWriter writer = null;
        RIFFReader reader = null;
        File tempfile = File.createTempFile("test",".riff");
        try
        {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            writer = new RIFFWriter(baos, "TEST");
            RIFFWriter chunk = writer.writeChunk("TSCH");
            chunk.write((byte)33);
            writer.close();
            writer = null;
            ByteArrayInputStream fis = new ByteArrayInputStream(baos.toByteArray());
            reader = new RIFFReader(fis);
            assertEquals(reader.getFormat(), "RIFF");
            assertEquals(reader.getType(), "TEST");
            RIFFReader readchunk = reader.nextChunk();
            assertEquals(readchunk.getFormat(), "TSCH");
            assertEquals(readchunk.read(), 33);
            fis.close();
            reader = null;
        }
        finally
        {
            if(writer != null)
                writer.close();
            if(reader != null)
                reader.close();
            if(tempfile.exists())
                if(!tempfile.delete())
                    tempfile.deleteOnExit();
        }
    }
}
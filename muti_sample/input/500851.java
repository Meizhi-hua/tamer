public class SerializerSwitcher
{
  public static void switchSerializerIfHTML(
          TransformerImpl transformer, String ns, String localName)
            throws TransformerException
  {
    if (null == transformer)
      return;
    if (((null == ns) || (ns.length() == 0))
            && localName.equalsIgnoreCase("html"))
    {
      if (null != transformer.getOutputPropertyNoDefault(OutputKeys.METHOD))
        return;
      Properties prevProperties = transformer.getOutputFormat().getProperties();
      OutputProperties htmlOutputProperties = new OutputProperties(Method.HTML);
      htmlOutputProperties.copyFrom(prevProperties, true);
      Properties htmlProperties = htmlOutputProperties.getProperties();
      try
      {
        Serializer oldSerializer = null;
        if (null != oldSerializer)
        {
          Serializer serializer =
            SerializerFactory.getSerializer(htmlProperties);
          Writer writer = oldSerializer.getWriter();
          if (null != writer)
            serializer.setWriter(writer);
          else
          {
            OutputStream os = oldSerializer.getOutputStream();
            if (null != os)
              serializer.setOutputStream(os);
          }
          ContentHandler ch = serializer.asContentHandler();
          transformer.setContentHandler(ch);
        }
      }
      catch (java.io.IOException e)
      {
        throw new TransformerException(e);
      }
    }
  }
  private static String getOutputPropertyNoDefault(String qnameString, Properties props)
    throws IllegalArgumentException
  {    
    String value = (String)props.get(qnameString);
    return value;
  }
  public static Serializer switchSerializerIfHTML(
          String ns, String localName, Properties props, Serializer oldSerializer)
            throws TransformerException
  {
    Serializer newSerializer = oldSerializer;
    if (((null == ns) || (ns.length() == 0))
            && localName.equalsIgnoreCase("html"))
    {
      if (null != getOutputPropertyNoDefault(OutputKeys.METHOD, props))
        return newSerializer;
      Properties prevProperties = props;
      OutputProperties htmlOutputProperties = new OutputProperties(Method.HTML);
      htmlOutputProperties.copyFrom(prevProperties, true);
      Properties htmlProperties = htmlOutputProperties.getProperties();
      {
        if (null != oldSerializer)
        {
          Serializer serializer =
            SerializerFactory.getSerializer(htmlProperties);
          Writer writer = oldSerializer.getWriter();
          if (null != writer)
            serializer.setWriter(writer);
          else
          {
            OutputStream os = serializer.getOutputStream();
            if (null != os)
              serializer.setOutputStream(os);
          }
          newSerializer = serializer;
        }
      }
    }
    return newSerializer;
  }
}

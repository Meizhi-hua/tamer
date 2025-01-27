public class AttributeListImpl implements AttributeList
{
    public AttributeListImpl ()
    {
    }
    public AttributeListImpl (AttributeList atts)
    {
    setAttributeList(atts);
    }
    public void setAttributeList (AttributeList atts)
    {
    int count = atts.getLength();
    clear();
    for (int i = 0; i < count; i++) {
        addAttribute(atts.getName(i), atts.getType(i), atts.getValue(i));
    }
    }
    public void addAttribute (String name, String type, String value)
    {
    names.addElement(name);
    types.addElement(type);
    values.addElement(value);
    }
    public void removeAttribute (String name)
    {
    int i = names.indexOf(name);
    if (i >= 0) {
        names.removeElementAt(i);
        types.removeElementAt(i);
        values.removeElementAt(i);
    }
    }
    public void clear ()
    {
    names.removeAllElements();
    types.removeAllElements();
    values.removeAllElements();
    }
    public int getLength ()
    {
    return names.size();
    }
    public String getName (int i)
    {
    if (i < 0) {
        return null;
    }
    try {
        return (String)names.elementAt(i);
    } catch (ArrayIndexOutOfBoundsException e) {
        return null;
    }
    }
    public String getType (int i)
    {
    if (i < 0) {
        return null;
    }
    try {
        return (String)types.elementAt(i);
    } catch (ArrayIndexOutOfBoundsException e) {
        return null;
    }
    }
    public String getValue (int i)
    {
    if (i < 0) {
        return null;
    }
    try {
        return (String)values.elementAt(i);
    } catch (ArrayIndexOutOfBoundsException e) {
        return null;
    }
    }
    public String getType (String name)
    {
    return getType(names.indexOf(name));
    }
    public String getValue (String name)
    {
    return getValue(names.indexOf(name));
    }
    Vector names = new Vector();
    Vector types = new Vector();
    Vector values = new Vector();
}

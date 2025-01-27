class MockAttributeSet
    implements AttributeSet, MutableAttributeSet
{
    public Dictionary<Object, Object> backing;
    public boolean isEmpty()
    {
         return backing.isEmpty();
    }
    public int getAttributeCount()
    {
         return backing.size();
    }
    public boolean isDefined(Object name)
    {
         return ( backing.get(name) ) != null;
    }
    public boolean isEqual(AttributeSet attr)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public AttributeSet copyAttributes()
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public Object getAttribute(Object name)
    {
        return backing.get(name);
    }
    public void addAttribute(Object name, Object value)
    {
        backing.put(name, value);
    }
    public void addAttributes(AttributeSet attr)
    {
        Enumeration as = attr.getAttributeNames();
        while(as.hasMoreElements()) {
            Object el = as.nextElement();
            backing.put(el, attr.getAttribute(el));
        }
    }
    public void removeAttribute(Object name)
    {
        backing.remove(name);
    }
    public void removeAttributes(AttributeSet attr)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public void removeAttributes(Enumeration<?> en)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public void setResolveParent(AttributeSet pp)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public Enumeration getAttributeNames()
    {
         return backing.keys();
    }
    public boolean containsAttribute(Object name, Object value)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public boolean containsAttributes(AttributeSet attr)
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
    public AttributeSet getResolveParent()
    {
         throw new InternalError("MockAttributeSet: charade revealed!");
    }
}

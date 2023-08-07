public class XMLPropertyElement implements XMLProperty {
    protected XMLProperty parent;
    protected String name;
    protected String type;
    protected String className;
    protected List content = new ArrayList();
    public XMLPropertyElement(XMLProperty mother) {
        parent = mother;
    }
    public XMLPropertyElement(XMLProperty mother, String propertyName, String propertyType, Object value) {
        this(mother);
        name = propertyName;
        type = propertyType;
        if (type.equals("string")) {
            if (XML.requiresCDATA((String) value)) {
                content.add(XML.CDATA_PRE + value + XML.CDATA_POST);
            } else {
                content.add(value.toString());
            }
        } else if ("intdoubleboolean".indexOf(type) != -1) {
            content.add(value.toString());
        } else if (type.equals("object")) {
            className = value.getClass().getName();
            XMLControl control = new XMLControlElement(this);
            control.saveObject(value);
            content.add(control);
        } else if (type.equals("collection")) {
            className = value.getClass().getName();
            Iterator it = ((Collection) value).iterator();
            int i = 0;
            while (it.hasNext()) {
                Object next = it.next();
                String type = XML.getDataType(next);
                if (type == null) {
                    continue;
                }
                content.add(new XMLPropertyElement(this, "item", type, next));
                i++;
            }
        } else if (type.equals("array")) {
            className = value.getClass().getName();
            Class baseType = value.getClass().getComponentType();
            Object array = value;
            int count = Array.getLength(array);
            while (count > 0 && baseType.getComponentType() != null) {
                baseType = baseType.getComponentType();
                array = Array.get(array, 0);
                if (array == null) {
                    break;
                }
                count = count * Array.getLength(array);
            }
            boolean primitive = "intdoubleboolean".indexOf(baseType.getName()) != -1;
            if (primitive && count > XMLControlElement.compactArraySize) {
                String s = getArrayString(value);
                content.add(new XMLPropertyElement(this, "array", "string", s));
            } else {
                int length = Array.getLength(value);
                for (int j = 0; j < length; j++) {
                    Object next = Array.get(value, j);
                    String type = XML.getDataType(next);
                    if (type == null) {
                        continue;
                    }
                    content.add(new XMLPropertyElement(this, "[" + j + "]", type, next));
                }
            }
        }
    }
    public String getPropertyName() {
        return name;
    }
    public String getPropertyType() {
        return type;
    }
    public Class getPropertyClass() {
        if (type.equals("int")) {
            return Integer.TYPE;
        } else if (type.equals("double")) {
            return Double.TYPE;
        } else if (type.equals("boolean")) {
            return Boolean.TYPE;
        } else if (type.equals("string")) {
            return String.class;
        }
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            return null;
        }
    }
    public XMLProperty getParentProperty() {
        return parent;
    }
    public int getLevel() {
        return parent.getLevel() + 1;
    }
    public List getPropertyContent() {
        return content;
    }
    public XMLControl getChildControl(String name) {
        XMLControl[] children = getChildControls();
        for (int i = 0; i < children.length; i++) {
            if (children[i].getPropertyName().equals(name)) {
                return children[i];
            }
        }
        return null;
    }
    public XMLControl[] getChildControls() {
        if (type.equals("object")) {
            XMLControl child = (XMLControl) getPropertyContent().get(0);
            return new XMLControl[] { child };
        } else if ("arraycollection".indexOf(type) != -1) {
            ArrayList list = new ArrayList();
            Iterator it = getPropertyContent().iterator();
            while (it.hasNext()) {
                XMLProperty prop = (XMLProperty) it.next();
                if (prop.getPropertyType().equals("object")) {
                    list.add(prop.getPropertyContent().get(0));
                }
            }
            return (XMLControl[]) list.toArray(new XMLControl[0]);
        }
        return new XMLControl[0];
    }
    public void setValue(String stringValue) {
        boolean valid = true;
        try {
            if (type.equals("int")) {
                Integer.parseInt(stringValue);
            } else if (type.equals("double")) {
                Double.parseDouble(stringValue);
            } else if (type.equals("boolean")) {
                stringValue = stringValue.equals("true") ? "true" : "false";
            } else if ("objectarraycollection".indexOf(type) != -1) {
                valid = false;
            } else if (type.equals("string") && XML.requiresCDATA(stringValue)) {
                stringValue = XML.CDATA_PRE + stringValue + XML.CDATA_POST;
            }
        } catch (NumberFormatException ex) {
            valid = false;
        }
        if (valid) {
            content.clear();
            content.add(stringValue);
        }
    }
    public String toString() {
        StringBuffer xml = new StringBuffer(XML.NEW_LINE + indent(getLevel()) + "<property name=\"" + name + "\" type=\"" + type + "\"");
        if ("arraycollection".indexOf(type) != -1) {
            xml.append(" class=\"" + className + "\"");
        }
        List content = getPropertyContent();
        if (content.isEmpty()) {
            xml.append("/>");
            return xml.toString();
        }
        xml.append(">");
        boolean hasChildren = false;
        Iterator it = content.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            hasChildren = hasChildren || (next instanceof XMLProperty);
            xml.append(next);
        }
        if (hasChildren) {
            xml.append(XML.NEW_LINE + indent(getLevel()));
        }
        xml.append("</property>");
        return xml.toString();
    }
    protected String indent(int level) {
        String space = "";
        for (int i = 0; i < XML.INDENT * level; i++) {
            space += " ";
        }
        return space;
    }
    protected String getArrayString(Object array) {
        StringBuffer sb = new StringBuffer("{");
        int length = Array.getLength(array);
        for (int j = 0; j < length; j++) {
            if (j > 0) {
                sb.append(',');
            }
            Object element = Array.get(array, j);
            if (element != null && element.getClass().isArray()) {
                sb.append(getArrayString(element));
            } else {
                sb.append(element);
            }
        }
        sb.append('}');
        return sb.toString();
    }
}

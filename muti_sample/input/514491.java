public class UiViewElementNode extends UiElementNode {
    private AttributeDescriptor[] mCachedAttributeDescriptors;
    public UiViewElementNode(ViewElementDescriptor elementDescriptor) {
        super(elementDescriptor);
    }
    @Override
    public AttributeDescriptor[] getAttributeDescriptors() {
        if (mCachedAttributeDescriptors != null) {
            return mCachedAttributeDescriptors;
        }
        UiElementNode ui_parent = getUiParent();
        AttributeDescriptor[] direct_attrs = super.getAttributeDescriptors();
        mCachedAttributeDescriptors = direct_attrs;
        AttributeDescriptor[] layout_attrs = null;
        boolean need_xmlns = false;
        if (ui_parent instanceof UiDocumentNode) {
            List<ElementDescriptor> layoutDescriptors = null;
            IProject project = getEditor().getProject();
            if (project != null) {
                Sdk currentSdk = Sdk.getCurrent();
                if (currentSdk != null) {
                    IAndroidTarget target = currentSdk.getTarget(project);
                    if (target != null) {
                        AndroidTargetData data = currentSdk.getTargetData(target);
                        layoutDescriptors = data.getLayoutDescriptors().getLayoutDescriptors();
                    }
                }
            }
            if (layoutDescriptors != null) {
                for (ElementDescriptor desc : layoutDescriptors) {
                    if (desc instanceof ViewElementDescriptor &&
                            desc.getXmlName().equals(AndroidConstants.CLASS_NAME_FRAMELAYOUT)) {
                        layout_attrs = ((ViewElementDescriptor) desc).getLayoutAttributes();
                        need_xmlns = true;
                        break;
                    }
                }
            }
        } else if (ui_parent instanceof UiViewElementNode){
            layout_attrs =
                ((ViewElementDescriptor) ui_parent.getDescriptor()).getLayoutAttributes();
        }
        if (layout_attrs == null || layout_attrs.length == 0) {
            return mCachedAttributeDescriptors;
        }
        mCachedAttributeDescriptors =
            new AttributeDescriptor[direct_attrs.length +
                                    layout_attrs.length +
                                    (need_xmlns ? 1 : 0)];
        System.arraycopy(direct_attrs, 0,
                mCachedAttributeDescriptors, 0,
                direct_attrs.length);
        System.arraycopy(layout_attrs, 0,
                mCachedAttributeDescriptors, direct_attrs.length,
                layout_attrs.length);
        if (need_xmlns) {
            AttributeDescriptor desc = new XmlnsAttributeDescriptor(
                    "android",  
                    SdkConstants.NS_RESOURCES);
            mCachedAttributeDescriptors[direct_attrs.length + layout_attrs.length] = desc;
        }
        return mCachedAttributeDescriptors;
    }
    @Override
    protected void setUiParent(UiElementNode parent) {
        super.setUiParent(parent);
        mCachedAttributeDescriptors = null;
    }
}

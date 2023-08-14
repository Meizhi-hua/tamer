public class Test6524757 {
    private static final String[] KEYS = {
            "ColorChooser.okText", 
            "ColorChooser.cancelText", 
            "ColorChooser.resetText", 
            "ColorChooser.resetMnemonic", 
            "ColorChooser.swatchesNameText", 
            "ColorChooser.swatchesMnemonic", 
            "ColorChooser.swatchesSwatchSize", 
            "ColorChooser.swatchesRecentText", 
            "ColorChooser.swatchesRecentSwatchSize", 
            "ColorChooser.hsvNameText", 
            "ColorChooser.hsvMnemonic", 
            "ColorChooser.hsvHueText", 
            "ColorChooser.hsvSaturationText", 
            "ColorChooser.hsvValueText", 
            "ColorChooser.hsvTransparencyText", 
            "ColorChooser.hslNameText", 
            "ColorChooser.hslMnemonic", 
            "ColorChooser.hslHueText", 
            "ColorChooser.hslSaturationText", 
            "ColorChooser.hslLightnessText", 
            "ColorChooser.hslTransparencyText", 
            "ColorChooser.rgbNameText", 
            "ColorChooser.rgbMnemonic", 
            "ColorChooser.rgbRedText", 
            "ColorChooser.rgbGreenText", 
            "ColorChooser.rgbBlueText", 
            "ColorChooser.rgbAlphaText", 
            "ColorChooser.rgbHexCodeText", 
            "ColorChooser.rgbHexCodeMnemonic", 
            "ColorChooser.cmykNameText", 
            "ColorChooser.cmykMnemonic", 
            "ColorChooser.cmykCyanText", 
            "ColorChooser.cmykMagentaText", 
            "ColorChooser.cmykYellowText", 
            "ColorChooser.cmykBlackText", 
            "ColorChooser.cmykAlphaText", 
    };
    private static final Object[] KOREAN = convert(Locale.KOREAN, KEYS);
    private static final Object[] FRENCH = convert(Locale.FRENCH, KEYS);
    public static void main(String[] args) {
        Locale.setDefault(Locale.KOREAN);
        validate(KOREAN, create());
        Locale.setDefault(Locale.CANADA);
        validate(KOREAN, create());
        JComponent.setDefaultLocale(Locale.FRENCH);
        validate(FRENCH, create());
    }
    private static void validate(Object[] expected, Object[] actual) {
        int count = expected.length;
        if (count != actual.length) {
            throw new Error("different size: " + count + " <> " + actual.length);
        }
        for (int i = 0; i < count; i++) {
            if (!expected[i].equals(actual[i])) {
                throw new Error("unexpected value for key: " + KEYS[i]);
            }
        }
    }
    private static Object[] convert(Locale locale, String[] keys) {
        int count = keys.length;
        Object[] array = new Object[count];
        for (int i = 0; i < count; i++) {
            array[i] = convert(locale, keys[i]);
        }
        return array;
    }
    private static Object convert(Locale locale, String key) {
        if (key.endsWith("Text")) { 
            return UIManager.getString(key, locale);
        }
        if (key.endsWith("Size")) { 
            return UIManager.getDimension(key, locale);
        }
        if (key.endsWith("Color")) { 
            return UIManager.getColor(key, locale);
        }
        int value = SwingUtilities2.getUIDefaultsInt(key, locale, -1);
        return Integer.valueOf(value);
    }
    private static Object[] create() {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setVisible(true);
        JColorChooser chooser = new JColorChooser();
        JDialog dialog = JColorChooser.createDialog(frame, null, false, chooser, null, null);
        dialog.setVisible(true);
        List<Object> list = new ArrayList<Object>(KEYS.length);
        Component component = getC(getC(dialog.getLayeredPane(), 0), 1);
        AbstractButton ok = (AbstractButton) getC(component, 0);
        AbstractButton cancel = (AbstractButton) getC(component, 1);
        AbstractButton reset = (AbstractButton) getC(component, 2);
        list.add(ok.getText());
        list.add(cancel.getText());
        list.add(reset.getText());
        list.add(Integer.valueOf(reset.getMnemonic()));
        for (int i = 0; i < 5; i++) {
            AbstractColorChooserPanel panel = (AbstractColorChooserPanel) getC(getC(getC(chooser, 0), i), 0);
            list.add(panel.getDisplayName());
            list.add(Integer.valueOf(panel.getMnemonic()));
            if (i == 0) {
                JLabel label = (JLabel) getC(getC(panel, 0), 1);
                JPanel upper = (JPanel) getC(getC(getC(panel, 0), 0), 0);
                JPanel lower = (JPanel) getC(getC(getC(panel, 0), 2), 0);
                addSize(list, upper, 1, 1, 31, 9);
                list.add(label.getText());
                addSize(list, lower, 1, 1, 5, 7);
            }
            else {
                Component container = getC(panel, 0);
                for (int j = 0; j < 3; j++) {
                    AbstractButton button = (AbstractButton) getC(container, j);
                    list.add(button.getText());
                }
                JLabel label = (JLabel) getC(container, 3);
                list.add(label.getText());
                if (i == 4) {
                    label = (JLabel) getC(container, 4);
                    list.add(label.getText());
                }
                if (i == 3) {
                    label = (JLabel) getC(panel, 1);
                    list.add(label.getText());
                    list.add(Integer.valueOf(label.getDisplayedMnemonic()));
                }
            }
        }
        dialog.setVisible(false);
        dialog.dispose();
        frame.setVisible(false);
        frame.dispose();
        return list.toArray();
    }
    private static void addSize(List<Object> list, Component component, int x, int y, int w, int h) {
        Dimension size = component.getPreferredSize();
        int width = (size.width + 1) / w - x;
        int height = (size.height + 1) / h - y;
        list.add(new Dimension(width, height));
    }
    private static Component getC(Component component, int index) {
        Container container = (Container) component;
        return container.getComponent(index);
    }
}

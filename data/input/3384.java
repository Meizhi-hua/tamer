public class bug6823603 {
    private static final String TEST_ELEMENT = "Test1";
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        testRawSignatures();
        testGenericSignatures();
        testGetSelectedValuesList(); 
    }
    @SuppressWarnings("unchecked")
    private static void testRawSignatures() {
        ListModel rawTestModel = new DefaultListModel();
        new JList();
        new JList(rawTestModel);
        new JList(new Object[]{TEST_ELEMENT});
        JList rawTestList = new JList(new Vector());
        rawTestList.getPrototypeCellValue();
        rawTestList.setPrototypeCellValue(TEST_ELEMENT);
        rawTestList.getCellRenderer();
        rawTestList.setCellRenderer(new DefaultListCellRenderer());
        rawTestList.getModel();
        rawTestList.setModel(rawTestModel);
        rawTestList.setListData(new Object[]{TEST_ELEMENT});
        rawTestList.setListData(new Vector());
        @SuppressWarnings("deprecation")
        Object[] selectedValues = rawTestList.getSelectedValues();
        rawTestList.getSelectedValue();
        ListCellRenderer rawTestCellRenderer = new DefaultListCellRenderer();
        String testEntry = "Test";
        @SuppressWarnings("unchecked")
        JList rawJList = new JList(new Object[]{testEntry});
        rawTestCellRenderer.getListCellRendererComponent(rawJList,
                testEntry, 0, true, true);
        DefaultListModel testModel = new DefaultListModel();
        testModel.addElement(TEST_ELEMENT);
        rawTestModel = testModel;
        rawTestModel.getElementAt(0);
        DefaultListModel defaultListModel = new DefaultListModel();
        defaultListModel.addElement(TEST_ELEMENT);
        defaultListModel.getElementAt(0);
        defaultListModel.elements();
        defaultListModel.elementAt(0);
        defaultListModel.firstElement();
        defaultListModel.lastElement();
        String testElement2 = "Test2";
        defaultListModel.setElementAt(testElement2, 0);
        defaultListModel.insertElementAt(TEST_ELEMENT, 0);
        defaultListModel.get(0);
        defaultListModel.set(0, testElement2);
        defaultListModel.add(0, TEST_ELEMENT);
        defaultListModel.remove(0);
        @SuppressWarnings("serial")
        ListModel abstractListModel = new AbstractListModel() {
            public int getSize() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public Object getElementAt(int index) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        DefaultListCellRenderer cellRenderer = new DefaultListCellRenderer();
        @SuppressWarnings("unchecked")
        JList list = new JList(new Object[]{testEntry});
        cellRenderer.getListCellRendererComponent(rawJList, testEntry, 0, true, true);
    }
    private static <E> void testGenericSignatures() {
        ListModel<String> stringListModel = new DefaultListModel<String>();
        new JList<String>();
        new JList<String>(stringListModel);
        new JList<String>(new String[]{TEST_ELEMENT});
        JList<String> stringTestList = new JList<String>(new Vector<String>());
        stringTestList.getPrototypeCellValue();
        stringTestList.setPrototypeCellValue(TEST_ELEMENT);
        ListCellRenderer<? super String> cellRenderer = stringTestList.getCellRenderer();
        stringTestList.setCellRenderer(new DefaultListCellRenderer());
        ListModel<String> model = stringTestList.getModel();
        stringTestList.setModel(stringListModel);
        stringTestList.setListData(new String[]{TEST_ELEMENT});
        stringTestList.setListData(new Vector<String>());
        @SuppressWarnings("deprecation")
        Object[] selectedValues = stringTestList.getSelectedValues();
        stringTestList.getSelectedValue();
        ListCellRenderer<Object> stringTestCellRenderer =
                new DefaultListCellRenderer();
        String testEntry = "Test";
        JList<String> stringJList = new JList<String>(new String[]{testEntry});
        Component listCellRendererComponent2 =
                stringTestCellRenderer.getListCellRendererComponent(stringJList,
                testEntry, 0, true, true);
        DefaultListModel<String> testModel = new DefaultListModel<String>();
        testModel.addElement(TEST_ELEMENT);
        stringListModel = testModel;
        String element1 = stringListModel.getElementAt(0);
        DefaultListModel<String> stringTestModel = new DefaultListModel<String>();
        stringTestModel.addElement(TEST_ELEMENT);
        element1 = stringTestModel.getElementAt(0);
        Enumeration<String> elements = stringTestModel.elements();
        String element2 = stringTestModel.elementAt(0);
        String firstElement = stringTestModel.firstElement();
        String lastElement = stringTestModel.lastElement();
        String testElement2 = "Test2";
        stringTestModel.setElementAt(testElement2, 0);
        stringTestModel.insertElementAt(TEST_ELEMENT, 0);
        String element3 = stringTestModel.get(0);
        String element4 = stringTestModel.set(0, testElement2);
        stringTestModel.add(0, TEST_ELEMENT);
        String removedElement = stringTestModel.remove(0);
        stringListModel = new AbstractListModel<String>() {
            public int getSize() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public String getElementAt(int index) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        @SuppressWarnings("serial")
        ListModel<E> genericTestModel = new AbstractListModel<E>() {
            public int getSize() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
            public E getElementAt(int index) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        cellRenderer = new DefaultListCellRenderer();
        stringJList = new JList<String>(new String[]{testEntry});
        listCellRendererComponent2 = cellRenderer.getListCellRendererComponent(stringJList, testEntry, 0, true, true);
    }
    private static void testGetSelectedValuesList() {
        Vector<Integer> data = new Vector<Integer>();
        for (int i = 0; i < 10; i++) {
            data.add(i);
        }
        JList<Integer> list = new JList<Integer>(data);
        list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        list.setSelectedIndices(new int[]{1, 2, 3, 5, 6, 8});
        @SuppressWarnings("deprecation")
        Object[] expectedSelectedValues = list.getSelectedValues();
        List<Integer> selectedValuesList = list.getSelectedValuesList();
        assertEquals(expectedSelectedValues, selectedValuesList.toArray());
    }
    private static void assertEquals(Object[] expectedArray,
            Object[] actualArray) {
        if (!Arrays.equals(expectedArray, actualArray)) {
            throw new RuntimeException("Expected: " + Arrays.toString(
                    expectedArray) + " but was: " + Arrays.toString(actualArray));
        }
    }
}

final class MethodItem extends Item
{
    public MethodItem (final IItem parent, final int ID, final String name, final String descriptor, final int firstLine)
    {
        super (parent);
        m_ID = ID;
        m_name = name;
        m_descriptor = descriptor;
        m_firstLine = firstLine;
    }
    public String getName ()
    {
        if (m_userName == null)
        {
            m_userName = Descriptors.methodVMNameToJavaName (m_parent.getName (), m_name, m_descriptor, true, true, true);
        }
        return m_userName;
    }
    public int getID ()
    {
        return m_ID;
    }
    public int getFirstLine ()
    {
        return m_firstLine;
    }
    public int getAggregate (final int type)
    {
        final int [] aggregates = m_aggregates;
        int value = aggregates [type];
        if (value < 0)
        {
            final ClassItem parent = ((ClassItem) m_parent);
            final MethodDescriptor method = parent.m_cls.getMethods () [m_ID];
            final int status = method.getStatus ();
            if ((status & IMetadataConstants.METHOD_NO_BLOCK_DATA) != 0)
            {
                if ($assert.ENABLED) $assert.ASSERT (false, "excluded method in report data model");
                for (int i = 0; i < aggregates.length; ++ i) aggregates [i] = 0;
            }
            else
            {
                final boolean lineInfo = ((status & IMetadataConstants.METHOD_NO_LINE_NUMBER_TABLE) == 0);                
                final boolean [] coverage = parent.m_coverage != null ? parent.m_coverage [m_ID] : null;  
                final int totalBlockCount = method.getBlockCount ();
                aggregates [TOTAL_METHOD_COUNT] = 1; 
                aggregates [TOTAL_BLOCK_COUNT] = totalBlockCount;
                int totalBlockInstr = 0;
                final int [] blockSizes = method.getBlockSizes ();
                if (coverage != null)
                {
                    int coverageBlockCount = 0, coverageLineCount = 0;
                    int coverageBlockInstr = 0, coverageLineInstr = 0;
                    for (int b = 0; b < totalBlockCount; ++ b)
                    {
                        final int instr = blockSizes [b];
                        totalBlockInstr += instr;
                        if (coverage [b])
                        {
                            ++ coverageBlockCount;
                            coverageBlockInstr += instr;
                        }
                    }
                    if (lineInfo)
                    {
                        final IntObjectMap lineMap = method.getLineMap (); 
                        final int totalLineCount = lineMap.size ();
                        aggregates [TOTAL_LINE_COUNT] = totalLineCount;
                        final int [] lines = lineMap.keys ();
                        for (int l = 0; l < totalLineCount; ++ l)
                        {
                            final int [] blocks = (int []) lineMap.get (lines [l]);
                            int thisLineCoverageCount = 0; final int thisLineTotalCount = blocks.length;
                            int thisLineCoverageInstr = 0, thisLineTotalInstr = 0;
                            for (int bID = 0; bID < thisLineTotalCount; ++ bID)
                            {
                                final int b = blocks [bID];
                                final int instr = blockSizes [b];
                                thisLineTotalInstr += instr;
                                if (coverage [b])
                                {
                                    ++ thisLineCoverageCount;
                                    thisLineCoverageInstr += instr;
                                }
                            }
                            coverageLineCount += (PRECISION * thisLineCoverageCount) / thisLineTotalCount;
                            coverageLineInstr += (PRECISION * thisLineCoverageInstr) / thisLineTotalInstr;
                        }
                        aggregates [COVERAGE_LINE_COUNT] = coverageLineCount;
                        aggregates [COVERAGE_LINE_INSTR] = coverageLineInstr;
                    }
                    aggregates [TOTAL_BLOCK_INSTR] = totalBlockInstr;
                    aggregates [COVERAGE_METHOD_COUNT] = coverageBlockCount > 0 ? 1 : 0;                                        
                    aggregates [COVERAGE_BLOCK_COUNT] = coverageBlockCount;
                    aggregates [COVERAGE_BLOCK_INSTR] = coverageBlockInstr;
                }
                else
                {
                    for (int b = 0; b < totalBlockCount; ++ b)
                    {
                        totalBlockInstr += blockSizes [b];
                    }
                    aggregates [TOTAL_BLOCK_INSTR] = totalBlockInstr;
                    aggregates [COVERAGE_METHOD_COUNT] = 0;
                    aggregates [COVERAGE_BLOCK_COUNT] = 0;
                    aggregates [COVERAGE_BLOCK_INSTR] = 0;
                    if (lineInfo)
                    {
                        final IntObjectMap lineMap = method.getLineMap (); 
                        final int totalLineCount = lineMap.size ();
                        aggregates [TOTAL_LINE_COUNT] = totalLineCount;    
                        aggregates [COVERAGE_LINE_COUNT] = 0;
                        aggregates [COVERAGE_LINE_INSTR] = 0;
                    }
                }
            }
            return aggregates [type];
        }
        return value;
    }
    public void accept (final IItemVisitor visitor, final Object ctx)
    {
        visitor.visit (this, ctx);
    }
    public final IItemMetadata getMetadata ()
    {
        return METADATA;
    }
    public static IItemMetadata getTypeMetadata ()
    {
        return METADATA;
    }
    private final int m_ID;
    private final String m_name, m_descriptor;
    private final int m_firstLine;
    private transient String m_userName;
    private static final Item.ItemMetadata METADATA; 
    static
    {
        METADATA = new Item.ItemMetadata (IItemMetadata.TYPE_ID_METHOD, "method",
            1 << IItemAttribute.ATTRIBUTE_NAME_ID |
            1 << IItemAttribute.ATTRIBUTE_METHOD_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_BLOCK_COVERAGE_ID |
            1 << IItemAttribute.ATTRIBUTE_LINE_COVERAGE_ID);
    }
} 

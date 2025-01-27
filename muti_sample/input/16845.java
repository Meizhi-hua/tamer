public class SimpleInstrument extends ModelInstrument {
    private static class SimpleInstrumentPart {
        ModelPerformer[] performers;
        int keyFrom;
        int keyTo;
        int velFrom;
        int velTo;
        int exclusiveClass;
    }
    protected int preset = 0;
    protected int bank = 0;
    protected boolean percussion = false;
    protected String name = "";
    protected List<SimpleInstrumentPart> parts
            = new ArrayList<SimpleInstrumentPart>();
    public SimpleInstrument() {
        super(null, null, null, null);
    }
    public void clear() {
        parts.clear();
    }
    public void add(ModelPerformer[] performers, int keyFrom, int keyTo,
            int velFrom, int velTo, int exclusiveClass) {
        SimpleInstrumentPart part = new SimpleInstrumentPart();
        part.performers = performers;
        part.keyFrom = keyFrom;
        part.keyTo = keyTo;
        part.velFrom = velFrom;
        part.velTo = velTo;
        part.exclusiveClass = exclusiveClass;
        parts.add(part);
    }
    public void add(ModelPerformer[] performers, int keyFrom, int keyTo,
            int velFrom, int velTo) {
        add(performers, keyFrom, keyTo, velFrom, velTo, -1);
    }
    public void add(ModelPerformer[] performers, int keyFrom, int keyTo) {
        add(performers, keyFrom, keyTo, 0, 127, -1);
    }
    public void add(ModelPerformer[] performers) {
        add(performers, 0, 127, 0, 127, -1);
    }
    public void add(ModelPerformer performer, int keyFrom, int keyTo,
            int velFrom, int velTo, int exclusiveClass) {
        add(new ModelPerformer[]{performer}, keyFrom, keyTo, velFrom, velTo,
                exclusiveClass);
    }
    public void add(ModelPerformer performer, int keyFrom, int keyTo,
            int velFrom, int velTo) {
        add(new ModelPerformer[]{performer}, keyFrom, keyTo, velFrom, velTo);
    }
    public void add(ModelPerformer performer, int keyFrom, int keyTo) {
        add(new ModelPerformer[]{performer}, keyFrom, keyTo);
    }
    public void add(ModelPerformer performer) {
        add(new ModelPerformer[]{performer});
    }
    public void add(ModelInstrument ins, int keyFrom, int keyTo, int velFrom,
            int velTo, int exclusiveClass) {
        add(ins.getPerformers(), keyFrom, keyTo, velFrom, velTo, exclusiveClass);
    }
    public void add(ModelInstrument ins, int keyFrom, int keyTo, int velFrom,
            int velTo) {
        add(ins.getPerformers(), keyFrom, keyTo, velFrom, velTo);
    }
    public void add(ModelInstrument ins, int keyFrom, int keyTo) {
        add(ins.getPerformers(), keyFrom, keyTo);
    }
    public void add(ModelInstrument ins) {
        add(ins.getPerformers());
    }
    public ModelPerformer[] getPerformers() {
        int percount = 0;
        for (SimpleInstrumentPart part : parts)
            if (part.performers != null)
                percount += part.performers.length;
        ModelPerformer[] performers = new ModelPerformer[percount];
        int px = 0;
        for (SimpleInstrumentPart part : parts) {
            if (part.performers != null) {
                for (ModelPerformer mperfm : part.performers) {
                    ModelPerformer performer = new ModelPerformer();
                    performer.setName(getName());
                    performers[px++] = performer;
                    performer.setDefaultConnectionsEnabled(
                            mperfm.isDefaultConnectionsEnabled());
                    performer.setKeyFrom(mperfm.getKeyFrom());
                    performer.setKeyTo(mperfm.getKeyTo());
                    performer.setVelFrom(mperfm.getVelFrom());
                    performer.setVelTo(mperfm.getVelTo());
                    performer.setExclusiveClass(mperfm.getExclusiveClass());
                    performer.setSelfNonExclusive(mperfm.isSelfNonExclusive());
                    performer.setReleaseTriggered(mperfm.isReleaseTriggered());
                    if (part.exclusiveClass != -1)
                        performer.setExclusiveClass(part.exclusiveClass);
                    if (part.keyFrom > performer.getKeyFrom())
                        performer.setKeyFrom(part.keyFrom);
                    if (part.keyTo < performer.getKeyTo())
                        performer.setKeyTo(part.keyTo);
                    if (part.velFrom > performer.getVelFrom())
                        performer.setVelFrom(part.velFrom);
                    if (part.velTo < performer.getVelTo())
                        performer.setVelTo(part.velTo);
                    performer.getOscillators().addAll(mperfm.getOscillators());
                    performer.getConnectionBlocks().addAll(
                            mperfm.getConnectionBlocks());
                }
            }
        }
        return performers;
    }
    public Object getData() {
        return null;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public ModelPatch getPatch() {
        return new ModelPatch(bank, preset, percussion);
    }
    public void setPatch(Patch patch) {
        if (patch instanceof ModelPatch && ((ModelPatch)patch).isPercussion()) {
            percussion = true;
            bank = patch.getBank();
            preset = patch.getProgram();
        } else {
            percussion = false;
            bank = patch.getBank();
            preset = patch.getProgram();
        }
    }
}

public class FilterChain implements ChangedEventProvider<FilterChain> {
    private List<Filter> filters;
    private transient ChangedEvent<FilterChain> changedEvent;
    private boolean fireEvents;
    public FilterChain() {
        filters = new ArrayList<Filter>();
        changedEvent = new ChangedEvent<FilterChain>(this);
        this.fireEvents = true;
    }
    public FilterChain(FilterChain f) {
        this.filters = new ArrayList<Filter>(f.filters);
        changedEvent = new ChangedEvent<FilterChain>(this);
        this.fireEvents = true;
    }
    public ChangedEvent<FilterChain> getChangedEvent() {
        return changedEvent;
    }
    public Filter getFilterAt(int index) {
        assert index >= 0 && index < filters.size();
        return filters.get(index);
    }
    public void apply(Diagram d) {
        for (Filter f : filters) {
            f.apply(d);
        }
    }
    public void apply(Diagram d, FilterChain sequence) {
        List<Filter> applied = new ArrayList<Filter>();
        for (Filter f : sequence.getFilters()) {
            if (filters.contains(f)) {
                f.apply(d);
                applied.add(f);
            }
        }
        for (Filter f : filters) {
            if (!applied.contains(f)) {
                f.apply(d);
            }
        }
    }
    public void beginAtomic() {
        this.fireEvents = false;
    }
    public void endAtomic() {
        this.fireEvents = true;
        changedEvent.fire();
    }
    public void addFilter(Filter filter) {
        assert filter != null;
        filters.add(filter);
        if (fireEvents) {
            changedEvent.fire();
        }
    }
    public void addFilterSameSequence(Filter filter) {
        assert filter != null;
        filters.add(filter);
        if (fireEvents) {
            changedEvent.fire();
        }
    }
    public boolean containsFilter(Filter filter) {
        return filters.contains(filter);
    }
    public void removeFilter(Filter filter) {
        assert filters.contains(filter);
        filters.remove(filter);
        if (fireEvents) {
            changedEvent.fire();
        }
    }
    public void moveFilterUp(Filter filter) {
        assert filters.contains(filter);
        int index = filters.indexOf(filter);
        if (index != 0) {
            filters.remove(index);
            filters.add(index - 1, filter);
        }
        if (fireEvents) {
            changedEvent.fire();
        }
    }
    public void moveFilterDown(Filter filter) {
        assert filters.contains(filter);
        int index = filters.indexOf(filter);
        if (index != filters.size() - 1) {
            filters.remove(index);
            filters.add(index + 1, filter);
        }
        if (fireEvents) {
            changedEvent.fire();
        }
    }
    public List<Filter> getFilters() {
        return Collections.unmodifiableList(filters);
    }
    public void clear() {
        filters.clear();
    }
}

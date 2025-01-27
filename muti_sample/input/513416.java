abstract class AbstractMultiset<E> extends AbstractCollection<E>
    implements Multiset<E> {
  public abstract Set<Entry<E>> entrySet();
  @Override public int size() {
    long sum = 0L;
    for (Entry<E> entry : entrySet()) {
      sum += entry.getCount();
    }
    return (int) Math.min(sum, Integer.MAX_VALUE);
  }
  @Override public boolean isEmpty() {
    return entrySet().isEmpty();
  }
  @Override public boolean contains(@Nullable Object element) {
    return elementSet().contains(element);
  }
  @Override public Iterator<E> iterator() {
    return new MultisetIterator();
  }
  private class MultisetIterator implements Iterator<E> {
    private final Iterator<Entry<E>> entryIterator;
    private Entry<E> currentEntry;
    private int laterCount;
    private int totalCount;
    private boolean canRemove;
    MultisetIterator() {
      this.entryIterator = entrySet().iterator();
    }
    public boolean hasNext() {
      return laterCount > 0 || entryIterator.hasNext();
    }
    public E next() {
      if (!hasNext()) {
        throw new NoSuchElementException();
      }
      if (laterCount == 0) {
        currentEntry = entryIterator.next();
        totalCount = laterCount = currentEntry.getCount();
      }
      laterCount--;
      canRemove = true;
      return currentEntry.getElement();
    }
    public void remove() {
      checkState(canRemove,
          "no calls to next() since the last call to remove()");
      if (totalCount == 1) {
        entryIterator.remove();
      } else {
        AbstractMultiset.this.remove(currentEntry.getElement());
      }
      totalCount--;
      canRemove = false;
    }
  }
  public int count(Object element) {
    for (Entry<E> entry : entrySet()) {
      if (Objects.equal(entry.getElement(), element)) {
        return entry.getCount();
      }
    }
    return 0;
  }
  @Override public boolean add(@Nullable E element) {
    add(element, 1);
    return true;
  }
  public int add(E element, int occurrences) {
    throw new UnsupportedOperationException();
  }
  @Override public boolean remove(Object element) {
    return remove(element, 1) > 0;
  }
  public int remove(Object element, int occurrences) {
    throw new UnsupportedOperationException();
  }
  public int setCount(E element, int count) {
    return setCountImpl(this, element, count);
  }
  public boolean setCount(E element, int oldCount, int newCount) {
    return setCountImpl(this, element, oldCount, newCount);
  }
  @Override public boolean containsAll(Collection<?> elements) {
    return elementSet().containsAll(elements);
  }
  @Override public boolean addAll(Collection<? extends E> elementsToAdd) {
    if (elementsToAdd.isEmpty()) {
      return false;
    }
    if (elementsToAdd instanceof Multiset) {
      @SuppressWarnings("unchecked")
      Multiset<? extends E> that = (Multiset<? extends E>) elementsToAdd;
      for (Entry<? extends E> entry : that.entrySet()) {
        add(entry.getElement(), entry.getCount());
      }
    } else {
      super.addAll(elementsToAdd);
    }
    return true;
  }
  @Override public boolean removeAll(Collection<?> elementsToRemove) {
    Collection<?> collection = (elementsToRemove instanceof Multiset)
        ? ((Multiset<?>) elementsToRemove).elementSet() : elementsToRemove;
    return elementSet().removeAll(collection);
  }
  @Override public boolean retainAll(Collection<?> elementsToRetain) {
    checkNotNull(elementsToRetain);
    Iterator<Entry<E>> entries = entrySet().iterator();
    boolean modified = false;
    while (entries.hasNext()) {
      Entry<E> entry = entries.next();
      if (!elementsToRetain.contains(entry.getElement())) {
        entries.remove();
        modified = true;
      }
    }
    return modified;
  }
  @Override public void clear() {
    entrySet().clear();
  }
  private transient Set<E> elementSet;
  public Set<E> elementSet() {
    Set<E> result = elementSet;
    if (result == null) {
      elementSet = result = createElementSet();
    }
    return result;
  }
  Set<E> createElementSet() {
    return new ElementSet();
  }
  private class ElementSet extends AbstractSet<E> {
    @Override public Iterator<E> iterator() {
      final Iterator<Entry<E>> entryIterator = entrySet().iterator();
      return new Iterator<E>() {
        public boolean hasNext() {
          return entryIterator.hasNext();
        }
        public E next() {
          return entryIterator.next().getElement();
        }
        public void remove() {
          entryIterator.remove();
        }
      };
    }
    @Override public int size() {
      return entrySet().size();
    }
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (object instanceof Multiset) {
      Multiset<?> that = (Multiset<?>) object;
      if (this.size() != that.size()) {
        return false;
      }
      for (Entry<?> entry : that.entrySet()) {
        if (count(entry.getElement()) != entry.getCount()) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
  @Override public int hashCode() {
    return entrySet().hashCode();
  }
  @Override public String toString() {
    return entrySet().toString();
  }
}

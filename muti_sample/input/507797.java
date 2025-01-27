class RegularImmutableList<E> extends ImmutableList<E> {
  private final transient int offset;
  private final transient int size;
  private final transient Object[] array;
  RegularImmutableList(Object[] array, int offset, int size) {
    this.offset = offset;
    this.size = size;
    this.array = array;
  }
  RegularImmutableList(Object[] array) {
    this(array, 0, array.length);
  }
  public int size() {
    return size;
  }
  @Override public boolean isEmpty() {
    return false;
  }
  @Override public boolean contains(Object target) {
    return indexOf(target) != -1;
  }
  @SuppressWarnings("unchecked")
  @Override public UnmodifiableIterator<E> iterator() {
    return (UnmodifiableIterator<E>) Iterators.forArray(array, offset, size);
  }
  @Override public Object[] toArray() {
    Object[] newArray = new Object[size()];
    Platform.unsafeArrayCopy(array, offset, newArray, 0, size);
    return newArray;
  }
  @Override public <T> T[] toArray(T[] other) {
    if (other.length < size) {
      other = ObjectArrays.newArray(other, size);
    } else if (other.length > size) {
      other[size] = null;
    }
    Platform.unsafeArrayCopy(array, offset, other, 0, size);
    return other;
  }
  @SuppressWarnings("unchecked")
  public E get(int index) {
    Preconditions.checkElementIndex(index, size);
    return (E) array[index + offset];
  }
  @Override public int indexOf(Object target) {
    if (target != null) {
      for (int i = offset; i < offset + size; i++) {
        if (array[i].equals(target)) {
          return i - offset;
        }
      }
    }
    return -1;
  }
  @Override public int lastIndexOf(Object target) {
    if (target != null) {
      for (int i = offset + size - 1; i >= offset; i--) {
        if (array[i].equals(target)) {
          return i - offset;
        }
      }
    }
    return -1;
  }
  @Override public ImmutableList<E> subList(int fromIndex, int toIndex) {
    Preconditions.checkPositionIndexes(fromIndex, toIndex, size);
    return (fromIndex == toIndex)
        ? ImmutableList.<E>of()
        : new RegularImmutableList<E>(
            array, offset + fromIndex, toIndex - fromIndex);
  }
  public ListIterator<E> listIterator() {
    return listIterator(0);
  }
  public ListIterator<E> listIterator(final int start) {
    Preconditions.checkPositionIndex(start, size);
    return new ListIterator<E>() {
      int index = start;
      public boolean hasNext() {
        return index < size;
      }
      public boolean hasPrevious() {
        return index > 0;
      }
      public int nextIndex() {
        return index;
      }
      public int previousIndex() {
        return index - 1;
      }
      public E next() {
        E result;
        try {
          result = get(index);
        } catch (IndexOutOfBoundsException rethrown) {
          throw new NoSuchElementException();
        }
        index++;
        return result;
      }
      public E previous() {
        E result;
        try {
          result = get(index - 1);
        } catch (IndexOutOfBoundsException rethrown) {
          throw new NoSuchElementException();
        }
        index--;
        return result;
      }
      public void set(E o) {
        throw new UnsupportedOperationException();
      }
      public void add(E o) {
        throw new UnsupportedOperationException();
      }
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  @Override public boolean equals(@Nullable Object object) {
    if (object == this) {
      return true;
    }
    if (!(object instanceof List)) {
      return false;
    }
    List<?> that = (List<?>) object;
    if (this.size() != that.size()) {
      return false;
    }
    int index = offset;
    if (object instanceof RegularImmutableList) {
      RegularImmutableList<?> other = (RegularImmutableList<?>) object;
      for (int i = other.offset; i < other.offset + other.size; i++) {
        if (!array[index++].equals(other.array[i])) {
          return false;
        }
      }
    } else {
      for (Object element : that) {
        if (!array[index++].equals(element)) {
          return false;
        }
      }
    }
    return true;
  }
  @Override public int hashCode() {
    int hashCode = 1;
    for (int i = offset; i < offset + size; i++) {
      hashCode = 31 * hashCode + array[i].hashCode();
    }
    return hashCode;
  }
  @Override public String toString() {
    StringBuilder sb = new StringBuilder(size() * 16);
    sb.append('[').append(array[offset]);
    for (int i = offset + 1; i < offset + size; i++) {
      sb.append(", ").append(array[i]);
    }
    return sb.append(']').toString();
  }
  int offset() {
    return offset;
  }
  Object[] array() {
    return array;
  }
}

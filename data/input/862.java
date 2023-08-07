class ClientLeaseMapWrapper extends AbstractMap implements LeaseMap {
    private static final long serialVersionUID = 1L;
    private static final Method[] leaseToLeaseMapMethods;
    static {
        try {
            Method cancel = Lease.class.getMethod("cancel", new Class[] {});
            Method cancelAll = LeaseMap.class.getMethod("cancelAll", new Class[] {});
            Method renew = Lease.class.getMethod("renew", new Class[] { long.class });
            Method renewAll = LeaseMap.class.getMethod("renewAll", new Class[] {});
            leaseToLeaseMapMethods = new Method[] { cancel, cancelAll, renew, renewAll };
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        }
    }
    private final LeaseMap clientLeaseMap;
    private final Map wrapperMap = new HashMap();
    private final ClientLeaseWrapper example;
    ClientLeaseMapWrapper(ClientLeaseWrapper wrapper, long duration) {
        final Lease clientLease = wrapper.getClientLease();
        if (clientLease == null) {
            throw new IllegalArgumentException("Wrapper cannot be deformed");
        }
        LeaseMap leaseMap = clientLease.createLeaseMap(duration);
        if (clientLease instanceof RemoteMethodControl && leaseMap instanceof RemoteMethodControl) {
            leaseMap = (LeaseMap) ((RemoteMethodControl) leaseMap).setConstraints(ConstrainableProxyUtil.translateConstraints(((RemoteMethodControl) clientLease).getConstraints(), leaseToLeaseMapMethods));
        }
        clientLeaseMap = leaseMap;
        wrapperMap.put(clientLease, wrapper);
        example = wrapper;
    }
    public void cancelAll() {
        throw new UnsupportedOperationException("ClientLeaseMapWrapper.cancelAll: " + "LRS should not being canceling client leases");
    }
    private void applyException(Throwable t) {
        for (Iterator i = wrapperMap.values().iterator(); i.hasNext(); ) {
            final ClientLeaseWrapper clw = (ClientLeaseWrapper) i.next();
            clw.failedRenewal(t);
        }
    }
    public void renewAll() throws LeaseMapException, RemoteException {
        LeaseMapException lme = null;
        Map newExceptionMap = null;
        final long now = System.currentTimeMillis();
        for (Iterator i = wrapperMap.values().iterator(); i.hasNext(); ) {
            final ClientLeaseWrapper clw = (ClientLeaseWrapper) i.next();
            if (!clw.ensureCurrent(now)) {
                if (newExceptionMap == null) newExceptionMap = new HashMap(wrapperMap.size());
                newExceptionMap.put(clw, LRMEventListener.EXPIRED_SET_EXCEPTION);
                i.remove();
                clientLeaseMap.remove(clw.getClientLease());
            }
        }
        if (clientLeaseMap.isEmpty()) {
            if (newExceptionMap == null) return;
            throw new LeaseMapException("Expired Sets", newExceptionMap);
        }
        try {
            clientLeaseMap.renewAll();
        } catch (LeaseMapException e) {
            lme = e;
        } catch (RemoteException e) {
            applyException(e);
            throw e;
        } catch (Error e) {
            applyException(e);
            throw e;
        } catch (RuntimeException e) {
            applyException(e);
            throw e;
        }
        for (Iterator i = clientLeaseMap.keySet().iterator(); i.hasNext(); ) {
            final Lease cl = (Lease) i.next();
            final ClientLeaseWrapper clw = (ClientLeaseWrapper) wrapperMap.get(cl);
            clw.successfulRenewal();
        }
        if (lme == null && newExceptionMap == null) return;
        if (lme != null) {
            final Map exceptionMap = lme.exceptionMap;
            if (newExceptionMap == null) newExceptionMap = new HashMap(exceptionMap.size());
            for (Iterator i = exceptionMap.entrySet().iterator(); i.hasNext(); ) {
                final Map.Entry e = (Map.Entry) i.next();
                final Lease cl = (Lease) e.getKey();
                final Throwable t = (Throwable) e.getValue();
                final ClientLeaseWrapper clw = (ClientLeaseWrapper) wrapperMap.remove(cl);
                i.remove();
                clw.failedRenewal(t);
                newExceptionMap.put(clw, t);
            }
        }
        if (newExceptionMap != null) {
            throw new LeaseMapException((lme == null) ? "Expired Sets" : lme.getMessage(), newExceptionMap);
        }
        return;
    }
    public boolean canContainKey(Object key) {
        return key instanceof Lease && example.canBatch((Lease) key);
    }
    private Lease checkKey(Object key) {
        if (canContainKey(key)) return ((ClientLeaseWrapper) key).getClientLease();
        throw new IllegalArgumentException("key is not valid for this LeaseMap");
    }
    private static void checkValue(Object value) {
        if (!(value instanceof Long)) throw new IllegalArgumentException("value is not a Long");
    }
    public boolean containsKey(Object key) {
        final Lease cl = checkKey(key);
        return clientLeaseMap.containsKey(cl);
    }
    public boolean containsValue(Object value) {
        checkValue(value);
        return clientLeaseMap.containsValue(value);
    }
    public Object get(Object key) {
        final Lease cl = checkKey(key);
        return clientLeaseMap.get(cl);
    }
    public Object put(Object key, Object value) {
        final Lease cl = checkKey(key);
        checkValue(value);
        wrapperMap.put(cl, key);
        return clientLeaseMap.put(cl, value);
    }
    public Object remove(Object key) {
        final Lease cl = checkKey(key);
        wrapperMap.remove(cl);
        return clientLeaseMap.remove(cl);
    }
    public void putAll(Map m) {
        Iterator iter = m.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry e = (Map.Entry) iter.next();
            put(e.getKey(), e.getValue());
        }
    }
    public void clear() {
        clientLeaseMap.clear();
        wrapperMap.clear();
    }
    public boolean equals(Object o) {
        return clientLeaseMap.equals(o);
    }
    public int hashCode() {
        return clientLeaseMap.hashCode();
    }
    public Set entrySet() {
        return new EntrySet();
    }
    private final class EntrySet extends AbstractSet {
        public Iterator iterator() {
            return new EntryIterator();
        }
        private Lease getClientLease(Object o) {
            if (!(o instanceof Map.Entry)) return null;
            final Map.Entry e = (Map.Entry) o;
            final Object eValue = e.getValue();
            if (!(e.getKey() instanceof ClientLeaseWrapper) || !(eValue instanceof Long) || (eValue == null)) {
                return null;
            }
            final ClientLeaseWrapper clw = (ClientLeaseWrapper) e.getKey();
            return clw.getClientLease();
        }
        public boolean contains(Object o) {
            final Lease cl = getClientLease(o);
            if (cl == null) return false;
            final Object eValue = ((Map.Entry) o).getValue();
            final Object value = clientLeaseMap.get(cl);
            if (value == null) return false;
            return value.equals(eValue);
        }
        public boolean remove(Object o) {
            final Lease cl = getClientLease(o);
            if (cl == null) return false;
            final Object eValue = ((Map.Entry) o).getValue();
            final Object value = clientLeaseMap.get(cl);
            if (value == null || !value.equals(eValue)) return false;
            clientLeaseMap.remove(cl);
            wrapperMap.remove(cl);
            return true;
        }
        public int size() {
            return clientLeaseMap.size();
        }
        public void clear() {
            wrapperMap.clear();
            clientLeaseMap.clear();
        }
    }
    private final class Entry implements Map.Entry {
        private final ClientLeaseWrapper key;
        public Entry(ClientLeaseWrapper key) {
            this.key = key;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return clientLeaseMap.get(key.getClientLease());
        }
        public Object setValue(Object value) {
            checkValue(value);
            return clientLeaseMap.put(key.getClientLease(), value);
        }
        public boolean equals(Object o) {
            if (o instanceof Entry) {
                final Entry that = (Entry) o;
                return that.key.equals(key);
            }
            return false;
        }
        public int hashCode() {
            return key.hashCode();
        }
    }
    private final class EntryIterator implements Iterator {
        private final Iterator iter;
        private Lease last;
        public EntryIterator() {
            iter = wrapperMap.entrySet().iterator();
        }
        public boolean hasNext() {
            return iter.hasNext();
        }
        public Object next() {
            final Map.Entry e = (Map.Entry) iter.next();
            last = (Lease) e.getKey();
            return new Entry((ClientLeaseWrapper) e.getValue());
        }
        public void remove() {
            clientLeaseMap.remove(last);
            iter.remove();
        }
    }
}

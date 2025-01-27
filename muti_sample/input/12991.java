class CastTest {
    private interface CA<T> { }
    private interface CB<T> extends CA<T> { }
    private interface CC<T> extends CA<T> { }
    private class CD<T> implements CB<T> { }
    private interface CE<T> extends CC<T> { }
    private interface CF<S> { }
    private interface CG<T> { }
    private class CH<S, T> implements CF<S>, CG<T> { }
    private interface CI<S> extends CF<S> { }
    private interface CJ<T> extends CG<T> { }
    private interface CK<S, T> extends CI<S>, CJ<T> { }
    private void supertypeParameterTransfer() {
        Object o;
        CE<?> ce = (CD<?>) null; 
    }
}

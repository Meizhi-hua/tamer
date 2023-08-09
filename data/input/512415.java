public class URIUtils {
    public static URI createURI(
            final String scheme,
            final String host,
            int port,
            final String path,
            final String query,
            final String fragment) throws URISyntaxException {
        StringBuilder buffer = new StringBuilder();
        if (host != null) {
            if (scheme != null) {
                buffer.append(scheme);
                buffer.append(":
            }
            buffer.append(host);
            if (port > 0) {
                buffer.append(':');
                buffer.append(port);
            }
        }
        if (path == null || !path.startsWith("/")) {
            buffer.append('/');
        }
        if (path != null) {
            buffer.append(path);
        }
        if (query != null) {
            buffer.append('?');
            buffer.append(query);
        }
        if (fragment != null) {
            buffer.append('#');
            buffer.append(fragment);
        }
        return new URI(buffer.toString());
    }
    public static URI rewriteURI(
            final URI uri, 
            final HttpHost target,
            boolean dropFragment) throws URISyntaxException {
        if (uri == null) {
            throw new IllegalArgumentException("URI may nor be null");
        }
        if (target != null) {
            return URIUtils.createURI(
                    target.getSchemeName(), 
                    target.getHostName(), 
                    target.getPort(), 
                    uri.getRawPath(), 
                    uri.getRawQuery(), 
                    dropFragment ? null : uri.getRawFragment());
        } else {
            return URIUtils.createURI(
                    null, 
                    null, 
                    -1, 
                    uri.getRawPath(), 
                    uri.getRawQuery(), 
                    dropFragment ? null : uri.getRawFragment());
        }
    }
    public static URI rewriteURI(
            final URI uri, 
            final HttpHost target) throws URISyntaxException {
        return rewriteURI(uri, target, false);
    }
    public static URI resolve(final URI baseURI, final String reference) {
        return URIUtils.resolve(baseURI, URI.create(reference));
    }
    public static URI resolve(final URI baseURI, URI reference){
        if (baseURI == null) {
            throw new IllegalArgumentException("Base URI may nor be null");
        }
        if (reference == null) {
            throw new IllegalArgumentException("Reference URI may nor be null");
        }
        boolean emptyReference = reference.toString().length() == 0;
        if (emptyReference) {
            reference = URI.create("#");
        }
        URI resolved = baseURI.resolve(reference);
        if (emptyReference) {
            String resolvedString = resolved.toString();
            resolved = URI.create(resolvedString.substring(0,
                resolvedString.indexOf('#')));
        }
        return resolved;
    }
    private URIUtils() {
    }
}

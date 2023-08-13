public final class URLUtil {
    private static final String LOGTAG = "webkit";
    static final String ASSET_BASE = "file:
    static final String RESOURCE_BASE = "file:
    static final String FILE_BASE = "file:
    static final String PROXY_BASE = "file:
    public static String guessUrl(String inUrl) {
        String retVal = inUrl;
        WebAddress webAddress;
        Log.v(LOGTAG, "guessURL before queueRequest: " + inUrl);
        if (inUrl.length() == 0) return inUrl;
        if (inUrl.startsWith("about:")) return inUrl;
        if (inUrl.startsWith("data:")) return inUrl;
        if (inUrl.startsWith("file:")) return inUrl;
        if (inUrl.startsWith("javascript:")) return inUrl;
        if (inUrl.endsWith(".") == true) {
            inUrl = inUrl.substring(0, inUrl.length() - 1);
        }
        try {
            webAddress = new WebAddress(inUrl);
        } catch (ParseException ex) {
            if (DebugFlags.URL_UTIL) {
                Log.v(LOGTAG, "smartUrlFilter: failed to parse url = " + inUrl);
            }
            return retVal;
        }
        if (webAddress.mHost.indexOf('.') == -1) {
            webAddress.mHost = "www." + webAddress.mHost + ".com";
        }
        return webAddress.toString();
    }
    public static String composeSearchUrl(String inQuery, String template,
                                          String queryPlaceHolder) {
        int placeHolderIndex = template.indexOf(queryPlaceHolder);
        if (placeHolderIndex < 0) {
            return null;
        }
        String query;
        StringBuilder buffer = new StringBuilder();
        buffer.append(template.substring(0, placeHolderIndex));
        try {
            query = java.net.URLEncoder.encode(inQuery, "utf-8");
            buffer.append(query);
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
        buffer.append(template.substring(
                placeHolderIndex + queryPlaceHolder.length()));
        return buffer.toString();
    }
    public static byte[] decode(byte[] url) throws IllegalArgumentException {
        if (url.length == 0) {
            return new byte[0];
        }
        byte[] tempData = new byte[url.length];
        int tempCount = 0;
        for (int i = 0; i < url.length; i++) {
            byte b = url[i];
            if (b == '%') {
                if (url.length - i > 2) {
                    b = (byte) (parseHex(url[i + 1]) * 16
                            + parseHex(url[i + 2]));
                    i += 2;
                } else {
                    throw new IllegalArgumentException("Invalid format");
                }
            }
            tempData[tempCount++] = b;
        }
        byte[] retData = new byte[tempCount];
        System.arraycopy(tempData, 0, retData, 0, tempCount);
        return retData;
    }
    static boolean verifyURLEncoding(String url) {
        int count = url.length();
        if (count == 0) {
            return false;
        }
        int index = url.indexOf('%');
        while (index >= 0 && index < count) {
            if (index < count - 2) {
                try {
                    parseHex((byte) url.charAt(++index));
                    parseHex((byte) url.charAt(++index));
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else {
                return false;
            }
            index = url.indexOf('%', index + 1);
        }
        return true;
    }
    private static int parseHex(byte b) {
        if (b >= '0' && b <= '9') return (b - '0');
        if (b >= 'A' && b <= 'F') return (b - 'A' + 10);
        if (b >= 'a' && b <= 'f') return (b - 'a' + 10);
        throw new IllegalArgumentException("Invalid hex char '" + b + "'");
    }
    public static boolean isAssetUrl(String url) {
        return (null != url) && url.startsWith(ASSET_BASE);
    }
    public static boolean isResourceUrl(String url) {
        return (null != url) && url.startsWith(RESOURCE_BASE);
    }
    @Deprecated
    public static boolean isCookielessProxyUrl(String url) {
        return (null != url) && url.startsWith(PROXY_BASE);
    }
    public static boolean isFileUrl(String url) {
        return (null != url) && (url.startsWith(FILE_BASE) &&
                                 !url.startsWith(ASSET_BASE) &&
                                 !url.startsWith(PROXY_BASE));
    }
    public static boolean isAboutUrl(String url) {
        return (null != url) && url.startsWith("about:");
    }
    public static boolean isDataUrl(String url) {
        return (null != url) && url.startsWith("data:");
    }
    public static boolean isJavaScriptUrl(String url) {
        return (null != url) && url.startsWith("javascript:");
    }
    public static boolean isHttpUrl(String url) {
        return (null != url) &&
               (url.length() > 6) &&
               url.substring(0, 7).equalsIgnoreCase("http:
    }
    public static boolean isHttpsUrl(String url) {
        return (null != url) &&
               (url.length() > 7) &&
               url.substring(0, 8).equalsIgnoreCase("https:
    }
    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return isHttpUrl(url) || isHttpsUrl(url);
    }
    public static boolean isContentUrl(String url) {
        return (null != url) && url.startsWith("content:");
    }
    public static boolean isValidUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return (isAssetUrl(url) ||
                isResourceUrl(url) ||
                isFileUrl(url) ||
                isAboutUrl(url) ||
                isHttpUrl(url) ||
                isHttpsUrl(url) ||
                isJavaScriptUrl(url) ||
                isContentUrl(url));
    }
    public static String stripAnchor(String url) {
        int anchorIndex = url.indexOf('#');
        if (anchorIndex != -1) {
            return url.substring(0, anchorIndex);
        }
        return url;
    }
    public static final String guessFileName(
            String url,
            String contentDisposition,
            String mimeType) {
        String filename = null;
        String extension = null;
        if (filename == null && contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                    }
                }
            }
        }
        if (filename == null) {
            filename = "downloadfile";
        }
        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                }
            }
            if (extension == null) {
                if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                    if (mimeType.equalsIgnoreCase("text/html")) {
                        extension = ".html";
                    } else {
                        extension = ".txt";
                    }
                } else {
                    extension = ".bin";
                }
            }
        } else {
            if (mimeType != null) {
                int lastDotIndex = filename.lastIndexOf('.');
                String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        filename.substring(lastDotIndex + 1));
                if (typeFromExt != null && !typeFromExt.equalsIgnoreCase(mimeType)) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        extension = "." + extension;
                    }
                }
            }
            if (extension == null) {
                extension = filename.substring(dotIndex);
            }
            filename = filename.substring(0, dotIndex);
        }
        return filename + extension;
    }
    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$",
            Pattern.CASE_INSENSITIVE);
    static String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(2);
            }
        } catch (IllegalStateException ex) {
        }
        return null;
    }
}

public class LocaleExtensions {
    private final Map<Character, Extension> extensionMap;
    private final String id;
    public static final LocaleExtensions CALENDAR_JAPANESE
        = new LocaleExtensions("u-ca-japanese",
                               UnicodeLocaleExtension.SINGLETON,
                               UnicodeLocaleExtension.CA_JAPANESE);
    public static final LocaleExtensions NUMBER_THAI
        = new LocaleExtensions("u-nu-thai",
                               UnicodeLocaleExtension.SINGLETON,
                               UnicodeLocaleExtension.NU_THAI);
    private LocaleExtensions(String id, Character key, Extension value) {
        this.id = id;
        this.extensionMap = Collections.singletonMap(key, value);
    }
    LocaleExtensions(Map<CaseInsensitiveChar, String> extensions,
                     Set<CaseInsensitiveString> uattributes,
                     Map<CaseInsensitiveString, String> ukeywords) {
        boolean hasExtension = !LocaleUtils.isEmpty(extensions);
        boolean hasUAttributes = !LocaleUtils.isEmpty(uattributes);
        boolean hasUKeywords = !LocaleUtils.isEmpty(ukeywords);
        if (!hasExtension && !hasUAttributes && !hasUKeywords) {
            id = "";
            extensionMap = Collections.emptyMap();
            return;
        }
        SortedMap<Character, Extension> map = new TreeMap<>();
        if (hasExtension) {
            for (Entry<CaseInsensitiveChar, String> ext : extensions.entrySet()) {
                char key = LocaleUtils.toLower(ext.getKey().value());
                String value = ext.getValue();
                if (LanguageTag.isPrivateusePrefixChar(key)) {
                    value = InternalLocaleBuilder.removePrivateuseVariant(value);
                    if (value == null) {
                        continue;
                    }
                }
                map.put(key, new Extension(key, LocaleUtils.toLowerString(value)));
            }
        }
        if (hasUAttributes || hasUKeywords) {
            SortedSet<String> uaset = null;
            SortedMap<String, String> ukmap = null;
            if (hasUAttributes) {
                uaset = new TreeSet<>();
                for (CaseInsensitiveString cis : uattributes) {
                    uaset.add(LocaleUtils.toLowerString(cis.value()));
                }
            }
            if (hasUKeywords) {
                ukmap = new TreeMap<>();
                for (Entry<CaseInsensitiveString, String> kwd : ukeywords.entrySet()) {
                    String key = LocaleUtils.toLowerString(kwd.getKey().value());
                    String type = LocaleUtils.toLowerString(kwd.getValue());
                    ukmap.put(key, type);
                }
            }
            UnicodeLocaleExtension ule = new UnicodeLocaleExtension(uaset, ukmap);
            map.put(UnicodeLocaleExtension.SINGLETON, ule);
        }
        if (map.isEmpty()) {
            id = "";
            extensionMap = Collections.emptyMap();
        } else {
            id = toID(map);
            extensionMap = map;
        }
    }
    public Set<Character> getKeys() {
        if (extensionMap.isEmpty()) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(extensionMap.keySet());
    }
    public Extension getExtension(Character key) {
        return extensionMap.get(LocaleUtils.toLower(key));
    }
    public String getExtensionValue(Character key) {
        Extension ext = extensionMap.get(LocaleUtils.toLower(key));
        if (ext == null) {
            return null;
        }
        return ext.getValue();
    }
    public Set<String> getUnicodeLocaleAttributes() {
        Extension ext = extensionMap.get(UnicodeLocaleExtension.SINGLETON);
        if (ext == null) {
            return Collections.emptySet();
        }
        assert (ext instanceof UnicodeLocaleExtension);
        return ((UnicodeLocaleExtension)ext).getUnicodeLocaleAttributes();
    }
    public Set<String> getUnicodeLocaleKeys() {
        Extension ext = extensionMap.get(UnicodeLocaleExtension.SINGLETON);
        if (ext == null) {
            return Collections.emptySet();
        }
        assert (ext instanceof UnicodeLocaleExtension);
        return ((UnicodeLocaleExtension)ext).getUnicodeLocaleKeys();
    }
    public String getUnicodeLocaleType(String unicodeLocaleKey) {
        Extension ext = extensionMap.get(UnicodeLocaleExtension.SINGLETON);
        if (ext == null) {
            return null;
        }
        assert (ext instanceof UnicodeLocaleExtension);
        return ((UnicodeLocaleExtension)ext).getUnicodeLocaleType(LocaleUtils.toLowerString(unicodeLocaleKey));
    }
    public boolean isEmpty() {
        return extensionMap.isEmpty();
    }
    public static boolean isValidKey(char c) {
        return LanguageTag.isExtensionSingletonChar(c) || LanguageTag.isPrivateusePrefixChar(c);
    }
    public static boolean isValidUnicodeLocaleKey(String ukey) {
        return UnicodeLocaleExtension.isKey(ukey);
    }
    private static String toID(SortedMap<Character, Extension> map) {
        StringBuilder buf = new StringBuilder();
        Extension privuse = null;
        for (Entry<Character, Extension> entry : map.entrySet()) {
            char singleton = entry.getKey();
            Extension extension = entry.getValue();
            if (LanguageTag.isPrivateusePrefixChar(singleton)) {
                privuse = extension;
            } else {
                if (buf.length() > 0) {
                    buf.append(LanguageTag.SEP);
                }
                buf.append(extension);
            }
        }
        if (privuse != null) {
            if (buf.length() > 0) {
                buf.append(LanguageTag.SEP);
            }
            buf.append(privuse);
        }
        return buf.toString();
    }
    @Override
    public String toString() {
        return id;
    }
    public String getID() {
        return id;
    }
    @Override
    public int hashCode() {
        return id.hashCode();
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof LocaleExtensions)) {
            return false;
        }
        return id.equals(((LocaleExtensions)other).id);
    }
}

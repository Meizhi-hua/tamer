class UnixUserPrincipals {
    private static User createSpecial(String name) { return new User(-1, name); }
    static final User SPECIAL_OWNER = createSpecial("OWNER@");
    static final User SPECIAL_GROUP = createSpecial("GROUP@");
    static final User SPECIAL_EVERYONE = createSpecial("EVERYONE@");
    static class User implements UserPrincipal {
        private final int id;             
        private final boolean isGroup;
        private final String name;
        private User(int id, boolean isGroup, String name) {
            this.id = id;
            this.isGroup = isGroup;
            this.name = name;
        }
        User(int id, String name) {
            this(id, false, name);
        }
        int uid() {
            if (isGroup)
                throw new AssertionError();
            return id;
        }
        int gid() {
            if (isGroup)
                return id;
            throw new AssertionError();
        }
        boolean isSpecial() {
            return id == -1;
        }
        @Override
        public String getName() {
            return name;
        }
        @Override
        public String toString() {
            return name;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (!(obj instanceof User))
                return false;
            User other = (User)obj;
            if ((this.id != other.id) ||
                (this.isGroup != other.isGroup)) {
                return false;
            }
            if (this.id == -1 && other.id == -1)
                return this.name.equals(other.name);
            return true;
        }
        @Override
        public int hashCode() {
            return (id != -1) ? id : name.hashCode();
        }
    }
    static class Group extends User implements GroupPrincipal {
        Group(int id, String name) {
            super(id, true, name);
        }
    }
    static User fromUid(int uid) {
        String name = null;
        try {
            name = new String(getpwuid(uid));
        } catch (UnixException x) {
            name = Integer.toString(uid);
        }
        return new User(uid, name);
    }
    static Group fromGid(int gid) {
        String name = null;
        try {
            name = new String(getgrgid(gid));
        } catch (UnixException x) {
            name = Integer.toString(gid);
        }
        return new Group(gid, name);
    }
    private static int lookupName(String name, boolean isGroup)
        throws IOException
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("lookupUserInformation"));
        }
        int id = -1;
        try {
            id = (isGroup) ? getgrnam(name) : getpwnam(name);
        } catch (UnixException x) {
            throw new IOException(name + ": " + x.errorString());
        }
        if (id == -1) {
            try {
                id = Integer.parseInt(name);
            } catch (NumberFormatException ignore) {
                throw new UserPrincipalNotFoundException(name);
            }
        }
        return id;
    }
    static UserPrincipal lookupUser(String name) throws IOException {
        if (name.equals(SPECIAL_OWNER.getName()))
            return SPECIAL_OWNER;
        if (name.equals(SPECIAL_GROUP.getName()))
            return SPECIAL_GROUP;
        if (name.equals(SPECIAL_EVERYONE.getName()))
            return SPECIAL_EVERYONE;
        int uid = lookupName(name, false);
        return new User(uid, name);
    }
    static GroupPrincipal lookupGroup(String group)
        throws IOException
    {
        int gid = lookupName(group, true);
        return new Group(gid, group);
    }
}

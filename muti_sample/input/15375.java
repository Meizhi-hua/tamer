public class UserPrincipalNotFoundException
    extends IOException
{
    static final long serialVersionUID = -5369283889045833024L;
    private final String name;
    public UserPrincipalNotFoundException(String name) {
        super();
        this.name = name;
    }
    public String getName() {
        return name;
    }
}

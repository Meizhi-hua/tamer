class JDMTrapCommunity extends SimpleNode {
  protected String community= "";
  JDMTrapCommunity(int id) {
    super(id);
  }
  JDMTrapCommunity(Parser p, int id) {
    super(p, id);
  }
  public static Node jjtCreate(int id) {
      return new JDMTrapCommunity(id);
  }
  public static Node jjtCreate(Parser p, int id) {
      return new JDMTrapCommunity(p, id);
  }
  public String getCommunity() {
        return community;
  }
}

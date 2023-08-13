public final class ClientCallbackHandler extends TextCallbackHandler {
    private String username = "john";
    private String password = "test123";
    private boolean auto;
    public ClientCallbackHandler(boolean auto) {
        super();
        this.auto = auto;
    }
    public void handle(Callback[] callbacks) throws UnsupportedCallbackException,
    IOException {
        NameCallback ncb = null;
        PasswordCallback pcb = null;
        RealmChoiceCallback rccb = null;
        List namePw = new ArrayList(3);
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                if (auto) {
                    ((NameCallback)callbacks[i]).setName(username);
                } else {
                    namePw.add(callbacks[i]);
                }
            } else if (callbacks[i] instanceof PasswordCallback) {
                if (auto) {
                    ((PasswordCallback)callbacks[i]).setPassword(
                        password.toCharArray());
                } else {
                    namePw.add(callbacks[i]);
                }
            } else if (callbacks[i] instanceof RealmChoiceCallback) {
                RealmChoiceCallback rcb = (RealmChoiceCallback) callbacks[i];
                if (!auto) {
                    System.err.println(rcb.getPrompt());
                }
                String[] choices = rcb.getChoices();
                if (!auto) {
                    for (int j=0; j < choices.length; j++) {
                        System.err.println(j + ":" + choices[j]);
                    }
                }
                int selection;
                if (auto) {
                    selection = 0;
                } else {
                    System.err.print("Enter choice number: ");
                    String result = readLine();
                    if (result.equals("")) {
                        selection = rcb.getDefaultChoice();
                    } else {
                        selection = Integer.parseInt(result);
                    }
                }
                rcb.setSelectedIndex(selection);
            } else if (callbacks[i] instanceof RealmCallback) {
                RealmCallback rcb = (RealmCallback) callbacks[i];
                String realm = rcb.getDefaultText();
                if (auto) {
                    if (realm != null) {
                        rcb.setText(realm);
                    }
                } else {
                    if (realm == null) {
                        System.err.print(rcb.getPrompt());
                    } else {
                        System.err.print(rcb.getPrompt() + " [" + realm + "] ");
                    }
                    System.err.flush();
                    String result = readLine();
                    if (result.equals("")) {
                        result = realm;
                    }
                    rcb.setText(result);
                }
            } else {
                throw new UnsupportedCallbackException(callbacks[i]);
            }
        }
        if (namePw.size() > 0) {
            Callback[] np = new Callback[namePw.size()];
            namePw.toArray(np);
            super.handle(np);
        }
    }
    private String readLine() throws IOException {
        return new BufferedReader
            (new InputStreamReader(System.in)).readLine();
    }
}

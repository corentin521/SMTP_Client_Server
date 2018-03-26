package smtp_server;

public class User {
    private String name;
    private String password;

    public User(){

    }

    public User(String name, String motDePasse) {
        this.name = name;
        this.password = motDePasse;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }
}

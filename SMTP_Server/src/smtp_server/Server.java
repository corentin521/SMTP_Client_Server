package smtp_server;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Server {

    public static Server instance;

    private static final int FREE_PORT = 2500;
    private static final int HOTMAIL_PORT = 2501;
    private static final int GMAIL_PORT = 2502;
    private static final String FREE_DOMAIN = "free.fr";
    private static final String HOTMAIL_DOMAIN = "hotmail.fr";
    private static final String GMAIL_DOMAIN = "gmail.com";
    private List<ServeurDomaine> domaines;
    private List<User> users;

    public Server() {
        domaines = new ArrayList<>();
        users = loadUsers();
        launchEveryDomains();
    }

    private List<User> loadUsers() {
        users = new ArrayList<>();
        // Désérialisation des utilisateurs stockés dans le fichier users.json
        ObjectMapper objectMapper = new ObjectMapper();
        //File file = new File(System.getProperty("user.dir") + "/users.json");
        File file = new File("../TP_POP3_S8/POP3/users.json");
        try {
            users = objectMapper.readValue(file, new TypeReference<ArrayList<User>>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;

    }

    private void launchEveryDomains() {
        domaines.add(new ServeurDomaine(FREE_PORT, "Free", getUsersFromDomain(FREE_DOMAIN)));
        domaines.add(new ServeurDomaine(HOTMAIL_PORT, "Hotmail", getUsersFromDomain(HOTMAIL_DOMAIN)));
        domaines.add(new ServeurDomaine(GMAIL_PORT, "Gmail", getUsersFromDomain(GMAIL_DOMAIN)));
    }

    private List<User> getUsersFromDomain(String domain) {
        List<User> users = new ArrayList<>();

        for(User user : this.users){
            if(user.getName().split("@")[1].toLowerCase().equals(domain.toLowerCase()))
                users.add(user);
        }

        return users;
    }


    public static Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    public List<ServeurDomaine> getDomaines() {
        return domaines;
    }
}
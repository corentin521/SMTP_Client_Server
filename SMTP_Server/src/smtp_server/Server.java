package smtp_server;

import java.util.ArrayList;
import java.util.List;

public class Server {

    public static Server instance;

    private static final int FREE_PORT = 2500;
    private static final int HOTMAIL_PORT = 2501;
    private static final int GMAIL_PORT = 2502;
    private List<ServeurDomaine> domaines;

    public Server() {
        domaines = new ArrayList<>();
        launchEveryDomains();
    }

    private void launchEveryDomains() {
        domaines.add(new ServeurDomaine(FREE_PORT, "Free"));
        domaines.add(new ServeurDomaine(HOTMAIL_PORT, "Hotmail"));
        domaines.add(new ServeurDomaine(GMAIL_PORT, "Gmail"));
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
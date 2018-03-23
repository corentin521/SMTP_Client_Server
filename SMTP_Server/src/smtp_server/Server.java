package smtp_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

public class Server extends Observable implements Observer {

    public static Server instance;

    private static final int FREE_PORT = 2500;
    private static final int HOTMAIL_PORT = 2501;
    private static final int GMAIL_PORT = 2502;

    public Server() {
        launchEveryDomains();
    }

    private void launchEveryDomains() {
        ServeurDomaine.getInstance(FREE_PORT);
        ServeurDomaine.getInstance(HOTMAIL_PORT);
        ServeurDomaine.getInstance(GMAIL_PORT);
    }

    public static Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            setChanged();
            notifyObservers(arg);
        }
    }
}
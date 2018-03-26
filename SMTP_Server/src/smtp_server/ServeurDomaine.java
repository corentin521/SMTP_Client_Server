package smtp_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class ServeurDomaine extends Observable implements Observer {

    private int port;
    private ServerSocket serverDomaineSocket;
    private boolean isRunning = true;
    private String name;
    private List<User> users;

    public ServeurDomaine(int port, String name, List<User> users) {
        this.port = port;
        this.name = name;
        this.users = users;

        try {
            serverDomaineSocket = new ServerSocket(port);
            setChanged();
            System.out.println("ServeurDomaine lancé sur le port " + port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launchServer() {
        Thread t = new Thread(() -> {
            while (isRunning) {
                try {
                    notifyObservers("Serveur domaine " + name + " lancé sur le port " + port);
                    Socket client = serverDomaineSocket.accept();

                    System.out.println("[ServeurDomaine:" + port + "] Connexion client reçue !");
                    Communication communication = new Communication(client, this.users);
                    communication.addObserver(ServeurDomaine.this);
                    new Thread(communication).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverDomaineSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                serverDomaineSocket = null;
            }
        });

        t.start();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            setChanged();
            notifyObservers(arg);
        }
    }
}
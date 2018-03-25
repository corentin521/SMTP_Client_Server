package smtp_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Observable;
import java.util.Observer;

public class ServeurDomaine extends Observable implements Observer {

    private int port;
    private ServerSocket serverDomaineSocket;
    private boolean isRunning = true;
    private String name;

    public ServeurDomaine(int port, String name) {
        this.port = port;
        this.name = name;

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
                    Communication communication = new Communication(client);
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
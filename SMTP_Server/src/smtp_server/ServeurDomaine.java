package smtp_server;

import com.sun.corba.se.spi.activation.RepositoryPackage.ServerDef;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

public class ServeurDomaine extends Observable implements Observer {

    private static ServeurDomaine instance;
    private int port;
    private ServerSocket serverDomaineSocket;
    private boolean isRunning = true;

    private ServeurDomaine(int port) {
        this.instance = this;
        this.port = port;

        try {
            serverDomaineSocket = new ServerSocket(port);
            System.out.println("ServeurDomaine lancé sur le port " + port);
        }
        catch (UnknownHostException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        launchServer();
    }

    public static ServeurDomaine getInstance(int port) {
        if (instance == null)
            instance = new ServeurDomaine(port);

        return instance;
    }

    public void launchServer() {
        Thread t = new Thread(() -> {
            while (isRunning) {
                try {
                    Socket client =  serverDomaineSocket.accept();

                    System.out.println("[ServeurDomaine:" + port + "] Connexion client reçue !");
                    Communication communication = new Communication(client);
                    communication.addObserver(ServeurDomaine.this);
                    new Thread(communication).start();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverDomaineSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
                serverDomaineSocket = null;
            }
        });

        t.start();
    }

    public void close() {
        isRunning = false;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            getInstance(port).setChanged();
            getInstance(port).notifyObservers(arg);
        }
    }
}
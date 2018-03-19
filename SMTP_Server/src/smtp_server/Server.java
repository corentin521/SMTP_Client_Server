package smtp_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Observable;
import java.util.Observer;

public class Server extends Observable implements Observer {

    private static Server instance;
    private int port;
    private ServerSocket serverSocket;
    private boolean isRunning = true;


    public Server() {
        this(110);
    }

    public Server(int port) {
        this.port = port;

        try {
            serverSocket = new ServerSocket(port);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        launchServer();
    }

    public static Server getInstance() {
        if (instance == null)
            instance = new Server();

        return instance;
    }

    public void setPort(int port) {
        this.port = port;
        try {
            this.serverSocket.close();
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void launchServer() {
        Thread t = new Thread(() -> {
            while (isRunning) {
                try {
                    Socket client =  serverSocket.accept();

                    System.out.println("[Server] Connexion client reçue !");
                    Communication communication = new Communication(client);
                    communication.addObserver(Server.this);
                    new Thread(communication).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                serverSocket = null;
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
            getInstance().setChanged();
            getInstance().notifyObservers(arg);
        }
    }
}
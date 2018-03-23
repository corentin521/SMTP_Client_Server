package smtp_client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import static smtp_client.Enums.Response.LOGGED;

public class Client extends Observable implements Observer, Runnable {

    private enum State {
        WAITING_FOR_TCP_CONNECTION,
        CONNECTED
    }

    private Socket connection = null;
    private BufferedInputStream bufferedInputStream = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private State state = State.WAITING_FOR_TCP_CONNECTION;
    private String IPAddress;
    private int port;
    private boolean isRunning;
    private BufferedReader bufferedReader;

    public Client(String IPAddress, int port) throws Exception {
        this.IPAddress = IPAddress;
        this.port = port;
        this.isRunning = true;

        initCommunication();
    }

    private void initCommunication() throws IOException {
        connection = new Socket(IPAddress, port);

        bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
    }

    @Override
    public void run() {
        System.out.println("Thread running on Client");

        try {
            while (isRunning) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("[Client] Message reçu : " + line);
                    parseReceivedExpression(line);
                }
            }
        }
        catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            bufferedOutputStream.write(message.getBytes());
            System.out.println(message);
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseReceivedExpression(String expression) {
        String[] splittedExpression = expression.split(" ");

        if(splittedExpression.length > 0){
            String returnCode = splittedExpression[0];

            switch (returnCode){
                case "220":
                    handleValidatedTCPConnectionAnswer(splittedExpression);
            }
        }
    }

    private void handleValidatedTCPConnectionAnswer(String[] splittedExpression) {
        state = State.CONNECTED;
        setChanged();
        notifyObservers(LOGGED);
    }






    public void sendMail(String from, String to, String subject, String content) {
        try {
            // Récupération des différents noms de domaine
            String[] recipients = to.split(";");
            ArrayList<String> domains = new ArrayList<>();
            String currentDomain;
            for (String recipient : recipients) {
                currentDomain = recipient.split("@")[1];
                if (!domains.contains(currentDomain))
                    domains.add(currentDomain);
            }

            // Création des ClientDomaine pour chaque domaine des receveurs
            ArrayList<String> recipientsFromCurrentDomain = new ArrayList<>();
            ClientDomaine clientDomaine;
            for (String domain : domains) {
                for (String recipient : recipients) {
                    if (recipient.split("@")[1].equals(domain))
                        recipientsFromCurrentDomain.add(recipient);
                }

                clientDomaine = new ClientDomaine(this.IPAddress, this.port, from, recipientsFromCurrentDomain, subject, content);
                clientDomaine.addObserver(Client.this);
                new Thread(clientDomaine).start();

                recipientsFromCurrentDomain.clear();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }





    @Override
    public void update(Observable o, Object arg) {

    }
}
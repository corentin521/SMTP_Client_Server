package smtp_client;

import java.io.*;
import java.net.Socket;
import java.util.Observable;

import static smtp_client.Enums.Response.LOGGED;

public class Client extends Observable implements Runnable {

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
        // TODO : Ouvrir un ClientDomaine par domaine et lui fournir les mails le concernant
    }

}
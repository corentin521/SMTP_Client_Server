package smtp_client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;

public class ClientDomaine extends Observable implements Runnable {

    private enum State {
        CONNECTED,
        IDENTIFIED,
        WAITING_FOR_RECIPIENT,
        VALIDATED_RECIPIENT,
        SENDING,
        SENDING_DATA
    }

    private Socket connection = null;
    private BufferedInputStream bufferedInputStream = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private State state = State.CONNECTED;
    private String IPAddress;
    private int port;
    private boolean isRunning;
    private BufferedReader bufferedReader;

    public ClientDomaine(String IPAddress, int port, String from, ArrayList<String> recipients, String subject, String content) throws Exception {
        this.IPAddress = IPAddress;
        this.port = port;
        this.isRunning = true;

        initCommunication();
    }

    private void initCommunication() throws IOException {
        connection = new Socket(IPAddress, port);

        bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        // TODO : envoyer les EHLO puis les RCPT puis les DATA
    }

    @Override
    public void run() {
        System.out.println("Thread running on ClientDomaine");

        try {
            while (isRunning) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("[ClientDomaine] Message reçu : " + line);
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
                case "250":
                    handlePositiveAnswer(splittedExpression);
            }
        }
    }

    private void handlePositiveAnswer(String[] splittedExpression) {
        switch(state){
            case CONNECTED:
                break;
            case IDENTIFIED:
                break;
            case WAITING_FOR_RECIPIENT:
                break;
            case VALIDATED_RECIPIENT:
                break;
            case SENDING:
                break;
            case SENDING_DATA:
                break;
        }
    }

    // EHLO
    public void ehlo(String username) {
        String message = "EHLO " + username + "\n";
        sendMessage(message);
    }

    //QUIT
    public void quit() {
        sendMessage("QUIT ");
    }

    public void stop() {

        this.isRunning = false;
        try {
            this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
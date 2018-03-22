package smtp_client;

import java.io.*;
import java.net.Socket;
import java.util.Observable;

import static smtp_client.Enums.Response.LOGGED;

public class Client extends Observable implements Runnable {

    private enum State {
        WAITING_FOR_TCP_CONNECTIION,
        CONNECTED,
        WAITING_FOR_RECIPIENT,
        SENDING,
        VALIDATED_RECIPIENT
    }

    private Socket connection = null;
    private BufferedInputStream bufferedInputStream = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private State state = State.WAITING_FOR_TCP_CONNECTIION;
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
        System.out.println("Thread running on pop3_client.Client");

        try {
            while (isRunning) {

                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    parseReceivedExpression(line);
                }
            }
        } catch (Exception ex) {
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
                case "250": handlePositiveAnswer(splittedExpression);
            }
        }
    }

    private void handlePositiveAnswer(String[] splittedExpression) {
        switch(state){
            case WAITING_FOR_TCP_CONNECTIION:
                if(splittedExpression.length > 1 && splittedExpression[1].equals("bienvenue")){
                    state = State.CONNECTED;
                    setChanged();
                    notifyObservers(LOGGED);
                }

                break;
            case CONNECTED:
                break;
            case WAITING_FOR_RECIPIENT:
                break;
            case SENDING:
                break;
            case VALIDATED_RECIPIENT:
                break;
        }
    }

    // EHLO
    public void validateLoginDetails(String username) {
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
package smtp_client;

import java.io.*;
import java.net.Socket;
import java.util.*;

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

    private ArrayList<String> recipients;
    private String subject;
    private String content;
    private String from;

    public ClientDomaine(String IPAddress, int port, String from, ArrayList<String> recipients, String subject, String content) throws Exception {
        this.IPAddress = IPAddress;
        this.port = port;
        this.isRunning = true;

        this.recipients = recipients;
        this.subject = subject;
        this.content = content;
        this.from = from;

        initCommunication();
    }

    private void initCommunication() throws IOException {
        connection = new Socket(IPAddress, port);

        bufferedOutputStream = new BufferedOutputStream(connection.getOutputStream());
        bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));

        ehlo(from);
    }

    @Override
    public void run() {
        System.out.println("Thread running on ClientDomaine"+port);

        try {
            while (isRunning) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    System.out.println("[ClientDomaine:"+port+"] Message reçu : " + line);
                    parseReceivedExpression(line);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            bufferedOutputStream.write(message.getBytes());
            System.out.println("[ClientDomaine:" + port + "] Message envoyé : " + message);
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
                    break;
                case "354":
                    handlePositiveAnswer(splittedExpression);
                    break;
                case "550":
                    handleNegativeAnswer(splittedExpression);
                    break;
            }
        }
    }

    private void handleNegativeAnswer(String[] splittedExpression) {
        switch(state){
            case WAITING_FOR_RECIPIENT:
                if(recipients.isEmpty()) {
                    quit();
                    stop();
                }
                else {
                    rcpt();
                }
                break;
            case VALIDATED_RECIPIENT:
                if(recipients.isEmpty()) {
                    state = State.SENDING;
                    data();
                }
                else {
                    rcpt();
                }
                break;
            case SENDING:
                break;
            case SENDING_DATA:
                break;
        }
    }

    private void handlePositiveAnswer(String[] splittedExpression) {
        switch(state){
            case CONNECTED:
                state = State.IDENTIFIED;
                mailFrom(from);
                break;
            case IDENTIFIED:
                state = State.WAITING_FOR_RECIPIENT;
                rcpt();
                break;
            case WAITING_FOR_RECIPIENT:
                if(recipients.isEmpty()) {
                    state = State.SENDING;
                    data();
                }
                else {
                    state = State.VALIDATED_RECIPIENT;
                    rcpt();
                }
                break;
            case VALIDATED_RECIPIENT:
                if(recipients.isEmpty()) {
                    state = State.SENDING;
                    data();
                }
                else {
                    rcpt();
                }
                break;
            case SENDING:
                state = State.SENDING_DATA;
                sendMail();
                break;
            case SENDING_DATA:
                quit();
                stop();
                break;
        }
    }

    // EHLO
    public void ehlo(String username) {
        String message = "EHLO " + username + "\n";
        sendMessage(message);
    }

    //MAIL FROM
    public void mailFrom(String from)
    {
        String message = "MAIL FROM: " + from + "\n";
        sendMessage(message);
    }

    //RCPT
    public void rcpt()
    {
        String recipient = recipients.remove(0);
        String message = "RCPT TO: " + recipient + "\n";
        sendMessage(message);
    }

    //DATA
    public void data()
    {
        sendMessage("DATA \n");
    }


    // Envoi du mail
    public void sendMail()
    {
        // TODO : le mail doit s'envoyer autant de fois qu'il y a de destinataire ! Dans le fichier .txt de réception faudra qu'il y ait qu'un seul destinataire dans le "To:"
        String message =
                //"From: " + from + "\n" +
                //        "To: " + recipients + "\n" +
                        "Subject: " + subject + "\n" +
                        "Date: " + new Date() + "\n" +
                        "Message-ID: " + getRandomID() + "\n" +
                        content + "\n" +
                        ".\n";
        sendMessage(message);
    }

    //QUIT
    public void quit() {
        sendMessage("QUIT \n");
    }

    public void stop() {

        this.isRunning = false;
        try {
            this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRandomID () {
        String IDChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
        StringBuilder generatedID = new StringBuilder();
        Random random = new Random();
        int index;

        while (generatedID.length() < 20) {
            index = (int) (random.nextFloat() * IDChars.length());
            generatedID.append(IDChars.charAt(index));
        }

        return generatedID.toString();
    }
}
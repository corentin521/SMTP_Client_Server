package smtp_server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Communication extends Observable implements Runnable {

    private enum Command {
        EHLO,
        MAIL,
        RCPT,
        RST,
        DATA,
        QUIT,
        NONE
    }

    private enum State {
        CONNECTED,
        IDENTIFIED,
        WAITING_FOR_RECIPIENT,
        VALIDATED_RECIPIENT,
        SENDING,
        SENDING_DATA
    }


    private Socket socket;
    private boolean isRunning;
    private Command currentCommand;
    private BufferedOutputStream bufferedOutputStream;
    private String userLogin = "";
    private BufferedReader bufferedReader;

    private State state = State.CONNECTED;

    private List<String> validRecipients;


    public Communication(Socket socket) {
        this.socket = socket;
        this.isRunning = true;
        currentCommand = Command.NONE;
        validRecipients = new ArrayList<>();

        try {
            bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        sendReadyMessage();

        try {
            while (isRunning) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    parseReceivedExpression(line);
                }
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void parseReceivedExpression(String expression) throws IOException {
        if(state == State.SENDING) {
            System.out.println("SENDING : " + expression + " / expression.equals('.') ? " + expression.equals("."));
        }
        else {
            String[] splittedCommand = expression.split(" ");
            Command currentCommand = getCommandFromEnum(splittedCommand[0]);

            switch (currentCommand){
                case EHLO:
                    sendMessage("250-smtp." + socket.getInetAddress().toString() + ":" + socket.getPort() + "\n" +
                                "250-PIPELINING\n" +
                                "250 8BITMIME");
                    state = State.IDENTIFIED;

                    setChanged();
                    notifyObservers("Établissement d'une connection TCP avec" + socket.getInetAddress().toString() + " (port " + socket.getPort() + ")");
                    break;
                case MAIL:
                    if(state == State.IDENTIFIED) {
                        sendMessage("250 Sender ok");
                        state = State.WAITING_FOR_RECIPIENT;
                    }
                    else {
                        sendMessage("550");
                    }
                    break;
                case RCPT:
                    if(state == State.WAITING_FOR_RECIPIENT || state == State.VALIDATED_RECIPIENT) {
                        if(recipientIsValid(splittedCommand[2])) {
                            sendMessage("250 Recipient ok");
                            validRecipients.add(splittedCommand[2]);

                            if(state == State.WAITING_FOR_RECIPIENT)
                                state = State.VALIDATED_RECIPIENT;
                        }
                        else {
                            sendMessage("550 Invalid user");
                        }
                    }
                    else {
                        sendMessage("550");
                    }
                    break;
                case RST:
                    break;
                case DATA:
                    if(state == State.VALIDATED_RECIPIENT) {
                        sendMessage("354 Send message content; end with <CRLF>.<CRLF>");
                        state = State.SENDING;
                    }
                    else {
                        sendMessage("550");
                    }
                    break;
                case QUIT:
                    sendMessage("221 Bye");
                    setChanged();
                    notifyObservers("Déconnexion de " + userLogin);

                    closeConnection();
                    break;
            }
        }
    }

    private Command getCommandFromEnum(String command) {
        switch (command) {
            case "EHLO":
                return Command.EHLO;
            case "MAIL":
                return Command.MAIL;
            case "RCPT":
                return Command.RCPT;
            case "RST":
                return Command.RST;
            case "DATA":
                return Command.DATA;
            case "QUIT":
                return Command.QUIT;
            default:
                return Command.NONE;
        }
    }

    private void sendReadyMessage() {
        String message = "220 smtp." + socket.getInetAddress().toString() + ":" + socket.getPort() + " SMTP Ready";
        setChanged();
        notifyObservers("Établissement d'une connection TCP avec" + socket.getInetAddress().toString() + " (port " + socket.getPort() + ")");
        try {
            sendMessage(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean recipientIsValid(String recipient)
    {
        return true;
    }

    private void sendMessage(String message) throws IOException {
        message += "\n";
        bufferedOutputStream.write(message.getBytes("UTF-8"));
        System.out.println("[Communication:"+socket.getPort()+"] Message envoyé : " + message);
        try {
            bufferedOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeConnection()
    {
        isRunning = false;
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package smtp_server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.List;
import java.util.Observable;

public class Communication extends Observable implements Runnable {

    private enum Command {
        EHLO,
        MAIL,
        RCPT,
        RSET,
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
            System.err.println(ex.getMessage());
        }
    }

    private void parseReceivedExpression(String expression) throws IOException {
        if(state == State.SENDING)
        {

        }
        else
        {
            String[] splittedCommand = expression.split(" ");
            Command currentCommand = getCommandFromEnum(splittedCommand[0]);

            switch (currentCommand){
                case EHLO:
                    ehlo();
                    state = State.IDENTIFIED;
                case MAIL:
                    if(state == State.IDENTIFIED)
                    {
                        sendMessage("250 OK ");
                        state = State.WAITING_FOR_RECIPIENT;
                    }
                    else
                    {
                        sendMessage("550 ");
                    }
                    break;
                case RCPT:
                    if(state == State.WAITING_FOR_RECIPIENT)
                    {
                        if(recipientIsValid(splittedCommand[2]))
                        {
                            sendMessage("250 OK ");
                            validRecipients.add(splittedCommand[2]);
                            state = State.VALIDATED_RECIPIENT;
                        }
                        else
                        {
                            sendMessage("550 invalid user");
                        }
                    }
                    else if(state == State.VALIDATED_RECIPIENT)
                    {
                        if(recipientIsValid(splittedCommand[2]))
                        {
                            sendMessage("250 OK ");
                            validRecipients.add(splittedCommand[2]);
                        }
                        else
                        {
                            sendMessage("550 invalid user");
                        }
                    }
                    break;
                case RSET:
                    break;
                case DATA:
                    if(state == State.VALIDATED_RECIPIENT)
                    {
                        sendMessage("354 Send message content; end with <CRLF>.<CRLF>");
                        state = State.SENDING;
                    }
                    else
                    {
                        sendMessage("550 ");
                    }
                    break;
                case QUIT:
                    quit();
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
            case "RSET":
                return Command.RSET;
            case "DATA":
                return Command.DATA;
            case "QUIT":
                return Command.QUIT;
            default:
                return Command.NONE;
        }
    }

    private void sendReadyMessage() {
        String message = "220 " + socket.getInetAddress().toString() + ":" + socket.getPort() + " SMTP Ready";
        setChanged();
        notifyObservers("Établissement d'une connection TCP avec" + socket.getInetAddress().toString() + " (port " + socket.getPort() + ")");
        try {
            sendMessage(message);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ehlo(){
        String message = "250 SMTP server ready";
        setChanged();
        notifyObservers("Établissement d'une connection TCP avec" + socket.getInetAddress().toString() + " (port " + socket.getPort() + ")");
        try {
            sendMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void quit()
    {
        try {
            sendMessage("+OK Logging out");
            setChanged();
            notifyObservers("Déconnexion de " + userLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeConnection();
    }

    private boolean recipientIsValid(String recipient)
    {
        return true;
    }

    private void sendMessage(String message) throws IOException {
        message += "\n";
        bufferedOutputStream.write(message.getBytes("UTF-8"));
        System.out.println("[ServeurDomaine] Message envoyé : " + message);
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

package smtp_server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
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

    private Socket socket;
    private boolean isRunning;
    private Command currentCommand;
    private BufferedOutputStream bufferedOutputStream;
    private String userLogin = "";
    private BufferedReader bufferedReader;


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
        String[] splittedCommand = expression.split(" ");
        Command currentCommand = getCommandFromEnum(splittedCommand[0]);

        switch (currentCommand){
            case EHLO:
                ehlo();
            case MAIL:
                break;
            case RCPT:
                break;
            case RSET:
                break;
            case DATA:
                break;
            case QUIT:
                break;
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
            sendMessage("+OK Logging out\n");
            setChanged();
            notifyObservers("Déconnexion de " + userLogin);
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeConnection();
    }

    private void sendMessage(String message) throws IOException {
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

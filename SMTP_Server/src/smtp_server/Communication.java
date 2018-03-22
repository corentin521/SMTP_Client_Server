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
        CHECK,
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
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

    private void parseReceivedExpression(String expression) throws IOException {
        String[] splittedCommand = expression.split(" ");
        Command currentCommand = getCommandFromEnum(splittedCommand[0]);

        switch (currentCommand){
            case EHLO: sendMessage("250 bienvenue\n");
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
            case CHECK:
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
            case "CHECK":
                return Command.CHECK;
            default:
                return Command.NONE;
        }
    }

    private int getCommandParameterCount(Command command) {
        switch (command) {
            case EHLO:
            case MAIL:
            case RCPT:
                return 1;
            case CHECK:
            case QUIT:
            case RSET:
            case DATA:
                return 0;
            default:
                return -1;
        }
    }

    private void executeCommand() {
        switch (currentCommand) {
            case EHLO:
                break;
            case MAIL:
                break;
            case RCPT:
                break;
            case RSET:
                break;
            case DATA:
                break;
            case QUIT:
                quit();
                break;
            case CHECK:
                sendWelcomeMessage();
                break;
            default:
                break;
        }
    }

    private void sendWelcomeMessage(){
        String message = "+OK POP3 server ready";
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
        System.out.println(message);
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

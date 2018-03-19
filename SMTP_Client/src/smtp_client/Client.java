package smtp_client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.*;
import java.net.Socket;
import java.util.Observable;

public class Client extends Observable implements Runnable {

    private enum State {
        WAITING_FOR_TCP_CONNECTIION,
        AUTHORIZATION,
        TRANSACTION,
        UPDATE
    }

    private Socket connection = null;
    private BufferedInputStream bufferedInputStream = null;
    private BufferedOutputStream bufferedOutputStream = null;
    private State state = State.WAITING_FOR_TCP_CONNECTIION;
    private String IPAddress;
    private int port;
    private boolean isRunning;
    private boolean serverIsReachable = false;
    private BufferedReader bufferedReader;

    private boolean retrieveIsReceiving = false;
    private boolean listIsReceiving = false;
    private int listMessagesTreated = 0;
    private Mail currentRetrievedMail;
    ObservableSet<String> data = FXCollections.observableSet();

    public Client(String IPAddress, int port) throws Exception {
        this.IPAddress = IPAddress;
        this.port = port;
        this.isRunning = true;

        initCommunication();

        currentRetrievedMail = new Mail();
    }

    private void initCommunication() throws IOException {
        connection = new Socket(IPAddress, port);


        state = State.AUTHORIZATION;
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
        try {
            if (retrieveIsReceiving) {
                retrieveIsReceiving = !expression.equals(".");

                if (!retrieveIsReceiving) {
                    setChanged();
                    notifyObservers(currentRetrievedMail);

                    currentRetrievedMail = new Mail();
                } else {
                    if (expression.toLowerCase().contains("from:"))
                        currentRetrievedMail.setFrom(expression.substring(6));
                    else if (expression.toLowerCase().contains("to:"))
                        currentRetrievedMail.setTo(expression.substring(4));
                    else if (expression.toLowerCase().contains("date:"))
                        currentRetrievedMail.setDate(expression.substring(6));
                    else if (expression.toLowerCase().contains("subject:"))
                        currentRetrievedMail.setSubject(expression.substring(9));
                    else if (expression.toLowerCase().contains("message-id:"))
                        currentRetrievedMail.setId(expression.substring(12));
                    else
                        currentRetrievedMail.setContent(expression + "\n");
                }
            }
            if (listIsReceiving) {
                listIsReceiving = !expression.equals(".");

                if (!listIsReceiving) {
                    setChanged();
                    notifyObservers(data);
                } else {
                    Platform.runLater(() -> {
                                data.add(expression.split(" ")[0]);
                            }
                    );

                }
            } else {
                switch (expression) {
                    case "+OK logged":
                        state = State.TRANSACTION;
                        setChanged();
                        notifyObservers(Enums.Response.LOGGED);
                        break;
                    case "-ERR unknown username":
                        setChanged();
                        notifyObservers(Enums.Response.UNKNOWN_USER);
                        break;
                    case "-ERR wrong password":
                        setChanged();
                        notifyObservers(Enums.Response.WRONG_PASSWORD);
                        break;
                    case "+OK POP3 server ready":
                        serverIsReachable = true;
                        state = State.AUTHORIZATION;
                        break;
                    case "+OK Logging out":
                        state = State.AUTHORIZATION;
                        stop();
                        setChanged();
                        notifyObservers(Enums.Response.LOGGING_OUT);
                        break;
                    default:
                        if (expression.matches("\\+OK \\d* \\d*")) {
                            String[] results = expression.split(" ");
                            notifyObservers(results[1]);
                        } else if (expression.matches("\\+OK \\d* octets .*")) {
                            retrieveIsReceiving = true;
                        } else if (expression.matches("\\+OK \\d* messages \\(\\d* octets\\) .*")) {
                            listIsReceiving = true;
                        } else {
                            System.out.println("réponse non traitée");
                        }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void checkServerAvailablity() {
        String message = "CHECK ";
        sendMessage(message);

        try {
            while (!serverIsReachable) {
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    parseReceivedExpression(line);
                }
            }
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
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

    public void saveClientMail(String username, Mail mail) {
        try {
            //String pathToMailFolder = System.getProperty("user.dir") + "/usersMails/" + username + ".txt";
            String pathToMailFolder = "POP3_Client/usersMails/" + username + ".txt";
            File f = new File(pathToMailFolder);
            if (!f.exists()) {
                f.createNewFile();
            }

            BufferedReader br = new BufferedReader(new FileReader(pathToMailFolder));
            BufferedWriter bw = new BufferedWriter(new FileWriter(pathToMailFolder, true));

            String line = "";
            boolean messageFound = false;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().contains("message-id:") && line.split(" ")[1].equals(mail.getId())) {
                    messageFound = true;
                    break;
                }
            }

            if (!messageFound) {
                bw.append("From: " + mail.getFrom());
                bw.newLine();
                bw.append("To: " + mail.getTo());
                bw.newLine();
                bw.append("Subject: " + mail.getSubject());
                bw.newLine();
                bw.append("Date: " + mail.getDate());
                bw.newLine();
                bw.append("Message-ID: " + mail.getId());
                bw.newLine();
                bw.newLine();

                for (String contentPart : mail.getContent().split("\n")) {
                    bw.append(contentPart);
                    bw.newLine();
                }

                bw.append(".");
                bw.newLine();

                bw.flush();
            }

            bw.close();
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

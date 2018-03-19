package smtp_server;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerMain extends Application implements Observer {

    private static Server server;
    private ObservableList<Node> logLinesList;
    private ScrollPane sp;

    public static void main(String args[]) throws IOException {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws UnknownHostException {

//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();

        initUserInterface(primaryStage);
    }

    private void initUserInterface(Stage primaryStage) throws UnknownHostException {
        primaryStage.setTitle("Serveur SMTTP");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 10, 10, 10));

        Label serverPortLabel = new Label("Port :");
        serverPortLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        grid.add(serverPortLabel, 0, 0);

        TextField serverPortField = new TextField();
        serverPortField.setPrefWidth(50);
        // Default value
        serverPortField.setText("1230");
        grid.add(serverPortField, 1, 0);

        // Port validation
        serverPortField.textProperty().addListener((observable, oldValue, newValue) -> {
            // Remove non numeric character
            if (!newValue.matches("\\d*")) {
                serverPortField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            // Set the string length to 5 because the max port is 65536
            if (!newValue.equals("") && !oldValue.equals("")
                    && Integer.valueOf(newValue) > Integer.valueOf(oldValue)
                    && serverPortField.getText().length() >= 5) {
                serverPortField.setText(serverPortField.getText().substring(0, 5));
            }
        });

        Button startServerButton = new Button("Démarrer");
        startServerButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        grid.add(startServerButton, 2, 0);

        Label IPAddresstLabel = new Label("Adresse IP :");
        IPAddresstLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        grid.add(IPAddresstLabel, 3, 0);

        Label IPAddress = new Label(InetAddress.getLocalHost().getHostAddress());
        IPAddress.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        IPAddress.setTextFill(Color.GREEN);
        IPAddress.setMinWidth(90);
        grid.add(IPAddress, 4, 0);

        Button clearLogButton = new Button("Vider le log");
        clearLogButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        grid.add(clearLogButton, 5, 0);

        FlowPane fp = new FlowPane();
        this.logLinesList = fp.getChildren();
        sp = new ScrollPane();
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        sp.setContent(fp);
        grid.add(sp, 0, 2, 7, 40);

        writeInformationInLog("Veuillez choisir un port et démarrer le serveur.");

        startServerButton.setOnAction(e -> {
            try {
                if (serverPortField.getText().equals(""))
                    writeErrorInLog("Une erreur est survenue. Le port doit être renseigné.");
                else {
                    int port = Integer.valueOf(serverPortField.getText());

                    if (startServerButton.getText().equals("Démarrer")) {
                        server = Server.getInstance();

                        if (!serverPortField.getText().isEmpty())
                            server.setPort(Integer.valueOf(serverPortField.getText()));
                        server.addObserver(ServerMain.this);
                        startServerButton.setText("Arrêter");
                        writeSuccessInLog("Le serveur a démarré avec succès sur le port " + port + " de l'hôte " + InetAddress.getLocalHost());

                    } else {
                        server.close();
                        startServerButton.setText("Démarrer");
                        writeSuccessInLog("Le serveur a été arrêté avec succès.");
                    }

                }
            } catch (Exception ex) {
                writeErrorInLog("Une erreur est survenue. Le port n'est pas disponible. Veuillez en choisir un autre.");
                Logger.getLogger(ServerMain.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        clearLogButton.setOnAction(e -> {
            logLinesList.clear();
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        });

        Scene scene = new Scene(grid, 540, 450);

        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() {
        System.exit(0);
    }

    private void writeInformationInLog(String message) {
        writeInLog(message, Color.BLACK);
    }

    private void writeSuccessInLog(String message) {
        writeInLog(message, Color.GREEN);
    }

    private void writeWarningInLog(String message) {
        writeInLog(message, Color.ORANGE);
    }

    private void writeErrorInLog(String message) {
        writeInLog(message, Color.FIREBRICK);
    }

    private void writeInLog(String message, Color color) {

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime currentDate = LocalDateTime.now();

        String string = dtf.format(currentDate).toString() + " : " + message;
        Text finalMessage = new Text(string);

        finalMessage.setWrappingWidth(460);
        finalMessage.setFill(color);
        this.logLinesList.add(finalMessage);
        if (sp.getVbarPolicy() == ScrollPane.ScrollBarPolicy.NEVER)
            sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String) {
            Platform.runLater(() -> {
                    String message = (String) arg;
                    writeInformationInLog(message);
                }
            );
        }
    }
}
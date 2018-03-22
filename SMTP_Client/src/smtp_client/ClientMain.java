package smtp_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.ConnectException;
import java.util.Observable;
import java.util.Observer;

public class ClientMain extends Application implements Observer {

    private String userName;
    private Client client;

    // UI
    private Stage primaryStage;
    private Scene loginScene;
    private Scene mailFormScene;
    private TextField userNameTextField;
    private Text errorField;
    private TextField fromTextField;
    private TextField dateTextField;
    private TextField subjectTextField;
    private TextArea contentTextField;

    public static void main(String args[]){
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
//        primaryStage.setTitle("Hello World");
//        primaryStage.setScene(new Scene(root, 300, 275));
//        primaryStage.show();
        this.primaryStage = primaryStage;
        initUserInterface();
    }

    private void initUserInterface() {
        primaryStage.setTitle("Client SMTP");

        createLoginInterface();
        createMailboxInterface();

        primaryStage.setScene(loginScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void createLoginInterface() {
        GridPane loginGrid = new GridPane();
        loginGrid.setAlignment(Pos.CENTER);
        loginGrid.setHgap(10);
        loginGrid.setVgap(10);
        loginGrid.setPadding(new Insets(25, 25, 25, 25));

        Text loginSceneTitle = new Text("Connexion");
        loginSceneTitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        loginGrid.add(loginSceneTitle, 0, 0, 2, 1);

        Label userNameLabel = new Label("Nom :");
        loginGrid.add(userNameLabel, 0, 1);

        userNameTextField = new TextField();
        userNameTextField.setText("arthur2.dupont@free.fr");
        loginGrid.add(userNameTextField, 1, 1);

        Button signInButton = new Button("Se connecter");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(signInButton);
        loginGrid.add(hbBtn, 0, 2);

        errorField = new Text();
        errorField.setFill(Color.FIREBRICK);
        loginGrid.add(errorField, 1, 4);
        signInButton.setOnAction(e -> {
            if (userNameTextField.getText().equals(""))
                errorField.setText("Le nom d'utilisateur doit être renseigné.");
            else {
                if(client == null)
                    createClient();
                userName = userNameTextField.getText();
                login();
            }
        });

        userNameTextField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                signInButton.fire();
            }
        });

        loginScene = new Scene(loginGrid, 500, 450);
    }

    private void createMailboxInterface(){
        GridPane mailboxGrid = new GridPane();
        mailboxGrid.setHgap(10);
        mailboxGrid.setVgap(10);
        mailboxGrid.setPadding(new Insets(10, 10, 10, 10));

        Button logOutButton = new Button("Se déconnecter");
        logOutButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        mailboxGrid.add(logOutButton, 38, 0, 8, 1);
        
        Label fromLabel = new Label("De :");
        mailboxGrid.add(fromLabel, 1, 2);
        fromTextField = new TextField();
        fromTextField.setDisable(true);
        mailboxGrid.add(fromTextField, 2, 2, 45, 1);
        
        Label dateLabel = new Label("Date :");
        mailboxGrid.add(dateLabel, 1, 3);
        dateTextField = new TextField();
        mailboxGrid.add(dateTextField, 2, 3, 45, 1);
        
        Label subjectLabel = new Label("Objet :");
        mailboxGrid.add(subjectLabel, 1, 4);
        subjectTextField = new TextField();
        mailboxGrid.add(subjectTextField, 2, 4, 45, 1);

        Label contentLabel = new Label("Contenu :");
        mailboxGrid.add(contentLabel, 1, 5);
        contentTextField = new TextArea();
        mailboxGrid.add(contentTextField, 1, 6, 45, 18);

        Button sendMailButton = new Button("Envoyer");
        sendMailButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        mailboxGrid.add(sendMailButton, 22, 25, 8, 1);

        logOutButton.setOnAction(e -> {
            userNameTextField.setText("");
            errorField.setText("");
            client.quit();
        });

        logOutButton.setOnAction(e -> {
           //TODO Envoyer le mail
        });

        mailFormScene = new Scene(mailboxGrid, 650, 450);
    }

    private void createClient() {
        Thread clientThread;
        try{
            client = new Client("localhost", 1230);
            client.addObserver(ClientMain.this);
            clientThread = new Thread(client);
            clientThread.start();
        }
        catch (Exception e) {
            if(e instanceof ConnectException)
                errorField.setText("Une erreur réseau est survenue.");
        }
    }

    private void login() {
        try{
            client.validateLoginDetails(userName);
        }
        catch (Exception e) {
            if(e instanceof ConnectException)
                errorField.setText("Une erreur est survenue.");
        }
    }

   @Override
   public void update(Observable o, Object arg) {
       if(arg instanceof Enums.Response){
           Enums.Response response = (Enums.Response)arg;

           switch (response){
                case LOGGED:
                    setCurrentScene(mailFormScene);
                    fromTextField.setText(userName);
                    break;
                case UNKNOWN_USER:
                    errorField.setText("Utilisateur inconnu.");
                    break;
                case WRONG_PASSWORD:
                    errorField.setText("Mot de passe incorrect");
                    break;
               case LOGGING_OUT:
                   setCurrentScene(loginScene);
                   client = null;
                   break;
           }
       }
   }

   private void setCurrentScene(Scene scene){
       Platform.runLater(() -> {
           primaryStage.setScene(scene);
               }
       );
   }
}
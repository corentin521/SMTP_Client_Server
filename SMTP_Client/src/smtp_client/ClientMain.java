package smtp_client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
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
    private Scene mailboxScene;
    private TextField userNameTextField;
    private Text errorField;
    private ListView<String> listView;
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

        Label IPAddresstLabel = new Label();
        IPAddresstLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 12));
        mailboxGrid.add(IPAddresstLabel, 0, 0);

        Button refreshButton = new Button("Actualiser");
        refreshButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        mailboxGrid.add(refreshButton, 0, 0, 6, 1);


        Button logOutButton = new Button("Se déconnecter");
        logOutButton.setFont(Font.font("Tahoma", FontWeight.NORMAL, 13));
        mailboxGrid.add(logOutButton, 8, 0, 6, 1);
        
        Label fromLabel = new Label("De :");
        mailboxGrid.add(fromLabel, 11, 2);
        fromTextField = new TextField();
        mailboxGrid.add(fromTextField, 12, 2, 34, 1);
        
        Label dateLabel = new Label("Date :");
        mailboxGrid.add(dateLabel, 11, 3);
        dateTextField = new TextField();
        mailboxGrid.add(dateTextField, 12, 3, 34, 1);
        
        Label subjectLabel = new Label("Objet :");
        mailboxGrid.add(subjectLabel, 11, 4);
        subjectTextField = new TextField();
        mailboxGrid.add(subjectTextField, 12, 4, 34, 1);

        Label contentLabel = new Label("Contenu :");
        mailboxGrid.add(contentLabel, 11, 5);
        contentTextField = new TextArea();
        mailboxGrid.add(contentTextField, 11, 6, 35, 18);

        listView = new ListView<>();
        listView.setPrefSize(200, 200);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (ObservableValue<? extends String> ov, String old_val, String new_val) -> {
                    //mailClicked(new_val);
                });
        
        StackPane root = new StackPane();
        root.getChildren().add(listView);
        mailboxGrid.add(root, 0, 2, 10, 25);

        logOutButton.setOnAction(e -> {
            userNameTextField.setText("");
            errorField.setText("");
            client.quit();
        });

        refreshButton.setOnAction(e -> {
        });

        mailboxScene = new Scene(mailboxGrid, 650, 450);
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
                    setCurrentScene(mailboxScene);
                    break;
                case UNKNOWN_USER:
                    errorField.setText("Utilisateur inconnu.");
                    break;
                case WRONG_PASSWORD:
                    errorField.setText("Mot de passe incorrect");
                    break;
               case LOGGING_OUT:
                   setCurrentScene(loginScene);
                   listView.getSelectionModel().clearSelection();
                   //listView.getFocusModel().isFocused()
                   client = null;
                   break;
           }
       }
       else if (arg instanceof Mail) {
            Mail mail = (Mail) arg;
            
            client.saveClientMail(userName, mail);
            
            fromTextField.setText(mail.getFrom());
            fromTextField.setDisable(false);
            dateTextField.setText(mail.getDate());
            dateTextField.setDisable(false);
            subjectTextField.setText(mail.getSubject());
            subjectTextField.setDisable(false);
            contentTextField.setText(mail.getContent());
            contentTextField.setDisable(false);
       }
       else if (arg instanceof ObservableSet) {
           Platform.runLater(() -> {
               ObservableSet<String> mailSet = (ObservableSet<String>) arg;
                    listView.setItems(FXCollections.observableArrayList(mailSet));
               }
           );

       }
       else
       {
           ObservableList<String> data = FXCollections.observableArrayList();
           for(int i = 1; i <= Integer.valueOf((String)arg);i++)
           {
               data.add("Mail" + String.valueOf(i));
           }
           listView.setItems(data);
       }
   }

   private void setCurrentScene(Scene scene){
       Platform.runLater(() -> {
           primaryStage.setScene(scene);
               }
       );
   }
}
package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AnalysisDashboard extends Application {

    @FXML
    public TextField serverIpField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button uploadButton;
    @FXML
    public TextArea analysisArea;
    @FXML
    public ProgressBar progressBar;
    @FXML
    public ListView<String> fileListView;
    @FXML
    public ComboBox<String> fileTypeFilter;
    @FXML
    public DatePicker dateFilter;
    @FXML
    public TextField searchBar;
    @FXML
    public Button connectButton;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private DataOutputStream dataOutputStream; // Use DataOutputStream directly
    private ObservableList<String> fileList = FXCollections.observableArrayList();
    private List<File> allFiles = new ArrayList<>();

    private static final int TIMEOUT = 30000; // Increased timeout

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/fxml/dashboard.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Code Analysis Dashboard");
        scene.getStylesheets().add(getClass().getResource("/resources/css/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //The start method loads the FXML file (dashboard.fxml) to define the GUI layout and applies a stylesheet (styles.css).

    @FXML
    public void initialize() {
        fileTypeFilter.getItems().addAll("All", "C++", "Python", "CUDA C++");
        fileTypeFilter.setValue("All");
        fileTypeFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        initializeSidebar();
        uploadButton.setDisable(true);
        connectButton.setOnAction(event -> connectToServer());
        uploadButton.setOnAction(event -> uploadFile());
        progressBar.setVisible(false);
    }

    /*
        The initialize method sets up the GUI components, 
        such as populating the file type filter, configuring event listeners, and initializing the sidebar. 
    */
    @FXML
    public void connectToServer() {
        String serverIp = serverIpField.getText().trim();
        String password = passwordField.getText().trim();

        if (serverIp.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Connection Error", "Please enter server IP and password.");
            return;
        }

        new Thread(() -> { //multithreading is used here. a new thread is started to avoid blocking the GUI
            try {
                socket = new Socket(serverIp, 8080);
                socket.setSoTimeout(TIMEOUT);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                dataOutputStream = new DataOutputStream(socket.getOutputStream()); 
               
                /*  
                    Output (PrintWriter), input (BufferedReader), and binary output (DataOutputStream) 
                    streams are initialized for communication with the server.
                */

                String serverMessage = in.readLine();
                if ("PASSWORD_REQUEST".equals(serverMessage)) { //waits for a PASSWORD_REQUEST message from server
                    out.println(password);
                    out.flush();

                    String authResponse = in.readLine();
                    Platform.runLater(() -> {
                        if ("AUTH_SUCCESS".equals(authResponse)) {
                            showAlert(Alert.AlertType.INFORMATION, "Connection Successful", "Connected to server!");
                            uploadButton.setDisable(false);
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Authentication Failed", "Incorrect password.");
                            uploadButton.setDisable(true);
                        }
                    });
                } else {
                    throw new IOException("Unexpected server response");
                }

            } 
            
            catch (IOException e) 
            {
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.ERROR, "Connection Error", "Could not connect to server: " + e.getMessage());
                    uploadButton.setDisable(true);
                });
                closeSocket();
            }
        }).start();
    }

    private void initializeSidebar() 
    {
        fileListView.setItems(fileList);
        fileListView.setOnMouseClicked(event -> 
        {
            String selectedFile = fileListView.getSelectionModel().getSelectedItem();
            if (selectedFile != null) 
            {
                File fileToShow = allFiles.stream()
                        .filter(f -> f.getName().equals(selectedFile))
                        .findFirst()
                        .orElse(null);
                if (fileToShow != null) 
                {
                    displayAnalysisResults(fileToShow);
                }
            }
        });
    }

    private String readFileContent(File file) 
    {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) 
        {
            StringBuilder content = new StringBuilder(); //created to accumulate the content of the file
            String line;
            while ((line = reader.readLine()) != null) //reads line by line and appends to stringbuilder 
            {
                content.append(line).append("\n");
            }
            return content.toString(); //ensure it is converted to string and then returned
        } 
        
        catch (IOException e) 
        {
            return "Error reading file: " + e.getMessage();
        }
    }

    private void displayAnalysisResults(File file) {
        File outputDirectory = new File("/home/saketh/Desktop/java/24010203008/cpp-analyzer/output");
        String prefix;

        if (file.getName().endsWith(".cpp")) {
            prefix = "cpp-analysis-report-";
        } else if (file.getName().endsWith(".cu")) {
            prefix = "cuda-analysis-report-";
        } else if (file.getName().endsWith(".py")) {
            prefix = "python-analysis-report-";
        } else {
            analysisArea.setText("Unsupported file type for analysis results.");
            return;
        }

        String outputFileName = prefix + file.getName() + ".txt";
        File targetFile = new File(outputDirectory, outputFileName);

        if (targetFile.exists()) {
            String analysisContent = readFileContent(targetFile);
            analysisArea.setText(analysisContent);
        } else {
            analysisArea.setText("Analysis results not found in the output directory.");
        }
    }

    private void applyFilters() {
        String selectedType = fileTypeFilter.getValue(); // selected file is retreived from filetypeFilter dropdown
        LocalDate selectedDate = dateFilter.getValue(); // data is also selected similarlys
        String searchText = searchBar.getText().toLowerCase(); 
        List<File> filteredFiles = allFiles.stream() //iterated over all files and applies the filters
                .filter(file -> {
                    String filename = file.getName();
                    boolean typeMatch = selectedType.equals("All") ||
                            (selectedType.equals("C++") && filename.endsWith(".cpp")) ||
                            (selectedType.equals("Python") && filename.endsWith(".py")) ||
                            (selectedType.equals("CUDA C++") && filename.endsWith(".cu"));
                    boolean dateMatch = selectedDate == null ||
                            LocalDate.ofEpochDay(file.lastModified() / (24 * 60 * 60)).equals(selectedDate);
                    boolean searchMatch = searchText.isEmpty() || filename.toLowerCase().contains(searchText);
                    return typeMatch && dateMatch && searchMatch;
                })
                .collect(Collectors.toList());

        ObservableList<String> filteredList = FXCollections.observableArrayList(
                filteredFiles.stream().map(File::getName).collect(Collectors.toList())
        );      

        /*
            The filtered files are collected into a new list (filteredFiles).
            The names of the filtered files are added to an ObservableList (filteredList)
            which is then set as the items for the fileListView.
        */

        fileListView.setItems(filteredList);
    }

    private void sendFileToServer(File file) throws IOException {
        try {
            if (socket == null || socket.isClosed()) {
                reconnectToServer();
            }

            // Send command first
            out.println("FILE_TRANSFER"); //sends a FILE_TRANSFER command to the server to indicate start of trasfer
            out.flush();
            // Send file size
            dataOutputStream.writeLong(file.length());
            dataOutputStream.flush();

            // Send file name
            dataOutputStream.writeUTF(file.getName());  // Send the actual file name

            // Send file contents
            FileInputStream fis = new FileInputStream(file); //to read file in chunks
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = fis.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytesRead); //Writes each chunk to the server
            }

            fis.close(); //close the file input stream
            dataOutputStream.flush();
             // Read response
             in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Re-initialize to ensure proper stream state
             String response = in.readLine();

            if (!"FILE_RECEIVED".equals(response)) {
                throw new IOException("File transfer failed: " + response);
            }

            Platform.runLater(() -> {
                showAlert(Alert.AlertType.INFORMATION, "Upload Successful", "File sent to server successfully!");
                if (!allFiles.contains(file)) {
                    allFiles.add(file); //upadte UI to show a sucess alert and add file to allFIles and fileList
                    fileList.add(file.getName());
                }
            });

        } catch (SocketTimeoutException e) {
            System.err.println("Socket timeout during file transfer");
            throw e;
        } catch (IOException ex) {
            System.err.println("Detailed error sending file: " + ex.getMessage());
            ex.printStackTrace();
            throw ex;
        }
    }

    @FXML
    public void uploadFile() {
        if (socket == null || socket.isClosed()) {
            showAlert(Alert.AlertType.WARNING, "Upload Error", "Please connect to the server first.");
            return;
        }

        FileChooser fileChooser = new FileChooser(); //let the user select a file
        fileChooser.setTitle("Select Code File");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Code Files", "*.cpp", "*.py", "*.cu"), //only selects files with these extensions
                new FileChooser.ExtensionFilter("All Files", "*.*") //or all files
        );
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            new Thread(() -> {                      //if file is selected, start a new thread to handle file upload
                Platform.runLater(() -> {           //ensure that the GUI remains operational during upload process
                    progressBar.setVisible(true);
                    progressBar.setProgress(0.0);
                    uploadButton.setDisable(true); //button disabed to disable multiple uploads at the same time
                });
                try {
                    sendFileToServer(selectedFile); //send the file to the server
                    Platform.runLater(() -> progressBar.setProgress(1.0)); //update progress bar to show completion
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Upload Failed", "Error sending file: " + e.getMessage()));
                } finally {
                    Platform.runLater(() -> {
                        progressBar.setVisible(false); 
                        uploadButton.setDisable(false);
                    });
                }
            }).start();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    private void reconnectToServer() throws IOException {
        closeSocket();

        String serverIp = serverIpField.getText().trim();
        String password = passwordField.getText().trim();
        if (serverIp.isEmpty() || password.isEmpty()) {
            throw new IOException("Server IP or password is empty");
        }

        try {
            socket = new Socket(serverIp, 8080);
            socket.setSoTimeout(TIMEOUT);

            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataOutputStream = new DataOutputStream(socket.getOutputStream()); 

            String serverMessage = in.readLine();
            if ("PASSWORD_REQUEST".equals(serverMessage)) {
                out.println(password);
                out.flush();

                String authResponse = in.readLine();
                if (!"AUTH_SUCCESS".equals(authResponse)) {
                    throw new IOException("Reconnection authentication failed");
                }
            } else {
                throw new IOException("Unexpected server response during reconnection");
            }
        } catch (IOException e) {
            System.err.println("Reconnection failed: " + e.getMessage());
            throw e; // Re-throw the exception to be caught in the calling method
        }
    }

    private void closeSocket() {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (dataOutputStream != null) dataOutputStream.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            } finally {
                socket = null;
                out = null;
                in = null;
                dataOutputStream = null;
            }
        }
    }

        private void updateProgress(long sent, long total) {
        Platform.runLater(() -> {
            if (total > 0) {
                progressBar.setProgress((double) sent / total);
            }
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}

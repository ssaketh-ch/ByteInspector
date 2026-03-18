import java.io.*;
import java.net.*;

// Import the analyzer classes
import analyzer.CppAnalyzer;
import analyzer.CudaAnalyzer;
import analyzer.PythonAnalyzer;

public class SimpleServer {
    private static final int PORT = 8080;
    private static final String SECRET_PASSWORD = "sairam";
    private static final String UPLOAD_DIR = "uploads";
    private static final String OUTPUT_DIR = "output";

    public static void main(String[] args) {
        // Create directories if they don't exist
        createDirectoryIfNotExists(UPLOAD_DIR);
        createDirectoryIfNotExists(OUTPUT_DIR);
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
        }
    }

    private static void createDirectoryIfNotExists(String dirPath) {
        File dir = new File(dirPath);
        if (!dir.exists()) {
            if (dir.mkdir()) {
                System.out.println(dirPath + " directory created successfully.");
            } else {
                System.err.println("Failed to create " + dirPath + " directory.");
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 DataInputStream input = new DataInputStream(socket.getInputStream())) {

                System.out.println("Handling client...");

                // Authentication process
                out.println("PASSWORD_REQUEST");
                out.flush();
                String receivedPassword = in.readLine();
                if (receivedPassword == null || !receivedPassword.equals(SECRET_PASSWORD)) {
                    out.println("AUTH_FAILED");
                    out.flush();
                    System.out.println("Authentication failed for client");
                    return;
                }
                out.println("AUTH_SUCCESS");
                out.flush();
                System.out.println("Authentication successful for client");

                // Keep handling file transfers until the client closes the connection
                while (true) {
                    String command = in.readLine();
                    if (command == null || command.isEmpty()) {
                        System.out.println("Client disconnected or sent an empty command. Closing connection.");
                        break; // Exit the loop if the client disconnects
                    }

                    if ("FILE_TRANSFER".equals(command)) {
                        System.out.println("Received FILE_TRANSFER command");
                        try {
                            long fileSize = input.readLong(); // Read file size
                            byte[] buffer = new byte[4096];
                            String fileName = input.readUTF();  // Read the actual file name sent by client
                            File outputFile = new File(UPLOAD_DIR, fileName);
                            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                                int bytesRead;
                                long totalBytesRead = 0;
                                while (totalBytesRead < fileSize && (bytesRead = input.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead))) != -1) {
                                    fos.write(buffer, 0, bytesRead);
                                    totalBytesRead += bytesRead;
                                }
                            }

                            System.out.println("File received successfully: " + fileName);
                            out.println("FILE_RECEIVED");
                            out.flush();
                            
                            // Process the file immediately after receiving it
                            processFile(outputFile);
                            
                        } catch (IOException ex) {
                            System.out.println("File transfer exception: " + ex.getMessage());
                            out.println("FILE_TRANSFER_FAILED"); // Inform the client about the failure
                            out.flush();
                        }
                    } else {
                        System.out.println("Received unknown command: " + command);
                        break;
                    }
                }
            } catch (IOException ex) {
                System.out.println("Client handler exception: " + ex.getMessage());
            } finally {
                try {
                    socket.close();
                    System.out.println("Client connection closed");
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                }
            }
        }
    }
    
    // File processing methods integrated from FileProcessor
    private static void processFile(File file) {
        String fileName = file.getName();
        File outputFolder = new File(OUTPUT_DIR);
        
        System.out.println("Processing received file: " + fileName);
        
        if (fileName.endsWith(".cpp")) {
            invokeCppAnalyzer(file, outputFolder);
        } else if (fileName.endsWith(".cu")) {
            invokeCudaAnalyzer(file, outputFolder);
        } else if (fileName.endsWith(".py")) {
            invokePythonAnalyzer(file, outputFolder);
        } else {
            System.out.println("Unsupported file type: " + fileName);
        }
    }

    private static void invokeCppAnalyzer(File file, File outputFolder) {
        System.out.println("Processing C++ file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "cpp-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzeCppFile method with the correct arguments
            CppAnalyzer.analyzeCppFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("C++ analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing C++ file: " + e.getMessage());
        }
    }

    private static void invokeCudaAnalyzer(File file, File outputFolder) {
        System.out.println("Processing CUDA file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "cuda-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzeCudaFile method with the correct arguments
            CudaAnalyzer.analyzeCudaFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("CUDA analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing CUDA file: " + e.getMessage());
        }
    }

    private static void invokePythonAnalyzer(File file, File outputFolder) {
        System.out.println("Processing Python file: " + file.getName());
        try {
            // Define the output file path
            String outputFilePath = new File(outputFolder, "python-analysis-report-" + file.getName() + ".txt").getAbsolutePath();
            
            // Call the analyzePythonFile method with the correct arguments
            PythonAnalyzer.analyzePythonFile(file.getAbsolutePath(), outputFilePath);
            
            System.out.println("Python analysis complete. Report written to: " + outputFilePath);
        } catch (Exception e) {
            System.err.println("Error processing Python file: " + e.getMessage());
        }
    }
}
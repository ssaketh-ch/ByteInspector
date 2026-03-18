import java.io.*;
import java.net.*;

public class CppAnalyzerServer {
    private static final int PORT = 5000;
    private static final String SERVER_DIR = "/home/cab-prj/saketh/java/cpp-analyzer/";
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for connection...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                // Step 1: Receive the C++ file
                receiveFile(clientSocket, SERVER_DIR + "test-files/test.cpp");
                System.out.println("File received.");

                // Step 2: Run the analyzer
                runAnalyzer();

                // Step 3: Send back the report
                sendFile(clientSocket, SERVER_DIR + "output/analysis-report.txt");
                System.out.println("Analysis report sent back.");

                clientSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void receiveFile(Socket socket, String destinationPath) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        FileOutputStream fos = new FileOutputStream(destinationPath);
        
        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = dis.read(buffer)) > 0) {
            fos.write(buffer, 0, bytesRead);
        }
        
        fos.close();
    }

    private static void runAnalyzer() {
        try {
            Process process = Runtime.getRuntime().exec(
                "java -cp " + SERVER_DIR + "bin analyzer.CppAnalyzer " +
                SERVER_DIR + "test-files/test.cpp " +
                SERVER_DIR + "output/analysis-report.txt"
            );
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(Socket socket, String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) > 0) {
            dos.write(buffer, 0, bytesRead);
        }

        fis.close();
        dos.flush();
    }
}

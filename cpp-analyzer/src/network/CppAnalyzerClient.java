import java.io.*;
import java.net.*;

public class CppAnalyzerClient {
    private static final String SERVER_IP = "10.110.11.32";
    private static final int SERVER_PORT = 5000;
    private static final String LOCAL_TEST_FILE = "test-files/test.cpp";
    private static final String LOCAL_OUTPUT_FILE = "output/analysis-report.txt";

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_IP, SERVER_PORT)) {
            System.out.println("Connected to server.");

            // Step 1: Send C++ file
            sendFile(socket, LOCAL_TEST_FILE);
            System.out.println("File sent to server.");

            // Step 2: Receive analysis report
            receiveFile(socket, LOCAL_OUTPUT_FILE);
            System.out.println("Analysis report received.");

        } catch (IOException e) {
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
}

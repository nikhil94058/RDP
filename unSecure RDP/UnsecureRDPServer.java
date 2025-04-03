import java.io.*;
import java.net.*;

public class UnsecureRDPServer {
    private static final int PORT = 6000;  // Server Port

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Unsecure RDP Server started on port " + PORT);

            while (true) {
                // Accept client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Setup input/output streams for communication
                BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter output = new PrintWriter(clientSocket.getOutputStream(), true);

                // Read client message
                String clientMessage = input.readLine();
                System.out.println("Server received: " + clientMessage);

                // Send a response back to the client (No security)
                String serverResponse = "Hello from Server!";
                output.println(serverResponse);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

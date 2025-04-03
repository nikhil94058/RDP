import java.io.*;
import java.net.*;

public class UnsecureRDPClient {
    private static final String SERVER_ADDRESS = "localhost";  // Server IP address
    private static final int PORT = 6000;  // Server Port

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
            System.out.println("Connected to Unsecure RDP Server.");

            // Setup input/output streams
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            String message = "Hello from Client!";
            System.out.println("Client sending: " + message);
            output.println(message);

            // Receive the server's response (No security)
            String response = input.readLine();
            System.out.println("Client received: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

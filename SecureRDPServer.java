import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Scanner;

public class SecureRDPServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) { // Keep ServerSocket open
            System.out.println("Secure RDP Server is listening on port " + PORT);
            Scanner scanner = new Scanner(System.in); // Scanner for MFA

            // Continuous loop to accept multiple clients
            while (true) {
                try (Socket socket = serverSocket.accept()) {
                    InetAddress clientAddress = socket.getInetAddress();
                    System.out.println("Client connected from: " + clientAddress);

                    // Setup input/output streams
                    BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                    // Perform Multi-Factor Authentication (MFA)
                    if (!performMFA(output, input)) {
                        System.out.println("MFA failed. Closing connection.");
                        socket.close();
                        continue; // Skip to the next client connection
                    }

                    while (true) {
                        // Receive encrypted message
                        String encryptedMessage = input.readLine();
                        if (encryptedMessage == null) continue;

                        String decryptedMessage = EncryptionUtil.decrypt(encryptedMessage);
                        System.out.println("Received (decrypted): " + decryptedMessage);

                        // Encrypt response and send back
                        String response = "Message received securely: " + decryptedMessage.toUpperCase();
                        String encryptedResponse = EncryptionUtil.encrypt(response);

                        // Log the received message and server's response
                        logSession(clientAddress.toString(), decryptedMessage, "Received");
                        logSession(clientAddress.toString(), response, "Sent");

                        // Prompt server-side input and send response
                        System.out.println("Enter Message to send from Server:");
                        String toSend = scanner.nextLine();
                        String toSendEncryted = EncryptionUtil.encrypt(toSend);
                        output.println(toSendEncryted); // Send user input from server
                        output.println(encryptedResponse); // Send encrypted response

                        // Session monitoring (log both received and sent messages)
                        logSession(clientAddress.toString(), decryptedMessage, "Received");
                        logSession(clientAddress.toString(), toSend, "Sent");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean performMFA(PrintWriter output, BufferedReader input) throws IOException {
        Random rand = new Random();
        int otp = 100000 + rand.nextInt(900000); // Generate 6-digit OTP
        System.out.println("Generated OTP: " + otp);
        
        // Send OTP to client
        output.println("Enter OTP: " + otp);
    
        // Read OTP response from client
        String userOtp = input.readLine();
        
        if (userOtp != null && userOtp.equals(String.valueOf(otp))) {
            output.println("MFA_SUCCESS"); // Inform client that authentication succeeded
            return true;
        } else {
            output.println("MFA_FAILED"); // Inform client that authentication failed
            return false;
        }
    }
    
    private static void logSession(String clientIP, String message, String direction) {
        try (FileWriter fw = new FileWriter("session_logs.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println("[" + LocalDateTime.now() + "] " + direction + " Message from Client: " + clientIP + " - " + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

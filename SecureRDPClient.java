import java.io.*;
import java.net.*;
import java.util.Scanner;

public class SecureRDPClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 5000;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // ✅ Scanner should be outside the loop

        while (true) {
            try (Socket socket = new Socket(SERVER_ADDRESS, PORT)) {
                System.out.println("Connected to Secure RDP Server.");

                // Setup input/output streams
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                // 🔹 Step 1: Receive OTP prompt from the server
                String otpPrompt = input.readLine();
                if (otpPrompt == null || !otpPrompt.startsWith("Enter OTP:")) {
                    System.out.println("Invalid OTP request from server.");
                    return;
                }

                System.out.println(otpPrompt);

                // 🔹 Step 2: Extract OTP safely
                String[] otpParts = otpPrompt.split(":");
                String otp = (otpParts.length > 1) ? otpParts[1].trim() : "";
                if (otp.isEmpty()) {
                    System.out.println("Error: Received an empty OTP.");
                    return;
                }

                // 🔹 Step 3: User enters OTP and sends it back
                System.out.print("Enter OTP: ");
                String userOtp = scanner.nextLine();
                output.println(userOtp);

                // 🔹 Step 4: Server verifies OTP response
                String authResponse = input.readLine();
                if (authResponse == null || !"MFA_SUCCESS".equals(authResponse)) {
                    System.out.println("Authentication failed. Connection closing...");
                    return;
                }
                System.out.println("MFA successful! Secure communication established.");

                // 🔹 Secure Message Exchange (only if authentication succeeds)
                while (true) {
                    System.out.print("Enter message to send securely (type 'exit' to quit): ");
                    String message = scanner.nextLine();

                    if (message.equalsIgnoreCase("exit")) {
                        System.out.println("Exiting...");
                        break;
                    }

                    // Encrypt and send message
                    String encryptedMessage = EncryptionUtil.encrypt(message);
                    output.println(encryptedMessage);
                    System.out.println("Sent (encrypted): " + encryptedMessage);

                    // Receive and decrypt response
                    String encryptedResponse = input.readLine();
                   
                    if (encryptedResponse == null) {
                        System.out.println("Server disconnected unexpectedly.");
                        break;
                    }

                    String decryptedResponse = EncryptionUtil.decrypt(encryptedResponse);
                    System.out.println("Server response (decrypted): " + decryptedResponse);
                    String ServerEncryptedMsg =  input.readLine();
                    String SeverDecryptedMsg = EncryptionUtil.decrypt(ServerEncryptedMsg);
                    System.out.println(SeverDecryptedMsg);
                }

            } catch (IOException e) {
                System.out.println("Error: Unable to connect to server. Retrying in 5 seconds...");
                try {
                    Thread.sleep(5000); // Wait before retrying
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        scanner.close(); // ✅ Ensure scanner is closed at the end
    }
}

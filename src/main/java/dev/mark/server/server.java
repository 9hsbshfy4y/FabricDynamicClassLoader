package dev.mark.server;

import java.io.*;
import java.net.*;

public class server {
    public static void main(String[] args) throws IOException {
        int port = 9999;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Server started " + port);

        File zipFile = new File(""); // zip from jar2zip

        while (true) {
            Socket clientSocket = serverSocket.accept();

            try (clientSocket;
                 DataInputStream ignored = new DataInputStream(clientSocket.getInputStream());
                 DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
                 FileInputStream fis = new FileInputStream(zipFile)) {
                System.out.println("Client connected: " + clientSocket);

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != - 1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

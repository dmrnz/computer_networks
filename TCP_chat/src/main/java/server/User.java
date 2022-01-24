package server;

import java.io.IOException;
import java.net.Socket;

public class User {
    private final String name;
    private final Socket clientSocket;

    public User(Socket clientSocket, String name) {
        this.clientSocket = clientSocket;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public void closeSocket() {
        try {
            clientSocket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
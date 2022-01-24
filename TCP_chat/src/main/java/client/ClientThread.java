package client;

import interaction.DataWriter;
import protocol.Message;
import protocol.MessageWithFile;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import static phrases.Phrases.*;

public class ClientThread extends Thread {
    private final Socket socket;
    private final DataWriter writer;
    private final Scanner scanner;

    public ClientThread(Socket socket, DataWriter writer, Scanner scanner) {
        this.socket = socket;
        this.writer = writer;
        this.scanner = scanner;
    }

    @Override
    public void run() {
        while (isAlive()) {
            if (!socket.isClosed()) {
                MessageWithFile message = createMessage();
                if (message != null) {
                    writer.write(message);
                    if (message.getMessage().getText().equals(EXIT.getPhrase())) {
                        closeClientThread();
                        break;
                    }
                }
            } else {
                closeClientThread();
                break;
            }
        }
    }

    private MessageWithFile createMessage() {
        if (scanner.hasNext()) {
            if (!socket.isClosed()) {
                String text = scanner.nextLine();
                if (text.trim().length() > 0) {
                    if (text.contains(FILE.getPhrase())) {
                        int fileIndex = text.indexOf(FILE.getPhrase());
                        String filePath = text.substring(fileIndex + 6).trim();
                        File file = new File(filePath);
                        if (file.exists() && !file.isDirectory()) {
                            try {
                                byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
                                text = text.replace(text.substring(fileIndex), SENT_FILE.getPhrase() + file.getName());
                                return new MessageWithFile(new Message(null, null, text, file.getName(),
                                        fileBytes.length), fileBytes);
                            } catch (IOException exception) {
                                System.out.println(FILE_NOT_EXIST.getPhrase());
                                return null;
                            }
                        }
                    }
                    return new MessageWithFile(new Message(null, null, text, null, null), null);
                }
            }
        }
        return null;
    }

    public void closeClientThread() {
        writer.close();
        try {
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        interrupt();
    }
}
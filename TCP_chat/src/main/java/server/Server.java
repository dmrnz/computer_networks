package server;

import phrases.Phrases;
import protocol.MessageWithFile;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class Server {
    private final int port;
    public static SimpleDateFormat simpleDateFormat;
    private final Scanner scanner;
    private ServerThread serverThread;

    public Server(int port) {
        this.port = port;
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public void start() throws IOException {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        simpleDateFormat = new SimpleDateFormat(pattern);
        try (ServerSocket server = new ServerSocket(port)) {
            serverThread = new ServerThread(server);
            serverThread.start();

            while (!serverThread.isInterrupted()) {
                readCommand();
                
            }
        }
    }

    public static void printMessage(MessageWithFile message) {
        String text =   "<" + getCurrentTime(message.getMessage().getTime()) + "> " +
                        "[" + message.getMessage().getName() + "] " +
                        message.getMessage().getText();
        System.out.println(text);
    }

    public static String getCurrentTime(String time) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        ZonedDateTime currentZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        return currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void readCommand() {
        if (scanner.hasNext()) {
            String text = scanner.nextLine();
            try {
                if (text.trim().length() > 0) {
                    Phrases phrase = Phrases.fromString(text);
                    switch (phrase) {
                        case EXIT:
                            serverThread.closeServerThread();
                            break;
                        case HELP:
                            System.out.println(Phrases.COMMANDS.getPhrase());
                            break;
                        case USERS:
                            System.out.println(serverThread.getUsers().keySet());
                            break;
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println(Phrases.BAD_COMMANDS.getPhrase());
            }
        }
    }

    public static final int PORT = 60228;

    public static void main(String[] args) {
        server.Server server = new server.Server(PORT);
        try {
            server.start();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}

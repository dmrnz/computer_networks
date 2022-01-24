package server;

import phrases.Phrases;
import protocol.MessageWithFile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ServerNB {
    private final int port;
    public static SimpleDateFormat simpleDateFormat;
    private final Scanner scanner;
    private ServerThreadNB serverThread;

    public ServerNB(int port) {
        this.port = port;
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public void start() throws IOException {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        simpleDateFormat = new SimpleDateFormat(pattern);
        try (ServerSocketChannel server = ServerSocketChannel.open()) {
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(port));
            serverThread = new ServerThreadNB(server);
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
                            System.out.println(serverThread.getUsers().values());
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
        ServerNB server = new ServerNB(PORT);
        try {
            server.start();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
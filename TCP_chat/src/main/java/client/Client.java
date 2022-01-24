package client;

import interaction.DataReader;
import interaction.DataWriter;
import protocol.Message;
import protocol.MessageWithFile;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Scanner;

import static phrases.Phrases.*;

public class Client {
    private final String address;
    private final int port;
    public static SimpleDateFormat simpleDateFormat;
    private DataReader reader;
    private DataWriter writer;
    private final Scanner scanner;

    public Client(String address, int port) {
        this.address = address;
        this.port = port;
        this.scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    public void start() {
        String pattern = "yyyy-MM-dd'T'HH:mm:ssZ";
        simpleDateFormat = new SimpleDateFormat(pattern);
        try (Socket socket = new Socket(address,port)) {
            try {
                reader = new DataReader(socket.getInputStream());
                writer = new DataWriter(socket.getOutputStream());
            } catch (IOException exception) {
                exception.printStackTrace();
            }

            enterName();
            boolean nameVerified = false;
            while (!nameVerified) {
                if (reader.hasMessage()) {
                    MessageWithFile messageNameVerified = reader.read();
                    if (messageNameVerified.getMessage().getText().contains(WELCOME.getPhrase())) {
                        nameVerified = true;
                    }
                    String time = getCurrentTime(messageNameVerified.getMessage().getTime());
                    String messageToConsole =   "<" + time + "> " +
                                                "[" + messageNameVerified.getMessage().getName() + "] " +
                                                messageNameVerified.getMessage().getText();
                    System.out.println(messageToConsole);
                    if (!nameVerified) {
                        enterName();
                    }
                }
            }

            ClientThread clientThread = new ClientThread(socket, writer, scanner);
            clientThread.start();

            ReceiverThread receiverThread = new ReceiverThread(socket);
            receiverThread.start();

            while (!socket.isClosed()) {
                MessageWithFile message = readMessage();
                if (message != null) {
                    receiverThread.offer(message);
                }
            }

            scanner.close();
            writer.close();
            reader.close();
            clientThread.interrupt();
            receiverThread.interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void enterName() {
        System.out.print(ENTER_NAME.getPhrase());
        String nickname = scanner.nextLine();
        MessageWithFile loginMessage = new MessageWithFile(
                new Message(null, null, nickname, null, null), null);
        writer.write(loginMessage);
    }

    private MessageWithFile readMessage() {
        if (reader.hasMessage()) {
            return reader.read();
        }
        return null;
    }

    public static String getCurrentTime(String time) {
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(time, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
        ZonedDateTime currentZonedDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault());
        if (currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .equals(new SimpleDateFormat("yyyy-MM-dd").format(new Date()))) {
            return currentZonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        } else {
            return currentZonedDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    public static String ADDRESS = "localhost";
    public static int PORT = 60228;

    public static void main(String[] args) {
        Client client = new Client(ADDRESS, PORT);
        client.start();
    }
}
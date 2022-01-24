package server;

import interaction.DataReader;
import interaction.DataWriter;
import protocol.Message;
import protocol.MessageWithFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static phrases.Phrases.*;

public class ServerThread extends Thread {
    private final ServerSocket server;
    private final List<DataWriter> dataWriterList;
    private final List<ClientThread> clientThreadList;
    private final Map<String,User> users;

    public ServerThread(ServerSocket server) {
        this.server = server;
        this.dataWriterList = Collections.synchronizedList(new LinkedList<>());
        this.clientThreadList = new LinkedList<>();
        this.users = new HashMap<>();
    }

    @Override
    public void run() {
        while (!server.isClosed()) {
            try {
                Socket socket = server.accept();
                DataReader dataReader = new DataReader(socket.getInputStream());
                DataWriter dataWriter = new DataWriter(socket.getOutputStream());

                boolean isAdded = false;
                while (!isAdded) {
                    if (dataReader.hasMessage()) {
                        MessageWithFile messageWithFile = dataReader.read();
                        String userName = messageWithFile.getMessage().getText();
                        String time = Server.simpleDateFormat.format(new Date());
                        if (users.containsKey(userName)) {
                            MessageWithFile messageToUser = new MessageWithFile(
                                    new Message(time,SERVER.getPhrase(),NAME_IS_TAKEN.getPhrase(),null,null),
                                    null);
                            dataWriter.write(messageToUser);
                        } else {
                            User user = new User(socket,userName);
                            users.put(userName,user);
                            dataWriterList.add(dataWriter);

                            ClientThread clientThread = new ClientThread(dataReader,dataWriter,dataWriterList,user,users);
                            clientThread.start();
                            clientThreadList.add(clientThread);

                            MessageWithFile messageToUser = new MessageWithFile(
                                    new Message(time,SERVER.getPhrase(),WELCOME.getPhrase() + userName,null,null),
                                    null);
                            Server.printMessage(messageToUser);
                            for (DataWriter writer : dataWriterList) {
                                writer.write(messageToUser);
                            }
                            isAdded = true;
                        }
                    }
                }
            } catch (IOException exception) {
                System.out.println("Сервер выключен.");
            }
        }
    }

    public Map<String, User> getUsers() {
        return users;
    }

    public void closeServerThread() {
        String time = Server.simpleDateFormat.format(new Date());
        MessageWithFile messageToUser = new MessageWithFile(
                new Message(time,SERVER.getPhrase(),SERVER_CLOSED.getPhrase(),null,null), null);
        Server.printMessage(messageToUser);

        for (DataWriter writer : dataWriterList) {
            writer.write(messageToUser);
        }

        for (ClientThread clientThread : clientThreadList) {
            clientThread.closeClientThread();
            try {
                clientThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        try {
            System.out.println();
            server.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        interrupt();
    }
}
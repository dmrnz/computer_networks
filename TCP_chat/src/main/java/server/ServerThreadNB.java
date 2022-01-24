package server;

import interaction.DataReaderNB;
import interaction.DataWriterNB;
import protocol.Message;
import protocol.MessageWithFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;

import static phrases.Phrases.*;

public class ServerThreadNB extends Thread {
    private final ServerSocketChannel server;
    private final DataReaderNB dataReader;
    private final DataWriterNB dataWriter;
    private final LinkedList<SocketChannel> unregisteredUsers;
    private final Map<SocketChannel,String> users;
    private final Selector selector;
    private final ByteBuffer sharedBuffer;

    public ServerThreadNB(ServerSocketChannel server) throws IOException {
        this.server = server;
        users = new HashMap<>();
        unregisteredUsers = new LinkedList<>();
        sharedBuffer = ByteBuffer.allocateDirect(Integer.MAX_VALUE);
        dataReader = new DataReaderNB(sharedBuffer);
        dataWriter = new DataWriterNB(sharedBuffer);
        selector = Selector.open();
        this.server.register(selector, SelectionKey.OP_ACCEPT);
    }

    @Override
    public void run() {
        while (server.isOpen()) {
            try {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0) {
                    continue;
                }
                Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
                while (selectedKeys.hasNext()) {
                    SelectionKey key = selectedKeys.next();
                    selectedKeys.remove();
                    if (!key.isValid()) {
                        continue;
                    }
                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    }
                }
            } catch (IOException exception) {
                System.out.println("Сервер выключен.");
            }
        }
        try {
            selector.close();
            for (SocketChannel client : users.keySet()) {
                client.close();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void accept(SelectionKey key) {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();

            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            unregisteredUsers.add(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void read(SelectionKey key) {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        sharedBuffer.clear();

        try {
            if (dataReader.hasMessage(socketChannel)) {
                MessageWithFile messageWithFile = dataReader.read(socketChannel);
                if (unregisteredUsers.contains(socketChannel)) {
                    String userName = messageWithFile.getMessage().getText();
                    String time = Server.simpleDateFormat.format(new Date());
                    if (users.containsValue(userName)) {
                        MessageWithFile messageToUser = new MessageWithFile(
                                new Message(time, SERVER.getPhrase(), NAME_IS_TAKEN.getPhrase(), null, null),
                                null);
                        dataWriter.write(messageToUser, socketChannel);
                    } else {
                        unregisteredUsers.remove(socketChannel);
                        users.put(socketChannel, userName);

                        MessageWithFile messageToUser = new MessageWithFile(
                                new Message(time, SERVER.getPhrase(), WELCOME.getPhrase() + userName, null, null),
                                null);
                        Server.printMessage(messageToUser);
                        for (SocketChannel client : users.keySet()) {
                            dataWriter.write(messageToUser, client);
                        }
                    }
                } else {
                    if (messageWithFile != null) {
                        String time = Server.simpleDateFormat.format(new Date());
                        if (messageWithFile.getMessage().getText().trim().equals(EXIT.getPhrase())) {
                            socketChannel.close();
                            key.cancel();
                            messageWithFile = new MessageWithFile(
                                    new Message(time, SERVER.getPhrase(),users.get(socketChannel) +
                                            USER_DISCONNECT.getPhrase(), null, null), null);
                            users.remove(socketChannel);
                            Server.printMessage(messageWithFile);
                            for (SocketChannel client : users.keySet()) {
                                dataWriter.write(messageWithFile, client);
                            }
                        } else {
                            messageWithFile.getMessage().setTime(time);
                            messageWithFile.getMessage().setName(users.get(socketChannel));
                            Server.printMessage(messageWithFile);
                            for (SocketChannel client : users.keySet()) {
                                dataWriter.write(messageWithFile, client);
                            }
                        }
                    }
                }
            }
        } catch (IOException exception) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            key.cancel();
            String time = Server.simpleDateFormat.format(new Date());
            MessageWithFile messageWithFile = new MessageWithFile(
                    new Message(time, SERVER.getPhrase(),users.get(socketChannel) +
                            USER_DISCONNECT.getPhrase(), null, null), null);
            users.remove(socketChannel);
            Server.printMessage(messageWithFile);
            for (SocketChannel client : users.keySet()) {
                dataWriter.write(messageWithFile, client);
            }
        }
    }

    public Map<SocketChannel,String> getUsers() {
        return users;
    }

    public void closeServerThread() {
        String time = Server.simpleDateFormat.format(new Date());
        MessageWithFile messageToUser = new MessageWithFile(
                new Message(time,SERVER.getPhrase(),SERVER_CLOSED.getPhrase(),null,null), null);
        Server.printMessage(messageToUser);
        for (SocketChannel client : users.keySet()) {
            dataWriter.write(messageToUser, client);
        }

        try {
            server.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        interrupt();
    }
}
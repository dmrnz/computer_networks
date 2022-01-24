package server;

import interaction.DataReader;
import interaction.DataWriter;
import protocol.Message;
import protocol.MessageWithFile;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static phrases.Phrases.*;

public class ClientThread extends Thread {
    private final DataReader dataReader;
    private final DataWriter dataWriter;
    private final List<DataWriter> dataWriterList;
    private final User user;
    private final Map<String,User> users;

    public ClientThread(DataReader dataReader, DataWriter dataWriter, List<DataWriter> dataWriterList, User user, Map<String,User> users) {
        this.dataReader = dataReader;
        this.dataWriter = dataWriter;
        this.dataWriterList = dataWriterList;
        this.user = user;
        this.users = users;
    }

    @Override
    public void run() {
        MessageWithFile messageWithFile = null;

        while (!user.getClientSocket().isClosed()) {
            if (dataReader.hasMessage()) {
                messageWithFile = dataReader.read();
            }
            if (messageWithFile != null) {
                String time = Server.simpleDateFormat.format(new Date());
                if (messageWithFile.getMessage().getText().trim().equals(EXIT.getPhrase())) {
                    closeClientThread();
                    messageWithFile = new MessageWithFile(
                            new Message(time,SERVER.getPhrase(),user.getName() + USER_DISCONNECT.getPhrase(),null,null),
                            null);
                    Server.printMessage(messageWithFile);
                    for (DataWriter writer : dataWriterList) {
                        writer.write(messageWithFile);
                    }
                } else {
                    messageWithFile.getMessage().setTime(time);
                    messageWithFile.getMessage().setName(user.getName());
                    Server.printMessage(messageWithFile);
                    for (DataWriter writer : dataWriterList) {
                        writer.write(messageWithFile);
                    }
                }
                messageWithFile = null;
            }
        }
        closeClientThread();
    }

    public void closeClientThread() {
        dataReader.close();
        dataWriter.close();
        user.closeSocket();
        dataWriterList.remove(dataWriter);
        users.remove(user.getName());
        interrupt();
    }
}
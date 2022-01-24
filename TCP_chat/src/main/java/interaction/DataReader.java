package interaction;

import protocol.Message;
import protocol.MessageWithFile;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DataReader {
    private final DataInputStream inputStream;

    public DataReader(InputStream inputStream) {
        this.inputStream = new DataInputStream(inputStream);
    }

    public MessageWithFile read() {
        Message message = readMessageFromStream();
        byte[] file = null;
        if (message.getFileName() != null && message.getFileSize() != null) {
            file = readFileFromStream(message.getFileSize());
        }
        return new MessageWithFile(message,file);
    }

    private Message readMessageFromStream() {
        int messageSize = 0;
        try {
            for (int i = 0; i < 4; i++) {
                messageSize = messageSize << 8;
                messageSize += inputStream.read();
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        byte[] messageBytes = new byte[messageSize];
        for (int i = 0; i < messageSize; i++) {
            try {
                messageBytes[i] = inputStream.readByte();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return Message.toMessage(messageBytes);
    }

    private byte[] readFileFromStream(int fileSize) {
        byte[] file = new byte[fileSize];
        for (int i = 0; i < fileSize; i++) {
            try {
                file[i] = inputStream.readByte();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return file;
    }

    public boolean hasMessage() {
        try {
            return inputStream.available() > 0;
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    public void close() {
        try {
            inputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
package interaction;

import protocol.Message;
import protocol.MessageWithFile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class DataWriter {
    private final DataOutputStream outputStream;

    public DataWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(outputStream);
    }

    public void write(MessageWithFile messageWithFile) {
        Message message = messageWithFile.getMessage();
        try {
            writeMessageInStream(message);
            if (message.getFileName() != null && message.getFileSize() != null) {
               writeFileInStream(messageWithFile.getFile());
            }
            outputStream.flush();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void writeMessageInStream(Message message) throws IOException {
        byte[] messageBytes = message.toBytes();
        outputStream.write(messageBytes.length >> 24);
        outputStream.write(messageBytes.length >> 16);
        outputStream.write(messageBytes.length >> 8);
        outputStream.write(messageBytes.length);
        outputStream.write(messageBytes);
    }

    private void writeFileInStream(byte[] file) throws IOException {
        outputStream.write(file);
    }

    public void close() {
        try {
            outputStream.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
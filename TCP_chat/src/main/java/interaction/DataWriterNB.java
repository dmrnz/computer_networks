package interaction;

import protocol.Message;
import protocol.MessageWithFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DataWriterNB {
    private final ByteBuffer sharedBuffer;

    public DataWriterNB(ByteBuffer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
    }

    public void write(MessageWithFile messageWithFile, SocketChannel socketChannel) {
        Message message = messageWithFile.getMessage();
        try {
            writeMessageInStream(message, socketChannel);
            if (message.getFileName() != null && message.getFileSize() != null) {
               writeFileInStream(messageWithFile.getFile(), socketChannel);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    private void writeMessageInStream(Message message, SocketChannel socketChannel) throws IOException {
        byte[] messageBytes = message.toBytes();
        sharedBuffer.put((byte) (messageBytes.length >> 24));
        sharedBuffer.put((byte) (messageBytes.length >> 16));
        sharedBuffer.put((byte) (messageBytes.length >> 8));
        sharedBuffer.put((byte) messageBytes.length);
        sharedBuffer.flip();
        while (sharedBuffer.hasRemaining()) {
            socketChannel.write(sharedBuffer);
        }
        sharedBuffer.clear();

        sharedBuffer.put(messageBytes);
        sharedBuffer.flip();
        while (sharedBuffer.hasRemaining()) {
            socketChannel.write(sharedBuffer);
        }
        sharedBuffer.clear();
    }

    private void writeFileInStream(byte[] file, SocketChannel socketChannel) throws IOException {
        sharedBuffer.put(file);
        sharedBuffer.flip();
        while (sharedBuffer.hasRemaining()) {
            socketChannel.write(sharedBuffer);
        }
        sharedBuffer.clear();
    }
}

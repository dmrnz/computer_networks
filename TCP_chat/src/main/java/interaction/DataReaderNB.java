package interaction;

import protocol.Message;
import protocol.MessageWithFile;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class DataReaderNB {
    private final ByteBuffer sharedBuffer;

    public DataReaderNB(ByteBuffer sharedBuffer) {
        this.sharedBuffer = sharedBuffer;
    }

    public MessageWithFile read(SocketChannel socketChannel) {
        Message message = readMessageFromStream(socketChannel);
        byte[] file = null;
        if (message.getFileName() != null && message.getFileSize() != null) {
            file = readFileFromStream(message.getFileSize(), socketChannel);
        }
        return new MessageWithFile(message,file);
    }

    private Message readMessageFromStream(SocketChannel socketChannel) {
        int messageSize = 0;
        sharedBuffer.position(sharedBuffer.limit());
        sharedBuffer.limit(4);
        try {
            while (sharedBuffer.hasRemaining()) {
                socketChannel.read(sharedBuffer);
            }
            for (int i = 0; i < 4; i++) {
                messageSize = messageSize << 8;
                messageSize += sharedBuffer.get(i) & 0xFF;
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        sharedBuffer.clear();
        sharedBuffer.limit(messageSize);
        byte[] messageBytes = new byte[messageSize];
        readFromBuffer(socketChannel, messageBytes);

        return Message.toMessage(messageBytes);
    }

    private byte[] readFileFromStream(int fileSize, SocketChannel socketChannel) {
        byte[] file = new byte[fileSize];
        sharedBuffer.limit(fileSize);
        readFromBuffer(socketChannel, file);

        return file;
    }

    private void readFromBuffer(SocketChannel socketChannel, byte[] bytes) {
        try {
            while (sharedBuffer.hasRemaining()) {
                socketChannel.read(sharedBuffer);
            }
            for (int i = 0; i < sharedBuffer.limit(); i++) {
                bytes[i] = sharedBuffer.get(i);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        sharedBuffer.clear();
    }

    public boolean hasMessage(SocketChannel socketChannel) throws IOException {
        sharedBuffer.limit(4);
        socketChannel.read(sharedBuffer);
        sharedBuffer.flip();
        return sharedBuffer.hasRemaining();
    }
}
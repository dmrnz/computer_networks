package client;

import phrases.Phrases;
import protocol.MessageWithFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.Queue;

public class ReceiverThread extends Thread {
    private final Socket socket;
    private final Queue<MessageWithFile> receiverQueue = new ArrayDeque<>();

    public ReceiverThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        while (isAlive()) {
            if (!socket.isClosed()) {
                if (!receiverQueue.isEmpty()) {
                    MessageWithFile message = receiverQueue.poll();
                    readMessage(message);
                }
            } else {
                closeReceiverThread();
                break;
            }
        }
    }

    public void offer(MessageWithFile messageWithFile) {
        receiverQueue.offer(messageWithFile);
    }

    private void readMessage(MessageWithFile message) {
        String text =   "<" + Client.getCurrentTime(message.getMessage().getTime()) + "> " +
                        "[" + message.getMessage().getName() + "] " +
                        message.getMessage().getText();
        if (message.getFile() != null) {
            writeFile(message.getMessage().getName(), message.getMessage().getFileName(), message.getFile());
        }
        System.out.println(text);
        if (message.getMessage().getText().equals(Phrases.SERVER_CLOSED.getPhrase())) {
            closeReceiverThread();
        }
    }

    private String createDirectory(String name) {
        File directory = new File(System.getProperty("user.home") + File.separator +
                Phrases.DOWNLOAD_DIRECTORY.getPhrase() + File.separator + name);
        if (!directory.exists()) {
            if (directory.mkdir()) {
                System.out.println(Phrases.CREATE_DIRECTORY.getPhrase() + directory.getAbsolutePath());
            }
        }
        return directory.getAbsolutePath();
    }

    private void writeFile(String name, String fileName, byte[] fileBytes) {
        String directoryPath = createDirectory(name);
        File file = new File(directoryPath + File.separator + fileName);
        try {
            if (file.createNewFile()) {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(fileBytes);
                fileOutputStream.close();
                System.out.println(Phrases.FILE_RECEIVED.getPhrase() + fileName);
            } else {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(fileBytes);
                fileOutputStream.close();
                System.out.println(Phrases.FILE_OVERWRITTEN.getPhrase() + fileName);
            }
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void closeReceiverThread() {
        try {
            socket.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        interrupt();
    }
}
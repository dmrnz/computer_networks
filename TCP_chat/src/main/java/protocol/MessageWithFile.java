package protocol;

public class MessageWithFile {
    private final Message message;
    private final byte[] file;

    public MessageWithFile(Message message, byte[] file) {
        this.message = message;
        this.file = file;
    }

    public Message getMessage() {
        return message;
    }

    public byte[] getFile() {
        return file;
    }
}
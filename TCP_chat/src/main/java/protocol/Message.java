package protocol;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

public class Message implements Serializable {
    private String time;
    private String name;
    private String text;
    private String fileName;
    private Integer fileSize;

    public Message(String time, String name, String text, String fileName, Integer fileSize) {
        this.time = time;
        this.name = name;
        this.text = text;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public Message() {
        super();
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return "Message{" +
                "time='" + time + '\'' +
                ", name='" + name + '\'' +
                ", text='" + text + '\'' +
                ", file='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return fileSize.equals(message.fileSize) && Objects.equals(time, message.time) &&
                Objects.equals(name, message.name) && Objects.equals(text, message.text) &&
                Objects.equals(fileName, message.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, name, text, fileName, fileSize);
    }

    public byte[] toBytes() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsBytes(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Message toMessage(byte[] bytes) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(bytes, Message.class);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }
}
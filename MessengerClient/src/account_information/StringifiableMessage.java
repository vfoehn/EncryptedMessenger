package account_information;

import java.text.SimpleDateFormat;

public class StringifiableMessage {

    private String sender;
    private String sendTimeString;
    private String content;

    public StringifiableMessage(Message message) {
        this.sender = message.getSender();
        this.sendTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").
                format(message.getSendTime());
        this.content = message.getContent();
    }

    public String getSender() {
        return sender;
    }


    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSendTimeString() {
        return sendTimeString;
    }

    public void setSendTimeString(String sendTimeString) {
        this.sendTimeString = sendTimeString;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}

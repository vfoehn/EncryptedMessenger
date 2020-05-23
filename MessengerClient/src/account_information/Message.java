package account_information;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

// Message contains all the information for a sent or received message. This includes the message's
// content as well as meta-information. The class implements the interface Comparable so that we
// can create the correct order between sent and received messages.
public class Message implements Comparable<Message>{

	private String sender;
	private Timestamp sendTime;
	private String content;
	
	public Message(String sender, Timestamp sendTime, String content) {
		this.sender = sender;
		this.sendTime = sendTime;
		this.content = content;
	}
	
	public Message(StringifiableMessage stringifiableMessage) throws ParseException {
		this.sender = stringifiableMessage.getSender();
		this.sendTime = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").
				parse(stringifiableMessage.getSendTimeString()).getTime());
		this.content = stringifiableMessage.getContent();
	}
	
	public String getSender() {
		return sender;
	}	

	public Timestamp getSendTime() {
		return sendTime;
	}

	public String getContent() {
		return content;
	}

	@Override
	public int compareTo(Message o) {
		return this.sendTime.compareTo(o.getSendTime());
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Message))
			return false;
		
		Message other = (Message) obj;
		return this.sender.equals(other.getSender()) &&
				this.sendTime.equals(other.getSendTime()) &&
				this.content.equals(other.getContent());		
	}
	
}

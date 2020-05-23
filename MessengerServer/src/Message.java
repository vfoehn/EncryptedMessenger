import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Message {

	private Timestamp sendTime;
	private String content;
	
	// Used for writing to file and sending messages
	private String sendTimeString;
	
	public Message(Timestamp sendTime, String content) {
		this.sendTime = sendTime;
		this.content = content;
		
		this.sendTimeString =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS").format(sendTime);
	}

	public Timestamp getSendTime() {
		return sendTime;
	}

	public void setSendTime(Timestamp sendTime) {
		this.sendTime = sendTime;
	}
	
}

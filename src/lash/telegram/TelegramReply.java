package lash.telegram;

import lash.telegram.TelegramMessage;

public class TelegramReply implements Comparable<TelegramReply> {
	private TelegramMessage sourceMessage;
	private String message;
	
	public TelegramReply() {
		sourceMessage = new TelegramMessage();
	}
	
	public void set(String m) throws IllegalArgumentException {
		try {
			message = m;
			
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
	}
	
	public void setSource(TelegramMessage m) {
		this.sourceMessage = m;
	}
	
	public String getSource() {
		return this.sourceMessage.getId();
	}
	
	public int getUpdateId() {
		return sourceMessage.getUpdateId();
	}
	
	public String getText() {
		return message;
	}
	
	public int getChatId() {
		return sourceMessage.getChatId();
	}
	
	public String getChatUserId() {
		return sourceMessage.getChatUserId();
	}
	
	public String toString() {
		return String.format("update_id: %d\n" +
								"message:\n\n%s\n",
								this.sourceMessage.getUpdateId(), message);
	}
	
	public void unset() {
		message = null;
		sourceMessage = new TelegramMessage();
	}
	

	 public int compareTo(TelegramReply o) {
	  TelegramReply other = (TelegramReply) o;
	 
	  Integer ui1 = (Integer) this.sourceMessage.getMessageId();
	  Integer ui2 = (Integer) other.sourceMessage.getMessageId();
	 
	  return ui1.compareTo(ui2);
	 
	 }
}

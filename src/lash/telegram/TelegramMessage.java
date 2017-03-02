package lash.telegram;

import java.util.UUID;

public class TelegramMessage {

	/**
	 * @param parameterObject TODO
	 */
	
	private UUID id;
	private int chatId;
	private String chatUserId;
	private int messageId;
	private int updateId;
	private String text;
	
	public TelegramMessage() {
		this.id = UUID.randomUUID();
		this.messageId = -1;
		this.updateId = -1;
		this.chatId = -1;
		this.chatUserId = "";
		this.text = "";
	}

	public String getId() {
		return this.id.toString();
	}
	
	public int getChatId() {
		return this.chatId;
	}
	
	public String getChatUserId() {
		return this.chatUserId;
	}

	public void setChatId(int chatId) {
		this.chatId = chatId;
	}
	
	public void setChatUserId(String chatUserId) {
		this.chatUserId = chatUserId;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	
	public int getUpdateId() {
		return updateId;
	}

	public void setUpdateId(int updateId) {
		this.updateId = updateId;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

}

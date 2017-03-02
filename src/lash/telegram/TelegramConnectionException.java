package lash.telegram;

public class TelegramConnectionException extends Exception {
	static final long serialVersionUID = 1;
	private String detail;
	
	TelegramConnectionException(String d) {
		detail = d;
	}
	
	public String toString() {
		return "Telegram connection exception: " + detail;
	}
}

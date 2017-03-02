package lash.telegram;

public class TelegramAccessException extends Exception {
	static final long serialVersionUID = 1;
	private String detail;
	
	TelegramAccessException() {
		detail = "";
	}
	
	TelegramAccessException(String d) {
		detail = d;
	}
	
	public String toString() {
		return "Telegram exception exception: " + detail;
	}
}

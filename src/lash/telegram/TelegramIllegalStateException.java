package lash.telegram;

public class TelegramIllegalStateException extends Exception {
	static final long serialVersionUID = 1;
	private String detail;
	
	TelegramIllegalStateException(String d) {
		detail = d;
	}
	
	public String toString() {
		return "Telegram parse exception: " + detail;
	}
}

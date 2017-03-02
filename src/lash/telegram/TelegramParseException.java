package lash.telegram;

import java.lang.Exception;

public class TelegramParseException extends Exception {
	static final long serialVersionUID = 1;
	private String detail;
	
	TelegramParseException(String d) {
		detail = d;
	}
	
	public String toString() {
		return "Telegram parse exception: " + detail;
	}
}

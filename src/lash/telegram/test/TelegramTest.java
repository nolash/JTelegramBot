package lash.telegram.test;

import java.util.ArrayList;

import lash.telegram.Telegram;
import lash.telegram.TelegramPoller;
import lash.telegram.TelegramPoster;
import lash.telegram.TelegramQueueHandler;
import lash.telegram.TelegramReply;
import lash.telegram.TelegramInstruction;
import lash.telegram.TelegramInstructionHandler;
import lash.telegram.TelegramParseException;
import lash.telegram.TelegramIllegalStateException;
import lash.telegram.TelegramConnectionException;
import lash.telegram.TelegramMessage;

public class TelegramTest implements TelegramInstructionHandler {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws TelegramConnectionException, InterruptedException {
		TelegramTest t = new TelegramTest();
		Telegram.init(t);
		TelegramPoller poller = new TelegramPoller(); 
		TelegramQueueHandler queuehandler = new TelegramQueueHandler();
		TelegramPoster poster = new TelegramPoster();
		poller.t.join();
		queuehandler.t.join();
		poster.t.join();
		
	}
	
	public ArrayList<TelegramReply> processInstruction(TelegramInstruction instruction) {
		return new ArrayList<TelegramReply>();
	}
	
	public TelegramInstruction parseMessage(String s, TelegramMessage m) throws TelegramParseException, TelegramIllegalStateException {
		return new TelegramInstruction();
	}

}

package lash.telegram.test;

import java.util.ArrayList;
import java.util.HashMap;

import lash.telegram.TelegramIllegalStateException;
import lash.telegram.TelegramConnectionException;
import lash.telegram.TelegramAccessException;
import lash.telegram.TelegramParseException;
import lash.telegram.TelegramInitException;
import lash.telegram.TelegramInstruction;
import lash.telegram.TelegramPoller;
import lash.telegram.TelegramPoster;
import lash.telegram.TelegramQueueHandler;
import lash.telegram.TelegramInstructionHandler;
import lash.telegram.Telegram;
import lash.telegram.TelegramMessage;
import lash.telegram.TelegramReply;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

public class TelegramSecretary implements TelegramInstructionHandler {
	
	@Option(name="-c",usage="configuration file")
	private String configfile;
	
	private static int TELEGRAM_ECHO_RECEIPT_CHAT_ID = -1;
	
	private class TelegramEchoInstruction extends TelegramInstruction {
		public String testparam;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws TelegramConnectionException, InterruptedException, TelegramInitException {
		// TODO Auto-generated method stub
		HashMap<String, String> config = new HashMap<String, String>();
		TelegramSecretary t = new TelegramSecretary();
		
		CmdLineParser parser = new CmdLineParser(t);
        
        try {
            parser.parseArgument(args);
        } catch( CmdLineException e ) {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            System.err.println();
            return;
        }
        
		if (t.configfile != null) {
			Telegram.init(t, t.configfile, config);
		} else {
			Telegram.init(t);
		}
		if (!config.containsKey("RECEIPT_CHAT_ID")) {
			throw new TelegramInitException("Receipt chat id not set in config");
		}
		TELEGRAM_ECHO_RECEIPT_CHAT_ID = Integer.parseInt(config.get("RECEIPT_CHAT_ID"));
		TelegramPoller poller = new TelegramPoller(); 
		TelegramQueueHandler queuehandler = new TelegramQueueHandler();
		TelegramPoster poster = new TelegramPoster();
		poller.t.join();
		queuehandler.t.join();
		poster.t.join();
	}
	
	public ArrayList<TelegramReply> processInstruction(TelegramInstruction ti) {
		ArrayList<TelegramReply> replies = new ArrayList<TelegramReply>();
		TelegramReply tr = new TelegramReply();
		TelegramReply receipt = new TelegramReply();
		TelegramMessage receipt_messagesource = new TelegramMessage();
		
		tr.setSource(ti.getSource());
		tr.set("reply from message " + ti.getSource().getId() + " chatid " + ti.getParam("chatid") + " text " + ti.getValue());
		replies.add(tr);
		
		receipt_messagesource.setChatId(TELEGRAM_ECHO_RECEIPT_CHAT_ID);
		receipt_messagesource.setUpdateId(ti.getSource().getUpdateId());
		receipt.setSource(receipt_messagesource);
		receipt.set("got a message from chatid " + ti.getParam("chatid") + " saying " + ti.getValue());
		replies.add(receipt);
		
		return replies;
	}
	
	public TelegramInstruction parseMessage(String s, TelegramMessage message) throws TelegramParseException, TelegramIllegalStateException {
		TelegramEchoInstruction ti = new TelegramEchoInstruction();
		ti.testparam = String.valueOf(message.getChatId());
		ti.setSource(message);
		ti.set("content", message.getText());
		ti.setParam("chatid", ti.testparam);
		return (TelegramInstruction)ti;
	}

}

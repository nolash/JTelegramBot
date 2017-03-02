package lash.telegram;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Queue;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.SortedMap;
import java.util.Map;
import java.util.Vector;

import lash.telegram.TelegramConnectionException;
import lash.telegram.TelegramMessage;

public final class TelegramPoller extends Telegram implements Runnable {

	private final int polling_timeout = 7200000;
	private final int polling_retry_timeout = 5000;
	private boolean running = false;
	private String query;
	
	
	public TelegramPoller() throws TelegramConnectionException {
		t = new Thread(this, "poller");
		logger = Logger.getLogger(TelegramPoller.class.getName() + ":poller");
		
		t.start();
	}
	
	/**
	 * 
	 * TODO instruction string length cap
	 * TODO decide what to do when instruction could not be added to queue
	 * 
	 */
	public void run() {
		
		BufferedReader br;
		InputStream is;
		InputStreamReader r;
		String response;
		Vector<TelegramMessage> instructions;
		TelegramInstruction ti;
		int updateid;
		
		running = true;
		
		while (running) {
			
			
			try {
				query = String.format("timeout=%s&offset=%d", URLEncoder.encode(String.valueOf(polling_timeout), TELEGRAM_CHARSET), Telegram.getLastReceivedMessage().getUpdateId() + 1);
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.WARNING, "Telegram  '"+ t.getName() + "' query string build failed");
				running = false;
				continue;
				
			}
		
			try  {
				telegram_currenturl = new URL(String.format("%s://%s/bot%s:%s/getUpdates?%s", TELEGRAM_PROTOCOL, TELEGRAM_HOST, TELEGRAM_USER, TELEGRAM_API_KEY, query));
			} catch (MalformedURLException e) {
				logger.log(Level.WARNING, "Telegram  '"+ t.getName() + "' invalid update url.");
				running = false;
				continue;
			}
		
			response = "";
			
			try {
				telegram_conn = (HttpURLConnection)telegram_currenturl.openConnection();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Connect to Telegram failed: " + e);
			}
		
			telegram_conn.setDoOutput(true);
			telegram_conn.setReadTimeout(polling_timeout);
			telegram_conn.setRequestProperty("Accept-Charset", TELEGRAM_CHARSET);
			telegram_conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + TELEGRAM_CHARSET);
			
			try {
				response = doRequest(telegram_conn, query, t.getName());
			} catch (IOException e) {
				try {
					logger.log(Level.WARNING, "Telegram  '"+ t.getName() + "' connect failed");
					Thread.sleep(polling_retry_timeout);
					continue;
				} catch (InterruptedException e2) {
					logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' connect retry interrupted, resuming.");
				}
			}

			instructions = extractMessages(response);
			
			for (TelegramMessage instruction : instructions) {
				
				ti = null;
				
				try {
					ti = instruction_callback.parseMessage(instruction.toString(), instruction);
				} catch (TelegramParseException e) {
					//Telegram.setLastReceivedMessage(instruction);
					logger.log(Level.INFO, "Bogus command " + instruction.getText() + " received");
					continue;
				} catch (TelegramIllegalStateException e) {
					//Telegram.setLastReceivedMessage(instruction);
					logger.log(Level.INFO, "Command " + instruction.getText() + " out of context");
					continue;
				}
				
				if (ti != null) {
					//if (addToInstructionsQueue(ti))
					try {
						if (addRemoveInstructionsQueue(this, ti, 0, false) != null)
							Telegram.setLastReceivedMessage(instruction);
					} catch (TelegramAccessException e) {
						
					}
				}
						
			}
			
			
			//running = false;
		}
		
	}
}
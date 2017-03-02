package lash.telegram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Queue;

public final class TelegramPoster extends Telegram implements Runnable {

	private final int posting_timeout = 10000;
	private final int posting_interval_timeout = 4000;
	private final int posting_retry_timeout = 5000;
	private final int queue_retry_timeout = 10;
	private boolean running = false;
	private String query;
	
	//public TelegramPoster(boolean autostart) throws TelegramConnectionException  {
	public TelegramPoster() throws TelegramConnectionException  {
		t = new Thread(this, "poster");
		logger = Logger.getLogger(TelegramPoller.class.getName() + ":poster");
		//if (autostart)
			t.start();
	}
	
	/**
	 * TODO Initiate wait routines and recovery for queue access fail
	 * TODO 400 error handling for sends.
	 * TODO verify use of post, urlencode and httpurlconnection class change
	 */
	public void run() {
		
		String response;
		TelegramReply tr;
		String urlstem = String.format("%s://%s/bot%s:%s/sendMessage", TELEGRAM_PROTOCOL, TELEGRAM_HOST, TELEGRAM_USER, TELEGRAM_API_KEY);
			
		running = true;
		tr = null;
		
		while (running) {
			
			response = "";
			
			try {
				//tr = reserveRepliesQueue(this);
				tr = addRemoveRepliesQueue(this, null, 1, false);
				try {
					if (tr.getText().equals(""))
						continue;
				} catch (NullPointerException e) {
					continue; 
				}
			} catch (NoSuchElementException e) {
				logger.log(Level.FINE, "All replies processed");
				running = false;
				continue;
			} catch (TelegramAccessException e) {
				logger.log(Level.INFO, "Replies queue is locked.");
				try {
					Thread.sleep(queue_retry_timeout);
				} catch (InterruptedException e2) {
					logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' connect retry interrupted, resuming.");
				} finally {
					continue;
				}
			}
			
			//query = String.format("chat_id=%s&reply_to_message_id=%d&text=%s", TELEGRAM_CHAT_ID, tr.getUpdateId(), tr.getText());
			try {
				query = String.format("chat_id=%d&text=%s", tr.getChatId(), URLEncoder.encode(tr.getText(), TELEGRAM_CHARSET));
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.SEVERE, "Could not set encoding " + TELEGRAM_CHARSET + " for response to Telegram!");
			}
			
			try  {
				telegram_currenturl = new URL(urlstem + "?" + query);
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "Invalid send url for listener. Cannot initiate connection: " + e);
			}
		
			try {
				telegram_conn = (HttpURLConnection)telegram_currenturl.openConnection();
			} catch (IOException e) {
				logger.log(Level.SEVERE, "Request to Telegram failed: " + e);

				try {
					//freeRepliesQueue(this, false);
					Thread.sleep(posting_retry_timeout);
				} catch (InterruptedException e2) {
					logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' connect retry interrupted, resuming.");
				} finally {
					continue;
				}
			}
			
			telegram_conn.setDoOutput(true);
			try {
				telegram_conn.setRequestMethod("POST");
			} catch (ProtocolException e) {
				logger.log(Level.WARNING, "Couldn't set POST method for posting to Telegram");
			}
			telegram_conn.setReadTimeout(posting_timeout);
			telegram_conn.setRequestProperty("Accept-Charset", TELEGRAM_CHARSET);
			telegram_conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + TELEGRAM_CHARSET);
			
			try {
				response = doRequest(telegram_conn, query, t.getName());
			} catch (IOException e) {
				logger.log(Level.WARNING, "Response  '"+ t.getName() + "' connect failed for query " + query + ", error is: " + e);
				try {
					//freeRepliesQueue(this, false);
					Thread.sleep(posting_retry_timeout);
				} catch (InterruptedException e2) {
					logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' connect retry interrupted, resuming.");
				} finally {
					continue;
				}

			} finally {
				try {
					//freeRepliesQueue(this, true);
					addRemoveRepliesQueue(this, null, 1, true);
					Thread.sleep(posting_interval_timeout);
				} catch (InterruptedException e2) {
					logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' message post interval timeout interrupted, resuming.");
				} catch (TelegramAccessException e) {
					logger.log(Level.SEVERE, "Telegram  '" + t.getName() + "' should have lock on queue but doesn't, can't access");
				}
			}
			
			/*

ERROR AND OK RESPONSES:

{"ok":false,"error_code":400,"description":"[Error]: Bad Request: channel not found"}

{"ok":true,"result":{"message_id":8,"from":{"id":174329756,"first_name":"lashbot1","username":"lashbot1_bot"},"chat":{"id":163378306,"first_name":"Louis","last_name":"Holbrook","username":"nolash","type":"private"},"date":1453472550,"text":"testfrombot"}}lash@CANTANDO ~/tmp $ 

			 */
		}
		
	}
}
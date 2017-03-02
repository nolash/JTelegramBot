package lash.telegram;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;
import java.util.HashMap;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.FileNotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import lash.telegram.TelegramInstruction;
import lash.telegram.TelegramReply;
import lash.telegram.TelegramMessage;
import lash.telegram.TelegramInitException;


/**
 * 
 * @author lash
 *
 * TODO chat id needs to be saved in config
 * TODO move all constants for thread timeouts etc here
 * 
 */
public class Telegram {
	private static boolean initialized = false;
	
	protected static String TELEGRAM_USER = "";
	protected static String TELEGRAM_API_KEY = "";
	
	final protected static String TELEGRAM_CONFIG_FILENAME = "telegrambot.conf";
	final protected static String TELEGRAM_HOST = "api.telegram.org";
	final protected static String TELEGRAM_PROTOCOL = "https";
	final protected static String TELEGRAM_CHARSET = "UTF-8";
	final protected static int TELEGRAM_QUEUE_HANDLER_RETRY_DELAY = 500;
	final protected static int TELEGRAM_QUEUE_CAPACITY = 1000;
	
	protected static Logger logger;
	
	protected static Queue<TelegramInstruction> instructions;
	protected static Queue<TelegramReply> replies;
	
	//protected static LinkedList<TelegramInstruction> instructions;
	//protected static LinkedList<TelegramReply> replies;
	
	//private static Object replies_lock;

	protected static TelegramMessage last_received_telegram_message;
	protected static TelegramMessage last_processed_telegram_message;
	
	//private static TelegramInstruction current_instruction; // if a question is being asked, this is the instruction to which
	
	public Thread t;
	
	protected HttpURLConnection telegram_conn;
	protected URL telegram_currenturl;
	
	protected static TelegramInstructionHandler instruction_callback;
	
	/**
	 * TODO Initialize from saved settings file
	 */
	protected Telegram() {
		telegram_conn = null;
	}
	
	public static void setLastReceivedMessage(TelegramMessage m) {
		last_received_telegram_message = m; 
	}
	
	public static TelegramMessage getLastReceivedMessage() {
		return last_received_telegram_message; 
	}
	
	public static void setLastProcessedMessage(TelegramMessage m) {
		last_processed_telegram_message = m; 
	}
	/**
	 * 
	 */
	
	public static boolean init(TelegramInstructionHandler cb) {
		return init(cb, "", null, null);
	}
	
	public static boolean init(TelegramInstructionHandler cb, String configfilename, HashMap<String, String> localconfig) {
		return init(cb, configfilename, null, localconfig);
	}
	
	public static boolean init(TelegramInstructionHandler cb, String key, String val, HashMap<String, String> localconfig) {
		
		if (val != null) {
			Telegram.TELEGRAM_API_KEY = key;
			Telegram.TELEGRAM_USER = val;
		} else {
			try {
				Telegram.loadConfig(key, localconfig);
			} catch (TelegramInitException e) {
				throw new RuntimeException(e.getMessage());
			}
		}
		//instructions = new PriorityQueue<TelegramInstruction>(TELEGRAM_QUEUE_CAPACITY);
		//replies = new PriorityQueue<TelegramReply>(TELEGRAM_QUEUE_CAPACITY);
		instructions = new LinkedList<TelegramInstruction>();
		replies = new LinkedList<TelegramReply>();
		//current_instruction = null;
		
		last_received_telegram_message = new TelegramMessage();
		last_processed_telegram_message = new TelegramMessage();
		//replies_lock = null;
		logger.getLogger("Telegram");
		instruction_callback = cb;
		
		initialized = true;
		
		return true;
		
	}
	
	/**
	 * 
	 * @param caller
	 * @param ti
	 * @param direction int 0 = add to queue, 1 = remove from queue
	 * @param poll
	 * @return
	 * @throws TelegramAccessException
	 */
	protected synchronized TelegramInstruction addRemoveInstructionsQueue(Telegram caller, TelegramInstruction ti, int direction, boolean poll) throws TelegramAccessException {
		boolean wait = false;
		boolean notify = false;
		
		if (direction == 0) { // add to queue
			if (instructions.size() >= TELEGRAM_QUEUE_CAPACITY) {
				wait = true;
				return null;
			} else {
				try {
					instructions.offer(ti);
					//notify();
					notify = true;
				} catch (Exception e) {
					return null;
				}
				return ti;
			} 
		} else if (direction == 1) {
			if (instructions.size() == 0) {
				wait = true;
				return null;
			} else {
				if (poll) {
					//TODO poll fails here when there is more than one instruction
					ti = instructions.poll();
					notify = true;
					return ti;
				} else {
					return instructions.element();
				}
			}
			
		}
		if (notify)
			notify();
		if (wait)
			try {
				wait();
			} catch (InterruptedException e) {
				
			}
		return null;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param caller
	 * @param ty
	 * @param direction
	 * @param poll
	 * @return
	 * @throws TelegramAccessException
	 * 
	 * TODO on multiple instructions last reply is duplicated for all replies
	 * 
	 */
	protected synchronized TelegramReply addRemoveRepliesQueue(Telegram caller, TelegramReply ty, int direction, boolean poll) throws TelegramAccessException {
		boolean wait = false;
		boolean notify = false;
		
		if (direction == 0) { // add to queue
			if (replies.size() >= TELEGRAM_QUEUE_CAPACITY) {
				wait = true;
				return null;
			} else {
				if (ty.getUpdateId() > last_processed_telegram_message.getUpdateId()) {
					try {
						replies.offer(ty);
						//notify();
						notify = true;
					} catch (Exception e) {
						return null;
					}
					return ty;
				} else {
					return ty; // returns object also when not added, but should be same to caller as the queue decides what should in the end get added or not
				}
			}
		} else if (direction == 1) { // remove from queue
			if (replies.size() == 0) {
				wait = true;
				return null;
			} else {
				if (poll) {
					ty = replies.poll();
					notify = true;
					return ty;
				} else {
					return replies.element();
				}
			}
			
		}

		if (notify)
			notify();
		if (wait)
			try {
				wait();
			} catch (InterruptedException e) {
				
			}
		return null;
	}
	
	/**
	 * 
	 * 
	 * @param conn
	 * @param query
	 * @param connlabel
	 * @return
	 * @throws IOException
	 * 
	 * TODO handle timeout, retry
	 * 
	 */
	public String doRequest(HttpURLConnection conn, String query, String connlabel) throws IOException {
	
		String response;
		BufferedReader br;
		InputStream is;
		
		//InputStreamReader r;
		
		try  {
			OutputStream o = conn.getOutputStream();
			o.write(query.getBytes("UTF-8"));
			o.close();
		} catch (IOException e) {
			throw new IOException("Could not make '" + connlabel + "' connect to Telegram.\n\n" + e);
		}
		
		response = "";
		
		try {
			// solution for parsing inputstream to utf8 stolen here
			// http://stackoverflow.com/questions/4964640/reading-inputstream-as-utf-8
			
			is = conn.getInputStream();
			
			br = new BufferedReader(new InputStreamReader(is, TELEGRAM_CHARSET));
			String s;
			
			while ((s = br.readLine()) != null) {
				response += s;
			}
			is.close();
		} catch (IOException e){
			throw new IOException("Could not get inputstream from Telegram:\n\n" + e);
		}
		
		return response;
	}
	
	/**
	 * Extract the instruction from the Telegram message
	 * 
	 * @param s the json string to process
	 * @return string of message if successfully parsed, or null if parse couldn't be completed
	 * 
	 * TODO Probably should be moved to TelegramTaxi class.
	 * TODO Need to update static chat_id if it changes. Check for every message, and first make sure messages are ordered chronologically
	 * 
	 */
	public static Vector<TelegramMessage> extractMessages(String s) {
		Vector<TelegramMessage> messages;
	
		ObjectMapper o = new ObjectMapper();
		JsonNode jn;
		
		try {
			jn = o.readValue(s, JsonNode.class);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Couldn't process json");
			return null;
		}
		
		messages = new Vector<TelegramMessage>();
		
		JsonNode j1 = jn.get("result");
		
		if (j1.isArray()) {
			for (final JsonNode j2 : j1) {
				//messages.put(j2.get("update_id").asInt(), j2.get("message").get("text").asText().toLowerCase());
				JsonNode mo = null;
				TelegramMessage message = new TelegramMessage();
				if (j2.get("message") != null) {
					mo = j2.get("message");
				} else if (j2.get("edited_message") != null) {
					mo = j2.get("edited_message");
				} else {
					continue;
				}
				message.setText(mo.get("text").asText());
				message.setChatId(mo.get("chat").get("id").asInt());
				message.setMessageId(mo.get("message_id").asInt());
				message.setUpdateId(j2.get("update_id").asInt());
				messages.add(message);
			}
		}
		
		return messages;
		
	}
	
	private static void loadConfig(String filename, HashMap<String, String> localconfig) throws TelegramInitException {
		BufferedReader br = null;
		if ("".equals(filename))
			filename = TELEGRAM_CONFIG_FILENAME;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			String line = null;
			try {
				while ((line = br.readLine()) != null) {
					String[] data = line.split("\t"); 
					if (data.length > 1) {
						if (data[0].equals("API_KEY"))
							Telegram.TELEGRAM_API_KEY = data[1];
						else if (data[0].equals("USER"))
							Telegram.TELEGRAM_USER = data[1];
						else
							localconfig.put(data[0], data[1]);
					}
				}
			} catch (IOException e) {
				throw new TelegramInitException("Cannot read from config");
			}
		} catch (FileNotFoundException e) {
			throw new TelegramInitException("Config file " + filename + " not found");
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					throw new TelegramInitException("Cannot read from config");	
				}
			}
		}
	}
}

package lash.telegram;

import java.lang.IllegalArgumentException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author lash
 * TODO make superclass for TelegramReply and TelegramInstruction
 * 
 */
public class TelegramInstruction {
	private TelegramMessage sourceMessage;
	private String action;
	private String value;
	Map <String, String> params;
	
	public TelegramInstruction() {
		sourceMessage = new TelegramMessage();
		clean();
	}
	
	public void set(String a, String v) throws IllegalArgumentException {
		try {
			action = a;
			value = v;
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		
	}
	
	/**
	 * Initializes the interpreted params set
	 * 
	 */
	public void clean() {
		params = new HashMap<String, String>();
	}
	
	/**
	 * Sets the source message "pointer"
	 * @param m source message
	 */
	public void setSource(TelegramMessage m) {
		this.sourceMessage = m;
	}
	

	/**
	 * Returns the source message uuid
	 */
	public TelegramMessage getSource() {
		return this.sourceMessage;
	}
	
	/**
	 * Sets the param key k to value v. If the key already exists, the existing value is overwritten
	 * @param k key
	 * @param v value
	 */
	public void setParam(String k, String v) {
		params.put(k, v);
	}
	
	/**
	 * @return set of keys of interpreted params
	 */
	public Set<String> getParamKeys() {
		return params.keySet();
	}
	
	/**
	 * Check if instruction has any interpreted params
	 * @return true if yes, false if no
	 */
	public boolean hasParam() {
		return params.size() > 0 ? true : false;
	}
	
	public String getParam(String key) {
		return params.get(key);
	}
	
	public String getAction() {
		return action;
	}

	public String getValue() {
		return value;
	}
	
	public int getUpdateId() {
		return sourceMessage.getUpdateId();
	}
	
	public String toString() {
		return String.format(	"action: %s\n" +
								"value: %s\n" +
								"sourceMessage: %s\n",
								action, value, sourceMessage.getId());
	}
}

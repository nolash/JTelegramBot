package lash.telegram;

import java.util.ArrayList;

import lash.telegram.TelegramMessage;


public interface TelegramInstructionHandler {

	TelegramReply reply_to_instruction = new TelegramReply();
	public ArrayList<TelegramReply> processInstruction(TelegramInstruction instruction);
	public TelegramInstruction parseMessage(String instruction, TelegramMessage args) throws TelegramParseException, TelegramIllegalStateException;
}

package lash.telegram;

import java.util.logging.Level;

import java.util.ArrayList;

public class TelegramQueueHandler extends Telegram implements Runnable {

		private boolean running;
		public Thread t;
		private TelegramInstruction current_instruction;
		private ArrayList<TelegramReply> current_replies;
		
		public TelegramQueueHandler() {
			t = new Thread(this, "QueueHandler");
			running = false;
			current_instruction = null;
			current_replies = new ArrayList<TelegramReply>();
			t.start();
		}
		
		public void run() {
			running = true;
			while (running) {
				if (current_instruction == null) {
					try {
						current_instruction = addRemoveInstructionsQueue(this, null, 1, false);
						if (current_instruction == null) {
							try {
								Thread.sleep(TELEGRAM_QUEUE_HANDLER_RETRY_DELAY);
								continue;
							} catch (InterruptedException e) {
						
							}
						} else {
							addRemoveInstructionsQueue(this, null, 1, true);
						}
					} catch (TelegramAccessException e) {
						logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' could not retrieve from instructionsqueue.");
					}
				} else if (current_replies.isEmpty()) {
					current_replies = instruction_callback.processInstruction(current_instruction);
					
				} else {
					try {
						TelegramReply reply = current_replies.get(0);
						reply = addRemoveRepliesQueue(this, reply, 0, false);
						
						if (reply == null) {
							try {
								Thread.sleep(TELEGRAM_QUEUE_HANDLER_RETRY_DELAY);
								continue;
							} catch (InterruptedException e) {
						}
						} else {
							current_replies.remove(0);
							if (current_replies.isEmpty()) {
								current_instruction = null;
							}
						}
					} catch (TelegramAccessException e) {
						logger.log(Level.WARNING, "Telegram  '" + t.getName() + "' could not insert into replyqueue.");
					}
				}
			}
		}
}

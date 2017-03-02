# TELEGRAM BOT FRAMEWORK

Multithreaded framework for building simple bots for the Telegram messenger.

## Usage

Currently only a small test implementation is included, a "secretary" that replies by citing messages, and sends a receipt to a different chat id (if specified).

## Configuration

Configuration file is tab separated and should include the following data:

* API_KEY
* USER
* RECEIPT_CHAT_ID

## Dependencies

The following dependencies are redistributed in the [lib](/lib directory):

* [https://github.com/FasterXML](jackson json parser)
	- [lib/LICENSE_jackson.txt](Apache License 2.0)
* [https://github.com/kohsuke/args4j](args4j) 
	- (for test/TelegramSecretary only)

## License

Distributed under the [LICENSE](MIT LICENSE)

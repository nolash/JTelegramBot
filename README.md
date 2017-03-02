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

The following dependencies are redistributed in the [/lib](lib)  directory:

* [jackson json parser](https://github.com/FasterXML)
	- [Apache License 2.0](lib/LICENSE_jackson.txt)
* [args4j](https://github.com/kohsuke/args4j) 
	- (for test/TelegramSecretary only)

## License

Distributed under the [MIT LICENSE](LICENSE)

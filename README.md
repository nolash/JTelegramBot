# TELEGRAM BOT FRAMEWORK

Multithreaded framework for building simple bots for the Telegram messenger.

This is alpha stage software and should be considered highly volatile.

## Usage

Currently only a small test implementation is included, a "secretary" that replies by citing messages, and sends a receipt to a different chat id (if specified).

![test output](http://swarm-gateways.net/bzzr:/3789f19736792add013fd64bdc76a67fbc186cb9eb01c9906f634c6f658eb6ee?content_type=image/png)

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

## Version

0.1.1

## Roadmap

* 0.1.2
	- Create generic minimal handlers for all message types

## Changelog

* 0.1.1
	- Added support for command line options

* 0.1.0
	- Initial framework

## License

Released under the [MIT LICENSE](LICENSE)

## Contact

BM-NBXpv4oRf2BkNLJBZAm2muikBsm6EzvW

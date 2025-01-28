# Oddsmarket client

For API documentation and integration manual please read the [Wiki](https://github.com/AspiraLimited/oddsmarket_client/wiki) section. 

This repository contains following modules:

## `api`

Source code of DTO classes for API v4.

## `client`

Source code of Java client for [oddsmarket.org](https://oddsmarket.org/) Websocket 
service and REST dictionaries.

## `demo-app-events`

Demo application which consumes data from Oddsmarket Events websocket feed and 
prints received data into STDOUT.
This application is intended to be used by oddsmarket customers as 
reference implementation of Events websocket feed consumption.  

## `demo-app-feedreader`

Demo application which consumes data from Oddsmarket Odds websocket feed and 
prints received updates into STDOUT in human-readable format.
This application is intended to be used by oddsmarket customers as 
reference implementation of websocket feed consumption.  

[More details](demo-app-feedreader/README.md)

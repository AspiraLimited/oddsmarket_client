# Oddsmarket client

For API documentation and integration manual please read the [Wiki](wiki/) section. 

This repository contains following modules:

## `api`

Source code of DTO classes for API v4.

## `client`

Source code of Java client for [oddsmarket.org](https://oddsmarket.org/) Websocket 
service and REST dictionaries.

## `demo-app-feedreader`

Demo application which consumes data from Oddsmarket websocket feed and 
prints received updates into STDOUT in human-readable format.
This application is intended to be used by oddsmarket customers as 
reference implementation of websocket feed consumption.  

[More details](demo-app-feedreader/README.md)

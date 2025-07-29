# TradingFeedReader demo application

**TradingFeedReader** is a Java command-line application which consumes data from Oddsmarket websocket feed and 
prints received updates into STDOUT in human-readable format.
The application contains usage examples of `client` module and is intended to be used by [oddsmarket](https://oddsmarket.org/) customers as 
reference implementation of:
* websocket feed consumption
* updating bookmaker events in an in-memory data structure
* updating outcomes state in an in-memory data structure
* composing human-readable outcome names
* composing human-readable period names


## Building and running

1. Download and install JDK 17. Recommended JDK 17 binaries are distributed from [adoptium.net](https://adoptium.net/temurin/releases/?version=17) website.
2. Clone this repository on your local filesystem. Follow [GitHub documentation](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository) if you need help with this step.
3. Build and run TradingFeedReader program (see commands syntax and examples below). Building phase is performed only at first run. Next runs build phase is skipped.

Command-line syntax (Linux, MacOS):
```
bash tradingfeedreader.sh {websocket trading feed domain} {your API key} {bookmaker ID} [{sport ID 1},{sport ID 2},..] 
```

Command-line syntax (Windows):
```
tradingfeedreader.cmd {websocket trading feed domain} {your API key} {bookmaker ID} [{sport ID 1},{sport ID 2},..] 
```

Command line parameters explanation:
* `{websocket trading feed domain}` - use `api-pr.oddsmarket.org` for prematch events and `api-lv.oddsmarket.org` for live events
* `{your API key}` - your oddsmarket API key 
* `{bookmaker ID}` - bookmaker ID you want to subscribe to. See [bookmakers dictionary](https://github.com/AspiraLimited/oddsmarket_client/wiki/Get-Bookmakers-(API-v4)) for list of all IDs. 
* `[{sport ID 1},{sport ID 2},..]` - optional list of comma-separated sport IDs. See [sports dictionary](https://github.com/AspiraLimited/oddsmarket_client/wiki/Get-Sports-(API-v4)) for list of all IDs.


Command line examples:
```
bash tradingfeedreader.sh api-lv.oddsmarket.org 00000000000000000000000000000000 1
```

```
tradingfeedreader.cmd api-lv.oddsmarket.org 00000000000000000000000000000000 1 8,16
```

Output status prefixes explanation:
* `[NEW]` - new bookmaker event
* `[DEL]` - bookmaker event removed
* `[UPD]` - bookmaker event updated
* `[ODDS]` - odds in next lines are referring to this bookmaker event 
* `  [NEW]` - new outcome 
* `  [UPD]` - updated outcome 
* `  [DEL]` - removed outcome 


Output example:
```
Initial state transferring
[NEW] Event name1 [Jul 21 15:25] id=410150751, sportId=13, plannedStartTimestamp=1753100700000, name='null', outcomeMap={}
[NEW] Event name2 [Jul 21 21:40] id=410150864, sportId=13, plannedStartTimestamp=1753123200000, name='null', outcomeMap={}
[NEW] Event name3 [Jul 21 18:35] id=410150850, sportId=13, plannedStartTimestamp=1753112100000, name='null', outcomeMap={}
Initial state transferred
[ODDS] Event name1 [Aug 15 03:30]
    [UPD] TU(1.0) for Team1 [2nd Half]: odds= 1.047
    [UPD] TO(1.0) for Team1 [2nd Half]: odds= 7.9
    [UPD] Team2 to win by exactly 3 goals - no [Regular time]: odds= 1.016
[ODDS] Event name2 [Jul 26 19:00]
    [UPD] TO(1.75) for Team1 [1st Half]: odds= 11.25
    [UPD] TU(1.75) for Team1 [1st Half]: odds= 1.008
...
```



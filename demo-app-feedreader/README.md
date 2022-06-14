# FeedReader demo application

**FeedReader** is a Java command-line application which consumes data from Oddsmarket websocket feed and 
prints received updates into STDOUT in human-readable format.
The application contains usage examples of `client` module and is intended to be used by [oddsmarket](https://oddsmarket.org/) customers as 
reference implementation of:
* websocket feed consumption
* updating bookmaker events in an in-memory data structure
* updating outcomes state in an in-memory data structure
* composing human-readable outcome names
* composing human-readable period names


## Building and running

1. Download and install JDK 11. Recommended JDK 11 binaries are distributed from [adoptium.net](https://adoptium.net/temurin/releases/?version=11) website.
2. Clone this repository on your local filesystem. Follow [GitHub documentation](https://docs.github.com/en/repositories/creating-and-managing-repositories/cloning-a-repository) if you need help with this step.
3. Build and run FeedReader program (see commands syntax and examples below). Building phase is performed only at first run. Next runs build phase is skipped.

Command-line syntax (Linux, MacOS):
```
bash feedreader.sh {websocket feed domain} {your API key} {bookmaker ID} [{sport ID 1},{sport ID 2},..] 
```

Command-line syntax (Windows):
```
feedreader.cmd {websocket feed domain} {your API key} {bookmaker ID} [{sport ID 1},{sport ID 2},..] 
```

Command line parameters explanation:
* `{websocket feed domain}` - use `api-pr.oddsmarket.org` for prematch events and `api-lv.oddsmarket.org` for live events
* `{your API key}` - your oddsmarket API key 
* `{bookmaker ID}` - bookmaker ID you want to subscribe to. See [bookmakers dictionary](https://github.com/AspiraLimited/oddsmarket_client/wiki/Get-Bookmakers) for list of all IDs. 
* `[{sport ID 1},{sport ID 2},..]` - optional list of comma-separated sport IDs. See [sports dictionary](https://github.com/AspiraLimited/oddsmarket_client/wiki/Get-Sports) for list of all IDs.


Command line examples:
```
bash feedreader.sh api-lv.oddsmarket.org 00000000000000000000000000000000 1
```

```
feedreader.cmd wss://api-lv.oddsmarket.org/v3/odds_ws 00000000000000000000000000000000 1 8,16
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
Info: Subscribed successfully: {"msg":{"sportIds":[24],"bookmakerIds":[11]},"cmd":"subscribed"}
Info: init fields: {"msg":{"BookmakerEvent":["id","bookmakerId","active","eventId","name","nameRu","swapTeams","currentScore","startedAt","sportId","sportName","leagueName","rawId","directLink","updatedAt","homeId","awayId","home","away","leagueId"],"Outcomes":["id","bookmakerEventId","periodId","periodIdentifier","periodName","marketAndBetTypeId","marketAndBetTypeParameterValue","playerId1","playerName1","playerId2","playerName2","active","odds","oddsLay","isLay","marketDepth","directLink","updatedAt"]},"cmd":"fields"}
[NEW] Ireland Women (w) - South Africa Women (w) [Jun 6 18:30] BookmakerEvent{id=631354344, bookmakerId=11, active=true, eventId=41822940, name='Ireland Women (w) - South Africa Women (w)', nameRu='null', home='Ireland Women (w)', away='South Africa Women (w)', homeId='71975', awayId='71795', swapTeams=false, currentScore='null', startedAt=1654529400, sportId=24, sportName='Cricket', leagueId='21581', leagueName='International. Womens International Twenty20 Matches', rawId='null', directLink='31504042', updatedAt=1654532852}
[ODDS] Ireland Women (w) - South Africa Women (w) [Jun 6 18:30]
    [NEW] Team1 Win [with overtime]: odds=17.5, marketDepth=1364.67, directLink=199895927|10108898|0.0|2561122.45|17.5
    [NEW] Team2 Win [with overtime] [LAY]: odds=1.05, oddsLay=21.0, marketDepth=70.97, directLink=199895927|10108898|0.0|2561122.45|17.5
    [NEW] X2 [regular time] [LAY]: odds=1.0476191, oddsLay=22.0, marketDepth=67.74, directLink=199895930|10108898|0.0|108.98|6.2
    [NEW] Team2 Win [with overtime]: odds=1.05, marketDepth=1419.32, directLink=199895927|8436313|0.0|2561122.45|1.05
    [NEW] 2 [regular time]: odds=1.05, marketDepth=1435.09, directLink=199895930|8436313|0.0|108.98|1.14
    [NEW] Team1 Win [with overtime] [LAY]: odds=17.666683, oddsLay=1.06, marketDepth=16132.26, directLink=199895927|8436313|0.0|2561122.45|1.05
    [NEW] X [regular time]: odds=100.0, marketDepth=13.29, directLink=199895930|2312392|0.0|108.98|55.0
    [NEW] 1X [regular time] [LAY]: odds=15.285704, oddsLay=1.07, marketDepth=410.09, directLink=199895930|8436313|0.0|108.98|1.14
    [NEW] 1 [regular time]: odds=17.5, marketDepth=1364.67, directLink=199895930|10108898|0.0|108.98|6.2
[ODDS] Ireland Women (w) - South Africa Women (w) [Jun 6 18:30]
    [UPD] Team1 Win [with overtime]: marketDepth: 1364.67->1360.89; directLink: 199895927|10108898|0.0|2561122.45|17.5->199895927|10108898|0.0|2561525.67|18.0
    [UPD] Team2 Win [with overtime] [LAY]: marketDepth: 70.97->64.94; directLink: 199895927|10108898|0.0|2561122.45|17.5->199895927|10108898|0.0|2561525.67|18.0
    [UPD] X2 [regular time] [LAY]: marketDepth: 67.74->61.99
    [UPD] Team2 Win [with overtime]: marketDepth: 1419.32->1298.78; directLink: 199895927|8436313|0.0|2561122.45|1.05->199895927|8436313|0.0|2561525.67|1.06
    [UPD] 2 [regular time]: marketDepth: 1435.09->1393.97
    [UPD] Team1 Win [with overtime] [LAY]: marketDepth: 16132.26->16050.87; directLink: 199895927|8436313|0.0|2561122.45|1.05->199895927|8436313|0.0|2561525.67|1.06
    [UPD] 1X [regular time] [LAY]: marketDepth: 410.09->410.11
    [UPD] 1 [regular time]: marketDepth: 1364.67->1360.89
[ODDS] Ireland Women (w) - South Africa Women (w) [Jun 6 18:30]
    [UPD] Team1 Win [with overtime]: marketDepth: 1360.89->1362.21; directLink: 199895927|10108898|0.0|2561525.67|18.0->199895927|10108898|0.0|2562043.12|17.5
    [UPD] Team2 Win [with overtime] [LAY]: marketDepth: 64.94->59.74; directLink: 199895927|10108898|0.0|2561525.67|18.0->199895927|10108898|0.0|2562043.12|17.5
    [UPD] X2 [regular time] [LAY]: marketDepth: 61.99->57.02
    [UPD] Team2 Win [with overtime]: marketDepth: 1298.78->1194.75; directLink: 199895927|8436313|0.0|2561525.67|1.06->199895927|8436313|0.0|2562043.12|1.05
    [UPD] 2 [regular time]: marketDepth: 1393.97->1289.94
    [UPD] Team1 Win [with overtime] [LAY]: marketDepth: 16050.87->16073.61; directLink: 199895927|8436313|0.0|2561525.67|1.06->199895927|8436313|0.0|2562043.12|1.05
    [UPD] 1X [regular time] [LAY]: marketDepth: 410.11->410.12
    [UPD] 1 [regular time]: marketDepth: 1360.89->1362.21
[ODDS] Ireland Women (w) - South Africa Women (w) [Jun 6 18:30]
    [UPD] Team1 Win [with overtime]: odds: 17.5->21.0; marketDepth: 1362.21->29.42; directLink: 199895927|10108898|0.0|2562043.12|17.5->199895927|10108898|0.0|2564510.43|21.0
    [UPD] Team2 Win [with overtime] [LAY]: odds: 1.05->1.0454545; oddsLay: 21.0->23.0; marketDepth: 59.74->4.37; directLink: 199895927|10108898|0.0|2562043.12|17.5->199895927|10108898|0.0|2564510.43|21.0
    [UPD] X2 [regular time] [LAY]: odds: 1.0476191->1.0434783; oddsLay: 22.0->24.0; marketDepth: 57.02->4.18
    [UPD] Team2 Win [with overtime]: odds: 1.05->1.04; marketDepth: 1194.75->19196.01; directLink: 199895927|8436313|0.0|2562043.12|1.05->199895927|8436313|0.0|2564510.43|1.05
    [UPD] 2 [regular time]: marketDepth: 1289.94->381.93
    [UPD] Team1 Win [with overtime] [LAY]: odds: 17.666683->21.00002; oddsLay: 1.06->1.05; marketDepth: 16073.61->588.46; directLink: 199895927|8436313|0.0|2562043.12|1.05->199895927|8436313|0.0|2564510.43|1.05
    [UPD] 1X [regular time] [LAY]: odds: 15.285704->17.666683; oddsLay: 1.07->1.06; marketDepth: 410.12->410.08
    [UPD] 1 [regular time]: odds: 17.5->21.0; marketDepth: 1362.21->29.42
...
```



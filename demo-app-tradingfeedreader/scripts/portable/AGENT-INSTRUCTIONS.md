# TradingFeedReader — instructions for an AI agent

This portable bundle is a JVM-packaged trading-feed recorder. Use it to capture a
sample of the OddsMarket trading feed for a bounded period of time, then read the
structured output to answer questions.

When in doubt, run `RUN_ME.bat --help` for the authoritative flag list.

## Invocation

On Windows:

```
RUN_ME.bat --feedDomain=<host> --tradingFeedId=<n> [options...]
```

If your shell doesn't handle `.bat` well, the underlying executable accepts the
same flags directly:

```
TradingFeedReader.exe --feedDomain=<host> --tradingFeedId=<n> [options...]
```

**All parameters are named flags (`--name=value`). There are no positional arguments.**

### Required flags

- `--feedDomain=<host>` — `api-pr.oddsmarket.org` (prematch) or `api-lv.oddsmarket.org` (live)
- `--tradingFeedId=<n>` — numeric Trading Feed ID

### Authentication

Default: read the API key from `api-token.txt` in the current directory.

Other ways (in priority order):

- `--apiKey=<value>` — literal key on the command line
- `--apiKeyFile=<path>` — read from a custom path

### Run control (CRITICAL for agent use)

Without one of these, the tool runs forever (until Ctrl+C). **Always pass one** when
invoking from an agent:

- `--duration=<30s|5m|1h>` — stop gracefully after this much wall-clock time
- `--maxMessages=<n>` — stop after this many messages recorded to disk

Both can be combined; whichever triggers first wins. Either causes a clean
shutdown (final summary printed, `summary.json` written, exit code `0`).

### Other useful flags

- `--saveMessagesToFolder=<path>` — output folder (default `./data`)
- `--sportIds=<id1,id2,...>` — narrow the subscription
- `--recordOnlyEventIds=<id1,id2,...>` — strict filter: only record these OddsMarket event IDs
- `--recordOnlyRawEventIds=<id1,id2,...>` — same but matches the bookmaker's `rawEventId`
- `--locales=<en,ru,...>` — locale codes

## Output

After a run, look inside `<saveMessagesToFolder>/tradingFeedSessionData/`:

| File                     | Use for                                                              |
|--------------------------|----------------------------------------------------------------------|
| `summary.json`           | **Read this first.** Machine-readable outcome of the whole run.      |
| `messagesIndex.jsonl`    | One JSON per line. Grep/jq friendly index of every recorded message. |
| `messages/`              | One pretty-printed JSON file per server message.                     |
| `subscriptionInfo.json`  | Session config (API key is NOT included).                            |
| `subscriptionStats.json` | Final live counters and event lists.                                 |

### `summary.json` schema

```json
{
  "schemaVersion": 1,
  "startedAt": "2026-05-21T...",
  "endedAt": "2026-05-21T...",
  "durationSeconds": 300,
  "exit": {
    "code": 0,
    "reason": "duration_limit",
    "fatalErrorCode": null
  },
  "sessionId": "abc-123-def",
  "initialSyncCompleted": true,
  "sessionFolder": "C:\\...\\tradingFeedSessionData",
  "messagesSeen": {
    "total": 12345,
    "byType": {
      "eventPatch": 12000,
      ...
    }
  },
  "messagesRecorded": {
    "total": 4502,
    "percentOfSeen": 36.5,
    "byType": {
      ...
    }
  },
  "activeEventsAtEnd": 187,
  "distinctEventsSeen": 342,
  "filter": {
    "active": true,
    "recordOnlyEventIds": [
      ...
    ],
    "recordOnlyRawEventIds": [
      ...
    ]
  },
  "limits": {
    "durationSeconds": 300,
    "maxMessages": null
  }
}
```

`exit.reason` is one of `ctrl_c`, `duration_limit`, `max_messages_limit`, `fatal_error`.

### Messages index (Important for quick search)

There is a `messagesIndex.jsonl` file, each line containing a JSON object:

```json
{
  "messageId": 42,
  "type": "eventPatch",
  "eventId": 12345,
  "arrivalTimestamp": "2026-05-21T...",
  "fileName": "42_12345_event_patch.json",
  "sizeBytes": 1834
}
```

`type` is one of `sessionStart`, `eventSnapshot`, `eventPatch`, `eventsRemoved`,
`initialSyncComplete`, `heartbeat`, `errorMessage`, `payloadNotSet`. `eventId` is
`null` for messages that don't relate to a single event.

That is invaluable for quick search in which files there are messages about a specific event or messages of a specific
type.

### Individual message files (`messages/<filename>.json`)

Each file wraps one `ServerMessage` (converted from protobuf):

```json
{
  "arrivalTimestamp": "2026-05-21T...",
  "content": {
    "messageId": 42,
    "eventSnapshot": {
      ...
    }
  }
}
```

Default filename pattern: `<messageId>_<eventId>_<type>.json` (or
`<messageId>_<type>.json` for non-event messages). Sorting by filename gives
chronological order.

## Exit codes

| Code | Meaning                                                                                  |
|------|------------------------------------------------------------------------------------------|
| `0`  | Graceful stop (Ctrl+C, `--duration`, or `--maxMessages`)                                 |
| `1`  | Unexpected runtime error before recording started                                        |
| `2`  | Fatal subscription error (`AUTHENTICATION_FAILED`, `SUBSCRIPTION_FAILED`, `BAD_REQUEST`) |
| `3`  | Invalid CLI arguments                                                                    |

## Typical agent recipes

### "Record 2 minutes and report what came in"

```
RUN_ME.bat --feedDomain=api-lv.oddsmarket.org --tradingFeedId=500 --duration=2m
```

Then read `summary.json` for the verdict; for details, `messagesIndex.jsonl`.

### "Are there updates on event 12345 right now?"

```
RUN_ME.bat --feedDomain=api-pr.oddsmarket.org --tradingFeedId=500 \
  --recordOnlyEventIds=12345 --duration=2m
```

`summary.json` → `messagesRecorded.total == 0` means no updates arrived in that window.

### "Capture a sample, capped at 1000 messages"

```
RUN_ME.bat --feedDomain=api-lv.oddsmarket.org --tradingFeedId=500 --maxMessages=1000 --duration=10m
```

The 10-minute cap is a safety net in case the feed is slow and `maxMessages` is never reached.

## Notes / pitfalls

- Set the API key once via `api-token.txt`; don't pass it via `--apiKey=` on the
  shell (it would end up in shell history).
- The session folder is **wiped and recreated** at the start of every run. To keep
  prior data, change `--saveMessagesToFolder` to a unique path per run.
- `--recordOnlyRawEventIds` requires `--rawIdOriginBookmakerId=<n>` to also be set,
  otherwise the server doesn't include `rawEventId` in messages.

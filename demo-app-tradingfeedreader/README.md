# TradingFeedReader demo application

**TradingFeedReader** is a Java command-line tool that subscribes to an OddsMarket
[trading feed](https://github.com/AspiraLimited/oddsmarket_client/wiki) over a websocket
and **records every incoming message to disk** as pretty-printed JSON, along with structured
metadata and a final session summary.

## Table of contents

- [Quick start (interactive mode)](#quick-start-interactive-mode)
- [Quick start (CLI flags)](#quick-start-cli-flags)
- [CLI reference](#cli-reference)
- [Output: the session folder](#output-the-session-folder)
- [Exit codes](#exit-codes)
- [Building from source](#building-from-source)
- [Portable Windows build (no JDK required by the user)](#portable-windows-build-no-jdk-required-by-the-user)
- [Using from an AI agent](#using-from-an-ai-agent)

## Quick start (interactive mode)

From the repository root:

```bash
# Linux / macOS
bash tradingfeedreader.sh --interactive

# Windows
tradingfeedreader.cmd --interactive
```

The tool will prompt you for all parameters one by one. Each prompt shows its default in
the form `[optional](default - X) Question:`; just press Enter to accept the default.

Minimum required answers:

- **`oddsmarket domain`** — `live`, `prematch`, or a full hostname like `api-pr.oddsmarket.org`
- **`API key`** — leave the file prompt empty to read it from `./api-token.txt`, or type `paste` to enter the key directly
- **`Trading Feed ID`** — the numeric ID of the trading feed you have access to

Everything else has a sensible default (data is saved to `./data/tradingFeedSessionData/`).

Press **Ctrl+C** to stop recording. A summary is printed and saved to disk.

## Quick start (CLI flags)

All parameters are named flags (`--name=value`). There are **no positional arguments**.

```bash
# Minimal: 2 required flags, everything else uses defaults
bash tradingfeedreader.sh \
  --feedDomain=api-lv.oddsmarket.org \
  --tradingFeedId=500

# Record for 2 minutes and exit
bash tradingfeedreader.sh \
  --feedDomain=api-lv.oddsmarket.org \
  --tradingFeedId=500 \
  --duration=2m

# Record only a specific event for 5 minutes
bash tradingfeedreader.sh \
  --feedDomain=api-pr.oddsmarket.org \
  --tradingFeedId=500 \
  --recordOnlyEventIds=12345 \
  --duration=5m
```

By default the API key is read from `./api-token.txt`. Don't have one? Create it (gitignored)
or pass `--apiKey=...` / `--apiKeyFile=...` explicitly.

## CLI reference

### Required

| Flag | Description |
| --- | --- |
| `--feedDomain=<host>` | e.g. `api-pr.oddsmarket.org` (prematch) or `api-lv.oddsmarket.org` (live) |
| `--tradingFeedId=<n>` | Numeric Trading Feed ID provided by OddsMarket |

### Subscription options

| Flag | Default | Description |
| --- | --- | --- |
| `--sportIds=<id1,id2,...>` | all sports | Filter the subscription by sport IDs |
| `--locales=<en,ru,...>` | `en` | Comma-separated locale codes |
| `--rawIdOriginBookmakerId=<n>` | not set | Include the bookmaker's own `rawEventId` in each message |
| `--fillRawOutcomeId=true\|false` | server default | Include `rawOutcomeId` in outcomes |
| `--fillDirectLink=true\|false` | server default | Include `directLink` URLs in event metadata |

### Authentication (priority order)

| Flag / source | Description |
| --- | --- |
| `--apiKey=<value>` | Literal API key. Highest priority. |
| `--apiKeyFile=<path>` | Read the key from this file. |
| _default_ | If neither flag is set, the tool reads `./api-token.txt`. The repository's `.gitignore` already excludes this filename. |

The key is **never** written to `subscriptionInfo.json` or any output file. On startup
the tool prints which source it used (file path or `--apiKey option`), without revealing the key.

### Recording options

| Flag | Default | Description |
| --- | --- | --- |
| `--saveMessagesToFolder=<path>` | `./data` | Parent folder for the session subfolder |
| `--groupMessagesByEvent=true\|false` | `false` | Prefix message filenames with `eventId` (easier to scan in Explorer when investigating one event) |
| `--recordOnlyEventIds=<id1,id2,...>` | record all | **Strict filter**: only OddsMarket event IDs in this set are recorded. Non-event messages (`sessionStart`, `heartbeat`, ...) are dropped. |
| `--recordOnlyRawEventIds=<id1,id2,...>` | record all | Same as above but matches the bookmaker's `rawEventId`. Requires `--rawIdOriginBookmakerId` to also be set, otherwise the server doesn't include `rawEventId` in messages. |

### Run control

| Flag | Default | Description |
| --- | --- | --- |
| `--duration=<30s\|5m\|1h>` | run until Ctrl+C | Stop gracefully after this much time. Format: `<number><s\|m\|h>`. |
| `--maxMessages=<n>` | unlimited | Stop gracefully after this many messages have been recorded **to disk** (counted after the filter, if any). |

When a limit fires the tool prints a one-line reason, runs the same shutdown path as Ctrl+C
(close recorder, write final summary), and exits with code `0`.

## Output: the session folder

After a run, your `--saveMessagesToFolder` parent directory (default `./data`) contains:

```
data/
└── tradingFeedSessionData/
    ├── subscriptionInfo.json     # session config 
    ├── subscriptionStats.json    # live-updated stats, refreshed every 5s and on shutdown
    ├── summary.json              # machine-readable final outcome (see below)
    ├── messagesIndex.jsonl       # one line per recorded message (id, type, eventId, file, size)
    └── messages/
        ├── 12_42_session_start.json
        ├── 13_42_event_snapshot.json
        ├── 14_42_event_patch.json
        └── ...
```

The `tradingFeedSessionData/` subfolder is **wiped and recreated** at the start of every
session — only the contents of this exact subfolder are deleted, not the parent
`--saveMessagesToFolder`.

### Individual message files

Each file in `messages/` contains a single server message wrapped in an envelope:

```json
{
  "arrivalTimestamp": "2026-05-21T12:00:00.123Z",
  "content": {
    "messageId": 42,
    "eventSnapshot": { ... }
  }
}
```

The protobuf `ServerMessage` is converted to pretty-printed JSON, with all fields included
(zero/empty values are not omitted) so you can see the exact schema at a glance.

**Filename pattern** (default, `--groupMessagesByEvent=false`):
```
<messageId>_<eventId>_<type>.json   # for event_snapshot / event_patch
<messageId>_<type>.json             # for session_start / heartbeat / events_removed / ...
```

With `--groupMessagesByEvent=true`:
```
<eventId>_<messageId>_<type>.json   # event_id first, so all files for one event sort together
```

### `messagesIndex.jsonl`

One JSON object per line. Lightweight index for grepping / scripting:

```json
{"messageId":42,"type":"eventSnapshot","eventId":12345,"arrivalTimestamp":"2026-05-21T12:00:00.123Z","fileName":"42_12345_event_snapshot.json","sizeBytes":1834}
{"messageId":43,"type":"eventPatch","eventId":12345,"arrivalTimestamp":"2026-05-21T12:00:00.456Z","fileName":"43_12345_event_patch.json","sizeBytes":612}
```

Useful for `grep`, `jq`, or VS Code `Ctrl+F`-by-eventId.

### `subscriptionStats.json`

Updated every 5 seconds and on shutdown. Contains live counters, last processed message ID,
current active event list, and the full list of events ever seen in this session. Open it
while the tool is running to monitor progress without parsing thousands of message files.

## `summary.json`

Written once, at shutdown. Machine-readable final outcome of the session:

```json
{
  "schemaVersion": 1,
  "startedAt": "2026-05-21T14:50:00.123Z",
  "endedAt": "2026-05-21T14:55:00.456Z",
  "durationSeconds": 300,
  "exit": {
    "code": 0,
    "reason": "duration_limit",
    "fatalErrorCode": null
  },
  "sessionId": "abc-123-def",
  "initialSyncCompleted": true,
  "sessionFolder": "C:\\path\\to\\data\\tradingFeedSessionData",
  "messagesSeen":    { "total": 12345, "byType": { "eventPatch": 12000, ... } },
  "messagesRecorded":{ "total": 4502,  "percentOfSeen": 36.5, "byType": { ... } },
  "activeEventsAtEnd": 187,
  "distinctEventsSeen": 342,
  "filter": {
    "active": true,
    "recordOnlyEventIds": [12345, 67890],
    "recordOnlyRawEventIds": []
  },
  "limits": { "durationSeconds": 300, "maxMessages": null }
}
```

`exit.reason` is one of `ctrl_c`, `duration_limit`, `max_messages_limit`, `fatal_error`, `writer_queue_overflow`.

## Exit codes

| Code | Meaning |
| --- | --- |
| `0` | Graceful stop (Ctrl+C, `--duration` elapsed, or `--maxMessages` reached) |
| `1` | Unexpected runtime error before the recording session started |
| `2` | Fatal subscription error: `AUTHENTICATION_FAILED`, `SUBSCRIPTION_FAILED`, or `BAD_REQUEST` |
| `3` | Invalid CLI arguments (or no arguments and not in interactive mode) |
| `4` | Writer queue overflow — disk I/O could not keep up with the incoming feed |

The exit code is enforced via `Runtime.halt()` from the shutdown hook, so it is deterministic
even for Ctrl+C (would otherwise be 130 on Unix).

## Building from source

Requirements:

- **JDK 11+** to compile and run (the project sets `<release>11</release>` for the compiler)
- The provided Maven Wrapper takes care of the Maven version

The launcher scripts build automatically on first run:

```bash
# Linux / macOS
bash tradingfeedreader.sh --feedDomain=api-pr.oddsmarket.org --tradingFeedId=500

# Windows
tradingfeedreader.cmd --feedDomain=api-pr.oddsmarket.org --tradingFeedId=500
```

To build only (without running):

```bash
./mvnw -pl demo-app-tradingfeedreader -am clean package -DskipTests
```

The fat jar appears at:
```
demo-app-tradingfeedreader/target/demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar
```

## Portable Windows build (no JDK required by the user)

For distributing the tool to QA / PM who don't want to install Java, build a self-contained
portable zip using [`jpackage`](https://docs.oracle.com/en/java/javase/17/jpackage/).

### Build

From the repository root (Windows PowerShell):

```powershell
.\demo-app-tradingfeedreader\scripts\build-portable-exe.ps1
```

The script:

1. Cleans any previous `./packaging/` directory.
2. Builds the fat jar via the Maven Wrapper.
3. Runs `jpackage --type app-image` with `--win-console` so the tool keeps its console
   output (banner, summary, errors).
4. Copies `RUN_ME.bat` (a small launcher) into the packaged folder.
5. Bundles the resulting `TradingFeedReader/` folder (containing `RUN_ME.bat`,
   `TradingFeedReader.exe`, `app/`, and `runtime/` — i.e., an embedded JRE) into
   `./packaging/TradingFeedReader-portable.zip`.

## Using from an AI agent

Designed-in features that make this tool agent-friendly:

- **All parameters are named** (`--name=value`) — no positional args means an agent can
  inspect any invocation and immediately understand what each value means.
- **`--help`** is on the roadmap; for now, run with no arguments to see the same usage block.
- **Bounded runs** via `--duration` and `--maxMessages` — no need for the agent to send
  Ctrl+C, the tool stops itself.
- **`summary.json`** — one file the agent reads to know everything about the run outcome
  (counts, exit reason, filters, duration).
- **Deterministic exit codes** — `0` graceful, `2` fatal subscription error, `3` bad args.

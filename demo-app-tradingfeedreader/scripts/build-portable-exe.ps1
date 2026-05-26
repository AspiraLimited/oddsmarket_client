$AppName = "TradingFeedReader"
$AppVersion = "1.0.0"
$JarName = "demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar"
$MainClass = "com.aspiralimited.oddsmarket.client.demo.tradingfeedreader.TradingFeedReader"

Remove-Item .\packaging -Recurse -Force -ErrorAction SilentlyContinue

.\mvnw.cmd -pl demo-app-tradingfeedreader -am clean package -DskipTests -s demo-app-tradingfeedreader\settings-public.xml

New-Item -ItemType Directory -Force -Path .\packaging\input | Out-Null
New-Item -ItemType Directory -Force -Path .\packaging\dist | Out-Null

Copy-Item ".\demo-app-tradingfeedreader\target\$JarName" ".\packaging\input\$JarName"

jpackage `
  --type app-image `
  --name $AppName `
  --app-version $AppVersion `
  --dest .\packaging\dist `
  --input .\packaging\input `
  --main-jar $JarName `
  --main-class $MainClass `
  --arguments "--interactive" `
  --win-console `
  --vendor "Aspira" `
  --description "Portable Trading Feed Reader"

# Bundle launcher + user docs + agent docs next to the .exe.
# - RUN_ME.bat wraps the .exe with a `pause` so the console window stays open after exit.
# - HOW-TO-USE.txt is a short human-facing guide for QA / PM end-users.
# - AGENT-INSTRUCTIONS.md is a structured reference for AI agents (flags, output schema, recipes).
$PortableSrc = ".\demo-app-tradingfeedreader\scripts\portable"
$PortableDst = ".\packaging\dist\$AppName"
Copy-Item "$PortableSrc\RUN_ME.bat"             "$PortableDst\RUN_ME.bat"
Copy-Item "$PortableSrc\HOW-TO-USE.txt"         "$PortableDst\HOW-TO-USE.txt"
Copy-Item "$PortableSrc\AGENT-INSTRUCTIONS.md"  "$PortableDst\AGENT-INSTRUCTIONS.md"

Compress-Archive `
  -Path ".\packaging\dist\$AppName" `
  -DestinationPath ".\packaging\$AppName-portable.zip" `
  -Force

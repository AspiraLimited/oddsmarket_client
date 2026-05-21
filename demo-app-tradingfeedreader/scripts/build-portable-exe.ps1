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

# Bundle a .bat launcher next to the .exe.
# Users should double-click RUN_ME.bat (not the .exe directly) so that the console window
# stays open after the tool exits — otherwise Windows closes the window the instant the
# process ends and the final summary / any error messages are not visible.
Copy-Item ".\demo-app-tradingfeedreader\scripts\portable\RUN_ME.bat" ".\packaging\dist\$AppName\RUN_ME.bat"

Compress-Archive `
  -Path ".\packaging\dist\$AppName" `
  -DestinationPath ".\packaging\$AppName-portable.zip" `
  -Force

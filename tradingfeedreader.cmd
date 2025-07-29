IF NOT EXIST ".\demo-app-tradingfeedreader\target\demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar" (
  mvnw.cmd clean package -s demo-app-tradingfeedreader/settings-public.xml
)

java -jar ./demo-app-tradingfeedreader/target/demo-app-tradingfeedreader-1.0-SNAPSHOT-jar-with-dependencies.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
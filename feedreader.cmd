IF NOT EXIST ".\demo-app-feedreader\target\demo-app-feedreader-1.0-SNAPSHOT-jar-with-dependencies.jar" (
  mvnw.cmd clean package -s demo-app-feedreader/settings-public.xml
)

java -jar ./demo-app-feedreader/target/demo-app-feedreader-1.0-SNAPSHOT-jar-with-dependencies.jar %1 %2 %3 %4 %5 %6 %7 %8 %9
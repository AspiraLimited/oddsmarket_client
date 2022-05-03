# oddsmarket_client

This manual describes process of releasing oddsmarket Java client library JAR on Github Packages service.

## 
Add repository configuration to Maven settings file located at `~/.m2/settings.xml`

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                          https://maven.apache.org/xsd/settings-1.0.0.xsd">
      <localRepository/>
      <interactiveMode/>
      <offline/>
      <pluginGroups/>
<servers>
  <server>
    <id>github-oddsmarket_client</id>
    <password>....</password>
  </server>
</servers>
      <mirrors/>
      <proxies/>
      <profiles/>
      <activeProfiles/>
</settings>
```

In the "password" property insert personal token which can be generated on following page: https://github.com/settings/tokens

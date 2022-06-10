# Releasing oddsmarket Java client library


**This is internal documentation for oddsmarket developers.**

The manual describes process of releasing oddsmarket Java client library JAR 
on Github Packages service and creating release on GitHub.

## Configuring maven

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

## Publishing


1. Make required modifications to client, commit all of them to master
2. Assign new `client` module version (multiple `pom.xml` edits). Version assignment rules are explained below. 
3. Run `./deploy.sh`
4. Go to [GitHub releases](https://github.com/AspiraLimited/oddsmarket_client/releases), create release with 
tag that equals to client version (fill release notes). [Help on release publishing](https://docs.github.com/en/repositories/releasing-projects-on-github/managing-releases-in-a-repository).

## Client version assignment rules

Client versions layout:

`API version`.`Major version`.`Minor version`

Where:

### `API version` 

Version that is used in `/v(\d+)/` part of API endpoints.

### `Major version` 

Set to 0 after API version change. Must be incremented on breaking client changes. 

### `Minor version`

Set to 0 after major version change. Must be incremented on non-breaking client changes and bugfixes.
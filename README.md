# JMeter local plugins manager
Intranet Plugin Manager for JMeter - Periodically downloads plugins from the internet.

## Motive
In situations where specific hosts lack internet access, this tool enables the team to establish a dedicated server for managing plugins within the organization.

## Required Components
1. Java 8 or above

## Creating Properties file
create a properties file with the below contents.

```sh
server.port=2222
server.uri.path=/v1

# Scheduler interval (in ms)
scheduler.interval=86400000

# DB Connection Pool configuration
db.min.threads=2
db.max.threads=10
db.timeout.secs=300

# External APIs
jmeter.plugins.url=https://jmeter-plugins.org/repo/
mvn.repo.url=https://mvnrepository.com/search?q=

# Directory Configs
local.repo.path=/app/plugins-manager/

# Uncomment the below if you are running this on Windows
local.repo.path=C:\\Temp\\plugins-manager\\
```

## Available APIs

| Service            | HTTP Method | URI                                     |
|:-------------------|:-----------:|:----------------------------------------|
| App Running Status |     GET     | http://<hostname/IP>:\<port>/v1/        |
| Get Plugins        |     GET     | http://<hostname/IP>:\<port>/v1/plugins |
| Upload Custom Plugin      |     GET     | http://<hostname/IP>:\<port>/v1/upload  |


## How to Set up

* Download the Latest release from [here](https://github.com/rollno748/Jmeter-local-plugins-manager/tags)
* Create `configuration.properties` file
* Run the JAR (java -jar jmeter-local-plugins-manager-2.0.jar -c configuration.properties)
* Go to the JMeter installed directory and set `jpgc.repo.address` (this should be the local plugins manager API) in the jmeter.properties

## Features
1. Downloads plugins and their associated dependent libraries to local storage.
2. Stores all information in an SQLITE DB.
3. Provides a UI to upload custom plugins restricted to sharing within the organization.
4. Creates and exposes a modified API to retrieve plugin info for JMeter Plugin Manager.
5. Easily configurable scheduler to check for newly available plugins/versions in the market.

## Uploading Custom plugin
![Custom Upload Form](/img/upload-form.jpg)

## How it works ?

* Acts as an independent server that polls the plugins manager for updates (configurable).
* Creates the required directories to store plugins and their dependencies locally.
* Checks permissions on local directories before storing files.
* Exposes 3 APIs in the intranet:
    - Public plugins API
    - Custom Plugins API
    - Merged (Public and Custom) Plugins API

## Tools used
- Spark java framework
- Sqlite DB


## 💲 Support Me
<!-- [<a href="https://www.buymeacoffee.com/rollno748" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="45px" width="162px" alt="Buy Me A Coffee"></a>](https://www.buymeacoffee.com/rollno748) -->
If this project help you reduce time to develop, you can give me a cup of coffee :)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://ko-fi.com/rollno748)

Please rate a star(:star2:) - If you like it.

Please open up a bug(:beetle:) - If you experience abnormalities.


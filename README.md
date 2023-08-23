# Jmeter-local-plugins-manager
Intranet Plugin Manager for JMeter - which downloads the plugins from internet periodically.

## Motive 
Some organization will not have/provide access to internet on certain hosts - This will enable the team to create an own server to manage the plugins

## Features
1. Downloads the plugins, and it's associated dependent libraries to local storage.
2. Stores all the information to SQLITE DB.
3. Enables a UI to upload custom plugin which are restricted to share outside of organization.
4. Creates and exposes modified API with to get the Plugins info for Jmeter Plugin manager 
5. Easily configurable scheduler to check for the newly available plugins/versions in the market.


## Required Components
1. Java 8 or above

## Architecture


## How to Set up

* Download the source code and compile or Download the releases
* Create config to override configuration for application 
* Run the jar (java -jar jmeter-local-plugins-manager-2.0.jar)


## Available APIs

| Service            | HTTP Method | URI                                    |
|:-------------------|:-----------:|:---------------------------------------|
| App Running Status |     GET     | http://<hostname/IP>:<port>/v1/        |
| Upload Plugin      |     GET     | http://<hostname/IP>:<port>/v1/upload  |
| Get Plugins        |     GET     | http://<hostname/IP>:<port>/v1/plugins |


## Uploading Custom plugin
![Custom Upload Form](/img/upload-form.jpg)


## How it works ?

* Its acts as an independent server which polls plugins manager for update (which is configurable) 
* It creates the required directories to store the plugins and its dependencies to the local 
* It checks the permission on the local directories before storing the files
* Exposes 3 APIs in intranet 
 - Public plugins api
 - Custom Plugins api
 - Merged (Public and Custom) Plugins api

## Tools used
- Spark java framework
- Sqlite DB


## 💲 Support Me
<!-- [<a href="https://www.buymeacoffee.com/rollno748" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-yellow.png" height="45px" width="162px" alt="Buy Me A Coffee"></a>](https://www.buymeacoffee.com/rollno748) -->
If this project help you reduce time to develop, you can give me a cup of coffee :)

[![paypal](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://ko-fi.com/rollno748)

Please rate a star(:star2:) - If you like it.

Please open up a bug(:beetle:) - If you experience abnormalities.
 

# Jmeter-local-plugins-manager
Intranet Plugin Manager for Jmeter to avoid downloading plugins from Internet

## Features
1. Downloads the plugins and dependent libraries to local repository
2. Creates and exposes modified API with to get the Plugins info for Jmeter 
3. Creates periodic backup before update

## How it works ?

* Its acts as an independent server which polls plugins manager for update (which is configurable) 
* It creates the required directories to store the plugins and its dependencies to the local 
* It checks the permission on the local directories before storing the files
* Exposes 3 APIs in intranet 
 - Public plugins api
 - Custom Plugins api
 - Merged (Public and Custom) Plugins api



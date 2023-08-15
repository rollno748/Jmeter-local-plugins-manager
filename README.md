# Jmeter-local-plugins-manager
Intranet Plugin Manager for JMeter - which downloads the plugins from internet periodically.

## Motive 
Some organization will not have/provide access to internet on certain hosts - This will enable the team to create a own server to manage the plugins

## Features
1. Downloads the plugins and it's associated dependent libraries to local storage.
2. Stores all the information to SQLITE DB.
3. Enables a UI to upload custom plugin which are restricted to share outside of organization.
4. Creates and exposes modified API with to get the Plugins info for Jmeter Plugin manager 
5. Easily configurable scheduler to check for the newly available plugins/versions in the market.

## Architecture


## How to Setup

## Available APIs


## How it works ?


* Its acts as an independent server which polls plugins manager for update (which is configurable) 
* It creates the required directories to store the plugins and its dependencies to the local 
* It checks the permission on the local directories before storing the files
* Exposes 3 APIs in intranet 
 - Public plugins api
 - Custom Plugins api
 - Merged (Public and Custom) Plugins api

* Steps
 - ~~Create sqlite DB~~
 ~~- Create table~~ 
 ~~- Create a webserver for accessing jars~~
 - ~~Download plugins with threadpool executor~~ (plugins and dependencies separately) service
 - ~~Serve static form page for uploading custom plugins~~
 - Build a custom json for a combined information
 


## Tools used
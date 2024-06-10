# Welcome to your new project!


This is ZIO, Scala.js and Laminar project.

## Pre-requisites

- JDK
- sbt
- Node.js

Decent vesions of JDK, sbt and Node.js are required.

## Getting started

To get started, run the following command:

```bash
MOD=prod NODE_OPTIONS="--openssl-legacy-provider" sbt server/run
```

http://localhost:8080/public/index.html

## Features

- ZIO
- Scala.js
- Laminar



## Development

Development is done in two parts: the server and the client.

* The server is a ZIO application that serves the client.

* The client is a Scala.js application that is served by the server.

  * The client is built using the `fastLinkJS` command.
  * Vite is used to serve the client in development mode, with hot reloading.



### VS Code

This project [is configured to work](.vscode/tasks.json) with Visual Studio Code.

* metals is needed for Scala support.
* The Scala (Metals) extension is recommended.

To open the project in VS Code, run the following command:

```bash
code .
```

With a little luck, you will be prompted to install the recommended extensions, if not already installed.

The developpement environment should setup itself.

* npm install
* runDemo
  * sbt ~client/fastLinkJS
  * vite serve
  * runServer with reStart on file change



### Manualy start the development server

* Install the npm dependencies:

```bash
cd modules/client
npm install
```

* Start the development servers:
  * In one terminal, run the following command:
```bash
MOD=dev sbt ~client/fastLinkJS
```
  * In another terminal, run the following command:
```bash
cd modules/client
npm run dev
```
  * In another terminal, run the following command:
```bash
MOD=dev sbt server/run
```



To start the development server, run the following command:

```bash
MOD=dev NODE_OPTIONS="--openssl-legacy-provider" sbt ~client/fastLinkJS
```


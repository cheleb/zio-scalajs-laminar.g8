# Welcome to your new project!


This is ZIO, Scala.js and Laminar project.

## Getting started

To get started, run the following command:

```bash
DEV=prod NODE_OPTIONS="--openssl-legacy-provider" sbt server/run
```

http://localhost:8080/public/index.html

## Features

- ZIO
- Scala.js
- Laminar

## Pre-requisites

- JDK
- sbt
- Node.js

Decent vesions of JDK, sbt and Node.js are required.


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



### Manualy start the development server

To start the development server, run the following command:

```bash
DEV=dev NODE_OPTIONS="--openssl-legacy-provider" sbt ~client/fastLinkJS
```


# ZIO <3 ScalaJS <3 Laminar

This is a g8 scafolding of a full Scala stack web application.

* ScalaJS for the frontend
* ZIO for the backend


## Usage

```bash
sbt new cheleb/zio-scalajs-laminar.g8 --name=my-project 
```

If you want to deploy your project with ArgoCD, you can use the following command:

```bash
sbt new cheleb/zio-scalajs-laminar.g8 --name=my-project --with-argocd=true --githubUser=YOUR_GITHUB_USER
```



See following instructions, generated in the README of your newly shining project for more details.


## Features

* [ScalaJS](https://www.scala-js.org) for the frontend.
  * [Laminar](https://laminar.dev) as UI reactive framework.
  * [Scallablytyped](https://scalablytyped.org) Use Typescript libraries with Scala.js
  * [Tapir](https://tapir.softwaremill.com) for the Rest API (shared with the backend)
* [ZIO](https://zio.dev) for the backend
  * [Tapir](https://tapir.softwaremill.com) for the Rest API.
  * [Quill](https://getquill.io) for the database access.
  * [Flyway](https://flywaydb.org) for the database migrations.
* Docker support
* CD with [ArgoCD](https://argo-cd.readthedocs.io/en/stable/)
  * Image updater with [ArgoCD Image Updater](https://argocd-image-updater.readthedocs.io/en/stable/)

## Credits

* [Rock the JVM](https://rockthejvm.com/) - [ZIO rite of passage ](https://rockthejvm.com/p/zio-rite-of-passage)
* Kit Langton - [Laminar UI derivate from ScalaJS](https://github.com/kitlangton/formula?tab=readme-ov-file)

## License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0.html)

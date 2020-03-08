# Bolierplate API project written in Kotlin using coroutines 🛠
The API is deployed on Vert.X
## Purpose
Learning to build useful API using the well-known [LUCHTMEETNET 2020 OPENAPI](https://api-docs.luchtmeetnet.nl/) 
The website Luchtmeetnet is an initiative of the Ministry of Infrastructure and the Environment, 
the National Institute of Public Health and the Environment (RIVM), GGD Amsterdam, EPA for the greater Rotterdam area (DCMR), 
the Province of Limburg, EPA's middle and western Brabant (OMWB) and EPA region Arnhem (ODRA). 
The website displays the measured air quality on several measuring stations in the Netherlands and the calculated air quality maps.
This API grants direct access to detailed information of the stations and the measurements made there.

## Software Stack
* Java JDK 8+/Kotlin as main execution environment
* Gradle 6
* Docker is potentially supported if you intend trying out the demo on Mac / Linux 

## Setup
You need to have Java 8 SDK set up.

## IDE
This project was created in IntelliJ Community edition.

## How to run
This project uses [Gradle Wrapper](https://docs.gradle.org/current/userguide/gradle_wrapper.html).
Switch to the project root directory and use one the following commands.
Build the project:
```
$./gradlew build 
```
Run the Vert.X server in development mode
```
$./gradlew run 
```
Run the integration tests
```
$./gradlew test 
```
Or build a Jar and run in production mode
 ```sh
 $./gradlew shadowJar
 $java -jar build/libs/app-shadow.jar
 ``` 

## API ENDPOINTS
* `/v1/api/measurements` - values of air pollutants such as No2, O3, Particulate matter measured today near Pleinweg, Rotterdam
* `/v1/api/components` - list of measured air pollutants
* `/v1/api/stations` - list of stations daily monitoring the air quality in the Netherlands


### What's included:

* Building a Web application on top of [Vert.x](http://vertx.io/)
* [Kotlin coroutines](http://vertx.io/docs/vertx-lang-kotlin-coroutines/kotlin/) 🎉
* [Kotlin coroutines executed concurrently](https://kotlinexpertise.com/kotlin-coroutines-concurrency/) to query the remote servers  
* Querying remote services with the [Retrofit2](https://github.com/square/retrofit) (+ [Kotlin coroutines await() for Retrofit2 Call](https://github.com/gildor/kotlin-coroutines-retrofit))
* Using [Gradle](https://gradle.org/) for enabling efficient workflow
* Convenience function, await() for Kotlin coroutines []
* Integration tests with [testNG](https://github.com/cbeust/testng)
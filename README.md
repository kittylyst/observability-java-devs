# observability-java-devs
Materials for Observability For Java Developers

## Docker Bits

## The Disney Service

This project provides a simple structure to demonstrate distributed traces.

Simulates a battle between two characters chosen from several Disney-owned milieus.

Output:

{
  "good": <char1>,
  "evil": <char1>
}

Each low-level service contains a simple `GET /getCharacter` route, and top-level service calls one of the low-level services
application's `GET /getCharacter` route for each side (chosen randomly).  

The routes are as follows:



## Running the project

```shell
# Run each app in a separate shell
./gradlew disney:bootRun

./gradlew marvel:bootRun

./gradlew star-wars:bootRun
```
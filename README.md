# observability-java-devs
Materials for Observability For Java Developers

## Docker Bits


## The Disney Service

This project provides a simple structure to demonstrate distributed traces.

Simulates a battle between two characters chosen from several milieus.

Output:

{
  "good": <char1>,
  "evil": <char1>
}

Each application contains a simple `GET /ping` route, and top-level service calls one of the low-level services
application's `GET /ping` route for each side (chosen randomly).  

The routes are as follows:
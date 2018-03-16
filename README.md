Akka demo
=========

The application demonstrate a use case solved using Akka.

Problem
-------
Everyone has suffered because of lost the power of our smartphones in the worst moment. 
Our company has been created to solve the issue. 
We want to user drones and a super new and a confidential technology.
The new technology is an unlimited source of energy. 
The company decided to release a set of drones from the headquarter into a random locations and wait for orders.
The nearest available drone will be send to each newly created an order to charge a electronic device.

Configuration
=============

Google Maps
-----------
The project uses google maps which requires an api key [doc](https://developers.google.com/maps/documentation/javascript/get-api-key)

Then you have to modify `web/src/map/Map.js` file and use the Api Key.


Running
=======

Web App
-------
First you need to install `nodejs`.

Following operations you have to execute in `web` directory.

You have to download all dependencies:
```
npm i
```

Then you can start the app:
```
npm start

```

Server
------
First you need to install `sbt`.

Following operations you have to execute in the main directory of the project.

To start server you have to execute:
```
sbt main/run
```

Usage
=====
Adding new drones:
```
curl -H "Content-Type: application/json" \
    -X POST -d '{"number":10}' \
    http://localhost:8080/api/drones

```

To add an order you have to lick on the map or execute a command:
```
curl -H "Content-Type: application/json" \
    -X POST -d '{"loc":{"lat":53.09459358566883,"long":23.340553875464896}}' \
    http://localhost:8080/api/orders
```

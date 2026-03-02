# Examples
## No. 1

**Input** 

```json
{
  "commands": [
    { "type": "configureAlgorithm", "carPriority": 1.0, "busPriority": 10.0, "maxWaitTime": 5, "yellowTime": 1 },

    { "type": "addVehicle", "vehicleId": "car1", "vehicleType": "CAR", "startRoad": "north", "endRoad": "south" },
    { "type": "addVehicle", "vehicleId": "car2", "vehicleType": "CAR", "startRoad": "north", "endRoad": "south" },
    { "type": "addVehicle", "vehicleId": "bus1", "vehicleType": "BUS", "startRoad": "east",  "endRoad": "west"  },
    { "type": "addVehicle", "vehicleId": "car3", "vehicleType": "CAR", "startRoad": "west",  "endRoad": "east"  },

    { "type": "step" },
    { "type": "step" },

    { "type": "updateLaneStatus", "road": "north", "turn": "STRAIGHT", "blocked": true },

    { "type": "step" },
    { "type": "step" },

    { "type": "updateLaneStatus", "road": "north", "turn": "STRAIGHT", "blocked": false },

    { "type": "addVehicle", "vehicleId": "car4", "vehicleType": "CAR", "startRoad": "north", "endRoad": "south" },
    { "type": "addVehicle", "vehicleId": "car5", "vehicleType": "CAR", "startRoad": "north", "endRoad": "south" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "getStatistics" }
  ]
}
```

**Output** 

```json
{
  "stepStatuses" : [ {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "bus1", "car3" ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "car1" ]
  }, {
    "leftVehicles" : [ "car2" ]
  }, {
    "leftVehicles" : [ "car4" ]
  }, {
    "leftVehicles" : [ "car5" ]
  } ],
  "statistics" : {
    "totalVehiclesProcessed" : 6,
    "totalVehiclesStuck" : 2,
    "averageWaitTime" : 3.3333333333333335,
    "percentiles" : {
      "p50" : 3.0,
      "p75" : 5.0,
      "p90" : 6.0
    },
    "blockedLanes" : [ {
      "road" : "NORTH",
      "turn" : "STRAIGHT",
      "vehiclesAffected" : 2,
      "blockedFromStep" : 2,
      "blockedToStep" : "4"
    } ],
    "perRoadStats" : {
      "NORTH" : {
        "avgWaitTime" : 4.5,
        "vehiclesProcessed" : 4,
        "vehiclesStuck" : 2
      },
      "EAST" : {
        "avgWaitTime" : 1.0,
        "vehiclesProcessed" : 1,
        "vehiclesStuck" : 0
      },
      "WEST" : {
        "avgWaitTime" : 1.0,
        "vehiclesProcessed" : 1,
        "vehiclesStuck" : 0
      }
    }
  }
}
```
## No. 2

**Input** 

```json
{
  "commands": [
    {
      "type": "configureAlgorithm",
      "mode": "HISTORICAL",
      "carPriority": 1.0,
      "busPriority": 10.0,
      "maxWaitTime": 6,
      "yellowTime": 1,
      "historicalData": {
        "NORTH": [
          { "fromStep": 0,  "toStep": 5,  "factor": 0.1 },
          { "fromStep": 6,  "toStep": 20, "factor": 4.0 }
        ],
        "SOUTH": [
          { "fromStep": 0,  "toStep": 5,  "factor": 0.1 },
          { "fromStep": 6,  "toStep": 20, "factor": 4.0 }
        ],
        "EAST": [
          { "fromStep": 0,  "toStep": 5,  "factor": 5.0 },
          { "fromStep": 6,  "toStep": 20, "factor": 0.2 }
        ],
        "WEST": [
          { "fromStep": 0,  "toStep": 5,  "factor": 5.0 },
          { "fromStep": 6,  "toStep": 20, "factor": 0.2 }
        ]
      }
    },

    { "type": "addVehicle", "vehicleId": "ew_car1", "vehicleType": "CAR", "startRoad": "east", "endRoad": "west" },
    { "type": "addVehicle", "vehicleId": "ew_car2", "vehicleType": "CAR", "startRoad": "east", "endRoad": "west" },
    { "type": "addVehicle", "vehicleId": "ew_car3", "vehicleType": "CAR", "startRoad": "west", "endRoad": "east" },

    { "type": "step" },
    { "type": "step" },
    { "type": "step" },

    { "type": "addVehicle", "vehicleId": "ns_bus1", "vehicleType": "BUS", "startRoad": "north", "endRoad": "south" },
    { "type": "addVehicle", "vehicleId": "ns_car1", "vehicleType": "CAR", "startRoad": "north", "endRoad": "south" },
    { "type": "addVehicle", "vehicleId": "ns_car2", "vehicleType": "CAR", "startRoad": "south", "endRoad": "north" },

    { "type": "step" },
    { "type": "step" },

    { "type": "updateLaneStatus", "road": "east", "turn": "STRAIGHT", "blocked": true },

    { "type": "addVehicle", "vehicleId": "ew_car4", "vehicleType": "CAR", "startRoad": "east", "endRoad": "west" },
    { "type": "addVehicle", "vehicleId": "ew_car5", "vehicleType": "CAR", "startRoad": "east", "endRoad": "west" },

    { "type": "step" },
    { "type": "step" },
    { "type": "step" },

    { "type": "updateLaneStatus", "road": "east", "turn": "STRAIGHT", "blocked": false },

    { "type": "configureAlgorithm", "busPriority": 1.0 },

    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },
    { "type": "step" },

    { "type": "getStatistics" }
  ]
}
```

**Output** 

```json
{
  "stepStatuses" : [ {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "ew_car1", "ew_car3" ]
  }, {
    "leftVehicles" : [ "ew_car2" ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "ns_bus1", "ns_car2" ]
  }, {
    "leftVehicles" : [ "ns_car1" ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ ]
  }, {
    "leftVehicles" : [ "ew_car4" ]
  }, {
    "leftVehicles" : [ "ew_car5" ]
  }, {
    "leftVehicles" : [ ]
  } ],
  "statistics" : {
    "totalVehiclesProcessed" : 8,
    "totalVehiclesStuck" : 0,
    "averageWaitTime" : 4.625,
    "percentiles" : {
      "p50" : 5.0,
      "p75" : 6.0,
      "p90" : 9.0
    },
    "blockedLanes" : [ ],
    "perRoadStats" : {
      "NORTH" : {
        "avgWaitTime" : 5.5,
        "vehiclesProcessed" : 2,
        "vehiclesStuck" : 0
      },
      "EAST" : {
        "avgWaitTime" : 5.0,
        "vehiclesProcessed" : 4,
        "vehiclesStuck" : 0
      },
      "SOUTH" : {
        "avgWaitTime" : 5.0,
        "vehiclesProcessed" : 1,
        "vehiclesStuck" : 0
      },
      "WEST" : {
        "avgWaitTime" : 1.0,
        "vehiclesProcessed" : 1,
        "vehiclesStuck" : 0
      }
    }
  }
}
```
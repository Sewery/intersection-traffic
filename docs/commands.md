# Commands

| Command | Required fields | Optional fields |
|---|---|---|
| `addVehicle` | `vehicleId`, `startRoad`, `endRoad` | `vehicleType` (default: `CAR`) |
| `step` | – | – |
| `configureAlgorithm` | – | `mode`, `carPriority`, `busPriority`, `maxWaitTime`, `yellowTime`, `historicalData` |
| `updateLaneStatus` | `road`, `turn`, `blocked` | – |
| `getStatistics` | – | – |

### `addVehicle`
```json
{ "type": "addVehicle", "vehicleId": "bus1", "vehicleType": "BUS", "startRoad": "north", "endRoad": "south" }
```
- `vehicleType`: `CAR` or `BUS`
- `startRoad` / `endRoad`: `north`, `south`, `east`, `west`

### `step`
```json
{ "type": "step" }
```
Advances the simulation by one step. Vehicles crossing the intersection exit, lights update, new vehicles enter.

### `configureAlgorithm`
```json
{
  "type": "configureAlgorithm",
  "mode": "HISTORICAL",
  "carPriority": 1.0,
  "busPriority": 10.0,
  "maxWaitTime": 5,
  "yellowTime": 2,
  "historicalData": {
    "north": [{ "fromStep": 0, "toStep": 10, "factor": 3.0 }],
    "east":  [{ "fromStep": 0, "toStep": 10, "factor": 0.5 }]
  }
}
```
All fields are optional – unspecified fields keep their current value.

### `updateLaneStatus`
```json
{ "type": "updateLaneStatus", "road": "north", "turn": "STRAIGHT", "blocked": true }
```
- `turn`: `STRAIGHT`, `LEFT`, `RIGHT`
- `blocked`: `true` to block, `false` to unblock

### `getStatistics`
```json
{ "type": "getStatistics" }
```
Appends statistics to the output covering all steps up to this point.
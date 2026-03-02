# Traffic Intersection Simulator
## Overview
A four-way intersection simulator with intelligent traffic light control. The algorithm decides which direction gets green based on current traffic load, vehicle types and optionally historical data.

---

## Features

- **Collision-free phases** – only compatible movements get green simultaneously
- **Two algorithm modes** – reactive and historical
- **Starvation prevention** – no vehicle waits forever
- **Bus priority** – configurable per vehicle type
- **Lane blocking** – simulate road incidents
- **Statistics** – wait times, percentiles, per-road breakdown
- **Fully configurable** – all parameters tweakable at runtime via JSON
- **JSON I/O** – command-based input, structured output

---

## Intersection layout

Each road has 3 lanes (left, straight, right). The simulator supports all 4 directions: North, South, East, West.

Compatible phases run simultaneously without conflict, for example:
- North/South straight + right turn
- East/West left turns only

---

## Algorithm modes

### Reactive
Calculates a weight for each phase based on vehicles currently waiting:

```
weight = Σ (1 + waitTime^1.5) × vehiclePriority
```

Higher weight wins. Longer waiting → exponentially higher weight.

### Historical
Extends reactive by multiplying each direction's weight by a time-based factor:

```
weight = reactiveWeight × factor(direction, currentStep)
```

Factors are defined per direction and time window. Useful when traffic patterns are known in advance (e.g. rush hours).

If no vehicles are waiting, the factor alone acts as a base weight – so the algorithm can pre-select a direction before vehicles arrive.

### Starvation prevention
If any vehicle waits longer than `maxWaitTime`, its lane gets huge weight – overriding everything else including historical factors. This guarantees every vehicle eventually gets through.

---

## Lane blocking

A lane can be blocked to simulate incidents or roadworks. When blocked:
- vehicles in that lane are marked as stuck
- the lane is excluded from phase selection

Statistics track when each lane was blocked and unblocked.

---

## Configuration

All parameters can be changed at any point during the simulation:

| Parameter | Default | Description |
|---|---|---|
| `maxWaitTime` | `10` | steps before starvation kicks in |
| `yellowTime` | `3` | steps for yellow light transition |
| `carPriority` | `1.0` | weight multiplier for cars |
| `busPriority` | `5.0` | weight multiplier for buses |
| `mode` | `REACTIVE` | `REACTIVE` or `HISTORICAL` |
| `historicalData` | – | per-direction time slot factors |

Partial configuration is supported – unspecified fields keep their current value.

---

## Statistics

Collected on demand via `getStatistics` command:

- total vehicles processed
- total vehicles stuck
- average wait time
- percentiles: p50, p75, p90
- per-road breakdown: average wait, vehicles processed, vehicles stuck
- blocked lanes: which lane, how many vehicles affected, from/to step

---

## Input / Output format

Commands are passed as a JSON array. See [full command reference](docs/commands.md) for details.

**Input:**
```json
{
  "commands": [
    { "type": "addVehicle", "vehicleId": "vehicle1", "startRoad": "south", "endRoad": "north" },
    { "type": "addVehicle", "vehicleId": "vehicle2", "startRoad": "north", "endRoad": "south" },
    { "type": "step" },
    { "type": "step" }
  ]
}
```

**Output:**
```json
{
  "stepStatuses": [
    { "leftVehicles": [] },
    { "leftVehicles": ["vehicle2", "vehicle1"] }
  ]
}
```

More comprehensive examples [here](docs/examples.md).

---

## How to run
In project directory run:
```bash
./gradlew clean jar
```
Then with sample input:
```bash
java -jar build/libs/intersection-traffic.jar input.json output.json
```

---

## CI

Every push runs tests, JaCoCo coverage report and SonarCloud analysis.

## Future ideas

- **Generalized intersection** – configurable number of roads and lanes per road instead of fixed 4-direction, 3-lane layout
- **Time-dependent lane configuration** – lanes that open or close based on time of day (e.g. bus-only lane during rush hours)
- **Planned lane blocking** – schedule roadworks or accidents in advance as part of historical data
- **Reversible lanes** – lanes that change allowed direction based on time of day (e.g. more straight lanes from north in the morning, more from south in the evening)

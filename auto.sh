#!/bin/bash

start_rate=1
end_rate=10

for rate in $(seq $start_rate $end_rate); do
    echo "Iteration: $rate"
    java -cp "./lib/*:." edu.cwru.sepia.Main2 data/labs/infexf/OneUnitSmallMaze.xml
    # java -cp "./lib/*:." edu.cwru.sepia.Main2 data/labs/infexf/TwoUnitSmallMaze.xml
    # java -cp "./lib/*:." edu.cwru.sepia.Main2 data/labs/infexf/BigMaze.xml

    wait
done


#!/bin/bash

java\
  -Dspring.main.web-application-type=none \
  -Dspring.ai.mcp.server.stdio=true \
  -DUSER_HOME=$HOME \
  -jar $HOME/travel-agent-0.1.0-SNAPSHOT.jar

#!/bin/bash

mvn clean deploy -Dgpg.skip=false -DskipTests=true -Dmaven.javadoc.skip=true -B
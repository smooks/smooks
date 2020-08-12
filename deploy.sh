#!/bin/bash

mvn clean deploy --settings=.mvn/settings.xml -Dgpg.skip=false -DskipTests=true -B

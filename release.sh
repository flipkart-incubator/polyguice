#!/usr/bin/env bash
set -e
set -x
mvn clean install
mvn deploy -DskipTests -DaltDeploymentRepository=clojars::default::https://clojars.org/repo

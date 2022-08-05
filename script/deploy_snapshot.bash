#!/bin/bash

SCRIPT_DIR=$(cd $(dirname $0); pwd)
VERSION=$(cat "${SCRIPT_DIR}/../resources/version.txt")

clojure -T:build install :version "\"${VERSION}-SNAPSHOT\""

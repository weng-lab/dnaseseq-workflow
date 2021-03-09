#!/bin/bash
# Convenience script for building the application executable jar

set -e

# cd to project root directory
cd "$(dirname "$0")/.."
cd workflow

./gradlew clean shadowJar

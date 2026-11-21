#!/bin/sh
# simplified - CI will use setup-gradle which handles wrapper
exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"

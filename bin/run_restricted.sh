#! /bin/bash

if [ $# -lt 1 ]; then
    echo "Usage: $0 <.jar/.ll file> [...]"
    exit 1
fi

if [[ $1 == *.ll ]]; then
    JARFILE=`mktemp run-restricted-XXXXX.jar --tmpdir`
    TMPFILE=1
    ./bin/llcc -R -o $JARFILE $1 || exec ./bin/llcc -R -o $JARFILE $1
    shift 1
else
    JARFILE=$1
    TMPFILE=0
    shift 1
fi

atexit() {
    if [ $TMPFILE -eq 1 ]; then
        rm -rf "$JARFILE"
    fi
}
trap atexit EXIT

exec java -cp $JARFILE:libs/lejos0.9.0.jar Main $*

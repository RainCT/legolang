#! /bin/sh
#
# Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
# Released as open source under the ISC License

# Make sure we are in the right directory
if [ ! -f "tests/run-tests.sh" ]
then
    echo "Please run this script from the root directory..."
    exit 1
fi

LLCC=`pwd`/bin/llcc
RUNJ=`pwd`/bin/run_restricted.sh # "java -jar"
JARFILE=`mktemp llcc-test-XXXXX.jar --tmpdir`
OUTFILE=`mktemp llcc-test-XXXXX.out --tmpdir`

NUM_TESTS=0
NUM_FAILS=0

# Run execution tests
for TESTFILE in tests/$1*.ll
do
    [ -f "${TESTFILE}" ] || continue
    NUM_TESTS=`expr $NUM_TESTS + 1`
    echo "Running test ${TESTFILE}..."
    echo "================================================="
    ${LLCC} -R -q -o "${JARFILE}" "${TESTFILE}"
    if [ $? -ne 0 ]; then
        echo "COMPILATION FAILED."
        NUM_FAILS=`expr $NUM_FAILS + 1`
    else
        ${RUNJ} "${JARFILE}" >"${OUTFILE}"
        VALFILE=`echo ${TESTFILE} | sed -e 's/^\(.*\)\.ll$/\1\.out/g'`
        cmp -s "${OUTFILE}" "${VALFILE}"
        if [ $? -ne 0 ]; then
            NUM_FAILS=`expr $NUM_FAILS + 1`
            diff -uN "${VALFILE}" "${OUTFILE}"
            echo "\nFAILED!"
        else
            echo "OK!"
        fi
    fi
    echo ""
done

# Run build tests (for stuff requiring the robot to run)
for TESTFILE in tests/build_only/$1*.ll
do
    [ -f "${TESTFILE}" ] || continue
    NUM_TESTS=`expr $NUM_TESTS + 1`
    echo "Running test ${TESTFILE}..."
    echo "================================================="
    ${LLCC} -R -q -o "${JARFILE}" "${TESTFILE}"
    if [ $? -ne 0 ]; then
        echo "COMPILATION FAILED."
        NUM_FAILS=`expr $NUM_FAILS + 1`
    else
        echo "OK!"
    fi
    echo ""
done

# Run runtime error tests
for TESTFILE in tests/runtime_error/$1*.ll
do
    [ -f "${TESTFILE}" ] || continue
    NUM_TESTS=`expr $NUM_TESTS + 1`
    echo "Running test ${TESTFILE}..."
    echo "================================================="
    ${LLCC} -R -q -o "${JARFILE}" "${TESTFILE}"
    if [ $? -ne 0 ]; then
        echo "COMPILATION FAILED."
        NUM_FAILS=`expr $NUM_FAILS + 1`
    else
        ${RUNJ} "${JARFILE}" >"${OUTFILE}" >/dev/null 2>&1
        if [ $? -eq 0 ]; then
            NUM_FAILS=`expr $NUM_FAILS + 1`
            echo "FAILED! Program terminated normally but should have error."
        else
            echo "OK!"
        fi
    fi
    echo ""
done

# Run compilation time error tests
for TESTFILE in tests/compilation_error/$1*.ll
do
    [ -f "${TESTFILE}" ] || continue
    NUM_TESTS=`expr $NUM_TESTS + 1`
    echo "Running test ${TESTFILE}..."
    echo "================================================="
    ${LLCC} -S -R -q "${TESTFILE}" >/dev/null 2>&1
    if [ $? -eq 1 ]; then
        echo "OK!"
    else
        if [ $? -eq 0 ]; then
            echo "FAILED! Program passed semantic test but has invalid syntax."
        else
            ${LLCC} -S -R -q "${TESTFILE}"
            echo "\nFAILED! Unexpected compilation result."
        fi
        NUM_FAILS=`expr $NUM_FAILS + 1`
    fi
    echo ""
done

# Run parse time error tests
for TESTFILE in tests/parse_error/$1*.ll
do
    [ -f "${TESTFILE}" ] || continue
    NUM_TESTS=`expr $NUM_TESTS + 1`
    echo "Running test ${TESTFILE}..."
    echo "================================================="
    ${LLCC} -S -R -q "${TESTFILE}" >/dev/null 2>&1
    if [ $? -eq 2 ]; then
        echo "OK!"
    else
        echo "FAILED! Program was parsed correctly but it has invalid syntax."
        NUM_FAILS=`expr $NUM_FAILS + 1`
    fi
    echo ""
done

# Clean up
rm -f "${JARFILE}" "${OUTFILE}"

echo "RESULT"
echo "================================================="
echo "${NUM_FAILS} out of ${NUM_TESTS} tests failed."

if [ $NUM_FAILS -eq 0 ]; then
    echo
    echo "\o/ \o/ \o/ AWESOME! \o/ \o/ \o/"
fi

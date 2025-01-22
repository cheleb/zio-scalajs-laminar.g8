. ./scripts/env.sh
set -e
#
# This script is used to setup the project in a non-interactive way
# Allow to choose between ESModule or CommonJs
#

BUILD_MOD="ESModule"
BUILD_CLEAN=""

usage() {
    echo "Usage: $0 [-d] [-m] [-n] [-c] [-h]"
    echo "  -d: Development mode"
    echo "  -m: Build ESModule"
    echo "  -n: Build CommonJs"
    echo "  -c: Clean before building"
    echo "  -h: Display this help message"
}

while getopts ":d :m :n :c :h" opt; do
    case ${opt} in
    d)
        BUILD_MOD="dev"
        ;;
    m)
        BUILD_MOD="ESModule"
        ;;
    n)
        BUILD_MOD="CommonJs"
        ;;
    c)
        BUILD_CLEAN="clean; "
        ;;
    h)
        usage
        exit 0
        ;;
    \?)
        echo "Invalid option: $OPTARG" 1>&2
        usage
        exit 1
        ;;
    esac
done
echo "Js Module: $BUILD_MOD"

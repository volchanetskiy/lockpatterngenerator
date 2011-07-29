#!/bin/sh

PNAME='in.shick.lockpatterngenerator'
TITLE='LockPatternGenerator'

adb -e uninstall ${PNAME}
adb -e install bin/${TITLE}-debug.apk

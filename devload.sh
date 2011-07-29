#!/bin/sh

PNAME='in.shick.lockpatterngenerator'
TITLE='LockPatternGenerator'

adb -d uninstall ${PNAME}
adb -d install bin/${TITLE}-debug.apk

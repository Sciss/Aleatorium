[![Build Status](https://github.com/Sciss/Aleatorium/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/Aleatorium/actions?query=workflow%3A%22Scala+CI%22)

# Aleatorium

This repository contains code for an ongoing art project.

(C)opyright 2021 by Hanns Holger Rutz. All rights reserved. This project is released under the
[GNU Affero General Public License](https://github.comt/Sciss/Aleatorium/blob/main/LICENSE) v3+ and
comes with absolutely no warranties.
To contact the author, send an e-mail to `contact at sciss.de`.

## building

Builds with sbt against Scala 2.13.
Create executable: `sbt assembly`

## fix wiring-pi

__Important:__ Wiring-Pi is broken on the Pi 4. The pull up/down resistors cannot be configured.
See https://pi4j.com/1.3/install.html#WiringPi_Native_Library -- one needs to replace the installed versions
with an unofficial one!

    sudo apt remove wiringpi -y
    sudo apt install git-core gcc make
    cd ~/Documents/devel/
    git clone https://github.com/WiringPi/WiringPi --branch master --single-branch wiringpi
    cd wiringpi
    sudo ./build

## run on the Raspberry Pi

- THIS NO LONGER works: the JNI library `librpiws28114j.so` must be installed.
- the JNI library `libws281x.so` must be installed. Copy it to `/usr/lib/jni/`

To run

    java -Xmx768m -jar aleatorium.jar

## test runs

Light:

TODO: `Can't open /dev/mem: Permission denied`. Currently must use `sudo` for:

    java -cp aleatorium.jar:lib/rpi-ws281x-java-2.0.0-SNAPSHOT.jar de.sciss.aleatorium.Light


[![Build Status](https://github.com/Sciss/Aleatorium/workflows/Scala%20CI/badge.svg?branch=main)](https://github.com/Sciss/Aleatorium/actions?query=workflow%3A%22Scala+CI%22)

# Aleatorium

This repository contains code for an ongoing art project.

(C)opyright 2021 by Hanns Holger Rutz. All rights reserved. This project is released under the
[GNU Affero General Public License](https://github.comt/Sciss/Aleatorium/blob/main/LICENSE) v3+ and
comes with absolutely no warranties.
To contact the author, send an e-mail to `contact at sciss.de`.

## building

Builds with sbt against Scala 2.13. There are two sub-modules `alpha` (top robot) and `beta` (bottom robot). 
Create executable: `sbt alpha/assembly` or `sbt beta/assembly`.

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

## installing SuperCollider on the 'beta' Pi

We build SC 3.10.4:

```
cd ~/Documents/devel
git clone https://github.com/supercollider/supercollider.git

sudo apt install libjack-jackd2-dev libsndfile1-dev libasound2-dev libavahi-client-dev \
libreadline-dev libfftw3-dev libxt-dev libudev-dev libncurses5-dev cmake git qttools5-dev qttools5-dev-tools \
qtdeclarative5-dev libqt5svg5-dev qjackctl

cd supercollider
git checkout -b 3.10.4 Version-3.10.4

git submodule update --init --recursive

mkdir build
cd build

cmake -DCMAKE_BUILD_TYPE=Release -DSUPERNOVA=OFF -DSC_ED=OFF -DSC_EL=OFF -DSC_VIM=ON -DNATIVE=ON -DSC_USE_QTWEBENGINE:BOOL=OFF ..

cmake --build . --config Release --target all -- -j3

sudo cmake --build . --config Release --target install
```

This installs in `/usr/local/bin`. If debian package has been installed, it will override through `/usr/bin`,
to remove use `sudo apt remove supercollider-server` (or `-common` I guess?).

We use `JPverb` thus also need `sc3-plugins`:

```
cd ~/Documents/devel
git clone https://github.com/supercollider/sc3-plugins.git

cd sc3-plugins
git checkout -b 3.10.4 643709850b2f22f68792372aaece5fc6512defc6

git submodule update --init --recursive

mkdir build
cd build

cmake -DSC_PATH=/home/pi/Documents/devel/supercollider/ -SC_PATH=/home/pi/.local/share/SuperCollider ..

cmake --build . --config Release

sudo cmake --build . --config Release --target install

```

## run on the Raspberry Pi

- THIS NO LONGER works: the JNI library `librpiws28114j.so` must be installed.
- the JNI library `libws281x.so` must be installed. Copy it to `/usr/lib/jni/`

See run scripts `run-alpha.sh` and `run-beta.sh`. Note that beta now uses mainly a regular
Mellite workspace, and no longer the `Sound.scala` source code!

Beta assumes that light (needs sudo) runs in a separate process via OSC at port 57120,
and that the arm runs in a separate process via OSC at port 57121.

## test runs

Light:

The JNI library is build via https://github.com/Sciss/rpi-ws281x-java/tree/pi4

TODO: `Can't open /dev/mem: Permission denied`. Currently must use `sudo` for:

    java -cp aleatorium.jar:lib/rpi-ws281x-java-2.0.0-SNAPSHOT.jar de.sciss.aleatorium.Light

## IPs

- alpha (top): 192.168.0.30
- beta (bottom): 192.168.0.46

## Cabling and setup

Alpha foot switch: 3pin extension cable is connected white-on-white. On the GPIO, connect header pins
14 to 16 (outer row, skipping six pins), so that 0v, GPIO4 and GPIO5 are connected. The white wire goes
to GPIO5 (header pin 16).

Placement: The base plate is positioned c. 15 cm away from the wall. The right hand side extends around 7.5 cm
beyond the right hand side of the shelf. The dispenser is positioned based on visual markers on the base plate,
yielding an approx. distance from the wall of 22.3 cm. and its right hand side aligning with the right arm
of the shelf mount
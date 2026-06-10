# AdalightCockpit

<p>
  <img src="res/Screenshot_2.jpg" alt="Adalight photo 2" width="200" />
  <img src="res/Screenshot_3.jpg" alt="Adalight photo 3" width="200" />
</p>
<p>
  <img src="res/Screenshot_4.jpg" alt="Adalight photo 4" width="200" />
  <img src="res/Screenshot_5.jpg" alt="Adalight photo 5" width="200" />
</p>
<p>
  <img src="res/Screenshot_6.jpg" alt="Adalight photo 6" width="200" />
  <img src="res/Screenshot_1.jpg" alt="AdalightCockpit Screenshot" width="200" />
</p>

Small Java control panel (cockpit) for my Adalight.

It's an old personal project.  
It works but it's not exceptionally pretty :)

Currently It's only usage ready for Linux.  
If someone is interested in a Windows / Mac version - tell me  
(best via a Github issue).

The idea for the Adalight and most of the software is from:  
https://www.adafruit.com  
(awesome site - check them out).

Their tutorial for the Adalight is here:  
https://learn.adafruit.com/adalight-diy-ambient-tv-lighting  
All credit goes to them!

This uses:
- org.processing.Core
- com.fazecast.jSerialComm

<!--
TODOS
- needs to access /dev/ttyACM0 etc. think about group etc.
- better healthchecks for device not found
- adalight can only be activated after single color
- should start in adalight
- maybe go to java fx?
-->
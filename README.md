SpermProject - Client Side
==========================

This is the client application for the SpermProject. 
It consists of the Java application which is to run in the Android device of the user.
The client application is heavily based on the [OpenCamera project](http://opencamera.sourceforge.net/) 
due to the versatility that it offers for manipulating the camera.
The goal of the client is to capture a video of the sperm sample and send it to the server for further analysis.
The client steps, in more detail, are presented below:

* Shoot a video of predetermined length
* Make a TCP connection to the [analysis server](https://github.com/bergercookie/SpermProject_server)
* Transfer the captured video to the server
* Block for the image analysis algorithm to finish and receive the results.
* Print in a formatted manner the results in the screen of the user.

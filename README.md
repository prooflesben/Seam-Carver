# Seam-Carver
Fundies 2 Project to shrink images by deleting the least important seams.

# How to run the project
## Main Class 
This project was developed in Eclipse and needs to have tester.Main as the main class when running the project.

## Needed Jars
Two jars are used in this project tester.jar and javalib.jar these both can be found in the main project folder.

## JDK Version
This project uses jdk 11.0.17.

## How to change the image
First, create a new PixelImage object in testBigBang like shown below. Ensure that the jpg image you are referencing is in the main project folder.
PixelImage balloonsImage = new PixelImage(new FromFileImage("balloons.jpg"));

# Extra Features
To pause the game press space
To change to/from deleting vertical seams press v or V
To change to/from deleting horizontal seams press h or H
To change to/from grayscale press g or G
To change to/from seam weight press w or W
Click any other key and reverts to randomly deleting in full color

Then create a SeamCarvingWorld object using the PixelImage as an input for the constructor like below.
SeamCarvingWWorld ballonsWorld = new SeamCarvingWorld(balloonsImage);

In the testBigBang function in the ExamplePixels class call the big bang function of the SeamCarvingWorld object with its image.width and image.height fields like below.
birdsWorld.bigBang(birdsWorld.image.width, birdsWorld.image.height, .001);
The last argument of this function determines the tick-rate per second of the program. 


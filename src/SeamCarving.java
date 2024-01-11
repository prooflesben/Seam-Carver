import tester.Tester;

import javalib.impworld.*;
import javalib.worldimages.*;
import java.awt.Color;

// To pause the game press space
// To change to/from deleting vertical seams press v or V
// To change to/from deleting horizontal seams press h or H
// to change to/from grayscale press g or G
// To change to/from seam weight press w or W
// click any other key and reverts to randomly deleting in full color

// Represents an image consisting of pixels, with FromFileImage image representing
// the image coming from a file, ints width and height representing the image's width
// and height, an APixel topLeftPixel representing the top-left pixel of the image,
// whose neighbors and neighbor's neighbors consist of the image converted to APixels,
// a String grey that indicates if the image should be placed on a greyscale based on
// energy, seam weight, or none at all, and a double largestWeight which represents
// the largest seam weight among topLeftPixel and the Pixels connected to it
class PixelImage {
  FromFileImage image;
  int height;
  int width;
  APixel topLeftPixel;
  String grey;
  double largestWeight;

  // A constructor for a PixelImage, taking in an image file and using
  // it to determine the height and width. It also converts the image to 
  // APixel form, sets the top-left pixel as the PixelImage's topLeftPixel,
  // initializes grey as an empty string, and initializes largestWeight to 0.
  // An error is thrown if the topLeftPixel isn't well-formed
  PixelImage(FromFileImage image) {
    this.image = image;
    this.height = (int) image.getHeight();
    this.width = (int) image.getWidth();
    this.topLeftPixel = this.toPixel();
    this.grey = "";
    this.largestWeight = 0;
    if (!this.topLeftPixel.checkWellFormedness(true)) {
      throw new IllegalArgumentException("The image is not well-formed");
    }
  }

  // Uses the image value to convert it to APixel form, returning the top-left pixel
  // in APixel form
  public APixel toPixel() {
    APixel currentAbove = new NoPixel();
    APixel current = new Pixel(this.image.getColorAt(0, 0));
    APixel first = current;
    // For every row in the image except for the last one, turn its pixels into
    // APixels and set them related to each other as neighbors
    for (int y = 0; y < this.height - 1; y += 1) {
      if (y % 2 == 0) {
        // For every pixel in an image's even-numbered row except the last, set it's 
        // APixel form's right value as a new APixel, and set its up value as the currentAbove. 
        // This also sets the right value's left as this APixel and the up value's down as this
        // APixel. Then, shift the current and currentAbove to the right.
        for (int x = 0; x < this.width - 1; x += 1) {
          current.setRight(new Pixel(this.image.getColorAt(x + 1, y)), true);
          current.setUp(currentAbove, true);
          current = current.right;
          currentAbove = currentAbove.right;
        }
        // For the last pixel in an even-numbered row, set its up neighbor as currentAbove
        // and its down neighbor as the pixel below in its APixel form
        current.setDown(new Pixel(this.image.getColorAt(width - 1, y + 1)), true);
        current.setUp(currentAbove, true);
      }
      else {
        // For every pixel in an image's odd-numbered row except the last, set it's 
        // APixel form's left value as a new APixel, and set its up value as the currentAbove. 
        // This also sets the left value's right as this APixel and the up value's down as this
        // APixel. Then, shift the current and currentAbove to the left.
        for (int x = width - 1; x > 0; x -= 1) {
          current.setLeft(new Pixel(this.image.getColorAt(x - 1, y)), true);
          current.setUp(currentAbove, true);
          current = current.left;
          currentAbove = currentAbove.left;
        }
        // For the last pixel in an odd-numbered row, set its up neighbor as currentAbove
        // and its down neighbor as the pixel below in its APixel form
        current.setDown(new Pixel(this.image.getColorAt(0, y + 1)), true);
        current.setUp(currentAbove, true);
      }
      // Move on to the next row by moving currentAbove and current down
      currentAbove = current;
      current = current.down;
    }
    if (height % 2 == 1) {
      // If last row is odd numbered, set it's APixel form's right value as a new APixel, 
      // and set its up value as the currentAbove. This also sets the right value's left 
      // as this APixel and the up value's down as this APixel. Then, shift the current 
      // and currentAbove to the right.
      for (int x = 0; x < this.width - 1; x += 1) {
        current.setRight(new Pixel(this.image.getColorAt(x + 1, height - 1)), true);
        current.setUp(currentAbove, true);
        current = current.right;
        currentAbove = currentAbove.right;
      }
      // For the last pixel in the odd-number last row, set its up neighbor as currentAbove
      current.setUp(currentAbove, true);
    }
    else {
      // If last row is even numbered, set it's APixel form's left value as a new APixel,
      // and set its up value as the currentAbove. This also sets the left value's right 
      // as this APixel and the up value's down as this APixel. Then, shift the current 
      // and currentAbove to the left.
      for (int x = width - 1; x > 0; x -= 1) {
        current.setLeft(new Pixel(this.image.getColorAt(x - 1, height - 1)), true);
        current.setUp(currentAbove, true);
        current = current.left;
        currentAbove = currentAbove.left;
      }
      // For the last pixel in the even-number last row, set its up neighbor as currentAbove
      current.setUp(currentAbove, true);
    }
    // Return the initial Pixel created, representing the top-left pixel of the image
    return first;
  }

  // Carves the least-weight vertical seam out of a PixelImage and checks if the process
  // keeps well formedness. Effect: Deletes the smallest vertical seam and shifts the 
  // pixels to fill the gaps
  public void carveSeamVertical() {
    this.topLeftPixel.deleteSmallestVerticalSeam(this.topLeftPixel, false);
    if (!this.topLeftPixel.checkWellFormedness(true)) {
      this.topLeftPixel = this.topLeftPixel.right;
    }
    if (!this.topLeftPixel.checkWellFormedness(true)) {
      throw new IllegalArgumentException("Deleting seam undid well-formedness");
    }
  } 

  // Carves the least-weight horizontal seam out of a PixelImage and checks if the process 
  // keeps well formedness. Effect: Deletes the smallest horizontal seam and shifts the 
  // pixels to fill the gaps
  public void carveSeamHorizontal() {
    this.topLeftPixel.deleteSmallestHorizontalSeam(this.topLeftPixel, false);
    if (!this.topLeftPixel.checkWellFormedness(true)) {
      this.topLeftPixel = this.topLeftPixel.down;
    }
    if (!this.topLeftPixel.checkWellFormedness(true)) {
      throw new IllegalArgumentException("Deleting seam undid well-formedness");
    }

  }

  // Draws the smallest vertical seam red.
  // Effect: Each Pixel to the right and below topLeftPixel is assigned the least-weight
  // vertical seam ending at that Pixel, and the least-weight vertical seam ending at the
  // bottom row is colored red
  public void drawRedVertical() {
    this.topLeftPixel.createSeamVertical(true);
    this.topLeftPixel.deleteSmallestVerticalSeam(this.topLeftPixel, true);
  }

  // Draws the smallest horizontal seam red.
  // Effect: Each Pixel to the right and below topLeftPixel is assigned the least-weight
  // horizontal seam ending at that Pixel, and the least-weight horizontal seam ending at the
  // rightmost column is colored red
  public void drawRedHorizontal() {
    this.topLeftPixel.createSeamHorizontal(true);
    this.topLeftPixel.deleteSmallestHorizontalSeam(this.topLeftPixel, true);
  }

  // Takes a PixelImage and converts it to a ComputedPixelImage so it can be displayed
  public ComputedPixelImage convertToFile() {
    int width = 1 + this.topLeftPixel.right.getWidth();
    int height = 1 + this.topLeftPixel.down.getHeight();
    APixel current = this.topLeftPixel;
    APixel currentDown = this.topLeftPixel.down;
    ComputedPixelImage cpi = new ComputedPixelImage(width, height);
    // For every row in the image, build the row to create the ComputedPixelImage
    for (int y = 0; y < height; y += 1) {
      // For every Pixel in a row, set its corresponding coordinate in the
      // ComputedPixelImage to be the Pixel's color if String grey isn't "energy"
      // or "weight," a shade of grey based on the Pixel's energy if String grey
      // is "energy," or a shade of grey based on the Pixel's seam's weight
      // if String grey is "weight"
      for (int x = 0; x < width; x += 1) {
        if (this.grey.equals("energy")) {
          int energy = (int)(255 * current.calcEnergy() / Math.sqrt(32));
          cpi.setPixel(x, y, new Color(energy, energy, energy));
        } else if (this.grey.equals("weight")) {
          int weight = (int)(255 * current.seam.getSeamWeight() / this.largestWeight);
          cpi.setPixel(x, y, new Color(weight, weight, weight));
        } else {
          cpi.setPixel(x, y, current.color);
        }
        current = current.right;
      }
      // Shift make the current set as the currentDown, starting from
      // the beginning of the next row, and move currentDown down
      current = currentDown;
      currentDown = current.down;
    }
    return cpi;
  }

}

// An abstract class representing a Pixel or a NoPixel. They both contain a color, an APixel on 
// the left, right, up, and down, and an ISeam which represents the seam with the smallest
// total weight that ends at the APixel
abstract class APixel {
  Color color;
  APixel left;
  APixel right;
  APixel up;
  APixel down;
  ISeam seam;


  // A constructor for an APixel that takes in only the seam's color,
  // initializing the neighbors to itself and the seam to a NoSeam
  APixel(Color color) {
    this.color = color;
    this.left = this;
    this.right = this;
    this.up = this;
    this.down = this;
    this.seam = new NoSeam();
  }

  // makes the color of a APixel red
  public abstract void makeRed();

  // Takes an APixel and shifts it to the left
  public abstract void shiftRightPixelLeft();

  // A helper for shiftRightPixelLeft, helping to shift
  // the APixel left
  public abstract void shiftRPLHelp(Pixel right);

  // Deletes an APixel by having its left and right neighbors
  // point to each other
  public abstract void deletePixelVertical();

  // Takes an APixel and shifts it to Up
  public abstract void shiftDownPixelUp();

  // A helper for shiftDownPixelUp, helping to shift
  // the APixel Up
  public abstract void shiftDPUHelp(Pixel right);

  // Deletes an APixel by having its up and down neighbors
  // point to each other
  public abstract void deletePixelHorizontal();

  // Sets the inputed APixel right as the given APixel's right neighbor
  public abstract void setRight(APixel right, boolean setLeft);

  // Sets the inputed APixel left as the given APixel's left neighbor
  public abstract void setLeft(APixel left, boolean setRight);

  // Sets the inputed APixel up as the given APixel's up neighbor
  public abstract void setUp(APixel up, boolean setDown);

  // Sets the inputed APixel down as the given APixel's down neighbor
  public abstract void setDown(APixel down, boolean setUp);

  // Calculates a given APixel's energy
  public abstract double calcEnergy();

  // Calculates a given APixel's brightness
  public abstract double calcBrightness();

  // Calculates a given APixel's vertical energy
  public abstract double calcVertical();

  // Calculates a given APixel's horizontal energy
  public abstract double calcHorizontal();

  // Sets all APixels seams to be the least-weight vertical seam that ends at it
  public abstract void createSeamVertical(boolean firstColumn);

  // A helper for createSeamVeritcal that considers an APixel's up, up-right, and up-left
  // neighbor's seams in order to set a least-weight vertical seam
  public abstract void createSeamVerticalHelp(boolean firstColumn);

  // Returns the least-weight vertical seam between a given APixel and its left and right neighbors
  public abstract ISeam findSmallestVertical();

  // Deletes the least-weight vertical seam in the bottom row, searching for the first
  // APixel in this bottom row
  public abstract void deleteSmallestVerticalSeam(APixel previous, boolean drawRed);

  // A helper for deleteSmallestVerticalSeam that goes down the bottom row to find and
  // delete its least-weight vertical seam
  public abstract void deleteSmallestVerticalSeamHelp(ISeam smallest, boolean drawRed);


  // Sets all APixels seams to be the least-weight horizontal seam that ends at it
  public abstract void createSeamHorizontal(boolean firstColumn);

  // A helper for createSeamHorizontal that considers an APixel's left, down-left, and up-left
  // neighbor's seams in order to set a least-weight horizontal seam
  public abstract void createSeamHorizontalHelp(boolean firstColumn);

  // Returns the least-weight horizontal seam between a given APixel and its up and down neighbors
  public abstract ISeam findSmallestHorizontal();

  // Deletes the least-weight horizontal seam in the rightmost column, searching for the first
  // APixel in this right column
  public abstract void deleteSmallestHorizontalSeam(APixel previous, boolean drawRed);

  // A helper for deleteSmallestHorizontalSeam that goes down the rightmost column to find and
  // delete its least-weight horizontal seam
  public abstract void deleteSmallestHorizontalSeamHelp(ISeam smallest, boolean drawRed);

  // Returns whether or not an APixel and all its right and down neighbors
  // are well-formed, meaning their right neighbor's up neighbor is the same
  // as their up neighbor's right neighbor and etc.
  public abstract boolean checkWellFormedness(boolean firstColumn);

  // States whether two APixels are the same
  public abstract boolean samePixel(APixel pixel);

  // States whether an APixel and a NoPixel are both NoPixels
  public abstract boolean sameNoPixel(NoPixel noPixel);

  // Finds the largest-weight vertical seam
  public abstract double getLargestSeamWeightVertical();

  // Finds the largest-weight vertical seam in a row
  public abstract double getLargestSeamWeightInRow();

  // Finds the largest-weight horizontal seam
  public abstract double getLargestSeamWeightHorizontal();

  // Finds the largest-weight horizontal seam in a column
  public abstract double getLargestSeamWeightInColumn();

  // Gets the width of an image, assuming the APixel is the image's top-left pixel
  abstract int getWidth();

  // Gets the height of an image, assuming the APixel is the image's top-left pixel
  abstract int getHeight();


}

// Represents an APixel with an actual color, making it a full Pixel with at least
// two adjacent Pixels, composing the actual image
class Pixel extends APixel {

  // A constructor for a Pixel which uses the abstract constructor for APixel, setting
  // its color to the inputed color, its neighbors initialized as NoPixels, and its
  // seam to a seam containing the Pixel and a NoSeam for cameFrom
  Pixel(Color color) {
    super(color);
    this.left = new NoPixel();
    this.right = new NoPixel();
    this.up = new NoPixel();
    this.down = new NoPixel();
    this.seam = new SeamInfo(this, new NoSeam());
  }

  // Takes a Pixel and shifts its right pixel to have its current up and down
  // values. Effect: All pixels to the right of this pixel will have the up
  // and down values of its left neighbor
  public void shiftRightPixelLeft() {
    this.right.shiftRightPixelLeft();
    this.right.shiftRPLHelp(this);
  }

  // A helper function for shiftRightPixelLeft. Effect: changes a Pixel's up and down 
  // values to its left neighbor's up and down values, as well as sets the shiftingPixel's
  // up neighbor's down value to this pixel, and its down neighbor's up value to this pixel
  public void shiftRPLHelp(Pixel shiftingPixel) {
    this.setUp(shiftingPixel.up, true);
    this.setDown(shiftingPixel.down, true);
  }

  // Sets a Pixel's left and right neighbors to become neighbors. Effect: this Pixel's
  // left neighbor's right value is set to this Pixel's right neighbor, and vice versa, 
  // if they are both Pixels. If the left neighbor is a NoPixel, then the right neighbor's
  // left value is set to a NoPixel, and vice versa
  public void deletePixelVertical() {
    this.left.setRight(this.right, true);
    this.right.setLeft(this.left, true);
  }

  // Takes a Pixel and shifts its down pixel to have its current left and right
  // values. Effect: All pixels below this pixel will have the left
  // and right values of its up neighbor
  public void shiftDownPixelUp() {
    this.down.shiftDownPixelUp();
    this.down.shiftDPUHelp(this);
  }

  // A helper function for shiftDownPixelUp. Effect: changes a Pixel's left and right 
  // values to its up neighbor's left and right values, as well as sets the shiftingPixel's
  // left neighbor's right value to this pixel, and its right neighbor's left value to this pixel
  public void shiftDPUHelp(Pixel shiftingPixel) {
    this.setLeft(shiftingPixel.left, true);
    this.setRight(shiftingPixel.right, true);
  }

  // Sets a Pixel's up and down neighbors to become neighbors. Effect: this Pixel's
  // up neighbor's down value is set to this Pixel's down neighbor, and vice versa, 
  // if they are both Pixels. If the up neighbor is a NoPixel, then the down neighbor's
  // up value is set to a NoPixel, and vice versa
  public void deletePixelHorizontal() {
    this.up.setDown(this.down, true);
    this.down.setUp(this.up, true);
  }

  // Sets this Pixel's left neighbor to the given left APixel. Effect: this Pixel's
  // left value is changed to the left APixel, and if setRight is true, the left
  // APixel's right value is set to this Pixel
  public void setLeft(APixel left, boolean setRight) {
    this.left = left;
    if (setRight) {
      left.setRight(this, false);
    }
  }

  // Sets this Pixel's right neighbor to the given right APixel. Effect: this Pixel's
  // right value is changed to the right APixel, and if setLeft is true, the right
  // APixel's right value is set to this left
  public void setRight(APixel right, boolean setLeft) {
    this.right = right;
    if (setLeft) {
      right.setLeft(this, false);
    }
  }

  // Sets this Pixel's up neighbor to the given up APixel. Effect: this Pixel's
  // up value is changed to the up APixel, and if setDown is true, the up
  // APixel's down value is set to this Pixel
  public void setUp(APixel up, boolean setDown) {
    this.up = up;
    if (setDown) {
      up.setDown(this, false);
    }
  }

  // Sets this Pixel's down neighbor to the given down APixel. Effect: this Pixel's
  // down value is changed to the down APixel, and if setUp is true, the down
  // APixel's up value is set to this Pixel
  public void setDown(APixel down, boolean setUp) {
    this.down = down;
    if (setUp) {
      down.setUp(this, false);
    }
  }

  // Returns the energy of a Pixel by comparing it to its adjacent and diagonal neighbors
  public double calcEnergy() {
    double vE = this.calcVertical();
    double hE = this.calcHorizontal();
    return Math.sqrt(vE * vE + hE * hE);
  }

  // Returns the brightness of a Pixel, which is on a scale from 0 to 1 depeneding
  // on the Pixel's color values
  public double calcBrightness() {
    return (this.color.getBlue() + this.color.getGreen() + this.color.getRed()) / 3.0 / 255;
  }

  // Calculates the vertical energy of a Pixel by comparing the difference between its 
  // the up-adjacent APixels and its three down-adjacent APixels
  public double calcVertical() {
    return (this.up.left.calcBrightness() + (2 * this.up.calcBrightness())
        + this.up.right.calcBrightness())
        - (this.down.left.calcBrightness() + (2 * this.down.calcBrightness())
            + this.down.right.calcBrightness());
  }

  // Calculates the horizontal energy of a Pixel by comparing the difference between its 
  // the left-adjacent APixels and its three right-adjacent APixels
  public double calcHorizontal() {
    return (this.left.up.calcBrightness() + (2 * this.left.calcBrightness())
        + this.left.down.calcBrightness()) 
        - (this.right.up.calcBrightness() + (2 * this.right.calcBrightness())
            + this.right.down.calcBrightness());
  }

  // Sets a Pixel's seam to be a new seam containing this Pixel as its pixel, 
  // along with all Pixels to the right of it. It also sets every Pixel in a lower
  // row's seams to be the least-weight vertical seam ending at the Pixel. Effect: all 
  // Pixels to the right and below the initial Pixel will have a seam value set to be  
  // the least-weight vertical seam ending at the Pixel
  public void createSeamVertical(boolean firstColumn) {
    this.seam = new SeamInfo(this);
    this.right.createSeamVertical(false);
    if (firstColumn) {
      this.down.createSeamVerticalHelp(true);
    }
  }

  // A helper for createSeamVertical which creates vertical seams for all Pixel not in the 
  // first row. Effect: each Pixel not in the first row is given a seam value that is equivalent
  // to the least-weight vertical seam ending at the Pixel, which is determined by finding the 
  // smallest seam in the three up-adjacent Pixels
  public void createSeamVerticalHelp(boolean firstColumn) {
    this.seam = new SeamInfo(this, this.up.findSmallestVertical());
    this.right.createSeamVerticalHelp(false);
    if (firstColumn) {
      this.down.createSeamVerticalHelp(true);
    }
  }

  // Returns the smallest vertical seam between this Pixel and its left and right neighbors
  public ISeam findSmallestVertical() {
    return this.seam.findSmallest(this.left.seam, this.right.seam);
  }

  // Deletes the smallest vertical seam in the bottom row of an image by going down
  // to the bottom of the first column. Effect: one Pixel per row is "deleted," meaning
  // its left and right pixels point to each other and pixels to the right have updated
  // up and down values
  public void deleteSmallestVerticalSeam(APixel previous, boolean drawRed) {
    this.down.deleteSmallestVerticalSeam(this, drawRed);
  }


  // A helper for deleteSmallestVerticalSeam, which moves to the right in the bottom row
  // in order to find the least-weight vertical seam. Effect: one Pixel per row is "deleted," 
  // meaning its left and right pixels point to each other and pixels to the right have updated
  // up and down values
  public void deleteSmallestVerticalSeamHelp(ISeam smallest, boolean drawRed) {
    ISeam newSmallest = smallest.compareISeam(this.right.seam);
    this.right.deleteSmallestVerticalSeamHelp(newSmallest, drawRed);
  }


  // Sets a Pixel's seam to be a new seam containing this Pixel as its pixel, 
  // along with all Pixels below it. It also sets every Pixel in a column to the right's
  // seams to be the least-weight horizontal seam ending at the Pixel. Effect: all 
  // Pixels to the right and below the initial Pixel will have a seam value set to be  
  // the least-weight horizontal seam ending at the Pixel
  public void createSeamHorizontal(boolean firstRow) {
    this.seam = new SeamInfo(this);
    this.down.createSeamHorizontal(false);
    if (firstRow) {
      this.right.createSeamHorizontalHelp(true);
    }
  }

  // A helper for createSeamHorizontal which creates seams for all Pixel not in the first column.
  // Effect: each Pixel not in the first column is given a seam value that is equivalent
  // to the least-weight horizontal seam ending at the Pixel, which is determined by finding the 
  // smallest seam in the three left-adjacent Pixels
  public void createSeamHorizontalHelp(boolean firstRow) {
    this.seam = new SeamInfo(this, this.left.findSmallestHorizontal());
    this.down.createSeamHorizontalHelp(false);
    if (firstRow) {
      this.right.createSeamHorizontalHelp(true);
    }
  }

  // Returns the smallest seam between this Pixel and its up and down neighbors
  public ISeam findSmallestHorizontal() {
    return this.seam.findSmallest(this.up.seam, this.down.seam);
  }

  // Deletes the smallest seam in the rightmost column of an image by going right
  // to the end of the first row. Effect: one Pixel per column is "deleted," meaning
  // its up and down pixels point to each other and pixels below it have updated
  // left and right values
  public void deleteSmallestHorizontalSeam(APixel previous, boolean drawRed) {
    this.right.deleteSmallestHorizontalSeam(this, drawRed);
  }


  // A helper for deleteSmallestHorizontalSeam, which moves down in the rightmost column
  // in order to find the least-weight horizontal seam. Effect: one Pixel per column is "deleted,"
  // meaning its up and down pixels point to each other and pixels below it have updated
  // left and right values
  public void deleteSmallestHorizontalSeamHelp(ISeam smallest, boolean drawRed) {
    ISeam newSmallest = smallest.compareISeam(this.down.seam);
    this.down.deleteSmallestHorizontalSeamHelp(newSmallest, drawRed);
  }

  // Returns whether or not a Pixel and all its right and down neighbors
  // are well-formed, meaning their right neighbor's up neighbor is the same
  // as their up neighbor's right neighbor and etc. It recurs to the right and down
  // in the first row to check the well-formedness of all pixels to the right and down
  public boolean checkWellFormedness(boolean firstColumn) {
    boolean nextRowWellFormed;
    if (firstColumn) {
      nextRowWellFormed = this.down.checkWellFormedness(false);
    } else {
      nextRowWellFormed = true;
    }
    return this.down.right.samePixel(this.right.down)
        && this.down.left.samePixel(this.left.down)
        && this.up.right.samePixel(this.right.up)
        && this.up.left.samePixel(this.left.up)
        && this.right.checkWellFormedness(false)
        && nextRowWellFormed;
  }

  // Returns whether an APixel and a Pixel are the same. == is used because
  // it checks whether this Pixel and the argument APixel both have the same
  // location in memory, making them the same instance of a Pixel if true
  public boolean samePixel(APixel pixel) {
    return this == pixel;
  }

  // Returns whether a Pixel and a NoPixel are the same, which is false
  public boolean sameNoPixel(NoPixel noPixel) {
    return false;
  }

  // Returns the width of a Pixel, meaning the number of Pixels on its right,
  // this Pixel included
  int getWidth() {
    return 1 + this.right.getWidth();
  }

  // Returns the height of a Pixel, meaning the number of Pixels below it,
  // this Pixel included
  int getHeight() {
    return 1 + this.down.getHeight();
  }


  // Changes the color of the Pixel to red. Effect: the Pixel's
  // color value is changed to red
  public void makeRed() {
    this.color = Color.red;
  }

  // finds the largest vertical seam weight
  public double getLargestSeamWeightVertical() {
    double newLargest = this.getLargestSeamWeightInRow();
    return Math.max(newLargest, this.down.getLargestSeamWeightVertical());
  }

  // finds the largest seam weight in a row
  public double getLargestSeamWeightInRow() {
    return Math.max(this.seam.getSeamWeight(), this.right.getLargestSeamWeightInRow());
  }

  // finds the largest horizontal seam weight
  public double getLargestSeamWeightHorizontal() {
    double newLargest = this.getLargestSeamWeightInColumn();
    return Math.max(newLargest, this.right.getLargestSeamWeightHorizontal());
  }

  // finds the largest seam weight in a column
  public double getLargestSeamWeightInColumn() {
    return Math.max(this.seam.getSeamWeight(), this.down.getLargestSeamWeightInColumn());
  }

}

// An APixel which represents nothing, being the neighbors of Pixels on the
// edge of an image. All NoPixels have black as their color, itself as its neighbors,
// and a NoSeam as its seam
class NoPixel extends APixel {

  // A constructor for a NoPixel, taking in nothing and setting its color as black,
  // neighbors as itself, and seam as NoSeam using the abstract constructor
  NoPixel() {
    super(Color.black);
  }

  // Shifts all Pixels to the right of the NoPixel to the left.
  // Effect: there are no Pixels to the right of a NoPixel, so nothing
  public void shiftRightPixelLeft() {
    return;
  }

  // A helper for shifting all Pixels to the right of the NoPixel left
  // Effect: there are no Pixels to the right of a NoPixel, so nothing
  public void shiftRPLHelp(Pixel shiftingPixel) {
    return;
  }

  // Deletes a NoPixel. Effect: NoPixels can't be deleted, so nothing
  public void deletePixelVertical() {
    return;
  }

  // Shifts all Pixels to the below the NoPixel up.
  // Effect: there are no Pixels below the a NoPixel, so nothing
  public void shiftDownPixelUp() {
    return;
  }

  // A helper for shifting all Pixels below the NoPixel up
  // Effect: there are no Pixels below a NoPixel, so nothing
  public void shiftDPUHelp(Pixel shiftingPixel) {
    return;
  }

  // Deletes a NoPixel. Effect: NoPixels can't be deleted, so nothing
  public void deletePixelHorizontal() {
    return;
  }

  // Updates a NoPixel's right value. Effect: NoPixels always have a NoPixel as a right
  // value, so nothing
  public void setRight(APixel right, boolean setLeft) {
    return;
  }

  // Updates a NoPixel's left value. Effect: NoPixels always have a NoPixel as a left
  // value, so nothing
  public void setLeft(APixel left, boolean setRight) {
    return;
  }

  // Updates a NoPixel's up value. Effect: NoPixels always have a NoPixel as a up
  // value, so nothing
  public void setUp(APixel up, boolean setDown) {
    return;
  }

  // Updates a NoPixel's down value. Effect: NoPixels always have a NoPixel as a down
  // value, so nothing
  public void setDown(APixel down, boolean setUp) {
    return;
  }

  // Calculates a NoPixel's energy, which is 0
  public double calcEnergy() {
    return 0;
  }

  // Calculates a NoPixel's brightness, and since its color is black, its brightness is 0
  public double calcBrightness() {
    return 0;
  }

  // Calculates a NoPixel's vertical energy, which is 0
  public double calcVertical() {
    return 0;
  }

  // Calculates a NoPixel's horizontal energy, which is 0
  public double calcHorizontal() {
    return 0;
  }

  // Creates a vertical seam for a NoPixel. Effect: since NoPixels already
  // have a NoSeam as a seam value, nothing
  public void createSeamVertical(boolean firstColumn) {
    return;
  }

  // A helper for creating a vertical seam for a NoPixel. Effect: since NoPixels already
  // have a NoSeam as a seam value, nothing
  public void createSeamVerticalHelp(boolean firstColumn) {
    return;
  }

  // Returns the least-weight vertical seam between a NoPixel and its left and right neighbors,
  // which are all NoPixels, so the least-weight seam is a NoSeam
  public ISeam findSmallestVertical() {
    return new NoSeam();
  }

  // Deletes or paints the smallest seam in the bottom row of an image by going down
  // to the bottom of the first column. When called on a NoPixel, it means that
  // the first pixel in the bottom row is previous. Effect: one Pixel per row is "deleted," 
  // meaning its left and right pixels point to each other and pixels to the right have updated
  // up and down values, or it is painted red, meaning its color value changed to red and
  // will be deleted on the next tick
  public void deleteSmallestVerticalSeam(APixel previous, boolean drawRed) {
    previous.deleteSmallestVerticalSeamHelp(previous.seam, drawRed);
  }

  // A helper for deleteSmallestVerticalSeam, which moves to the right in the bottom row
  // in order to find the least-weight seam. When called on a NoPixel, it means that every
  // seam has been checked, and the smallest seam is ISeam smallest. If drawRed is true, then the
  // seam is painted red, but if it is false, the seam is deleted. Effect: one Pixel per 
  // row is "deleted," meaning its left and right pixels point to each other and pixels to
  // the right have updated up and down values, or it is painted red, meaning its color value
  // changed to red and will be deleted on the next tick
  public void deleteSmallestVerticalSeamHelp(ISeam smallest, boolean drawRed) {
    if (drawRed) {
      smallest.makeRed();
    } else {
      smallest.deleteSeamVertical();
    }
  }

  // Creates a horizontal seam for a NoPixel. Effect: since NoPixels already
  // have a NoSeam as a seam value, nothing
  public void createSeamHorizontal(boolean firstColumn) {
    return;
  }

  // A helper for creating a horizontal seam for a NoPixel. Effect: since NoPixels already
  // have a NoSeam as a seam value, nothing
  public void createSeamHorizontalHelp(boolean firstColumn) {
    return;
  }

  // Returns the least-weight horizontal seam between a NoPixel and its left and right neighbors,
  // which are all NoPixels, so the least-weight seam is a NoSeam
  public ISeam findSmallestHorizontal() {
    return new NoSeam();
  }

  // Deletes the smallest seam in the rightmost column of an image by going right
  // to the end of the first row. When called on a NoPixel, it means that
  // the first pixel in the rightmost column is previous. Effect: one Pixel per column is 
  // "deleted,"  meaning its up and down pixels point to each other and pixels down have 
  // updated left and right values, or it is painted red, meaning its color value
  // changed to red and will be deleted on the next tick
  public void deleteSmallestHorizontalSeam(APixel previous, boolean drawRed) {
    previous.deleteSmallestHorizontalSeamHelp(previous.seam, drawRed);
  }



  // A helper for deleteSmallestHorizontalSeam, which moves to the down in the rightmost column
  // in order to find the least-weight horizontal seam. When called on a NoPixel, it means that 
  // every seam has been checked, and the smallest seam is ISeam smallest. If drawRed is true,
  // then the seam is painted red, but if it is false, the seam is deleted. Effect: one Pixel per  
  // column is "deleted,"  meaning its up and down pixels point to each other and pixels down have
  // updated left and right values, or it is painted red, meaning its color value
  // changed to red and will be deleted on the next tick
  public void deleteSmallestHorizontalSeamHelp(ISeam smallest, boolean drawRed) {
    if (drawRed) {
      smallest.makeRed();
    } else {
      smallest.deleteSeamHorizontal();
    }
  }

  // Returns whether an APixel is the same as a NoPixel
  public boolean samePixel(APixel pixel) {
    return pixel.sameNoPixel(this);
  }

  // Returns whether a NoPixel and a NoPixel are both NoPixels, which is true
  public boolean sameNoPixel(NoPixel noPixel) {
    return true;
  }

  // Checks that a NoPixel is well-formed, which is true
  public boolean checkWellFormedness(boolean firstColumn) {
    return true;
  }

  // Returns the width of a NoPixel, meaning the number of Pixels on its right,
  // this NoPixel included, which is 0
  int getWidth() {
    return 0;
  }

  // Returns the height of a NoPixel, meaning the number of Pixels below it,
  // this NoPixel included, which is 0
  int getHeight() {
    return 0;
  }


  // makes NoPixel red
  public void makeRed() {
    return;
  }


  // finds the largest Vertical seam weight for a no pixel
  public double getLargestSeamWeightVertical() {
    return 0;
  }

  // finds the largest horizontal seam weight for a no pixel
  public double getLargestSeamWeightHorizontal() {
    return 0;
  }

  // finds the largest seam weight in a row for a no pixel
  public double getLargestSeamWeightInRow() {
    return 0;
  }

  // finds the largest seam weight in a column for a no pixel
  public double getLargestSeamWeightInColumn() {
    return 0;
  }

}

// An interface for an ISeam, which is a linked-list of Pixels in a zig-zag pattern
// in the form of a SeamInfo, or a NoSeam to act as an empty value for a SeamInfo
// with no valid SeamInfo above it
interface ISeam {

  // Returns the least-weight seam between this ISeam, which corresponds to an APixel, 
  // and its left and right neighboring APixel's seams
  public ISeam findSmallest(ISeam seam1, ISeam seam2);

  // Deletes a vertical seam from an image
  void deleteSeamVertical();

  // Deletes a horizontal seam from an image
  void deleteSeamHorizontal();

  // Compares an ISeam to another ISeam, returning the one with the smaller totalWeight
  ISeam compareISeam(ISeam that);

  // Compares an ISeam to a NoSeam, returning the one with the smaller totalWeight
  ISeam compareNoSeam(NoSeam that);

  // Compares an ISeam to a SeamInfo, returning the one with the smaller totalWeight
  ISeam compareSeam(SeamInfo that);
  
  // Gets an ISeam's totalWeight
  double getSeamWeight();

  // makes the seam red
  void makeRed();

}

// Represents a seam, with an APixel pixel which is in the seam, an ISeam cameFrom
// which contains previous pixels in the seam, and double totalWeight representing
// the total weight of the seam
class SeamInfo implements ISeam {
  APixel pixel;
  ISeam cameFrom;
  double totalWeight;


  // A constructor for a SeamInfo, with its pixel value being the given APixel,
  // the cameFrom value being the given ISeam, and the totalWeight being equal to
  // the pixel's energy and the totalWeight of the cameFrom ISeam
  SeamInfo(APixel pixel, ISeam cameFrom) {
    this.pixel = pixel;
    this.cameFrom = cameFrom;
    this.totalWeight = pixel.calcEnergy() + cameFrom.getSeamWeight();
  }

  // A constructor for a SeamInfo, with the pixel value being the given APixel,
  // and the cameFrom being set to a NoSeam, and the totalWeight being
  // set as the Pixel's energy, as the totalWeight of getSeamWeight is 0
  SeamInfo(APixel pixel) {
    this(pixel, new NoSeam());
  }

  // Deletes a vertical seam. Effects: all Pixels to the right of the pixels in the SeamInfo
  // have updated up and down values, and all left and right neighbors to pixels
  // in the SeamInfo point to each other
  public void deleteSeamVertical() {
    pixel.shiftRightPixelLeft();
    pixel.deletePixelVertical();
    cameFrom.deleteSeamVertical();
  }

  // Deletes a horizontal seam. Effects: all Pixels below the pixels in the SeamInfo
  // have updated left and right values, and all up and down neighbors to pixels
  // in the SeamInfo point to each other
  public void deleteSeamHorizontal() {
    pixel.shiftDownPixelUp();
    pixel.deletePixelHorizontal();
    cameFrom.deleteSeamHorizontal();
  }

  // Returns the least-weight SeamInfo between this SeamInfo, which corresponds to an APixel, 
  // and its left and right neighboring APixel's seams or its up and down neighboring APixel's seams
  public ISeam findSmallest(ISeam seam1, ISeam seam2) {
    ISeam smallest = this.compareISeam(seam1);
    smallest = seam2.compareISeam(smallest);
    return smallest;
  }

  // Returns the totalWeight of a SeamInfo
  public double getSeamWeight() {
    return this.totalWeight;
  }

  // Returns the least-weight SeamInfo of a SeamInfo and an ISeam
  public ISeam compareISeam(ISeam that) {
    return that.compareSeam(this);
  }

  // Returns the least-weight SeamInfo of a SeamInfo and a NoSeam, 
  // which is the SeamInfo
  public ISeam compareNoSeam(NoSeam that) {
    return this;
  }

  // Returns the least-weight SeamInfo between two SeamInfos
  public ISeam compareSeam(SeamInfo that) {
    if (this.totalWeight <= that.totalWeight) {
      return this;
    }
    else {
      return that;
    }
  }

  // makes the pixels in a seam red
  public void makeRed() {
    this.pixel.makeRed();
    this.cameFrom.makeRed();
  }

}

// Represents an empty case to use for the SeamInfo's cameFrom field when it
// is in the first row of an image, meaning there is no seam above it
class NoSeam implements ISeam {

  // Returns the least-weight SeamInfo between this NoSeam, which corresponds to an APixel, 
  // and its left and right neighboring APixel's seams
  // or its up and down neighboring APixel's seams
  public ISeam findSmallest(ISeam seam1, ISeam seam2) {
    ISeam smallest = this.compareISeam(seam1);
    smallest = seam2.compareISeam(smallest);
    return smallest;
  }

  // Deletes a NoSeam. Effect: since a NoSeam contains no pixels, no pixels are deleted,
  // so there's no effect
  public void deleteSeamVertical() {
    return;
  }

  // Deletes a NoSeam. Effect: since a NoSeam contains no pixels, no pixels are deleted,
  // so there's no effect
  public void deleteSeamHorizontal() {
    return;
  }

  // Returns the totalWeight of a NoSeam, which is 0
  public double getSeamWeight() {
    return 0;
  }

  // Returns the least-weight SeamInfo between a NoSeam and an ISeam
  public ISeam compareISeam(ISeam that) {
    return that.compareNoSeam(this);
  }

  // Returns the least-weight SeamInfo between a NoSeam and a SeamInfo, 
  // which is the SeamInfo
  public ISeam compareSeam(SeamInfo that) {
    return that;
  }

  // Returns the least-weight SeamInfo between two NoSeams. Since they are both
  // NoSeams, it doesn't matter which is returned, so the first one is returned
  public ISeam compareNoSeam(NoSeam that) {
    return this;
  }

  // makes the pixels in a no seam red. There are no pixels in a NoSeam, so there's
  // no effect
  public void makeRed() {
    return;
  }

}

// A World containing a PixelImage which can be seam carved
class SeamCarvingWorld extends World {

  PixelImage image;
  String currentKey;
  boolean drawRed;
  boolean vertical;
  boolean stop;

  // A constructor for a SeamCarvingWorld which takes in a PixelImage for
  // the image value
  SeamCarvingWorld(PixelImage image) {
    this.image = image;
    this.currentKey = "";
    this.drawRed = true;
    this.vertical = (((int) (Math.random() * 10)) % 2) == 0 ;
    this.stop = false;
  }

  // Creates a scene for the PixelImage to be displayed
  public WorldScene makeScene() {
    WorldScene ws = new WorldScene(image.width, image.height);
    ws.placeImageXY(image.convertToFile(), image.width / 2, image.height / 2);
    return ws;
  }

  // outprints the last scene
  public WorldScene lastScene(String message) {
    WorldScene ws = new WorldScene(image.width, image.height);
    TextImage text = new TextImage("You have ran out of seams to delete :(", 
        20, Color.black);
    ws.placeImageXY(text, image.width / 2, image.height / 2);
    return ws;
  }

  // determines if the world should end
  public boolean shouldWorldEnd() {
    return this.image.topLeftPixel.getHeight() == 1 
        || this.image.topLeftPixel.getWidth() == 1;
  }

  // Carves a seam from the PixelImage on each tick. Effect: on every other tick, unless
  // the scene is paused,a seam is carved from the image, updating the neighbors of the 
  // Pixels in the image and causing the image to gradually shrink. On the off-ticks, 
  // the seam that will next be deleted has all of its Pixel's color values changed to red
  public void onTick() {
    if (stop) {
      if (!this.drawRed) {
        if (this.vertical) {
          this.image.carveSeamVertical();
          this.drawRed = true;
        } else {
          this.image.carveSeamHorizontal();
          this.drawRed = true;
        }
      }
    }

    else {
      if (currentKey.equals("v") || currentKey.equals("V")) {
        if (!this.drawRed && !this.vertical) {
          this.image.carveSeamHorizontal();
          vertical = true;
          this.drawRed = true;
        }
        if (this.drawRed) {
          this.image.drawRedVertical();
          this.drawRed = false;
          vertical = true;
          this.image.largestWeight 
            = this.image.topLeftPixel.getLargestSeamWeightVertical();
        }

        else {
          this.image.carveSeamVertical();
          this.drawRed = true;
          vertical = true;
        }

      }

      else if (currentKey.equals("h") || currentKey.equals("H")) {
        if (!this.drawRed && this.vertical) {
          this.image.carveSeamVertical();
          vertical = false;
          this.drawRed = true;
        }
        if (this.drawRed) {
          this.image.drawRedHorizontal();
          this.drawRed = false;
          vertical = false;
          this.image.largestWeight 
            = this.image.topLeftPixel.getLargestSeamWeightHorizontal();
        }

        else {
          this.image.carveSeamHorizontal();
          this.drawRed = true;
          vertical = false;
        }
      }

      else {
        if (this.drawRed) {
          if (this.vertical) {
            this.image.drawRedVertical();
            this.drawRed = false;
            this.image.largestWeight 
              = this.image.topLeftPixel.getLargestSeamWeightVertical();
          }
          else {
            this.image.drawRedHorizontal();
            this.drawRed = false;
            this.image.largestWeight 
              = this.image.topLeftPixel.getLargestSeamWeightHorizontal();
          }
        }

        else {
          if (this.vertical) {
            this.image.carveSeamVertical();
            vertical = (((int) (Math.random() * 10)) % 2) == 0;
            this.drawRed = true;
          }
          else {
            this.image.carveSeamHorizontal();
            vertical = (((int) (Math.random() * 10)) % 2) == 0;
            this.drawRed = true;
          }

        }

      }

    }

  }
  // True/False to switch between painting red and deleting


  // Saves the current image to a file when "p" is pressed. Effect: does 
  // nothing to the scene, but saves a file containing the current image
  public void onKeyEvent(String key) {
    if (key.equals("p") || key.equals("P")) {
      this.image.convertToFile().saveImage("carvedManySeams.png");
    }

    else if (key.equals(" ")) {
      this.stop = !stop;
    }

    else if (key.equals("v") || key.equals("V")) {
      if (this.currentKey.equalsIgnoreCase("V")) {
        this.currentKey = "q";
      } else {
        this.currentKey = key;
      }
    }

    else if (key.equals("h") || key.equals("H")) {
      if (this.currentKey.equalsIgnoreCase("H")) {
        this.currentKey = "q";
      } else {
        this.currentKey = key;
      }
    }
    else if (key.equals("g") || key.equals("G")) {
      if (this.image.grey.equalsIgnoreCase("energy")) {
        this.image.grey = "";
      } else {
        this.image.grey = "energy";
      }
    }
    else if (key.equals("w") || key.equals("W")) {
      if (this.image.grey.equalsIgnoreCase("weight")) {
        this.image.grey = "";
      } else {
        this.image.grey = "weight";
      }
    }
    else {
      this.currentKey = "q";
      this.image.grey = "";
    }
  }

}

class ExamplePixels {

  SeamInfo s1;
  SeamInfo s2;
  SeamInfo s3;
  SeamInfo s4;
  SeamInfo s5;
  SeamInfo s6;
  APixel topLeft3x3;
  APixel topLeft4x4;
  APixel topLeft5x5;
  APixel balloons;
  APixel test3x3;
  PixelImage image3x3;
  PixelImage balloonsImage;
  PixelImage birdsImage;
  PixelImage test3x3Image;
  PixelImage carvedBalloons;
  SeamCarvingWorld balloonsWorld;
  SeamCarvingWorld birdsWorld;
  SeamCarvingWorld world3x3;
  SeamCarvingWorld test3x3World;
  APixel calc3x3;
  APixel bright;
  APixel dim;
  ISeam ns;
  SeamInfo s15x5;
  SeamInfo s25x5;
  SeamInfo s35x5;
  SeamInfo s45x5;
  SeamInfo s55x5;
  NoPixel np = new NoPixel();


  void initCalc3x3() {
    bright = new Pixel(Color.yellow);
    dim = new Pixel(Color.green);
    calc3x3 = dim;
    calc3x3.setRight(bright, true);
    calc3x3.right.setRight(bright, true);
    calc3x3.setDown(bright, true);
    calc3x3.down.setRight(dim, true);
    calc3x3.right.setDown(dim, true);
    calc3x3.down.right.setRight(bright, true);
    calc3x3.right.right.setDown(bright, true);
    calc3x3.down.setDown(bright, true);
    calc3x3.down.down.setRight(bright, true);
    calc3x3.down.right.setDown(bright, true);
    calc3x3.down.down.right.setRight(dim, true);
    calc3x3.down.right.right.setDown(dim, true);
    // calc3x3
    // dim bright bright
    // bright dim bright
    // bright bright dim
  }

  void initPixels() {
    bright = new Pixel(Color.yellow);
    dim = new Pixel(Color.green);
    ns = new NoSeam();

    topLeft3x3 = new PixelImage(new FromFileImage("testimage3x3.png")).toPixel();
    topLeft4x4 = new PixelImage(new FromFileImage("testimage4x4.png")).toPixel();
    topLeft5x5 = new PixelImage(new FromFileImage("testimage5x5.png")).toPixel();
    balloons = new PixelImage(new FromFileImage("balloons.jpg")).toPixel();
    test3x3 = new PixelImage(new FromFileImage("test3x3.png")).toPixel();
    test3x3Image = new PixelImage(new FromFileImage("test3x3.png"));
    image3x3 = new PixelImage(new FromFileImage("testimage3x3.png"));

    s1 = new SeamInfo(topLeft3x3.right.right, new NoSeam());
    s2 = new SeamInfo(topLeft3x3.right.right.down, s1);
    s3 = new SeamInfo(topLeft3x3.right.right.down.down, s2);
    s4 = new SeamInfo(topLeft3x3,new NoSeam());
    s5 = new SeamInfo(topLeft3x3.right, s4);
    s6 = new SeamInfo(topLeft3x3.right.right, s5);

    balloonsImage = new PixelImage(new FromFileImage("balloons.jpg"));
    birdsImage = new PixelImage(new FromFileImage("birds.jpg"));
    carvedBalloons = new PixelImage(new FromFileImage("carveOneSeam.png"));

    s15x5 = new SeamInfo(topLeft5x5, new NoSeam());
    s25x5 = new SeamInfo(topLeft5x5.right.down, s15x5);
    s35x5 = new SeamInfo(topLeft5x5.right.down.right.down, s25x5);
    s45x5 = new SeamInfo(topLeft5x5.right.down.right.down.down, s35x5);
    s55x5 = new SeamInfo(topLeft5x5.right.down.right.down.down.down, s45x5);


    world3x3 = new SeamCarvingWorld(image3x3);
    balloonsWorld = new SeamCarvingWorld(balloonsImage);
    birdsWorld = new SeamCarvingWorld(birdsImage);
    test3x3World = new SeamCarvingWorld(test3x3Image);

  }


//  void testCreateSeamVertical(Tester t) {
//    this.initPixels();
//
//    test3x3.createSeamVertical(true);
//
//    t.checkInexact(test3x3.seam.getSeamWeight(), 2.388, .01);
//    t.checkInexact(test3x3.right.seam.getSeamWeight(), 2.622, .01);
//    t.checkInexact(test3x3.right.right.seam.getSeamWeight(), 2.897,.01);
//    t.checkInexact(test3x3.down.seam.getSeamWeight(), 4.986 , .01);
//    t.checkInexact(test3x3.down.right.seam.getSeamWeight(), 3.383, .01);
//    t.checkInexact(test3x3.down.right.right.seam.getSeamWeight(), 5.220, .01);
//    t.checkInexact(test3x3.down.down.seam.getSeamWeight(), 6.306, .01);
//    t.checkInexact(test3x3.down.down.right.seam.getSeamWeight(), 6.006, .01);
//    t.checkInexact(test3x3.down.down.right.right.seam.getSeamWeight(), 6.734, .01);
//  }
//
//  void testCreateSeamHorizontal(Tester t) {
//    this.initPixels();
//
//    test3x3.createSeamHorizontal(true);
//
//    t.checkInexact(test3x3.seam.getSeamWeight(), 2.388, .01);
//    t.checkInexact(test3x3.right.seam.getSeamWeight(), 5.021, .01);
//    t.checkInexact(test3x3.right.right.seam.getSeamWeight(), 6.281,.01);
//    t.checkInexact(test3x3.down.seam.getSeamWeight(), 2.603, .01);
//    t.checkInexact(test3x3.down.right.seam.getSeamWeight(), 3.383, .01);
//    t.checkInexact(test3x3.down.right.right.seam.getSeamWeight(), 5.983, .01);
//    t.checkInexact(test3x3.down.down.seam.getSeamWeight(), 2.924, .01);
//    t.checkInexact(test3x3.down.down.right.seam.getSeamWeight(), 5.229, .01);
//    t.checkInexact(test3x3.down.down.right.right.seam.getSeamWeight(), 6.734, .01);
//  }
//
//
//  void testCompareISeam(Tester t) {
//    this.initPixels();
//    this.initCalc3x3();
//
//    t.checkExpect(ns.compareISeam(calc3x3.seam).getSeamWeight(),
//        calc3x3.seam.getSeamWeight());
//    t.checkExpect(calc3x3.seam.compareISeam(calc3x3.right.seam).getSeamWeight(),
//        calc3x3.seam.getSeamWeight());
//    t.checkExpect(test3x3.seam.compareISeam(test3x3.right.seam).getSeamWeight(),
//        test3x3.seam.getSeamWeight());
//    t.checkExpect(ns.compareISeam(ns).getSeamWeight(), ns.getSeamWeight());
//    t.checkExpect(calc3x3.seam.compareISeam(ns).getSeamWeight(),
//        calc3x3.seam.getSeamWeight());
//  }
//
//
//
//
//  boolean testCompareSeam(Tester t) {
//
//    this.initCalc3x3();
//    this.initPixels();
//
//    return t.checkExpect(ns.compareSeam((SeamInfo)calc3x3.seam).getSeamWeight(), 
//        calc3x3.seam.getSeamWeight())
//        && t.checkExpect(calc3x3.seam.compareSeam((SeamInfo)calc3x3.right.seam).getSeamWeight(), 
//            calc3x3.seam.getSeamWeight())
//        && t.checkExpect(test3x3.seam.compareSeam((SeamInfo)test3x3.right.seam).getSeamWeight(), 
//            test3x3.seam.getSeamWeight());
//  }
//
//
//
//  boolean testCompareNoSeam(Tester t) {
//
//    this.initCalc3x3();
//    this.initPixels();
//
//    return t.checkExpect(ns.compareNoSeam((NoSeam)ns).getSeamWeight(), ns.getSeamWeight())
//        && t.checkExpect(calc3x3.seam.compareNoSeam((NoSeam)ns).getSeamWeight(), 
//            calc3x3.seam.getSeamWeight());
//
//  }
//
//
//  void testFindSmallestISeam(Tester t) {
//
//    this.initPixels();
//    test3x3.createSeamVertical(true);
//
//
//    ISeam smallS = test3x3.seam;
//    ISeam bigS = test3x3.down.seam;
//    t.checkExpect(bigS.findSmallest(smallS, bigS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(smallS.findSmallest(bigS, bigS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(bigS.findSmallest(bigS, smallS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(bigS.findSmallest(bigS, bigS).getSeamWeight(), bigS.getSeamWeight());
//    t.checkExpect(smallS.findSmallest(smallS, smallS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(smallS.findSmallest(smallS, bigS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(bigS.findSmallest(smallS, smallS).getSeamWeight(), smallS.getSeamWeight());
//    t.checkExpect(smallS.findSmallest(bigS, smallS).getSeamWeight(), smallS.getSeamWeight());
//  }
//
//
//  void testShiftRightPixelLeft(Tester t) {
//
//    this.initPixels();
//
//    APixel middle = topLeft3x3.down.right;
//    APixel rightMiddle = middle.right;
//
//    t.checkExpect(middle.up.color, Color.black);
//    t.checkExpect(middle.down.color, Color.blue);
//    t.checkExpect(rightMiddle.up.color, Color.black);
//    t.checkExpect(rightMiddle.down.color, Color.green);
//    topLeft3x3.down.shiftRightPixelLeft();
//    t.checkExpect(middle.up.color, Color.red);
//    t.checkExpect(middle.down.color, Color.blue);
//    t.checkExpect(rightMiddle.up.color, Color.black);
//    t.checkExpect(rightMiddle.down.color, Color.blue);
//  }
//
//  void testShiftRPLHelp(Tester t) {
//
//    this.initPixels();
//
//    APixel middle = topLeft3x3.down.right;
//    APixel rightMiddle = middle.right;
//
//    t.checkExpect(middle.up.color, Color.black);
//    t.checkExpect(middle.down.color, Color.blue);
//    t.checkExpect(rightMiddle.up.color, Color.black);
//    t.checkExpect(rightMiddle.down.color, Color.green);
//    topLeft3x3.down.right.shiftRPLHelp((Pixel)topLeft3x3.down);
//    t.checkExpect(middle.up.color, Color.red);
//    t.checkExpect(middle.down.color, Color.blue);
//    t.checkExpect(rightMiddle.up.color, Color.black);
//    t.checkExpect(rightMiddle.down.color, Color.green);
//  }
//
//  void testShiftDownPixelUp(Tester t) {
//    this.initPixels();
//
//    APixel middle = topLeft3x3.down.right;
//    APixel downMiddle = middle.down;
//
//    t.checkExpect(middle.left.color, Color.red);
//    t.checkExpect(middle.right.color, Color.green);
//    t.checkExpect(downMiddle.left.color, Color.blue);
//    t.checkExpect(downMiddle.right.color, Color.green);
//    topLeft3x3.right.shiftDownPixelUp();
//    t.checkExpect(middle.left.color, Color.red);
//    t.checkExpect(middle.right.color, Color.black);
//    t.checkExpect(downMiddle.left.color, Color.red);
//    t.checkExpect(downMiddle.right.color, Color.green);
//  }
//
//  void testShiftDPUHelp(Tester t) {
//    this.initPixels();
//
//    APixel middle = topLeft3x3.down.right;
//    APixel downMiddle = middle.down;
//
//    t.checkExpect(middle.left.color, Color.red);
//    t.checkExpect(middle.right.color, Color.green);
//    t.checkExpect(downMiddle.left.color, Color.blue);
//    t.checkExpect(downMiddle.right.color, Color.green);
//    topLeft3x3.right.down.shiftDPUHelp((Pixel)topLeft3x3.right);
//    t.checkExpect(middle.left.color, Color.red);
//    t.checkExpect(middle.right.color, Color.black);
//    t.checkExpect(downMiddle.left.color, Color.blue);
//    t.checkExpect(downMiddle.right.color, Color.green);
//  }
//
//  void testDeletePixelVertical(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.left.color, Color.white);
//    topLeft3x3.down.right.deletePixelVertical();
//    t.checkExpect(topLeft3x3.down.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.right.left.color, Color.red);
//
//  }
//
//  void testDeletePixelHorizontal(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(topLeft3x3.right.down.color, Color.white);
//    t.checkExpect(topLeft3x3.right.down.down.up.color, Color.white);
//    topLeft3x3.down.right.deletePixelHorizontal();
//    t.checkExpect(topLeft3x3.right.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.right.down.up.color, Color.black);
//  }
//
//
//
//  void testSetRight(Tester t) {
//
//    this.initCalc3x3();
//
//    APixel test = dim;
//
//    t.checkExpect(test.right.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test.setRight(bright, true);
//    t.checkExpect(test.right.seam.getSeamWeight(), bright.seam.getSeamWeight());
//    t.checkExpect(bright.left.seam.getSeamWeight(), test.seam.getSeamWeight());
//
//    this.initCalc3x3();
//    APixel test2 = bright;
//
//    t.checkExpect(test2.right.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test2.setRight(dim, false);
//    t.checkExpect(test2.right.seam.getSeamWeight(), dim.seam.getSeamWeight());
//    t.checkExpect(dim.left.seam.getSeamWeight(), np.seam.getSeamWeight());
//
//  }
//
//
//  void testCreateSeamHelp(Tester t) {
//
//    this.initPixels();
//
//    test3x3.createSeamVerticalHelp(true);
//
//    t.checkInexact(test3x3.seam.getSeamWeight(), 2.388, .01);
//    t.checkInexact(test3x3.right.seam.getSeamWeight(), 2.622, .01);
//    t.checkInexact(test3x3.right.right.seam.getSeamWeight(), 2.897,.01);
//    t.checkInexact(test3x3.down.seam.getSeamWeight(), 4.986 , .01);
//    t.checkInexact(test3x3.down.right.seam.getSeamWeight(), 3.383, .01);
//    t.checkInexact(test3x3.down.right.right.seam.getSeamWeight(), 5.220, .01);
//    t.checkInexact(test3x3.down.down.seam.getSeamWeight(), 6.306, .01);
//    t.checkInexact(test3x3.down.down.right.seam.getSeamWeight(), 6.006, .01);
//    t.checkInexact(test3x3.down.down.right.right.seam.getSeamWeight(), 6.734, .01);
//  }
//
//
//  void testSetLeft(Tester t) {
//
//    this.initCalc3x3();
//
//    APixel test = dim;
//
//    t.checkExpect(test.left.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test.setLeft(bright, true);
//    t.checkExpect(test.left.seam.getSeamWeight(), bright.seam.getSeamWeight());
//    t.checkExpect(bright.right.seam.getSeamWeight(), test.seam.getSeamWeight());
//
//    this.initCalc3x3();
//
//    APixel test2 = bright;
//
//    t.checkExpect(test2.left.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test2.setLeft(dim, false);
//    t.checkExpect(test2.left.seam.getSeamWeight(), dim.seam.getSeamWeight());
//    t.checkExpect(dim.right.seam.getSeamWeight(), np.seam.getSeamWeight());
//
//  }
//
//
//  void testSetUp(Tester t) {
//
//    this.initCalc3x3();
//
//    APixel test = dim;
//
//    t.checkExpect(test.up.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test.setUp(bright, true);
//    t.checkExpect(test.up.seam.getSeamWeight(), bright.seam.getSeamWeight());
//    t.checkExpect(bright.down.seam.getSeamWeight(), test.seam.getSeamWeight());
//
//
//    this.initCalc3x3();
//    APixel test2 = bright;
//
//    t.checkExpect(test2.up.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test2.setUp(dim, false);
//    t.checkExpect(test2.up.seam.getSeamWeight(), dim.seam.getSeamWeight());
//    t.checkExpect(dim.down.seam.getSeamWeight(), np.seam.getSeamWeight());
//
//  }
//
//
//
//  void testSetDown(Tester t) {
//
//    this.initCalc3x3();
//
//    APixel test = dim;
//
//    t.checkExpect(test.down.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test.setDown(bright, true);
//    t.checkExpect(test.down.seam.getSeamWeight(), bright.seam.getSeamWeight());
//    t.checkExpect(bright.up.seam.getSeamWeight(), test.seam.getSeamWeight());
//
//    this.initCalc3x3();
//
//    APixel test2 = bright;
//
//    t.checkExpect(test2.down.seam.getSeamWeight(), np.seam.getSeamWeight());
//    test2.setDown(dim, false);
//    t.checkExpect(test2.down.seam.getSeamWeight(), dim.seam.getSeamWeight());
//    t.checkExpect(dim.up.seam.getSeamWeight(), np.seam.getSeamWeight());
//
//  }
//
//
//  boolean testCalcEnergy(Tester t) {
//
//    this.initPixels();
//
//    return t.checkInexact(test3x3.right.down.calcEnergy(), .989, .1)
//        && t.checkInexact(np.calcEnergy(), 0.0, .1)
//        && t.checkInexact(test3x3.down.calcEnergy(), 2.603, .1)
//        && t.checkInexact(test3x3.right.down.down.calcEnergy(), 2.625, 0.1)
//        && t.checkInexact(test3x3.right.right.calcEnergy(), 2.901, .1)
//        && t.checkInexact(test3x3.right.down.calcEnergy(), .989, .1 )
//        && t.checkInexact(test3x3.right.calcEnergy(), 2.625, .1 );
//
//  }
//
//
//  boolean testCalcBrightness(Tester t) {
//
//    this.initPixels();
//
//    return t.checkInexact(dim.calcBrightness(), .333, .1)
//        && t.checkInexact(bright.calcBrightness(), .666, .1)
//        && t.checkInexact(np.calcBrightness(), 0.0, .01)
//        && t.checkInexact(this.topLeft3x3.calcBrightness(), .3333, .1)
//        && t.checkInexact(this.topLeft3x3.right.calcBrightness(), 0.0, .1)
//        && t.checkInexact(this.topLeft3x3.right.down.calcBrightness(), 1.0, .1)
//        && t.checkInexact(topLeft3x3.down.down.calcBrightness(), .333, .1)
//        && t.checkInexact(this.topLeft3x3.right.right.down.calcBrightness(), .3333, .1)
//        && t.checkInexact(test3x3.calcBrightness(), .555, .01)
//        && t.checkInexact(test3x3.down.calcBrightness(), .5777, .01)
//        && t.checkInexact(test3x3.down.down.right.calcBrightness(), .9058, .01);
//
//  }
//
//
//  boolean testCalcVertical(Tester t) {
//
//    this.initPixels();
//
//    return t.checkInexact(dim.calcVertical(), 0.0, 0.01)
//        && t.checkInexact(test3x3.right.down.calcVertical(), -.700, 0.01)
//        && t.checkInexact(test3x3.right.calcVertical(), -2.594, 0.1);
//
//  }
//
//  boolean testCalcHorizontal(Tester t) {
//
//    this.initPixels();
//
//    return t.checkInexact(np.calcHorizontal(), 0.0, .1)
//        && t.checkInexact(dim.calcVertical(), 0.0, 0.01)
//        && t.checkInexact(test3x3.right.down.calcVertical(), -.700, 0.01)
//        && t.checkInexact(test3x3.right.right.calcVertical(), -2.594, 0.1);
//
//  }
//
//  void testDeleteSmallestVerticalSeam(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    topLeft3x3.createSeamVertical(true);
//    topLeft3x3.deleteSmallestVerticalSeam(topLeft3x3, false);
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//
//  }
//
//  void testDeleteSmallestHorizontalSeam(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    topLeft3x3.createSeamHorizontal(true);
//    topLeft3x3.deleteSmallestHorizontalSeam(topLeft3x3, false);
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//  }
//
//  void testDeleteSmallestSeamVerticalHelp(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    topLeft3x3.createSeamVertical(true);
//    new NoPixel().deleteSmallestVerticalSeamHelp(topLeft3x3.down.down.seam, false);
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//
//  }
//
//  void tesetDeleteSmallestHorizontalHelp(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    topLeft3x3.createSeamHorizontal(true);
//    new NoPixel().deleteSmallestHorizontalSeamHelp(topLeft3x3.down.down.seam, false);
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//
//  }
//
//
//
//  void testToPixel(Tester t) {
//
//    this.initPixels();
//
//    APixel finish;
//    Color purple = new Color(187,255,0);
//    Color tan = new Color(255,189,250);
//    Color green = new Color(166, 29, 230);
//
//    finish = new Pixel(green);
//    finish.setRight(new Pixel(green), true);
//    finish.setDown(new Pixel(purple), true);
//    finish.right.setRight(new Pixel(purple), true);
//    finish.right.setDown(new Pixel(green), true);
//    finish.down.setRight(finish.right.down, true);
//    finish.down.right.setUp(finish.right, true);
//    finish.right.right.setDown(new Pixel(tan), true);
//    finish.right.right.down.setLeft(finish.right.down, true);
//    finish.down.setDown(new Pixel(green), true);
//    finish.down.down.setRight(new Pixel(tan), true);
//    finish.down.down.right.setUp(finish.right.down, true);
//    finish.down.down.right.setRight(new Pixel(purple), true);
//    finish.down.down.right.right.setUp(finish.right.right.down, true);
//
//
//    finish.createSeamVertical(true);
//    test3x3.createSeamVertical(true);
//
//
//    t.checkInexact(finish.seam.getSeamWeight(), 
//        test3x3.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.right.seam.getSeamWeight(), 
//        test3x3.right.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.right.right.seam.getSeamWeight(), 
//        test3x3.right.right.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.seam.getSeamWeight(), 
//        test3x3.down.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.right.seam.getSeamWeight(), 
//        test3x3.down.right.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.right.right.seam.getSeamWeight(), 
//        test3x3.down.right.right.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.down.seam.getSeamWeight(), 
//        test3x3.down.down.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.down.right.seam.getSeamWeight(), 
//        test3x3.down.down.right.seam.getSeamWeight(), .01);
//    t.checkInexact(finish.down.down.right.right.seam.getSeamWeight(), 
//        test3x3.down.down.right.right.seam.getSeamWeight(), .01);
//
//  }
//
//
//  void testFindSmallestVertical(Tester t) {
//
//    this.initPixels();
//
//    test3x3.createSeamVertical(true);
//
//    t.checkExpect(test3x3.findSmallestVertical().getSeamWeight(), 
//        test3x3.seam.getSeamWeight());
//    t.checkExpect(test3x3.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.seam.getSeamWeight());
//    t.checkExpect(test3x3.right.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.findSmallestVertical().getSeamWeight(), 
//        test3x3.right.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.right.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.right.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.right.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.findSmallestVertical().getSeamWeight(), 
//        test3x3.down.down.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.down.down.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.right.right.findSmallestVertical().getSeamWeight(), 
//        test3x3.down.down.right.seam.getSeamWeight());
//  }
//
//  void testFindSmallestHorizontal(Tester t) {
//    this.initPixels();
//
//    test3x3.createSeamHorizontal(true);
//
//    t.checkExpect(test3x3.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.seam.getSeamWeight());
//    t.checkExpect(test3x3.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.right.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.right.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.right.right.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.down.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.right.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.down.right.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.down.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.down.right.seam.getSeamWeight());
//    t.checkExpect(test3x3.down.down.right.right.findSmallestHorizontal().getSeamWeight(), 
//        test3x3.down.right.right.seam.getSeamWeight());
//  }
//
//
//  void testDeleteSeamVertical(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.down.color, Color.green);
//    t.checkExpect(topLeft3x3.right.right.down.down.color, Color.green);
//    s3.deleteSeamVertical();
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.down.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.down.down.color, Color.black);
//
//  }
//
//  void testDeleteSeamHorizontal(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    s6.deleteSeamHorizontal();
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.white);
//    t.checkExpect(topLeft3x3.right.right.color, Color.green);
//  }
//
//
//
//  void testCarveSeamVertical(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    image3x3.topLeftPixel.createSeamVertical(true);
//    image3x3.carveSeamVertical();
//    t.checkExpect(image3x3.topLeftPixel.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.checkWellFormedness(true), true);
//    image3x3.topLeftPixel.createSeamVertical(true);
//    image3x3.carveSeamVertical();
//    t.checkExpect(image3x3.topLeftPixel.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.checkWellFormedness(true), true);
//
//  }
//
//  void testCarveSeamHorizontal(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.white);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.down.right.right.color, Color.green);
//    image3x3.topLeftPixel.createSeamHorizontal(true);
//    image3x3.carveSeamHorizontal();
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.down.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.right.color, Color.blue);
//    t.checkExpect(image3x3.topLeftPixel.down.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.checkWellFormedness(true), true);
//    image3x3.topLeftPixel.createSeamHorizontal(true);
//    image3x3.carveSeamHorizontal();
//    t.checkExpect(image3x3.topLeftPixel.color, Color.red);
//    t.checkExpect(image3x3.topLeftPixel.right.color, Color.black);
//    t.checkExpect(image3x3.topLeftPixel.right.right.color, Color.green);
//    t.checkExpect(image3x3.topLeftPixel.checkWellFormedness(true), true);
//  }
//
//  void testMakeRedSeam(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    topLeft3x3.createSeamVertical(true);
//    topLeft3x3.down.down.seam.makeRed();    
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//  }
//
//  void testColorRedVertical(Tester t) {
//    this.initPixels();
//
//    topLeft3x3.createSeamVertical(true);
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    topLeft3x3.deleteSmallestVerticalSeam(topLeft3x3, true);
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.red);
//    t.checkExpect(np.color, Color.black);
//    np.deleteSmallestVerticalSeam(np, true);
//    t.checkExpect(np.color, Color.black);
//
//  }
//
//  void testColorRedHorizontal(Tester t) {
//    this.initPixels();
//
//
//    topLeft3x3.createSeamHorizontal(true);
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    topLeft3x3.deleteSmallestHorizontalSeam(topLeft3x3, true);
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    t.checkExpect(np.color, Color.black);
//    np.deleteSmallestHorizontalSeam(np, true);
//    t.checkExpect(np.color, Color.black);
//  }
//
//  void testColorRedVeritcalHelp(Tester t) {
//    this.initPixels();
//
//    topLeft3x3.createSeamVertical(true);
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    topLeft3x3.down.down.deleteSmallestVerticalSeamHelp(topLeft3x3.down.down.seam, true);
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.red);
//
//  }
//
//  void testColorRedHorizontalHelp(Tester t) {
//    this.initPixels();
//
//    topLeft3x3.createSeamVertical(true);
//
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.white);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//    topLeft3x3.down.down.deleteSmallestHorizontalSeamHelp(topLeft3x3.down.down.seam, true);
//    t.checkExpect(topLeft3x3.color, Color.red);
//    t.checkExpect(topLeft3x3.right.color, Color.black);
//    t.checkExpect(topLeft3x3.right.right.color, Color.black);
//    t.checkExpect(topLeft3x3.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.color, Color.red);
//    t.checkExpect(topLeft3x3.down.right.right.color, Color.green);
//    t.checkExpect(topLeft3x3.down.down.color, Color.red);
//    t.checkExpect(topLeft3x3.down.down.right.color, Color.blue);
//    t.checkExpect(topLeft3x3.down.down.right.right.color, Color.green);
//  }
//
//  void testMakeRedPixel(Tester t) {
//    this.initPixels();
//
//    t.checkExpect(dim.color, Color.green);
//    dim.makeRed();
//    t.checkExpect(dim.color, Color.red);
//    t.checkExpect(np.color, Color.black);
//    np.makeRed();
//    t.checkExpect(np.color, Color.black);
//
//
//
//
//  }
//
//  // test when each different input is in that it works
//  //cases
//  // stop on and off
//  // v is pressed
//  // h is pressed
//  // draw red for both on one tick
//  // delete seam for another tick
//  void testOnTick(Tester t) {
//    this.initPixels();
//
//    SeamCarvingWorld stopVersion = test3x3World;
//    stopVersion.stop = true;
//    stopVersion.onTick();
//    t.checkExpect(stopVersion, test3x3World);
//
//    SeamCarvingWorld vVersion1 = test3x3World;
//    vVersion1.currentKey = "v";
//    SeamCarvingWorld vVersion2 = vVersion1;
//    vVersion1.onTick();
//    vVersion2.image.drawRedVertical();
//    t.checkExpect(vVersion1, vVersion2);
//
//    vVersion1.onTick();
//    vVersion2.image.carveSeamVertical();
//    t.checkExpect(vVersion1, vVersion2);
//
//    SeamCarvingWorld hVersion1 = test3x3World;
//    hVersion1.currentKey = "h";
//    SeamCarvingWorld hVersion2 = hVersion1;
//    hVersion1.onTick();
//    hVersion2.image.drawRedHorizontal();
//    t.checkExpect(hVersion1, hVersion2);
//
//    hVersion1.onTick();
//    hVersion2.image.carveSeamHorizontal();
//    t.checkExpect(hVersion1, hVersion2);
//
//    stopVersion.currentKey = "h";
//    stopVersion.onTick();
//    t.checkExpect(stopVersion, test3x3World);
//
//    stopVersion.currentKey = "v";
//    t.checkExpect(stopVersion, test3x3World);
//
//  }
//
//  // space is pressed = stop
//  // v or V or h or H = delete vertical seams or horizontal seams
//  // g or G = greyscale based on energy
//  // w or W = greyscale based on seam weight
//  // p or P = save image
//  // random key is pressed = reset to default, which is color and random direction
//  void testOnKeyEvent(Tester t) {
//
//    this.initPixels();
//
//    t.checkExpect(balloonsWorld.stop, false);
//    balloonsWorld.onKeyEvent(" ");
//    t.checkExpect(balloonsWorld.stop, true);
//    t.checkExpect(balloonsWorld.currentKey, "");
//
//    balloonsWorld.onKeyEvent("v");
//    t.checkExpect(balloonsWorld.currentKey, "v");
//    balloonsWorld.onKeyEvent("V");
//    t.checkExpect(balloonsWorld.currentKey, "q");
//
//    balloonsWorld.onKeyEvent("h");
//    t.checkExpect(balloonsWorld.currentKey, "h");
//    balloonsWorld.onKeyEvent("H");
//    t.checkExpect(balloonsWorld.currentKey, "q");
//    t.checkExpect(balloonsWorld.image.grey, "");
//
//    balloonsWorld.onKeyEvent("g");
//    t.checkExpect(balloonsWorld.image.grey, "energy");
//    balloonsWorld.onKeyEvent("G");
//    t.checkExpect(balloonsWorld.image.grey, "");
//
//    balloonsWorld.onKeyEvent("l");
//    t.checkExpect(balloonsWorld.currentKey, "q");
//    balloonsWorld.onKeyEvent("5");
//    t.checkExpect(balloonsWorld.currentKey, "q");
//
//    balloonsWorld.onTick();
//    balloonsWorld.onTick();
//    balloonsWorld.onKeyEvent("g");
//    t.checkExpect(balloonsWorld.image.grey, "energy");
//    balloonsWorld.onKeyEvent("w");
//    t.checkExpect(balloonsWorld.image.grey, "weight");
//    balloonsWorld.onKeyEvent("G");
//    t.checkExpect(balloonsWorld.image.grey, "energy");
//    balloonsWorld.onKeyEvent("W");
//    t.checkExpect(balloonsWorld.image.grey, "weight");
//
//  }
//
//  void testShouldWorldEnd(Tester t) {
//    this.initPixels();
//    t.checkExpect(world3x3.shouldWorldEnd(), false);
//    world3x3.onKeyEvent("v");
//    for (int i = 0; i < 20; i += 1) {
//      world3x3.onTick();
//    }
//    t.checkExpect(world3x3.shouldWorldEnd(), true);
//
//    this.initPixels();
//    world3x3.onKeyEvent("v");
//    t.checkExpect(world3x3.shouldWorldEnd(), false);
//
//    for (int i = 0; i < 20; i += 1) {
//      world3x3.onTick();
//    }
//    t.checkExpect(world3x3.shouldWorldEnd(), true);
//  }
//
//  void testLastScene(Tester t) {
//    this.initPixels();
//
//    PixelImage image = balloonsWorld.image;
//    WorldScene ws = new WorldScene(image.width, image.height);
//    TextImage text = new TextImage("You have ran out of seams to delete :(", 
//        20, Color.black);
//    ws.placeImageXY(text, image.width / 2, image.height / 2);
//
//    t.checkExpect(balloonsWorld.lastScene("The world has ended"), ws);
//  }
//
//  void testWellFormedness(Tester t) {
//    this.initPixels();
//    t.checkExpect(balloonsImage.topLeftPixel.checkWellFormedness(true), true);
//    balloonsImage.carveSeamVertical();
//    t.checkExpect(balloonsImage.topLeftPixel.checkWellFormedness(true), true);
//    this.initPixels();
//    balloonsImage.carveSeamHorizontal();
//    t.checkExpect(balloonsImage.topLeftPixel.checkWellFormedness(true), true);
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamVertical();
//    balloonsImage.carveSeamVertical();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamVertical();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamHorizontal();
//    balloonsImage.carveSeamVertical();
//    balloonsImage.carveSeamVertical();
//    t.checkExpect(balloonsImage.topLeftPixel.checkWellFormedness(true), true);
//    balloonsImage.topLeftPixel.right.right.down.deletePixelHorizontal();
//    t.checkException(new IllegalArgumentException("Deleting seam undid well-formedness"), 
//        balloonsImage, "carveSeamHorizontal");
//    t.checkException(new IllegalArgumentException("Deleting seam undid well-formedness"), 
//        balloonsImage, "carveSeamVertical");
//
//  }
//
//
//  void testGetLargestSeam(Tester t) {
//    this.initPixels();
//    topLeft3x3.createSeamVertical(true);
//    t.checkExpect(topLeft3x3.getLargestSeamWeightVertical(), 
//        topLeft3x3.down.down.right.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.getLargestSeamWeightInRow(), 
//        topLeft3x3.right.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.down.getLargestSeamWeightInRow(), 
//        topLeft3x3.down.right.right.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.down.down.getLargestSeamWeightInRow(), 
//        topLeft3x3.down.down.right.seam.getSeamWeight());
//    topLeft3x3.createSeamHorizontal(true);
//    t.checkExpect(topLeft3x3.getLargestSeamWeightHorizontal(), 
//        topLeft3x3.right.right.down.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.getLargestSeamWeightInColumn(), 
//        topLeft3x3.down.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.right.getLargestSeamWeightInColumn(), 
//        topLeft3x3.right.down.down.seam.getSeamWeight());
//    t.checkExpect(topLeft3x3.right.right.getLargestSeamWeightInColumn(), 
//        topLeft3x3.right.right.down.seam.getSeamWeight());
//  }



  void testBigBang(Tester t) {

    this.initPixels();

    birdsWorld.bigBang(birdsWorld.image.width, birdsWorld.image.height, .001);

  }

}
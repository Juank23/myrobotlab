/**
 *                    
 * @author greg (at) myrobotlab.org
 *  
 * This file is part of MyRobotLab (http://myrobotlab.org).
 *
 * MyRobotLab is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version (subject to the "Classpath" exception
 * as provided in the LICENSE.txt file that accompanied this code).
 *
 * MyRobotLab is distributed in the hope that it will be useful or fun,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * All libraries in thirdParty bundle are subject to their own license
 * requirements - please refer to http://myrobotlab.org/libraries for 
 * details.
 * 
 * Enjoy !
 * 
 * */

package org.myrobotlab.opencv;

import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_features2d.*;
import static org.bytedeco.javacpp.opencv_flann.*;
import static org.bytedeco.javacpp.opencv_highgui.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.*;
import static org.bytedeco.javacpp.opencv_ml.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;
import static org.bytedeco.javacpp.opencv_photo.*;
import static org.bytedeco.javacpp.opencv_shape.*;
import static org.bytedeco.javacpp.opencv_stitching.*;
import static org.bytedeco.javacpp.opencv_video.*;
import static org.bytedeco.javacpp.opencv_videostab.*;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_imgproc.cvPyrDown;

import java.awt.Color;
import java.nio.ByteBuffer;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.OpenCV;
import org.slf4j.Logger;

public class OpenCVFilterKinectNavigate extends OpenCVFilter {

  // useful data for the kinect is 632 X 480 - 8 pixels on the right edge are
  // not good data
  // http://groups.google.com/group/openkinect/browse_thread/thread/6539281cf451ae9e?pli=1

  private static final long serialVersionUID = 1L;

  public final static Logger log = LoggerFactory.getLogger(OpenCVFilterKinectNavigate.class);

  int filter = 7;
  boolean createMask = false;

  transient IplImage dst = null;
  transient IplImage src = null;
  transient IplImage mask = null;

  transient IplImage lastDepth = null;

  int x = 0;
  int y = 0;
  int clickCounter = 0;

  double minX = 0;
  double maxX = 65535;
  double minY = 0.0;
  double maxY = 1.0;

  boolean displayCamera = false;

  public OpenCVFilterKinectNavigate() {
    super();
  }

  public OpenCVFilterKinectNavigate(String name) {
    super(name);
  }

  public void setDisplayCamera(boolean b) {
    displayCamera = b;
  }

  public void createMask() {
    createMask = true;
  }

  @Override
  public void imageChanged(IplImage image) {
    // TODO Auto-generated method stub

  }

  @Override
  public IplImage process(IplImage image, OpenCVData data) throws InterruptedException {

    // INFO - This filter has 2 sources !!!
    IplImage depth = data.get(OpenCV.SOURCE_KINECT_DEPTH);
    if (depth != null) {

      lastDepth = depth;

      // we will translate the 1 channel 16 bit depth into a 3 channel 8 bit rgb
      IplImage color = IplImage.create(depth.width(), depth.height(), IPL_DEPTH_8U, 3);

      ByteBuffer colorBuffer = color.getByteBuffer();
      // it may be deprecated but the "new" function .asByteBuffer() does not
      // return all data
      ByteBuffer depthBuffer = depth.getByteBuffer();

      int depthBytesPerChannel = depth.depth() / 8;
      int colorBytesPerChannel = color.depth() / 8;
      //int bytesPerX = depthBytesPerChannel * lastDepth.nChannels();

      // sentinel values are:
      // Too near: 0x0000
      // Too far: 0x7ff8
      // Unknown: 0xfff8

      // iterate through the depth bytes bytes and convert to HSV / RGB format
      // map depth gray (0,65535) => 3 x (0,255) HSV :P
      for (int y = 0; y < depth.height(); y++) { // 480
        for (int x = 0; x < depth.width(); x++) { // 640
          int depthIndex = y * depth.widthStep() + x * depth.nChannels() * depthBytesPerChannel;
          int colorIndex = y * color.widthStep() + x * color.nChannels() * colorBytesPerChannel;

          // int value = buffer.get(y * bytesPerX * lastDepth.width() + x *
          // bytesPerX) & 0xFF;

          // Used to read the pixel value - the 0xFF is needed to cast from
          // an unsigned byte to an int.
          // int value = depthBuffer.get(depthIndex);// << 8 & 0xFF +
          // buffer.get(depthIndex+1)& 0xFF;
          // this is 16 bit depth - I switched the MSB !!!!
           int value = (depthBuffer.get(depthIndex + 1) & 0xFF) << 8 | (depthBuffer.get(depthIndex) & 0xFF);
          // int value = (depthBuffer.get(depthIndex) & 0xFF) << 8 | (depthBuffer.get(depthIndex + 1) & 0xFF);
          // int value = (depthBuffer.get(depthIndex) & 0xFF) | (depthBuffer.get(depthIndex + 1) << 8  & 0xFF);
          double hsv = minY + ((value - minX) * (maxY - minY)) / (maxX - minX);
          // log.warn(String.format("(%d, %d) = %d => %f", x, y, value, hsv));

          Color c = Color.getHSBColor((float) hsv, 0.9f, 0.9f);
          
          if (value == 0x0000) {
            // too near
            c = Color.RED;
          } else if (value == 0x7ff8) {
            c = Color.WHITE;
           // } else if (value == 0xfff8) {
          } else if (value == 0xff07) {
            c = Color.BLACK;
          }

          if (color.nChannels() == 3) {
            colorBuffer.put(colorIndex, (byte) c.getBlue());
            colorBuffer.put(colorIndex + 1, (byte) c.getRed());
            colorBuffer.put(colorIndex + 2, (byte) c.getGreen());
          } else if (color.nChannels() == 1) {
            colorBuffer.put(colorIndex, (byte) c.getBlue());
          }

          // Sets the pixel to a value (greyscale).
          // colorBuffer.put(index, (byte)hsv);

          // Sets the pixel to a value (RGB, stored in BGR order).
          // buffer.put(index, blue);
          // buffer.put(index + 1, green);
          // buffer.put(index + 2, red);
        }
      }

      boolean processDepth = false;

      if (!processDepth) {
        return color;
      }

      // IplImage color = IplImage.create(img.width(), img.height(),
      // IPL_DEPTH_8U, 3);
      // cvCvtColor(img, color, CV_GRAY2RGB );

      // BufferedImage image = OpenCV.IplImageToBufferedImage(img);

      // SerializableImage.writeToFile(image, "test.png");

      if (depth != null && processDepth) {

        // allowing publish & fork
        if (dst == null || dst.width() != image.width() || dst.nChannels() != image.nChannels()) {
          dst = cvCreateImage(cvSize(depth.width() / 2, depth.height() / 2), depth.depth(), depth.nChannels());
        }

        cvPyrDown(depth, dst, filter);
        invoke("publishDisplay", "kinectDepth", OpenCV.IplImageToBufferedImage(dst));
      }
      // end fork

      if (displayCamera) {
        return image;
      }

      return depth;

    } else {
      lastDepth = image;
    }

    return image;
    /*
     * // check for depth ! 1 ch 16 depth - if not format error & return if
     * (image.nChannels() != 1 || image.depth() != 16) { log.error(
     * "image is not a kinect depth image"); return image; }
     * 
     * if (dst == null) { //dst = cvCreateImage(cvSize(image.width(),
     * image.height()), image.depth(),image.nChannels()); //dst =
     * cvCreateImage(cvSize(image.width(), image.height()), 8, 1); src =
     * cvCreateImage(cvSize(image.width(), image.height()), 8, 1); dst =
     * cvCreateImage(cvSize(image.width(), image.height()), 8, 1); }
     * 
     * cvConvertScale(image, src, 1, 0); //cvThreshold(dst, dst, 30, 255,
     * CV_THRESH_BINARY);
     * 
     * CvScalar min = cvScalar(30000, 0.0, 0.0, 0.0); CvScalar max =
     * cvScalar(150000, 0.0, 0.0, 0.0);
     * 
     * cvInRangeS(image, min, max, dst);
     * 
     * createMask = true; if (createMask) { if (mask == null) { mask =
     * cvCreateImage(cvSize(image.width(), image.height()), 8, 1); } cvCopy(dst,
     * mask, null); myService.setMask(this.getName(), mask); createMask = false;
     * } //cvCvtColor /* ByteBuffer source = image.getByteBuffer(); int z =
     * source.capacity(); ByteBuffer destination = dst.getByteBuffer(); z =
     * destination.capacity();
     * 
     * int depth = 0;
     * 
     * Byte b = 0xE; int max = 0;
     * 
     * for (int i=0; i<image.width()*image.height(); i++) {
     * 
     * depth = source.get(i) & 0xFF; depth <<= 8; depth = source.get(i+1) &
     * 0xFF; if (depth > max) max = depth;
     * 
     * if (depth > 100 && depth < 400) { destination.put(i, b); } }
     */

    // return dst;
  }

  public void samplePoint(Integer inX, Integer inY) {
    ++clickCounter;
    if (lastDepth != null) {
      x = inX;
      y = inY;

      ByteBuffer depthBuffer = lastDepth.getByteBuffer();
      
      int depthBytesPerChannel = lastDepth.depth() / 8;
      
      int depthIndex = y * lastDepth.widthStep() + x * lastDepth.nChannels() * depthBytesPerChannel;
      int value = (depthBuffer.get(depthIndex + 1) & 0xFF) << 8 | (depthBuffer.get(depthIndex) & 0xFF);
      
      
      // FIXME - put in method
      
      double hsv = minY + ((value - minX) * (maxY - minY)) / (maxX - minX);
      // log.warn(String.format("(%d, %d) = %d => %f", x, y, value, hsv));

      Color c = Color.getHSBColor((float) hsv, 0.9f, 0.9f);
      
      if (value == 0x0000) {
        // too near
        c = Color.RED;
      } else if (value == 0x7ff8) {
        c = Color.WHITE;
       // } else if (value == 0xfff8) {
      } else if (value == 0xff07) {
        c = Color.BLACK;
      }

      
      String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());  
      
      log.info("({},{}) {} color R{} G{} B{} {}", inX, inY, value, c.getRed(), c.getGreen(), c.getBlue(), hex);
      info("(%d,%d) distance %d color %s r%d g%d b%d", inX, inY, value,  hex, c.getRed(), c.getGreen(), c.getBlue());
      
      // log.info("here");
    }
  }

}

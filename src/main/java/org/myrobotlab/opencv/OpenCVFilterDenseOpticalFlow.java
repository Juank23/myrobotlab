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
 * http://docs.opencv.org/modules/imgproc/doc/feature_detection.html
 * http://stackoverflow.com/questions/19270458/cvcalcopticalflowpyrlk-not-working-as-expected
 * 
 * Enjoy !
 * 
 * */

package org.myrobotlab.opencv;

import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.CV_32FC1;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_ITER;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32F;
import static org.bytedeco.javacpp.opencv_core.cvCopy;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_core.cvTermCriteria;
import static org.bytedeco.javacpp.opencv_imgcodecs.CV_LOAD_IMAGE_GRAYSCALE;
import static org.bytedeco.javacpp.opencv_imgcodecs.imread;
import static org.bytedeco.javacpp.opencv_imgcodecs.imwrite;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvGoodFeaturesToTrack;
import static org.bytedeco.javacpp.opencv_imgproc.cvLine;
import static org.bytedeco.javacpp.opencv_video.createOptFlow_DualTVL1;
import static org.bytedeco.javacpp.opencv_video.cvCalcOpticalFlowPyrLK;

import java.nio.FloatBuffer;
import java.util.ArrayList;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_video.DenseOpticalFlow;
import org.bytedeco.javacpp.helper.opencv_core.CvArr;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.data.Point2Df;
import org.slf4j.Logger;

public class OpenCVFilterDenseOpticalFlow extends OpenCVFilter {

  private static final long serialVersionUID = 1L;

  public final static Logger log = LoggerFactory.getLogger(OpenCVFilterDenseOpticalFlow.class);

  public ArrayList<Point2Df> pointsToPublish = new ArrayList<Point2Df>();

  public OpenCVFilterDenseOpticalFlow() {
    super();
  }

  public OpenCVFilterDenseOpticalFlow(String name) {
    super(name);
  }

  public void clearPoints() {

  }

  transient Mat pFrame = null;
  transient Mat cFrame = null;
  transient Mat pGray = new Mat();
  transient Mat cGray = new Mat();
  transient Mat flow = new Mat();
  transient IplImage gray = null;
  transient final DenseOpticalFlow tvl1 = createOptFlow_DualTVL1();

  @Override
  public IplImage display(IplImage frame, OpenCVData data) {

    return frame;
  }

  @Override
  public void imageChanged(IplImage image) {
    gray = IplImage.create(imageSize, 8, 1);
  }
  
  Mat OF =null;

  @Override
  public IplImage process(IplImage inImage, OpenCVData data) {
    
    if (channels == 3) {
      cvCvtColor(inImage, gray, CV_BGR2GRAY);
    } else {
      gray = inImage;
    }

    // pFrame = imread("samples/image0.png", CV_LOAD_IMAGE_GRAYSCALE);
    cFrame = new Mat(gray);// FIXME - make load gray image
                            // imread("samples/image1.png",
                            // CV_LOAD_IMAGE_GRAYSCALE);
    if (pFrame != null) {

 
      pGray = new Mat();
      cGray = new Mat();
      flow = new Mat();

      pFrame.convertTo(pGray, CV_32FC1);
      cFrame.convertTo(cGray, CV_32FC1);

      
      tvl1.calc(pGray, cGray, flow);

      OF = new Mat(pGray.rows(), pGray.cols(), CV_32FC1);
      FloatBuffer in = flow.createBuffer(), out = OF.createBuffer();

      int height = pGray.rows(), width = pGray.cols();

      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          final float xVelocity = in.get();
          final float yVelocity = in.get();
          final float pixelVelocity = (float) Math.sqrt(xVelocity * xVelocity + yVelocity * yVelocity);
          out.put(pixelVelocity);
        }
      }
      // imwrite("OF.png", OF);
    }

    pFrame = cFrame;
    if (OF != null) {
      return new IplImage(OF);
    }
    return inImage;
  }

  public void samplePoint(Float x, Float y) {
  }

  public void samplePoint(Integer x, Integer y) {
  }

}

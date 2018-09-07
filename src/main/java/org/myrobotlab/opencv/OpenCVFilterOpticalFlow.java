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

import static org.bytedeco.javacpp.opencv_bioinspired.*; 
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
import static org.bytedeco.javacpp.helper.opencv_core.CV_RGB;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_EPS;
import static org.bytedeco.javacpp.opencv_core.CV_TERMCRIT_ITER;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_32F;
import static org.bytedeco.javacpp.opencv_core.cvCreateImage;
import static org.bytedeco.javacpp.opencv_core.cvGetSize;
import static org.bytedeco.javacpp.opencv_core.cvPoint;
import static org.bytedeco.javacpp.opencv_core.cvSize;
import static org.bytedeco.javacpp.opencv_core.cvTermCriteria;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2GRAY;
import static org.bytedeco.javacpp.opencv_imgproc.cvCvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.cvFindCornerSubPix;
import static org.bytedeco.javacpp.opencv_imgproc.cvGoodFeaturesToTrack;
import static org.bytedeco.javacpp.opencv_imgproc.cvLine;
import static org.bytedeco.javacpp.opencv_video.cvCalcOpticalFlowPyrLK;

import java.util.ArrayList;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.FloatPointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.opencv_core.CvPoint;
import org.bytedeco.javacpp.opencv_core.CvPoint2D32f;
import org.bytedeco.javacpp.opencv_core.CvReleaseFunc;
import org.bytedeco.javacpp.opencv_core.CvSize;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.helper.opencv_core.CvArr;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.service.data.Point2Df;
import org.slf4j.Logger;

/**
 *
 * @author GroG
 * 
 * https://www.codeproject.com/Articles/840823/Object-Feature-Tracking-in-Csharp (excellent artical describing sparse optical flow)
 *
 */
public class OpenCVFilterOpticalFlow extends OpenCVFilter {

  private static final long serialVersionUID = 1L;

  public final static Logger log = LoggerFactory.getLogger(OpenCVFilterOpticalFlow.class);

  public ArrayList<Point2Df> pointsToPublish = new ArrayList<Point2Df>();

  int win_size = 15;
  int MAX_CORNERS = 500;

  public OpenCVFilterOpticalFlow() {
    super();
  }

  public OpenCVFilterOpticalFlow(String name) {
    super(name);
  }

  public void clearPoints() {

  }

  @Override
  public IplImage display(IplImage frame, OpenCVData data) {

    return frame;
  }

  @Override
  public void imageChanged(IplImage image) {
    currentImg = IplImage.create(imageSize, 8, 1);
    lastImg = IplImage.create(imageSize, 8, 1);
  }

  IplImage lastImg = null;
  IplImage currentImg = null;

  @Override
  public IplImage process(IplImage inImage, OpenCVData data) {

    if (channels == 3) {
      cvCvtColor(inImage, currentImg, CV_BGR2GRAY);
    } else {
      currentImg = inImage;
    }

    if (lastImg != null) {

      // Load two images and allocate other structures
      // currentImg = cvLoadImage("flow1.png", CV_LOAD_IMAGE_GRAYSCALE);
      // lastImg = cvLoadImage("flow2.png", CV_LOAD_IMAGE_GRAYSCALE);

      CvSize img_sz = cvGetSize(currentImg);

      // IplImage imgC = cvLoadImage("OpticalFlow1.png",
      // CV_LOAD_IMAGE_UNCHANGED);
      // IplImage imgC = cvLoadImage("flow1.png", CV_LOAD_IMAGE_UNCHANGED);
      // Get the features for tracking
      IplImage eig_image = cvCreateImage(img_sz, IPL_DEPTH_32F, 1);
      IplImage tmp_image = cvCreateImage(img_sz, IPL_DEPTH_32F, 1);

      IntPointer corner_count = new IntPointer(1).put(MAX_CORNERS);
      CvPoint2D32f cornersA = new CvPoint2D32f(MAX_CORNERS);

      CvArr mask = null;
      cvGoodFeaturesToTrack(currentImg, eig_image, tmp_image, cornersA, corner_count, 0.05, 5.0, mask, 3, 0, 0.04);

      cvFindCornerSubPix(currentImg, cornersA, corner_count.get(), cvSize(win_size, win_size), cvSize(-1, -1), cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.03));

      // Call Lucas Kanade algorithm
      BytePointer features_found = new BytePointer(MAX_CORNERS);
      FloatPointer feature_errors = new FloatPointer(MAX_CORNERS);

      CvSize pyr_sz = cvSize(currentImg.width() + 8, lastImg.height() / 3);

      IplImage pyrA = cvCreateImage(pyr_sz, IPL_DEPTH_32F, 1);
      IplImage pyrB = cvCreateImage(pyr_sz, IPL_DEPTH_32F, 1);
      

      CvPoint2D32f cornersB = new CvPoint2D32f(MAX_CORNERS);
      cvCalcOpticalFlowPyrLK(currentImg, lastImg, pyrA, pyrB, cornersA, cornersB, corner_count.get(), cvSize(win_size, win_size), 5, features_found, feature_errors,
          cvTermCriteria(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.3), 0);

      // Make an image of the results
      for (int i = 0; i < corner_count.get(); i++) {
        if (features_found.get(i) == 0 || feature_errors.get(i) > 550) {
          System.out.println("Error is " + feature_errors.get(i) + "/n");
          continue;
        }
        cornersA.position(i);
        cornersB.position(i);
        CvPoint p0 = cvPoint(Math.round(cornersA.x()), Math.round(cornersA.y()));
        CvPoint p1 = cvPoint(Math.round(cornersB.x()), Math.round(cornersB.y()));
        cvLine(inImage, p0, p1, CV_RGB(255, 0, 0), 2, 8, 0); // FIXME - don't tamper with out image - change only display :(
      }
      // imwrite("flow.out.png", new Mat(imgC));
      
      cvReleaseImage(eig_image);
      cvReleaseImage(tmp_image);
      cvReleaseImage(pyrA);
      cvReleaseImage(pyrB);
    }
    // lastImg = currentImg;
    cvCopy(currentImg, lastImg);
    return inImage; // FIXME - don't tamper with out image - change only display :(
  }

  public void samplePoint(Float x, Float y) {
  }

  public void samplePoint(Integer x, Integer y) {
  }

}

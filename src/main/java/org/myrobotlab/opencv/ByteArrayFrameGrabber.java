package org.myrobotlab.opencv;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.myrobotlab.logging.LoggerFactory;
import org.slf4j.Logger;

public class ByteArrayFrameGrabber extends FrameGrabber {

  public final static Logger log = LoggerFactory.getLogger(ByteArrayFrameGrabber.class.getCanonicalName());

  transient private IplImage image;
  transient private IplImage lastImage;
  transient private Map<String, IplImage> cache = new TreeMap<String, IplImage>();
  int frameCounter = 0;
  String filePath;

  // 
  File path;
  transient OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

  
  public ByteArrayFrameGrabber() {    
  }
  // FIXME - remove filePath - use Path as input - scan directory
  public ByteArrayFrameGrabber(String path) {
    this.path = new File(path);    
  }

  @Override
  public Frame grab() {
    if (!cache.containsKey(filePath)) {
      image = cvLoadImage(filePath);
      cache.put(filePath, image);
    } else {
      image = cache.get(filePath).clone();
    }

    ++frameCounter;

    if (frameCounter > 1) {
      lastImage.release();
    }

    lastImage = image;
    return converter.convert(image);
  }

  @Override
  public void release() throws Exception {
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  @Override
  public void trigger() throws Exception {
  }

}

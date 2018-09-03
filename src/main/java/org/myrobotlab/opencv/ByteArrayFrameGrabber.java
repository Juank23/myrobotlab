package org.myrobotlab.opencv;

import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.indexer.UShortIndexer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.myrobotlab.logging.LoggerFactory;
import org.slf4j.Logger;

// FIXME replace ImageFile with this - just make it handle known formats as well
public class ByteArrayFrameGrabber extends FrameGrabber {

  public final static Logger log = LoggerFactory.getLogger(ByteArrayFrameGrabber.class);

  /**
   * default format for kinect depth
   *
   */
  public class Format {
    public Integer height = 640;
    public Integer width = 420;
    public Integer depth = 16;
    public Integer channels = 1;

    public Format(int height, int width, int depth, int channels) {
      this.height = height;
      this.width = width;
      this.depth = depth;
      this.channels = channels;
    }
  }
  
  public class ByteArrayFrameGrabberImage {
    File src;
    Map<String, Format> formats;
    
    public ByteArrayFrameGrabberImage(Map<String, Format> formats, File src) {
      this.src = src;
      this.formats = formats;
    }
    
    public String getExt() {
      String filename = src.getName();
      String ext = "";

      int e = filename.lastIndexOf('.');
      if (e > 0) {
        ext = filename.substring(e + 1).toLowerCase();
      }
      return ext;
    }
  }

  List<Frame> cache = new ArrayList<Frame>();
  Map<String, Format> formats = new HashMap<String, Format>();

  int frameIndex = 0;
  String filePath;

  transient OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

  // FIXME - remove filePath - use Path as input - scan directory
  public ByteArrayFrameGrabber(String path) throws IOException {
    formats.put("raw", new Format(640, 400, 16, 1));
    loadFiles(path);
  }

  public void addExtentionFormat(String ext, int height, int width, int depth, int channels) {
    formats.put(ext.toLowerCase(), new Format(height, width, depth, channels));
  }

  public void loadFiles(String filename) throws IOException {
    // if path == dir
    File file = new File(filename);
    if (file.isDirectory()) {
      File[] list = file.listFiles();
      for (File f : list) {
        loadBytes(f);        
      }
    } else {
      loadBytes(new File(filename));      
    }
  }

  public void loadBytes(File file) throws IOException {
    String filename = file.getName();
    String ext = "";

    int e = filename.lastIndexOf('.');
    if (e > 0) {
      ext = filename.substring(e + 1).toLowerCase();
    }
    
    if (formats.containsKey(ext)) {
      // load byte array
      byte[] fileContent = Files.readAllBytes(file.toPath());
      Format format = formats.get(ext);
      
      IplImage image = IplImage.create(format.width, format.height, format.depth, format.channels);
      BytePointer rawImageData = new BytePointer(fileContent);
      int step = format.width * format.depth * format.channels;
      cvSetData(image, rawImageData, step);
      
      cache.add(converter.convert(image));
    } else {
      // regular cvLoadImage for known formats
      IplImage image = cvLoadImage(file.getAbsolutePath());
      /*
      Mat m = imread(file.getAbsolutePath());
      Frame f = new Frame(640,480,16,1);
     
      
      UShortIndexer grayFrameIdx = f.createIndexer();
      for (int i = 0; i < grayFrameIdx.rows(); i++) {
          for (int j = 0; j < grayFrameIdx.cols(); j++) {
              grayFrameIdx.put(i, j, i + j);
          }
      }
      */
      
      cache.add(converter.convert(image));
    }

    // if (file.get)
  }

  @Override
  public Frame grab() {

    // if frame index is > file list size

    // clear list of files

    // if path is file
    // load file
    if (frameIndex < cache.size()) {
      Frame frame = cache.get(frameIndex);
      ++frameIndex;
      return frame;
    } else {
      // reset
      frameIndex = 0;
    }

    return null;
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

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
    public Integer width = null;
    public Integer height = null;
    public Integer depth = null;
    public Integer channels = null;

    public Format(int width, int height, int depth, int channels) {
      this.width = width;
      this.height = height;
      this.depth = depth;
      this.channels = channels;
    }
  }
  
  public class FrameFile {
    public Frame frame;
    public File src;
    
    public FrameFile(Frame frame, File src) {
      this.frame = frame;
      this.src = src;
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

  List<FrameFile> cache = new ArrayList<FrameFile>();
  Map<String, Format> formats = new HashMap<String, Format>();

  int frameIndex = 0;
  String filePath;

  transient OpenCVFrameConverter.ToIplImage converter = new OpenCVFrameConverter.ToIplImage();

  // FIXME - remove filePath - use Path as input - scan directory
  public ByteArrayFrameGrabber(String path) throws IOException {
    formats.put("raw", new Format(640, 480, 16, 1));
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
      log.info("frame grabber found {} files", list.length);
      // java.util.Arrays.sort(list); - not guaranteed order
      for (File f : list) {
        if (f.isDirectory()) {
          log.info("skipping directory {}", f.getName());
          continue;
        }
        log.info("loading {}", f);
        loadBytes(f);        
      }
    } else {
      loadBytes(new File(filename));      
    }
    
    log.info("loaded files - cache now {} frames", cache.size());
    // sort cache ?
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
      
      cache.add(new FrameFile(converter.convert(image), file));
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
      
      cache.add(new FrameFile(converter.convert(image), file));
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
      FrameFile ff = cache.get(frameIndex);
      if (log.isDebugEnabled()) {
        log.debug("{}", ff.src.getName());
      }
      ++frameIndex;
      return ff.frame;
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

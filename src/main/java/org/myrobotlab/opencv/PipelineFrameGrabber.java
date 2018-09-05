package org.myrobotlab.opencv;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.slf4j.Logger;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;

/**
 * @author GroG
 * 
 *         A pipeline frame grabber can attach to another OpenCV's output and
 *         perform its own processing. They can be stacked creating much more
 *         complex image pipelines.
 *
 */
public class PipelineFrameGrabber extends FrameGrabber {

  public final static Logger log = LoggerFactory.getLogger(PipelineFrameGrabber.class);
  Map<String, Object> sources = OpenCVData.getSources();
  Long lastFrameTs = 0L;
  String sourceKey = null;
  Long pauseTimeMs = 100L;

  public PipelineFrameGrabber(String sourceKey) {
    log.info("attaching video pipeline feed to {}", sourceKey);
    this.sourceKey = sourceKey;
  }

  @Override
  public Frame grab() {

    Frame frame = null;

    // find source of frames
    if (!sources.containsKey(sourceKey)) {
      log.warn("pipeline source {} not found", sourceKey);
    } else {
      
      Object thingy = sources.get(sourceKey);
      if (thingy.getClass() == Frame.class) {
        frame = (Frame)thingy;
      } else if (thingy.getClass() == IplImage.class) {
        // frame = new Frame((IplImage)thingy);
        log.error("implement with converters");
      } else if (thingy.getClass() == Mat.class) {
        // frame = new Frame((Mat)thingy);
        log.error("implement with converters");
      }
    }

    // find timestamp of source
    String tsKey = String.format("%s.ts", sourceKey);
    if (!sources.containsKey(tsKey)) {
      log.warn("pipeline source ts {} not found", tsKey);
    } else {
      lastFrameTs = (Long) sources.get(tsKey);
    }

    if (frame == null) {
      try {
        log.warn("pipeline pausing for {} ms", pauseTimeMs);
        Thread.sleep(pauseTimeMs);
      } catch (InterruptedException e) {       
      }
    }
    
    // TODO - minimalLatencyTime optional minimal latency - will "not" use the reference if its
    // "too" old - this would represent the upstream process taking "too" long

    return frame;
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

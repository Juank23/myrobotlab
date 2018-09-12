package org.myrobotlab.service;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import org.myrobotlab.framework.Service;
import org.myrobotlab.framework.ServiceType;
import org.myrobotlab.image.DisplayedImage;
import org.myrobotlab.logging.Level;
import org.myrobotlab.logging.LoggerFactory;
import org.myrobotlab.logging.Logging;
import org.myrobotlab.logging.LoggingFactory;
import org.slf4j.Logger;

public class ImageDisplay extends Service {

  transient private static GraphicsDevice gd;

  private static int h, w;
  public final static Logger log = LoggerFactory.getLogger(ImageDisplay.class);
  private static final long serialVersionUID = 1L;

  /**
   * This static method returns all the details of the class without it having
   * to be constructed. It has description, categories, dependencies, and peer
   * definitions.
   * 
   * @return ServiceType - returns all the data
   * 
   */
  static public ServiceType getMetaData() {

    ServiceType meta = new ServiceType(ImageDisplay.class.getCanonicalName());
    meta.addDescription("Service to Display Images");
    meta.addCategory("display");
    meta.addDependency("org.beryx", "awt-color-factory", "1.0.0");
    return meta;
  }

  // Getting display resolution: width and height
  public static void getResolution() {
    w = gd.getDisplayMode().getWidth();
    h = gd.getDisplayMode().getHeight();
    log.info("display resolution: {} x {} ", w, h);
  }

  // Returns the Height-factor of the DisplayResolution.
  public static int getResolutionOfH() {
    return gd.getDisplayMode().getHeight();
  }

  // Returns the Width-factor of the DisplayResolution.
  public static int getResolutionOfW() {
    return gd.getDisplayMode().getWidth();
  }

  transient List<JFrame> frames = new ArrayList<JFrame>();

  int hOffset = 20;

  int wOffset = 0;

  Color bgcolor = Color.BLACK;

  public ImageDisplay(String n) {
    super(n);
  }

  // builds a JFrame of the right size for the image.
  private void buildFrame(DisplayedImage image) {
    JFrame f = new JFrame();
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // f.setLayout(new BorderLayout());
    // f.add(image, BorderLayout.CENTER);
    
    f.setSize(image.getWidth() + wOffset, image.getHeight() + hOffset);
    getResolution();
    f.setLocation(w / 2 - image.getWidth() / 2, h / 2 - (image.getHeight() + hOffset) / 2);
    f.setVisible(true);
    frames.add(f);
  }

  // builds a JFrame for the FullScreen sized image.
  private void buildFrameFS(DisplayedImage image) {
    final JFrame f = new JFrame();
    // Exit program on mouse click
    f.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent e) {
        gd.setFullScreenWindow(null);
        f.dispose();
        ;
      }

      @Override
      public void mouseEntered(MouseEvent arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseExited(MouseEvent arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mousePressed(MouseEvent arg0) {
        // TODO Auto-generated method stub

      }

      @Override
      public void mouseReleased(MouseEvent arg0) {
        // TODO Auto-generated method stub

      }
    });
    image.setBackground(bgcolor);
    
    // image.setLocation(Point(1024, 0));
    f.setBackground(bgcolor);
    f.getContentPane().setBackground(bgcolor);
    f.add(image);
    
    // f.setLayout(new BorderLayout());
    // f.add(image, BorderLayout.CENTER);
    
    // It sets the size of the Frame to the size of the picture, if not it will
    // be build a boarder to the right end of the screen.
    
    f.setSize(image.getWidth() + wOffset, image.getHeight() + hOffset);
    getResolution();
    // f.setLocation(image.getwOffset(), image.gethOffset());
    f.toFront();
    gd.setFullScreenWindow(f);
    f.setLocation(1024, image.gethOffset());
    
    f.setVisible(true);
    frames.add(f);
  }

  public void closeAll() {
    for (int i = 0; i < frames.size(); ++i) {
      JFrame f = frames.get(i);
      f.dispose();
    }
    frames.clear();
  }

  // Displays an image.
  // @param source = path.
  public void display(String source) {
    DisplayedImage image = new DisplayedImage(source, 1.0f);
    log.info("Loading image done");
    buildFrame(image);
  }

  // Displays a faded image.
  // @param source = path.
  // @param alpha = Value how much the image is faded float from 0.0 to 1.0.
  public void display(String source, float alpha) {
    DisplayedImage image = new DisplayedImage(source, alpha);
    log.info("Loading image done");
    buildFrame(image);
  }

  // Displays an image by Fading it in.
  // @param source = path.
  public void displayFadeIn(String source) {
    DisplayedImage image = new DisplayedImage(source);
    log.info("Loading image done");
    buildFrame(image);
  }

  // Displays an image in FullScreen mode.
  // @param source = path.
  public void displayFullScreen(String source) {
    DisplayedImage image = new DisplayedImage(source, 1.0f, true);
    log.info("Loading image done");
    buildFrameFS(image);
  }

  // Displays a faded image in FullScreen mode.
  // @param source = path.
  // @param alpha = Value how much the image is faded float from 0.0 to 1.0.
  public void displayFullScreen(String source, float alpha) {
    DisplayedImage image = new DisplayedImage(source, alpha, true);
    log.info("Loading image done");
    buildFrameFS(image);
  }

  // Displays a resized image in FullScreen mode.
  // @param source = path.
  // @param scaling = scale factor to resize the image.
  public void displayScaled(String source, float scaling) {
    DisplayedImage image = new DisplayedImage(source, 1, scaling);
    log.info("Loading image done");
    buildFrame(image);
  }

  /**
   * @param source
   *          = path
   * @param alpha
   *          alpha = Value how much the image is faded float from 0.0 to 1.0.
   * @param scaling
   *          scaling = scale factor to resize the image.
   */
  public void displayScaled(String source, float alpha, float scaling) {
    DisplayedImage image = new DisplayedImage(source, alpha, scaling);
    log.info("Loading image done");
    buildFrame(image);
  }

  /**
   * Exits the Fullscreen mode.
   */
  public void exitFS() {
    gd.setFullScreenWindow(null);
  }

  @Override
  public void startService() {
    super.startService();
    if (GraphicsEnvironment.isHeadless()) {
      log.warn("in headless mode - can not start awt components");
      return;
    } else {
      gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }
  }

  @Override
  public void stopService() {
    super.stopService();
    closeAll();
  }

  public void setBackground(Color color) {
    bgcolor = color;
  }

  public void setBackground(String color) {
    bgcolor = Color.decode(color);
  }

  public static void main(String[] args) {
    LoggingFactory.init(Level.INFO);

    try {
      // String path =
      // "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQJwoVloTUs4cW2uWdsIbP_Fdph0IfEhODRrQoOgFOiYrYj_9J01A";
      // String path2 = "/Users/Sebastien/Pictures/scan10.jpeg";

      // TODO - test without SwingGui
      // TODO - test ssl non ssl
      // TODO - test http redirect
      // TODO - png background transparency
      // TODO - frameless border on jframe
      // TODO - test animation - gif
      // TODO - animate multiple arbitrary frame
      // TODO - slideshow

      // TODO - fullscreen plus set position of image (default should be center)

      ImageDisplay display = (ImageDisplay) Runtime.start("ImageDisplay", "ImageDisplay");
      // Runtime.start("gui", "SwingGui");

      // TODO - display.background(Color "0x343434"
      display.setBackground(Color.BLACK);
      // display.displayFullScreen("emoji-svg/Noto_Emoji_Oreo_1f600.svg");
      display.displayFullScreen("emoji-gerty/1f61c.png");
      log.info("here");

      display.displayFullScreen("https://upload.wikimedia.org/wikipedia/commons/thumb/b/b9/Noto_Emoji_Oreo_1f601.svg/1024px-Noto_Emoji_Oreo_1f601.svg.png");
      display.displayFullScreen("https://upload.wikimedia.org/wikipedia/commons/thumb/6/63/Noto_Emoji_Oreo_1f602.svg/768px-Noto_Emoji_Oreo_1f602.svg.png");
      display.displayFullScreen("https://upload.wikimedia.org/wikipedia/commons/thumb/c/c8/Noto_Emoji_Oreo_1f603.svg/768px-Noto_Emoji_Oreo_1f603.svg.png");

      log.info("here");
      display.displayFullScreen(
          "data:image/png;base64,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");
      // TODO - visible(true/false) , keyed

      display.setBackground(Color.WHITE);
      // not supportable
      display.displayFullScreen("https://upload.wikimedia.org/wikipedia/commons/thumb/7/76/OpenMoji-black_1F380.svg/512px-OpenMoji-black_1F380.svg.png");
      display.display("https://goo.gl/srjBgX");
      display.display("https://goo.gl/mgPxTL");

      display.displayFadeIn("http://myrobotlab.org/sites/default/files/images/mrl_logo.jpg");
      display.displayScaled("http://myrobotlab.org/sites/default/files/images/mrl_logo.jpg", 2);
      display.displayScaled("http://myrobotlab.org/sites/default/files/images/mrl_logo.jpg", 4);

      // TODO - test scaled
      // TODO - test all the various types of scaling
      // TODO - test fading in / out
      // TODO - test closing frames - keyed by name

      display.displayFullScreen("https://goo.gl/2Ctrsd");

      // Runtime.start("webgui", "WebGui");

      // FIXME file caching if fetched from internet (configurable) !!!

    } catch (Exception e) {
      Logging.logError(e);
    }
  }

}

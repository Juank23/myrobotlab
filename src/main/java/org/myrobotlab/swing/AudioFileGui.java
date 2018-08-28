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

package org.myrobotlab.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.myrobotlab.service.AudioFile;
import org.myrobotlab.service.SwingGui;


// FIXME - add stopwatch capabilities
public class AudioFileGui extends ServiceGui implements ActionListener {
  static final long serialVersionUID = 1L;
  
 
  public AudioFileGui(final String boundServiceName, final SwingGui myService) {
    super(boundServiceName, myService);  
    // addTop(3, clockDisplay);
    // addTop(startClock, interval, "ms");
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();

    // if (o == startClock) {
  }


  @Override
  public void subscribeGui() {
  }

  @Override
  public void unsubscribeGui() {
  }

  public void onState(final AudioFile worke) {
    /*
     * setText IS THREAD SAFE !!!!!
     *
     * SwingUtilities.invokeLater(new Runnable() { public void run() {
     */
/*
    interval.setText((worke.interval + ""));

    if (worke.isClockRunning) {
      startClock.setText("stop clock");
      interval.setEnabled(false);
    } else {ClockGui.java
      startClock.setText("start clock");
      interval.setEnabled(true);
    }
    */
  }

}

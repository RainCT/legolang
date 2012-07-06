/**
 * Copyright © 2012 Siegfried-A. Gevatter Pujals <siegfried@gevatter.com>
 * Copyright © 2012 Gerard Canal Camprodon <grar.knal@gmail.com>
 *
 * Permission to use, copy, modify, and/or distribute this software for
 * any purpose with or without fee is hereby granted, provided that the
 * above copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION
 * OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
*/

package llcclib;
import llcclib.LegoTypes;

import java.util.Vector;

import lejos.nxt.Motor;
import lejos.robotics.objectdetection.Feature;
import lejos.robotics.objectdetection.RangeFeatureDetector;
import lejos.robotics.objectdetection.TouchFeatureDetector;
import lejos.robotics.objectdetection.FeatureListener;
import lejos.robotics.objectdetection.FeatureDetector;
import lejos.util.Delay;
import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.robotics.Color;
import lejos.nxt.SensorPort;
import lejos.nxt.ColorSensor;
import lejos.nxt.TouchSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.LCD;
import llcclib.LegoTypes;
import java.util.Random;

public class LegoFunctions {

    // Sensors
    private static TouchSensor[] ts = {null, null, null, null};
    private static TouchFeatureDetector[] tfd = {null, null, null, null};
    private static UltrasonicSensor[] uss = {null, null, null, null};
    private static RangeFeatureDetector[] fd = {null, null, null, null};
    private static ColorSensor[] cs = {null, null, null, null};
    private static boolean touchSignalAdded[] = {false, false, false, false};
    private static boolean rangeSignalAdded[] = {false, false, false, false};
    private static Random rand = new Random();
    
    public static void move(Long speed, LegoTypes.Motor motor) { // in degrees per second
        if (speed == null) speed = 10L;
        motor.getMotor().setSpeed(Math.abs(speed.intValue()));
        if (speed > 0) motor.getMotor().forward();
        else motor.getMotor().backward();
    }
    
    public static void stop(LegoTypes.Motor motor) {
        motor.getMotor().stop(true);
    }

    public static void rotate(Long degrees, LegoTypes.Motor motor) {
        motor.getMotor().rotate(degrees.intValue(), true);
    }
    
    public static void rotateAndWait(Long degrees, LegoTypes.Motor motor) {
        motor.getMotor().rotate(degrees.intValue());
    }
    
    public static Long rangeScan(Long distance, LegoTypes.Sensor port) {
        // We instantiate the sensor in a class member since it's
        // only possible to create one instance of each sensor.
        if (uss[port.getID()] == null) uss[port.getID()] = new UltrasonicSensor(port.getSensor());
        if (fd[port.getID()] == null) fd[port.getID()] = new RangeFeatureDetector(uss[port.getID()], distance.floatValue(), 500);
        Feature result = fd[port.getID()].scan();
        if(result != null) return new Long((long) result.getRangeReading().getRange());
        else return new Long(-1);
    }

    public static boolean isTouching(LegoTypes.Sensor port) {
        if (ts[port.getID()] == null) ts[port.getID()] = new TouchSensor(port.getSensor());
        if (tfd[port.getID()] == null) tfd[port.getID()] = new TouchFeatureDetector(ts[port.getID()]);
        Feature result = tfd[port.getID()].scan();
        return result != null;
    }
    
    public static void setLamp(Color c, LegoTypes.Sensor port) {
        //Get predominant color
        int predominant = Math.max(c.getRed(), Math.max(c.getBlue(), c.getGreen()));
        int color = ColorSensor.Color.NONE;
        if (predominant == c.getRed()) color = ColorSensor.Color.RED;
        else if (predominant == c.getBlue()) color = ColorSensor.Color.BLUE;
        else if (predominant == c.getGreen()) color = ColorSensor.Color.GREEN;
        if (cs[port.getID()] == null) cs[port.getID()] = new ColorSensor(port.getSensor(), color);
        else cs[port.getID()].setFloodlight(color);
    }
    
    public static void stopLamp(LegoTypes.Sensor port) {
        if (cs[port.getID()] == null) cs[port.getID()] = new ColorSensor(port.getSensor(), ColorSensor.Color.NONE);
        else cs[port.getID()].setFloodlight(ColorSensor.Color.NONE);
    }
    
    public static llcclib.Color readColor(LegoTypes.Sensor port) {
        if (cs[port.getID()] == null) cs[port.getID()] = new ColorSensor(port.getSensor());
        return new llcclib.Color(cs[port.getID()].getColor());
    }

    public static void calibrateWhite(LegoTypes.Sensor port) {
        if (cs[port.getID()] == null) cs[port.getID()] = new ColorSensor(port.getSensor());
        cs[port.getID()].calibrateHigh();
    }

    public static void calibrateBlack(LegoTypes.Sensor port) {
        if (cs[port.getID()] == null) cs[port.getID()] = new ColorSensor(port.getSensor());
        cs[port.getID()].calibrateLow();
    }
    
    public static void waitForButton() {
        Button.waitForPress();
    }
    
    public static Long getRed(Color c) {
        return new Long(c.getRed());
    }
    
    public static Long getBlue(Color c) {
        return new Long(c.getBlue());
    }    
    
    public static Long getGreen(Color c) {
        return new Long(c.getGreen());
    }

    public static Float colorSimilarity(Color c1, Color c2) {
        float diff = Math.abs(c1.getRed() - c2.getRed()) +
                     Math.abs(c1.getGreen() - c2.getGreen()) +
                     Math.abs(c1.getBlue() - c2.getBlue());
        return new Float(1.0 - diff / (255.0 * 3.0));
    }

    public static boolean isColor(Color c1, Color c2, Float tolerance) {
        if (tolerance == null) tolerance = 0.0f;
        return (1.0 - colorSimilarity(c1, c2)) <= tolerance;
    }
       
    public static boolean isPressed(LegoTypes.Button button) {
        return button.getButton().isPressed();
    }
     
    public static void waitForUpAndDown(LegoTypes.Button button) {
        button.getButton().waitForPressAndRelease();
    }
    
    public static void waitForPress(LegoTypes.Button button, @DefaultValue("0") Long timeout) {
        if (timeout == 0) button.getButton().waitForPress();
        button.getButton().waitForPress(timeout.intValue());
    }
    
    public static void delay(Long millis) {
        Delay.msDelay(millis.longValue());
    }
    
    public static void clear() {
        LCD.clear();
    }

    public static Long random(Long start, Long end) {
        if (start == null && end == null) return new Long(rand.nextInt());
        else if (end == null) {
            end = start;
            start = 0L;
        }
        float round = (start >= 0) ? 0.5f : -0.5f;
        return (long) (start + rand.nextFloat() * (end - start) + round);
    }

    public interface SignalCallback {

        public abstract void __run__(Long data);

    }
    
    public static class SignalManager implements ButtonListener, FeatureListener {

        private static SignalManager mInstance = null;

        public static void registerCallback(String name, SignalCallback cbClass) {
            if (mInstance == null)
                mInstance = new SignalManager();
            
            if (name.equals("any_button_pressed")) {
                mInstance.cbsAnyButtonPressed.addElement(cbClass);
            }
            else if (name.equals("enter_button_pressed")) {
                mInstance.cbsEnter.addElement(cbClass);
            }
            else if (name.equals("left_button_pressed")) {
                mInstance.cbsLeft.addElement(cbClass);
            }
            else if (name.equals("right_button_pressed")) {
                mInstance.cbsRight.addElement(cbClass);
            }
            else if (name.equals("escape_button_pressed")) {
                mInstance.cbsEscape.addElement(cbClass);
            }
            else System.out.println("Unknown signal: " + name);
        }
        
        public static void registerCallback(String name, SignalCallback cbClass, LegoTypes.Sensor port) {
            if (mInstance == null)
                mInstance = new SignalManager();

            if (name.equals("onTouch")) {
                if (!touchSignalAdded[port.getID()]) {
                    touchSignalAdded[port.getID()] = true;
                    if (ts[port.getID()] == null) ts[port.getID()] = new TouchSensor(port.getSensor());
                    if (tfd[port.getID()] == null) tfd[port.getID()] = new TouchFeatureDetector(ts[port.getID()]);
                    tfd[port.getID()].addListener(mInstance);
                }
                mInstance.cbsOnTouch.addElement(cbClass);
            }
            else if (name.startsWith("onRange")) {
                float distance = 50;
                if (name.length() > 7) distance = Float.parseFloat(name.substring(7, name.length()));
                
                if (!rangeSignalAdded[port.getID()]) {
                    rangeSignalAdded[port.getID()] = true;
                    if (uss[port.getID()] == null) uss[port.getID()] = new UltrasonicSensor(port.getSensor());
                    if (fd[port.getID()] == null) fd[port.getID()] = new RangeFeatureDetector(uss[port.getID()], distance, 500);
                    fd[port.getID()].addListener(mInstance);
                }
                mInstance.cbsOnRange.addElement(cbClass);
            }
            else System.out.println("Unknown signal: " + name);
        }

        private Vector<SignalCallback> cbsAnyButtonPressed;
        private Vector<SignalCallback> cbsOnTouch;
        private Vector<SignalCallback> cbsOnRange;
        private Vector<SignalCallback> cbsEnter;
        private Vector<SignalCallback> cbsEscape;
        private Vector<SignalCallback> cbsRight;
        private Vector<SignalCallback> cbsLeft;

        private SignalManager() {
            Button.ENTER.addButtonListener(this);
            Button.ESCAPE.addButtonListener(this);
            Button.LEFT.addButtonListener(this);
            Button.RIGHT.addButtonListener(this);

            cbsAnyButtonPressed = new Vector<SignalCallback>();
            cbsOnTouch          = new Vector<SignalCallback>();
            cbsOnRange          = new Vector<SignalCallback>();
            cbsEnter            = new Vector<SignalCallback>();
            cbsEscape           = new Vector<SignalCallback>();
            cbsRight            = new Vector<SignalCallback>();
            cbsLeft             = new Vector<SignalCallback>();
        }

        private void issueCallbacks(Vector<SignalCallback> v) {
            for (int i = 0; i < v.size(); ++i) {
                v.elementAt(i).__run__(null);
            }
        }
        
        private void issueCallbacks(Vector<SignalCallback> v, Long data) {
            for (int i = 0; i < v.size(); ++i) {
                v.elementAt(i).__run__(data);
            }
        }

        public void buttonPressed(Button b) {
            issueCallbacks(cbsAnyButtonPressed);
            switch (b.getId()) {
                case Button.ID_ENTER:
                    issueCallbacks(cbsEnter);
                    break;
                case Button.ID_ESCAPE:
                    issueCallbacks(cbsEscape);
                    break;
                case Button.ID_RIGHT:
                    issueCallbacks(cbsRight);
                    break;
                case Button.ID_LEFT:
                    issueCallbacks(cbsLeft);
                    break;
            }
        }

        public void buttonReleased(Button b) {
        }
        
        public void featureDetected(Feature feature, FeatureDetector detector) {
            if (detector instanceof TouchFeatureDetector) issueCallbacks(cbsOnTouch);
            else if (detector instanceof RangeFeatureDetector) issueCallbacks(cbsOnRange, new Long((long) feature.getRangeReading().getRange()));
        }

    }


    // Sound functions

    public static void twoBeeps() {
        lejos.nxt.Sound.twoBeeps();
    }

    public static void beepSequence() {
        lejos.nxt.Sound.twoBeeps();
    }

    public static void beepSequenceUp() {
        lejos.nxt.Sound.twoBeeps();
    }

    public static void buzz() {
        lejos.nxt.Sound.buzz();
    }

    public static void setVolume(Long volume) {
        lejos.nxt.Sound.setVolume(volume.intValue());
    }

    public static Long getVolume() {
        return new Long(lejos.nxt.Sound.getVolume());
    }

    public static void playSample(llcclib.DynamicArray<Long> samples,
            Long freq, Long volume)
    {
        // Optional parameters
        if (volume == null) volume = new Long(lejos.nxt.Sound.getVolume());

        // Convert samples to byte array
        byte[] data = new byte[samples.size()];
        for (int i = 0; i < samples.size(); i++)
            data[i] = samples.elementAt(i).byteValue();

        // Play it!
        lejos.nxt.Sound.playSample(data, 0, samples.size(),
            freq.intValue(), volume.intValue());
    }

    public static void playTone(Long freq, Long duration, Long volume) {
        if (volume == null)
            lejos.nxt.Sound.playTone(freq.intValue(), duration.intValue());
        else
            lejos.nxt.Sound.playTone(freq.intValue(), duration.intValue(),
                volume.intValue());
    }

    // General utility functions - move these to UtilFunctions.java at some point

    public static void abort() {
        System.exit(1);
    }

    /*
     * Current UNIX timestamp in milliseconds.
     * */
    public static Long getTimestamp() {
        return System.currentTimeMillis();
    }

    /*
     * Uptime in milliseconds.
     * */
    public static Long getUptime() {
        return System.nanoTime() / 1000000;
    }

    /*
     * Uptime in nanoseconds.
     * */
    public static Long getUptimeNano() {
        return System.nanoTime() / 1000000;
    }

    public static Float round(Float f, Long precision) {
        double scale = Math.pow(10, precision);
        return new Float(((int) (f * scale + 0.5)) / scale);
    }

    public static Float pow(Float f, Float exp) {
        return new Float(Math.pow(f, exp));
    }


    // Easter eggs

    public static void washMyDishes(LegoTypes.Motor roda1, LegoTypes.Motor roda2, LegoTypes.Sensor light) {
        if (cs[light.getID()] == null) cs[light.getID()] = new ColorSensor(light.getSensor(), ColorSensor.Color.GREEN);
        else cs[light.getID()].setFloodlight(ColorSensor.Color.GREEN);
        roda1.getMotor().forward();
        roda2.getMotor().forward();
        Delay.msDelay(1500);
        cs[light.getID()].setFloodlight(ColorSensor.Color.RED);
        roda1.getMotor().stop(true); roda2.getMotor().stop();
        boolean red = true;
        roda1.getMotor().rotate(90, true);//un canto
        roda2.getMotor().rotate(-90);
        for (int i = 0 ; i < 3 ; ++i) {
            roda1.getMotor().rotate(-180, true);//cap a l'altre canto
            roda2.getMotor().rotate(180);
            roda1.getMotor().rotate(180,true);//torna a l'inici
            roda2.getMotor().rotate(-180);

            if (getUptime() % 500 == 0) {
                if (red) cs[light.getID()].setFloodlight(ColorSensor.Color.NONE);
                else cs[light.getID()].setFloodlight(ColorSensor.Color.RED);
                red = !red;
            }
        }
        roda1.getMotor().rotate(-90, true);//torna al lloc
        roda2.getMotor().rotate(90);
        cs[light.getID()].setFloodlight(ColorSensor.Color.BLUE);
        Delay.msDelay(3000);
    }
    
}

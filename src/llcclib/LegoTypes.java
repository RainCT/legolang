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
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Button;
import lejos.nxt.SensorPort;

public class LegoTypes {
    
    public static class Sensor {
    
        private SensorPort sensor;
    
        public Sensor(String s) {
            sensor = getSensorPort(s);
        }
        
        public SensorPort getSensor() {
            return sensor;
        }
        
        public int getID() {
            return sensor.getId();
        }
        
        private SensorPort getSensorPort(String port) {
            if (port.equals("S1"))  return SensorPort.S1;
            if (port.equals("S2"))  return SensorPort.S2;
            if (port.equals("S3"))  return SensorPort.S3;
            if (port.equals("S4"))  return SensorPort.S4;
            return null;
        }
    }
    
    public static class Button {
         lejos.nxt.Button b;
        
        public Button(String button) { 
            b = getButton(button);
        }
        
        public lejos.nxt.Button getButton() {
            return b;
        }
    
        private lejos.nxt.Button getButton(String button) {
            if (button.equals("ENTER"))     return lejos.nxt.Button.ENTER;
            if (button.equals("LEFT"))      return lejos.nxt.Button.LEFT;
            if (button.equals("RIGHT"))     return lejos.nxt.Button.RIGHT;
                                            return lejos.nxt.Button.ESCAPE;   
        }
    }
    
    public static class Motor {
        NXTRegulatedMotor motor;
        
        public Motor(String mot) {
            motor = getNXTMotor(mot);
        }
        
        public NXTRegulatedMotor getMotor() {
            return motor;
        }
    
        private NXTRegulatedMotor getNXTMotor(String motor) {
            if (motor.equals("motorA")) return lejos.nxt.Motor.A;
            if (motor.equals("motorB")) return lejos.nxt.Motor.B;
            if (motor.equals("motorC")) return lejos.nxt.Motor.C;
            return null;
        }
    
    }

}

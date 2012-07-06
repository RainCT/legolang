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

package interp;
import java.util.HashMap;

public class PortConfig {

    // ultrasonic, sound, touch, light
    private String[] defaults = { null, null, null, null };
    private HashMap<String, String> usedSensors = new HashMap<String, String>();//<Port, sensorName>

    public void setDefault(String sensor, String port) {
        int index = getIndexForName(sensor);
        if (defaults[index] != null)
            throw new RuntimeException(
                "Duplicate default definition for " + sensor);
        defaults[index] = port;
        usedSensors.put(port, sensor);
    }

    public String getDefault(String sensor) {
        String value = defaults[getIndexForName(sensor)];
        if (value == null)
            throw new RuntimeException(
                "Missing default definition for " + sensor);
        return value;
    }

    private int getIndexForName(String sensor) {
        if (sensor.equals("ultrasonic")) return 0;
        if (sensor.equals("sound")) return 1;
        if (sensor.equals("touch")) return 2;
        if (sensor.equals("light")) return 3;
        assert false;
        return 0;
    }
    
    public void addUsed(String sensor, String port, int line) {
        String storedSensor = usedSensors.get(port);//name of the sensor
        if (storedSensor != null && !storedSensor.equals(sensor))         
            System.err.println("Warning in line " + line + ": You are already using port "+port+" for the "+
                                storedSensor+" sensor. Are you sure that you want to use it now for the "+sensor+" sensor?");
        else usedSensors.put(port, sensor); 
    }

}

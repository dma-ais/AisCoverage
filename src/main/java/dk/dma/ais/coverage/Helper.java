/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.coverage;

import java.util.Calendar;
import java.util.Date;

import dk.dma.ais.coverage.calculator.geotools.SphereProjection;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.data.Cell;

public class Helper {

    public static Date analysisStarted;
    public static Date latestMessage;
    public static Date firstMessage;
    private static SphereProjection projection = new SphereProjection();
    public static AisCoverageConfiguration conf;

    public static SphereProjection getProjection() {
        return projection;
    }

    public static Date getFloorDate(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);

        // Set time fields to zero
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public static Date getCeilDate(Date d) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(d.getTime() + 1000 * 60 * 60));

        // Set time fields to zero
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();

    }

    /**
     * latitude is rounded down longitude is rounded down. The id is lat-lon-coords representing bottom-left point in cell
     */
    public static String getCellId(double latitude, double longitude, int multiplicationFactor) {
        //TODO make a more space efficient ID. Instead of using a concatenated string.
        return roundLat(latitude, multiplicationFactor) + "_" + roundLon(longitude, multiplicationFactor);
    }

    public static double roundLat(double latitude, int multiplicationFactor) {
        double multiple = conf.getLatSize() * multiplicationFactor;
        return multiple * Math.floor(latitude / multiple);
    }

    public static double roundLon(double longitude, int multiplicationFactor) {
        double multiple = conf.getLonSize() * multiplicationFactor;
        return multiple * Math.floor(longitude / multiple);
    }
    
    public static void setLatLonSize(int meters, double latitude){
        conf.setLatSize(SphereProjection.metersToLatDegree(meters));
        conf.setLonSize(SphereProjection.metersToLonDegree(latitude, meters));
    }
    
    public static boolean isInsideBox(Cell c, double latStart, double lonStart, double latEnd, double lonEnd){
        if (lonStart > lonEnd) {

            if (c.getLatitude() <= latStart && c.getLatitude() >= latEnd) {
                if (c.getLongitude() >= lonStart || c.getLongitude() <= lonEnd) {

                    return true;
                }
            }

        } else {

            if (c.getLatitude() <= latStart && c.getLatitude() >= latEnd) {
                if (c.getLongitude() >= lonStart && c.getLongitude() <= lonEnd) {

                    return true;
                }
            }

        }
        return false;
    }
}

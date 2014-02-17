/* Copyright (c) 2011 Danish Maritime Authority
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this library.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.dma.ais.coverage.calculator.geotools;

import java.util.Calendar;
import java.util.Date;

public class Helper {

    public static double latSize = 0.022522522522522525;
    public static double lonSize = 0.03868125413876516;
    public static Date analysisStarted;
    public static Date latestMessage;
    private static SphereProjection projection = new SphereProjection();

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
        // System.out.println("roundlat="+roundLat(latitude, multiplicationFactor));
        // System.out.println("roundLon="+roundLon(latitude, multiplicationFactor));
        // System.out.println();
        return roundLat(latitude, multiplicationFactor) + "_" + roundLon(longitude, multiplicationFactor);
    }

    public static double roundLat(double latitude, int multiplicationFactor) {
        double multiple = (latSize * multiplicationFactor);
        return multiple * (Math.floor(latitude / multiple));
    }

    public static double roundLon(double longitude, int multiplicationFactor) {
        double multiple = (lonSize * multiplicationFactor);
        return multiple * (Math.floor(longitude / multiple));
    }
}

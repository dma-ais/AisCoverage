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

public class GeoConverter {
    public static double metersToLonDegree(double latitude, double meters) {

        // calculate length of 1 degree lon
        double latRad = Math.toRadians(latitude);
        double a = 6378137;
        double b = 6356752.3142;
        double ee = ((a * a) - (b * b)) / (a * a);
        double oneDegreeLength = (Math.PI * a * Math.cos(latRad))
                / (180 * Math.pow(1 - ee * ((Math.sin(latRad) * Math.sin(latRad))), 0.5));
        double lonDegree = (1 / oneDegreeLength) * meters;

        return lonDegree;
    }

    public static double metersToLatDegree(double meters) {
        return ((double) 1 / 111000) * meters;
    }
    // public static double latToMeters(double p1Lat, double p2Lat, double lon){
    // Position p1 = new Position(p1Lat, lon);
    //
    // Position p2 = new Position(p2Lat,lon);
    //
    // double distanceInMeters = p1.getRhumbLineDistance(p2);
    // return distanceInMeters;
    // }
    // public static double lonToMeters(double p1Lon, double p2Lon, double lat){
    // GeoLocation p1 = new GeoLocation();
    // p1.setLongitude(p1Lon);
    // p1.setLatitude(lat);
    // GeoLocation p2 = new GeoLocation();
    // p2.setLongitude(p2Lon);
    // p2.setLatitude(lat);
    // double distanceInMeters = p1.getRhumbLineDistance(p2);
    // return distanceInMeters;
    // }

}

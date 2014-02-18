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
package dk.dma.ais.coverage.data;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Cell {

    private int NOofReceivedSignals;
    private int NOofMissingSignals;
    private double latitude;
    private double longitude;
    private List<TimeSpan> timeSpans;
    private Map<Long, TimeSpan> fixedWidthSpans = new HashMap<Long, TimeSpan>();

    public Map<Long, TimeSpan> getFixedWidthSpans() {
        return fixedWidthSpans;
    }

    public void setFixedWidthSpans(Map<Long, TimeSpan> fixedWidthSpans) {
        this.fixedWidthSpans = fixedWidthSpans;
    }

    public List<TimeSpan> getTimeSpans() {
        return timeSpans;
    }

    public void setTimeSpans(List<TimeSpan> timeSpans) {
        this.timeSpans = timeSpans;
    }

    public Cell(Source grid, double lat, double lon, String id) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public Cell(double lat, double lon, String id) {
        this.latitude = lat;
        this.longitude = lon;
    }

    public void incrementNOofReceivedSignals() {
        NOofReceivedSignals++;
    }

    public void incrementNOofMissingSignals() {
        NOofMissingSignals++;
    }

    public long getTotalNumberOfMessages() {
        return NOofReceivedSignals + NOofMissingSignals;
    }

    public double getCoverage() {
        return (double) NOofReceivedSignals / (double) getTotalNumberOfMessages();
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getId() {
        return this.latitude + "_" + this.longitude;
    }

    public int getNOofReceivedSignals(Date starttime, Date endTime) {
        int result = 0;
        Collection<TimeSpan> spans = fixedWidthSpans.values();
        for (TimeSpan timeSpan : spans) {

            if (timeSpan.getFirstMessage().getTime() >= starttime.getTime()
                    && timeSpan.getLastMessage().getTime() <= endTime.getTime()) {

                result = result + timeSpan.getMessageCounterTerrestrial();
            }
        }

        return result;
    }

    public int getNOofMissingSignals(Date starttime, Date endTime) {
        int result = 0;
        Collection<TimeSpan> spans = fixedWidthSpans.values();
        for (TimeSpan timeSpan : spans) {
            if (timeSpan.getFirstMessage().getTime() >= starttime.getTime()
                    && timeSpan.getLastMessage().getTime() <= endTime.getTime()) {
                result = result + timeSpan.getMissingSignals();
            }
        }
        return result;
    }

    public int getNOofReceivedSignals() {
        return this.NOofReceivedSignals;
    }

    public int getNOofMissingSignals() {
        return this.NOofMissingSignals;
    }

    public void addReceivedSignals(int amount) {
        this.NOofReceivedSignals += amount;
    }

    public void addNOofMissingSignals(int amount) {
        this.NOofMissingSignals += amount;
    }

    public void setNoofMissingSignals(int amount) {
        this.NOofMissingSignals = amount;
    }

}

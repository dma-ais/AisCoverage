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
package dk.dma.ais.coverage.calculator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dk.dma.ais.coverage.AisCoverage;
import dk.dma.ais.coverage.Helper;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.data.Cell;
import dk.dma.ais.coverage.data.CustomMessage;
import dk.dma.ais.coverage.data.QueryParams;
import dk.dma.ais.coverage.data.Ship;
import dk.dma.ais.coverage.data.Ship.ShipClass;
import dk.dma.ais.coverage.data.Source;
import dk.dma.ais.coverage.data.Source.ReceiverType;
import dk.dma.ais.coverage.data.Source_UserProvided;
import dk.dma.ais.coverage.event.AisEvent;
import dk.dma.ais.coverage.event.AisEvent.Event;
import dk.dma.ais.coverage.event.IAisEventListener;
import dk.dma.ais.coverage.export.data.ExportCell;
import dk.dma.ais.coverage.export.data.JSonCoverageMap;
import dk.dma.ais.coverage.export.data.JsonConverter;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage4;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.proprietary.IProprietarySourceTag;

/**
 * This calculator expects a filtered data stream! (No doublets) The stream must not be downsampled!
 * 
 * It maintains a buffer for each Ship instance.
 * 
 * Rotation is determined based on difference between course over ground (cog) from first and last message in buffer. If rotation is
 * ignored, missing points will only be calculated for ships that are NOT rotating.
 */
public class TerrestrialCalculator extends AbstractCalculator {

    private static final long serialVersionUID = 1L;
    private int bufferInSeconds = 20;
    private int degreesPerMinute = 20;
    private boolean ignoreRotation;
    private List<IAisEventListener> listeners = new ArrayList<IAisEventListener>();
    public boolean debug;
    private LinkedHashMap<String, CustomMessage> doubletBuffer = new LinkedHashMap<String, CustomMessage>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CustomMessage> eldest) {
            return this.size() > 10000;
        }
    };

    public TerrestrialCalculator(){
        
    }
    public void addListener(IAisEventListener l) {
        listeners.add(l);
    }

    public void broadcastEvent(AisEvent e) {
        for (IAisEventListener l : listeners) {
            l.aisEventReceived(e);
        }
    }

    public TerrestrialCalculator(boolean ignoreRotation) {
        this.ignoreRotation = ignoreRotation;
    }

    private boolean checkDoublets(CustomMessage m) {
        String key = m.getKey();

        // if message exist in queue return true, otherwise false.
        if (doubletBuffer.containsKey(key)) {
            return true;
        }
        doubletBuffer.put(key, m);
        return false;

    }

    /**
     * This is called whenever a message is received
     */
    public void calculate(CustomMessage message) {


        Ship ship = dataHandler.getShip(message.getShipMMSI());

        // put message in ships' buffer
        ship.addToBuffer(message);

        // If this message is filtered, we empty the ships' buffer and returns
        if (filterMessage(message)) {
            ship.emptyBuffer();
            return;
        }

        if(Helper.firstMessage == null){
            Helper.firstMessage=Helper.getFloorDate(message.getTimestamp());
        }
        Helper.latestMessage = Helper.getFloorDate(message.getTimestamp());
        // Time difference between first and last message in buffer
        CustomMessage firstMessage = ship.getFirstMessageInBuffer();
        CustomMessage lastMessage = ship.getLastMessageInBuffer();

        if (ship.getMessages().size() == 1) {
            return;
        }
        
        int timeDifference = this.getTimeDifference(firstMessage, lastMessage);
        // Check if it is time to process the buffer
        if (timeDifference >= bufferInSeconds) {
            

            List<CustomMessage> buffer = ship.getMessages();
            double rotation = Math.abs(angleDiff(firstMessage.getCog(), lastMessage.getCog()));

            // Ship is rotating
            if (rotation > ((double) degreesPerMinute / 60) * timeDifference) {
                if (!ignoreRotation) {
                    for (int i = 0; i < ship.getMessages().size() - 1; i++) {
                        calculateMissingPoints(buffer.get(i), buffer.get(i + 1), true);
                    }
                }
            }
            // ship is not rotating
            else {
                for (int i = 0; i < ship.getMessages().size() - 1; i++) {
                    calculateMissingPoints(buffer.get(i), buffer.get(i + 1), false);
                }
            }

            // empty buffer
            ship.emptyBuffer();
        }
    }

    /**
     * Calculates missing points between two messages and add them to corresponding cells
     */
    private void calculateMissingPoints(CustomMessage m1, CustomMessage m2, boolean rotating) {

        Ship ship = dataHandler.getShip(m1.getShipMMSI());

        dataHandler.incrementReceivedSignals(AbstractCalculator.SUPERSOURCE_MMSI, m1.getLatitude(), m1.getLongitude(), m1.getTimestamp());
        approveMessage(m1);
        
        

        Long p1Time = m1.getTimestamp().getTime();
        Long p2Time = m2.getTimestamp().getTime();
        double p1Lat = m1.getLatitude();
        double p1Lon = m1.getLongitude();
        double p2Lat = m2.getLatitude();
        double p2Lon = m2.getLongitude();
        projection.setCentralPoint(p1Lon, p1Lat);
        double p1X = projection.lon2x(p1Lon, p1Lat);
        double p1Y = projection.lat2y(p1Lon, p1Lat);
        double p2X = projection.lon2x(p2Lon, p2Lat);
        double p2Y = projection.lat2y(p2Lon, p2Lat);

        double timeSinceLastMessage = getTimeDifference(p1Time, p2Time);
        int sog = (int) m2.getSog();
        double expectedTransmittingFrequency = getExpectedTransmittingFrequency(sog, rotating, ship.getShipClass());
        /*
         * Calculate missing messages and increment missing signal to corresponding cell. Lat-lon points are calculated to metric
         * x-y coordinates before missing points are calculated. In order to find corresponding cell, x-y coords are converted back
         * to lat-lon.
         */

        int missingMessages;
        if (timeSinceLastMessage > expectedTransmittingFrequency) {

            // Number of missing points between the two points
            missingMessages = (int) (Math.round(timeSinceLastMessage / expectedTransmittingFrequency) - 1);

            // Finds lat/lon of each missing point and adds "missing signal" to
            // corresponding cell
            for (int i = 1; i <= missingMessages; i++) {
                double xMissing = getX(i * expectedTransmittingFrequency, p1Time, p2Time, p1X, p2X);
                double yMissing = getY(i * expectedTransmittingFrequency, p1Time, p2Time, p1Y, p2Y);

                // Add number of missing messages to cell
                Date stamp = new Date((long) (m1.getTimestamp().getTime() + (i * expectedTransmittingFrequency * 1000)));
                dataHandler.incrementMissingSignals(AbstractCalculator.SUPERSOURCE_MMSI, projection.y2Lat(xMissing, yMissing),
                        projection.x2Lon(xMissing, yMissing), stamp);
            }
        }
    }

 
    private void approveMessage(CustomMessage approvedMessage) {
        for (String source : approvedMessage.getSourceList()) {
            dataHandler.incrementReceivedSignals(source, approvedMessage.getLatitude(),
                    approvedMessage.getLongitude(), approvedMessage.getTimestamp());
            
        }
//        System.out.println();
    }

    /**
     * Calculates the signed difference between angle A and angle B
     * 
     * @param a
     *            Angle1 in degrees
     * @param b
     *            Angle2 in degrees
     * @return The difference in degrees
     */
    private double angleDiff(double a, double b) {
        double difference = b - a;
        while (difference < -180.0) {
            difference += 360.0;
        }
        while (difference > 180.0) {
            difference -= 360.0;
        }
        return difference;
    }
    
    //TODO consider moving this method to a calculator
    public JSonCoverageMap getTerrestrialCoverage(double latStart, double lonStart, double latEnd, double lonEnd,
            Set<String> sources, int multiplicationFactor, Date starttime, Date endtime) {

        AisCoverageConfiguration conf = AisCoverage.get().getConf();
        JSonCoverageMap map = new JSonCoverageMap();
        map.latSize = conf.getLatSize() * multiplicationFactor;
        map.lonSize = conf.getLonSize() * multiplicationFactor;

        HashMap<String, ExportCell> JsonCells = new HashMap<String, ExportCell>();

        QueryParams params = new QueryParams();
        params.latStart = latStart;
        params.latEnd = latEnd;
        params.lonStart = lonStart;
        params.lonEnd = lonEnd;
        params.sources = sources;
        params.multiplicationFactor = multiplicationFactor;
        params.startDate = starttime;
        params.endDate = endtime;

        List<Cell> celllist = dataHandler.getCells(params);
        Set<String> superSourceIsHere = new HashSet<String>();
        superSourceIsHere.add(AbstractCalculator.SUPERSOURCE_MMSI);
        params.sources = superSourceIsHere;
        List<Cell> celllistSuper = dataHandler.getCells(params);
        Map<String, Cell> superMap = new HashMap<String, Cell>();
        for (Cell cell : celllistSuper) {
            if (cell.getNOofReceivedSignals() > 0) {
                superMap.put(cell.getId(), cell);
            }
        }

        if (!celllist.isEmpty()) {
            map.latSize = conf.getLatSize() * multiplicationFactor;
        }

        for (Cell cell : celllist) {
            Cell superCell = superMap.get(cell.getId());
            if (superCell == null) {

            } else {
                    ExportCell existing = JsonCells.get(cell.getId());
                    ExportCell theCell = JsonConverter.toJsonCell(cell, superCell, starttime, endtime);
                    if (existing == null || theCell.getCoverage() > existing.getCoverage()) {
                        JsonCells.put(cell.getId(), theCell);
                    }
                
            }
        }

        map.cells = JsonCells;

        return map;
    }

    // Getters and setters
    private double getY(double seconds, Long p1Time, Long p2Time, double p1y, double p2y) {
        double distanceInMeters = p2y - p1y;
        double timeDiff = getTimeDifference(p1Time, p2Time);
        double metersPerSec = distanceInMeters / timeDiff;
        return p1y + (metersPerSec * seconds);
    }

    private double getX(double seconds, Long p1Time, Long p2Time, double p1x, double p2x) {
        double distanceInMeters = p2x - p1x;
        double timeDiff = getTimeDifference(p1Time, p2Time);
        double metersPerSec = distanceInMeters / timeDiff;
        return p1x + (metersPerSec * seconds);
    }

    public int getBufferInSeconds() {
        return bufferInSeconds;
    }

    public void setBufferInSeconds(int bufferInSeconds) {
        this.bufferInSeconds = bufferInSeconds;
    }

    public int getDegreesPerMinute() {
        return degreesPerMinute;
    }

    public void setDegreesPerMinute(int degreesPerMinute) {
        this.degreesPerMinute = degreesPerMinute;
    }

    public boolean isIgnoreRotation() {
        return ignoreRotation;
    }

    public void setIgnoreRotation(boolean ignoreRotation) {
        this.ignoreRotation = ignoreRotation;
    }

}

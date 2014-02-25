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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import dk.dma.ais.coverage.AisCoverageGUI;
import dk.dma.ais.coverage.calculator.geotools.SphereProjection;
import dk.dma.ais.coverage.data.CustomMessage;
import dk.dma.ais.coverage.data.ICoverageData;
import dk.dma.ais.coverage.data.OnlyMemoryData;
import dk.dma.ais.coverage.data.Ship;
import dk.dma.ais.coverage.data.Ship.ShipClass;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage5;
import dk.dma.ais.message.ShipTypeCargo;
import dk.dma.ais.message.ShipTypeCargo.ShipType;

/**
 * See CoverageCalculator and DensityPlotCalculator for examples of how to extend this class. When a calculator is added to an
 * AisCoverageProject instance, the calculator automatically receives CustomMessages via calculate().
 * 
 */
public abstract class AbstractCalculator implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = LoggerFactory.getLogger(AisCoverageGUI.class);

    // protected AisCoverageProject project;
    protected Map<ShipClass, ShipClass> allowedShipClasses = new ConcurrentHashMap<ShipClass, ShipClass>();
    protected Map<ShipType, ShipType> allowedShipTypes = new ConcurrentHashMap<ShipType, ShipType>();
    protected Map<Integer, Boolean> allowedShips = new ConcurrentHashMap<Integer, Boolean>();
    protected CustomMessage firstMessage;
    protected CustomMessage currentMessage;
    protected ICoverageData dataHandler = new OnlyMemoryData();
    protected double filterTimeDifference;
    protected int maxDistanceBetweenFirstAndLast = 2000;
    protected int minAllowedSpeed = 3;
    protected int maxAllowedSpeed = 50;
    protected transient SphereProjection projection = new SphereProjection();
    public static final String SUPERSOURCE_MMSI = "supersource";
    
//    private int cellSize = 2500;

    public double getFilterTimeDifference() {
        return filterTimeDifference;
    }

    public void setFilterTimeDifference(double filterTimeDifference) {
        this.filterTimeDifference = filterTimeDifference;
    }

    public int getMaxDistanceBetweenFirstAndLast() {
        return maxDistanceBetweenFirstAndLast;
    }

    public void setMaxDistanceBetweenFirstAndLast(int maxDistanceBetweenFirstAndLast) {
        this.maxDistanceBetweenFirstAndLast = maxDistanceBetweenFirstAndLast;
    }

    public int getMinAllowedSpeed() {
        return minAllowedSpeed;
    }

    public void setMinAllowedSpeed(int minAllowedSpeed) {
        this.minAllowedSpeed = minAllowedSpeed;
    }

    public int getMaxAllowedSpeed() {
        return maxAllowedSpeed;
    }

    public void setMaxAllowedSpeed(int maxAllowedSpeed) {
        this.maxAllowedSpeed = maxAllowedSpeed;
    }

    public abstract void calculate(CustomMessage m);

    /**
     * Determines the expected transmitting frequency, based on speed over ground(sog), whether the ship is rotating and ship class.
     * This can be used to calculate coverage.
     */
    public double getExpectedTransmittingFrequency(double sog, boolean rotating, ShipClass shipClass) {
        double expectedTransmittingFrequency;
        if (shipClass == ShipClass.CLASS_A) {
            if (rotating) {
                if (sog < .2) {
                    expectedTransmittingFrequency = 180;
                } else if (sog < 14) {
                    expectedTransmittingFrequency = 3.33;
                } else if (sog < 23) {
                    expectedTransmittingFrequency = 2;
                } else {
                    expectedTransmittingFrequency = 2;
                }
            } else {
                if (sog < .2) {
                    expectedTransmittingFrequency = 180;
                } else if (sog < 14) {
                    expectedTransmittingFrequency = 10;
                } else if (sog < 23) {
                    expectedTransmittingFrequency = 6;
                } else {
                    expectedTransmittingFrequency = 2;
                }
            }
        } else {
            if (sog <= 2) {
                expectedTransmittingFrequency = 180;
            } else {
                expectedTransmittingFrequency = 30;
            }
        }

        return expectedTransmittingFrequency;

    }

    /*
     * Use this method to filter out unwanted messages. The filtering is based on rules of thumbs. For instance, if a distance
     * between two messages is over 2000m, we filter
     */
    public boolean filterMessage(CustomMessage customMessage) {

        if (customMessage.getSog() < 3 || customMessage.getSog() > 50) {
            return true;
        }
        if (customMessage.getCog() == 360) {
            return true;
        }

        Ship ship = dataHandler.getShip(customMessage.getShipMMSI());

        CustomMessage firstMessage = ship.getFirstMessageInBuffer();
        CustomMessage lastMessage = ship.getLastMessageInBuffer();
        if (lastMessage != null) {

            // Filter message based on distance between first and last message
            projection.setCentralPoint(firstMessage.getLongitude(), firstMessage.getLatitude());
            double distance = projection.distBetweenPoints(firstMessage.getLongitude(), firstMessage.getLatitude(),
                    lastMessage.getLongitude(), lastMessage.getLatitude());
            if (distance > 2000) {
                return true;
            }

            // Filter message based on time between first and last message
            double timeDifference = this.getTimeDifference(firstMessage, lastMessage);
            if (timeDifference > 1200) {
                return true;
            }

        }
        return false;
    }

    protected boolean isShipAllowed(AisMessage aisMessage) {
        if (allowedShipTypes.size() > 0) {

            // Ship type message
            if (aisMessage instanceof AisMessage5) {

                // if ship type is allowed, we add ship mmsi to allowedShips map
                AisMessage5 m = (AisMessage5) aisMessage;
                ShipTypeCargo shipTypeCargo = new ShipTypeCargo(m.getShipType());
                if (allowedShipTypes.containsKey(shipTypeCargo.getShipType())) {
                    allowedShips.put(m.getUserId(), true);
                }
                // It's not a position message, so we return false
                return false;
            }

            // if ship isn't in allowedShips we don't process the message
            if (!allowedShips.containsKey(aisMessage.getUserId())) {
                return false;
            }
        }
        return true;
    }

//    protected ShipClass extractShipClass(AisMessage aisMessage) {
//        if (aisMessage.getMsgId() == 18) {
//            // class B
//            return Ship.ShipClass.CLASS_B;
//        } else {
//            // class A
//            return Ship.ShipClass.CLASS_A;
//        }
//    }

    // getters and setters
    public ICoverageData getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(ICoverageData dataHandler) {
        this.dataHandler = dataHandler;
    }


    /**
     * Time difference between two messages in seconds
     */
    public int getTimeDifference(CustomMessage m1, CustomMessage m2) {
        return (int) Math.abs(((m2.getTimestamp().getTime() - m1.getTimestamp().getTime())) / 1000);
    }

    public double getTimeDifference(Long m1, Long m2) {
        return (double) Math.abs((m2 - m1) / 1000);
    }

    public CustomMessage getFirstMessage() {
        return firstMessage;
    }

    public CustomMessage getCurrentMessage() {
        return currentMessage;
    }
    
   

}

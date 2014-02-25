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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dma.ais.coverage.Helper;

/**
 * A single ship instance exist for each real life ship. The instance maintains a message buffer.
 * Messages are filtered such that only a single message is in the buffer, no matter how many sources received it.
 * 
 * Also a location history is maintained
 *
 */
public class Ship implements Serializable {
    private static final long serialVersionUID = 1L;

    private int mmsi;
    private List<CustomMessage> messageBuffer = new ArrayList<CustomMessage>();
    private ShipClass shipClass;
    private Map<Short, Hour> hours = new HashMap<Short, Hour>(); //used for location tracking
    
    public Map<Short, Hour> getHours() {
        return hours;
    }


    public enum ShipClass {
        CLASS_A, CLASS_B
    }
    public Ship(int mmsi, ShipClass shipClass) {
        this.mmsi = mmsi;
        this.shipClass = shipClass;
    }

    public void addToBuffer(CustomMessage m) {
        messageBuffer.add(m);
    }

    public List<CustomMessage> getMessages() {
        return messageBuffer;
    }

    public void emptyBuffer() {
        CustomMessage last = getLastMessageInBuffer();
        messageBuffer.clear();
        messageBuffer.add(last); // We still want the last message in the buffer
    }

    public ShipClass getShipClass() {
        return shipClass;
    }

    public void setShipClass(ShipClass shipClass) {
        this.shipClass = shipClass;
    }

    public CustomMessage getFirstMessageInBuffer() {
        if(messageBuffer.isEmpty()){
            return null;
        }
        return messageBuffer.get(0);
    }

    public CustomMessage getLastMessageInBuffer() {
        if(messageBuffer.isEmpty()){
            return null;
        }
        return messageBuffer.get(messageBuffer.size() - 1);
    }

    public int getMmsi() {
        return mmsi;
    }
    
    public void registerMessage(Date timestamp, float lat, float lon) {
        int minutesSince = (int) ((timestamp.getTime() - Helper.firstMessage.getTime()) / 1000 / 60);
        short hoursSince = (short) ((Helper.getFloorDate(timestamp).getTime() - Helper.firstMessage.getTime()) / 1000 / 60 / 60);
        int minutesOffset = minutesSince - hoursSince * 60;
        if (minutesSince < 0) {
            return;
        }

        Hour hour = hours.get(hoursSince);
        if (hour == null) {
            hour = new Hour();
            hours.put(hoursSince, hour);
        }
        hour.setPosition(minutesOffset, lat, lon);

    }

    public static void main(String[] args) {
//        Ship ss = new Ship();
//        Hour h = ss.new Hour();
//        h.setPosition(0, 57.7819f, 2.8765f);
//        h.setPosition(0, 57.8819f, 2.8765f);
//        h.setPosition(13, 57.58194683f, 2.63f);
//        // for (int i = 0; i < 60; i++) {
//        // System.out.println(i+ " "+h.gotSignal(i));
//        // }
//        System.out.println(h.getLat(0));
//        System.out.println(h.getLon(9));

    }

    public class Hour {

        // Position offset
        float latOffset;
        float lonOffset;

        // Positions per 10th minute in 10-meters from offset
        short[] positions = new short[6 * 2];

        // Two integers represents 60 bits (60 minutes)
        // Indicating if a message was received in the corresponding minute
        public int half1;
        public int half2;

        /**
         * Sets the position at the given minute A position must not be more than 32,767*10 meters from offset We assume no ships
         * travel more than 328 km within an hour In this way we can store a position using 2 shorts isntead of 2 floats
         * 
         * @param minute
         * @param lat
         * @param lon
         */
        public void setPosition(int minute, float lat, float lon) {

            // Set bit-flag at minute
            if (minute < 30) {
                half1 |= 1 << minute;
            } else {
                half2 |= 1 << (minute - 30);
            }

            // If lat-lon offset has not been set, set it
            if (latOffset == 0) {
                latOffset = lat;
                lonOffset = lon;
            }

            double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
            double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
            double p2X = Helper.getProjection().lon2x(lon, lat);
            double p2Y = Helper.getProjection().lat2y(lon, lat);
            short xDistance = (short) ((p1X - p2X) / 10); // Distance to offset in xDistance*10 meters
            short yDistance = (short) ((p1Y - p2Y) / 10); // Distance to offset in yDistance*10 meters
            if (xDistance == 0) {
                xDistance = 1;
            }
            if (yDistance == 0) {
                xDistance = 1;
            }
            if (xDistance > 32767 || yDistance > 32767) {
                return; // Something wrong with this message
            }
            // Set meters from offset at the right position field
            if (minute < 5) {
                positions[0] = xDistance;
                positions[1] = yDistance;
            } else if (minute < 15) {
                positions[2] = xDistance;
                positions[3] = yDistance;
            } else if (minute < 25) {
                positions[4] = xDistance;
                positions[5] = yDistance;
            } else if (minute < 35) {
                positions[6] = xDistance;
                positions[7] = yDistance;
            } else if (minute < 45) {
                positions[8] = xDistance;
                positions[9] = yDistance;
            } else if (minute < 55) {
                positions[10] = xDistance;
                positions[11] = yDistance;
            }
        }

        public float getLat(int minute) {
            double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
            double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
            if (minute < 5) {
                if (positions[0] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[0] * 10), p1Y - (positions[1] * 10));
            } else if (minute < 15) {
                if (positions[2] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[2] * 10), p1Y - (positions[3] * 10));
            } else if (minute < 25) {
                if (positions[4] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[4] * 10), p1Y - (positions[5] * 10));
            } else if (minute < 35) {
                if (positions[6] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[6] * 10), p1Y - (positions[7] * 10));
            } else if (minute < 45) {
                if (positions[8] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[8] * 10), p1Y - (positions[9] * 10));
            } else if (minute < 55) {
                if (positions[10] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[10] * 10), p1Y - (positions[11] * 10));
            } else {
                if (positions[10] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().y2Lat(p1X - (positions[10] * 10), p1Y - (positions[11] * 10));
            }
        }

        public float getLon(int minute) {
            double p1X = Helper.getProjection().lon2x(lonOffset, latOffset);
            double p1Y = Helper.getProjection().lat2y(lonOffset, latOffset);
            if (minute < 5) {
                if (positions[0] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[0] * 10), p1Y - (positions[1] * 10));
            } else if (minute < 15) {
                if (positions[2] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[2] * 10), p1Y - (positions[3] * 10));
            } else if (minute < 25) {
                if (positions[4] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[4] * 10), p1Y - (positions[5] * 10));
            } else if (minute < 35) {
                if (positions[6] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[6] * 10), p1Y - (positions[7] * 10));
            } else if (minute < 45) {
                if (positions[8] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[8] * 10), p1Y - (positions[9] * 10));
            } else if (minute < 55) {
                if (positions[10] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[10] * 10), p1Y - (positions[11] * 10));
            } else {
                if (positions[10] == 0) {
                    return 0;
                }
                return (float) Helper.getProjection().x2Lon(p1X - (positions[10] * 10), p1Y - (positions[11] * 10));
            }
        }

        public boolean gotSignal(int minute) {
            if (minute < 30) {
                if ((half1 & (1 << minute)) == 0) {
                    return false;
                }
                return true;
            } else {
                minute = minute - 30;
                if ((half2 & (1 << minute)) == 0) {
                    return false;
                }
                return true;
            }
        }
    }

}

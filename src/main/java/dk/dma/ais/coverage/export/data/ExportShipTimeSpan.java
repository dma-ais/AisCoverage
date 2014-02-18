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
package dk.dma.ais.coverage.export.data;

import java.util.ArrayList;
import java.util.List;

public class ExportShipTimeSpan {

    private List<LatLon> positions = new ArrayList<LatLon>();

    public ExportShipTimeSpan(long time) {
        firstMessage = time;
        lastMessage = time;
    }

    public List<LatLon> getPositions() {
        return positions;
    }

    public void setPositions(List<LatLon> positions) {
        this.positions = positions;
    }

    public long getFirstMessage() {
        return firstMessage;
    }

    public void setFirstMessage(long firstMessage) {
        this.firstMessage = firstMessage;
    }

    public long getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(long lastMessage) {
        this.lastMessage = lastMessage;
    }

    private long firstMessage, lastMessage;

    public class LatLon {
        private float lat, lon;

        public LatLon(float lat, float lon) {
            this.lat = lat;
            this.lon = lon;
        }

        public float getLat() {
            return lat;
        }

        public void setLat(float lat) {
            this.lat = lat;
        }

        public float getLon() {
            return lon;
        }

        public void setLon(float lon) {
            this.lon = lon;
        }
    }

}

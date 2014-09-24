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

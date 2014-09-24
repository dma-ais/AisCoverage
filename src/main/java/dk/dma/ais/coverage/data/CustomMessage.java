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
package dk.dma.ais.coverage.data;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dk.dma.ais.message.AisMessage;
import dk.dma.ais.packet.AisPacketTags.SourceType;

/**
 * Used for storing information relevant for the calculators
 */
public class CustomMessage implements Serializable {
    private AisMessage originalMessage;

    private static final long serialVersionUID = 1L;
    private double cog;
    private double sog;
    private double latitude;
    private double longitude;
    private long timestamp;
    private Set<String> sourceList = new HashSet<String>();
    private int shipMMSI;
    private long timeSinceLastMsg;
    private String key;
    private SourceType sourceType;

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public String getKey() {
        return getCog() + "" + getLatitude() + "" + getLongitude() + "" + getShipMMSI() + "" + getSog();
    }

    public double getCog() {
        return cog;
    }

    public void setCog(double cog) {
        this.cog = cog;
    }

    public double getSog() {
        return sog;
    }

    public void setSog(double sog) {
        this.sog = sog;
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

    public Date getTimestamp() {
        return new Date(timestamp);
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp.getTime();
    }

    public Set<String> getSourceList() {
        return sourceList;
    }
    public void addSourceMMSI(String source){
        sourceList.add(source);
    }

    public int getShipMMSI() {
        return shipMMSI;
    }

    public void setShipMMSI(int shipMMSI) {
        this.shipMMSI = shipMMSI;
    }

    public long getTimeSinceLastMsg() {
        return timeSinceLastMsg;
    }

    public void setTimeSinceLastMsg(long timeSinceLastMsg) {
        this.timeSinceLastMsg = timeSinceLastMsg;
    }

    public AisMessage getOriginalMessage() {
        return originalMessage;
    }

    public void setOriginalMessage(AisMessage originalMessage) {
        this.originalMessage = originalMessage;
    }

}

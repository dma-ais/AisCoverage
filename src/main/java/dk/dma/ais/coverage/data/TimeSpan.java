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

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TimeSpan {
    private Map<String, Boolean> distinctShipsSat = new ConcurrentHashMap<String, Boolean>();
    private Map<String, Boolean> distinctShipsTerrestrial = new ConcurrentHashMap<String, Boolean>();
    private long firstMessage, lastMessage;
    private int messageCounterSat;
    private int messageCounterTerrestrial;
    private int missingSignals;
    private int messageCounterTerrestrialUnfiltered;

    public int getMessageCounterTerrestrialUnfiltered() {
        return messageCounterTerrestrialUnfiltered;
    }

    public void incrementMessageCounterTerrestrialUnfiltered() {
        this.messageCounterTerrestrialUnfiltered++;
    }

    public void setMessageCounterTerrestrialUnfiltered(int number) {
        this.messageCounterTerrestrialUnfiltered = number;
    }

    public void addMessageCounterTerrestrialUnfiltered(int number) {
        this.messageCounterTerrestrialUnfiltered += number;
    }

    public void incrementMissingSignals() {
        missingSignals++;
    }

    public int getMissingSignals() {
        return missingSignals;
    }

    public Map<String, Boolean> getDistinctShipsTerrestrial() {
        return distinctShipsTerrestrial;
    }

    public int getMessageCounterTerrestrial() {
        return messageCounterTerrestrial;
    }

    public void setMessageCounterTerrestrial(int messageCounterTerrestrial) {
        this.messageCounterTerrestrial = messageCounterTerrestrial;
    }

    public TimeSpan(Date firstMessage) {
        this.firstMessage = firstMessage.getTime();
        this.lastMessage = firstMessage.getTime();
    }

    public Map<String, Boolean> getDistinctShipsSat() {
        return distinctShipsSat;
    }

    public Date getFirstMessage() {
        return new Date(firstMessage);
    }

    public void setFirstMessage(Date firstMessage) {
        this.firstMessage = firstMessage.getTime();
    }

    public Date getLastMessage() {
        return new Date(lastMessage);
    }

    public void setLastMessage(Date lastMessage) {
        this.lastMessage = lastMessage.getTime();
    }

    public int getMessageCounterSat() {
        return messageCounterSat;
    }

    public void setMessageCounterSat(int messageCounter) {
        this.messageCounterSat = messageCounter;
    }

    public void add(TimeSpan span2) {
        this.setMessageCounterSat(this.getMessageCounterSat() + span2.getMessageCounterSat());
        this.setMessageCounterTerrestrial(this.getMessageCounterTerrestrial() + span2.getMessageCounterTerrestrial());
        this.addMessageCounterTerrestrialUnfiltered(span2.getMessageCounterTerrestrialUnfiltered());
        for (String s : span2.distinctShipsSat.keySet()) {
            this.distinctShipsSat.put(s, true);
        }
        for (String s : span2.distinctShipsTerrestrial.keySet()) {
            this.distinctShipsTerrestrial.put(s, true);
        }
    }

    public TimeSpan copy() {
        TimeSpan copy = new TimeSpan(this.getFirstMessage());
        copy.setLastMessage(this.getLastMessage());
        copy.setMessageCounterSat(this.getMessageCounterSat());
        copy.setMessageCounterTerrestrial(this.getMessageCounterTerrestrial());
        copy.setMessageCounterTerrestrialUnfiltered(this.messageCounterTerrestrialUnfiltered);
        for (String s : this.distinctShipsSat.keySet()) {
            copy.distinctShipsSat.put(s, true);
        }
        for (String s : this.distinctShipsTerrestrial.keySet()) {
            copy.distinctShipsTerrestrial.put(s, true);
        }
        return copy;
    }
}

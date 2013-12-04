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
package dk.dma.ais.coverage.event;

public class AisEvent {

    public static enum Event {
        PROJECT_LOADED, PROJECT_CREATED, ANALYSIS_STARTED, ANALYSIS_STOPPED, BS_VISIBILITY_CHANGED, BS_ADDED, BS_POSITION_FOUND, AISMESSAGE_APPROVED, AISMESSAGE_REJECTED
    }

    private Event event;
    private Object source;
    private Object eventObject;

    public AisEvent() {
    }

    public AisEvent(Event event, Object source, Object eventObject) {
        this.event = event;
        this.source = source;
        this.eventObject = eventObject;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public Object getEventObject() {
        return eventObject;
    }

    public void setEventObject(Object eventObject) {
        this.eventObject = eventObject;
    }
}

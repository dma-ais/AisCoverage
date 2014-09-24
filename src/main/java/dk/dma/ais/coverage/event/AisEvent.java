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

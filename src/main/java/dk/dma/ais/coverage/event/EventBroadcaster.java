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

import java.util.ArrayList;
import java.util.List;

/**
 * Useful for listening for AIS events, just register a IAisEventListener. Other objects can broadcast events to listeners via
 * broadcastEvent(AisEvent).
 * 
 * Get access to this by calling ProjectHandler.getInstance()
 * 
 */
public final class EventBroadcaster {

    private List<IAisEventListener> listeners = new ArrayList<IAisEventListener>();

    public void broadcastEvent(AisEvent event) {
        for (IAisEventListener listener : listeners) {
            listener.aisEventReceived(event);
        }
    }

    public void addProjectHandlerListener(IAisEventListener listener) {
        listeners.add(listener);
    }

    // Singleton stuff
    private static EventBroadcaster singletonObject;

    private EventBroadcaster() {

    }

    public static synchronized EventBroadcaster getInstance() {
        if (singletonObject == null) {
            singletonObject = new EventBroadcaster();
        }
        return singletonObject;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}

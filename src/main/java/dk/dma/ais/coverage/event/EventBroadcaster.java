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

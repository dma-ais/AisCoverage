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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.coverage.calculator.AbstractCalculator;

public class SourceHandler implements Serializable {

    private static final long serialVersionUID = 1L;
    private ConcurrentHashMap<String, Source> baseStations = new ConcurrentHashMap<String, Source>();
    
    public SourceHandler(){
        baseStations.put(AbstractCalculator.SUPERSOURCE_MMSI, new Source(AbstractCalculator.SUPERSOURCE_MMSI));
    }

    /*
     * Create grid associated to a specific transponder
     */
    public Source createGrid(String bsMmsi) {
        Source grid = new Source(bsMmsi);
        baseStations.put(bsMmsi, grid);

        return grid;
    }

    public void setAllVisible(boolean b) {
        Collection<Source> basestations = baseStations.values();
        for (Source baseStation : basestations) {
            setVisible(baseStation.getIdentifier(), b);
        }
    }

    public void setVisible(String mmsi, boolean b) {
        Source baseStation = baseStations.get(mmsi);
        if (baseStation != null) {
            baseStation.setVisible(b);
        }
    }

    public Source getSource(String bsMmsi) {
        return baseStations.get(bsMmsi);
    }

    public Map<String, Source> getSources() {
        return baseStations;
    }
}

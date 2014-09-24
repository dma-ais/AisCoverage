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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.dma.ais.coverage.calculator.AbstractCalculator;
import dk.dma.ais.coverage.data.Cell;
import dk.dma.ais.coverage.data.Source;
import dk.dma.ais.coverage.data.TimeSpan;

public class JsonConverter {

    public static Map<String, JsonSource> toJsonSources(Collection<Source> sources) {
        Map<String, JsonSource> sourcesMap = new HashMap<String, JsonSource>();
        for (Source baseStation : sources) {
            if(!baseStation.getIdentifier().equals(AbstractCalculator.SUPERSOURCE_MMSI)){
                JsonSource s = new JsonSource();
                s.mmsi = baseStation.getIdentifier();
    
                if (baseStation.getName().equals("")) {
                    s.name = baseStation.getIdentifier();
                } else {
                    s.name = baseStation.getName();
                }
                s.type = baseStation.getReceiverType().name();
    
                if (baseStation.getLatitude() != null) {
                    s.lat = baseStation.getLatitude();
                }
    
                if (baseStation.getLongitude() != null) {
                    s.lon = baseStation.getLongitude();
                }
    
                sourcesMap.put(s.mmsi, s);
            }
        }
        return sourcesMap;
    }

    public static ExportCell toJsonCell(Cell cell, Cell superCell, Date starttime, Date endtime) {

        long expected = superCell.getNOofReceivedSignals() + superCell.getNOofMissingSignals();
        // System.out.println(superCell.getNOofMissingSignals());

        ExportCell Jcell = new ExportCell();
        Jcell.lat = cell.getLatitude();
        Jcell.lon = cell.getLongitude();
        Jcell.nrOfMisMes = expected - cell.getNOofReceivedSignals();
        Jcell.nrOfRecMes = cell.getNOofReceivedSignals();

        // if(expected < Jcell.nrOfRecMes){
        // System.out.println("supercell received="+superCell.getNOofReceivedSignals());
        // System.out.println("supercell missng="+superCell.getNOofMissingSignals());
        // System.out.println("expected: "+expected);
        // System.out.println("received: "+Jcell.nrOfRecMes);
        // System.out.println("misses: "+Jcell.nrOfMisMes);
        // System.out.println();
        // }

        return Jcell;
    }

    public static List<JsonTimeSpan> toJsonTimeSpan(List<TimeSpan> timespans) {
        List<JsonTimeSpan> jsonSpans = new ArrayList<JsonTimeSpan>();
        // SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm");

        TimeSpan first = null;
        TimeSpan previous = null;
        for (TimeSpan timeSpan : timespans) {
            JsonTimeSpan jsonspan = new JsonTimeSpan();

            if (first == null) {
                first = timeSpan;
            }

            long timeSinceLastTimeSpan = 0;
            if (previous != null) {
                timeSinceLastTimeSpan = Math.abs(timeSpan.getFirstMessage().getTime() - previous.getLastMessage().getTime()) / 1000 / 60;
            }

            // last is determined by the order of receival, but it is not guaranteed that the tag is actually the last
            // from time, to time, data time, time since last timespan, accumulated time, signals, distinct ships
            jsonspan.fromTime = timeSpan.getFirstMessage().getTime();
            jsonspan.toTime = timeSpan.getLastMessage().getTime();
            jsonspan.spanLength = (int) (Math.abs(timeSpan.getLastMessage().getTime() - timeSpan.getFirstMessage().getTime()) / 1000 / 60);
            if (jsonspan.spanLength == 0) {
                jsonspan.spanLength = 1;
            }
            jsonspan.timeSinceLastSpan = (int) timeSinceLastTimeSpan;
            jsonspan.accumulatedTime = (int) (Math.abs(timeSpan.getLastMessage().getTime() - first.getLastMessage().getTime()) / 1000 / 60);
            jsonspan.signals = timeSpan.getMessageCounterSat();
            jsonspan.distinctShips = timeSpan.getDistinctShipsSat().size();

            jsonSpans.add(jsonspan);
            previous = timeSpan;

        }
        return jsonSpans;
    }
}

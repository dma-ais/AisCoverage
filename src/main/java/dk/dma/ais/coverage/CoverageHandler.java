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
package dk.dma.ais.coverage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.coverage.calculator.AbstractCalculator;
import dk.dma.ais.coverage.calculator.SatCalculator;
import dk.dma.ais.coverage.calculator.TerrestrialCalculator;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.data.Cell;
import dk.dma.ais.coverage.data.CustomMessage;
import dk.dma.ais.coverage.data.ICoverageData;
import dk.dma.ais.coverage.data.OnlyMemoryData;
import dk.dma.ais.coverage.data.QueryParams;
import dk.dma.ais.coverage.data.Ship;
import dk.dma.ais.coverage.data.Source;
import dk.dma.ais.coverage.data.Ship.ShipClass;
import dk.dma.ais.coverage.data.Source.ReceiverType;
import dk.dma.ais.coverage.event.AisEvent;
import dk.dma.ais.coverage.event.EventBroadcaster;
import dk.dma.ais.coverage.export.data.ExportCell;
import dk.dma.ais.coverage.export.data.JSonCoverageMap;
import dk.dma.ais.coverage.export.data.JsonConverter;
import dk.dma.ais.message.AisMessage;
import dk.dma.ais.message.AisMessage4;
import dk.dma.ais.message.AisPositionMessage;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.packet.AisPacketTags;
import dk.dma.ais.packet.AisPacketTags.SourceType;
import dk.dma.ais.proprietary.IProprietarySourceTag;
import dk.dma.enav.model.geometry.Position;

/**
 * Handler for received AisPackets
 */
public class CoverageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageHandler.class);
    
    //list of calculators
    private ArrayList<AbstractCalculator> calculators = new ArrayList<AbstractCalculator>();
    private ICoverageData dataHandler;
    
    public ICoverageData getDataHandler() {
        return dataHandler;
    }

    public void setDataHandler(ICoverageData dataHandler) {
        this.dataHandler = dataHandler;
    }

    //A doublet filtered message buffer, where a custom message will include a list of all sources
    private LinkedHashMap<String, CustomMessage> doubletBuffer = new LinkedHashMap<String, CustomMessage>() {
        private static final long serialVersionUID = 1L;

        @Override
        protected boolean removeEldestEntry(Map.Entry<String, CustomMessage> eldest) {
            process(eldest.getValue());
            return this.size() > 10000;
        }
    };
      
    //Fields used for debugging purposes
    private int unfiltCount;
    private long biggestDelay;
    private int weird;
    private int delayedMoreThanTen;
    private int delayedLessThanTen;

    private AisCoverageConfiguration conf;

    public CoverageHandler(AisCoverageConfiguration conf) {
        this.conf=conf;
        Helper.conf=conf;
        
        //Creating up data handler
        dataHandler = new OnlyMemoryData();
        LOG.info("coverage calculators set up with memory only data handling");
        
        //creating calculators
        calculators.add(new TerrestrialCalculator(false));
        calculators.add(new SatCalculator());
        
        for (AbstractCalculator calc : calculators) {
            calc.setDataHandler(dataHandler);
        }

        // Logging grid granularity
        LOG.info("grid granularity initiated with lat: " + conf.getLatSize() + " and lon: " + conf.getLonSize()); 
        
        // One could set grid granularity based on meter scale and a latitude position like this
            // Helper.setLatLonSize(meters, latitude);
        
        if (conf.getVerbosityLevel() > 0) {
            verboseDebug();
        }

    }

    public void receiveUnfiltered(AisPacket packet) {
        unfiltCount++;
        //extracts packet information
        preProcess(packet);
    }
    
    private void process(CustomMessage m){
        for (AbstractCalculator calc : calculators) {
            calc.calculate(m);
        }
    }
    
    private void preProcess(AisPacket packet){
        AisMessage aisMessage = packet.tryGetAisMessage();
        if (aisMessage == null) {
            return;
        }
        
        String baseId = "default"; //the default id
        ReceiverType receiverType = ReceiverType.NOTDEFINED;
        Date timestamp = null;
        ShipClass shipClass = null;
        AisPositionMessage posMessage;
        SourceType sourceType = SourceType.TERRESTRIAL;

        // Get source tag properties
        IProprietarySourceTag sourceTag = aisMessage.getSourceTag();
        AisPacketTags packetTags = packet.getTags();
        
        //Determine if source is sat or terrestrial
        if(packetTags != null && packetTags.getSourceType() == SourceType.SATELLITE){
            sourceType = SourceType.SATELLITE;
        }

        //Determine source mmsi and receivertype    
        if (sourceTag != null) {
            timestamp = sourceTag.getTimestamp();
            if(sourceTag.getBaseMmsi() != null){ //it's a base station
                baseId=sourceTag.getBaseMmsi()+"";   
                receiverType = ReceiverType.BASESTATION;
            }else if(!sourceTag.getRegion().equals("")){ //It's a region
                baseId=sourceTag.getRegion();
                receiverType=ReceiverType.REGION;
            }
        }


        // If time stamp is not present, we add one
        //TODO this only makes sense for real-time data. We should check if it is real-time
        if (timestamp == null) {
            timestamp = new Date();
        }
        
        if(Helper.firstMessage == null){
            Helper.firstMessage = Helper.getFloorDate(timestamp);
        }

        // It's a base station position message
        if (aisMessage instanceof AisMessage4) {
            Source b = dataHandler.getSource(baseId);
            AisMessage4 m = (AisMessage4) aisMessage;
            if (conf.getSourceNameMap() != null && conf.getSourceNameMap().containsKey(baseId)) {
                //user already provided name and location for this source
            } else if (b != null) {
                Position pos = m.getPos().getGeoLocation();
                if (pos != null ) {
                    b.setLatitude(m.getPos().getGeoLocation().getLatitude());
                    b.setLongitude(m.getPos().getGeoLocation().getLongitude());
                }
            }
            return;
        }


        // Handle position messages. If it's not a position message
        // the calculators can't use them
        if (aisMessage instanceof AisPositionMessage) {
            posMessage = (AisPositionMessage) aisMessage;
        } else {
            return;
        }

        // Check if position is valid
        if (!posMessage.isPositionValid()) {
            return;
        }
        
        // Extract Base station
        Source source = dataHandler.getSource(baseId);
        if (source == null) {
            //source wasn't found, we need to create
            source = dataHandler.createSource(baseId);
            source.setReceiverType(receiverType);
            
            //Let's check if user provided a name and position for this source
            if (conf.getSourceNameMap() != null && conf.getSourceNameMap().containsKey(baseId)) {
                source.setLatitude(conf.getSourceNameMap().get(baseId).getLatitude());
                source.setLongitude(conf.getSourceNameMap().get(baseId).getLongitude());   
                source.setName(conf.getSourceNameMap().get(baseId).getName());
            }
        }
        

        //Extract ship
        Ship ship = dataHandler.getShip(aisMessage.getUserId());
        if (ship == null) {
            ship = dataHandler.createShip(aisMessage.getUserId(), shipClass);
        }
        
        CustomMessage newMessage = new CustomMessage();
        newMessage.setCog((double) posMessage.getCog() / 10);
        newMessage.setSog((double) posMessage.getSog() / 10);
        newMessage.setLatitude(posMessage.getPos().getGeoLocation().getLatitude());
        newMessage.setLongitude(posMessage.getPos().getGeoLocation().getLongitude());
        newMessage.setTimestamp(timestamp);
        newMessage.addSourceMMSI(baseId);
        newMessage.setShipMMSI(aisMessage.getUserId());
        newMessage.setSourceType(sourceType);
        
        String key = newMessage.getKey();

        // if message exist in queue return true, otherwise false.
        CustomMessage existing = doubletBuffer.get(key);
        if (existing == null) {
            doubletBuffer.put(key, newMessage);
        }else{
            existing.addSourceMMSI(baseId);
        }
    }

    //TODO consider moving this method to a calculator
    public JSonCoverageMap getTerrestrialCoverage(double latStart, double lonStart, double latEnd, double lonEnd,
            Set<String> sources, int multiplicationFactor, Date starttime, Date endtime) {

        
        JSonCoverageMap map = new JSonCoverageMap();
        map.latSize = conf.getLatSize() * multiplicationFactor;
        map.lonSize = conf.getLonSize() * multiplicationFactor;

        HashMap<String, ExportCell> JsonCells = new HashMap<String, ExportCell>();

        QueryParams params = new QueryParams();
        params.latStart = latStart;
        params.latEnd = latEnd;
        params.lonStart = lonStart;
        params.lonEnd = lonEnd;
        params.sources = sources;
        params.multiplicationFactor = multiplicationFactor;
        params.startDate = starttime;
        params.endDate = endtime;

        List<Cell> celllist = calculators.get(0).getDataHandler().getCells(params);
        Set<String> superSourceIsHere = new HashSet<String>();
        superSourceIsHere.add(AbstractCalculator.SUPERSOURCE_MMSI);
        params.sources = superSourceIsHere;
        List<Cell> celllistSuper = calculators.get(0).getDataHandler().getCells(params);
        Map<String, Cell> superMap = new HashMap<String, Cell>();
        for (Cell cell : celllistSuper) {
            if (cell.getNOofReceivedSignals() > 0) {
                superMap.put(cell.getId(), cell);
            }
        }

        if (!celllist.isEmpty()) {
            map.latSize = conf.getLatSize() * multiplicationFactor;
        }

        for (Cell cell : celllist) {
            Cell superCell = superMap.get(cell.getId());
            if (superCell == null) {

            } else {
                    ExportCell existing = JsonCells.get(cell.getId());
                    ExportCell theCell = JsonConverter.toJsonCell(cell, superCell, starttime, endtime);
                    if (existing == null || theCell.getCoverage() > existing.getCoverage()) {
                        JsonCells.put(cell.getId(), theCell);
                    }
                
            }
        }

        map.cells = JsonCells;

        return map;
    }
    
    public void verboseDebug(){
        final Date then = new Date();
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(10000);
                    } catch (Exception e) {

                    }

                    Date now = new Date();
                    // System.out.println((((now.getTime()-then.getTime())/1000)));
                    LOG.info("messages per second: " + (unfiltCount / (((now.getTime() - then.getTime()) / 1000))));
                    LOG.info("messages processed: " + unfiltCount);
//                    LOG.info("biggest delay in minutes: " + biggestDelay / 1000 / 60);
//                    LOG.info("weird stamps: " + weird);
//                    LOG.info("delayed more than ten min: " + delayedMoreThanTen);
//                    LOG.info("delayed less than ten min: " + delayedLessThanTen);
//                    long numberofcells = 0;
//                    long uniquecells = 0;
//                    long uniqueships = 0;
//                    long uniqueShipHours = 0;
//                    for (Ship ss : dataHandler.getShips()) {
//                        uniqueShipHours += ss.getHours().size();
//                    }
//                    for (String sourcename : dataHandler.getSourceNames()) {
//                        
//                    }
//
//                    
//                    LOG.info("Unique cells: " + dataHandler.getSource("supersource").getGrid().size());
//                    LOG.info("total cell timespans: " + numberofcells);
//                    LOG.info("Unique ships: " + dataHandler.getShips().size());
//                    LOG.info("Unique ship hours: " + uniqueShipHours);
//                    LOG.info(""+calculators.get(0).getDataHandler().getSources().size());
//                    LOG.info("");
                }

            }
        });

        t.start();
    }

    public SatCalculator getSatCalc() {
        return (SatCalculator) calculators.get(1);
    }

}

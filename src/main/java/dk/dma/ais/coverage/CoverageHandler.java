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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.coverage.calculator.TerrestrialDistributor;
import dk.dma.ais.coverage.calculator.SatCalculator;
import dk.dma.ais.coverage.calculator.TerrestrialSuperSourceCalculator;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.data.Cell;
import dk.dma.ais.coverage.data.CustomMessage;
import dk.dma.ais.coverage.data.ICoverageData;
import dk.dma.ais.coverage.data.OnlyMemoryData;
import dk.dma.ais.coverage.data.QueryParams;
import dk.dma.ais.coverage.data.Ship;
import dk.dma.ais.coverage.data.Source;
import dk.dma.ais.coverage.data.SuperShip;
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
import dk.dma.ais.packet.AisPacketTags.SourceType;
import dk.dma.ais.proprietary.IProprietarySourceTag;

/**
 * Handler for received AisPackets
 */
public class CoverageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageHandler.class);
    
    //Calculators used for processing AIS messages received by terrestrial receivers.
    private TerrestrialSuperSourceCalculator superCalc;
    private TerrestrialDistributor distributeOnlyCalc;
    
    //Used for processing AIS messages received by satellite receivers.
    private SatCalculator satCalc;
      
    //Fields used for debugging purposes
    private int unfiltCount;
    private long biggestDelay;
    private int weird;
    private int delayedMoreThanTen;
    private int delayedLessThanTen;

    public CoverageHandler(AisCoverageConfiguration conf) {

        //Storing source information provided by the user
        Helper.sourceInfo = conf.getSourceNameMap();
        
        //setting up the calculators
        superCalc = new TerrestrialSuperSourceCalculator(false);
        distributeOnlyCalc = new TerrestrialDistributor();
        superCalc.addListener(distributeOnlyCalc);
        satCalc = new SatCalculator();

        // Setting data handlers
        if (conf.getDatabaseConfiguration().getType().toLowerCase().equals("memoryonly")) {
            ICoverageData dataH = new OnlyMemoryData();
            distributeOnlyCalc.setDataHandler(new OnlyMemoryData());
            superCalc.setDataHandler(dataH);
            satCalc.setDataHandler(dataH);
            LOG.info("coverage calculators set up with memory only data handling");
        }else{
            //it is still an issue how to store data on disk.
        }

        // setting grid granularity
        Helper.latSize = conf.getLatSize();
        Helper.lonSize = conf.getLonSize();
        LOG.info("grid granularity initiated with lat: " + conf.getLatSize() + " and lon: " + conf.getLonSize()); 
        
        // One could also set grid granularity based on meter scale and a latitude position like this
            // Helper.setLatLonSize(meters, latitude);
        
        if (Helper.debugVerbosityLevel > 0) {
            verboseDebug();
        }

    }

    public void receiveUnfiltered(AisPacket packet) {
        unfiltCount++;
        superCalc.processMessage(packet, "supersource");
        distributeOnlyCalc.processMessage(packet, "1");
        satCalc.processMessage(packet, "sat");
    }

    //TODO consider moving this method to a calculator
    public JSonCoverageMap getTerrestrialCoverage(double latStart, double lonStart, double latEnd, double lonEnd,
            Map<String, Boolean> sources, int multiplicationFactor, Date starttime, Date endtime) {

        
        JSonCoverageMap map = new JSonCoverageMap();
        map.latSize = Helper.latSize * multiplicationFactor;
        map.lonSize = Helper.lonSize * multiplicationFactor;

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

        List<Cell> celllist = distributeOnlyCalc.getDataHandler().getCells(params);
        HashMap<String, Boolean> superSourceIsHere = new HashMap<String, Boolean>();
        superSourceIsHere.put("supersource", true);
        params.sources = superSourceIsHere;
        List<Cell> celllistSuper = superCalc.getDataHandler().getCells(params);
        Map<String, Cell> superMap = new HashMap<String, Cell>();
        for (Cell cell : celllistSuper) {
            if (cell.getNOofReceivedSignals() > 0) {
                superMap.put(cell.getId(), cell);
            }
        }

        if (!celllist.isEmpty()) {
            map.latSize = Helper.latSize * multiplicationFactor;
        }

        for (Cell cell : celllist) {
            Cell superCell = superMap.get(cell.getId());
            if (superCell == null) {

            } else {
                ExportCell existing = JsonCells.get(cell.getId());
                ExportCell theCell = JsonConverter.toJsonCell(cell, superCell, starttime, endtime);
                if (existing == null) {
                    existing = JsonCells.put(cell.getId(), JsonConverter.toJsonCell(cell, superCell, starttime, endtime));
                } else if (theCell.getCoverage() > existing.getCoverage()) {
                    JsonCells.put(cell.getId(), theCell);
                }
            }
        }

        map.cells = JsonCells;

        return map;
    }

    public TerrestrialDistributor getDistributeCalc() {
        return distributeOnlyCalc;
    }

    public TerrestrialSuperSourceCalculator getSupersourceCalc() {
        return superCalc;
    }

    public SatCalculator getSatCalc() {
        return satCalc;
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
                    LOG.info("biggest delay in minutes: " + biggestDelay / 1000 / 60);
                    LOG.info("weird stamps: " + weird);
                    LOG.info("delayed more than ten min: " + delayedMoreThanTen);
                    LOG.info("delayed less than ten min: " + delayedLessThanTen);
                    long numberofcells = 0;
                    long uniquecells = 0;
                    long uniqueships = 0;
                    long uniqueShipHours = 0;
                    for (SuperShip ss : satCalc.getSuperships().values()) {
                        uniqueShipHours += ss.getHours().size();
                    }
                    // for (Integer iterable_element : satCalc.getSuperships().keySet()) {
                    // System.out.println(iterable_element);
                    // }
                    for (Source s : satCalc.getDataHandler().getSources()) {
                        numberofcells += s.getGrid().size();
                        uniquecells += s.getGrid().size();
                        uniqueships += s.getShips().size();
                    }
                    for (Source s : distributeOnlyCalc.getDataHandler().getSources()) {
                        numberofcells += s.getGrid().size();
                    }
                    LOG.info("total cells: " + numberofcells);
                    LOG.info("Unique cells: " + uniquecells);
                    LOG.info("Unique ships: " + uniqueships);
                    LOG.info("Unique ship hours: " + uniqueShipHours);
                    LOG.info(""+satCalc.getDataHandler().getSources().size());
                    LOG.info("");
                }

            }
        });

        t.start();
    }

}

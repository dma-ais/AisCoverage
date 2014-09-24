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
package dk.dma.ais.coverage;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.coverage.calculator.AbstractCalculator;
import dk.dma.ais.coverage.calculator.SatCalculator;
import dk.dma.ais.coverage.calculator.TerrestrialCalculator;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.data.CustomMessage;
import dk.dma.ais.coverage.data.ICoverageData;
import dk.dma.ais.coverage.data.OnlyMemoryData;
import dk.dma.ais.coverage.data.Ship;
import dk.dma.ais.packet.AisPacket;

/**
 * Handler for received AisPackets
 */
public class CoverageHandler {

    private static final Logger LOG = LoggerFactory.getLogger(CoverageHandler.class);
    
    //list of calculators
    private ArrayList<AbstractCalculator> calculators = new ArrayList<AbstractCalculator>();
    public ArrayList<AbstractCalculator> getCalculators() {
        return calculators;
    }

    public void setCalculators(ArrayList<AbstractCalculator> calculators) {
        this.calculators = calculators;
    }

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
        
        //extract relevant information from packet
        CustomMessage message = dataHandler.packetToCustomMessage(packet);
        if(message == null){
            return;
        }
        
        String key = message.getKey();
        
        //Add to doublet buffer.
        CustomMessage existing = doubletBuffer.get(key);
        if (existing == null) {
            doubletBuffer.put(key, message);
        }else{
            existing.addSourceMMSI(message.getSourceList().iterator().next());
        }

    }
    
    private void process(CustomMessage m){
        for (AbstractCalculator calc : calculators) {
            calc.calculate(m);
        }
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
//                    long uniquecells = 0;
//                    long uniqueships = 0;
                    long uniqueShipHours = 0;
                    for (Ship ss : dataHandler.getShips()) {
                        uniqueShipHours += ss.getHours().size();
                    }
//                    for (String sourcename : dataHandler.getSourceNames()) {
//                        
//                    }

                    
                    LOG.info("Unique cells: " + dataHandler.getSource("supersource").getGrid().size());
                    LOG.info("total cell timespans: " + numberofcells);
                    LOG.info("Unique ships: " + dataHandler.getShips().size());
                    LOG.info("Unique ship hours: " + uniqueShipHours);
                    LOG.info(""+calculators.get(0).getDataHandler().getSources().size());
                    LOG.info("");
                }

            }
        });

        t.start();
    }

    public SatCalculator getSatCalc() {
        return (SatCalculator) calculators.get(1);
    }

}

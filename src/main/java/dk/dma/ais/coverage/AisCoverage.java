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

import java.util.function.Consumer;

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.ais.coverage.web.WebServer;
import dk.dma.ais.packet.AisPacket;
import dk.dma.ais.reader.AisReader;


/**
 * AIS coverage analyzer
 */
@ThreadSafe
public final class AisCoverage {

    private static final Logger LOG = LoggerFactory.getLogger(AisCoverage.class);

    @GuardedBy("AisCoverage")
    private static AisCoverage instance;

    private final AisCoverageConfiguration conf;
    private final CoverageHandler handler;
    private final AisBus aisBus;
    private final WebServer webServer;
    private AisReader aisReader;

    private AisCoverage(AisCoverageConfiguration conf) {
        this.conf = conf;

        // Create handler
        handler = new CoverageHandler(conf);

        // Create AisBus
        aisBus = conf.getAisbusConfiguration().getInstance();
        

        // Create web server
        if (conf.getServerConfiguration() != null) {
            webServer = new WebServer(conf.getServerConfiguration());
        } else {
            webServer = null;
        }

        final DistributerConsumer unfilteredConsumer = new DistributerConsumer(true);
        unfilteredConsumer.init();
        // Delegate unfiltered packets to handler
        unfilteredConsumer.getConsumers().add(new Consumer<AisPacket>() {
            @Override
            public void accept(AisPacket packet) {
                handler.receiveUnfiltered(packet);
            }
        });                
        aisBus.registerConsumer(unfilteredConsumer);
    }

    public void start() {
        // Start aisBus
        if (aisReader == null) {
            aisBus.start();
            aisBus.startConsumers();
            aisBus.startProviders();
            LOG.info("aisbus startet");
        }
        // Start web server
        if (webServer != null) {
            try {
                webServer.start();
                LOG.info("webserver startet");
            } catch (Exception e) {
                LOG.error("Failed to start web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        
     // Start web server
        if (webServer != null) {
            try {
                webServer.stop();
                LOG.info("webserver stopped");
            } catch (Exception e) {
                LOG.error("Failed to stop web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Stop AisBus
        aisBus.cancel();
        LOG.info("aisbus stopped");
    }

    public AisCoverageConfiguration getConf() {
        return conf;
    }

    public CoverageHandler getHandler() {
        return handler;
    }

    public static synchronized AisCoverage create(AisCoverageConfiguration conf) {
        instance = new AisCoverage(conf);
        return instance;
    }

    public static synchronized AisCoverage get() {
        return instance;
    }

}

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

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.Parameter;
import com.google.inject.Injector;

import dk.dma.ais.coverage.configuration.AisCoverageConfiguration;
import dk.dma.commons.app.AbstractDaemon;

/**
 * AIS coverage analyzer daemon
 */
public class AisCoverageDaemon extends AbstractDaemon {

    private static final Logger LOG = LoggerFactory.getLogger(AisCoverageDaemon.class);

    @Parameter(names = "-file", description = "AisCoverage configuration file")
    String confFile = "coverage-sample.xml";

    private AisCoverage aisCoverage;

    @Override
    protected void runDaemon(Injector injector) throws Exception {
        LOG.info("Starting AisCoverageDaemon with configuration: " + confFile);

        // Get configuration
        AisCoverageConfiguration conf;
        try {
            conf = AisCoverageConfiguration.load(confFile);
        } catch (FileNotFoundException e) {
            LOG.error(e.getMessage());
            return;
        }

        if (!conf.getDatabaseConfiguration().getType().toLowerCase().equals("memoryonly")
                && !conf.getDatabaseConfiguration().getType().toLowerCase().equals("mongodb")) {
            LOG.error("Unknown database type");
            return;
        }

        // Create and start
        aisCoverage = AisCoverage.create(conf);

        aisCoverage.start();
    }

    @Override
    public void shutdown() {
        LOG.info("Shutting down");
        if (aisCoverage != null) {
            aisCoverage.stop();
        }
        super.shutdown();
    }

    public static void main(String[] args) throws Exception {
        // Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
        // @Override
        // public void uncaughtException(Thread t, Throwable e) {
        // LOG.error("Uncaught exception in thread " + t.getClass().getCanonicalName() + ": " + e.getMessage(), t);
        // System.exit(-1);
        // }
        // });
        new AisCoverageDaemon().execute(args);
    }

}

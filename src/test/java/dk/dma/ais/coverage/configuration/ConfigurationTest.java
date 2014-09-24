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
package dk.dma.ais.coverage.configuration;

import java.io.FileNotFoundException;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import dk.dma.ais.bus.AisBus;
import dk.dma.ais.bus.consumer.DistributerConsumer;
import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.configuration.bus.consumer.DistributerConsumerConfiguration;
import dk.dma.ais.configuration.bus.provider.TcpClientProviderConfiguration;
import dk.dma.ais.configuration.filter.DuplicateFilterConfiguration;

public class ConfigurationTest {

    @Test
    public void makeConfiguration() throws FileNotFoundException, JAXBException {
        String filename = "src/main/resources/coverage-test.xml";
        AisCoverageConfiguration conf = new AisCoverageConfiguration();
        AisBusConfiguration aisBusConf = new AisBusConfiguration();

        // Provider
        TcpClientProviderConfiguration reader = new TcpClientProviderConfiguration();
        reader.getHostPort().add("ais163.sealan.dk:65262");
        aisBusConf.getProviders().add(reader);

        // Unfiltered consumer
        DistributerConsumerConfiguration unfilteredDist = new DistributerConsumerConfiguration();
        unfilteredDist.setName("UNFILTERED");
        aisBusConf.getConsumers().add(unfilteredDist);

        // Filtered consumer
        DistributerConsumerConfiguration filteredDist = new DistributerConsumerConfiguration();
        filteredDist.setName("FILTERED");
        DuplicateFilterConfiguration duplicateFilter = new DuplicateFilterConfiguration();
        filteredDist.getFilters().add(duplicateFilter);
        aisBusConf.getConsumers().add(filteredDist);
        conf.setAisbusConfiguration(aisBusConf);

        conf.setLatSize(1.5);
        conf.setLonSize(1.5);
        DatabaseConfiguration dbConf = new DatabaseConfiguration();
        conf.setDatabaseConfiguration(dbConf);
        // dbConf.set
        // dbConf.setName("MongoDB");
        // dbConf.setAddr("localhost");
        // dbConf.setPort(9999);
        // conf.setDatabase("MemoryOnly");

        AisCoverageConfiguration.save(filename, conf);

        conf = AisCoverageConfiguration.load(filename);
        AisBus aisBus = conf.getAisbusConfiguration().getInstance();
        DistributerConsumer filtered = (DistributerConsumer) aisBus.getConsumer("FILTERED");
        Assert.assertNotNull(filtered);
        DistributerConsumer unfiltered = (DistributerConsumer) aisBus.getConsumer("UNFILTERED");
        Assert.assertNotNull(unfiltered);
    }

}

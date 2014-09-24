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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import dk.dma.ais.configuration.bus.AisBusConfiguration;
import dk.dma.ais.coverage.data.Source_UserProvided;
import dk.dma.ais.coverage.web.WebServerConfiguration;

/**
 * Class to represent AIS coverage configuration. To be marshalled and unmarshalled by JAXB.
 */
@XmlRootElement
public class AisCoverageConfiguration {

    private AisBusConfiguration aisbusConfiguration;
    private WebServerConfiguration serverConfiguration;
    private double latSize = 0.0225225225;
    private double lonSize = 0.0386812541;
    private int verbosityLevel;
    private DatabaseConfiguration dbConf = new DatabaseConfiguration();
    private Map<String, Source_UserProvided> sourcenames = new HashMap<String, Source_UserProvided>();

    public Map<String, Source_UserProvided> getSourceNameMap() {
        return sourcenames;
    }

    public void setSourceNameMap(Map<String, Source_UserProvided> map) {
        sourcenames = map;
    }

    public DatabaseConfiguration getDatabaseConfiguration() {
        return dbConf;
    }

    public void setDatabaseConfiguration(DatabaseConfiguration dbConf) {
        this.dbConf = dbConf;
    }

    public AisCoverageConfiguration() {

    }

    @XmlElement(name = "aisbus")
    public AisBusConfiguration getAisbusConfiguration() {
        return aisbusConfiguration;
    }

    public void setAisbusConfiguration(AisBusConfiguration aisbusConfiguration) {
        this.aisbusConfiguration = aisbusConfiguration;
    }

    public WebServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    public void setServerConfiguration(WebServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public void setLatSize(double latSize) {
        this.latSize = latSize;
    }

    public void setLonSize(double lonSize) {
        this.lonSize = lonSize;
    }

    public double getLatSize() {
        return this.latSize;
    }

    public double getLonSize() {
        return this.lonSize;
    }

    public static void save(String filename, AisCoverageConfiguration conf) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisCoverageConfiguration.class);
        Marshaller m = context.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.marshal(conf, new FileOutputStream(new File(filename)));
    }

    public static AisCoverageConfiguration load(String filename) throws JAXBException, FileNotFoundException {
        JAXBContext context = JAXBContext.newInstance(AisCoverageConfiguration.class);
        Unmarshaller um = context.createUnmarshaller();
        return (AisCoverageConfiguration) um.unmarshal(new FileInputStream(new File(filename)));
    }

    public int getVerbosityLevel() {
        return verbosityLevel;
    }

    public void setVerbosityLevel(int verbosityLevel) {
        this.verbosityLevel = verbosityLevel;
    }

}

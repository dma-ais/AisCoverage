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
package dk.dma.ais.coverage.export.generators;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.coverage.data.Cell;
import dk.dma.ais.coverage.data.Source;

public class CSVGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(KMLGenerator.class);

    // public static void generateKML(CoverageCalculator calc, String path) {
    public static void generateCSV(Collection<Source> grids, double latSize, double lonSize, int multiplicity,
            HttpServletResponse response) {

        LOG.info("startet csv generation");

        HttpServletResponse out = response;

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();

        String fileName = "aiscoverage-" + dateFormat.format(date) + "_latSize " + latSize + "_lonSize " + lonSize
                + "multiplicationfactor" + multiplicity + ".csv";
        // out.setContentType("application/vnd.google-earth.kml+xml");
        out.setContentType("text/csv");
        out.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        writeLine("latstart, longstart, latend, longend, received, missing, coverage percentage", out);

        for (Source grid : grids) {
            generateGrid(grid.getIdentifier(), grid.getGrid().values(), out, latSize * multiplicity, lonSize * multiplicity);
        }

        // TODO check hvad det er der giver en aw snap internal error fejl efter kml generate er kørt
        try {
            out.getOutputStream().close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
        LOG.info("Finished csv generation");
    }

    private static void writeLine(String line, HttpServletResponse out) {
        try {
            out.getOutputStream().write((line + "\n").getBytes());
            out.getOutputStream().flush();
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private static void generateGrid(String bsMmsi, Collection<Cell> cells, HttpServletResponse out, double latSize, double lonSize) {

        for (Cell cell : cells) {

            // We ignore cells, where average number of messages, is below 10 per ship
            // Maybe there is a bug in AISMessage system, that assign some messages to wrong Base Stations
            // Bug found and fixed
            // if (cell.NOofReceivedSignals / cell.ships.size() > 10) {

            writeLine(
                    cell.getLatitude() + "," + cell.getLongitude() + "," + (cell.getLatitude() + latSize) + ","
                            + (cell.getLongitude() + lonSize) + "," + cell.getNOofReceivedSignals() + ","
                            + cell.getNOofMissingSignals() + "," + (cell.getCoverage() * 100), out);

            // }

        }
    }

}

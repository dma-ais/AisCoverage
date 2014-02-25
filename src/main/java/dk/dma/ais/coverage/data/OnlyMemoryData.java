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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.coverage.Helper;
import dk.dma.ais.coverage.calculator.AbstractCalculator;
import dk.dma.ais.coverage.data.Ship.ShipClass;

public class OnlyMemoryData implements ICoverageData {

    protected SourceHandler gridHandler = new SourceHandler();
    private Map<Integer, Ship> ships = new ConcurrentHashMap<Integer, Ship>();

    @Override
    public Ship getShip(int shipMmsi) {
        return ships.get(shipMmsi);
    }

    @Override
    public void updateShip(Ship ship) {

    }

    @Override
    public Cell getCell(String sourceMmsi, double lat, double lon) {
        return gridHandler.getSource(sourceMmsi).getCell(lat, lon);
    }

    @Override
    public void updateCell(Cell c) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public Collection<Ship> getShips(){
        return ships.values();
    }

    private List<Cell> getCells() {
        List<Cell> cells = new ArrayList<Cell>();
        Collection<Source> basestations = gridHandler.getSources().values();
        for (Source basestation : basestations) {
            if (basestation.isVisible()) {

                // For each cell
                Collection<Cell> bscells = basestation.getGrid().values();
                for (Cell cell : bscells) {
                    cells.add(cell);
                }
            }

        }
        return cells;
    }

    @Override
    public Ship createShip(int shipMmsi, ShipClass shipClass) {
        Ship ship = new Ship(shipMmsi, shipClass);
        ships.put(shipMmsi, ship);
        return ship;
    }

    @Override
    public Cell createCell(String sourceMmsi, double lat, double lon) {
        return gridHandler.getSource(sourceMmsi).createCell(lat, lon);
    }

    @Override
    public Source getSource(String sourceId) {
        return gridHandler.getSources().get(sourceId);
    }

    @Override
    public Source createSource(String sourceId) {
        return gridHandler.createGrid(sourceId);
    }

    @Override
    public Collection<Source> getSources() {
        return gridHandler.getSources().values();
    }

    private List<Cell> getCells(double latStart, double lonStart, double latEnd, double lonEnd, Set<String> sources,
            int multiplicationFactor, Date starttime, Date endtime) {

        List<Cell> cells = new ArrayList<Cell>();

        
        for (String sourcename : sources) {
            
            //Make new cells that matches the multiplication factor
            Source source = gridHandler.getSources().get(sourcename);
            if(source != null){                
                Source cellMultiplicationSource = new Source(source.getIdentifier());
                cellMultiplicationSource.setMultiplicationFactor(multiplicationFactor);
                // Make 
                Collection<Cell> bscells = source.getGrid().values();
                for (Cell cell : bscells) {

                    if (Helper.isInsideBox(cell, latStart, lonStart, latEnd, lonEnd)) {
                        Cell tempCell = cellMultiplicationSource.getCell(cell.getLatitude(), cell.getLongitude());
                        if (tempCell == null) {
                            tempCell = cellMultiplicationSource.createCell(cell.getLatitude(), cell.getLongitude());
                        }
                        tempCell.addNOofMissingSignals((int) cell.getNOofMissingSignals(starttime, endtime));
                        tempCell.addReceivedSignals(cell.getNOofReceivedSignals(starttime, endtime));
                    }

                }
                
                //add cells for particular source to cell-list.
                for (Cell cell : cellMultiplicationSource.getGrid().values()) {
                    if(cell.getNOofReceivedSignals() > 0){
                        cells.add(cell);
                    }
                }
            }
        }
        return cells;
    }

    @Override
    public List<Cell> getCells(QueryParams params) {
        if (params == null) {
            return getCells();
        }
        return getCells(params.latStart, params.lonStart, params.latEnd, params.lonEnd, params.sources,
                params.multiplicationFactor, params.startDate, params.endDate);

    }

    @Override
    public void incrementReceivedSignals(String sourceMmsi, double lat, double lon, Date timestamp) {
        Cell cell = getCell(sourceMmsi, lat, lon);
        if (cell == null) {
            cell = createCell(sourceMmsi, lat, lon);
        }
        Date id = Helper.getFloorDate(timestamp);
        TimeSpan ts = cell.getFixedWidthSpans().get(id.getTime());
        if (ts == null) {
            ts = new TimeSpan(id);
            ts.setLastMessage(Helper.getCeilDate(timestamp));
            cell.getFixedWidthSpans().put(id.getTime(), ts);
        }
        ts.setMessageCounterTerrestrial(ts.getMessageCounterTerrestrial() + 1);

    }

    @Override
    public void incrementMissingSignals(String sourceMmsi, double lat, double lon, Date timestamp) {

        Cell cell = getCell(sourceMmsi, lat, lon);
        if (cell == null) {
            cell = createCell(sourceMmsi, lat, lon);
        }
        Date id = Helper.getFloorDate(timestamp);
        TimeSpan ts = cell.getFixedWidthSpans().get(id.getTime());
        if (ts == null) {
            ts = new TimeSpan(id);
            ts.setLastMessage(Helper.getCeilDate(timestamp));
            cell.getFixedWidthSpans().put(id.getTime(), ts);
        }
        ts.incrementMissingSignals();

    }

}

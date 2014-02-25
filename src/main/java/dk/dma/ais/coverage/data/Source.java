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

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dk.dma.ais.coverage.Helper;
import dk.dma.ais.coverage.data.Ship.ShipClass;

public class Source implements Serializable {

    private static final long serialVersionUID = 1L;
    private Map<String, Cell> grid = new ConcurrentHashMap<String, Cell>();
    private String name = "Unknown";
    private String identifier;
    private double latitude;
    private double longitude;
    private long messageCount;
    private boolean isVisible = true;
    private ReceiverType receiverType = ReceiverType.NOTDEFINED;
    private int multiplicationFactor = 1;

    public int getMultiplicationFactor() {
        return multiplicationFactor;
    }

    public void setMultiplicationFactor(int multiplicationFactor) {
        this.multiplicationFactor = multiplicationFactor;
    }

    public enum ReceiverType {
        BASESTATION, REGION, NOTDEFINED
    }

    public Source(String identifier) {
        this.identifier = identifier;
    }

    public Source() {

    }

    public void incrementMessageCount() {
        messageCount++;
    }

    public ReceiverType getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(ReceiverType receiverType) {
        this.receiverType = receiverType;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Cell getCell(double latitude, double longitude) {
        return grid.get(Helper.getCellId(latitude, longitude, multiplicationFactor));
    }

    public Cell getTempCell(double latitude, double longitude, int multiplicationFactorTemp) {
        return grid.get(Helper.getCellId(latitude, longitude, multiplicationFactorTemp));
    }

    public Cell createCell(double latitude, double longitude) {
        String id = Helper.getCellId(latitude, longitude, multiplicationFactor);
        double lat = Helper.roundLat(latitude, multiplicationFactor);
        double lon = Helper.roundLon(longitude, multiplicationFactor);
        Cell cell = new Cell(this, lat, lon, id);
        grid.put(cell.getId(), cell);

        return cell;
    }

    public Cell createTempCell(double latitude, double longitude, int multiplicationFactorTemp) {
        String id = Helper.getCellId(latitude, longitude, multiplicationFactorTemp);
        double lat = Helper.roundLat(latitude, multiplicationFactorTemp);
        double lon = Helper.roundLon(longitude, multiplicationFactorTemp);
        Cell cell = new Cell(this, lat, lon, id);
        grid.put(cell.getId(), cell);

        return cell;
    }

    public Map<String, Cell> getGrid() {
        return grid;
    }

    public void setGrid(ConcurrentHashMap<String, Cell> grid) {
        this.grid = grid;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public long getMessageCount() {
        return messageCount;
    }
}

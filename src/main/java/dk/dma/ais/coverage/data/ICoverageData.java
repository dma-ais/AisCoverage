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
package dk.dma.ais.coverage.data;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import dk.dma.ais.coverage.data.Ship.ShipClass;
import dk.dma.ais.packet.AisPacket;

public interface ICoverageData {

    CustomMessage packetToCustomMessage(AisPacket packet);
    Ship createShip(int shipMmsi, ShipClass shipClass);
    Ship getShip(int shipMmsi);
    Collection<Ship> getShips();
    void updateShip(Ship ship);
    Cell createCell(String sourceMmsi, double lat, double lon);
    Cell getCell(String sourceMmsi, double lat, double lon);
    void updateCell(Cell c);
    List<Cell> getCells(QueryParams params);
    Source getSource(String sourceId);
    Source createSource(String sourceId);
    Collection<Source> getSources();
    void incrementReceivedSignals(String sourceMmsi, double lat, double lon, Date timestamp);
    void incrementMissingSignals(String sourceMmsi, double lat, double lon, Date timestamp);

}

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
package dk.dma.ais.coverage.export.data;

import java.io.Serializable;

public class ExportCell implements Serializable {
    private static final long serialVersionUID = 1L;

    public double lat;
    public double lon;
    public long nrOfRecMes;
    public long nrOfMisMes;
    public String sourceMmsi;

    public double getCoverage() {
        return (double) nrOfRecMes / (double) (nrOfMisMes + nrOfRecMes);
    }
}

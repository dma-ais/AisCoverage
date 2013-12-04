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

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ColorGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(ColorGenerator.class);

    private static Color[] colors = { Color.RED, interpolate(Color.RED, Color.YELLOW, 0.33f),
            interpolate(Color.RED, Color.YELLOW, 0.66f), Color.YELLOW, interpolate(Color.YELLOW, Color.GREEN, 0.33f),
            interpolate(Color.YELLOW, Color.GREEN, 0.66f), Color.GREEN };

    public static Color interpolate(Color colorA, Color colorB, float bAmount) {
        float aAmount = (float) (1.0 - bAmount);
        int r = (int) (colorA.getRed() * aAmount + colorB.getRed() * bAmount);
        int g = (int) (colorA.getGreen() * aAmount + colorB.getGreen() * bAmount);
        int b = (int) (colorA.getBlue() * aAmount + colorB.getBlue() * bAmount);
        return new Color(r, g, b);
    }

    public static Color getCoverageColor(Cell c, double highThreshold, double lowThreshold) {
        try {
            double deltaThreshold = highThreshold - lowThreshold;
            double incrementValue = deltaThreshold / 5;

            double coverage = c.getCoverage();
            Color color;
            if (coverage < lowThreshold) {
                color = colors[0];
            } else if (coverage < lowThreshold + incrementValue) {
                color = colors[1];
            } else if (coverage < lowThreshold + (incrementValue * 2)) {
                color = colors[2];
            } else if (coverage < lowThreshold + (incrementValue * 3)) {
                color = colors[3];
            } else if (coverage < lowThreshold + (incrementValue * 4)) {
                color = colors[4];
            } else if (coverage < highThreshold) {
                color = colors[5];
            } else {
                color = colors[6];
            }

            return color;
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return Color.WHITE;
        }

    }
}

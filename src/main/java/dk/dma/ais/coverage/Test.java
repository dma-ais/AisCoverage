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
package dk.dma.ais.coverage;

import java.util.Date;
import java.util.Random;

public class Test {

    /**
     * @param args
     */
    public static void main(String[] args) {
        long nrofmessages = 280000000;
        long[] messages = new long[(int) (nrofmessages * 2)];
        Random rand = new Random();
        for (int i = 0; i < messages.length; i++) {

            messages[i] = rand.nextLong();

        }
        Date then = new Date();
        // long last = 0;
        // int counter=0;
        for (int i = 0; i < messages.length; i++) {

            // if(messages[i] > last && messages[i+1] < 90 && messages[i+1] > 12){
            // counter++;
            // }
            // last = messages[i];
            i++;
        }
        Date now = new Date();
        System.out.println(now.getTime() - then.getTime());
        // System.out.println(counter);

        // byte
        // TODO Auto-generated method stub

    }

}

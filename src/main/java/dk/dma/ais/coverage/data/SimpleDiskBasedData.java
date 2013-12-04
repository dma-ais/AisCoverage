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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleDiskBasedData extends OnlyMemoryData {
    private int intervalMinutes = 15;
    private String filename = "coverageData.db";
    private static final Logger LOG = LoggerFactory.getLogger(SimpleDiskBasedData.class);

    public SimpleDiskBasedData() {
        load();
        new Thread() {
            public void run() {
                while (true) {
                    try {
                        Thread.sleep(intervalMinutes * 60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    save();
                    // LOG.info("data saved to disk");
                }

            }
        }.start();

    }

    private void load() {
        long starttime = System.currentTimeMillis();
        ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filename)));
            SourceHandler handler = (SourceHandler) in.readObject();
            this.gridHandler = handler;
            LOG.info("DB loaded in " + (System.currentTimeMillis() - starttime));
        } catch (Exception e) {
            LOG.info("DB not found, using new DB");
            LOG.error(e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                }
            }
        }

    }

    private void save() {
        long starttime = System.currentTimeMillis();
        try {
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(filename)));
            out.writeObject(this.gridHandler);
            out.close();
            LOG.info("project saved in " + (System.currentTimeMillis() - starttime));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new SimpleDiskBasedData();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

}

package dk.dma.ais.coverage.data.json;

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

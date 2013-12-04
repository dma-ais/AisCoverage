package dk.dma.ais.coverage.data.json;

import java.io.Serializable;
import java.util.Map;

public class JSonCoverageMap implements Serializable {
    private static final long serialVersionUID = 1L;
    public double latSize;
    public double lonSize;
    public Map<String, ExportCell> cells;
}

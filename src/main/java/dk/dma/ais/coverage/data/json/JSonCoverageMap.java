package dk.dma.ais.coverage.data.json;

import java.io.Serializable;
import java.util.Map;

public class JSonCoverageMap implements Serializable {
	public double latSize;
	public double lonSize;
	public Map<String,ExportCell> cells;
}

package dk.dma.ais.coverage;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.dma.ais.coverage.data.ICoverageData;

public class Purger extends Thread{
	private static final Logger LOG = LoggerFactory.getLogger(Purger.class);
	
	private final int maxWindowSize;
	private final ICoverageData dataHandler;
	private final int pollTimeInSeconds;
	
	public Purger(int maxWindowSize, ICoverageData dataHandler, int pollTimeInSeconds){
		this.maxWindowSize = maxWindowSize;
		this.dataHandler = dataHandler;
		this.pollTimeInSeconds = pollTimeInSeconds;
	}
	@Override
	public void run() {
		while(true){
			if(Helper.latestMessage != null && Helper.firstMessage != null){
				int windowSize = (int) ((Helper.getCeilDate(Helper.latestMessage).getTime() - Helper.getFloorDate(Helper.firstMessage).getTime())/1000/60/60);

				if(windowSize > maxWindowSize ){
					Date trimPoint = new Date(Helper.getCeilDate(Helper.latestMessage).getTime()-(1000*60*60*maxWindowSize));
					LOG.info("Window size: "+ windowSize + ". Max window size: "+maxWindowSize+". Lets purge data until "+trimPoint);

					dataHandler.trimWindow(trimPoint);
					
				}
			}
			
			try {
				Thread.sleep(pollTimeInSeconds*1000);
			} catch (InterruptedException e) {
				LOG.error("Failed sleeping", e);
			}
		}		
	}

	
}

package it.geosolutions.ie.actions;

import it.geosolutions.ie.dao.CombinationDao;
import it.geosolutions.ie.dao.ComputationDao;
import it.geosolutions.ie.model.Combination;
import it.geosolutions.ie.model.Computation;
import it.geosolutions.utils.db.LayerIntersector;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

public class RebuildStatistic 
{
	private ThreadPoolExecutor executor;

	// run the operation
	public RebuildStatistic(String wfsServerUrl, ThreadPoolExecutor executor){
		LOGGER.info("Created rebuilder with the following wfs url:");
		this.hostname = wfsServerUrl + "/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
		LOGGER.info(this.hostname);
		this.executor = executor;
		if (this.executor != null) {
			this.executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
			this.executor.prestartAllCoreThreads();
		}
	}

    private CombinationDao combinationDao;
    private ComputationDao computationDao;

    private String hostname;
    
 	 private static final Logger LOGGER = Logger.getLogger(RebuildStatistic.class);

	 public void rebuildAll(){
		 executor.submit(new ComputationBuilder());
	 }
	 
	 public void rebuild(Combination combination){
		   Computation computation = new Computation();
		   computation.start();
		   computationDao.persist(computation);
		   try {
			   URL wfsURL = new URL(hostname);
			   String source = combination.getSource();
			   String target = combination.getTarget();
			   String sourceMask = combination.getMask();
			   String targetMask = combination.getMask();
			   String sourceCode = combination.getSourceCode();
			   String targetCode = combination.getTargetCode();
			   LOGGER.info("Rebuilding combination #" + combination.getId() + "= ("+ source + ","+ target+ ")");
	           new LayerIntersector(wfsURL, source, target, sourceMask ,targetMask, sourceCode, targetCode);
			   computation.finish(true);
		     } catch (Exception e) {
			   LOGGER.error("Computation failed", e);
			   computation.finish(false);
		     } 
	       computationDao.update(computation);
	       LOGGER.info("Computation duration: "+ computation.getDuration());
	 }
	 
	 public void addCombination(String source,String target, String mask, String sourceCode, String targetCode ){
		 LOGGER.info("Creating new combination: " + "From " + source + " to " + target + " masked by " + mask);
		 Combination c = new Combination();
		 c.populate(source, target, mask, sourceCode, targetCode);
		 combinationDao.persist(c);
	 }
	 
	public List<Combination> getCombinations(){
		LOGGER.info("Getting all combinations");
		return combinationDao.getCombinations();
    }
	public List<Computation> getComputations(){
		LOGGER.info("Getting all computations");
		return computationDao.getComputations();
	}	 
	public void removeCombination(Long id){
		LOGGER.info("Removing combination #"+ id);
		combinationDao.remove(id);
	}	 
    public void setCombinationDao(CombinationDao combinationDao) {
    	this.combinationDao = combinationDao;
    }
    public void setComputationDao(ComputationDao computationDao) {
    	this.computationDao = computationDao;
    }
    
    public void setCombination(Long id, boolean enabled){
    	combinationDao.enable(id, enabled);
    }
    
    /**
	 * 
	 * @author Alessio Fabiani, GeoSolutions SAS
	 *
	 */
    private class ComputationBuilder implements Callable<String> {
		
		public String call() throws Exception {
			LOGGER.info("Launched a rebuildAll operation...");

			List<Combination> combinations = combinationDao.getEnabledCombinations();
			try{
				LayerIntersector.clean(new URL(hostname));
				computationDao.purgeStatus();
			}catch(Exception e){
				LOGGER.error("Could not clean records", e);
			}
			for (Combination combination : combinations) {
				rebuild(combination);
			}
			
			return "DONE!";
		}
	}
}
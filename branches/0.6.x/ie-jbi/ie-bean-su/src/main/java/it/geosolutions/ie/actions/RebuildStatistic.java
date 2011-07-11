package it.geosolutions.ie.actions;

import it.geosolutions.ie.config.DataStoreParams;
import it.geosolutions.ie.dao.CombinationDao;
import it.geosolutions.ie.dao.ComputationDao;
import it.geosolutions.ie.model.Combination;
import it.geosolutions.ie.model.Computation;
import it.geosolutions.ie.model.Download;
import it.geosolutions.utils.db.LayerIntersector;

import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

public class RebuildStatistic 
{
	private DataStoreParams dsParams;
	
	private ThreadPoolExecutor executor;

	// run the operation
	public RebuildStatistic(String wfsServerUrl, DataStoreParams dsParams, ThreadPoolExecutor executor){
		LOGGER.info("Created rebuilder with the following wfs url:");
		this.hostname = wfsServerUrl + "/wfs?service=WFS&request=GetCapabilities&version=1.0.0";
		LOGGER.info(this.hostname);
		
		this.dsParams = dsParams;
		
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
            LayerIntersector  li = new LayerIntersector(
                    wfsURL, source, target, sourceMask ,targetMask, sourceCode, targetCode,
                    dsParams.getHostname(), dsParams.getPort(), dsParams.getDatabase(), 
                    dsParams.getSchema(), dsParams.getUser(), dsParams.getPassword(),
                    combination.getId() , combination.getPreserveTarget()
            );
            
            if (li.getSize()>0){
                combination.setEmpty(false);	   
            }else{
                combination.setEmpty(true);
            }
            
            LOGGER.info("Replicating combination information to download table");            
            combinationDao.persistDownload(combination);
            computation.finish(true);            

        } catch (Exception e) {
            LOGGER.error("Computation failed", e);
            computation.finish(false);
            } 
        computationDao.update(computation);
        combinationDao.update(combination);
        
        combinationDao.persistDownload(combination);
        LOGGER.info("Computation duration: "+ computation.getDuration());
        
    }
	 
	 public void addCombination(String source,String target, String mask, String sourceCode, String targetCode, boolean preserve_target ){
		 LOGGER.info("Creating new combination: " + "From " + source + " to " + target + " masked by " + mask);
		 Combination c = new Combination();
		 c.populate(source, target, mask, sourceCode, targetCode, preserve_target);
		 combinationDao.persist(c);
	 }
	 
	public List<Combination> getCombinations(){
		LOGGER.info("Getting all combinations");
		return combinationDao.getCombinations();
    }
	public List<Download> getPopulatedCombinations(){
		LOGGER.info("Getting all populated downloads");
		return combinationDao.getPopulatedCombinations();
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
            //Remove all downloads
            combinationDao.clearDownloads();
            
            List<Combination> combinations = combinationDao.getEnabledCombinations();
            try{
                LayerIntersector.clean(new URL(hostname));
                LOGGER.info("Before purge");
                computationDao.purgeStatus();
                LOGGER.info("After purge");
            }catch(Exception e){
                    LOGGER.error("Could not clean records", e);
            }
            LOGGER.info("Starting the rebuild process");
            
            if (combinations.size() == 0 || combinations == null){
                LOGGER.info("No enabled combinations, aborting rebuild");
                return "No enabled combinations";
            }else{
                LOGGER.info("Yes, there are combinations :)");

                for (Combination combination : combinations) {
                    LOGGER.info("Rebuilding combination #" + combination.getId());
                    rebuild(combination);
                }
                return "DONE!";
            }
              
            }
        }
   }
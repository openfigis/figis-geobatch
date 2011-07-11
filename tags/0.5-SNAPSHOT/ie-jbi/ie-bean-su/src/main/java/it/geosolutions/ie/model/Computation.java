package it.geosolutions.ie.model;
import it.geosolutions.ie.actions.RebuildStatistic;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;

import org.apache.log4j.Logger;


@Entity
public class Computation {
	 private static final Logger LOGGER = Logger.getLogger(Computation.class);

	
    @Id @GeneratedValue
    private Long id;
    
    @Column(nullable=false)
    private Date started;
    @Column(nullable=true)
    private Date finished = null;
    @Column(nullable=false)
    private String status = "RUNNING";
    @Column(nullable=false)
    private long duration;

    /**
	 * @param finished the finished to set
	 */
	public void setFinished(Date finished) {
		this.finished = finished;
	}
	/**
	 * @return the finished
	 */
	public Date getFinished() {
		return finished;
	}
	/**
	 * @param started the started to set
	 */
	public void setStarted(Date started) {
		this.started = started;
	}
	/**
	 * @return the started
	 */
	public Date getStarted() {
		return started;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	public void finish(Boolean successful) {
		setFinished(new Date());
		calculateDuration();
        if(successful) setStatus("COMPLETED");
        else setStatus("FAILED");
	}

	public void start() {
		setStarted(new Date());
	}

	public void calculateDuration(){
	    LOGGER.info("Computation started at " + started.toString() + " ("+ started.getTime() +")");
		LOGGER.info("Computation finished at " + finished.toString() + " ("+ finished.getTime() +")");
		long d = finished.getTime() - started.getTime() ;
		LOGGER.info("Duration: " + d);
	    setDuration(d);
	    LOGGER.info("this.duration: " + this.duration);
	}
	/**
	 * @param duration the duration to set
	 */
	public void setDuration(long duration) {
		this.duration = duration;
	}
	/**
	 * @return the duration
	 */
	public long getDuration() {
		return duration;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatus() {
		return status;
	}

    
}

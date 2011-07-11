package it.geosolutions.ie.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;

@Entity
public class Download {
	
	public void populate(Combination c){
	        this.cid = c.getId();
		this.source = c.getSource();
		this.target = c.getTarget();
		this.mask = c.getMask();
		this.sourceCode = c.getSourceCode();
		this.targetCode = c.getTargetCode();
		this.preserveTarget = c.getPreserveTarget();
		this.empty = c.getEmpty();
		setStatus(c.getStatus());
	}
    @Id @GeneratedValue
    private Long id;

    private Long cid;
    /**
     * Computations will be initialized with this
     * number of day that should pass between refreshing of the Computation.
     */
    @Column(nullable=false)
    private String source;
    
    @Column(nullable=false)
    private String target;
    
    @Column(nullable=false)
    private String mask;
    
    @Column(nullable=true)
    private String sourceCode;
    
    @Column(nullable=true)
    private String targetCode;
    
    @Column(nullable=false)
    private String status;
    
    @Column(nullable=false)
    private Boolean preserveTarget;

    @Column(nullable=false)
    private Boolean empty;

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

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param target the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * @return the target
	 */
	public String getTarget() {
		return target;
	}

	/**
	 * @param mask the mask to set
	 */
	public void setMask(String mask) {
		this.mask = mask;
	}

	/**
	 * @return the mask
	 */
	public String getMask() {
		return mask;
	}

	/**
	 * @param sourceCode the sourceCode to set
	 */
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	/**
	 * @return the sourceCode
	 */
	public String getSourceCode() {
		return sourceCode;
	}

	/**
	 * @param targetCode the targetCode to set
	 */
	public void setTargetCode(String targetCode) {
		this.targetCode = targetCode;
	}

	/**
	 * @return the targetCode
	 */
	public String getTargetCode() {
		return targetCode;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getStatus() {
		return status;
	}
    public void enable(){
    	setStatus("ENABLED");
    }
    public void disable(){
    	setStatus("DISABLED");
    }

	public void setPreserveTarget(Boolean preserveTarget) {
		this.preserveTarget = preserveTarget;
	}

	public boolean getPreserveTarget() {
		return preserveTarget;
	}
	
	public void setEmpty(Boolean empty) {
		this.empty = empty;
	}

	public boolean getEmpty() {
		return empty;
	}

    public void setCid(Long cid) {
        this.cid = cid;
    }

    public Long getCid() {
        return cid;
    }

}

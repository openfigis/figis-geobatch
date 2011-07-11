package it.geosolutions.ie.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Entity;

@Entity
public class Combination {
	
	public void populate(String source, String target, String mask, 
			             String sourceCode, String targetCode, boolean preserveTarget){
		this.source = source;
		this.target = target;
		this.mask = mask;
		this.sourceCode = sourceCode;
		this.targetCode = targetCode;
		this.preserveTarget = preserveTarget;
		this.empty = true;
		setStatus("ENABLED");
	}
    @Id @GeneratedValue
    private Long id;
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

}

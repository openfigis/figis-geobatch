package it.geosolutions.figis.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "INTERSECTION")
@XStreamAlias("Intersection")
@XmlRootElement(name = "intersection")
public class Intersection {
	
    public enum Status{

        TOCOMPUTE(1),
        COMPUTED(2),
        COMPUTING(3);

        private int value;

        Status(int value) {
            this.value = value;
        }
        
        // the identifierMethod
        public int toInt() {
          return value;
        }

         // the valueOfMethod
         public  static Status fromInt(int value) {    
             switch(value) {
                 case 1: return TOCOMPUTE;
                 case 2: return COMPUTED;
                 case 3: return COMPUTING;
                 default:
                         return TOCOMPUTE;
             }
        }
       
        public String toString() {
          switch(this) {
            case TOCOMPUTE:
                return "toCompute";
            case COMPUTED:
                return "Computed";
            case COMPUTING:
                return "Computing";  
            default:
            	return "toCompute";
          }
        }
    }
	
	
	@Id
	@GeneratedValue
	@Column(name = "INTERSECTION_ID")
	private long id;
	
	
	@Column(name = "MASK")
	@XStreamAlias("mask")
	@XStreamAsAttribute
	boolean mask;
	
	@Column(name = "FORCE")
	@XStreamAlias("force")
	@XStreamAsAttribute	
	boolean force;
	
	
	@Column(name = "PRESERVETRGGEOM")
	@XStreamAlias("preserveTrgGeom")
	@XStreamAsAttribute	
	boolean preserveTrgGeom;
	
	
	@Column(name = "SRCLAYER")
	@XStreamAlias("srcLayer")
	String srcLayer;
	
	@Column(name = "TRGLAYER")
	@XStreamAlias("trgLayer")
	String trgLayer;
	
	@Column(name = "SRCCODEFIELD")
	@XStreamAlias("srcCodeField")
	String srcCodeField;
	
	@Column(name = "TRGCODEFIELD")
	@XStreamAlias("trgCodeField")
	String trgCodeField;
	
	@Column(name = "MASKLAYER", nullable = true)
	@XStreamAlias("maskLayer")
	String maskLayer;
	
	@Column(name = "AREACRS", nullable = true)
	@XStreamAlias("areaCRS")
	String areaCRS;
		
	
	

	
    @Column(name= "STATUS", columnDefinition="integer", nullable = true)
    @Type(
        type = "org.hibernate.type.EnumType",
        parameters = {
                @Parameter(
                    name  = "enumClass",                      
                    value = "it.geosolutions.figis.persistence.model.Intersection$Status"),
                @Parameter(
                    name  = "identifierMethod",
                    value = "toInt"),
                @Parameter(
                    name  = "valueOfMethod",
                    value = "fromInt")
                }
    )
    
    
    
    private Status status = Status.TOCOMPUTE;

    
	public Intersection(boolean mask, boolean force, boolean preserveTrgGeom,
			String srcLayer, String trgLayer, String srcCodeField,
			String trgCodeField, String maskLayer, String areaCRS, Status status) {
		super();
		this.mask = mask;
		this.force = force;
		this.preserveTrgGeom = preserveTrgGeom;
		this.srcLayer = srcLayer;
		this.trgLayer = trgLayer;
		this.srcCodeField = srcCodeField;
		this.trgCodeField = trgCodeField;
		this.maskLayer = maskLayer;
		this.areaCRS = areaCRS;
		this.status = status;
	}



	public Intersection() {
		super();
	}

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
	
	
	
	public Status getStatus() {
		return status;
	}


	public void setStatus(Status status) {
		this.status = status;
	}

	public boolean isMask() {
		return mask;
	}
	public void setMask(boolean mask) {
		this.mask = mask;
	}
	public boolean isForce() {
		return force;
	}
	public void setForce(boolean force) {
		this.force = force;
	}
	public boolean isPreserveTrgGeom() {
		return preserveTrgGeom;
	}
	public void setPreserveTrgGeom(boolean preserveTrgGeom) {
		this.preserveTrgGeom = preserveTrgGeom;
	}
	public String getSrcLayer() {
		return srcLayer;
	}
	public void setSrcLayer(String srcLayer) {
		this.srcLayer = srcLayer;
	}
	public String getTrgLayer() {
		return trgLayer;
	}
	public void setTrgLayer(String trgLayer) {
		this.trgLayer = trgLayer;
	}
	public String getSrcCodeField() {
		return srcCodeField;
	}
	public void setSrcCodeField(String srcCodeField) {
		this.srcCodeField = srcCodeField;
	}
	public String getTrgCodeField() {
		return trgCodeField;
	}
	public void setTrgCodeField(String trgCodeField) {
		this.trgCodeField = trgCodeField;
	}
	public String getMaskLayer() {
		return maskLayer;
	}
	public void setMaskLayer(String maskLayer) {
		this.maskLayer = maskLayer;
	}
	public String getAreaCRS() {
		return areaCRS;
	}
	public void setAreaCRS(String areaCRS) {
		this.areaCRS = areaCRS;
	}

	
	

}

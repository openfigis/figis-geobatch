package it.geosolutions.figis.model;
/***********
 * this class is the model for the Intersection object
 */


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import javax.xml.bind.annotation.XmlRootElement;


@XStreamAlias("Intersection")
@XmlRootElement(name = "intersection")
public class Intersection {
	
        public enum Status{
            NOVALUE(0),
            TOCOMPUTE(1),
            COMPUTED(2),
            COMPUTING(3),
            TODELETE(4);

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
                     case 0: return NOVALUE;
                     case 1: return TOCOMPUTE;
                     case 2: return COMPUTED;
                     case 3: return COMPUTING;
                     case 4: return TODELETE;
                     default:
                             return TOCOMPUTE;
                 }
            }

            public String toString() {
              switch(this) {
                case NOVALUE:
                      return "noValue";
                case TOCOMPUTE:
                    return "toCompute";
                case COMPUTED:
                    return "Computed";
                case COMPUTING:
                    return "Computing";
                case TODELETE:
                    return "toDelte";
                default:
                    return "toCompute";
              }
            }
        }
	
	
	private long id;

	@XStreamAlias("mask")
	@XStreamAsAttribute
	boolean mask;
	

	@XStreamAlias("force")
	@XStreamAsAttribute	
	boolean force;

	@XStreamAlias("preserveTrgGeom")
	@XStreamAsAttribute	
	boolean preserveTrgGeom;

	@XStreamAlias("srcLayer")
	String srcLayer;
	

	@XStreamAlias("trgLayer")
	String trgLayer;
	

	@XStreamAlias("srcCodeField")
	String srcCodeField;
	

	@XStreamAlias("trgCodeField")
	String trgCodeField;
	

	@XStreamAlias("maskLayer")
	String maskLayer;
	

	@XStreamAlias("areaCRS")
	String areaCRS;
    
        private Status status = Status.NOVALUE;

    
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

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Intersection other = (Intersection) obj;
        if ((this.srcLayer == null) ? (other.srcLayer != null) : !this.srcLayer.equals(other.srcLayer)) {
            return false;
        }
        if ((this.trgLayer == null) ? (other.trgLayer != null) : !this.trgLayer.equals(other.trgLayer)) {
            return false;
        }
        return true;
    }
    
    
    

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
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

    @Override
    public String toString() {
        return "Intersection{" + "id=" + id + ", mask=" + mask + ", force=" + force + ", preserveTrgGeom=" + preserveTrgGeom + ", srcLayer=" + srcLayer + ", trgLayer=" + trgLayer + ", srcCodeField=" + srcCodeField + ", trgCodeField=" + trgCodeField + ", maskLayer=" + maskLayer + ", areaCRS=" + areaCRS + ", status=" + status + '}';
    }

	
	

}

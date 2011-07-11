package it.geosolutions.figis.client;

import java.io.Serializable;

import org.apache.wicket.Component;

@SuppressWarnings("serial")
public abstract class IEComponentInfo<C extends Component> implements Serializable {

    /**
     * the id of the component
     */
    String id;
    /**
     * the title of the component
     */
    String title;
    /**
     * The description of the component
     */
    String description;
    /**
     * the class of the component
     */
    Class<C> componentClass;
    
    /**
     * The id of the component.
     */
    public String getId() {
        return id;
    }
    /**
     * Sets the id of the component.
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * The i18n key for the title of the component.
     * <p>
     * The exact way this title is used depends one the component. For instance
     * if the component is a page, the title could be the used for a link to the
     * page. If the component is a panel in a tabbed panel, the title might be 
     * the label on the tab.
     * </p>
     */
    public String getTitleKey() {
        return title;
    }
    /**
     * The i18n key for the title of the component.
     */
    public void setTitleKey(String title) {
        this.title = title;
    }
    
    /**
     * The i18n key for the description of the component.
     * <p>
     * This description is often used as a tooltip, or some contextual help.
     * </p>
     * 
     */
    public String getDescriptionKey() {
        return description;
    }
    
    /**
     * Sets the description of the component.
     */
    public void setDescriptionKey( String description ) {
        this.description = description;
    }

    /**
     * The implementation class of the component.
     */
    public Class<C> getComponentClass() {
        return componentClass;
    }
    
    /**
     * Sets the implementation class of the component.
     */
    public void setComponentClass(Class<C> componentClass) {
        this.componentClass = componentClass;
    }
}


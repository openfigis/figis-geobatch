package it.geosolutions.figis.client;

import it.geosolutions.figis.client.security.IESecuredPage;
import it.geosolutions.figis.client.security.PageAuthorizer;


@SuppressWarnings("serial")
public class IEMenuPageInfo extends IEComponentInfo<IEBasePage> implements Comparable<IEMenuPageInfo> {
    IECategory category;
    int order;
    String icon;

    public void setCategory(IECategory category){
        this.category = category;
    }

    public IECategory getCategory(){
        return category;
    }

    public void setOrder(int order){
        this.order = order;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getOrder(){
        return order;
    }

    public int compareTo(IEMenuPageInfo other){
        return getOrder() - other.getOrder();
    }
    
    public PageAuthorizer getPageAuthorizer() {
        return IESecuredPage.DEFAULT_AUTHORIZER;
    }
}


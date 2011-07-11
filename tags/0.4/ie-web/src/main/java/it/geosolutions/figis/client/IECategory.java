package it.geosolutions.figis.client;

import java.io.Serializable;

@SuppressWarnings("serial")
public class IECategory implements Comparable<IECategory>, Serializable {

	private int order;

	private String namekey;

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public String getNameKey() {
		return namekey;
	}

	public void setNameKey(String namekey) {
		this.namekey = namekey;
	}

	public int compareTo(IECategory other) {
		return getOrder() - other.getOrder();
	}
}

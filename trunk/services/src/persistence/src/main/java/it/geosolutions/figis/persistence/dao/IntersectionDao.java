package it.geosolutions.figis.persistence.dao;
import it.geosolutions.figis.persistence.model.Intersection;

import java.util.List;

import com.trg.dao.jpa.GenericDAO;

public interface IntersectionDao extends GenericDAO<Intersection, Long>{
	public boolean clean();
	public boolean force(Intersection intersection);
	public void insertIntersection(List<Intersection> intersections);
	public Intersection lookUp(Intersection intersection);
	public void insert(Intersection intersection);
	public List<Intersection> getAllIntersections();
	public boolean remove(Intersection intersection);
}

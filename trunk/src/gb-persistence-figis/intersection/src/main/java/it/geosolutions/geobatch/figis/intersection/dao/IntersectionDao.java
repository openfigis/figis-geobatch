package it.geosolutions.geobatch.figis.intersection.dao;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;

import java.util.List;

public interface IntersectionDao {
	public boolean clean();
	public boolean force(Intersection intersection);
	public void insertIntersection(List<Intersection> intersections);
	public Intersection lookUp(Intersection intersection);
	public void insert(Intersection intersection);
	public List<Intersection> getAllIntersections();
	public boolean remove(Intersection intersection);
}

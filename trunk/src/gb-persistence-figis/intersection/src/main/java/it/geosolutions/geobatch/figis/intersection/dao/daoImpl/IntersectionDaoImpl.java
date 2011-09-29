package it.geosolutions.geobatch.figis.intersection.dao.daoImpl;

import java.util.List;

import org.hibernate.SessionFactory;

import it.geosolutions.geobatch.figis.intersection.dao.IntersectionDao;
import it.geosolutions.geobatch.figis.intersection.model.Intersection;

public class IntersectionDaoImpl extends BaseDAO<Intersection, Long> implements IntersectionDao{


	
	public IntersectionDaoImpl() {
		
	}

	public boolean clean() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean force(Intersection intersection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void insertIntersection(List<Intersection> intersections) {
		// TODO Auto-generated method stub
		
	}

	public Intersection lookUp(Intersection intersection) {
		// TODO Auto-generated method stub
		return null;
	}

	public void insert(Intersection intersection) {
		// TODO Auto-generated method stub
		
	}

	public List<Intersection> getAllIntersections() {
		// TODO Auto-generated method stub
		return null;
	}
		
/*
	public boolean clean() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean force(Intersection intersection) {
		// TODO Auto-generated method stub
		return false;
	}

	public void insertIntersection(List<Intersection> intersections) {
		// TODO Auto-generated method stub
		
	}

	public Intersection lookUp(Intersection intersection) {
		// TODO Auto-generated method stub
		return null;
	}



	public void insert(Intersection intersection) {
		// TODO Auto-generated method stub
		
	}



	public List<Intersection> getAllIntersections() {
		// TODO Auto-generated method stub
		return null;
	}



	public boolean remove(Intersection intersection) {
		// TODO Auto-generated method stub
		return false;
	}
*/
}

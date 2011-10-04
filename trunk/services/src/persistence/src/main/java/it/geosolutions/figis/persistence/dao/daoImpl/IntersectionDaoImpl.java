package it.geosolutions.figis.persistence.dao.daoImpl;

import java.util.List;


import org.hibernate.SessionFactory;
import org.springframework.transaction.annotation.Transactional;

import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.persistence.model.Intersection;

 @Transactional
public class IntersectionDaoImpl extends BaseDAO<Intersection, Long> implements IntersectionDao{


	
	public IntersectionDaoImpl() {
		
	}
	
	
	

	@Override
	public Intersection save(Intersection entity) {
		// TODO Auto-generated method stub
		return super.save(entity);
	}

    @Override
    public boolean remove(Intersection entity) {
        return super.remove(entity);
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

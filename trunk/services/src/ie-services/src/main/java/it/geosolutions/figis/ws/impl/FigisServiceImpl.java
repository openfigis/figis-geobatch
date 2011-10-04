package it.geosolutions.figis.ws.impl;

import it.geosolutions.figis.persistence.dao.ConfigDao;
import it.geosolutions.figis.persistence.dao.IntersectionDao;
import it.geosolutions.figis.persistence.dao.daoImpl.IntersectionDaoImpl;
import it.geosolutions.figis.persistence.model.Intersection;
import it.geosolutions.figis.ws.FigisService;
import it.geosolutions.figis.persistence.model.Config;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundDetails;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import java.util.List;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class FigisServiceImpl implements FigisService{

    public FigisServiceImpl() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        this.configDao = (ConfigDao) ctx.getBean("ie-configDAO");
        intersectionDao = (IntersectionDao) ctx.getBean("ie-intersectionDAO");
    }


    ConfigDao configDao =  null;
    IntersectionDao intersectionDao= null;

    public ConfigDao getConfigDao() {
        return configDao;
    }


    public void setConfigDao(ConfigDao configDao) {
        this.configDao = configDao;
    }

    public IntersectionDao getIntersectionDao() {
        return intersectionDao;
    }

    public void setIntersectionDao(IntersectionDao intersectionDao) {
        this.intersectionDao = intersectionDao;
    }

    @Override
    public Config getConfig(Long id) {
        return configDao.find(id);
    }

    @Override
    public List<Config> getAllConfigs() {
        return configDao.findAll();
    }

    @Override
    public Intersection getIntersection(Long id) {
        IntersectionDao intersectionDao = new IntersectionDaoImpl();
        return intersectionDao.find(id);
    }

    @Override
    public List<Intersection> getAllIntersections() {
        return intersectionDao.findAll();
    }


    @Override
    public long insertConfig(Config config) {
        configDao.save(config);
        return config.getConfigId();
    }

    @Override
    public long insertIntersection(Intersection intersection) {
        intersectionDao.save(intersection);
        return intersection.getId();
    }

    @Override
    public boolean deleteConfig(long id) throws ResourceNotFoundFault {
                Config config = configDao.find(id);

		if (config == null) {
			ResourceNotFoundDetails details = new ResourceNotFoundDetails();
			details.setId(id);
			details.setMessage("Config not found!");
			throw new ResourceNotFoundFault(details);
		}


		return configDao.remove(config);
    }

    @Override
    public boolean deleteIntersection(long id) throws ResourceNotFoundFault {
                Intersection intersection = intersectionDao.find(id);

		if (intersection == null) {
			ResourceNotFoundDetails details = new ResourceNotFoundDetails();
			details.setId(id);
			details.setMessage("Intersection not found!");
			throw new ResourceNotFoundFault(details);
		}


		return intersectionDao.remove(intersection);
    }





}

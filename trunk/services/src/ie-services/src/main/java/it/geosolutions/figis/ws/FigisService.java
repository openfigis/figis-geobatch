package it.geosolutions.figis.ws;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import it.geosolutions.figis.persistence.model.Config;
import it.geosolutions.figis.persistence.model.Intersection;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;

import java.util.List;
import javax.jws.WebParam;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;


@Path("/")
@Produces("application/xml")
public interface FigisService {

	    @GET
	    @Path("/config/{id}")
	    public Config getConfig(@PathParam("id") Long id);

            @GET
	    @Path("/config/")
	    public List<Config> getAllConfigs();


           @PUT
           @Path("/config/")
           long insertConfig(@WebParam(name = "Config") Config config);

           @DELETE
           @Path("/config/{id}")
           boolean deleteConfig(@PathParam("id") long id) throws ResourceNotFoundFault;

	    @GET
	    @Path("/intersection/{id}")
	    public Intersection getIntersection(@PathParam("id") Long id);

            @GET
	    @Path("/intersection/")
	    public List<Intersection> getAllIntersections();

           @PUT
           @Path("/intersection/")
           long insertIntersection(@WebParam(name = "Intersection") Intersection intersection);

           @DELETE
           @Path("/intersection/{id}")
           boolean deleteIntersection(@PathParam("id") long id) throws ResourceNotFoundFault;

	}

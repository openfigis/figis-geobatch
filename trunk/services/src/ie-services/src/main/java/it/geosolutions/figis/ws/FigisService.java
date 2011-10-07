package it.geosolutions.figis.ws;
/****************
 * This is the REST interface for the it.geosolutions.figis.model classes
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.model.Intersection.Status;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.response.Intersections;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.PUT;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import org.springframework.transaction.annotation.Transactional;


@Path("/")
@Produces("application/xml")
@WebService(name = "FigisService", targetNamespace = "http://services.figis.geosolutions.it/")
@Transactional
public interface FigisService {

	    @GET
	    @Path("/config/{id}")
	    public Config getConfig(@PathParam("id") Long id) throws ResourceNotFoundFault;

            @GET
	    @Path("/config/")
	    public Config existConfig();


           @POST
           @Path("/config/")
           long insertConfig(@WebParam(name = "Config") Config config);


           @PUT
           @Path("/config/")
           long updateConfig(@WebParam(name = "id") long id, @WebParam(name = "Config") Config config);

           @DELETE
           @Path("/config/{id}")
           boolean deleteConfig(@PathParam("id") long id) throws ResourceNotFoundFault;

	    @GET
	    @Path("/intersection/{id}")
	    public Intersection getIntersection(@PathParam("id") Long id) throws ResourceNotFoundFault;

	    @GET
	    @Path("/intersection/{srcLayer}/{trgLayer}")
	    public Intersections getIntersectionsByLayerNames(@PathParam("srcLayer") String srcLayer, @PathParam("trgLayer") String trgLayer);

            @GET
	    @Path("/intersection/")
	    public Intersections getAllIntersections();

           @POST
           @Path("/intersection/")
           public long insertIntersection(@WebParam(name = "Intersection") Intersection intersection);

           @PUT
           @Path("/intersection/{id}/{status}")
           public long updateIntersectionStatusByID(@WebParam(name = "id") long id, @WebParam(name = "status") Status status) throws ResourceNotFoundFault;

           @DELETE
           @Path("/intersection/{id}")
           boolean deleteIntersection(@PathParam("id") long id) throws ResourceNotFoundFault;

           @DELETE
           @Path("/intersection/")
           boolean deleteIntersections();

	}

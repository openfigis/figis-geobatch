/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.figis.ws;
/****************
 * This is the REST interface for the it.geosolutions.figis.model classes
 */
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.exceptions.BadRequestExceptionFault;
import it.geosolutions.figis.ws.response.Intersections;
import it.geosolutions.figis.ws.response.IntersectionsPageCount;

import java.util.List;
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
	    public List<Config> getConfigs();

        @POST
        @Path("/config/")
        long insertConfig(@WebParam(name = "Config") Config config);


        @PUT
        @Path("/config/{id}")
        long updateConfig(@PathParam("id") long id, @WebParam(name = "Config") Config config);

        @DELETE
        @Path("/config/{id}")
        boolean deleteConfig(@PathParam("id") long id) throws ResourceNotFoundFault;

	    @GET
	    @Path("/intersection/{id}")
	    public Intersection getIntersection(@PathParam("id") Long id) throws ResourceNotFoundFault;

	    @GET
	    @Path("/intersection/{srcLayer}/{trgLayer}")
	    public Intersections getIntersectionsByLayerNames(@PathParam("srcLayer") String srcLayer, @PathParam("trgLayer") String trgLayer);


        //@GET
	    //@Path("/intersection/")
	   // public List<Intersection> getAllIntersections();
	    
        @GET
	    @Path("/intersections/count/")
	    public IntersectionsPageCount getAllIntersectionsCount(@QueryParam("start") Integer start,@QueryParam("limit") Integer limit) throws BadRequestExceptionFault;
	    
	    @GET
	    @Path("/intersection/countallintersection/")
	    public long getCountIntersections(@QueryParam("mask") String mask) ;
	    
	    @GET
	    @Path("/intersection/")
	    public List<Intersection> getAllIntersections(@QueryParam("start") Integer start,@QueryParam("limit") Integer limit) throws BadRequestExceptionFault;
	    
           @POST
           @Path("/intersection/")
           public long insertIntersection(@WebParam(name = "Intersection") Intersection intersection);

           @PUT
           @Path("/intersection/{id}")
           public long updateIntersectionByID(@PathParam("id") long id, @WebParam(name = "Intersection") Intersection intersection) throws ResourceNotFoundFault;

           @DELETE
           @Path("/intersection/{id}")
           boolean deleteIntersection(@PathParam("id") long id) throws ResourceNotFoundFault;

           @DELETE
           @Path("/intersection/")
           boolean deleteIntersections() throws BadRequestExceptionFault;

	}

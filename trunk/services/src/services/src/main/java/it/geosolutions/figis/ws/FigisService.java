/*
 * ====================================================================
 *
 * Intersection Engine
 *
 * Copyright (C) 2007 - 2011 GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 *
 * GPLv3 + Classpath exception
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by developers
 * of GeoSolutions.  For more information on GeoSolutions, please see
 * <http://www.geo-solutions.it/>.
 *
 */
package it.geosolutions.figis.ws;

/****************
 * This is the REST interface for the it.geosolutions.figis.model classes
 */
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import it.geosolutions.figis.model.Config;
import it.geosolutions.figis.model.Intersection;
import it.geosolutions.figis.ws.exceptions.BadRequestExceptionFault;
import it.geosolutions.figis.ws.exceptions.ResourceNotFoundFault;
import it.geosolutions.figis.ws.response.Intersections;
import it.geosolutions.figis.ws.response.IntersectionsPageCount;

import org.springframework.transaction.annotation.Transactional;


@Path("/")
@Produces("application/xml")
@WebService(name = "FigisService", targetNamespace = "http://services.figis.geosolutions.it/")
@Transactional
@RolesAllowed({ "ADMIN", "USER", "GUEST" })
public interface FigisService
{

    @GET
    @Path("/config/{id}")
    @RolesAllowed({ "ADMIN" })
    public Config getConfig(@PathParam("id") Long id) throws ResourceNotFoundFault;


    @GET
    @Path("/config/")
    @RolesAllowed({ "ADMIN" })
    public List<Config> getConfigs();

    @POST
    @Path("/config/")
    @RolesAllowed({ "ADMIN" })
    long insertConfig(@WebParam(name = "Config") Config config);


    @PUT
    @Path("/config/{id}")
    @RolesAllowed({ "ADMIN" })
    long updateConfig(@PathParam("id") long id,
        @WebParam(name = "Config") Config config);

    @DELETE
    @Path("/config/{id}")
    @RolesAllowed({ "ADMIN" })
    boolean deleteConfig(@PathParam("id") long id) throws ResourceNotFoundFault;

    @GET
    @Path("/intersection/{id}")
    @RolesAllowed({ "ADMIN", "USER", "GUEST" })
    public Intersection getIntersection(@PathParam("id") Long id) throws ResourceNotFoundFault;

    @GET
    @Path("/intersection/{srcLayer}/{trgLayer}")
    @RolesAllowed({ "ADMIN", "USER", "GUEST" })
    public Intersections getIntersectionsByLayerNames(@PathParam("srcLayer") String srcLayer,
        @PathParam("trgLayer") String trgLayer);

    @GET
    @Path("/intersection/count/")
    @RolesAllowed({ "ADMIN", "USER", "GUEST" })
    public IntersectionsPageCount getAllIntersectionsCount(@QueryParam("start") Integer start,
        @QueryParam("limit") Integer limit) throws BadRequestExceptionFault;

    @GET
    @Path("/intersection/countallintersection/")
    @RolesAllowed({ "ADMIN", "USER", "GUEST" })
    public long getCountIntersections(@QueryParam("mask") String mask);

    @GET
    @Path("/intersection/")
    @RolesAllowed({ "ADMIN", "USER", "GUEST" })
    public List<Intersection> getAllIntersections(@QueryParam("start") Integer start,
        @QueryParam("limit") Integer limit) throws BadRequestExceptionFault;

    @POST
    @Path("/intersection/")
    @RolesAllowed({ "ADMIN" })
    public long insertIntersection(@WebParam(name = "Intersection") Intersection intersection);

    @PUT
    @Path("/intersection/{id}")
    @RolesAllowed({ "ADMIN" })
    public long updateIntersectionByID(@PathParam("id") long id,
        @WebParam(name = "Intersection") Intersection intersection) throws ResourceNotFoundFault;

    @DELETE
    @Path("/intersection/{id}")
    @RolesAllowed({ "ADMIN" })
    boolean deleteIntersection(@PathParam("id") long id) throws ResourceNotFoundFault;

    @DELETE
    @Path("/intersection/")
    @RolesAllowed({ "ADMIN" })
    boolean deleteIntersections() throws BadRequestExceptionFault;

}

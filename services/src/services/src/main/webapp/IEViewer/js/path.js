/*
 * ====================================================================
 *
 * Intersection Viewer
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
	var debug = false;
	var abilitaPulsanti = true;
	var myWindow;
	var user = '';
	var userProfile = 'view';
	var queryString = '';
	var searchString = '';
	var PAGINATION = 20;
	var store;
	var xg;
	var grid;
	var btnSearch;
	var expandPressed = false;
	var mapIdToOpen = ""; mapIdToOpen = window.top.location;
	var timerStarted = false;
	var TIMER_INTERVAL = 30000;
	var APPLICATION_CONTEXT_PATH = '';
	var FDHUrl = APPLICATION_CONTEXT_PATH+'/ie-services/intersection/count/';
	var PROXY_URL = APPLICATION_CONTEXT_PATH+'/ie-services/intersection/count/';
	var PROXY_URL_DEL = APPLICATION_CONTEXT_PATH+'/ie-services/intersection/count/';
	//var proxyUrlCount = APPLICATION_CONTEXT_PATH+'/ie-services/intersection/countallintersection/';
	var PROXY_FIGIS = 'http://192.168.139.128:8484/figis';
	var PROXY_DOWNLOAD = '';
	var PROXY_FIGIS_DOWNLOAD = PROXY_FIGIS+'/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA';
	var PROXY_URL_GEN_STATUS = APPLICATION_CONTEXT_PATH+'/ie-services/intersection/generalStatusComputing/';
	
	
		
			

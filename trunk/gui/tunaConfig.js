
var Tuna = {
	isDeveloper: (document.domain.indexOf('192.168.') == 0),
	isTesting: ((document.domain.indexOf('figis02') == 0 || document.domain.indexOf('193.43.36.238') == 0 || document.domain.indexOf('www-data.fao.org')==0)),
	currentSiteURI: location.href.replace(/^([^:]+:\/\/[^\/]+).*$/,"$1")
};

Tuna.geoServerBase = Tuna.isDeveloper ? 'http://192.168.1.110:8484' : ( Tuna.isTesting ? 'http://193.43.36.238:8484' : ('http://' + document.domain ) );
Tuna.restBase = Tuna.isDeveloper ? 'http://192.168.1.110:8484' : ( Tuna.isTesting ? 'http://193.43.36.238:8484' : ('http://' + document.domain ) );

Tuna.vars = {
	geoserverURL: Tuna.geoServerBase + "/figis/geoserver",
	geowebcacheURL: Tuna.geoServerBase + "/figis/geoserver/gwc/service",
	wms: Tuna.geoServerBase + "/figis/geoserver" + "/wms",
	gwc: Tuna.geoServerBase + "/figis/geoserver/gwc/service" + "/wms"
};

Tuna.http_timeout = 60000;
Tuna.speciesURL = Tuna.restBase + "/figis/figis-tuna/rest/tuna/species";
Tuna.gearsURL = Tuna.restBase + "/figis/figis-tuna/rest/tuna/gears";
Tuna.yearsURL = Tuna.restBase + "/figis/figis-tuna/rest/tuna/years";
Tuna.animationsURL = Tuna.restBase + "/figis/geoserver/wms/animate?";
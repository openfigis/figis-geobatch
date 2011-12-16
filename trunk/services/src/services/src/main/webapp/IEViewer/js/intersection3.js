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
	//alert('intersection3.js');
	function getParameter ( queryString1, parameterName1 ) {
	try{
		// Add "=" to the parameter name (i.e. parameterName=value)
		if(debug)alert('queryString1='+queryString1+'----parameterName1: ' + parameterName1  );
		var parameterName = new String(parameterName1 + "=");
		var queryString = new String(queryString1);
		if(debug)alert('parameterName='+parameterName);
		if (queryString!=null && queryString!='' && queryString!='undefined' && queryString.length > 0 ) {
			// Find the beginning of the string
			if(debug)alert('ENTRATO!!!');
			begin = queryString.indexOf(parameterName);
			if(debug)alert('begin='+begin);
			// If the parameter name is not found, skip it, otherwise return the value
			if ( begin != -1 ) {//alert('trovato parametro');
				// Add the length (integer) to the beginning
				begin += parameterName.length;
				// Multiple parameters are separated by the "&" sign
				end = queryString.indexOf ( "&" , begin );
				if(debug)alert('end='+end);
				if ( end == -1 ) {
				end = queryString.length
				}
				// Return the string
				return unescape ( queryString.substring ( begin, end ) );
			}
			// Return "view" if no parameter has been found
			return "";
		}else{
			return queryString;
		}
		}catch(e){
			if(debug)alert('ERRORE:'+e.message);
		}
	} 
	
	/* cut the string from ':'*/
	function cutStr(value) {
	  var ret = value;
	  try{
	       if(value != null){
		      var M = new String(value);
				  if(M.indexOf(':')!=-1){
					   ret = M.substring(M.indexOf(':')+1);
					return ret;
				  }else{
				       if(debug)alert(value);
					return value;
				  }
		    }
		  }catch(e){
			if(debug)alert("ERRORE: "+e.message);
			return value;
		  }
	  }



	/* Open windows page as external window */
	function download(srcLayer,srcCodeField,trgLayer,trgCodeField,type,newW,status,type2show){//openDownloadWindow(typeFunc,userProfile,idMap,newW,desc){
		if(status=='TOCOMPUTE' || status=='COMPUTING' || status=='TODELETE' || status=='FAILED'){
			Ext.Msg.alert('Status: '+status+'', 'You can\'t still export this Intersection now');
			return false;
		}
		Ext.Msg.confirm('', 'Export \''+type2show+'\' format?', function(btn,text){
		if (btn == 'yes'){
		 if(eval(newW)){
		 /* Open link in a new window - a new window every-time */
		 		var srcLayerCut = cutStr(srcLayer);
				var srcCodeFieldCut = cutStr(srcCodeField);
				var trgLayerCut = cutStr(trgLayer);
				var trgCodeFieldCut = cutStr(trgCodeField);
				var downlSrc = PROXY_FIGIS_DOWNLOAD+'&outputFormat='+type+'&CQL_FILTER=(SRCLAYER=\''+srcLayerCut+'\' AND SRCCODENAME=\''+srcCodeFieldCut+'\' AND TRGLAYER=\''+trgLayerCut+'\' AND TRGCODENAME=\''+trgCodeFieldCut+'\')';
				//downlSrc = 'http://localhost:8081/download/TUNA_SPATIAL_STAT_DATA.zip';
				if(debug)alert('downlSrc=='+downlSrc);

               //funziona se sono sul server da cui scarico o se downlSrc passa per il proxy
			   //-->
			   var downlSrcPrxy = PROXY_DOWNLOAD+'download.html?src2Down='+downlSrc;
			  // download3(downlSrcPrxy);
			   /**/
			   //funziona ovunque, poco elegante
			  var stylew = "location=1,status=0, toolbar=0";
			  //alert(stylew);
			   if(type=='csv')stylew='location=1,status=0,toolbar=0,scrollbars=0,menubar=0,width=10,height=5';
			   if(type=='GML2')stylew='location=1,status=0,toolbar=0,scrollbars=1,menubar=1,width=600,height=500';
			   if(type=='GML2-ZIP')stylew='location=1,status=0, toolbar=0,scrollbars=0,menubar=0,width=10,height=5';
			   if(type=='text/xml;%20subtype=gml/3.1.1')stylew='location=1,status=0,toolbar=0,scrollbars=1, menubar=1, width=600,height=500';
			   if(type=='text/xml;%20subtype=gml/3.2')stylew='location=1,status=0,toolbar=0,scrollbars=1, menubar=1, width=600,height=500';
			   if(type=='json')stylew='location=1,status=0, toolbar=0,scrollbars=1,menubar=1, width=600,height=500';
			   if(type=='SHAPE-ZIP')stylew='location=1,status=0, toolbar=0,scrollbars=0,width=10,height=5';
			   //download4(downlSrc,type2show);
			  // alert(stylew);
			  var w = window.open(downlSrc,'Download',stylew);//funziona ovunque, sistema classico --> vedi anche downlSrcPrxy
			  //funziona ovunque, ma deve essere nel server da cui si scarica e funziona da proxy per la richiesta ajaxsistema classico con pagina
			  //mettere PROXY_FIGIS+'download....' e montare la pagina 'download.html' nella dir di PROXY_FIGIS
			  //-->var w = window.open(proxyDownload+'download.html?src2Down='+downlSrc,'Download','location=1,status=0,scrollbars=0, menubar=0, toolbar=0, width=600,height=300');
				  /*w.document.write("<html>");
				  w.document.write("<head>");
				  //w.document.write("<meta http-equiv='refresh' content='1;url=\'"+downlSrc+"\''>");

				  w.document.write("</head>");
				  w.document.write("<body>");// onload='javascript:startDownload(\""+downlSrc+"\");'
				  w.document.write("<h2><a id=\"idDown\" href=#>You attempt to download '"+type+"' file</a></h2>");
				  w.document.write("<script>");
				  w.document.write("function startDownload(sr){");
				  w.document.write("alert(sr);");
				  w.document.write("       document.getElementById(\"idDown\").href=sr;");
				  w.document.write("       document.getElementById(\"idDown\").click();");
				  w.document.write("       window.document.close();");
				  w.document.write("}");
				  w.document.write("</script>");
				  w.document.write("</body>");
				  w.document.write("</html>");*/
				  
			 /* */
			  ////w.close();
			  /*
			  
			  */
			 /* apre una finestra extjs dentro una finestra window*/
			 /*-->
			 Ext.IframeWindow = Ext.extend(Ext.Window, {
				onRender: function() {
					this.headCfg = {
						head: this.head
					};
					this.bodyCfg = {
						tag: 'iframe',
						src: this.src,
						cls: this.bodyCls,
						head: this.head,
						style: {
							border: '0px none',
							width: '100%'//
							, height: '500px'//'100%'
						}
					};
					Ext.IframeWindow.superclass.onRender.apply(this, arguments);
				}
				});

				var w = new Ext.IframeWindow({
					id:'idMapManager',
					width: '70%',//
					collapsible:false,
					closable: false, //Hide close button of the Window
					modal: true, //When modal:true it make the window modal and mask everything behind it
					loadMask: { 
						msg: 'Loading ...' 
				    	}, 
					maskEmpty: true, 
					height: '500px',
					title:"Download window - '"+(type!='-1'?(type+", "):'')+type+"'",
					
					src: (proxyDownload+'download.html?src2Down='+downlSrc),//(downlSrc),
					buttons: [{
							text: 'Close',
							handler: function(){
								w.close();
							}
						}
					]
				});
				
			w.show();
			*/
		}
				return true;
	      } else {
				if(debug)alert('abort id=='+id);
				return false;
	      }
	});
	}

	function formatTimestampString(str){
		try{
			var retStr = '';
			retStr+=str.substring(8,10)+'-'+str.substring(5,7)+'-'+
				str.substring(0,4)+' '+str.substring(11,16);
			return retStr;
		}catch(e4){
			if(debug)alert(e4.message);
			return str;
		}
	}
	 
	 /** download with extjs and iframe */
	function download4(srcd,type2show){
		//result  =   Ext.decode(response.responseText);
		try {
			Ext.destroy(Ext.get('iframe'+type2show));
		}catch(e) {alert('d4: '+e.message);}
		try{
			var wms = Ext.MessageBox.show({
	                msg: 'Downloading your data, please wait...',
	                progressText: 'Saving...',
	                width:300,
	                wait:true,
	                waitConfig: {interval:2200}
			   });
			   
			Ext.DomHelper.append(document.body, {
				tag: 'iframe',
				id:'iframe'+type2show,
				css: 'display:none;visibility:hidden;height:0px;',
				src: srcd,//result.filename,
				frameBorder: 0,
				width: 0,
				height: 0
			});
			wms.hide();
		}catch(e){
			alert(e.message);
		}
	}
	
	/** download with ajax request: must be on same server of server-download*/
	function download3(src){
				var wms = Ext.MessageBox.show({
	                msg: 'Downloading your data, please wait...',
	                progressText: 'Saving...',
	                width:300,
	                wait:true,
	                waitConfig: {interval:1200}
			   });
			  if(debug)alert('downloading resource from link: '+src);
				Ext.Ajax.request({
						url : src,
						method: 'GET',
						success: function() { 
							Ext.MessageBox.alert('Success', 'Downloading...');
							wms.hide();
						},
						failure: function() { 
							wms.hide();
							Ext.MessageBox.alert('Failed', 'Something wrong has appened '); 
						} 
					});
	}
	
	/** refresh grid */
	function reloadGrid(){
		grid.getStore().reload();
	}
	
	
	var c=TIMER_INTERVAL/1000;
	var t;

	function timedCount()
	{
		//document.getElementById('idBtnPolling').value=c;
		c=c-1;
		if(timerStarted){
		
		t=setTimeout("timedCount()",TIMER_INTERVAL/30);
		if(c==0){
			c=TIMER_INTERVAL/1000;
			reloadGrid();
		}
		}
	}
	/*function for refreshing data*/
	function doTimer(){
		timerStarted=!timerStarted;
		if (timerStarted){
		  //Ext.Msg.alert('Polling', 'You have started the polling');
		  timedCount();
	  }
	}

	function doCountDown(){
		timerStarted=!timerStarted;
		if (timerStarted){
		  timedCount();
	  }
	}
	/* This function re-init store and grid for refreshing every time it will be called*/
	function initStore(str){
		xg = Ext.grid;
		//var totalCount2 = totalCount();
		store =  new Ext.data.XmlStore({
			root: 'Intersections',
			totalProperty: 'totalCount',
			totalRecords: 'totalCount',
			idProperty: 'id',
			remoteSort: false,
			record: 'Intersection',
			//encode: true,
			disableCaching: true,
			restful: true, // <-- This Store is RESTful
			fields: [
				'Intersections',
				'Intersection',
				'areaCRS',
				'id',
				'maskLayer',
				'status',
				'trgCodeField',
				'trgLayer',
				'force',
				'mask',
				'srcLayer',
				'srcCodeField',
				'totalCount'
			],
			success: function ( result ) {

			},
			failure: function ( result ) {
					alert('There\'s a problem with the connection.');
			},
			proxy: new Ext.data.HttpProxy({
				type: 'ajax',
				restful: true,
				url: PROXY_URL+searchString.replace(' ',''),
				method : 'GET',
				success: function ( result ) {

				},
				failure: function ( result ) {
					alert('There\'s a problem with proxy connection.');
				},

				reader: {
					//type: 'json',
					type: 'xml',
					root: 'Intersections',
					id: 'id',
					record: 'Intersection',
					totalProperty: 'totalCount'
				}
			})
			
		});

		// pluggable renders
		function renderTopic(value, p, r){
			var linkToAdd = FDHUrl+'{2}';
			return String.format('<b><a href="#" onclick="addLink(\'elID6\',\'{2}\',\'true\')">{2}</a></b>',value, r.data.name, r.id, r.data.id);
		}
		
		function renderLast(value, p, r){
			//return String.format('{0}', value, r.data['creation']);
			var retStr = formatTimestampString(r.data['creation']);
			return retStr;
		}
		
		function customRenderer(value,p,r){
			var ret = value;
			var stt = (r.data['status']);
			if(stt=='TODELETE')ret='<span style=\'color: #0000FF\'>'+value+'</span>';//blue
			if(stt=='COMPUTING')ret='<span style=\'color: orange\'>'+value+'</span>';
			if(stt=='TOCOMPUTE')ret='<span style=\'color: #FF0000\'>'+value+'</span>';
			if(stt=='NOSTATE')ret='<span style=\'color: #FF0000\'>'+value+'</span>';
			if(stt=='COMPUTED')ret='<span style=\'color: GREEN\'>'+ value +'</span>';
			if(stt=='FAILED')ret='<span style=\'color: RED\'>'+ value +'</span>';
			return ret;
		}

		// row expander
		var expander = new Ext.ux.grid.RowExpander({
			tpl : new Ext.XTemplate(
		'<div style="background-color: #f9f9f9;">&nbsp;&nbsp;&nbsp;&nbsp;',
		'&nbsp;&nbsp;&nbsp;&nbsp;<hr style="margin-left:20px;margin-right: 35px"/>',
		'<div class="x-toolbar-cell" style="margin-right: 40px;" align="right" id="ext-gen29">'+
			'<table cellspacing="0" class="x-btn x-btn-text-icon  x-btn-pressed" id="tableBtn" style="width: auto;">'+
			'<tbody class="x-btn-small x-btn-icon-small-left">'+
			'<tr>',
			'<tpl if="abilitaPulsanti==true">'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td class=""></td>'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td  class="x-btn-mc" >'+
						'<em unselectable="on" class=" x-btn-text csv">'+
						'<button type="button" id="csvBtn" class=" x-btn-text csv" alt="Export CSV" onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'csv\',true,\'{status}\',\'CSV\');">CSV</button></em></td>'+
						'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
		
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml2">'+
							'<button type="button" id="gml2Btn" class=" x-btn-text gml2"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'GML2\',true,\'{status}\',\'GML2\');">GML2</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
		
				'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml2-gzip">'+
							'<button type="button" id="zipBtn" class=" x-btn-text zip" onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'GML2-ZIP\',true,\'{status}\',\'ZIP\');">ZIP</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
	
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml31">'+
							'<button type="button" id="gml31Btn" class=" x-btn-text gml31"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'text/xml;%20subtype=gml/3.1.1\',true,\'{status}\',\'GML3.1.1\');">GML3.1</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
		
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml32">'+
							'<button type="button" id="gml32Btn" class=" x-btn-text gml32"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'text/xml;%20subtype=gml/3.2\',true,\'{status}\',\'GML3.2\');">GML3.2</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
	
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text geoJSON">'+
							'<button type="button" id="geoJSONVBtn" class=" x-btn-text geoJSON"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'json\',true,\'{status}\',\'GML-JSON\');">GML-JSON</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
		
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text shapefile">'+
							'<button type="button" id="shapefileBtn" class=" x-btn-text shapefile"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'SHAPE-ZIP\',true,\'{status}\',\'SHP\');">SHP</button></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'</tr>'+
			'<tr>'+
			'<td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td>'+
			'<td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td>'+
			'<td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td><td class=""><i>&nbsp;</i></td>'+
			'</tr></tbody>'+
			'</table>'+
		'</div>',
		'</div>'
			)
		});
		
	
		if(grid!=null && grid!='undefined' &&  grid!=''){
			grid.destroy();
		}
		
		grid = new xg.GridPanel({
			initPreview: true, 
		    	loadMask: { 
				msg: 'Loading ...' 
		    	}, 
			maskEmpty: true, 
			iconCls: 'icon-grid',
			title:'Intersection Engine',
			store: store,
			trackMouseOver:false,
			disableSelection:false,

			loadMask: true,
			
			colModel: new xg.ColumnModel({
				columns: [
					expander,
				   {
						id: 'id', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
						header: "Id",
						dataIndex: 'id',
						width: 4,
						sortable: false,
						menuDisabled: true
					},{
						header: "Source Layer",
						dataIndex: 'srcLayer',
						width: 17,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true
					},{
						header: "Target Layer",
						dataIndex: 'trgLayer',
						width: 13,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true
					},{
						header: "Source Code Field",
						dataIndex: 'srcCodeField',
						width: 14,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true
					},{
						header: "Target Code Field",
						dataIndex: 'trgCodeField',
						width: 14,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true
					},{
						header: "Area CRS",
						dataIndex: 'areaCRS',
						width: 12,
						hidden: false,
						sortable: false,
						menuDisabled: true
					},{
						header: "Mask Layer",
						dataIndex: 'maskLayer',
						width: 15,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true
					},{
						header: "Status",
						dataIndex: 'status',
						width: 13,
						hidden: false,
						align: 'left',
						sortable: false,
						menuDisabled: true, 
						renderer: customRenderer
					},{
						header: "Force",
						dataIndex: 'force',
						width: 7,
						hidden: false,
						align: 'center',
						sortable: false,
						menuDisabled: true
					},{
						header: "Mask",
						dataIndex: 'mask',
						width: 7,
						hidden: false,
						align: 'center',
						sortable: false,
						menuDisabled: true
					}
						]
			}

				),//end column model

			// customize view config
			viewConfig: {
				forceFit: true,
				showPreview: true, // custom property
				enableRowBody: true, // required to create a second, full-width row to show expanded Record data
				getRowClass : function(record, rowIndex, p, store){
					if(this.showPreview){
						p.body = '<p>'+record.data.excerpt+'</p>';
						return 'x-grid3-row-expanded';
					}
					return 'x-grid3-row-collapsed';
				}
			},
			sm: new Ext.grid.RowSelectionModel({
				singleSelect: true,
				listeners: {
				     rowselect: function(smObj, rowIndex, record) {
				         selRecordStore = record;
				    }
			       }
			    }),
					
			// paging bar on the bottom
			bbar: new Ext.PagingToolbar({
				pageSize: PAGINATION,
				store: store,
				displayInfo: true,
				//encode: true,
				displayMsg: 'Displaying results {0} - {1} of {2}',
				emptyMsg: 'No results to display',
				items:[
					'-',{
					pressed: expandPressed,
					enableToggle:true,
					text: 'Expand All',
					cls: 'x-btn-text-icon row_expand',
					toggleHandler: function(btn, pressed){
					var i = 0;
						if(pressed){
							for(i = 0; i <= grid.getStore().getCount(); i++) {
							    expander.expandRow(i);
							}
						}else{
							for(i = 0; i <= grid.getStore().getCount(); i++) {
							    expander.collapseRow(i);
							}
						}
					}
				},'-',{
					pressed: timerStarted,
					enableToggle:true,
					text: 'Polling DB',
					cls: 'x-btn-text-icon polling',
					//iconCls: 'silk-find',
					toggleHandler: function(btn, pressed){
							doTimer();
					}
				},'-']
			}),
			width:'98%',
			height:520,
			plugins: expander//,buttons
		});
		
		grid.on('render', function(){
			try{
				for(i = 0; i < grid.getStore().getCount(); i++) {
						var stt = grid.getStore().getAt(i).get('status');
							    if(stt=='TODELETE')grid.getView().getRow(i).style.color="#0000FF";//blue
								if(stt=='COMPUTING')grid.getView().getRow(i).style.color="#FF0000";
								if(stt=='TOCOMPUTE')grid.getView().getRow(i).style.color="#FF0000";//red
								if(stt=='NOSTATE')grid.getView().getRow(i).style.color="#FF0000";
								 var record = grid.getStore().getAt(i);  // Get the Record
								 grid.getSelectionModel().selectRow(i);
							}
							
			}catch(e3){alert(e3.message);}
			}, this, {
			single: true
		});
		
		// trigger the data store load
		store.load({params:{start:0, limit:PAGINATION}});

		// render it
		grid.render('topic-grid');
		
	}



	try {

		Ext.onReady(function(){

		store = initStore('');

	});

}catch(e){
	if(debug)alert(e.message);
}

		
			

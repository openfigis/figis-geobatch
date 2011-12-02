
	var debug = false;
	var abilitaPulsanti = true;
	var myWindow;
	var user = '';
	var userProfile = 'view';
	var queryString = '';
	var searchString = '';
	var pagination = 20;
	var store;
	var xg;
	var grid;
	var btnSearch;
	var expandPressed = false;
	var mapIdToOpen = ""; mapIdToOpen = window.top.location;
	
	
	var application_context_path = '';//'/test/test';
	var FDHUrl = application_context_path+'/ie-services/intersection/count/';//'/ie-services/intersection2/';//'data/intersection.xml';//'data/sheldonIntersection.json';//'/mapcomposer/?mapId=';
	var proxyUrl = application_context_path+'/ie-services/intersection/count/';//'/ie-services/intersection2/';//intersections/count//'data/intersection.xml';//'data/sheldonIntersection.json';//'/geostore/rest/extjs/search/';
	var proxyUrlDel = application_context_path+'/ie-services/intersection/count/';//'/ie-services/intersection2/';//intersections/count//'data/intersection.xml';//'data/sheldonIntersection.json';//'/geostore/rest/resources/resource/';
	//var proxyUrlCount = application_context_path+'/ie-services/intersection/countallintersection/';//'data/intersection.xml';//'data/sheldonIntersection.json';//'/geostore/rest/resources/resource/';
	var proxyFigis = 'http://192.168.1.110:8484/figis';
	var proxyDownload = '';
	var proxyFigisDownloadUrl = proxyFigis+'/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA';
	
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
	function download(srcLayer,srcCodeField,trgLayer,trgCodeField,type,newW){//openDownloadWindow(typeFunc,userProfile,idMap,newW,desc){
		Ext.Msg.confirm('', 'Export \''+type+'\' format?', function(btn,text){
			//if(debug)alert('btn == '+btn+', id=='+id);
		      if (btn == 'yes'){
			//if(debug)alert('go ahead=='+id);
		//if(idMap==null || idMap=='undefined' || idMap==''){idMap='-1';}
		 if(eval(newW)){
		 /* Open link in a new window - a new window every-time */
		 //if(debug)alert(srcLayer+srcCodeField+trgLayer+trgCodeField+type+newW);
		 		var srcLayerCut = cutStr(srcLayer);
				var srcCodeFieldCut = cutStr(srcCodeField);
				var trgLayerCut = cutStr(trgLayer);
				var trgCodeFieldCut = cutStr(trgCodeField);
				var downlSrc = proxyFigisDownloadUrl+'&outputFormat='+type+'&CQL_FILTER=(SRCLAYER=\''+srcLayerCut+'\' AND SRCCODENAME=\''+srcCodeFieldCut+'\' AND TRGLAYER=\''+trgLayerCut+'\' AND TRGCODENAME=\''+trgCodeFieldCut+'\')';
				//downlSrc = 'http://localhost:8081/download/TUNA_SPATIAL_STAT_DATA.zip';
				if(debug)alert('downlSrc=='+downlSrc);

               //funziona se sono sul server da cui scarico o se downlSrc passa per il proxy
			   //-->download3(downlSrc);
			   /**/
			   //funziona ovunque, poco elegante
			  var w = window.open(downlSrc,"Download window","location=1,status=0,scrollbars=0, menubar=0, toolbar=0, width=100,height=100");//funziona ovunque, sistema classico
			  //funziona ovunque, ma deve essere nel server da cui si scarica e funziona da proxy per la richiesta ajaxsistema classico con pagina
			  //mettere proxyFigis+'download....' e montare la pagina 'download.html' nella dir di proxyFigis
			  //-->var w = window.open(proxyDownload+'download.html?src2Down='+downlSrc,'Download window','location=1,status=0,scrollbars=0, menubar=0, toolbar=0, width=600,height=300');
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
		}else{
			if(debug)alert('attenzione: stai eseguendo un form redirect');
			document.location='http://demo1.geo-solutions.it/FDHWebGis/?appTabs="+userProfile+"&userProfile="+userProfile+"&id="+id';
			myWindow=document;
			formRedirect(document.location);
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
						url : src,//proxyUrlDel+id , 
						//headers: {
						//'X-CUSTOM-USERID': 'foo'
						//},
						//params : {  },
						method: 'GET',
						success: function() { 
							Ext.MessageBox.alert('Success', 'Downloading...');//result.responseText
							wms.hide();
						},
						failure: function() { 
							wms.hide();
							Ext.MessageBox.alert('Failed', 'Something wrong has appened ');//+result.date 
						} 
					});
	}
	/* Delete a map by id*/
	function download2(src){
		/*
		 Ext.Msg.confirm('', 'Do You want to download this map?', function(btn,text){
			if(debug)alert('btn == '+btn+', id=='+id);
		    if (btn == 'yes'){
					if(debug)alert('go ahead=='+id);
					var wms = Ext.MessageBox.show({
							msg: 'Downloadin your data, please wait...',
							progressText: 'Saving...',
							width:300,
							wait:true,
							waitConfig: {interval:200}
					   });
					   //Ext.MessageBox.hide();
					Ext.Ajax.request({
						url : src;//proxyUrlDel+id , 
						//headers: {
						//'X-CUSTOM-USERID': 'foo'
						//},
						//params : {  },
						method: 'GET',
						success: function () { 
						//wms.hide();
						Ext.MessageBox.alert('Success', 'Map with id:  has been deleted');//result.responseText

					},
					failure: function () { 
						Ext.MessageBox.alert('Failed', 'Something wrong has appened ');//+result.date 
					} 
				});
				return true;
	      } else {
				if(debug)alert('abort id=='+id);
				return false;
	      }
	});*/
	}
	
	function reloadGrid(){
		grid.getStore().reload();
	}
	
	/* This function re-init store and grid for refreshing every time it will be called*/
	function initStore(str){
/*
		if(str==null || str == '' || str == 'undefined')str = '';
			if(debug)alert('str: '+str);
			if(debug)alert('searchString: '+searchString);
			if(debug)alert('mapIdToOpen: '+mapIdToOpen);
*/
		xg = Ext.grid;
		//var totalCount2 = totalCount();
		store =  new Ext.data.XmlStore({
			root: 'Intersections',
			totalProperty: 'totalCount',
			totalRecords: 'totalCount',
			//successProperty: 'success',
			//idProperty: 'intersection',
			idProperty: 'id',
			//model: 'intersection',
			remoteSort: false,
			record: 'Intersection',
			//encode: true,
			disableCaching: true,
			restful: true, // <-- This Store is RESTful
            //totalRecords: '@total',
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
				/*if(successProperty=='false' || successProperty==false){
					alert('There\'s a problem with Geoserver connection.');
				}else{
					
					
				}*/

			},
			failure: function ( result ) {
					alert('There\'s a problem with the connection.');
			},
			proxy: new Ext.data.HttpProxy({
				type: 'ajax',
				restful: true,
				url: proxyUrl+searchString.replace(' ',''),//(!=''?searchString.replace(' ',''):''),//'data/sheldonIntersection.json',//
				method : 'GET',
				success: function ( result ) {
					/*if(document.getElementById('ext-gen30')!=null)document.getElementById('ext-gen30').click();
					
					try{
						if(mapIdToOpen!=''){
							if(debug)alert('mapIdToOpen: '+mapIdToOpen);
							var recordIndex = grid.store.getById(mapIdToOpen);
							if(debug)alert('recordIndex.id=='+recordIndex.id);	
							var getat = grid.store.findExact("id",parseInt(mapIdToOpen));
					
							if(debug)alert('getat: '+getat);
						}
					}catch(e3){if(debug)alert(e3.message);}*/
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
		
		// row expander
		var expander = new Ext.ux.grid.RowExpander({
			tpl : new Ext.XTemplate(
		'<div style="background-color: #f9f9f9;">&nbsp;&nbsp;&nbsp;&nbsp;<b>Description:</b> {description}<br/>',
		'&nbsp;&nbsp;&nbsp;&nbsp;<b>Last update:</b>{lastUpdate}<hr style="margin-left:20px;margin-right: 35px"/>',
		'<div class="x-toolbar-cell" style="margin-right: 40px;" align="right" id="ext-gen29">'+
			'<table cellspacing="0" class="x-btn x-btn-text-icon  x-btn-pressed" id="tableBtn" style="width: auto;">'+
			'<tbody class="x-btn-small x-btn-icon-small-left">'+
			'<tr>',
			'<tpl if="abilitaPulsanti==true">'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td class=""></td>'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td class=""><i>&nbsp;</i></td>'+
						'<td  class="x-btn-mc">'+
						'<em unselectable="on" class=" x-btn-text csv">'+
						'<button type="button" id="csvBtn" class=" x-btn-text csv" alt="Export CSV" onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'csv\',true);">CSV</button></em></td>'+
						//'<a  class=" x-btn-text csv" style="margin-top:5px" alt="Export CSV" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=csv&CQL_FILTER=(SRCLAYER=\'{srcLayer}\' AND SRCCODENAME=\'{srcCodeField}\' AND TRGLAYER=\'{trgLayer}\' AND TRGCODENAME=\'{trgCodeField}\')"  target="_blank">CSV</a></em></td>'+
						'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml2">'+
							'<button type="button" id="gml2Btn" class=" x-btn-text gml2"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'GML2\',true);">GML2</button></em></td>'+
							//'<a  class=" x-btn-text gml2" style="margin-top:5px" alt="Export GML2" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=GML2&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">GML2</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
				'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml2-gzip">'+
							'<button type="button" id="zipBtn" class=" x-btn-text zip" onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'GML2-ZIP\',true);">ZIP</button></em></td>'+
							//'<a  class=" x-btn-text zip" style="margin-top:5px" alt="Export GML2-GZIP" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=GML2-GZIP&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">GML2-ZIP</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml31">'+
							'<button type="button" id="gml31Btn" class=" x-btn-text gml31"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'text/xml;%20subtype=gml/3.1.1\',true);">GML3.1</button></em></td>'+
							//'<a  class=" x-btn-text gml31" style="margin-top:5px" alt="Export GML3.1" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=text/xml;%20subtype=gml/3.1.1&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">GML3.1</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text gml32">'+
							'<button type="button" id="gml32Btn" class=" x-btn-text gml32"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'text/xml;%20subtype=gml/3.2\',true);">GML3.2</button></em></td>'+
							//'<a  class=" x-btn-text gml32" style="margin-top:5px" alt="Export GML3.1" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=text/xml;%20subtype=gml/3.2&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">GML3.2</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text geoJSON">'+
							'<button type="button" id="geoJSONVBtn" class=" x-btn-text geoJSON"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'json\',true);">GML-JSON</button></em></td>'+
							//'<a  class=" x-btn-text geoJSON" style="margin-top:5px" alt="Export GML JSON" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=json&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">GML-JSON</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
			'<tpl if="abilitaPulsanti==true">'+
					'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text shapefile">'+
							'<button type="button" id="shapefileBtn" class=" x-btn-text shapefile"  onClick="javascript:download(\'{srcLayer}\',\'{srcCodeField}\',\'{trgLayer}\',\'{trgCodeField}\',\'SHAPE-ZIP\',true);">SHP</button></em></td>'+
							//'<a  class=" x-btn-text shapefile" style="margin-top:5px" alt="Export Shapefile" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=SHAPE-ZIP&CQL_FILTER=(INTERSECTION_ID LIKE %{srcLayer}_{srcLayer}_{srcCodeField}_{trgLayer}_{trgLayer}_{trgCodeField}%)"  target="_blank">SHP</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
			'</tpl>',
/*
			'<tpl if="abilitaPulsanti==true">'+
				'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class=""><i>&nbsp;</i></td>'+
							'<td class="x-btn-mc">'+
							'<em unselectable="on" class=" x-btn-text pdf">'+
							//'<button type="button" id="pdfBtn" alt="Export PDF" class=" x-btn-text pdf" onClick="javascript:download({id},\'PDF\');">PDF</button></em></td>'+
							'<a  class=" x-btn-text pdf" style="margin-top:5px" alt="Export RDF" href="http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=SHAPE-RDF"  target="_blank">RDF</a></em></td>'+
							'<td class="x-btn-mr"><i>&nbsp;</i></td>'+
						
			'</tpl>',	*/
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
			//grid.getView().refresh();
			//grid.getStore().removeAll();
			//grid.getStore().reload();
			grid.destroy();
		}
		//if(grid==null || grid=='' || grid=='undefined' || grid=='null')
		grid = new xg.GridPanel({
			initPreview: true, 
		    	loadMask: { 
				msg: 'Loading ...' 
		    	}, 
			maskEmpty: true, 
			iconCls: 'icon-grid',
			//frame: true,
			title:'Intersection Engine',
			store: store,
			trackMouseOver:false,
			disableSelection:true,
			sm: new Ext.grid.RowSelectionModel({
				singleSelect: true,
				listeners: {
				     rowselect: function(smObj, rowIndex, record) {
				         selRecordStore = record;
				    }
			       }
			    }),
			loadMask: true,
			cm: new xg.ColumnModel({
				defaults: {
					width: 20,
					sortable: false
				},
				//sortable: false,
				columns: [
					expander,
				   {
						id: 'id', // id assigned so we can apply custom css (e.g. .x-grid-col-topic b { color:#333 })
						header: "Id",
						dataIndex: 'id',
						width: 5,
						sortable: false
					},{
						header: "Source Layer",
						dataIndex: 'srcLayer',
						width: 15,
						hidden: false,
						align: 'left',
						sortable: false
					},{
						header: "Target Layer",
						dataIndex: 'trgLayer',
						width: 13,
						hidden: false,
						align: 'left',
						sortable: false
					},{
						header: "Source Code Field",
						dataIndex: 'srcCodeField',
						width: 17,
						hidden: false,
						align: 'left',
						sortable: false
					},{
						header: "Target Code Field",
						dataIndex: 'trgCodeField',
						width: 17,
						hidden: false,
						align: 'left',
						sortable: false
					},{
						header: "Area CRS",
						dataIndex: 'areaCRS',
						width: 13,
						hidden: false,
						sortable: false
					},{
						header: "Mask Layer",
						dataIndex: 'maskLayer',
						width: 19,
						hidden: false,
						//align: 'left',
						sortable: false
					},{
						header: "Status",
						dataIndex: 'status',
						width: 12,
						hidden: false,
						//align: 'left',
						sortable: false
					},{
						header: "Force",
						dataIndex: 'force',
						width: 7,
						hidden: false,
						align: 'left',
						sortable: false
					},{
						header: "Mask",
						dataIndex: 'mask',
						width: 7,
						hidden: false,
						align: 'left',
						sortable: false
					}
						]
			}),

			// customize view config
			viewConfig: {
				forceFit:true,
				enableRowBody:false,
				showPreview:false,
				getRowClass : function(record, rowIndex, p, store){
					if(this.showPreview){
						p.body = '<p>'+record.data.excerpt+'</p>';
						return 'x-grid3-row-expanded';
					}
					return 'x-grid3-row-collapsed';
				}
			},
			
			// paging bar on the bottom
			bbar: new Ext.PagingToolbar({
				pageSize: pagination,
				store: store,
				displayInfo: true,
				//encode: true,
				displayMsg: 'Displaying results {0} - {1} of {2}',
				emptyMsg: "No results to display",
				items:[
					'-',/* {
					pressed: true,
					enableToggle:true,
					text: 'Close Window',
					cls: 'x-btn-text-icon decline',
					//iconCls: 'silk-find',
					toggleHandler: function(btn, pressed){
						parent.window.close();
					}
				},'-', {
					pressed: true,
					enableToggle:true,
					text: 'New Map',
					cls: 'x-btn-text-icon map_add',
					//iconCls: 'silk-find',
					toggleHandler: function(btn, pressed){
						openDownloadWindow('edit','&auth=true',-1,'true','New Map');
					}
				},'-', */{
					pressed: expandPressed,
					enableToggle:true,
					text: 'Expand All',
					//text: (pressed?'Expand All':'Collapse All'),
					cls: 'x-btn-text-icon row_expand',
					toggleHandler: function(btn, pressed){
						if(pressed){
							for(i = 0; i <= grid.getStore().getCount(); i++) {
							    expander.expandRow(i);
							}
						}else{
							//text: 'Collapse All';
							for(i = 0; i <= grid.getStore().getCount(); i++) {
							    expander.collapseRow(i);
							}
						}
					}
				},'-']
			}),
			width:'95%',
			//width:700,
			//height:'90%',<<--- non lo prende
			height:520,
			plugins: expander//,buttons
		});

		/*grid.store.on('beforeload', function(){
			if(debug)alert('beforeload');
			if(debug)alert('beforeload: str: '+str);
			if(debug)alert('beforeload: searchString: '+searchString);
			if(mapIdToOpen!=''){
				Ext.Ajax.request({

					url : proxyUrlDel+mapIdToOpen , 
					headers: {
					//'Accept': 'application/json'
					'Accept': 'application/xml'
					},
					//params : {  },
					method: 'GET',
					success: function (result, request) { 
		  				//searchString = Ext.util.JSON.decode(result.responseText).Resource.name;
						searchString = Ext.util.XML.decode(result.responseText).Resource.name;
						if(searchString=='')searchString='';
						inputSearch.setValue(searchString);str = searchString;
						//grid.store.proxy.url = proxyUrl+str.replace(' ','');//Ext.urlEncode();
						if(debug)alert('beforeload_success: str: '+str);
						if(debug)alert('beforeload_success: searchString: '+searchString);
						if(searchString=='')searchString='';
						if(str=='')str='';
					},
					failure: function ( result, request) { 
						//Ext.MessageBox.alert('Failed', 'Something wrong has appened ');//+result.date 
					} 
				});
			}
			if(debug)alert('beforeload_end: str: '+str);
			if(debug)alert('beforeload_end: searchString: '+searchString);
		});*/

		/*grid.store.on('load', function(){
			if(debug)alert(inputSearch.getValue());
			if(debug)alert(grid.store.url);
			if(debug)alert('onload: str: '+str);
			//if(document.getElementById('ext-gen30')!=null)document.getElementById('ext-gen30').click();
			try{
				if(mapIdToOpen!=''){
					if(debug)alert('mapIdToOpen: '+mapIdToOpen);
					var recordIndex = grid.store.getById(mapIdToOpen);
					if(debug)alert('recordIndex.id=='+recordIndex.id);	
					var getat = grid.store.findExact("id",parseInt(mapIdToOpen));
					
					if(debug)alert('getat: '+getat);
						getat = grid.store.findExact("id",parseInt(mapIdToOpen));
						if(debug)alert('getat: '+getat);
						grid.getSelectionModel().selectRow(getat);
						grid.fireEvent('rowdblclick', grid, getat)
			
				}
			}catch(e3){if(debug)alert(e3.message);}
			}, this, {
			single: true
		});*/
		/*grid.store.proxy.on("exception", function(){
		alert('errore');
		});*/
		//grid.totalLenght = totalCount();
		//grid.totalProperty = totalCount();
		// trigger the data store load
		store.load({params:{start:0, limit:pagination}});

		// render it
		grid.render('topic-grid');

	}



	try {
		/*try {	//prendo il valore del mapId dalla get
			if(debug)alert(mapIdToOpen);
			var M = new String(mapIdToOpen);
			var io = M.indexOf('=');
			if(io!=-1){
				mapIdToOpen = M.substring(io+1);
			}else{	
				mapIdToOpen='';
			}
			if(mapIdToOpen!=''){
				if(M.indexOf('&fullScreen=')!=-1){
					mapIdToOpen = M.substring(io+1,M.indexOf('&fullScreen='));
				}else{
					mapIdToOpen = M.substring(io+1);
				}
			}
			if(debug)alert(mapIdToOpen);
		}catch(e4){
			if(debug)alert(e4.message);
		}
		if(debug)alert(mapIdToOpen);*/
		Ext.onReady(function(){

		store = initStore('');
			/*try{
				//var bt = Ext.StoreMgr.get('searchBtn');
				//var bt = Ext.StoreMgr.get('ext-gen87');
				var bt = Ext.getCmp('searchBtn');
				//var bt = document.getElementById('searchBtn');
				alert('c\'e\' passato');
				alert('trovato: '+bt);//tb.get
				var ft = btnSearch.getEl();
				alert('ft: '+ft.getItemId());
				alert('e lo clikka!!!');
			}catch(e){
				alert('nn lo pigliaaaa: '+e.message);
			}*/
	});

}catch(e){
	//if(debug)
	alert(e.message);
}

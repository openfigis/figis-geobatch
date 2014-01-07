/*
	var debug = false;

	var searchString = '';
	var pagination = 20;
	var store;
	var xg;
	var grid;
	var btnSearch;
	var expandPressed = false;
	var mapIdToOpen = ""; mapIdToOpen = window.top.location;
	var application_context_path = '';//'/test/test';
	var FDHUrl = application_context_path+'/ie-services/services/intersection/count/';
	var PROXY_URL = application_context_path+'/ie-services/services/intersection/count/';
	var PROXY_URL_DEL = application_context_path+'/ie-services/services/intersection/count/';
	//var proxyUrlCount = application_context_path+'/ie-services/services/intersection/countallintersection/';//'data/intersection.xml';//'data/sheldonIntersection.json';//'/geostore/rest/resources/resource/';
	var PROXY_FIGIS = 'http://192.168.1.110:8484/figis';
	
	var PROXY_FIGIS_DOWNLOAD = PROXY_FIGIS+'/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA';
	*/
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
	function download(srcLayer,srcCodeField,trgLayer,trgCodeField,mask,prsrvTrgGeom,type,newW){//openDownloadWindow(typeFunc,userProfile,idMap,newW,desc){
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
				var maskCut = cutStr(mask);
				var prsrvTrgGeomCut = cutStr(prsrvTrgGeom);
				var downlSrc = PROXY_FIGIS_DOWNLOAD+'&outputFormat='+type+'&CQL_FILTER=(SRCLAYER=\''+srcLayerCut+'\' AND SRCCODENAME=\''+srcCodeFieldCut+'\' AND TRGLAYER=\''+trgLayerCut+'\' AND TRGCODENAME=\''+trgCodeFieldCut+'\' AND MASKLAYER=\''+maskCut+'\' AND PRESERVETRGGEOM=\''+prsrvTrgGeomCut+'\' )';
				downlSrc = 'http://localhost:8081/download/TUNA_SPATIAL_STAT_DATA.zip';
				if(debug)alert('downlSrc=='+downlSrc);
//"http://192.168.1.110:8484/figis/geoserver/fifao/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=fifao:TUNA_SPATIAL_STAT_DATA&outputFormat=csv&CQL_FILTER=
//(SRCLAYER=\'{srcLayer}\' AND SRCCODENAME=\'{srcCodeField}\' AND TRGLAYER=\'{trgLayer}\' AND TRGCODENAME=\'{trgCodeField}\')"
               
			   download3(downlSrc);
			   /**/
			  //var w = window.open(downlSrc,"Download window","location=1,status=0,scrollbars=0, menubar=0, toolbar=0, width=100,height=100");
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
			 /*
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
							width: '10%'//
							, height: '100px'//'100%'
						}
					};
					Ext.IframeWindow.superclass.onRender.apply(this, arguments);
				}
				});

				var w = new Ext.IframeWindow({
					id:'idMapManager',
					width: '10%',//720
					collapsible:false,
					closable: false, //Hide close button of the Window
					modal: true, //When modal:true it make the window modal and mask everything behind it
					loadMask: { 
						msg: 'Loading ...' 
				    	}, 
					maskEmpty: true, 
					height: '600px',//100%
					title:"Download window - '"+(type!='-1'?(type+", "):'')+type+"'",
					
					src: (downlSrc),
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
				//alert('chai');
				var wms = Ext.MessageBox.show({
	                msg: 'Downloading your data, please wait...',
	                progressText: 'Saving...',
	                width:300,
	                wait:true,
	                waitConfig: {interval:200}
			   });
			  if(debug)alert('download3: downloading resource from link: '+src);
				Ext.Ajax.request({
						url : src,//PROXY_URL_DEL+id , 
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
						url : src;//PROXY_URL_DEL+id , 
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
	

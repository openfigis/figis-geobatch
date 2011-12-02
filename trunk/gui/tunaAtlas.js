var bounds;
var myMap;
var wms;
var wms_continent;
var NameArea;
var idArea;
var posPlus;
var param_filter;
var tuna_map;
var top_continent;
var inputParameters;
var nav;
var mouseMove;
var update_tuna;
var params;
var brkdwn_params;

var selectedGears = [];
var selectedSpecies = [];

var activeTab;

// pink tile avoidance
OpenLayers.IMAGE_RELOAD_ATTEMPTS = 5;
// make OL compute scale according to WMS spec
OpenLayers.DOTS_PER_INCH = 25.4 / 0.28;

Ext.onReady( function() {
            
    OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
        defaultHandlerOptions: {
            'single': true,
            'double': false,
            'pixelTolerance': 0,
            'stopSingle': false,
            'stopDouble': false
        },
        initialize: function (options) {
            this.handlerOptions = OpenLayers.Util.extend({}, this.defaultHandlerOptions);
            OpenLayers.Control.prototype.initialize.apply(this, arguments);
            this.handler = new OpenLayers.Handler.Click(this, { 'click': this.trigger }, this.handlerOptions);
        },
        trigger: function (e) {
            var lonlat = myMap.getLonLatFromViewPortPx(e.xy);
            
            // support GetFeatureInfo
            document.getElementById('nodelist').innerHTML = "Loading... please wait... <img src=\"images/grid-loading.gif\">";
            params = {
                SERVICE: "WMS",
                REQUEST: "GetFeatureInfo",
                EXCEPTIONS: "application/vnd.ogc.se_xml",
                BBOX: myMap.getExtent().toBBOX(),
                X: e.xy.x,
                Y: e.xy.y,
                INFO_FORMAT: 'text/html',
                QUERY_LAYERS: 'fifao:TUNA_YEARLY_CATCHES',
                FEATURE_COUNT: 50,
                Layers: 'fifao:TUNA_YEARLY_CATCHES',
                Styles: 'polygon',
                Srs: 'EPSG:4326',
                WIDTH: myMap.size.w,
                HEIGHT: myMap.size.h,
                format: 'image/png',
                'viewparams': getViewParams(),
                source: inputParameters
            };
            OpenLayers.loadURL( Tuna.vars.geoserverURL + '/ows', params, this, this.setHTML, this.setHTML);
            document.getElementById('brkdwn_nodelist').innerHTML = "Loading... please wait...  <img src=\"images/grid-loading.gif\">";
            brkdwn_params = {
                SERVICE: "WMS",
                REQUEST: "GetFeatureInfo",
                EXCEPTIONS: "application/vnd.ogc.se_xml",
                BBOX: myMap.getExtent().toBBOX(),
                X: e.xy.x,
                Y: e.xy.y,
                INFO_FORMAT: 'text/html',
                QUERY_LAYERS: 'fifao:TS_FI_TA_BREAKDOWN',
                FEATURE_COUNT: 50,
                Layers: 'fifao:TS_FI_TA_BREAKDOWN',
                Styles: 'polygon',
                Srs: 'EPSG:4326',
                WIDTH: myMap.size.w,
                HEIGHT: myMap.size.h,
                format: 'image/png',
                gs_url: Tuna.vars.geoserverURL + '/ows',
                source: inputParameters,
                viewparams: getViewParams(),
                specbrkdwn: true
            };
            OpenLayers.loadURL(Tuna.vars.geoserverURL + '/ows', brkdwn_params, this, this.setBRKDWNHTML, this.setBRKDWNHTML);
        },
        // sets the HTML provided into the nodelist element
        setHTML: function (response) {
            var text = response.responseText;
            if (text.indexOf("Exception") < 0)
                document.getElementById('nodelist').innerHTML = text;
            else
                document.getElementById('nodelist').innerHTML = "";
        },
        // sets the HTML provided into the brkdwn_nodelist element
        setBRKDWNHTML: function (response) {
            var text = response.responseText;
            if (text.indexOf("Exception") < 0)
                document.getElementById('brkdwn_nodelist').innerHTML = text;
            else
                document.getElementById('brkdwn_nodelist').innerHTML = "";
        }
    });	
	  
	  bounds = new OpenLayers.Bounds(-180, -90, 180, 90);
	  
    var options = {
    	theme: null, 
        controls: [],
        maxExtent: bounds,
        maxResolution: 0.703125,
        numZoomLevels: 10,
        projection: "EPSG:4326",
        units: 'degrees'        
    };
	
	  myMap = new OpenLayers.Map('gxmappanel', options);
	  
    fao_areas = new OpenLayers.Layer.WMS("FAO Fishing Areas", Tuna.vars.gwc,
        {
            layers: 'fifao:FISHING_AREAS',
            format: OpenLayers.Util.alphaHack() ? "image/gif" : "image/jpeg",
            tiled: 'true',
            tilesOrigin : "-180.0,-90.0",
            transparent: true
        },{
            singleTile: false,
            buffer: 0,
            isBaseLayer: false,
            wrapDateLine: true,
            ratio: 1
        }
    );
    
    tuna_map = new OpenLayers.Layer.WMS("Tuna Atlas", Tuna.vars.wms, 
        {
            layers: 'fifao:TUNA_YEARLY_CATCHES',
            format: OpenLayers.Util.alphaHack() ? "image/gif" : "image/png",
            tiled: 'true',
            tilesOrigin : "-180.0,-90.0",
            transparent: true
        },{
            singleTile: false,
            buffer: 0,
            isBaseLayer: false,
            wrapDateLine: true,
            ratio: 1
        }
    );
    
    world_grid = new OpenLayers.Layer.WMS("5x5-deg Grid", Tuna.vars.gwc,
        {
            layers: 'fifao:GRID_G5',
            format: OpenLayers.Util.alphaHack() ? "image/gif" : "image/png",
            tiled: 'true',
            tilesOrigin : "-180.0,-90.0",
            transparent: OpenLayers.Util.alphaHack() ? false : true
        },{
            singleTile: false,
            buffer: 0,
            isBaseLayer: OpenLayers.Util.alphaHack() ? true : false,
            wrapDateLine: true,
            ratio: 1
        }
    );
    
    top_continent = new OpenLayers.Layer.WMS("Continents", Tuna.vars.gwc,
        {
            layers: 'fifao:UN_CONTINENT',
            format: OpenLayers.Util.alphaHack() ? "image/gif" : "image/png",
            tiled: 'true',
            tilesOrigin : "-180.0,-90.0",
            transparent: OpenLayers.Util.alphaHack() ? true : false
        },{
            singleTile: false,
            buffer: 0,
            isBaseLayer: OpenLayers.Util.alphaHack() ? false : true,
            wrapDateLine: true,
            ratio: 1
        }
    );
    
    NameArea = 'FAO_MAJOR';
    idArea = 0;
    posPlus = -1;
    
    if (OpenLayers.Util.alphaHack()) {
        myMap.addLayers([world_grid, tuna_map, top_continent]);
    } else {
        myMap.addLayers([top_continent, world_grid, tuna_map, fao_areas]);
    }
    
    var poweredByControl = new OpenLayers.Control();
    OpenLayers.Util.extend(poweredByControl, {
        draw: function () {
          OpenLayers.Control.prototype.draw.apply(this, arguments);
          this.div.innerHTML = '<img src=\"images/FAO_blue_20_transp.gif\" width=\"60\" height=\"60\" class=\"olPoweredBy\" id=\"olPoweredBy\" title=\"Powered by...\" style=\"position:absolute;left:690px;top:285px\"/>';
          return this.div;
        }

    });
    
    myMap.addControl(poweredByControl);
	
    //
    // build up all controls            
    //
    nav = new OpenLayers.Control.Navigation({zoomBoxEnabled:true, documentDrag: true});
    myMap.addControl(new OpenLayers.Control.LoadingPanel());		
    myMap.addControl(new OpenLayers.Control.ZoomPanel());
    myMap.addControl(new OpenLayers.Control.PanPanel());
    myMap.addControl(nav);
    myMap.addControl(new OpenLayers.Control.ScaleLine());
    var mouseMove = new OpenLayers.Control.MousePosition();
    myMap.addControl(mouseMove);
    myMap.addControl(new OpenLayers.Control.LayerSwitcher());
	
    //myMap.zoomToMaxExtent();

    OpenLayers.Util.onImageLoad = function(){
       // //////////////////////
       // OL code
       // //////////////////////
       if (!this.viewRequestID || (this.map && this.viewRequestID == this.map.viewRequestID)) { 
            this.style.display = "";  
       }
        
       OpenLayers.Element.removeClass(this, "olImageLoadError");
       
       // //////////////////////
       // Tuna code
       // ////////////////////// 
       if(tuna_map && !(activeTab instanceof AnimationPanel)){               
            Ext.getCmp('quarter-slider').enable(); 
            Ext.getCmp('years-slider').enable();       
            
            Ext.getCmp("quarter-max-button").enable(); 
            Ext.getCmp("quarter-min-button").enable(); 
            Ext.getCmp("year-min-largestep").enable(); 
            Ext.getCmp("year-min-littlestep").enable(); 
            Ext.getCmp("year-max-littlestep").enable(); 
            Ext.getCmp("year-max-largestep").enable();
            Ext.getCmp("last-year").enable(); 
            Ext.getCmp("first-year").enable(); 
       }
    };
    
    //
    // support GetFeatureInfo
    //
    var click = new OpenLayers.Control.Click();
    myMap.addControl(click);
    click.activate();
    
    // //////////////////////////////////////////////////////////////////////////////
    // FIX for a bug in ExtJs 3.3.1 due to a problem in the Ext.Slider drag event
    // //////////////////////////////////////////////////////////////////////////////
    Ext.override(Ext.dd.DragTracker, {
        onMouseMove: function (e, target) {
            var isIE9 = Ext.isIE && (/msie 9/.test(navigator.userAgent.toLowerCase())) && document.documentMode != 6;
            if (this.active && Ext.isIE && !isIE9 && !e.browserEvent.button) {
                e.preventDefault();
                this.onMouseUp(e);
                return;
            }
            e.preventDefault();
            var xy = e.getXY(), s = this.startXY;
            this.lastXY = xy;
            if (!this.active) {
                if (Math.abs(s[0] - xy[0]) > this.tolerance || Math.abs(s[1] - xy[1]) > this.tolerance) {
                    this.triggerStart(e);
                } else {
                    return;
                }
            }
            this.fireEvent('mousemove', this, e);
            this.onDrag(e);
            this.fireEvent('drag', this, e);
        }
    });

    var panel = new Ext.Panel({
        id:'main-panel',
        baseCls:'x-plain',
        renderTo: 'ext-main',//Ext.getBody(),
        layout:'table',
        layoutConfig: {columns:2},
        width : 800,
        style: {
            'text-align': 'left'
        },
        // applied to child components
        defaults: {frame:false, header: false},
        items:[
            {
              xtype: 'panel',
              id: 'info-panel',
              header: false,
              autoScroll: true,
              collapsible: true,
              collapsed: true,
              forceLayout: true,
              width: 790,//730
              colspan : 2,
              border: true,
              listeners: {
                 expand: function(p){
                    myMap.updateSize();
                 }
              },
              html: [
                  '<table class="reftopmenu" border="0" cellpadding="2" cellspacing="2" width="100%"><tbody>',
                  '<tr>',
                  '<td align="left" nowrap="nowrap" width="5%"><b>Map Title:</b>',
                  '</td>',
                  '<td id="tabtitle" align="left" width="95%">',
                  '</td>',
                  '</tr>',
                  '<tr>',
                  '<td align="left" width="5%"><b>Species:</b>',
                  '</td>',
                  '<td id="tabspecies" align="left" width="95%">',
                  '</td>',
                  '</tr>',
                  '<tr>',
                  '<td align="left" width="5%"><b>Gears:</b>',
                  '</td>',
                  '<td id="tabgears" align="left" width="95%">',
                  '</td>',
                  '</tr>',
                  '<tr>',
                  '<td align="left" width="5%"><b>Periods:</b>',
                  '</td>',
                  '<td id="tabperiod" align="left" width="95%">',							
                  '</td>',
                  '</tr>',
                  '<tr>',
                  '<td align="left" width="5%"><b>Quarters:</b>',
                  '</td>',
                  '<td id="tabquarters" align="left" width="95%">',							
                  '</td>',
                  '</tr>',
                  '<tr>',
                  '<td colspan="2">',
                  '</td>',
                  '</tr></tbody>',
                  '</table>'
              ].join('')
            },
            new GeoExt.MapPanel({
                id: "gxmappanel",
                stateId: "mappanel",
                renderTo: '',
                height: 350,
                width: 760,//700
                map: myMap,
                border: true
            }),
            {
                layout:'vbox',
                border : false,
                layoutConfig: {
                  padding:'5',
                  align:'middle',
                  border: false
                },
                width: 40,
                height: 350,
                items: [
                    new Ext.Button({
                        tooltip: "Click to move up the range",
                        tooltipType: 'title',
                        id: "quarter-max-button",
                        iconCls: "quarter-max-button",
                        handler: function(){
                            var quarters = Ext.getCmp('quarter-slider');	
                            var qt_start = quarters.getValues()[0];
                            var qt_end = quarters.getValues()[1];
                
                            quarters.setValue(0, qt_start+1);
                            quarters.setValue(1, qt_end+1);
                            
                            Ext.getCmp('quarter-min-field').setValue('Q' + quarters.getValues()[0]);
                            Ext.getCmp('quarter-max-field').setValue('Q' + quarters.getValues()[1]);
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    }),
                    {
                        id : 'quarter-max-field',
                        xtype: 'textfield',
                        readOnly: true,
                        width: 22,
                        value: 'Q4'
                    },
                    new Ext.slider.MultiSlider({
                        id : 'quarter-slider',
                        vertical : true,
                        height   : 254,
                        minValue: 1,
                        maxValue: 4,
                        values  : [1, 4],
                        plugins : new Ext.slider.Tip({
                          getText: function(thumb){
                            return String.format('<b>Q{0}</b>', thumb.value);
                          }
                        }),
                        listeners: {
                          changecomplete : function (){
                            if (validateSelection()) {
				                       expandPan();
                            }
                            
                            Ext.getCmp('quarter-min-field').setValue('Q' + this.getValues()[0]);
                            Ext.getCmp('quarter-max-field').setValue('Q' + this.getValues()[1]);
                          }
                        }
                    }), 
                    {
                        id : 'quarter-min-field',
                        xtype: 'textfield',
                        readOnly: true,
                        width: 22,
                        value: 'Q1'
                    },
                    new Ext.Button({
                        tooltip: "Click to move down the range",
                        tooltipType: 'title',
                        id: "quarter-min-button",
                        iconCls: "quarter-min-button",
                        handler: function(){
                            var quarters = Ext.getCmp('quarter-slider');	
                            var qt_start = quarters.getValues()[0];
                            var qt_end = quarters.getValues()[1];
                            
                            quarters.setValue(0, qt_start-1);
                            quarters.setValue(1, qt_end-1);
                            
                            Ext.getCmp('quarter-min-field').setValue('Q' + quarters.getValues()[0]);
                            Ext.getCmp('quarter-max-field').setValue('Q' + quarters.getValues()[1]);
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    })
                ]
            }, 
            {
                height:40,
                colspan:2, 
                layout:'hbox',
                border : false,
                layoutConfig: {
                  border: false
                },
                items: [
                    new Ext.Button({
                        tooltip: "First year",
                        tooltipType: 'title',
                        id: "first-year",
                        iconCls: "first-year",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_end = years.getValues()[1];
                            
                            years.setValue(0, '1950');
                            years.setValue(1, yr_end);
                            
                            Ext.getCmp('years-min-field').setValue('1950');
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    }),
                    new Ext.Button({
                        tooltip: "Large interval decrement",
                        tooltipType: 'title',
                        id: "year-min-largestep",
                        iconCls: "year-min-largestep",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_start = years.getValues()[0];
                            var yr_end = years.getValues()[1];
                            
                            years.setValue(0, yr_start-10);
                            years.setValue(1, yr_end-10);
                            
                            Ext.getCmp('years-min-field').setValue(years.getValues()[0]);
                            Ext.getCmp('years-max-field').setValue(years.getValues()[1]);
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    }),
                    new Ext.Button({
                        tooltip: "Small interval decrement",
                        tooltipType: 'title',
                        id: "year-min-littlestep",
                        iconCls: "year-min-littlestep",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_start = years.getValues()[0];
                            var yr_end = years.getValues()[1];
                            
                            years.setValue(0, yr_start-1);
                            years.setValue(1, yr_end-1);
                            
                            Ext.getCmp('years-min-field').setValue(years.getValues()[0]);
                            Ext.getCmp('years-max-field').setValue(years.getValues()[1]);
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    }), 
                    {
                        id : 'years-min-field',
                        xtype: 'textfield',
                        readOnly: true,
                        width: 40,
                        value: '1950'
                    },
                    new Ext.slider.MultiSlider({
                        id : 'years-slider',
                        vertical : false,
                        width   : 550, //518,
                        minValue: 1950,
                        maxValue: new Date().getFullYear(),
                        values  : ['1950', new Date().getFullYear()],
                        plugins : new Ext.slider.Tip({
                            getText: function(thumb){
                                return String.format('<b>{0}</b>', thumb.value);
                            }
                        }),
                        listeners: {
                            changecomplete : function (){
                                if (validateSelection()) {
                                	expandPan();
                                }
                                Ext.getCmp('years-min-field').setValue(this.getValues()[0]);
                                Ext.getCmp('years-max-field').setValue(this.getValues()[1]);
                            }
                        }
                    }),
                    {
                        id : 'years-max-field',
                        xtype: 'textfield',
                        readOnly: true,
                        width: 40,
                        value: new Date().getFullYear()
                    },
                    new Ext.Button({
                        tooltip: "Small interval increment",
                        tooltipType: 'title',
                        id: "year-max-littlestep",
                        iconCls: "year-max-littlestep",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_start = years.getValues()[0];
                            var yr_end = years.getValues()[1];
                            
                            years.setValue(0, yr_start+1);
                            years.setValue(1, yr_end+1);
                            
                            Ext.getCmp('years-min-field').setValue(years.getValues()[0]);
                            Ext.getCmp('years-max-field').setValue(years.getValues()[1]);
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    }),				
                    new Ext.Button({
                        tooltip: "Large interval increment",
                        tooltipType: 'title',
                        id: "year-max-largestep",
                        iconCls: "year-max-largestep",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_start = years.getValues()[0];
                            var yr_end = years.getValues()[1];
                            
                            years.setValue(0, yr_start+10);
                            years.setValue(1, yr_end+10);
                            
                            Ext.getCmp('years-min-field').setValue(years.getValues()[0]);
                            Ext.getCmp('years-max-field').setValue(years.getValues()[1]);
                            
                            if (validateSelection()) {
				                      expandPan();					
                            }
                        }
                    }),
                    new Ext.Button({
                        tooltip: "Last year",
                        tooltipType: 'title',
                        id: "last-year",
                        iconCls: "last-year",
                        handler: function(){
                            var years = Ext.getCmp('years-slider');	
                            var yr_start = years.getValues()[0];
                            
                            years.setValue(0, yr_start);
                            years.setValue(1, new Date().getFullYear());
                            
                            Ext.getCmp('years-max-field').setValue(new Date().getFullYear());
                            
                            if (validateSelection()) {
				                       expandPan();					
                            }
                        }
                    })
                ]
            },
            {
               xtype: 'tabpanel',
               id: 'tab-panel',
               width: 790, //730,
               height: 500,
               colspan : 2,
               activeTab: 0,
               border: false,  
               deferredRender: true,
               listeners: {
                  scope: this,
                  beforetabchange : function(tabPanel, newTab, currentTab){
                      var controlsTab = Ext.getCmp('controls-panel'); 
                      
                      if(controlsTab.isVisible()){
                      
                          //
                          // Checking the Gears Type and Species mandatory selections
                          // 
                          if(this.validateSelection()){
                              return true;
                          }else{
                              Ext.Msg.show({
                                 title: "Tab Selection",
                                 msg: "Please select at least one Gear Type and one Species",
                                 buttons: Ext.Msg.OK,
                                 icon: Ext.MessageBox.WARNING
                              });
                              
                              return false;
                          }
                      }
                  }, 
                  tabChange: function(tabPanel, panel){
                      
                      if(panel.items.first() instanceof AnimationPanel){
                      
                          //
                          // Disabling map sliders
                          //
                          Ext.getCmp('quarter-slider').disable(); 
                          Ext.getCmp('years-slider').disable();       
                          
                          Ext.getCmp("quarter-max-button").disable(); 
                          Ext.getCmp("quarter-min-button").disable(); 
                          Ext.getCmp("year-min-largestep").disable(); 
                          Ext.getCmp("year-min-littlestep").disable(); 
                          Ext.getCmp("year-max-littlestep").disable(); 
                          Ext.getCmp("year-max-largestep").disable();
                         
                          Ext.getCmp("last-year").disable(); 
                          Ext.getCmp("first-year").disable();                             
                      }else{
                      
                          //
                          // Enabling map sliders
                          //
                          Ext.getCmp('quarter-slider').enable(); 
                          Ext.getCmp('years-slider').enable();       
                          
                          Ext.getCmp("quarter-max-button").enable(); 
                          Ext.getCmp("quarter-min-button").enable(); 
                          Ext.getCmp("year-min-largestep").enable(); 
                          Ext.getCmp("year-min-littlestep").enable(); 
                          Ext.getCmp("year-max-littlestep").enable(); 
                          Ext.getCmp("year-max-largestep").enable();
                          Ext.getCmp("last-year").enable(); 
                          Ext.getCmp("first-year").enable();                              
                      }
                      
                      activeTab = panel.items.first();
                  }
               },        
               items: [
                  {
                      xtype: 'panel',
                      title: 'Query parameters',
                      iconCls: 'controls-tab',
                      id: 'controls-panel',
                      width: 770, //730,
                      items: [
                          {
                              xtype: 'panel',
                              //width: 730,
                              height: 200,
                              border: false,
                              autoScroll: true,
                              buttons: [
                                  new Ext.Button({
                                      id: 'map-button',
                                      text: 'Map',
                                      iconCls: 'map-button-img',
                                      handler: function(){                                              
                                          if (validateMap()) {
						                                 expandPan();
                                          }else{
                                              if(selectedSpecies.length < 1){
                                                  document.getElementById('species').value = -1;
                                              }
                                              
                                              if(selectedGears.length < 1){
                                                  document.getElementById('gearType').value = -1;
                                              }
                                              
                                              document.getElementById('tabspecies').innerHTML = "";
                                              document.getElementById('tabgears').innerHTML = "";
                                              document.getElementById('tabquarters').innerHTML = "";
                                              document.getElementById('tabperiod').innerHTML = "";
                                              document.getElementById('tabtitle').innerHTML = "";
                                              
                                              Ext.getCmp('info-panel').collapse();
                                              
                                              document.getElementById('nodelist').innerHTML = '';
                                              document.getElementById('brkdwn_nodelist').innerHTML = '';	
                                          }
                                      }
                                  }),
                                  new Ext.Button({
                                      id: 'print-button',
                                      text: 'Print',
                                      disabled: true,
                                      iconCls: 'print-button-img',
                                      handler: function(){                                              
                                          if (validateMap()) {
                                              var href = location.href;
                                              if(href.indexOf("#") != -1){
                                                  href = href.replace("#", "");
                                              }
                                              
                                              var layer1 = myMap.layers[1].visibility;
                                              var layer2 = myMap.layers[2].visibility;
                                              var layer3 = myMap.layers[3].visibility;
                                              
                                          if(params === undefined){
                                          	var printBaseURL = href + 'print.html?' + getViewParams() + '&' + myMap.getExtent().left + '&' + myMap.getExtent().bottom + '&' + myMap.getExtent().right + '&' + myMap.getExtent().top + '&' + layer1 + '&' + layer2 + '&' + layer3 ;
                                          }else{
                                              var printBaseURL = href + 'print.html?' + getViewParams() + '&' + myMap.getExtent().left + '&' + myMap.getExtent().bottom + '&' + myMap.getExtent().right + '&' + myMap.getExtent().top + '&' + layer1 + '&' + layer2 + '&' + layer3 + '&' + params.BBOX + '&' + params.X + '&' + params.Y + '&' + params.WIDTH + '&' + params.HEIGHT + '&' + brkdwn_params.BBOX + '&' + brkdwn_params.X + '&' + brkdwn_params.Y + '&' + brkdwn_params.WIDTH + '&' + brkdwn_params.HEIGHT  ;
                                              }
                                              
                                              window.open(printBaseURL);
                                          }
                                      }
                                  })
                              ],
                              html : [
                                '<table  class="gfi" border="0" cellpadding="0" cellspacing="0" style="width:650px;">',
							                  '<tr>',
                                '<td colspan="4"><b>Select</b></td>',
                                '<td><b>Aggregation method</b></td>',
							                  '</tr>',
                                '<tr>',
                                '<td style="width:70px; vertical-align:top;"> ',
                                '<select id="gearType">',
                                '<option value="-1">Gear Types</option>',
                                '</select>',
                                '</td>',
                                '<td style="width:20px; vertical-align:top;">',
                                '<div id="gearslist"></div>',
                                '</td>',
                                '<td style="width:70px; vertical-align:top;">',
                                '<select id="species">',
                                '<option value="-1">Species</option>',
                                '</select>',
                                '</td>',
                                '<td style="width:20px; vertical-align:top;">',
                                '<div id="specieslist"></div>',
                                '</td>',
                                '<td >',
				                        '<input type="radio" name="statistics" id="sum" checked="checked" /><label for="sum">Sum across years</label> <br />',
                                '<input type="radio" name="statistics" id="avg" /><label for="avg">Average across years</label> <br />',
								                '</td>',
								                '</tr>',
                                '<tr>',
                                '<td style="vertical-align:top;">',
                                '<div id="selectedGearType"></div>',
                                '</td>',
                                '<td style="vertical-align:top;">',
                                '</td>',
                                '<td style="vertical-align:top;">',
                                '<div id="selectedSpecies" ></div>',
                                '</td>',
								                '<td>&#160;</td>',
                                '<td style="vertical-align:top; align:center;">',
                                '</td>',
								                '</tr>',
                                '</table>'].join('')
                          },
                          {
                              xtype: 'panel',
                              height: 273,
                              //width: 730,
                              autoScroll: true,
                              html : [
                                  '<table class="gfi" border="0" cellpadding="0" cellspacing="0">',
                                  '<tr>',
								                  '<td rowspan="6" style="vertical-align: top; width: 150px;"><b>Legend</b> <br/>Catches (tonnes) <br /><br />',
                                  '<div id="legendTunaAtlas">',
                                  '</div>',
                                  '</td>',
                                  '<td style="vertical-align: top;"><b>Cell info</b><br /><i>Click one cell on the map to get more info</i><br /><br /></td>',
                                  '</tr>',
								                  '<tr>',
                                  '<td style="vertical-align: top;"><b><i>Aggregated catches</i></b></td>',
                                  '</tr>',                                 
                                  '<tr>',
                                  '<td>',
                                  '<div id="nodelist"></div>',
                                  '</td>',
                                  '</tr>',
                                  '<tr>',
                                  '<td>',
                                  '<b><i>Catches composition</i></b>',
                                  '</td>',
                                  '</tr>',
                                  '<tr>',
                                  '<td>',
                                  '<div id="brkdwn_nodelist"></div>',
                                  '</td>',
                                  '</tr>',
                                  '</table>'
                                ].join('')
                          }
                   ]
                 },{
                    xtype: 'panel',
                    border: false,
                    width: 790, //730,
                    title: 'Animations',
                    iconCls: 'animation-tab',
                    items: [
                       new AnimationPanel({
                          width: 790, //730,
                          id: 'animation-panel',
                          map: myMap
                       })
                    ]
                 }
               ]
           }
        ]
    });
    
    var isIE9 = Ext.isIE && (/msie 9/.test(navigator.userAgent.toLowerCase())) && document.documentMode != 6;
    
    if(!isIE9){
        //
        // Setting sliders fields tooltips
        //
        new Ext.ToolTip({
            target: 'years-max-field',
            html: "Years Range Max limit"
        });
        
        new Ext.ToolTip({
            target: 'years-min-field',
            html: "Years Range Min limit"
        });
        
        new Ext.ToolTip({
            target: 'quarter-min-field',
            html: "Min limit of the quarters"
        });
        
        new Ext.ToolTip({
            target: 'quarter-max-field',
            html: "Max limit of the quarters"
        });

        new Ext.ToolTip({
            target: 'years-slider',
            html: "Allows to set the years range"
        });
            
        new Ext.ToolTip({
            target: 'quarter-slider',
            html: "Move to define the quarters range"
        });
    }
    
    document.getElementById('specieslist').innerHTML = "<img src=\"images/grid-loading.gif\">";   
    var speciesURL = Tuna.speciesURL;
    Ext.Ajax.request({
       url: speciesURL,
       method: 'GET',
       timeout: Tuna.http_timeout,
       success: function(response, opts){
            document.getElementById('specieslist').innerHTML = "";
            var json = Ext.decode( response.responseText );
            
            var size = json.length;
            for(var i=0; i<size; i++){
                var option = document.createElement("option"); 
                option.value = json[i].ficItem;
                option.innerHTML = json[i].name;
                document.getElementById("species").appendChild(option); 
            }
       },
       failure:  function(response, opts){
            document.getElementById('specieslist').innerHTML = "";
            Ext.Msg.show({
               title: "Species Types Loading",
               msg: "An error occurred while loading Species Types: " + response.status,
               buttons: Ext.Msg.OK,
               icon: Ext.MessageBox.ERROR
            });
       }
    });
    
    document.getElementById('gearslist').innerHTML = "<img src=\"images/grid-loading.gif\">";
    var gearsURL = Tuna.gearsURL;
    Ext.Ajax.request({
       url: gearsURL,
       method: 'GET',
       timeout: Tuna.http_timeout,
       success: function(response, opts){
            document.getElementById('gearslist').innerHTML = "";
            var json = Ext.decode( response.responseText );
            
            var size = json.length;
            for(var i=0; i<size; i++){
                var option = document.createElement("option"); 
                option.value = json[i].gearType;
                option.innerHTML = json[i].name;
                document.getElementById("gearType").appendChild(option);
            }
       },
       failure:  function(response, opts){
            document.getElementById('gearslist').innerHTML = "";
            Ext.Msg.show({
               title: "Gears Types Loading",
               msg: "An error occurred while loading Gears Types: " + response.status,
               buttons: Ext.Msg.OK,
               icon: Ext.MessageBox.ERROR
            });
       }
    });
    
    /*$('#species').click(function () {
        var childs = document.getElementById("species").childNodes.length;
        
        if(childs == 1){
            document.getElementById('specieslist').innerHTML = "<img src=\"images/grid-loading.gif\">";
        
            var url = Tuna.speciesURL;
            Ext.Ajax.request({
               url: url,
               method: 'GET',
               timeout: Tuna.http_timeout,
               success: function(response, opts){
                    document.getElementById('specieslist').innerHTML = "";
                    var json = Ext.decode( response.responseText );
                    
                    var size = json.length;
                    for(var i=0; i<size; i++){
                        var option = document.createElement("option"); 
                        option.value = json[i].ficItem;
                        option.innerHTML = json[i].name;
                        document.getElementById("species").appendChild(option); 
                    }
               },
               failure:  function(response, opts){
                    Ext.Msg.show({
                       title: "Species Types Loading",
                       msg: "An error occurred while loading Species Types: " + response.status,
                       buttons: Ext.Msg.OK,
                       icon: Ext.MessageBox.ERROR
                    });
               }
            });
        }
    });
    
    $('#gearType').click(function () {
        var childs = document.getElementById("gearType").childNodes.length;
        
        if(childs == 1){
            document.getElementById('gearslist').innerHTML = "<img src=\"images/grid-loading.gif\">";

            var url = Tuna.gearsURL;
            Ext.Ajax.request({
               url: url,
               method: 'GET',
               timeout: Tuna.http_timeout,
               success: function(response, opts){
                    document.getElementById('gearslist').innerHTML = "";
                    var json = Ext.decode( response.responseText );
                    
                    var size = json.length;
                    for(var i=0; i<size; i++){
                        var option = document.createElement("option"); 
                        option.value = json[i].gearType;
                        option.innerHTML = json[i].name;
                        document.getElementById("gearType").appendChild(option);
                    }
               },
               failure:  function(response, opts){
                    Ext.Msg.show({
                       title: "Gears Types Loading",
                       msg: "An error occurred while loading Gears Types: " + response.status,
                       buttons: Ext.Msg.OK,
                       icon: Ext.MessageBox.ERROR
                    });
               }
            });
        }
    });*/
    
    $('#avg').change(function () {   
        Ext.getCmp('print-button').disable();
    });
    
    $('#sum').change(function () {   
        Ext.getCmp('print-button').disable();
    });
	
    $('#gearType').change(function () {
        var selection = $('#gearType :selected').text();
        
        if ($(this).val() >= 0 ) {    
            $('#gearType :selected').attr('disabled','disabled');
            
            if (selection != '') {
                if (selection.length > 15) selection = selection.substring(0, 15) + "...";
                $("<div >" + 
                    "<table style='background-color:#CEDFF5; border:1px solid #15428B; margin-top:5px; font-size:12px;' cellpadding='0' cellspacing='0' width='100%'>" +
                	"<tr>" +
                	    "<td>" + 
                		"<span style='color:#15428B; font-weight:bold; margin-left:5px;' value='" + $(this).val() + "'>" + selection + "</span>" + 
                            "</td>" +
                            "<td width='13'>" +
                		"<a href='#' onclick='removeMe(this, \"gearType\", \"" + selection + "\");' title='Remove item'>X</a>" + 
                	    "</td>" +
                	    "<td width='13'>" +
                		"<a href='http://www.geo-solutions.it' onclick='' title='Info'>I</a>" +
                	    "</td>" +
                	"</tr>" + 
                     "</table>" +
                  "</div>").appendTo($('#selectedGearType'));
                selectedGears.push(selection);
            }
        }
        Ext.getCmp('print-button').disable();
    });

    $('#species').change(function () {
        var selection = $('#species :selected').text();
        
        if ($(this).val() >= 0 ) {
            $('#species :selected').attr('disabled','disabled');
            
            if (selection != '') {
                if (selection.length > 15) selection = selection.substring(0, 15) + "...";
                $("<div>" + 
                    "<table style='background-color:#CEDFF5; border:1px solid #15428B; margin-top:5px; font-size:12px;' cellpadding='0' cellspacing='0' width='100%'>" +
                	"<tr>" +
                	    "<td>" + 
                		"<span style='color:#15428B; font-weight:bold; margin-left:5px;' value='" + $(this).val() + "'>" + selection + "</span>" + 
                            "</td>" +
                            "<td width='13'>" +
                		"<a href='#' onclick='removeMe(this, \"species\", \"" + selection + "\");' title='Remove item'>X</a>" + 
                	    "</td>" + 
                	    "<td width='13'>" +
                		"<a href='http://www.geo-solutions.it' onclick='' title='Info'>I</a>" +
                	    "</td>" +
                	"</tr>" + 
                     "</table>" +
                  "</div>").appendTo($('#selectedSpecies'));
                selectedSpecies.push(selection);	
            }
        }
        
        Ext.getCmp('print-button').disable();
    });
	
    var getViewParams = function(){
        // //////////////////////////////////////////////////////////////////////////////////
        // VIEWPARAMS Example:
        //    viewparams: 'FIC_ITEM:2494\,2498;CD_GEAR:802\,803\,805;YR_TA:1960\,1961\,1962' 
        // //////////////////////////////////////////////////////////////////////////////////
        
        var cd_gear =  'CD_GEAR:';
        
        for(g = 0; g < $('#selectedGearType').find('tr').length; g++) {
        
            var selected = $($('#selectedGearType').find('tr')[g]).contents()[0];
            
            var value = $($(selected).contents()[0]).attr('value');
            
            cd_gear += value + (g < $('#selectedGearType').find('tr').length - 1 ? '\\,' : '');
        }
        
        var fic_item = 'FIC_ITEM:';

        for(g = 0; g < $('#selectedSpecies').find('tr').length; g++) {
            var selected = $($('#selectedSpecies').find('tr')[g]).contents()[0];
            var value = $($(selected).contents()[0]).attr('value');
            fic_item += value + (g < $('#selectedSpecies').find('tr').length - 1 ? '\\,' : '');
        }		
        
        var years = Ext.getCmp('years-slider');	
        var yr_start = years.getValues()[0];
        var yr_end = years.getValues()[1];
        
        var yr_ta = 'YR_TA:';
          
        for(y = 0; y <= (yr_end - yr_start); y++) {
            yr_ta += (yr_start + y) + (y <= (yr_end - yr_start)-1 ? '\\,' : '');
        }
        
        var quarters = Ext.getCmp('quarter-slider');	
        var qt_start = quarters.getValues()[0];
        var qt_end = quarters.getValues()[1];
        
        var qt_ta = 'QTR_TA:';
        
        for(var q = 0; q <= (qt_end - qt_start ); q++) {
            qt_ta += (qt_start + q) + (q <= (qt_end - qt_start)-1 ? '\\,' : '');
        }
        
        var statistic = 'OP:' +  ($('#avg').attr("checked") ? 'avg' : 'sum');
        
        var viewparams =  [cd_gear, fic_item, yr_ta, qt_ta, statistic].join(';');
        
        return viewparams;
    };
      
    function issueUpdate(){
    	
        Ext.getCmp('quarter-slider').disable(); 
        Ext.getCmp('years-slider').disable();
        
        Ext.getCmp("quarter-max-button").disable(); 
        Ext.getCmp("quarter-min-button").disable(); 
        Ext.getCmp("year-min-largestep").disable(); 
        Ext.getCmp("year-min-littlestep").disable(); 
        Ext.getCmp("year-max-littlestep").disable(); 
        Ext.getCmp("year-max-largestep").disable();
        Ext.getCmp("last-year").disable(); 
        Ext.getCmp("first-year").disable();  
          
        tuna_map.mergeNewParams({'viewparams':getViewParams()});

        var legendParams = [
          "REQUEST=GetLegendGraphic",
          "LAYER=fifao:TUNA_YEARLY_CATCHES",
          "WIDTH=20",
          "HEIGHT=20",
          "format=image/png",
          "viewparams=" + getViewParams()
        ];
        
        document.getElementById('legendTunaAtlas').innerHTML = 
          '<img alt="" src="' + Tuna.vars.wms + '?' + legendParams.join('&')+'">';
          
        document.getElementById('nodelist').innerHTML = '';
        document.getElementById('brkdwn_nodelist').innerHTML = '';
        	
        //params = "null";
        //brkdwn_params = "null";
        
        //
        // Filling map information table 
        // 
        document.getElementById('tabspecies').innerHTML = selectedSpecies.join(',');
        document.getElementById('tabgears').innerHTML = selectedGears.join(',');
        
        var quarters = Ext.getCmp('quarter-slider');	
        var qt_start = quarters.getValues()[0];
        var qt_end = quarters.getValues()[1];
        
        var qt_ta = '';
        
        for(var q = 0; q <= (qt_end - qt_start ); q++) {
          qt_ta += (qt_start + q) + (q <= (qt_end - qt_start)-1 ? ',' : '');
        }
        
        document.getElementById('tabquarters').innerHTML = qt_ta;
        
        var years = Ext.getCmp('years-slider');	
        var yr_start = years.getValues()[0];
        var yr_end = years.getValues()[1];
        
        var yr_ta = '';
          
        for(y = 0; y <= (yr_end - yr_start); y++) {
          yr_ta += (yr_start + y) + (y <= (yr_end - yr_start)-1 ? ',' : '');
        }
        
        document.getElementById('tabperiod').innerHTML = yr_ta;
        
        var statistic = ($('#avg').attr("checked") ? 'Average' : 'Cumulative') + " Tuna Yearly Catches";
        
        document.getElementById('tabtitle').innerHTML = statistic;
        
        Ext.getCmp('print-button').enable();
    }    

    function expandPan(){
        var infoPanel = Ext.getCmp('info-panel');
        var mainPanel = Ext.getCmp('main-panel');
        var controlPanel = Ext.getCmp('controls-panel');
        var tabpanel = Ext.getCmp('tab-panel');
        var panelMap = Ext.getCmp('gxmappanel');
                    
        if(infoPanel.collapsed)
            infoPanel.expand();
        if(Ext.isIE7){
            mainPanel.syncSize();
        }else{
            controlPanel.syncSize();
            tabpanel.syncSize();
        }
        
        issueUpdate();		
    }
});

function validateMap() {
    if ($('#avg')[0].checked) {
        var years = Ext.getCmp('years-slider');
        
        if (years.getValues()[0] == years.getValues()[1]){
            Ext.Msg.show({
               title: "Map Draw",
               msg: "Please select at least two years",
               buttons: Ext.Msg.OK,
               icon: Ext.MessageBox.WARNING
            });
            
            return false;
        }
    }
    
    if ((selectedGears.length < 1) || (selectedSpecies.length < 1)) {
        Ext.Msg.show({
           title: "Map Draw",
           msg: "Please select at least one Gear Type and one Species",
           buttons: Ext.Msg.OK,
           icon: Ext.MessageBox.WARNING
        });
        
        return false;myMap = new OpenLayers.Map('map', options);
    }
  
    return true;
}

function validateSelection() {
    if ($('#selectedGearType').find('tr').length == 0 || $('#selectedSpecies').find('tr').length == 0) {
      return false;
    }
  
    return true;
}	
	
function removeMe(sender, type, selection) {
    var value = $(sender.parentNode.parentNode).find('span').attr('value')
    $(sender.parentNode.parentNode.parentNode.parentNode).remove();
    
    $('#'+type+' option[value='+value+']').attr('disabled', '');
          
    if(type.indexOf("species") != -1){
        selectedSpecies.pop(selection);
    }else if(type.indexOf("gearType") != -1){
        selectedGears.pop(selection);
    }
    
    Ext.getCmp('print-button').disable();
}

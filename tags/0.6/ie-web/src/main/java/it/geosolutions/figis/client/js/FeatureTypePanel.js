/**
 * @constructor
 */
Ext.namespace('Ext.ux');
Ext.ux.FeatureTypePanel = function(config) {

	var defaults = {
		width : 160
	};
	var defaultType = 'textfield';
        
	var name = new Ext.form.TextField({
		fieldLabel : 'Name',
		disabled: true
	});
	var info = new Ext.form.TextArea({
		fieldLabel : 'Info',
		disabled: true
	});
	
	
    var combo = new Ext.form.ComboBox({
        //store: store,
        //displayField:'name',
        fieldLabel: 'Code Field',
        typeAhead: true,
        mode: 'local',
        forceSelection: true,
        triggerAction: 'all',
        emptyText:'Select a code field...',
        selectOnFocus:true
    });

	
	var items = [ name, info, combo ];

	var setCurrentFeatureType = function(url) {
		var success = function(response) {
			var featureType = Ext.decode(response.responseText).featureType;
			var featureName = featureType.namespace.name + ":"  + featureType.name;
            var attData = [];
            
            if (featureType.attributes != undefined) {
	            var attArray = featureType.attributes.attribute;
	            for (i in attArray){
	                attData[i] = [attArray[i].name];
	            
	            };
            }

            var store = new Ext.data.ArrayStore({
              fields: ['name'],
              data : attData
            });
            combo.store = store;
            combo.displayField = 'name';
			name.setValue(featureName); 
			var infoValue = featureType.title + "\n" + featureType.abstract + "\nKeywords: " + featureType.keywords.string.join(" ");
			info.setValue(infoValue);
		};
		
		Ext.Ajax.request( {
			url : url,
			success : success
		});
	};
    var getFeatureType = function(){
            return name.getValue();
    };
    var getCodeField = function(){
            return combo.getValue();
    }
    
	Ext.ux.FeatureTypePanel.superclass.constructor.call(this, Ext.apply( {
		title: 'FeatureTypePanel',
		defaultType : defaultType,
		defaults : defaults,
		bodyStyle : 'padding:5px 5px 0',
		labelAlign: 'top',
		items : items
	}, config));

    this.getCodeField = getCodeField;  
    this.getFeatureType = getFeatureType;
    this.setCurrentFeatureType = setCurrentFeatureType;
};

Ext.extend(Ext.ux.FeatureTypePanel, Ext.form.FormPanel);

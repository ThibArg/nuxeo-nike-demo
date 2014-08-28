/* main.js
*/
// Cf NikeRoot/index.ftl:
var kDIV_RESULTS = "queryResults",
	kDIV_BOXES_SEASONS = "cb_Seasons",
	kDIV_BOXES_EVENTS = "cb_Events",
	kDIV_BOXES_CATEGORIES = "cb_Categories",
	kDIV_BOXES_KEYWORDS = "cb_Keywords",
	kDIV_BOXES_WIDTHRANGE = "cb_WidthRange",
	kDIV_BOXES_HEIGHTRANGE = "cb_HeightRange",
	kRADIO_OR = "radioOR",
	kRADIO_AND = "radioAND";

var gDivResults,
	gRadioOR, gRadioAND,
	gDivBoxesSeasons,
	gDivBoxesEvents,
	gDivBoxesCategories,
	gDivBoxesKeywords,
	gDivBoxesWidthRange,
	gDivBoxesHeightRange,
	gNuxeoClient;

var kCHECKBOX_TEMPLATE = '<div class="oneCheckboxDiv">'
						+ '<input id="THE_ID" type="checkbox" name="THE_NAME" value="THE_VALUE" checked="checked" class="oneCheckbox">'
						+ '<label for="THE_ID">THE_TITLE</label>'
						+ '</div>';

// The SQL aggregation only handles proxies, not version or other factes
// Let's do the same so the results are accurate
var kNXQL_TEMPLATE = "SELECT * FROM Document"
					+ " WHERE ecm:mixinType = 'Asset'"
					  //+ " AND ecm:mixinType != 'HiddenInNavigation'"
					  //+ " AND ecm:currentLifeCycleState != 'deleted'"
					  + " AND ecm:isVersion = 0"
					  + " AND ecm:isProxy = 0"
					  + " AND (CHECKBOXES_QUERY)"
					  + " ORDER BY dc:modified";

var kONE_DOC_TEMPLATE = '<div class="oneDoc">'
						  + '<div>'
							  //+ '<img style="display: block;margin-left: auto;margin-right: auto;max-width: 140px;"'
							    + '<img class="oneDocThumbnail"'
									+ 'src="/nuxeo/nxthumb/default/THE_DOC_ID/blobholder:0/"'
									+ 'alt="(no thumbnail)"} ></img>'
						  + '</div>'
						  //+ '<p></p>'
						  + '<div class="oneDocTitle">'
							  + '<a href="THE_HREF">THE_DOC_TITLE</a>'
						  + '</div>'
						+ '</div>';

jQuery(document).ready(function() {

	gDivResults = jQuery("#" + kDIV_RESULTS);
	gDivBoxesSeasons = jQuery("#" + kDIV_BOXES_SEASONS);
	gDivBoxesEvents = jQuery("#" + kDIV_BOXES_EVENTS);
	gDivBoxesCategories = jQuery("#" + kDIV_BOXES_CATEGORIES);
	gDivBoxesKeywords = jQuery("#" + kDIV_BOXES_KEYWORDS);
	gDivBoxesWidthRange = jQuery("#" + kDIV_BOXES_WIDTHRANGE);
	gDivBoxesHeightRange = jQuery("#" + kDIV_BOXES_HEIGHTRANGE);

	gRadioOR = jQuery("#" + kRADIO_OR);
	gRadioOR.change(orAndBoxChanged);
	gRadioAND = jQuery("#" + kRADIO_AND);
	gRadioAND.change(orAndBoxChanged);

	buildCheckboxes();
});

function orAndBoxChanged() {
	if(gRadioOR.is(':checked')) {
		jQuery(".oneCheckbox").prop("checked", true);
	} else  {
		jQuery(".oneCheckbox").prop("checked", false);
	}
	doTheQuery();
}

function buildCheckboxes() {

	// Request from Nuxeo...
	gNuxeoClient = new nuxeo.Client({
		username: "Administrator",
		password: "Administrator"
	});

	gNuxeoClient.operation("AggregateValuesInBlob")
		  .params({
		  	statsOnWhat: "All"
		  })
		  .execute(function(error, data) {
		  	if(error) {
		  		alert("Error while getting the facets: " + error);
		  	} else {
		  		var obj = JSON.parse(data);

		  		Object.keys(obj).forEach(function(key) {
		  			buildCheckboxesForOne(key, obj[key]);
		  			jQuery("#" + key).on("click", function(inEvt) {
		  				if(inEvt.metaKey && inEvt.altKey) {
		  					// Uncheck all boxes
		  					jQuery(".oneCheckbox").prop("checked", false);
		  				} else if(inEvt.altKey) {
							jQuery("#" + key + " .oneCheckbox").prop("checked", false);
						}
						doTheQuery();
					});
		  		});

		  		if(gRadioOR.is(':checked')) {
		  			// OK, lets all bow checked by default
		  		} else {
		  			// Uncheck them all
		  			jQuery("#" + key + " .oneCheckbox").prop("checked", false);
		  		}

		  		// Do the first query
		  		doTheQuery();
		  	}
		  });

	//var TEST = '{"events":[{"label":"rugby 2014", "count":4},{"label":"event3", "count":3},{"label":"other event", "count":4},{"label":"evt1", "count":3},{"label":"evt2", "count":2}],"categories":[{"label":"cat1", "count":3},{"label":"cat2", "count":2},{"label":"cat3", "count":3},{"label":"Rugby", "count":4},{"label":"other category", "count":4}],"keywords":[{"label":"kw1", "count":4},{"label":"kw2", "count":8},{"label":"kw3", "count":4}]}';
	//var obj = JSON.parse(TEST);

}

// // We receive an array of objects. Each object is a label and the count

function buildCheckboxesForOne(inKey, inData) {
	var mainDiv = null,
		isWidthRange = false,
		isHeightRange = false;

	switch(inKey) {
	case "seasons":
		mainDiv = gDivBoxesSeasons;
		break;

	case "events":
		mainDiv = gDivBoxesEvents;
		break;

	case "categories":
		mainDiv = gDivBoxesCategories;
		break;

	case "keywords":
		mainDiv = gDivBoxesKeywords;
		break;

	case "widthRange":
		mainDiv = gDivBoxesWidthRange;
		isWidthRange = true;
		break;

	case "heightRange":
		mainDiv = gDivBoxesHeightRange;
		isHeightRange = true;
		break;

	default:
		console.error("buildCheckboxesForOne: Unknown key (" + inkey + ")");
	}

	if(mainDiv != null) {
		mainDiv.empty();
		var idPrefix = mainDiv.attr("id");
		var i = 0;
		inData.forEach(function(inCurrent) {
			var box = kCHECKBOX_TEMPLATE,
				title = inCurrent.label;
			i += 1;
			var cbId = idPrefix + i;
			if(isWidthRange || isHeightRange) {
				switch(title) {
				case "300":
					title = "< 300";
					break;
				case "600":
					title = "300-599";
					break;
				case "900":
					title = "600-899";
					break;
				case "1000":
					title = ">= 900";
				}
			}
			box = box.replace(/THE_ID/g, cbId)
					 .replace(/THE_NAME/g, inKey)
					 .replace(/THE_VALUE/g, inCurrent.label)
					 .replace(/THE_TITLE/g, title + " <span class='cbLabelCount'>(" + inCurrent.count + ")</span>");
			mainDiv.append(box);
			jQuery("#" + cbId).change(doTheQuery);
		});
	}
}
function doTheQuery() {
	var cbQuery = "", queryStr;
	var doOR = gRadioOR.is(':checked');
	var checkedBoxes = jQuery('.oneCheckbox:checkbox:checked');

	checkedBoxes.each(function(){
		cbQuery += doOR ? " OR " : " AND ";
		switch(this.name) {
		case "seasons":
			cbQuery += "ac:season = '" + this.value + "'";
			break;

		case "events":
			cbQuery += "ac:event = '" + this.value + "'";
			break;

		case "categories":
			cbQuery += " ac:category = '" + this.value + "'";
			break;

		case "keywords":
			cbQuery += " ac:keywords = '" + this.value + "'";
			break;

		case "widthRange":
			cbQuery += " ac:width_range = " + this.value;
			break;

		case "heightRange":
			cbQuery += " ac:height_range = " + this.value;
			isHeightRange = true;
			break;
		}
	});
	// Remove first OR/AND
	if(cbQuery == "") {
		// All boxes unchecked and AND => find everything. Else, find nothing
		if(doOR) {
			cbQuery = "ac:event = '" + Math.random() + "'";
		} else {
			// everything
			cbQuery = "ecm:uuid != ''";
		}
	} else {
		cbQuery = doOR ? cbQuery.replace("OR", "") : cbQuery.replace("AND", "");
	}

	if(!doOR) {
		//cbQuery += " AND ecm:mixinType != 'HiddenInNavigation' AND ecm:currentLifeCycleState != 'deleted'";
	}

	queryStr = kNXQL_TEMPLATE.replace("CHECKBOXES_QUERY", cbQuery);
	//console.log(queryStr);
	gNuxeoClient.request("path///@search?query=" + queryStr + "&pageSize=200")
				.get(function(error, data) {
					if(error) {
						alert("Error fetching the documents: " + error);
					} else {
						displayResults(data);
					}
				});

}

function displayResults(inData) {
	gDivResults.empty();
	//gDivResults.text("Found: " + inData.resultsCount);
	inData.entries.forEach(function(oneDoc) {
		var div;//"THE_DOC_ID", "THE_DOC_TITLE"

		div = kONE_DOC_TEMPLATE.replace(/THE_DOC_ID/g, oneDoc.uid)
							   .replace(/THE_DOC_TITLE/g, oneDoc.title)
							   .replace(/THE_HREF/g, "/nuxeo/nxdoc/default/" + oneDoc.uid + "/view_documents");
		gDivResults.append(div);
	});
}

// --EOF--


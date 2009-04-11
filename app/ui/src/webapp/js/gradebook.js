function clearIfDefaultString(formField, defaultString) {
	if(formField.value == defaultString) {
		formField.value = "";
	}
}

// We sometimes want to have a default submit button that's not
// the first one in the form.
//
// USAGE:
//
//   <h:inputText id="Score" value="#{scoreRow.score}"
//     onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
//   <h:commandButton id="saveButton" ... />
//
// It's important to specify "onkeypress" rather than "onkeydown". Otherwise,
// IE will work fine, but Mozilla and Co. will send the key release event
// on to the first button on the form no matter what.
//
function submitOnEnter(event, defaultButtonId) {
	var characterCode;
	if (event.which) {
		characterCode = event.which;
	} else if (event.keyCode) {
		characterCode = event.keyCode;
	}

	if (characterCode == 13) {
		event.returnValue = false;
		event.cancel = true;
		document.getElementById(defaultButtonId).click();
		return false;
	} else {
		return true;
	}
}



/* DHTML windows for grading event display */

var openedWindows = new Array();

function toggleWindow(elmnt, title, text) {
	if(openedWindows[elmnt.id] == null || openedWindows[elmnt.id] == undefined) {
		/* Open the window */
		var newWindow = new dhtmlWindow(5,5,300,200,title,text);
		elmnt.parentNode.appendChild(newWindow);
		openedWindows[elmnt.id] = newWindow;
	} else {
		/*  Close the window */
		var oldOpenedWindow = openedWindows[elmnt.id];
		elmnt.parentNode.removeChild(oldOpenedWindow);
		openedWindows[elmnt.id] = null;
	}
}

/* TODO format the table properly */
function parseText(text) {
	var textArray = text.split('|');
	var html = "<table>\n"
	for(var i=0; i < textArray.length; i++) {
		if((i) % 3 == 0) {
			html += "\t<tr>\n"
		}
		html += "\t\t<td>"
		html += textArray[i]
		html += "</td>\n"
		if((i+1) % 3 == 0 || (i+1) == textArray.length) {
			html += "\t</tr>\n"
		}
	}
	html += "</table>"
	return html
}

function dhtmlWindow(x,y,w,h,title,text){

 var winBody = new divElement(x,y,w,h,"#cccccc")
  winBody.style.borderStyle = "outset"
  winBody.style.borderWidth = "2px"
  winBody.style.borderColor = "#aaaaaa"
  winBody.style.zIndex = (dhtmlWindow.zCount++)
  
 
 var toolBar = new divElement(4,4,w-14,18,"#006699")
  toolBar.style.position = "absolute"
  toolBar.style.color = "#ffffff"
  toolBar.style.fontFamily = "arial"
  toolBar.style.fontSize = "10pt"
  toolBar.style.paddingLeft="4px"
  
  toolBar.proxyFor = winBody
 
 var contentArea = new divElement(4,26,w-10,h-40,"#ffffff")
  if (document.all) contentArea.style.width = (parseInt(contentArea.style.width)-4)+"px"
  else contentArea.style.width = (parseInt(contentArea.style.width)-7)+"px"
  contentArea.style.borderColor="#cccccc"
  contentArea.style.borderStyle="inset"
  contentArea.style.borderWidth="1px"
  contentArea.style.overflow="auto"
  contentArea.style.paddingLeft="4px"
  contentArea.style.paddingRight="2px"
  contentArea.style.fontFamily = "arial"
  contentArea.style.fontSize = "10pt"
  winBody.content = contentArea;

 var titleDiv = document.createElement("div")
 titleDiv.appendChild(document.createTextNode(title));
 
 contentArea.innerHTML = parseText(text)

 winBody.appendChild(contentArea)
 toolBar.appendChild(titleDiv)
 winBody.appendChild(toolBar)
 return winBody

}

dhtmlWindow.zCount=0;

function divElement (x,y,w,h,col){
	var lyr = document.createElement("div")
 	 lyr.style.position = "relative"
	 lyr.style.left = x + "px"
	 lyr.style.top = y + "px"
	 lyr.style.width = w + "px"
	 lyr.style.height = h + "px"
	 lyr.style.backgroundColor = col
	 lyr.style.visibility = "visible"
	 lyr.style.padding= "0px 0px 0px 0px"
	return lyr
}

function getTheElement(thisid)
{
  var thiselm = null;
  if (document.getElementById) {
    thiselm = document.getElementById(thisid);
  } else if (document.all) {
    thiselm = document.all[thisid];
  } else if (document.layers) {
    thiselm = document.layers[thisid];
  }

  if(thiselm) {
    if(thiselm == null) {
      return;
    } else {
      return thiselm;
    }
  }
}

// Update the running total
function updateRunningTotal(thisForm) {
	var runningTotal = 0.0;
	var adjustmentTotal = 0.0;
	
  for (var i=0; i < thisForm.elements.length; ++i) {
  	formElement = thisForm.elements[i];
    elementName = formElement.name;
    var elementNamePieces = elementName.split(":");
    var highlightTotal = true;

    if (elementNamePieces[3] == "weightInput") {
    	// probably a better way to get the adjustment checkbox, but this works
    	var adjustmentValEl = getTheElement(elementNamePieces[0] + ":" + elementNamePieces[1] + ":" + elementNamePieces[2] + ":adjustment");
        weight = parseFloat(formElement.value);
        if (weight >= 0)
        {
        	if (undefined != adjustmentValEl)
        	{
   				if (adjustmentValEl.checked == true)
   				{
           			adjustmentTotal += weight;
           		}
           	}
            runningTotal += weight;
        }
    }
  }
  
  var neededTotal = 100.0 - runningTotal + adjustmentTotal;

  var runningTotalValEl = getTheElement(thisForm.name + ":runningTotalVal");
  var runningTotalEl = getTheElement(thisForm.name + ":runningTotal");
  var neededTotalEl = getTheElement(thisForm.name + ":neededTotalVal");
  runningTotalValEl.innerHTML = runningTotal;
  neededTotalEl.innerHTML = neededTotal;
  if (neededTotal == 0)
  	runningTotalEl.className="courseGrade";
  else
  	runningTotalEl.className = "highlight courseGrade";
}

// for toggling display of gradebook items associated with a category
function showHideDiv(hideDivisionNo, context, expandAlt, collapseAlt, expandTitle, collapseTitle)
{
  var tmpdiv = hideDivisionNo + "__hide_division_";
  var tmpimg = hideDivisionNo + "__img_hide_division_";
  var divisionNo = getTheElement(tmpdiv);
  var imgNo = getTheElement(tmpimg);

  if(divisionNo)
  {
    if(divisionNo.style.display =="block" || divisionNo.style.display =="table-row")
    {
      divisionNo.style.display="none";
      if (imgNo)
      {
        imgNo.src = context + "/images/collapse.gif";
        imgNo.alt = collapseAlt;
        imgNo.title = collapseTitle;
      }
    }
    else
    {
      if(navigator.product == "Gecko")
      {
        divisionNo.style.display="table-row";
      }
      else
      {
        divisionNo.style.display="block";
      }
      if(imgNo)
      {
        imgNo.src = context + "/images/expand.gif";
        imgNo.alt = expandAlt;
        imgNo.title = expandTitle;
      }
    }
  }
}

// for toggling all gradebook items displayed within a category
function showHideAll(numToggles, context, expandAlt, collapseAlt, expandTitle, collapseTitle)
{
  var allimg = "expandCollapseAll";
  var imgAll = getTheElement(allimg);
  var imgAllSrcPieces = imgAll.src.split("/");

  var expanded = false;
	if (imgAllSrcPieces[(imgAllSrcPieces.length - 1)] == "expand.gif")
	 	expanded = true;
	 	
	for (var i=0; i < numToggles; i++) {
	  var tmpdiv = "_id_" + i + "__hide_division_";
	  var tmpimg = "_id_" + i + "__img_hide_division_";
	  var divisionNo = getTheElement(tmpdiv);
	  var imgNo = getTheElement(tmpimg);
	
	  if(divisionNo)
	  {
	  	if (expanded) {
		    divisionNo.style.display="none";
		    
		    if (imgNo) {
		      imgNo.src = context + "/images/collapse.gif";
		      imgNo.alt =  collapseAlt;
		      imgNo.title = collapseTitle;
		    }
		    if (imgAll) {
		      imgAll.src = context + "/images/collapse.gif";
		      imgAll.alt =  collapseAlt;
		      imgAll.title = collapseTitle;
		    }
		  }
		  else {
		    if(navigator.product == "Gecko")
      	{
        	divisionNo.style.display="table-row";
      	}
      	else
      	{
        	divisionNo.style.display="block";
     	 	}
		    
		    if (imgNo) {
		      imgNo.src = context + "/images/expand.gif";
		      imgNo.alt =  expandAlt;
		      imgNo.title = expandTitle;
		    }

		    if (imgAll) {
		    	imgAll.src = context + "/images/expand.gif";
		    	imgAll.alt =  expandAlt;
		      imgAll.title = expandTitle;
		    }
		  }
	  }
  }
}

// if user unchecks box to release items, we must uncheck
// and disable the option to include item in cumulative score
// we will also add check the counted box if ungraded is false or undefined
function assignmentReleased(myForm, releasedChanged) {
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var countedCheckboxEl =   getTheElement(myForm + ':countAssignment');
	var ungradedCheckboxEl =  getTheElement(myForm + ':ungraded');
	
	if (undefined != releasedCheckboxEl)
	{
		if (releasedCheckboxEl.checked == false) {
			if (undefined != countedCheckboxEl)
			{
				countedCheckboxEl.checked = false;
				countedCheckboxEl.disabled = true;
			}
		} else if (releasedCheckboxEl.checked == true) {
			if (undefined != countedCheckboxEl)
			{
				countedCheckboxEl.disabled = false;
				if (undefined != ungradedCheckboxEl)
				{
					if (ungradedCheckboxEl == true)
					{
						// extra insurance its off when it should be
						countedCheckboxEl.checked = false;
					}
					else
					{
						countedCheckboxEl.checked = true;
					}
				}
				else
				{
					// if the ungraded checkbox does not exist
					countedCheckboxEl.checked = true;
				}	
			}
		}
	}
}

// similar as above, but we want to insure the counted checkbox
// is unchecked and disabled for an checked non-calculating item
function assignmentUngraded(myForm) {
	var ungradedCheckboxEl =  getTheElement(myForm + ':ungraded');
	var countedCheckboxEl =   getTheElement(myForm + ':countAssignment');
	var countedLabelEl =   getTheElement(myForm + ':countAssignmentLabel');
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var pointsInputEl =  getTheElement(myForm + ':points');
	var pointsLabelEl =  getTheElement(myForm + ':pointsLabel');
	var categoryInstructionTextEl = getTheElement(myForm + ':nonCalCategoryInstructionText');
	var pointsLabelAst =  getTheElement(myForm + ':pointsLabelAsterisk');
	var pointsLabelEditAst = getTheElement(myForm + ':pointsLabelEditAsterisk');

	if (undefined != ungradedCheckboxEl)
	{
		if (ungradedCheckboxEl.checked == true) {
			if (undefined != countedCheckboxEl)
			{
				countedCheckboxEl.checked = false;
				countedCheckboxEl.disabled = true;
				countedCheckboxEl.style.display="none";
			}
			if (undefined != countedLabelEl)
			{
				countedLabelEl.style.display="none";
			}
			if (undefined != pointsInputEl)
			{
				pointsInputEl.value = "";
				pointsInputEl.style.display="none";
			}
			if (undefined != pointsLabelAst)
			{
				pointsLabelAst.style.display="none";
			}
			if (undefined != pointsLabelEditAst)
			{
				pointsLabelEditAst.style.display="none";
			}
			if (undefined != pointsLabelEl)
			{
				pointsLabelEl.style.display="none";
			}
			categoryInstructionTextEl.style.display="none";
		} else if (ungradedCheckboxEl.checked == false) {
			if (undefined != countedCheckboxEl)
			{
				if (undefined != releasedCheckboxEl)
				{
					if(releasedCheckboxEl.checked){
						countedCheckboxEl.disabled = false;
					}else{
						countedCheckboxEl.disabled = true;
					}
				}else{
					countedCheckboxEl.disabled = false;
				}
				countedCheckboxEl.style.display="inline";
			}
			if (undefined != countedLabelEl)
			{
				countedLabelEl.style.display="inline";
			}
			if (undefined != pointsInputEl)
			{
				pointsInputEl.style.display="inline";
			}
			if (undefined != pointsLabelAst)
			{
				pointsLabelAst.style.display="inline";
			}
			if (undefined != pointsLabelEditAst)
			{
				pointsLabelEditAst.style.display="inline";
			}
			if (undefined != pointsLabelEl)
			{
				pointsLabelEl.style.display="inline";
			}
			categoryInstructionTextEl.style.display="inline";
		}
	}
}

//similar as above, but we want to insure the counted checkbox
//is unchecked and disabled for an checked non-calculating item
function assignmentNonCalc(myForm) {
	var ungradedDropdownEl =  getTheElement(myForm + ':selectGradeEntry');
	var countedCheckboxEl =   getTheElement(myForm + ':countAssignment');
	var countedLabelEl =   getTheElement(myForm + ':countAssignmentLabel');
	var releasedCheckboxEl =  getTheElement(myForm + ':released');
	var pointsInputEl =  getTheElement(myForm + ':points');
	var pointsLabelEl =  getTheElement(myForm + ':pointsLabel');
	var categoryInstructionTextEl = getTheElement(myForm + ':nonCalCategoryInstructionText');
	var pointsLabelAst =  getTheElement(myForm + ':pointsLabelAsterisk');
	var pointsLabelEditAst = getTheElement(myForm + ':pointsLabelEditAsterisk');
	var categoryDropDown1 = getTheElement(myForm + ':selectCategory');
	var categoryDropDown2 = getTheElement(myForm + ':selectCategory2');

	if (undefined != ungradedDropdownEl)
	{
		if (ungradedDropdownEl[ungradedDropdownEl.selectedIndex].value == "Non-calculating") {
			if (undefined != countedCheckboxEl)
			{
				countedCheckboxEl.checked = false;
				countedCheckboxEl.disabled = true;
				countedCheckboxEl.style.display="none";
			}
			if (undefined != countedLabelEl)
			{
				countedLabelEl.style.display="none";
			}
			if (undefined != pointsInputEl)
			{
				pointsInputEl.value = "";
				pointsInputEl.style.display="none";
			}
			if (undefined != pointsLabelAst)
			{
				pointsLabelAst.style.display="none";
			}
			if (undefined != pointsLabelEditAst)
			{
				pointsLabelEditAst.style.display="none";
			}
			if (undefined != pointsLabelEl)
			{
				pointsLabelEl.style.display="none";
			}
			if (undefined != categoryInstructionTextEl)
			{
				categoryInstructionTextEl.style.display="none";
			}
			if (undefined != categoryDropDown2)
			{
				categoryDropDown1.style.display="inline";
				categoryDropDown2.style.display="none";
			}
		} else if (ungradedDropdownEl[ungradedDropdownEl.selectedIndex].value == "Points" || ungradedDropdownEl[ungradedDropdownEl.selectedIndex].value == "Percentage") {
			if (undefined != countedCheckboxEl)
			{
				if (undefined != releasedCheckboxEl)
				{
					if(releasedCheckboxEl.checked){
						countedCheckboxEl.disabled = false;
					}else{
						countedCheckboxEl.disabled = true;
					}
				}else{
					countedCheckboxEl.disabled = false;
				}
				countedCheckboxEl.style.display="inline";
			}
			if (undefined != countedLabelEl)
			{
				countedLabelEl.style.display="inline";
			}
			if (undefined != pointsInputEl)
			{
				pointsInputEl.style.display="inline";
			}
			if (undefined != pointsLabelAst)
			{
				pointsLabelAst.style.display="inline";
			}
			if (undefined != pointsLabelEditAst)
			{
				pointsLabelEditAst.style.display="inline";
			}
			if (undefined != pointsLabelEl)
			{
				if (ungradedDropdownEl.options[0].value == "Percentage")
				{
					pointsLabelEl.innerHTML = "Gradebook Item Relative Weight";
				}
				else if (ungradedDropdownEl.options[0].value == "Points")
				{
					pointsLabelEl.innerHTML = "Gradebook Item Point Value";
				}
				pointsLabelEl.style.display="inline";
			}
			if (undefined != categoryInstructionTextEl)
			{
				categoryInstructionTextEl.style.display="inline";
			}
			if (undefined != categoryDropDown2)
			{
				categoryDropDown1.style.display="inline";
				categoryDropDown2.style.display="none";
			}
		} else if (ungradedDropdownEl[ungradedDropdownEl.selectedIndex].value == "Adjustment") {
				if (undefined != countedCheckboxEl)
				{
					if (undefined != releasedCheckboxEl)
					{
						if(releasedCheckboxEl.checked){
							countedCheckboxEl.disabled = false;
						}else{
							countedCheckboxEl.disabled = true;
						}
					}else{
						countedCheckboxEl.disabled = false;
					}
					countedCheckboxEl.style.display="inline";
				}
				if (undefined != countedLabelEl)
				{
					countedLabelEl.style.display="inline";
				}
				if (undefined != pointsInputEl)
				{
					if (ungradedDropdownEl.options[0].value == "Percentage")
					{
						pointsInputEl.value = "";
						pointsInputEl.style.display="none";
					}
					else
					{
						pointsInputEl.style.display="inline";
					}
				}
				if (undefined != pointsLabelAst)
				{
					pointsLabelAst.style.display="none";
				}
				if (undefined != pointsLabelEditAst)
				{
					pointsLabelEditAst.style.display="inline";
				}
				if (undefined != pointsLabelEl)
				{
					if (ungradedDropdownEl.options[0].value == "Percentage")
					{
						pointsLabelEl.style.display="none";
					}
					else
					{
						pointsLabelEl.innerHTML = "Adjustment Item Point Value";
						pointsLabelEl.style.display="inline";
					}
				}
				if (undefined != categoryInstructionTextEl)
				{
					categoryInstructionTextEl.style.display="inline";
				}
				if (undefined != categoryDropDown2)
				{
					categoryDropDown1.style.display="none";
					syncAdjustmentDropdowns(myForm);
					categoryDropDown2.style.display="inline";
				}
			}
	}

}

function syncAdjustmentDropdowns(myForm) {
	var categoryDropDown1 = getTheElement(myForm + ':selectCategory');
	var categoryDropDown2 = getTheElement(myForm + ':selectCategory2');
	
	if (undefined != categoryDropDown2)
	{
		categoryDropDown1.selectedIndex=categoryDropDown2.options[categoryDropDown2.selectedIndex].value;
	}
}

function adjustmentDropdownOnLoad(myForm) {
	var categoryDropDown1 = getTheElement(myForm + ':selectCategory');
	var categoryDropDown2 = getTheElement(myForm + ':selectCategory2');
	
	if (undefined != categoryDropDown2)
	{
		for (i=0;i<categoryDropDown2.options.length;i++){
			if (categoryDropDown2.options[i].value==categoryDropDown1.options[categoryDropDown1.selectedIndex].value){
				categoryDropDown2.selectedIndex=i;
				return true;
			}
		}
	}
}


//This gets numTotalBulkGradebookItems element so can be manipulated
function addBulkItems(myForm) {
	var numBulkInputEl =  getTheElement(myForm + ':selectBulkGradebookItem');
	var numBulkGradebookItems = parseInt(numBulkInputEl[numBulkInputEl.selectedIndex].value);
	
	if (undefined != numBulkInputEl)
	{
		//TODO
		
	} 
	
}


// if the containing frame is small, then offsetHeight is pretty good for all but ie/xp.
// ie/xp reports clientHeight == offsetHeight, but has a good scrollHeight
function mySetMainFrameHeight(id)
{
	// run the script only if this window's name matches the id parameter
	// this tells us that the iframe in parent by the name of 'id' is the one who spawned us
	if (typeof window.name != "undefined" && id != window.name) return;

	var frame = parent.document.getElementById(id);
	if (frame)
	{

		var objToResize = (frame.style) ? frame.style : frame;

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		//gsilver: changing this from 50 to 10, and adding extra bottom padding to the portletBody		
		var newHeight = height + 150;
		//contributed patch from hedrick@rutgers.edu (for very long documents)
		if (newHeight > 32760)
		newHeight = 32760;

		// no need to be smaller than...
		//if (height < 200) height = 200;
		objToResize.height=newHeight + "px";
	
		var s = " scrollH: " + scrollH + " offsetH: " + offsetH + " clientH: " + clientH + " innerDocScrollH: " + innerDocScrollH + " Read height: " + height + " Set height to: " + newHeight;

	}
}
	
function displayHideElement(myForm, displayId, radioId, radioElementValue) {
	displayElement = getTheElement(myForm.name + ":" + displayId);
	radioElement = getTheElement(myForm.name + ":" + radioId);
	
	var inputs = radioElement.getElementsByTagName ('input');
  for (i=0;i<inputs.length;i++){
    if (inputs[i].checked==true){
      var selection = inputs[i].value;
      if (selection == radioElementValue) 
				 displayElement.style.display="block";
			else
				displayElement.style.display="none";
			
			break;
    }
  }
}


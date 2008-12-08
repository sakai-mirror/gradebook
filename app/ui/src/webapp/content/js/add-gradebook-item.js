var gbItemHelper = gbItemHelper || {};

(function (jQuery, gbItemHelper) {
	/**
	 * This function will deselect and disable the "counted" checkbox if the gb item has not
	 * been marked as "released"
	 */
	gbItemHelper.handleReleaseVsCounted = function(){
		var counted_checkbox = $("input:checkbox[@name='course_grade']").get(0);
		if (counted_checkbox) {
			if(!$("input:checkbox[@name='release']").get(0).checked){
				counted_checkbox.checked = false;
				counted_checkbox.disabled = true;
			}else{
				counted_checkbox.disabled = false;
			}
		}
	};
	
	/**
	 * if item non-calculating, we need to hide points possible and "counted" checkbox
	 */
	gbItemHelper.hideOrDisplayNonCalcInfo = function() {
		var noncalc_checkbox = jQuery("input:checkbox[@name='non-calc']").get(0);
		if (noncalc_checkbox) {
			// hide the points possible and counted in course grade options if checked
			if (noncalc_checkbox.checked) {
				// hide the counted section
				jQuery("#counted").attr("style", "display:none;");
				
				// hide the points possible section
				jQuery("#points-possible").attr("style", "display:none;");
			} else {
				// display the counted section
				jQuery("#counted").removeAttr("style");
				
				// display the points possible section
				jQuery("#points-possible").removeAttr("style");
			}
		}	
	};
	
	/**
	 * if "Require Due Date?" checkbox is checked, display due date entry option
	 */
	gbItemHelper.show_due_date = function() {
		el = jQuery("input:checkbox[@name='require_due_date']").get(0);
		if (el) {
			if (el.checked) {
			    jQuery("#require_due_date_container").show();
			} else {
			    jQuery("#require_due_date_container").hide();
			}
		}
	};

})(jQuery, gbItemHelper);

	
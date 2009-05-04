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
	 * selectively hide/display certain options when the user changes the gradebook item
	 * type (ie points, non-calculating, adjustment item, etc)
	 */
	gbItemHelper.gradeEntryDisplay = function() {
		var grade_entry_select = jQuery("#grade_entry-selection");
		var selected_entry = grade_entry_select.val();
		
		// we have two category selects: one with adjustment cats and one without
		var all_cats_select = jQuery("#all_categories");
		var non_adj_cats_select = jQuery("#non_adj_categories");
		
		if (selected_entry == 'normal') {
			// display the points possible section
			jQuery("#points-possible").removeClass("hideElement");
			// hide the adjustment points possible section
			jQuery("#adjustment-points-possible").addClass("hideElement");
			// display the counted section
			jQuery("#counted").removeClass("hideElement");
			// display the category select w/ all categories
			all_cats_select.removeClass("hideElement");
			non_adj_cats_select.addClass("hideElement");
		} else if (selected_entry == 'non-cal') {
			// hide all points possible
			jQuery("#points-possible").addClass("hideElement");
			jQuery("#adjustment-points-possible").addClass("hideElement");
			// hide the "counted" option
			jQuery("#counted").addClass("hideElement");
			// display the category select w/ all categories
			all_cats_select.removeClass("hideElement");
			non_adj_cats_select.addClass("hideElement");
		} else if (selected_entry == 'adj') {
			// display the "adjustment" points possible section
			jQuery("#adjustment-points-possible").removeClass("hideElement");	
			// hide the normal points possible section
			jQuery("#points-possible").addClass("hideElement");
			// display the counted section
			jQuery("#counted").removeClass("hideElement");
			
			// display the category select w/o adjustment categories
			all_cats_select.addClass("hideElement");
			non_adj_cats_select.removeClass("hideElement");
			
			// if the selected category is an adjustment item, we need to reset 
			// the "all categories" drop-down. the adj category drop down will be -1
			var non_adj_cats_select = jQuery("#nonAdjCategory-selection");
			var sel_category = non_adj_cats_select.val();
			
			if (sel_category == -1) {
				var all_cats_select = jQuery("#category-selection");
				all_cats_select.val(-1);
			}
		}
		
		gbItemHelper.categoryDisplay();
	}
	
	/**
	 * To keep the two point inputs in synch. We render two
	 * because they have different labels, depending upon
	 * the grade entry type
	 */
	gbItemHelper.updatePointValue = function(points) {
		var normal_point_input = jQuery("#point");
		var adj_point_input = jQuery("#adj_point");
		normal_point_input.val(points);
		adj_point_input.val(points);
	}
	
	gbItemHelper.updateCategorySelection = function(selection) {
		var all_cats_select = jQuery("#category-selection");
		var non_adj_cats_select = jQuery("#nonAdjCategory-selection");
		
		all_cats_select.val(selection);
		non_adj_cats_select.val(selection);
	}
	
	/**
	 * handles which category select should display (all cats or just non-adj cats)
	 * and disables points input if required
	 */
	gbItemHelper.categoryDisplay = function() {
		// get the grade entry selection
    	var grade_entry_select = jQuery("#grade_entry-selection");
		var selected_entry = grade_entry_select.val();
		
		// get the selected category (both menus should have the same 
		// value if updateCategorySelection is working properly!)
		var sel_category_id = jQuery("#category-selection").val();
		
		// figure out if we are currently displaying the regular or adj points possible
    	var points_possible_input;
    	var points_possible_label;
		if (selected_entry == 'adj') {
			points_possible_input = jQuery("#adj_point");
			points_possible_label = jQuery("#adj_points_instruction");
		} else {
			points_possible_input = jQuery("#point");
			points_possible_label = jQuery("#points_instruction");
		}
		
		// if there are categories that can't have their points edited,
		// this select menu will be present (though hidden) in the html
		var points_disabled = false;
		var uneditable_cat_points = jQuery("#categories_with_uneditable_points-selection").get(0);
		if (uneditable_cat_points) {
			// depending on which category is selected, we may need to
			// disable the points possible input and display a message
			for(i=0;i<uneditable_cat_points.length;i++){
                if(uneditable_cat_points.options[i].text == sel_category_id){
                	// disable points possible and replace with point value
                	var point_val = uneditable_cat_points.options[i].value;
            		
            		// set the input value and disable it
            		points_disabled = true;
            		points_possible_input.val(point_val);
            		points_possible_input.attr("disabled", "disabled");
            		// show the info message that goes with this scenario
            		points_possible_label.removeClass("hideElement");
            		
            		break;
                }
            }
		}
		
		if (!points_disabled) {
			points_possible_input.removeAttr("disabled");
			points_possible_label.addClass("hideElement");
		}
	}
	
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

	
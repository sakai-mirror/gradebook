/**********************************************************************************
 *
 * $Id: AssignmentPointsConverter.java 20001 2007-04-18 19:41:33Z rjlowe@iupui.edu $
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.Category;

/**
 * This formatting-only converver consolidates the rather complex formatting
 * logic for assignment and assignment grade points. If the points are null,
 * they should be displayed in a special way. If the points belong to an
 * assignment which doesn't count toward the final grade, they should be
 * displayed in a special way with a tooltip "title" attribute.
 */
public class CategoryPointsConverter extends PointsConverter {
	private static final Log log = LogFactory.getLog(AssignmentPointsConverter.class);

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedScore;
		boolean notCounted = false;
		Double studentPoints = 0.0;
		Double categoryPoints = 0.0;
		Category cat = null;
		
		if (value != null) {
			if (value instanceof Map) {
				studentPoints = (Double) ((Map)value).get("studentPoints");
				categoryPoints = (Double) ((Map)value).get("categoryPoints");
				cat = (Category) ((Map)value).get("category");
			}
		}
		//if Category is null, then this is "Unassigned" therefore n/a
		if( cat == null){
			formattedScore = FacesUtil.getLocalizedString("overview_unassigned_cat_avg");
		} else if( cat.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_POINTS){
			//if grade by points 
			formattedScore = 
				super.getAsString(context, component, studentPoints) + "/" +
				super.getAsString(context, component, categoryPoints);
		} else {
			//display percentage
			formattedScore = super.getAsString(context, component,((studentPoints / categoryPoints) * 100)) + "%";
		}
		return formattedScore;
	}
}

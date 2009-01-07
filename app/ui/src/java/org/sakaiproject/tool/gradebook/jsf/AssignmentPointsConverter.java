/**********************************************************************************
 *
 * $Id$
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

import java.io.Serializable;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.Gradebook;

/**
 * This formatting-only converver consolidates the rather complex formatting
 * logic for assignment and assignment grade points. If the points are null,
 * they should be displayed in a special way. If the points belong to an
 * assignment which doesn't count toward the final grade, they should be
 * displayed in a special way with a tooltip "title" attribute.
 * 
 * This converter does not convert numeric values since we want to display
 * what the user has entered exactly.  This converter should only be used to
 * display individual student grades.  See {@link CourseGradeConverter} {@link ClassAvgConverter} {@link PointsConverter}
 * for converting calculations
 */
public class AssignmentPointsConverter implements Converter, Serializable{
	private static final Log log = LogFactory.getLog(AssignmentPointsConverter.class);

	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (log.isDebugEnabled()) log.debug("getAsString(" + context + ", " + component + ", " + value + ")");

		String formattedScore = null;
		boolean notCounted = false;
		boolean ungraded = false;
		Object workingValue = value;
		boolean percentage = false;
		boolean letterGrade = false;
		
		if (value != null) {
			if (value instanceof Assignment) {
				Assignment assignment = (Assignment)value;
				workingValue = assignment.getPointsPossible();
				notCounted = assignment.isNotCounted();
				if (assignment.getGradebook().getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
					letterGrade = true;
				}
				// if weighting enabled, item is not counted if not assigned
				// a category
				if (!notCounted && assignment.getGradebook().getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					notCounted = assignment.getCategory() == null;
				}
				if(assignment.getUngraded()){
					ungraded = true;
				}
			} else if (value instanceof AssignmentGradeRecord) {
				Gradebook gradebook = ((GradableObject)((AbstractGradeRecord)value).getGradableObject()).getGradebook();
				int gradeType = gradebook.getGrade_type();
				AssignmentGradeRecord agr = (AssignmentGradeRecord)value;
				if (agr.isUserAbleToView()) {
					if(gradeType == GradebookService.GRADE_TYPE_POINTS ){
						//if grade by points and no category weighting
						workingValue = ((AbstractGradeRecord)value).getPointsEarned();	
					} else if (gradeType == GradebookService.GRADE_TYPE_LETTER) {
						workingValue = agr.getPointsEarned();
						letterGrade = true;
					} else {
						//display percentage
						percentage = true;
						workingValue = agr.getPointsEarned();
					}
				} else {
					workingValue = " ";
				}

				Assignment assignment = agr.getAssignment();
				notCounted = assignment.isNotCounted();
				// if weighting enabled, item is only counted if assigned
				// a category
				if (!notCounted && assignment.getGradebook().getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
					notCounted = assignment.getCategory() == null;
				}
				
				if(assignment.getUngraded()){
					ungraded = true;
				}

			} else if (value instanceof CourseGradeRecord) {
				// display percentage
				percentage = true;
				workingValue = ((AbstractGradeRecord)value).getGradeAsPercentage();
			}
		}
		
		//we do not want to convert the assignment value since NON-CAL changes.
		//but if value is null, display a "-" (null place holder)
		if(workingValue == null){
			formattedScore = FacesUtil.getLocalizedString("score_null_placeholder");
		}else{
			formattedScore = workingValue.toString();
		}
			
		if (notCounted && letterGrade) {
			formattedScore = FacesUtil.getLocalizedString("score_not_counted",
					new String[] {formattedScore, FacesUtil.getLocalizedString("score_not_counted_tooltip")});
		} else {
			if (notCounted) {
				formattedScore = FacesUtil.getLocalizedString("score_not_counted_with_paren",
					new String[] {formattedScore, FacesUtil.getLocalizedString("score_not_counted_tooltip")});
			}
		}
		if(percentage && workingValue != null && !ungraded){
			formattedScore += "%";
		}
		return formattedScore;
	}

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null) {
			return null;
		}
		return arg2;
	}
}

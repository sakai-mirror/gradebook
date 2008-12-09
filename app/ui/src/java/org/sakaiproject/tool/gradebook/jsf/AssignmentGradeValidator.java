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
import java.math.BigDecimal;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import javax.servlet.http.HttpServletRequest;
import org.sakaiproject.service.gradebook.shared.Grade;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidDecimalGradeException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.GradebookException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeLengthException;
import org.sakaiproject.service.gradebook.shared.NegativeGradeException;
import org.sakaiproject.service.gradebook.shared.NonNumericGradeException;

import org.sakaiproject.tool.gradebook.ui.AssignmentDetailsBean;
import org.sakaiproject.tool.gradebook.ui.AssignmentGradeRow;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Validates assignment grades entered into the gradebook.  Since we display a
 * maximum of two decimal places in the UI, we use this validator to ensure that
 * the maximum precision entered into the gradebook is also two decimal places.
 * This should reduce rounding errors between actual scores and what is displayed
 * in the UI.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman </a>
 */
public class AssignmentGradeValidator implements Validator, Serializable {
	private static Log logger = LogFactory.getLog(AssignmentGradeValidator.class);

	/**
	 * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		
		HttpServletRequest req = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
		AssignmentDetailsBean adb = (AssignmentDetailsBean) req.getAttribute("assignmentDetailsBean");
		
		AssignmentGradeRow agr = (AssignmentGradeRow) req.getAttribute("row");
				
		int grade_type = -1;
		boolean ungraded = false;
		
		if(adb != null) {
			grade_type = adb.getAssignment().getGradebook().getGrade_type();
			ungraded = adb.getAssignment().getUngraded();
		}
		
		if(agr != null) {
			grade_type = agr.getAssociatedAssignment().getGradebook().getGrade_type();
			ungraded = agr.getAssociatedAssignment().getUngraded();
		}
		
		String stringValue = null;
		if (value != null) {
			if (!(value instanceof String)) {
				throw new IllegalArgumentException("The assignment grade must be a String");
			}
			stringValue = (String)value;
		}

		try {
			Grade grade = new Grade(stringValue, grade_type, ungraded);
		} catch (NonNumericGradeException nnge) {
			throw new ValidatorException(new FacesMessage(
					FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.Number.INVALID")));
		} catch (NegativeGradeException nge) {
			throw new ValidatorException(new FacesMessage(
					FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.Number.GREATER")));
		} catch (InvalidDecimalGradeException idge) {
			throw new ValidatorException(new FacesMessage(
					FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.PRECISION")));
		} catch (InvalidGradeLengthException igle) {
			throw new ValidatorException(new FacesMessage(
					FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.LetterGrade.INVALID")));				
		} catch (InvalidGradeException ige) {
			logger.error("Unknown type of InvalidGradeException thrown while validating grade: " + stringValue);
			throw new ValidatorException(new FacesMessage(
					FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.Number.INVALID")));
		} catch (GradebookException gbe) {
			logger.info("Gradebook grade_type is invalid");
		}			
	}
}



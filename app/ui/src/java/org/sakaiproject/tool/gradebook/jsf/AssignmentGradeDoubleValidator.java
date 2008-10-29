package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This method is for validating Double value. When we edit gradebook item, 
 * The type of gradebook item value is double , we need use this method to validate. 
 */
public class AssignmentGradeDoubleValidator implements Validator, Serializable {
	private static Log logger = LogFactory.getLog(AssignmentGradeDoubleValidator.class);

    /**
	 * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		if (value != null) {
			if (!(value instanceof Number)) {
				throw new IllegalArgumentException("The assignment grade must be a number");
			}
			double grade = ((Number)value).doubleValue();
            BigDecimal bd = new BigDecimal(grade);
            bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
            double roundedVal = bd.doubleValue();
            double diff = grade - roundedVal;
            if(diff != 0) {
                throw new ValidatorException(new FacesMessage(
                	FacesUtil.getLocalizedString(context, "org.sakaiproject.gradebook.tool.jsf.AssignmentGradeValidator.PRECISION")));
            }
		}
	}
}

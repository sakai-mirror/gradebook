package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
 * Validates category item value entered on the Gradebook setup page.
 *
 */
public class CategoryItemValidator implements Validator, Serializable {

	/**
	 * @see javax.faces.validator.Validator#validate(javax.faces.context.FacesContext,
	 *      javax.faces.component.UIComponent, java.lang.Object)
	 */
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
	    
        if (value != null) {
            if (!(value instanceof Double)) {
                throw new IllegalArgumentException("The drop score value must be a Double");
            }
        }

        try {
            Double toValidate = (Double)value;
            if(toValidate < 0) {
                throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_require_positive")));
            }
            BigDecimal bd = new BigDecimal((Double)value);
            if(bd.scale() > 2) {
                throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_too_precise")));            
            }
        } catch(NumberFormatException e) {
            throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_invalid")));
        }
	}
}



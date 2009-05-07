package org.sakaiproject.tool.gradebook.jsf;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;

import org.sakaiproject.tool.gradebook.Category;

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

	    Double toValidate = null;
	    
        if (value != null) {
            if (!(value instanceof Double)) {
                throw new IllegalArgumentException("The drop score value must be a Double");
            }
            toValidate = (Double)value;
        }
        
        HttpServletRequest req = ((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest());
        
        Category category = (Category) req.getAttribute("category");
        
        if(category != null) {
            if(category.isDropScores()) {
                if(toValidate == null || toValidate <= 0) { // if category drops scores, toValidate (itemValue) cannot be null or <=0
                    throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_require_positive")));
                }
            }
        }

        try {
            if(toValidate != null && toValidate < 0) {
                throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_require_positive")));
            }
            BigDecimal bd = new BigDecimal(toValidate);
            if(bd.scale() > 2) {
                throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_too_precise")));            
            }
        } catch(NumberFormatException e) {
            throw new ValidatorException(new FacesMessage(FacesUtil.getLocalizedString(context, "cat_itemvalue_invalid")));
        }
	}
}



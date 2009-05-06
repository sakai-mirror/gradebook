package org.sakaiproject.tool.gradebook.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.convert.NumberConverter;

public class DropScoresConverter extends NumberConverter {

    public DropScoresConverter() {
        setType("number");
    }
    
    public Object getAsObject(FacesContext context, UIComponent component, String newValue) throws ConverterException {
        try {
            Integer converted = new Integer(newValue);
            return converted;
        } catch(NumberFormatException e) {
            FacesMessage message = new FacesMessage(FacesUtil.getLocalizedString("cat_invalid_drop_score"));
            message.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ConverterException(message);
        }
    }
}

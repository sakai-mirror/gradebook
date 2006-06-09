package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * User: louis
 * Date: Jun 2, 2006
 * Time: 1:59:43 PM
 */
public class SpreadsheetListingBean extends GradebookDependentBean implements Serializable {


    private List spreadSheets;
    private static final Log logger = LogFactory.getLog(SpreadsheetListingBean.class);
    private Long spreadsheetId;
    private String pageName;

    FacesContext facesContext;
    HttpServletRequest request;
    HttpSession session;


    /**
     * TODO database presristence not yet implement
     */

    public SpreadsheetListingBean() {

        facesContext = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        session = (HttpSession) facesContext.getExternalContext().getSession(true);

        logger.debug("loading spreadsheetListingBean()");
        spreadSheets = (ArrayList) session.getAttribute("spreadsheets");

        logger.debug("trying to find the valud of spreadsheet id");
        logger.debug("spreadsheet id is  "+spreadsheetId);

    }


    public List getSpreadSheets() {
        return spreadSheets;
    }

    public void setSpreadSheets(List spreadSheets) {
        this.spreadSheets = spreadSheets;
    }

    public String deleteItem(){

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        List spreadsheets = (ArrayList)session.getAttribute("spreadsheets");
        spreadsheets.remove(getSpreadsheetId().intValue());
        FacesUtil.addErrorMessage("Item removed ");

        return null;
    }

    public String viewItem(){

        logger.debug("loading viewItem()");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        List spreadsheets = (ArrayList)session.getAttribute("spreadsheets");
        logger.debug("spreadsheetid "+getSpreadsheetId());
        SpreadsheetBean sp = (SpreadsheetBean) spreadsheets.get(getSpreadsheetId().intValue());
        StringBuffer sb = new StringBuffer();
        sb.append(sp.getContents());

        List contents = new ArrayList();

        String lineitems[] = sb.toString().split("\n");
        for(int i = 0;i<lineitems.length;i++){
           logger.debug("line item contents \n" + lineitems[i]);
          contents.add(lineitems[i]);
        }
        session.setAttribute("filecontents",contents);
        return "spreadsheetPreview";
    }

    public Long getSpreadsheetId() {
        return spreadsheetId;
    }

    public void setSpreadsheetId(Long spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

}

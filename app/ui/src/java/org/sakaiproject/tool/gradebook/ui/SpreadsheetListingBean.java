/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.tool.gradebook.Spreadsheet;

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
    private SpreadsheetBean spreadsheet;

    FacesContext facesContext;
    HttpServletRequest request;
    HttpSession session;


    /**
     * TODO database presristence not yet implement
     */

    public SpreadsheetListingBean() {

        facesContext = FacesContext.getCurrentInstance();
        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
        }catch(Exception e){
            logger.debug("unable to load");
        }


    }


    public List getSpreadSheets() {
        return getGradebookManager().getSpreadsheets(getGradebookId());
    }

    public void setSpreadSheets(List spreadSheets) {
        this.spreadSheets = spreadSheets;
    }

    public String deleteItem(){
        return "spreadsheetRemove";
    }

    public String viewItem(){

        logger.debug("loading viewItem()");

        Spreadsheet sp = getGradebookManager().getSpreadsheet(spreadsheetId);


        StringBuffer sb = new StringBuffer();
        sb.append(sp.getContent());

        List contents = new ArrayList();

        String lineitems[] = sb.toString().split("\n");
        for(int i = 0;i<lineitems.length;i++){
           logger.debug("line item contents \n" + lineitems[i]);
          contents.add(lineitems[i]);
        }

        spreadsheet.setTitle(sp.getName());
        spreadsheet.setDate(sp.getDateCreated());
        spreadsheet.setUserId(sp.getCreator());
        spreadsheet.setLineitems(contents);
        
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

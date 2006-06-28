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

import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;

import org.apache.myfaces.custom.fileupload.UploadedFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import javax.faces.context.FacesContext;

public class SpreadsheetUploadBean extends GradebookDependentBean implements Serializable {


    private String title;
    private UploadedFile upFile;
    private static final Log logger = LogFactory.getLog(SpreadsheetUploadBean.class);
    private SpreadsheetBean spreadsheet;


    public SpreadsheetUploadBean() {


        FacesContext facesContext = FacesContext.getCurrentInstance();
        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
        }catch(Exception e){
            if(logger.isDebugEnabled()) logger.debug("unable to load spreadsheetBean");
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_config_error"));

        }

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UploadedFile getUpFile() {
        return upFile;
    }

    public void setUpFile(UploadedFile upFile) {
        if(logger.isDebugEnabled()) logger.debug("upload file name " + upFile.getName());
        this.upFile = upFile;
    }


    public String processFile() throws Exception {

        if(logger.isDebugEnabled()) logger.debug("check if upFile is intialized");
        if(upFile == null){
            if(logger.isDebugEnabled()) logger.debug("upFile not initialized");
            FacesUtil.addErrorMessage(getLocalizedString("Upload_view_failure"));
            return null;
        }

        if(logger.isDebugEnabled()){
            logger.debug("file size " + upFile.getSize() + "file name " + upFile.getName() + "file Content Type " + upFile.getContentType() + "");
        }

        if(logger.isDebugEnabled()) logger.debug("check that the file is csv file");
        if(!upFile.getName().endsWith("csv")){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error",new String[] {upFile.getName()}));
            return null;
        }
        /**
        logger.debug("check that file content type");
        
        logger.debug("check the file content type");
        if(!upFile.getContentType().equalsIgnoreCase("application/vnd.ms-excel")){
            FacesUtil.addErrorMessage(getLocalizedString("upload_view_filetype_error",new String[] {upFile.getName()}));
            return null;
        }
         **/

        InputStream inputStream = new BufferedInputStream(upFile.getInputStream());
        List contents;
        contents = csvtoArray(inputStream);
        spreadsheet.setDate(new Date());
        spreadsheet.setTitle(this.getTitle());
        spreadsheet.setFilename(upFile.getName());
        spreadsheet.setLineitems(contents);


        return "spreadsheetUploadPreview";


    }

    /**
     * method converts an input stream to an List consist of strings
     * representing a line
     *
     * @param inputStream
     * @return contents
     */
    private List csvtoArray(InputStream inputStream) throws IOException{

        /**
         * TODO this well probably be removed
         */

        if(logger.isDebugEnabled()) logger.debug("csvtoArray()");
        List contents = new ArrayList();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = reader.readLine())!=null){
            //logger.debug("contents of line: "+line);
            contents.add(line);
        }
        return contents;

    }



}




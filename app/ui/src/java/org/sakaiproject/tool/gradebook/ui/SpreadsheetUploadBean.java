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


import javax.faces.context.FacesContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * User: louis
 * Date: Apr 26, 2006
 * Time: 10:10:23 AM
 */
public class SpreadsheetUploadBean implements Serializable {


    private String title;
    private UploadedFile upFile;
    private static final Log logger = LogFactory.getLog(SpreadsheetUploadBean.class);
    private SpreadsheetBean spreadsheet;


    public SpreadsheetUploadBean() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
        }catch(Exception e){
            logger.debug("unable to load");
        }

    }

    public String getTitle() {

        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UploadedFile getUpFile() {
        logger.debug("getUpFile()" +upFile);
        return upFile;
    }

    public void setUpFile(UploadedFile upFile) {
        logger.debug("setUpFile() " + upFile);
        if(upFile!=null) logger.debug("file name: " + upFile.getName() +"");
        this.upFile = upFile;
    }


    public String processFile() throws Exception {

        /**
         * TODO this needs revision espceially the session stuff next item online
         */

        logger.debug("processFile()-----------------------------------------");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

        logger.debug("file size " + upFile.getSize() + "");
        logger.debug("file name " + upFile.getName() + "");
        logger.debug("file Content Type " + upFile.getContentType() + "");


        InputStream inputStream = new BufferedInputStream(upFile.getInputStream());
        logger.debug("inputstream contents : "+inputStream.toString() );

        List contents = csvtoArray(inputStream);

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
    
        logger.debug("csvtoArray()");
        List contents = new ArrayList();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        contents = new ArrayList();
        String line;
        while((line = reader.readLine())!=null){
            logger.debug("contents of line: "+line);
            contents.add(line);
        }
        return contents;

    }



}




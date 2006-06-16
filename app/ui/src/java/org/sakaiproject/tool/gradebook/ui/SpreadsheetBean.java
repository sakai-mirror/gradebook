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

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: louis
 * Date: Jun 6, 2006
 * Time: 12:11:01 PM
 */
public class SpreadsheetBean extends GradebookDependentBean implements Serializable {

    private String title;
    private Date date;
    private String userId;
    private String contents;
    private String displayName;
    private Long gradebookId;
    private String filename;
    private List lineitems;
    private Map selectedAssignment;


    private static final Log logger = LogFactory.getLog(SpreadsheetBean.class);



    public SpreadsheetBean(String title, Date date, String userId, String contents) {

        logger.debug("loading SpreadsheetBean()");

        this.title = title;
        this.date = date;
        this.userId = userId;
        this.contents = contents;
    }

    public SpreadsheetBean() {

        logger.debug("loading SpreadsheetBean()");

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getDisplayName() {
        return getUserDirectoryService().getUserDisplayName(getUserId());
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getGradebookId() {
        return gradebookId;
    }

    public void setGradebookId(Long gradebookId) {
        this.gradebookId = gradebookId;
    }


    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public List getLineitems() {
        return lineitems;
    }

    public void setLineitems(List lineitems) {
        this.lineitems = lineitems;
    }


    public Map getSelectedAssignment() {
        return selectedAssignment;
    }

    public void setSelectedAssignment(Map selectedAssignment) {
        this.selectedAssignment = selectedAssignment;
    }


}

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
import org.sakaiproject.service.gradebook.shared.UnknownUserException;


import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

/**
 * User: louis
 * Date: Apr 26, 2006
 * Time: 10:12:39 AM
 */
public class SpreadsheetPreviewBean extends GradebookDependentBean implements Serializable {


    private List assignmentList;
    private List studentRows;
    private List assignmentHeaders;
    private Map selectedAssignment;
    private List assignmentColumnSelectItems;
    private boolean saved = false;
    private SpreadsheetBean spreadsheet;

    private FacesContext facesContext;
    private HttpServletRequest request;
    private HttpSession session;


    private static final Log logger = LogFactory.getLog(SpreadsheetPreviewBean.class);


    public SpreadsheetPreviewBean() {

        facesContext = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        session = (HttpSession) facesContext.getExternalContext().getSession(true);


        //List contents =  (ArrayList) session.getAttribute("filecontents");
        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
        }catch(Exception e){
            logger.debug("unable to load");
        }



        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        assignmentHeaders = new ArrayList();

        SpreadsheetHeader header = new SpreadsheetHeader((String) spreadsheet.getLineitems().get(0),",");
        assignmentHeaders = header.getHeaderWithoutUser();


        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetRow  row = new SpreadsheetRow(line,",");
                studentRows.add(row);
                logger.debug("row added" + rowcount);
            }
           rowcount++;
        }
        //create a numeric list of assignment headers

        logger.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            logger.debug("col added" + i);

        }


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            logger.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }
                
        logger.debug("Map initialized " +studentRows.size());
        logger.debug("assignmentList " +assignmentList.size());


    }

    public class SpreadsheetHeader implements Serializable{

        private List header;
        private List headerWithoutUser;
        private int columnCount;

        public List getHeader() {
            return header;
        }

        public void setHeader(List header) {
            this.header = header;
        }

        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }


        public List getHeaderWithoutUser() {
            List head = header;
            head.remove(0);
            headerWithoutUser = head;
            return head;
        }

        public void setHeaderWithoutUser(List headerWithoutUser) {
            this.headerWithoutUser = headerWithoutUser;
        }


        public SpreadsheetHeader(String source, String delim) {

            logger.debug("creating header from "+source);

            header = new ArrayList();
            String tokens[] = source.split(delim);
            for(int x =0;x<tokens.length;x++){
                logger.debug("token value using split "+tokens[x]);
                header.add(tokens[x]);

            }
            columnCount = tokens.length;

        }

    }

    public class SpreadsheetRow implements Serializable {

        private List rowcontent;
        private int columnCount;
        private String userDisplayName;
        private String userId;



        public SpreadsheetRow(String source, String delim) {

            logger.debug("creating row from string " + source);
            rowcontent = new ArrayList();
            String tokens[] = source.split(delim);
            for(int x =0;x<tokens.length;x++){
                logger.debug("token value using split "+tokens[x]);
                rowcontent.add(tokens[x]);
            }

            try {
                logger.debug("getuser name for "+ tokens[0]);
                userDisplayName = getUserDirectoryService().getUserDisplayName(tokens[0]);
                userId = tokens[0];
                logger.debug("get userid "+tokens[0] + "username is "+userDisplayName);

            } catch (UnknownUserException e) {
                logger.debug("User " + tokens[0] + " is unknown to this gradebook ");
                logger.error(e);
                userDisplayName = getLocalizedString("import_preview_unknownuser");
                userId = tokens[0];
                //FacesUtil.addErrorMessage("The Student with userid "+userId + " is not known to sakai");
            }

        }

        public List getRowcontent() {

            return rowcontent;
        }

        public void setRowcontent(List rowcontent) {
            this.rowcontent = rowcontent;
        }


        public int getColumnCount() {
            return columnCount;
        }

        public void setColumnCount(int columnCount) {
            this.columnCount = columnCount;
        }

        public String getUserDisplayName() {
            return userDisplayName;
        }

        public void setUserDisplayName(String userDisplayName) {
            this.userDisplayName = userDisplayName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

    }





    public String processFile(){


        logger.debug("processFile()");


        String selectedColumn =  request.getParameter("form:assignment");
        logger.debug("the selected column is " + selectedColumn);

        selectedAssignment = new HashMap();

        try{
            selectedAssignment.put("Assignment", assignmentHeaders.get(Integer.parseInt(selectedColumn) - 1));
        }catch(Exception e){
            logger.debug("no assignment selected");
            FacesUtil.addErrorMessage(getLocalizedString("import_preview_assignment_selection_failure"));
            return null;
        }

        Iterator it = studentRows.iterator();
        logger.debug("number of student rows "+studentRows.size() );
        int i = 0;
        while(it.hasNext()){

            logger.debug("row " + i);
            SpreadsheetRow row = (SpreadsheetRow) it.next();
            List line = row.getRowcontent();

            String user = (String)line.get(0);
            String points;
            try{
                points = (String) line.get(Integer.parseInt(selectedColumn));
            }catch(Exception e){
                logger.error(e);
                points = "";

            }
            logger.debug("user "+user + " points "+points);
            if(!points.equals("")){
                selectedAssignment.put(user,points);
            }
            i++;
        }


        spreadsheet.setSelectedAssignment(selectedAssignment);
        //session.setAttribute("selectedAssignment",selectedAssignment);
        //logger.debug("save map in session");
        ///Map map = (Map) session.getAttribute("selectedAssignment");
        //logger.debug("retrive map from session");
        //logger.debug("session info" +  map);

        return "spreadsheetImport";
    }


    public List getAssignmentList() {
        return assignmentList;
    }

    public void setAssignmentList(List assignmentList) {
        this.assignmentList = assignmentList;
    }

    public List getStudentRows() {
        return studentRows;
    }

    public void setStudentRows(List studentRows) {
        this.studentRows = studentRows;
    }

    public List getAssignmentHeaders() {
        return assignmentHeaders;
    }

    public void setAssignmentHeaders(List assignmentHeaders) {
        this.assignmentHeaders = assignmentHeaders;
    }

    public Map getSelectedAssignment() {
        return selectedAssignment;
    }

    public void setSelectedAssignment(Map selectedAssignment) {
        this.selectedAssignment = selectedAssignment;
    }

    public List getAssignmentColumnSelectItems() {
        return assignmentColumnSelectItems;
    }

    public void setAssignmentColumnSelectItems(List assignmentColumnSelectItems) {
        this.assignmentColumnSelectItems = assignmentColumnSelectItems;
    }

    public boolean isSaved() {
        return saved;
    }

    public void setSaved(boolean saved) {
        this.saved = saved;
    }




}

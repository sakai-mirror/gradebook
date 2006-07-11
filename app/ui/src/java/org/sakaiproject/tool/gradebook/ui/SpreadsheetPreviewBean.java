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
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.api.section.coursemanagement.EnrollmentRecord;
import org.sakaiproject.api.section.coursemanagement.User;

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

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
    private String columnCount;
    private String rowCount;
    private SpreadsheetBean spreadsheet;
    private Map rosterMap;
    private String rowStyles;
    private boolean hasUnknownUser;

    private static final Log logger = LogFactory.getLog(SpreadsheetPreviewBean.class);


    public void init() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
        }catch(Exception e){
            if(logger.isDebugEnabled()) logger.debug("unable to load spreadsheetBean");
        }


        //initialize rosteMap which is map of displayid and user objects
        rosterMap = new HashMap();
        List  enrollments = getAvailableEnrollments();
        if(logger.isDebugEnabled()) logger.debug("enrollment size " +enrollments.size());

        Iterator iter;
        iter = enrollments.iterator();
        while(iter.hasNext()){
            EnrollmentRecord enr;
            enr = (EnrollmentRecord)iter.next();
            if(logger.isDebugEnabled()) logger.debug("displayid "+enr.getUser().getDisplayId() + "  userid "+enr.getUser().getUserUid());
            rosterMap.put(enr.getUser().getDisplayId(),enr.getUser());
        }


        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        assignmentHeaders = new ArrayList();

        SpreadsheetPreviewBean.SpreadsheetHeader header = new SpreadsheetPreviewBean.SpreadsheetHeader((String) spreadsheet.getLineitems().get(0));
        assignmentHeaders = header.getHeaderWithoutUser();


        //generate spreadsheet rows
        Iterator it = spreadsheet.getLineitems().iterator();
        int rowcount = 0;
        int unknownusers = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetPreviewBean.SpreadsheetRow  row = new SpreadsheetPreviewBean.SpreadsheetRow(line);
                studentRows.add(row);
                //check the number of unkonw users in spreadsheet
                if(!row.isKnown())unknownusers = unknownusers + 1;
                if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("row added" + rowcount);
            }
            rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);
        if(unknownusers > 0){
            this.hasUnknownUser = true;
        }

        //create a numeric list of assignment headers

        SpreadsheetPreviewBean.logger.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size());


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("Map initialized " +studentRows.size());
        if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("assignmentList " +assignmentList.size());

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


        public SpreadsheetHeader(String source) {


            if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("creating header from "+source);
            header = new ArrayList();
            CSV csv = new CSV();
            header = csv.parse(source);
            columnCount = header.size();

        }

    }

    public class SpreadsheetRow implements Serializable {

        private List rowcontent;
        private int columnCount;
        private String userDisplayName;
        private String userId;
        private String userUid;
        private boolean isKnown;

        public SpreadsheetRow(String source) {


            if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("creating row from string " + source);
            rowcontent = new ArrayList();
             CSV csv = new CSV();
             rowcontent = csv.parse(source);

            try {
                if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("getuser name for "+ rowcontent.get(0));
                //userDisplayName = getUserDirectoryService().getUserDisplayName(tokens[0]);
                userId = (String) rowcontent.get(0);
                userDisplayName = ((User)rosterMap.get(rowcontent.get(0))).getDisplayName();
                userUid = ((User)rosterMap.get(rowcontent.get(0))).getUserUid();
                isKnown  = true;
                SpreadsheetPreviewBean.logger.debug("get userid "+ rowcontent.get(0) + "username is "+userDisplayName);

            } catch (Exception e) {
                if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.debug("User " + rowcontent.get(0) + " is unknown to this gradebook ");
                if(logger.isDebugEnabled()) SpreadsheetPreviewBean.logger.error(e);
                userDisplayName = "unknown student";
                userId = (String) rowcontent.get(0);
                userUid = null;
                isKnown = false;

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

        public String getUserUid() {
            return userUid;
        }

        public void setUserUid(String userUid) {
            this.userUid = userUid;
        }

        public boolean isKnown() {
            return isKnown;
        }

        public void setKnown(boolean known) {
            isKnown = known;
        }
    }

    public String saveFile(){

        StringBuffer sb = new StringBuffer();
        List contents =  spreadsheet.getLineitems();
        Iterator it = contents.iterator();
        while(it.hasNext()){
            String line = (String) it.next();
            sb.append(line + '\n');
        }

        String filename = spreadsheet.getFilename();

        /** temporary presistence code
         *
         */
        SpreadsheetPreviewBean.logger.debug("string to save "+sb.toString());
        try{
            getGradebookManager().createSpreadsheet(getGradebookId(),spreadsheet.getTitle(),this.getUserDirectoryService().getUserDisplayName(getUserUid()),new Date(),sb.toString());
        }catch(Exception e){
            if(logger.isDebugEnabled())logger.debug(e);
            FacesUtil.addErrorMessage(getLocalizedString("upload_preview_save_failure"));
            return null;
        }
        FacesUtil.addRedirectSafeMessage(getLocalizedString("upload_preview_save_confirmation", new String[] {filename}));
        return "spreadsheetListing";
    }


     public String processFile(){

         FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        if(logger.isDebugEnabled())logger.debug("processFile()");
        String selectedColumn =  request.getParameter("form:assignment");
        if(logger.isDebugEnabled())logger.debug("the selected column is " + selectedColumn);

        selectedAssignment = new HashMap();
        try{
            selectedAssignment.put("Assignment", assignmentHeaders.get(Integer.parseInt(selectedColumn) - 1));
        }catch(Exception e){
            if(logger.isDebugEnabled())logger.debug("no assignment selected");
            FacesUtil.addErrorMessage(getLocalizedString("import_preview_assignment_selection_failure"));
            return null;
        }

        Iterator it = studentRows.iterator();
        if(logger.isDebugEnabled())logger.debug("number of student rows "+studentRows.size() );
         int i = 0;
         while(it.hasNext()){

             if(logger.isDebugEnabled())logger.debug("row " + i);
             SpreadsheetRow row = (SpreadsheetRow) it.next();
             List line = row.getRowcontent();

             String userid = "";
             String user = (String)line.get(0);
             try{
                 userid = ((User)rosterMap.get(line.get(0))).getUserUid();
             }catch(Exception e){
                 if(logger.isDebugEnabled())logger.debug("user "+ user + "is not known to the system");
                 userid = "";
             }
             String points;
             try{
                 points = (String) line.get(Integer.parseInt(selectedColumn));
             }catch(Exception e){
                 if(logger.isDebugEnabled())logger.error(e);
                 points = "";

             }
             if(logger.isDebugEnabled())logger.debug("user "+user + " userid " + userid +" points "+points);
             if(!points.equals("") && (!userid.equals(""))){
                 selectedAssignment.put(userid,points);
             }
             i++;
         }
         if(logger.isDebugEnabled())logger.debug("scores to import "+ i);

         //spreadsheet.setSelectedAssignment(selectedAssignment);
         ((SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext)).setSelectedAssignment(selectedAssignment);
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


    public String getColumnCount() {
        return FacesUtil.getLocalizedString("upload_preview_column_count",new String[] {columnCount});
    }

    public void setColumnCount(String columnCount) {
        this.columnCount = columnCount;
    }

    public String getRowCount() {
        return FacesUtil.getLocalizedString("upload_preview_row_count",new String[] {rowCount});
    }

    public void setRowCount(String rowCount) {
        this.rowCount = rowCount;
    }

    public Map getRosterMap() {
        return rosterMap;
    }

    public void setRosterMap(Map rosterMap) {
        this.rosterMap = rosterMap;
    }


    public String getRowStyles() {
        StringBuffer sb = new StringBuffer();
        for(Iterator iter = studentRows.iterator(); iter.hasNext();){
            SpreadsheetRow row = (SpreadsheetRow)iter.next();
            if(row.isKnown()){
               sb.append("internal,");
            }else{
               sb.append("external,");
            }
        }
        logger.debug(sb.toString());
        return sb.toString();
    }

    public void setRowStyles(String rowStyles) {
        this.rowStyles = rowStyles;
    }

    public SpreadsheetBean getSpreadsheet() {
        return spreadsheet;
    }

    public void setSpreadsheet(SpreadsheetBean spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    public boolean getHasUnknownUser() {
        return hasUnknownUser;
    }

    public void setHasUnknownUser(boolean hasUnknownUser) {
        this.hasUnknownUser = hasUnknownUser;
    }


    /** Parse comma-separated values (CSV), a common Windows file format.
     * Sample input: "LU",86.25,"11/4/1998","2:19PM",+4.0625
     * <p>
     * Inner logic adapted from a C++ original that was
     * Copyright (C) 1999 Lucent Technologies
     * Excerpted from 'The Practice of Programming'
     * by Brian W. Kernighan and Rob Pike.
     * <p>
     * Included by permission of the http://tpop.awl.com/ web site,
     * which says:
     * "You may use this code for any purpose, as long as you leave
     * the copyright notice and book citation attached." I have done so.
     * @author Brian W. Kernighan and Rob Pike (C++ original)
     * @author Ian F. Darwin (translation into Java and removal of I/O)
     * @author Ben Ballard (rewrote advQuoted to handle '""' and for readability)
     */
    class CSV {

        public static final char DEFAULT_SEP = ',';

        /** Construct a CSV parser, with the default separator (`,'). */
        public CSV() {
            this(DEFAULT_SEP);
        }

        /** Construct a CSV parser with a given separator.
         * @param sep The single char for the separator (not a list of
         * separator characters)
         */
        public CSV(char sep) {
            fieldSep = sep;
        }

        /** The fields in the current String */
        protected List list = new ArrayList();

        /** the separator char for this parser */
        protected char fieldSep;

        /** parse: break the input String into fields
         * @return java.util.Iterator containing each field
         * from the original as a String, in order.
         */
        public List parse(String line)
        {
            StringBuffer sb = new StringBuffer();
            list.clear();      // recycle to initial state
            int i = 0;

            if (line.length() == 0) {
                list.add(line);
                return list;
            }

            do {
                sb.setLength(0);
                if (i < line.length() && line.charAt(i) == '"')
                    i = advQuoted(line, sb, ++i);  // skip quote
                else
                    i = advPlain(line, sb, i);
                list.add(sb.toString());
                i++;
            } while (i < line.length());

            return list;
        }

        /** advQuoted: quoted field; return index of next separator */
        protected int advQuoted(String s, StringBuffer sb, int i)
        {
            int j;
            int len= s.length();
            for (j=i; j<len; j++) {
                if (s.charAt(j) == '"' && j+1 < len) {
                    if (s.charAt(j+1) == '"') {
                        j++; // skip escape char
                    } else if (s.charAt(j+1) == fieldSep) { //next delimeter
                        j++; // skip end quotes
                        break;
                    }
                } else if (s.charAt(j) == '"' && j+1 == len) { // end quotes at end of line
                    break; //done
                }
                sb.append(s.charAt(j));  // regular character.
            }
            return j;
        }

        /** advPlain: unquoted field; return index of next separator */
        protected int advPlain(String s, StringBuffer sb, int i)
        {
            int j;

            j = s.indexOf(fieldSep, i); // look for separator
            if (j == -1) {                 // none found
                sb.append(s.substring(i));
                return s.length();
            } else {
                sb.append(s.substring(i, j));
                return j;
            }
        }
    }

}

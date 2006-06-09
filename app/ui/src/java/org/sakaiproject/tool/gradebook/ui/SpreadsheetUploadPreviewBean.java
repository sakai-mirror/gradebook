package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

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
public class SpreadsheetUploadPreviewBean extends GradebookDependentBean implements Serializable {


    private List assignmentList;
    private List studentRows;
    private List assignmentHeaders;
    private Map selectedAssignment;
    private List assignmentColumnSelectItems;
    private boolean saved = false;
    private String columnCount;
    private String rowCount;

    private FacesContext facesContext;
    private HttpServletRequest request;
    private HttpSession session;


    private static final Log logger = LogFactory.getLog(SpreadsheetUploadPreviewBean.class);

    /**
     * TODO this  class is very similar to SpreadsheetPreviewBean once i figure out the session handling
     * and request forwarding issue probably consolidate the two into one class
     * 
     */
    public SpreadsheetUploadPreviewBean() {

        facesContext = FacesContext.getCurrentInstance();
        request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        session = (HttpSession) facesContext.getExternalContext().getSession(true);


        List contents =  (ArrayList) session.getAttribute("filecontents");


        assignmentList = new ArrayList();
        studentRows = new ArrayList();
        assignmentColumnSelectItems = new ArrayList();
        assignmentHeaders = new ArrayList();

        SpreadsheetUploadPreviewBean.SpreadsheetHeader header = new SpreadsheetUploadPreviewBean.SpreadsheetHeader((String) contents.get(0),",");
        assignmentHeaders = header.getHeaderWithoutUser();


        //generate spreadsheet rows
        Iterator it = contents.iterator();
        int rowcount = 0;
        while(it.hasNext()){
            String line = (String) it.next();
            if(rowcount > 0){
                SpreadsheetUploadPreviewBean.SpreadsheetRow  row = new SpreadsheetUploadPreviewBean.SpreadsheetRow(line,",");
                studentRows.add(row);
                SpreadsheetUploadPreviewBean.logger.debug("row added" + rowcount);
            }
           rowcount++;
        }
        rowCount = String.valueOf(rowcount - 1);


        //create a numeric list of assignment headers

        SpreadsheetUploadPreviewBean.logger.debug("creating assignment List ---------");
        for(int i = 0;i<assignmentHeaders.size();i++){
            assignmentList.add(new Integer(i));
            SpreadsheetUploadPreviewBean.logger.debug("col added" + i);
        }
        columnCount = String.valueOf(assignmentHeaders.size() - 1);


        for(int i = 0;i<assignmentHeaders.size();i++){
            SelectItem item = new  SelectItem(new Integer(i + 1),(String)assignmentHeaders.get(i));
            SpreadsheetUploadPreviewBean.logger.debug("creating selectItems "+ item.getValue());
            assignmentColumnSelectItems.add(item);
        }

        SpreadsheetUploadPreviewBean.logger.debug("Map initialized " +studentRows.size());
        SpreadsheetUploadPreviewBean.logger.debug("assignmentList " +assignmentList.size());


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

            SpreadsheetUploadPreviewBean.logger.debug("creating header from "+source);

            header = new ArrayList();
            String tokens[] = source.split(delim);
            for(int x =0;x<tokens.length;x++){
                SpreadsheetUploadPreviewBean.logger.debug("token value using split "+tokens[x]);
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

            SpreadsheetUploadPreviewBean.logger.debug("creating row from string " + source);
            rowcontent = new ArrayList();
            String tokens[] = source.split(delim);
            for(int x =0;x<tokens.length;x++){
                SpreadsheetUploadPreviewBean.logger.debug("token value using split "+tokens[x]);
                rowcontent.add(tokens[x]);
            }



            try {
                SpreadsheetUploadPreviewBean.logger.debug("getuser name for "+ tokens[0]);
                userDisplayName = getUserDirectoryService().getUserDisplayName(tokens[0]);
                userId = tokens[0];
                SpreadsheetUploadPreviewBean.logger.debug("get userid "+tokens[0] + "username is "+userDisplayName);

            } catch (UnknownUserException e) {
                SpreadsheetUploadPreviewBean.logger.debug("User " + tokens[0] + " is unknown to this gradebook ");
                SpreadsheetUploadPreviewBean.logger.error(e);
                userDisplayName = "unknown student";
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

    public String saveFile(){

        StringBuffer sb = new StringBuffer();
        List contents =  (ArrayList) session.getAttribute("filecontents");
        Iterator it = contents.iterator();
        while(it.hasNext()){
            String line = (String) it.next();
            sb.append(line + '\n');
        }

        String filename = (String) session.getAttribute("filename");

        SpreadsheetUploadPreviewBean.logger.debug("string to save "+sb.toString());
        SpreadsheetBean spt = new SpreadsheetBean(filename,new Date(),getUserUid(),sb.toString());

        try{
            SpreadsheetUploadPreviewBean.logger.debug("store raw spreadsheet content");
            List spreadsheets = (ArrayList) session.getAttribute("spreadsheets");
            spreadsheets.add(spt);
        }catch(Exception e){
            SpreadsheetUploadPreviewBean.logger.error(e);
            SpreadsheetUploadPreviewBean.logger.debug("variable storage doesnt exist create one");
            List spreadsheets = new ArrayList();
            spreadsheets.add(spt);
            session.setAttribute("spreadsheets",spreadsheets);
        }
        FacesUtil.addRedirectSafeMessage(getLocalizedString("upload_preview_save_confirmation", new String[] {filename}));
        return "spreadsheetListing";
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

    public void setColumnCount(String columCount) {
        this.columnCount = columnCount;
    }

    public String getRowCount() {
        return FacesUtil.getLocalizedString("upload_preview_row_count",new String[] {rowCount});
    }

    public void setRowCount(String rowCount) {
        this.rowCount = rowCount;
    }


}

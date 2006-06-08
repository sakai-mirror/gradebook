package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.*;

/**
 * User: louis
 * Date: May 17, 2006
 * Time: 3:19:31 PM
 */
public class SpreadsheetImportBean extends GradebookDependentBean implements Serializable {


    private Map scores;
    private Assignment assignment;
    private GradeRecordSet graderecords;
    private Long assignmentId;
    FacesContext facesContext;
    HttpServletRequest request;
    HttpSession session;

    private static final Log logger = LogFactory.getLog(SpreadsheetImportBean.class);


    public SpreadsheetImportBean() {

        logger.debug("spreadsheetImporBean()");

        if(assignment == null) {
            assignment = new Assignment();
        }

        facesContext = FacesContext.getCurrentInstance();
        session = (HttpSession) facesContext.getExternalContext().getSession(true);


        scores =  (HashMap) session.getAttribute("selectedAssignment");

        logger.debug("retrieve selectedAssignment from session");
        logger.debug("score contents " + scores);
        logger.debug("get Assignment title which is first key");

        assignment.setName((String) scores.get("Assignment"));

    }


    public Map getScores() {
        return scores;
    }

    public void setScores(Map scores) {
        this.scores = scores;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }


    public String saveGrades(){

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();

        logger.debug("read all the request parameters------------");

        Enumeration paramNames = request.getParameterNames();
        while(paramNames.hasMoreElements()){

            String param = (String) paramNames.nextElement();
            String[] paramValues = request.getParameterValues(param);
            if(paramValues.length == 1){
                logger.debug("parameter: "+param +" is "+paramValues[0]);
            }
            if(paramValues.length == 0){
                logger.debug("parameter: "+param +" has no value ");
            }
            if(paramValues.length > 1){
                for(int i = 0;i< paramValues.length;i++)
                    logger.debug("parameter: "+param +" is "+paramValues[i]);
            }

        }
        logger.debug("end parameter read----------------");

        logger.debug("create assignment and save grades");

        try {

            assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()));
            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));


            assignment = getGradebookManager().getAssignmentWithStats(assignmentId);
            graderecords = new GradeRecordSet(assignment);

            logger.debug("remove title entry form map");
            scores.remove("Assignment");
            logger.debug("iterate through scores and and save assignment grades");


            Iterator it = scores.entrySet().iterator();
            while(it.hasNext()){

                Map.Entry entry = (Map.Entry) it.next();
                String uid = (String) entry.getKey();
                String points = (String) entry.getValue();
                AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,uid,Double.valueOf(points));
                graderecords.addGradeRecord(asnGradeRecord);
                logger.debug("added grades for " + uid + " - points scored " +points);

            }

            logger.debug("persist grade records to database");
            Set mismatchedScores  = getGradebookManager().updateAssignmentGradeRecords(graderecords);

            return  "spreadsheetPreview";

        } catch (ConflictingAssignmentNameException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));

        }

        return null;
    }


}

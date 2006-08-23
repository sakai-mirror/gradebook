/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California
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
    private SpreadsheetBean spreadsheet;
    private static final Log logger = LogFactory.getLog(SpreadsheetImportBean.class);


    public void init() {

        if(logger.isDebugEnabled())logger.debug("spreadsheetImporBean()");

        if(assignment == null) {
            assignment = new Assignment();
            assignment.setReleased(true);
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        try{
            spreadsheet  = (SpreadsheetBean) facesContext.getApplication().createValueBinding("#{spreadsheetBean}").getValue(facesContext);
            if(logger.isDebugEnabled())logger.debug(spreadsheet.toString());
        }catch(Exception e){
            if(logger.isDebugEnabled())logger.debug("unable to load SpreadsheetBean");
        }

        scores =  spreadsheet.getSelectedAssignment();
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

        if(logger.isDebugEnabled())logger.debug("create assignment and save grades");
        if(logger.isDebugEnabled()) logger.debug("first check if all avariable are numeric");

        Iterator iter = scores.entrySet().iterator();
        while(iter.hasNext()){
            Map.Entry entry  = (Map.Entry) iter.next();
            String points =  (String) entry.getValue();
            try{
                if(logger.isDebugEnabled()) logger.debug("checking if " +points +" is a numeric value");
                if(!entry.getKey().equals("Assignment"))Double.parseDouble(points);
            }catch(Exception e){
                if(logger.isDebugEnabled()) logger.debug(points + " is not a numeric value");
                FacesUtil.addRedirectSafeMessage(getLocalizedString("import_assignment_notsupported"));
                return "spreadsheetPreview";
            }

        }


        try {
            assignmentId = getGradebookManager().createAssignment(getGradebookId(), assignment.getName(), assignment.getPointsPossible(), assignment.getDueDate(), new Boolean(assignment.isNotCounted()),new Boolean(assignment.isReleased()));
            FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", new String[] {assignment.getName()}));

            assignment = getGradebookManager().getAssignmentWithStats(assignmentId);
            graderecords = new GradeRecordSet(assignment);

            if(logger.isDebugEnabled())logger.debug("remove title entry form map");
            scores.remove("Assignment");
            if(logger.isDebugEnabled())logger.debug("iterate through scores and and save assignment grades");

            Iterator it = scores.entrySet().iterator();
            while(it.hasNext()){

                Map.Entry entry = (Map.Entry) it.next();
                String uid = (String) entry.getKey();
                String points = (String) entry.getValue();
                AssignmentGradeRecord asnGradeRecord = new AssignmentGradeRecord(assignment,uid,Double.valueOf(points));
                graderecords.addGradeRecord(asnGradeRecord);
                if(logger.isDebugEnabled())logger.debug("added grades for " + uid + " - points scored " +points);

            }

            if(logger.isDebugEnabled())logger.debug("persist grade records to database");
            Set mismatchedScores  = getGradebookManager().updateAssignmentGradeRecords(graderecords);

            return  "spreadsheetPreview";
        } catch (ConflictingAssignmentNameException e) {
            if(logger.isErrorEnabled())logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("add_assignment_name_conflict_failure"));
        }
        return null;
    }

}

package org.sakaiproject.tool.gradebook.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetDataFileWriterCsv;
import org.sakaiproject.jsf.spreadsheet.SpreadsheetUtil;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.coursemanagement.User;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;

import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeleteAllGradesBean extends RosterBean implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Log logger = LogFactory.getLog(DeleteAllGradesBean.class);

	private boolean delete = false;
	
	public String processCancelDeleteAllGrades(){
		return "gradebookSetup";
	
	}
	
	public void processExportGradebook(){
		ActionEvent event = null;
		exportCsv(event);
	}		
	
	public String processDeleteGrades(){
		if(!delete) {
			FacesUtil.addErrorMessage(getLocalizedString("gb_delete_checkbox"));
			return null;
				
		}
		getGradebookManager().removeAllGrades(getGradebookId());
		getGradebookBean().getEventTrackingService().postEvent("gradebook.deleteAll","/gradebook/"+getGradebookId()+"/"+getAuthzLevel());
		return "gradebookSetup";
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

}

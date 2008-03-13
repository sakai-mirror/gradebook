package org.sakaiproject.tool.gradebook.business.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.thread_local.cover.ThreadLocalManager;
import org.sakaiproject.tool.api.ActiveTool;
import org.sakaiproject.tool.cover.ActiveToolManager;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.GradeRecordSet;
import org.sakaiproject.tool.gradebook.business.GbSynchronizer;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.iquiz.cover.IquizService;
import org.sakaiproject.tool.gradebook.CommonGradeRecord;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.business.GradebookManager;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

public class GbSynchronizerImpl extends HibernateDaoSupport
	implements GbSynchronizer  
{
    private static final Log log = LogFactory.getLog(GbSynchronizerImpl.class);
    
	public boolean isProjectSite() 
	{
		String siteId = ToolManager.getCurrentPlacement().getContext();
		return isProjectSite(siteId);
	}
	
	public boolean isProjectSite(String siteId) 
	{
		String siteType = null;

		try
		{
			siteType = SiteService.getSite(siteId).getType();
		}
		catch(IdUnusedException e){
			log.info("IdUnusedException for site: " + siteId);
			throw new DataAccessResourceFailureException("IdUnusedException for site: " + siteId);
		}
		return "project".equalsIgnoreCase(siteType);
	}

	public void deleteLegacyAssignment(String assignmentName) 
	{
        IquizService.deleteLegacyAssignment(assignmentName);
	}

	public Map convertEidUid(Collection gradeRecords) 
	{
		Map returnMap = new HashMap();
		for(Iterator iter = gradeRecords.iterator(); iter.hasNext();) 
		{
			AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
			
			
      		AssignmentGradeRecord record = new AssignmentGradeRecord(
      				agr.getAssignment(), agr.getStudentId(), agr.getPointsEarned());
      		record.setGraderId(agr.getGraderId());
      		record.setDateRecorded(agr.getDateRecorded()); 
			
			String studentEid = agr.getStudentId();
			String studentUid = null;
			
			try
			{
				UserDirectoryService.getUserEid(studentEid);
				studentUid = studentEid;
			}
			catch(UserNotDefinedException unde)
			{
				try
				{
					studentUid = UserDirectoryService.getUserId(studentEid);
				}
				catch(UserNotDefinedException une)
				{
					studentUid = studentEid;
				}
			}
			
			if(studentUid != null)
				record.setStudentId(studentUid);
			returnMap.put(studentUid, record);
		}
		return returnMap;
	}
	
	public Map getLegacyAssignmentWithStats(String assignmentName)
	{
		return IquizService.getLegacyAssignmentWithStats(assignmentName);
	}
	
	public AssignmentGradeRecord convertIquizRecordToUid(AssignmentGradeRecord iquizRecord, Map persistentRecordMap, boolean isUpdateAll, String graderId)
	{
		String studentEid = iquizRecord.getStudentId();
		String studentUid = null;
		try
		{
			studentUid = UserDirectoryService.getUserId(studentEid);
		}
		catch(UserNotDefinedException unde)
		{
		}
		if(studentUid != null)
		{
			AssignmentGradeRecord record = (AssignmentGradeRecord) persistentRecordMap.get(studentUid);

			if (!isUpdateAll)
			{
				// instructor save
				record.setGraderId(graderId);
			}
			else
			{
				// iquiz update-all synchronization save
				record.setGraderId(iquizRecord.getGraderId());
			}
			record.setDateRecorded(iquizRecord.getDateRecorded());
			record.setPointsEarned(iquizRecord.getPointsEarned());
			
			return record;
		}
		return null;
	}
	
	public CommonGradeRecord getNeededUpdateIquizRecord(Assignment assignment, AssignmentGradeRecord record)
	{
		  HttpSession httpSession = (HttpSession) SessionManager.getCurrentSession();
  		  String siteId = ToolManager.getCurrentPlacement().getContext();
  		  String key = "iquiz_assignments:" + siteId;
  		  List iquizAssocAssignments = (List) httpSession.getAttribute(key);
  		  List assignList = new ArrayList();
  		  assignList.add(assignment);
  		  Map legacyUpdateMap = reconcileAllAssignments(assignList);
  		  if(legacyUpdateMap != null)
  		  {
  	  		  GradeRecordSet gradeRecordSet = (GradeRecordSet) legacyUpdateMap.get(assignment.getName());
  	  		  if(gradeRecordSet != null)
  	  		  {
  	  			  Collection gradeRecordsFromCall = gradeRecordSet.getAllGradeRecords();
  	  			  if(gradeRecordsFromCall != null)
  	  			  {
  	  				  if (iquizAssocAssignments != null)
  	  				  {
  	  					  CommonGradeRecord commonGradeRecord = IquizService.getNewCommonGradeRecord();

  	  					  String studentUid = record.getStudentId();
  	  					  String studentEid = null;
  	  					  try
  	  					  {
  	  						  studentEid = UserDirectoryService.getUserEid(studentUid);
  	  					  }
  	  					  catch(UserNotDefinedException unde)
  	  					  {
  	  					  }
  	  					  if(studentEid != null)
  	  					  {
  	    	  				  Iterator iter = gradeRecordsFromCall.iterator();
  	    	  				  while(iter.hasNext())
  	    	  				  {
  	    	  					AssignmentGradeRecord agr = (AssignmentGradeRecord) iter.next();
  	    	  					if(agr.getStudentId().equals(studentEid))
  	    	  					{
  	  	  						  commonGradeRecord.setStudentUserId(studentEid);
  	  	  						  commonGradeRecord.setPointsEarned(record.getPointsEarned());
  	  	  						  commonGradeRecord.setGraderId(record.getGraderId());
  	  	  						  Date now = new Date();
  	  	  						  commonGradeRecord.setDateGraded(now);
  	  	  						  return commonGradeRecord;
  	    	  					}
  	    	  				  }
  	    	  				  return null;
  	  					  }
  	  					  return null;
  	  				  }
  	  				  return null;
  	  			  }
  	  			  return null;
  	  		  }
  			  return null;
  		  }
  		  return null;
	}
	
	public void updateLegacyGradeRecords(String assignmentName, List legacyUpdates)
	{
		IquizService.updateLegacyGradeRecords(assignmentName, legacyUpdates);
	}
	
	public Map reconcileAllAssignments(List assignments)
	{
		return IquizService.reconcileAllAssignments(assignments);
	}
	
	public void addLegacyAssignment(String name)
	{
		IquizService.addLegacyAssignment(name);
	}
	
	public Map getPersistentRecords(final Long gradableObjId)
	{
        HibernateCallback hc = new HibernateCallback() 
        {
            public Object doInHibernate(Session session) throws HibernateException 
            {
                Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.gradableObject.id=:gradableObjectId");
                q.setLong("gradableObjectId", gradableObjId);
                return q.list();
            }
        };
        List records = (List) getHibernateTemplate().execute(hc);
        Map returnMap = new HashMap();
        if(records != null)
        {
        	for(int i=0; i<records.size(); i++)
        	{
        		AssignmentGradeRecord agr = (AssignmentGradeRecord) records.get(i);
        		if(agr != null)
        		{
        			returnMap.put(agr.getStudentId(), agr);
        		}
        	}
        }

		return returnMap;
	}
	
	public Map getPersistentRecordsForStudent(final String studentId)
	{
        HibernateCallback hc = new HibernateCallback() 
        {
            public Object doInHibernate(Session session) throws HibernateException 
            {
                Query q = session.createQuery("from AssignmentGradeRecord as agr where agr.studentId=:studentId");
                q.setString("studentId", studentId);
                return q.list();
            }
        };
        List records = (List) getHibernateTemplate().execute(hc);
        Map returnMap = new HashMap();
        if(records != null)
        {
        	for(int i=0; i<records.size(); i++)
        	{
        		AssignmentGradeRecord agr = (AssignmentGradeRecord) records.get(i);
        		if(agr != null)
        		{
        			returnMap.put(agr.getGradableObject().getId(), agr);
        		}
        	}
        }

		return returnMap;
	}
	
	public void synchrornizeAssignments(List assignments)
	{
        if (ThreadLocalManager.get("iquiz_thread_visit") == null && !isProjectSite())
        {                	
        	ThreadLocalManager.set("iquiz_thread_visit", Boolean.TRUE);
        	ThreadLocalManager.set("iquiz_update_all", Boolean.TRUE);
        	Map legacyUpdateMap = reconcileAllAssignments(assignments);

        	org.sakaiproject.tool.api.Session sakaiSession = SessionManager.getCurrentSession();                                        
            ActiveTool activeTool = ActiveToolManager.getActiveTool("sakai.gradebook.tool");
            WebApplicationContext wac = WebApplicationContextUtils.getWebApplicationContext(activeTool.getServletContext());  
            GradebookManager gmhi = (GradebookManager) wac.getBean("org_sakaiproject_tool_gradebook_business_GradebookManager");          

        	if (sakaiSession == null){
        		log.error("Session is null");
        		throw new Error("Session is null");
        	}

        	List iquizAssocAssignments = new ArrayList();           

        	for (Iterator i = legacyUpdateMap.keySet().iterator(); i.hasNext();){
        		String assignmentName = (String) i.next();

        		// store all iquiz associated assignment names (used by AssignmentGradeValidator) 
        		iquizAssocAssignments.add(assignmentName);

        		GradeRecordSet gradeRecordSet = (GradeRecordSet) legacyUpdateMap.get(assignmentName);
        		Collection gradeRecordsFromCall = gradeRecordSet.getAllGradeRecords();
        		ThreadLocalManager.set("iquiz_call", Boolean.TRUE);
        		Long assignmentId = null;
        		for(int j=0; j<assignments.size(); j++)
        		{
        			Assignment assign = (Assignment)assignments.get(j);
        			if(assign != null && assign.getName().equals(assignmentName))
        				assignmentId = assign.getId();
        		}
        		gmhi.updateAssignmentGradeRecords(gmhi.getAssignment(assignmentId), gradeRecordsFromCall);
        		ThreadLocalManager.set("iquiz_call", null);
        	}

        	// store assignments which are attached to an iquiz assessment
        	// used in the AssignmentGradeValidator to check if points assigned > 5 chars
        	// this is need to protect gb_grade records from truncation error                    
        	synchronized(sakaiSession){ 
        		String siteId = ToolManager.getCurrentPlacement().getContext();
        		String key = "iquiz_assignments:" + siteId;                    	
        		sakaiSession.setAttribute(key, iquizAssocAssignments);                    	                    	
        	}
        }
	}
	
  	public void updateAssignment(String title, String newTitle)
  	{
  		IquizService.updateLegacyAssignmentTitle(title, newTitle);
  	}
}
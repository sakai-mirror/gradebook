/**********************************************************************************
 *
 * $Id$
 *
 ***********************************************************************************
 *
 * Copyright (c) 2005, 2006, 2007 The Sakai Foundation
 *
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/
package org.sakaiproject.tool.gradebook.ui;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.event.ActionEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.component.cover.ServerConfigurationService;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.legacydb.api.LegacyDataSource;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.api.SiteService.SortType;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.tool.gradebook.AbstractGradeRecord;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.GradableObject;
import org.sakaiproject.user.api.UserNotDefinedException;
import org.sakaiproject.user.cover.UserDirectoryService;


public class ExportForSISOncourseUtility {
	private static final Log logger = LogFactory.getLog(ExportForSISOncourseUtility.class);

	/**
	 * Returns a mapping of sakai user id to university id
	 * @return
	 */
	protected static HashMap getUnivIdAndUserIdMap() {
		HashMap rosterMap = new HashMap();
		String siteId = ToolManager.getCurrentPlacement().getContext();

		// get the site properties to determine Original course id and whether this is a combined course	
		ResourceProperties siteProperties = null;

		try{
			siteProperties = SiteService.getSite(siteId).getProperties();
		}
		catch(IdUnusedException e) {
			logger.error("IdUnusedException for site: " + siteId);
			throw new RuntimeException("IdUnusedException for site: " + siteId);
		}

		String courseId = null;
		String siteIsParent = null;

		if (siteProperties != null) {
			courseId = siteProperties.getProperty("site-oncourse-course-id");
			siteIsParent = siteProperties.getProperty("site-oncourse-combined-is-parent");
		}
		else {
			logger.error("No corresponding Original course id found for site id: " + siteId);
			return null;
		}

		// if this is a parent site for combined courses, we need to extract the univ ids from the child sites individually
		// there is no data in course_user_final corresponding to the combined course site
		if (siteIsParent != null && siteIsParent.equalsIgnoreCase("true")) {
			Map propertyCriteria = new HashMap();
			propertyCriteria.put("site-oncourse-combined-my-parent", siteId);
			List childSites = SiteService.getSites(org.sakaiproject.site.api.SiteService.SelectionType.ANY, 
					null, null, propertyCriteria, SortType.TITLE_ASC, null);

			if (childSites != null && !childSites.isEmpty()) {
				Iterator childIter = childSites.iterator();
				while (childIter.hasNext()) {
					Site childSite = (Site) childIter.next();
					String childSiteId = childSite.getId();

					ResourceProperties childSiteProperties = childSite.getProperties();

					String childCourseId = null;
					String childSiteIsParent = null;

					if (childSiteProperties != null) {
						childCourseId = childSiteProperties.getProperty("site-oncourse-course-id");
					}
					else {
						logger.error("No corresponding Original course id found for site id: " + childSiteId);
					}

					if (childCourseId != null && childSiteId != null) {
						rosterMap = retrieveUnivIdsFromOriginal(rosterMap, childSiteId, childCourseId);
					}
				}
			}
		}
		else {
			rosterMap = retrieveUnivIdsFromOriginal(rosterMap, siteId, courseId);
		}

		return rosterMap;
	}

	/**
	 * Given a CL site id and Original course id, will return a mapping of CL uids to university ids
	 * @param rosterMap
	 * @param siteId
	 * @param courseId
	 * @return
	 */
	private static HashMap retrieveUnivIdsFromOriginal(HashMap rosterMap, String siteId, String courseId) {

		Connection legacyConnection = null;
		Statement legacyStatement = null;
		ResultSet rs = null;

		LegacyDataSource legacyDataSource = (LegacyDataSource) ComponentManager.get("LegacyDataSource");

		if (legacyDataSource == null) {
			logger.error("Unable to retrieve LegacyDataSource component");
			return null;
		}
		try {
			legacyConnection = legacyDataSource.getConnectionWithSiteId(siteId);

			String query = "select univ_id, user_id from course_user_final where course_id = '" + courseId + "'";
			legacyStatement = legacyConnection.createStatement();
			rs = legacyStatement.executeQuery(query);

			while (rs.next()) {
				String univ_id = rs.getString("UNIV_ID");
				String user_id = rs.getString("USER_ID");

				if (univ_id != null && user_id != null) {

					try	{
						String uid = UserDirectoryService.getUserId(user_id);
						rosterMap.put(uid, univ_id);
					}
					catch(UserNotDefinedException unde)
					{
						logger.warn("User id " + user_id + " from original not found in cl");
					}
				}
			}

			legacyConnection.close();
		}
		catch (Exception e) {
			logger.error(e);
		}

		return rosterMap;
	}

	/**
	 * Returns comma-delimited string representation of the current course grades and corresponding university id
	 * for use with the "Load from file" option in SIS's system
	 * 
	 * To upload the grades in SIS's system, they must be formatted:
	 * University ID,Course Grade
	 * 
	 * Depending upon whether they are final or midterm grades, the first line must be designated "FIN" or "MID"
	 * 
	 * 
	 * @param origMap
	 * @return
	 */
	protected static String getCourseGradesInSISFormat(HashMap origMap, List enrollments, List gradableObjects, Map scoresMap) {
		StringBuffer sb = new StringBuffer();

		// is this midterm or final? - retrieve date range for FIN from sakai.properties
		// all other times it will be MID
		String finBeginDateStr = ServerConfigurationService.getString("exportGBForSIS.beginFin");
		String finEndDateStr = ServerConfigurationService.getString("exportGBForSIS.endFin");

		try{
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			Date finBeginDate = sdf.parse(finBeginDateStr.trim());
			Date finEndDate = sdf.parse(finEndDateStr.trim());
			Date currDate = new Date();

			if (currDate.after(finBeginDate) && currDate.before(finEndDate)) {
				sb.append("FIN" + "\n");
			}
			else {
				sb.append("MID" + "\n");
			}    
		} catch (Exception e){
			logger.error("Error determining date range for MID/FIN for export gradebook for SIS. Check sakai.properties settings");
			throw new RuntimeException("Error determining date range for MID/FIN for export gradebook for SIS. Check sakai.properties settings");
		}

		// iterate through the roster of students in the gradebook
		Collections.sort(enrollments, EnrollmentTableBean.ENROLLMENT_NAME_COMPARATOR);

		for(Iterator enrIter = enrollments.iterator(); enrIter.hasNext();) {
			EnrollmentRecord enr = (EnrollmentRecord)enrIter.next();
			String uid = enr.getUser().getUserUid();
			String univId = (String)origMap.get(uid);

			if (univId == null) {
				logger.warn("Univ Id not found for enrollee " + enr.getUser().getDisplayId());	
			}
			else {
				sb.append(univId + ",");
				Map studentMap = (Map)scoresMap.get(enr.getUser().getUserUid());
				if (studentMap == null) {
					studentMap = new HashMap();
				}

				for(Iterator goIter = gradableObjects.iterator(); goIter.hasNext();) {
					GradableObject go = (GradableObject)goIter.next();
					if(logger.isDebugEnabled()) logger.debug("userUid=" + enr.getUser().getUserUid() + ", go=" + go + ", studentMap=" + studentMap);
					Object cellValue = getScoreAsCellValue((studentMap != null) ? studentMap.get(go.getId()) : null);
					if(cellValue != null) {
						sb.append(cellValue);
					}
				}
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	private static Object getScoreAsCellValue(Object fromScoreMap) 
	{
		if (fromScoreMap == null) {
			return null;
		} else if (fromScoreMap instanceof AssignmentGradeRecord) {
			return ((AbstractGradeRecord)fromScoreMap).getPointsEarned();
		} else {
			return ((CourseGradeRecord)fromScoreMap).getDisplayGrade();
		}
	}
}

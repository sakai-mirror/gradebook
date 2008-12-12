/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.business.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.springframework.orm.hibernate3.HibernateCallback;

public class GradebookCalculationImpl extends GradebookManagerHibernateImpl
	implements GradebookManager 
{
	private static final Log log = LogFactory.getLog(GradebookCalculationImpl.class);

	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids) 
	{
		HibernateCallback hc = new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException 
			{
				if(studentUids == null || studentUids.size() == 0) 
				{
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords(CourseGrade, Collection).");
					return new ArrayList();
				}
				int gbGradeType = getGradebook(courseGrade.getGradebook().getId()).getGrade_type();
				//commented out for non-calculating grades. this may not be needed anymore
				//if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
				//{
				//	if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords(CourseGrade, Collection)");
				//	return new ArrayList();
				//}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Long gradebookId = courseGrade.getGradebook().getId();
				Gradebook gradebook = getGradebook(gradebookId);
				List cates = getCategories(gradebookId);
				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				// no need to calculate anything for non-calculating gradebook
				if (gbGradeType != GradebookService.GRADE_TYPE_LETTER)
				{
					for(Iterator iter = records.iterator(); iter.hasNext();) 
					{
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
						List totalEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session, gradebook, cates);
						double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
						double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
						double totalPointsPossible = getTotalPointsInternal(gradebookId, session, gradebook, cates, cgr.getStudentId());
						cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
						if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
					}
				}
				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}

	public List getPointsEarnedCourseGradeRecords(final CourseGrade courseGrade, final Collection studentUids, final Collection assignments, final Map gradeRecordMap) 
	{
		HibernateCallback hc = new HibernateCallback() 
		{
			public Object doInHibernate(Session session) throws HibernateException 
			{
				if(studentUids == null || studentUids.size() == 0) 
				{
					if(log.isInfoEnabled()) log.info("Returning no grade records for an empty collection of student UIDs in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords");
					return new ArrayList();
				}
				int gbGradeType = getGradebook(courseGrade.getGradebook().getId()).getGrade_type();
//				if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
//				{
//					if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getPointsEarnedCourseGradeRecords");
//					return new ArrayList();
//				}

				Query q = session.createQuery("from CourseGradeRecord as cgr where cgr.gradableObject.id=:gradableObjectId");
				q.setLong("gradableObjectId", courseGrade.getId().longValue());
				List records = filterAndPopulateCourseGradeRecordsByStudents(courseGrade, q.list(), studentUids);

				Gradebook gradebook = getGradebook(courseGrade.getGradebook().getId());
				List categories = getCategories(courseGrade.getGradebook().getId());

				Set assignmentsNotCounted = new HashSet();
				double totalPointsPossible = 0;
				Map cateTotalScoreMap = new HashMap();

				for (Iterator iter = assignments.iterator(); iter.hasNext(); ) 
				{
					Assignment assignment = (Assignment)iter.next();
					if (!assignment.isCounted() || assignment.getUngraded() || assignment.getPointsPossible().doubleValue() <= 0.0) 
					{
						assignmentsNotCounted.add(assignment.getId());
					}
				}
				if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) 
				{
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					double totalPointsEarned = 0;
					double literalTotalPointsEarned = 0;
					Map cateScoreMap = new HashMap();
					Map studentMap = (Map)gradeRecordMap.get(cgr.getStudentId());
					Set assignmentsTaken = new HashSet();
					if (studentMap != null) 
					{
						Collection studentGradeRecords = studentMap.values();
						for (Iterator gradeRecordIter = studentGradeRecords.iterator(); gradeRecordIter.hasNext(); ) 
						{
							AssignmentGradeRecord agr = (AssignmentGradeRecord)gradeRecordIter.next();
							if (!assignmentsNotCounted.contains(agr.getGradableObject().getId())) 
							{
								if(agr.getPointsEarned() != null && !agr.getPointsEarned().equals(""))
								{
									Double pointsEarned = new Double(agr.getPointsEarned());
									if (pointsEarned != null) 
									{
										if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
										{
											if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
											{
												totalPointsEarned += pointsEarned.doubleValue();
												literalTotalPointsEarned += pointsEarned.doubleValue();
												assignmentsTaken.add(agr.getAssignment().getId());
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && categories != null)
											{
												totalPointsEarned += pointsEarned.doubleValue();
												literalTotalPointsEarned += pointsEarned.doubleValue();
												assignmentsTaken.add(agr.getAssignment().getId());
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
											{
												for(int i=0; i<categories.size(); i++)
												{
													Category cate = (Category) categories.get(i);
													if(cate != null && !cate.isRemoved() && agr.getAssignment().getCategory() != null && cate.getId().equals(agr.getAssignment().getCategory().getId()))
													{
														assignmentsTaken.add(agr.getAssignment().getId());
														literalTotalPointsEarned += pointsEarned.doubleValue();
														if(cateScoreMap.get(cate.getId()) != null)
														{
															cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
														}
														else
														{
															cateScoreMap.put(cate.getId(), new Double(pointsEarned));
														}
														break;
													}
												}
											}
										}
										else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
										{
											if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
											{
												if(agr.getAssignment() != null && agr.getAssignment().getPointsPossible() != null)
												{
													totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
													literalTotalPointsEarned += pointsEarned.doubleValue();
													assignmentsTaken.add(agr.getAssignment().getId());
												}
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && categories != null)
											{
												if(agr.getAssignment() != null && agr.getAssignment().getPointsPossible() != null)
												{
													totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
													literalTotalPointsEarned += pointsEarned.doubleValue();
													assignmentsTaken.add(agr.getAssignment().getId());
												}
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
											{
												for(int i=0; i<categories.size(); i++)
												{
													Category cate = (Category) categories.get(i);
													if(cate != null && !cate.isRemoved() && agr.getAssignment().getCategory() != null && cate.getId().equals(agr.getAssignment().getCategory().getId()))
													{
														assignmentsTaken.add(agr.getAssignment().getId());
														literalTotalPointsEarned += pointsEarned.doubleValue();
														if(agr.getAssignment() != null && agr.getAssignment().getPointsPossible() != null)
														{
															if(cateScoreMap.get(cate.getId()) != null)
															{
																cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d)));
															}
															else
															{
																cateScoreMap.put(cate.getId(), new Double(pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d));
															}
														}
														break;
													}
												}
											}
										}
									}
								}
							}
						}

						cateTotalScoreMap.clear();
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
						{
							Iterator assignIter = assignments.iterator();
							while (assignIter.hasNext()) 
							{
								Assignment asgn = (Assignment)assignIter.next();
								if(assignmentsTaken.contains(asgn.getId()))
								{
									for(int i=0; i<categories.size(); i++)
									{
										Category cate = (Category) categories.get(i);
										if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()))
										{
											if(cateTotalScoreMap.get(cate.getId()) == null)
											{
												cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
											}
											else
											{
												cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
											}
										}
									}
								}
							}
						}

						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
								{
									totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
								}
							}
						}
					}

					totalPointsPossible = 0;
					if(!assignmentsTaken.isEmpty())
					{
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
								{
									totalPointsPossible += cate.getWeight().doubleValue();
								}
							}
						}
						Iterator assignIter = assignments.iterator();
						while (assignIter.hasNext()) 
						{
							Assignment assignment = (Assignment)assignIter.next();
							if(assignment != null)
							{
								Double pointsPossible = assignment.getPointsPossible();
								if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(assignment.getId()))
								{
									totalPointsPossible += pointsPossible.doubleValue();
								}
								else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(assignment.getId()))
								{
									totalPointsPossible += pointsPossible.doubleValue();
								}
							}
						}
					}
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}

	List getTotalPointsEarnedInternal(final Long gradebookId, final String studentId, final Session session, final Gradebook gradebook, final List categories) 
	{
		int gbGradeType = getGradebook(gradebookId).getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
			return new ArrayList();
		}

		double totalPointsEarned = 0;
		double literalTotalPointsEarned = 0;
		Iterator scoresIter = session.createQuery(
				"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.pointsPossible > 0 and asn.ungraded=false").
				setParameter("student", studentId).
				setParameter("gbid", gradebookId).
				list().iterator();

		List assgnsList = session.createQuery(
		"from Assignment as asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
		setParameter("gbid", gradebookId).
		list();

		Map cateScoreMap = new HashMap();
		Map cateTotalScoreMap = new HashMap();

		Set assignmentsTaken = new HashSet();
		while (scoresIter.hasNext()) 
		{
			Object[] returned = (Object[])scoresIter.next();
			if(returned[0] != null && !((String)returned[0]).equals(""))
			{
				Double pointsEarned = new Double((String)returned[0]);
				Assignment go = (Assignment) returned[1];
				if (go.isCounted() && pointsEarned != null) 
				{
					if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
					{
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
						{
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned += pointsEarned.doubleValue();
							assignmentsTaken.add(go.getId());
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
						{
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned += pointsEarned.doubleValue();
							assignmentsTaken.add(go.getId());
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
								{
									assignmentsTaken.add(go.getId());
									literalTotalPointsEarned += pointsEarned.doubleValue();
									if(cateScoreMap.get(cate.getId()) != null)
									{
										cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + pointsEarned.doubleValue()));
									}
									else
									{
										cateScoreMap.put(cate.getId(), new Double(pointsEarned));
									}
									break;
								}
							}
						}
					}
					else if(gbGradeType == GradebookService.GRADE_TYPE_PERCENTAGE)
					{
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
						{
							if(go.getPointsPossible() != null)
							{
								totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
								literalTotalPointsEarned += pointsEarned.doubleValue();
								assignmentsTaken.add(go.getId());
							}
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
						{
							if(go.getPointsPossible() != null)
							{
								totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
								literalTotalPointsEarned += pointsEarned.doubleValue();
								assignmentsTaken.add(go.getId());
							}
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
								{
									assignmentsTaken.add(go.getId());
									literalTotalPointsEarned += pointsEarned.doubleValue();
									if(go.getPointsPossible() != null)
									{
										if(cateScoreMap.get(cate.getId()) != null)
										{
											cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d)));
										}
										else
										{
											cateScoreMap.put(cate.getId(), new Double(pointsEarned * go.getPointsPossible() / 100.0d));
										}
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
		{
			Iterator assgnsIter = assgnsList.iterator();
			while (assgnsIter.hasNext()) 
			{
				Assignment asgn = (Assignment)assgnsIter.next();
				if(assignmentsTaken.contains(asgn.getId()))
				{
					for(int i=0; i<categories.size(); i++)
					{
						Category cate = (Category) categories.get(i);
						if(cate != null && !cate.isRemoved() && asgn.getCategory() != null && cate.getId().equals(asgn.getCategory().getId()))
						{

							if(cateTotalScoreMap.get(cate.getId()) == null)
							{
								cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
							}
							else
							{
								cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
							}

						}
					}
				}
			}
		}

		if(assignmentsTaken.isEmpty())
			totalPointsEarned = -1;

		if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
		{
			for(int i=0; i<categories.size(); i++)
			{
				Category cate = (Category) categories.get(i);
				if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
				{
					totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
				}
			}
		}

		if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
		List returnList = new ArrayList();
		returnList.add(new Double(totalPointsEarned));
		returnList.add(new Double(literalTotalPointsEarned));
		return returnList;
	}

	double getTotalPointsInternal(final Long gradebookId, Session session, final Gradebook gradebook, final List categories, final String studentId)
	{
		int gbGradeType = getGradebook(gradebookId).getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
			return -1;
		}

		double totalPointsPossible = 0;
		List assgnsList = session.createQuery(
		"select asn from Assignment asn where asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
		setParameter("gbid", gradebookId).
		list();

		Iterator scoresIter = session.createQuery(
		"select agr.pointsEarned, asn from AssignmentGradeRecord agr, Assignment asn where agr.gradableObject=asn and agr.studentId=:student and asn.gradebook.id=:gbid and asn.removed=false and asn.notCounted=false and asn.ungraded=false and asn.pointsPossible > 0").
		setParameter("student", studentId).
		setParameter("gbid", gradebookId).
		list().iterator();

		Set assignmentsTaken = new HashSet();
		Set categoryTaken = new HashSet();
		while (scoresIter.hasNext()) 
		{
			Object[] returned = (Object[])scoresIter.next();
			if(returned[0] != null && !((String)returned[0]).equals(""))
			{
				Double pointsEarned = new Double((String)returned[0]);
				Assignment go = (Assignment) returned[1];
				if (pointsEarned != null) 
				{
					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
					{
						assignmentsTaken.add(go.getId());
					}
					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
					{
						assignmentsTaken.add(go.getId());
					}
					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && go != null && categories != null)
					{
						for(int i=0; i<categories.size(); i++)
						{
							Category cate = (Category) categories.get(i);
							if(cate != null && !cate.isRemoved() && go.getCategory() != null && cate.getId().equals(go.getCategory().getId()))
							{
								assignmentsTaken.add(go.getId());
								categoryTaken.add(cate.getId());
								break;
							}
						}
					}
				}
			}
		}

		if(!assignmentsTaken.isEmpty())
		{
			if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
			{
				for(int i=0; i<categories.size(); i++)
				{
					Category cate = (Category) categories.get(i);
					if(cate != null && !cate.isRemoved() && categoryTaken.contains(cate.getId()) )
					{
						totalPointsPossible += cate.getWeight().doubleValue();
					}
				}
				return totalPointsPossible;
			}
			Iterator assignmentIter = assgnsList.iterator();
			while (assignmentIter.hasNext()) 
			{
				Assignment asn = (Assignment) assignmentIter.next();
				if(asn != null)
				{
					Double pointsPossible = asn.getPointsPossible();

					if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY && assignmentsTaken.contains(asn.getId()))
					{
						totalPointsPossible += pointsPossible.doubleValue();
					}
					else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(asn.getId()))
					{
						totalPointsPossible += pointsPossible.doubleValue();
					}
				}
			}
		}
		else
			totalPointsPossible = -1;

		return totalPointsPossible;
	}
}

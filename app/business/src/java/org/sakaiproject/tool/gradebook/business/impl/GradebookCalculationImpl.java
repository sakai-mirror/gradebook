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
import java.util.Collections;
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
				
				// get all of the AssignmentGradeRecords here to avoid repeated db calls
                Map<String, List<AssignmentGradeRecord>> gradeRecMap = getGradeRecordMapForStudents(session, gradebookId, studentUids);
                
                // get all of the counted assignments
                List<Assignment> countedAssigns = getCountedAssignments(session, gradebookId);

				//double totalPointsPossible = getTotalPointsInternal(gradebookId, session);
				//if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				// no need to calculate anything for non-calculating gradebook
				if (gbGradeType != GradebookService.GRADE_TYPE_LETTER)
				{
					for(Iterator iter = records.iterator(); iter.hasNext();) 
					{
						CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
						//double totalPointsEarned = getTotalPointsEarnedInternal(gradebookId, cgr.getStudentId(), session);
						List<AssignmentGradeRecord> studentGradeRecs = gradeRecMap.get(cgr.getStudentId());
						
						applyDropScores(studentGradeRecs);

						List totalEarned = getTotalPointsEarnedInternal(cgr.getStudentId(), gradebook, cates, studentGradeRecs, countedAssigns);
						double totalPointsEarned = ((Double)totalEarned.get(0)).doubleValue();
						double literalTotalPointsEarned = ((Double)totalEarned.get(1)).doubleValue();
						double courseGradePointsAdjustment = 0;
						if (cgr.getAdjustmentScore()!=null)
						{
							courseGradePointsAdjustment += cgr.getAdjustmentScore().doubleValue();
						}
						double totalPointsPossible = getTotalPointsInternal(gradebook, cates, cgr.getStudentId(), studentGradeRecs, countedAssigns);
						cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, literalTotalPointsEarned, courseGradePointsAdjustment);
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
					if (!assignment.isCounted() || assignment.getUngraded()) 
					{
						assignmentsNotCounted.add(assignment.getId());
					}
				}
				if(log.isDebugEnabled()) log.debug("Total points = " + totalPointsPossible);

				for(Iterator iter = records.iterator(); iter.hasNext();) 
				{
					CourseGradeRecord cgr = (CourseGradeRecord)iter.next();
					double totalPointsEarned = 0;
					double adjustmentPoints = 0;
					double courseGradePointsAdjustment = 0;
					if (cgr.getAdjustmentScore()!=null)
					{	
						courseGradePointsAdjustment += cgr.getAdjustmentScore().doubleValue();
					}
					BigDecimal literalTotalPointsEarned = new BigDecimal(0d);
					Map cateScoreMap = new HashMap();
					Map cateAdjustMap = new HashMap();
					Map studentMap = (Map)gradeRecordMap.get(cgr.getStudentId());
					Set assignmentsTaken = new HashSet();
					if (studentMap != null) 
					{
						Collection studentGradeRecords = studentMap.values();
						applyDropScores(studentGradeRecords);
						for (Iterator gradeRecordIter = studentGradeRecords.iterator(); gradeRecordIter.hasNext(); ) 
						{
							AssignmentGradeRecord agr = (AssignmentGradeRecord)gradeRecordIter.next();
							if (!assignmentsNotCounted.contains(agr.getGradableObject().getId()) && !agr.getDroppedFromGrade()) 
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
												literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
												assignmentsTaken.add(agr.getAssignment().getId());
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && categories != null)
											{
												totalPointsEarned += pointsEarned.doubleValue();
												literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
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
														literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
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
												if (agr.getAssignment() != null)
												{
													if(agr.getAssignment().getIsExtraCredit()!=null)
													{
														if(agr.getAssignment().getIsExtraCredit())
														{
															totalPointsEarned += pointsEarned.doubleValue() * 1d / 100.0d;
															literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
															assignmentsTaken.add(agr.getAssignment().getId());
														}
														else if(agr.getAssignment().getPointsPossible() != null)
														{
															totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
															literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
															assignmentsTaken.add(agr.getAssignment().getId());
														}
													}
													else if(agr.getAssignment().getPointsPossible() != null)
													{
														totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
														literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
														assignmentsTaken.add(agr.getAssignment().getId());
													}
												}
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && categories != null)
											{
												if (agr.getAssignment() != null)
												{
													if(agr.getAssignment().getIsExtraCredit()!=null)
													{
														if(agr.getAssignment().getIsExtraCredit())
														{
															totalPointsEarned += pointsEarned.doubleValue() * 1d / 100.0d;
															literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
															assignmentsTaken.add(agr.getAssignment().getId());
														}
														else if(agr.getAssignment().getPointsPossible() != null)
														{
															totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
															literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
															assignmentsTaken.add(agr.getAssignment().getId());
														}
													}
													else if(agr.getAssignment().getPointsPossible() != null)
													{
														totalPointsEarned += pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d;
														literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
														assignmentsTaken.add(agr.getAssignment().getId());
													}
												}
											}
											else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY && categories != null)
											{
												for(int i=0; i<categories.size(); i++)
												{
													Category cate = (Category) categories.get(i);
													if(cate != null && !cate.isRemoved() && agr.getAssignment().getCategory() != null && cate.getId().equals(agr.getAssignment().getCategory().getId()))
													{
														literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
														if(agr.getAssignment() != null)
														{
															if(agr.getAssignment().getIsExtraCredit()!=null)
															{
																if(agr.getAssignment().getIsExtraCredit())
																{
																	if(cateAdjustMap.get(cate.getId()) != null)
																	{
																		cateAdjustMap.put(cate.getId(), new Double(((Double)cateAdjustMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * 1d / 100.0d)));
																	}
																	else
																	{
																		cateAdjustMap.put(cate.getId(), new Double(pointsEarned.doubleValue() * 1d / 100.0d));
																	}
																}
																else if (agr.getAssignment().getPointsPossible() != null)
																{
																	assignmentsTaken.add(agr.getAssignment().getId());
																	if(cateScoreMap.get(cate.getId()) != null)
																	{
																		cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d)));
																	}
																	else
																	{
																		cateScoreMap.put(cate.getId(), new Double(pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d));
																	}
																}
															}
															else if (agr.getAssignment().getPointsPossible() != null)
															{
																assignmentsTaken.add(agr.getAssignment().getId());
																if(cateScoreMap.get(cate.getId()) != null)
																{
																	cateScoreMap.put(cate.getId(), new Double(((Double)cateScoreMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d)));
																}
																else
																{
																	cateScoreMap.put(cate.getId(), new Double(pointsEarned.doubleValue() * agr.getAssignment().getPointsPossible() / 100.0d));
																}
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
												if (asgn.getIsExtraCredit()!=null)
												{
													if (!asgn.getIsExtraCredit())
														cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
												}
												else
												{
													cateTotalScoreMap.put(cate.getId(), asgn.getPointsPossible());
												}
											}
											else
											{
												if (asgn.getIsExtraCredit()!=null)
												{
													if (!asgn.getIsExtraCredit())
														cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
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
						}

						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY)
						{
							for(int i=0; i<categories.size(); i++)
							{
								Category cate = (Category) categories.get(i);
								if(cate != null && !cate.isRemoved() && cateScoreMap.get(cate.getId()) != null && cateTotalScoreMap.get(cate.getId()) != null)
								{
									totalPointsEarned += ((Double)cateScoreMap.get(cate.getId())).doubleValue() * cate.getWeight().doubleValue() / ((Double)cateTotalScoreMap.get(cate.getId())).doubleValue();
									if(cateAdjustMap.get(cate.getId()) != null)
										totalPointsEarned += ((Double)cateAdjustMap.get(cate.getId())).doubleValue();
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
									if (assignment.getIsExtraCredit()!=null)
									{
										if (!assignment.getIsExtraCredit())
										{
											totalPointsPossible += pointsPossible.doubleValue();
										}
									}
									else
									{
										totalPointsPossible += pointsPossible.doubleValue();
									}
								}
								else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && assignmentsTaken.contains(assignment.getId()))
								{
									if (assignment.getIsExtraCredit()!=null)
									{
										if (!assignment.getIsExtraCredit())
										{
											totalPointsPossible += pointsPossible.doubleValue();
										}
									}
									else
									{
										totalPointsPossible += pointsPossible.doubleValue();
									}
								}
							}
						}
					}
					cgr.initNonpersistentFields(totalPointsPossible, totalPointsEarned, (new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue(), courseGradePointsAdjustment);
					if(log.isDebugEnabled()) log.debug("Points earned = " + cgr.getPointsEarned());
				}

				return records;
			}
		};
		return (List)getHibernateTemplate().execute(hc);
	}

	List getTotalPointsEarnedInternal(final String studentId, final Gradebook gradebook, final List categories,
	        final List<AssignmentGradeRecord> gradeRecs, List<Assignment> countedAssigns) 
	{
		int gbGradeType = gradebook.getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsEarnedInternal");
			return new ArrayList();
		}
		
		if (gradeRecs == null || countedAssigns == null) {
            if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for " +
                    "studentId=" + studentId + " returning 0 because null gradeRecs or countedAssigns");
            List returnList = new ArrayList();
            returnList.add(new Double(0));
            returnList.add(new Double(0));
            return returnList;
        }


		double totalPointsEarned = 0;
		BigDecimal literalTotalPointsEarned = new BigDecimal(0d);
		double adjustmentPoints = 0;

		Map cateScoreMap = new HashMap();
		Map cateTotalScoreMap = new HashMap();
		Map cateAdjustMap = new HashMap();

		Set assignmentsTaken = new HashSet();
		for (AssignmentGradeRecord gradeRec : gradeRecs)
		{
			if(gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("") && !gradeRec.getDroppedFromGrade())
			{
				Assignment go = gradeRec.getAssignment();
				if (go.isCounted() && !go.getUngraded()) 
				{
					Double pointsEarned = new Double(gradeRec.getPointsEarned());
					if(gbGradeType == GradebookService.GRADE_TYPE_POINTS)
					{
						if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_NO_CATEGORY)
						{
							if (go.getIsExtraCredit()!=null)
								if (go.getIsExtraCredit())
									adjustmentPoints += pointsEarned.doubleValue();
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
							assignmentsTaken.add(go.getId());
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
						{
							if (go.getIsExtraCredit()!=null)
								if (go.getIsExtraCredit())
									adjustmentPoints += pointsEarned.doubleValue();
							totalPointsEarned += pointsEarned.doubleValue();
							literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
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
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
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
							if (go.getIsExtraCredit()!=null)
							{
								if (go.getIsExtraCredit())
								{
									adjustmentPoints += pointsEarned.doubleValue() * 1d / 100.0d;
									totalPointsEarned += pointsEarned.doubleValue() * 1d / 100.0d;
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									assignmentsTaken.add(go.getId());
								}
								else if(go.getPointsPossible() != null)
								{
									totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									assignmentsTaken.add(go.getId());
								}
							}
							else if(go.getPointsPossible() != null)
							{
								totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
								literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
								assignmentsTaken.add(go.getId());
							}
						}
						else if(gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY && go != null)
						{
							if (go.getIsExtraCredit()!=null)
							{
								if (go.getIsExtraCredit())
								{
									adjustmentPoints += pointsEarned.doubleValue() * 1d / 100.0d;
									totalPointsEarned += pointsEarned.doubleValue() * 1d / 100.0d;
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									assignmentsTaken.add(go.getId());
								}
								else if(go.getPointsPossible() != null)
								{
									totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									assignmentsTaken.add(go.getId());
								}
							}
							else if(go.getPointsPossible() != null)
							{
								totalPointsEarned += pointsEarned.doubleValue() * go.getPointsPossible() / 100.0d;
								literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
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
									literalTotalPointsEarned = (new BigDecimal(pointsEarned.doubleValue())).add(literalTotalPointsEarned);
									if (go.getIsExtraCredit()!=null)
									{
										if (go.getIsExtraCredit())
										{
											adjustmentPoints += pointsEarned.doubleValue() * 1d / 100.0d;
											if(cateAdjustMap.get(cate.getId()) != null)
											{
												cateAdjustMap.put(cate.getId(), new Double(((Double)cateAdjustMap.get(cate.getId())).doubleValue() + (pointsEarned.doubleValue() * 1d / 100.0d)));
											}
											else
											{
												cateAdjustMap.put(cate.getId(), new Double(pointsEarned.doubleValue() * 1d / 100.0d));
											}
										}
										else if(go.getPointsPossible() != null)
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
									else if(go.getPointsPossible() != null)
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
			Iterator assgnsIter = countedAssigns.iterator();
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
								if (asgn.getIsExtraCredit()!=null)
								{
									if (!asgn.getIsExtraCredit())
										cateTotalScoreMap.put(cate.getId(), new Double(((Double)cateTotalScoreMap.get(cate.getId())).doubleValue() + asgn.getPointsPossible().doubleValue()));
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
					if(cateAdjustMap.get(cate.getId()) != null)
						totalPointsEarned += ((Double)cateAdjustMap.get(cate.getId())).doubleValue();
				}
			}
		}

		if (log.isDebugEnabled()) log.debug("getTotalPointsEarnedInternal for studentId=" + studentId + " returning " + totalPointsEarned);
		List returnList = new ArrayList();
		returnList.add(new Double(totalPointsEarned));
		returnList.add(new Double((new BigDecimal(literalTotalPointsEarned.doubleValue(), GradebookService.MATH_CONTEXT)).doubleValue()));
		returnList.add(new Double(adjustmentPoints));
		return returnList;
	}

	double getTotalPointsInternal(final Gradebook gradebook, final List categories, final String studentId, List<AssignmentGradeRecord> studentGradeRecs, List<Assignment> countedAssigns)
	{
		int gbGradeType = gradebook.getGrade_type();
		if( gbGradeType != GradebookService.GRADE_TYPE_POINTS && gbGradeType != GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			if(log.isInfoEnabled()) log.error("Wrong grade type in GradebookCalculationImpl.getTotalPointsInternal");
			return -1;
		}
		
		if (studentGradeRecs == null || countedAssigns == null) {
            if (log.isDebugEnabled()) log.debug("Returning 0 from getTotalPointsInternal " +
                    "since studentGradeRecs or countedAssigns was null");
            return 0;
        }
		
		double totalPointsPossible = 0;

		// we need to filter this list to identify only "counted" grade recs
        List<AssignmentGradeRecord> countedGradeRecs = new ArrayList<AssignmentGradeRecord>();
        for (AssignmentGradeRecord gradeRec : studentGradeRecs) {
            Assignment assign = gradeRec.getAssignment();
            boolean extraCredit = false;
            if (assign.getIsExtraCredit()!=null)
            	extraCredit = assign.getIsExtraCredit();
            if (assign.isCounted() && !assign.getUngraded() && !assign.isRemoved() && 
                    assign.getPointsPossible() != null && assign.getPointsPossible() > 0 && !gradeRec.getDroppedFromGrade() && !extraCredit) {
                countedGradeRecs.add(gradeRec);
            }
        }

		Set assignmentsTaken = new HashSet();
		Set categoryTaken = new HashSet();
		for (AssignmentGradeRecord gradeRec : countedGradeRecs)
		{
		    if (gradeRec.getPointsEarned() != null && !gradeRec.getPointsEarned().equals("")) 
		    {
		        Double pointsEarned = new Double(gradeRec.getPointsEarned());
		        Assignment go = gradeRec.getAssignment();
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
			Iterator assignmentIter = countedAssigns.iterator();
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
	
    public void applyDropScores(Collection<AssignmentGradeRecord> gradeRecords) {
        super.applyDropScores(gradeRecords);
    }
}

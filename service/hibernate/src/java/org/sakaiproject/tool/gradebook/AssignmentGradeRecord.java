/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook;

import java.math.BigDecimal;
import java.util.Comparator;

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 * An AssignmentGradeRecord is a grade record that can be associated with an
 * Assignment.
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class AssignmentGradeRecord extends AbstractGradeRecord {
    private String pointsEarned;
    private boolean userAbleToView;
    private Boolean excludedFromGrade;
    
    // used for drop highest/lowest score functionality
    private Boolean droppedFromGrade;

    public AssignmentGradeRecord() {
        super();
    }

    /**
     * The graderId and dateRecorded properties will be set explicitly by the
     * grade manager before the database is updated.
	 * @param assignment The assignment this grade record is associated with
     * @param studentId The student id for whom this grade record belongs
	 * @param grade The grade, or points earned
	 */
	public AssignmentGradeRecord(Assignment assignment, String studentId, String grade) {
        super();
        this.gradableObject = assignment;
        this.studentId = studentId;
        this.pointsEarned = grade == null ? null : grade.trim();
	}
	
    public static Comparator<AssignmentGradeRecord> calcComparator;
    public static Comparator<AssignmentGradeRecord> numericComparator;

    static {
        calcComparator = new Comparator<AssignmentGradeRecord>() {
            public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {
                if(agr1 == null && agr2 == null) {
                    return 0;
                }
                if(agr1 == null) {
                    return -1;
                }
                if(agr2 == null) {
                    return 1;
                }
                String agr1Points = agr1.getPointsEarned();
                String agr2Points = agr2.getPointsEarned();
                
                if (agr1Points == null && agr2Points == null) {
                    return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                    return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                    return 1;
                }
                return agr1Points.compareTo(agr2Points);
            }
        };
        numericComparator = new Comparator<AssignmentGradeRecord>() {
            public int compare(AssignmentGradeRecord agr1, AssignmentGradeRecord agr2) {
                if(agr1 == null && agr2 == null) {
                    return 0;
                }
                if(agr1 == null) {
                    return -1;
                }
                if(agr2 == null) {
                    return 1;
                }
                String agr1Points = agr1.getPointsEarned();
                String agr2Points = agr2.getPointsEarned();
                
                if (agr1Points == null && agr2Points == null) {
                    return 0;
                }
                if (agr1Points == null && agr2Points != null) {
                    return -1;
                }
                if (agr1Points != null && agr2Points == null) {
                    return 1;
                }
                Integer agr1PointsInt = Integer.parseInt(agr1Points);
                Integer agr2PointsInt = Integer.parseInt(agr2Points);
                return agr1PointsInt.compareTo(agr2PointsInt);
            }
        };
    }

    /**
     * @return Returns the pointsEarned
     */
    public String getPointsEarned() {
        return pointsEarned;
    }

	/**
	 * @param pointsEarned The pointsEarned to set.
	 */
	public void setPointsEarned(String pointsEarned) {
		this.pointsEarned = pointsEarned == null ? null : pointsEarned.trim();
	}

    /**
     * Returns null if the points earned is null.  Otherwise, returns earned / points possible * 100.
     *
     * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#getGradeAsPercentage()
     */
    public Double getGradeAsPercentage() {
        if (pointsEarned == null) {
            return null;
        }
        BigDecimal bdPointsEarned = new BigDecimal(pointsEarned.toString());
        BigDecimal bdPossible = new BigDecimal(((Assignment)getGradableObject()).getPointsPossible().toString());
        BigDecimal bdPercent = bdPointsEarned.divide(bdPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
        return new Double(bdPercent.doubleValue());
    }

    public Double getGradeAsPercentage(int gradebookType) {
      if (pointsEarned == null) {
          return null;
      }
      if(gradebookType == GradebookService.GRADE_TYPE_POINTS)
      {
      	BigDecimal bdPointsEarned = new BigDecimal(pointsEarned.toString());
      	BigDecimal bdPossible = new BigDecimal(((Assignment)getGradableObject()).getPointsPossible().toString());
      	BigDecimal bdPercent = bdPointsEarned.divide(bdPossible, GradebookService.MATH_CONTEXT).multiply(new BigDecimal("100"));
      	return new Double(bdPercent.doubleValue());
      }
      else if(gradebookType == GradebookService.GRADE_TYPE_PERCENTAGE)
      {
      	return new Double(pointsEarned);
      }
      else
      	return null;
    }	

    /**
	 * @see org.sakaiproject.tool.gradebook.AbstractGradeRecord#isCourseGradeRecord()
	 */
	public boolean isCourseGradeRecord() {
		return false;
	}

    public Assignment getAssignment() {
    	return (Assignment)getGradableObject();
    }
    
    public boolean isUserAbleToView() {
    	return userAbleToView;
    }
    public void setUserAbleToView(boolean userAbleToView) {
    	this.userAbleToView = userAbleToView;
    }

    public AssignmentGradeRecord clone()
    {
    	AssignmentGradeRecord agr = new AssignmentGradeRecord();
    	agr.setDateRecorded(dateRecorded);
    	agr.setGradableObject(gradableObject);
    	agr.setGraderId(graderId);
    	agr.setPointsEarned(pointsEarned);
    	agr.setStudentId(studentId);
    	return agr;
    }

    public Boolean isExcludedFromGrade() {
        return excludedFromGrade;
    }

	public void setExcludedFromGrade(Boolean isExcludedFromGrade) {
		this.excludedFromGrade = isExcludedFromGrade;
	}

    public Boolean getDroppedFromGrade() {
        return this.droppedFromGrade;
    }

    public void setDroppedFromGrade(Boolean droppedFromGrade) {
        this.droppedFromGrade = droppedFromGrade;
    }
}




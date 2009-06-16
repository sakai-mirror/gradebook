/**********************************************************************************
*
* $Id: AdjustmentCalculationsTest.java 63631 2009-06-12 20:26:28Z gjthomas@iupui.edu $
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

package org.sakaiproject.tool.gradebook.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.AssignmentGradeRecord;
import org.sakaiproject.tool.gradebook.CourseGrade;
import org.sakaiproject.tool.gradebook.CourseGradeRecord;
import org.sakaiproject.tool.gradebook.Gradebook;

import junit.framework.Assert;
import junit.framework.TestCase;


public class AdjustmentCalculationsTest extends TestCase {
	private Gradebook gradebook;
    private Assignment homework1points;
    private Assignment homework2points;
    private Assignment homework3points;
    private Assignment homework4points;
    private Assignment homework5points;
    private Assignment homework1percentage;
    private Assignment homework2percentage;
    private Assignment homework3percentage;
    private Assignment homework4percentage;
    private Assignment homework5percentage;
    private Set assignmentsPoints;
    private Set assignmentsPercentage;

	protected void setUp() throws Exception {
        Date now = new Date();

        gradebook = new Gradebook("Calculation Test GB");
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);

        homework1points = new Assignment(gradebook, "homework1points", new Double(200), now);
        homework2points = new Assignment(gradebook, "homework2points", new Double(300), now);
        homework3points = new Assignment(gradebook, "homework3points", new Double(400), now);
        // adjustment item with no points
        homework4points = new Assignment(gradebook, "homework4points", null, now);
        homework4points.setIsExtraCredit(true);
        // adjustment item with a points possible
        homework5points = new Assignment(gradebook, "homework5points", new Double(5), now);
        homework5points.setIsExtraCredit(true);
        
        homework1percentage = new Assignment(gradebook, "homework1percentage", new Double(1), now);
        homework2percentage = new Assignment(gradebook, "homework2percentage", new Double(1), now);
        homework3percentage = new Assignment(gradebook, "homework3percentage", new Double(1), now);
        // adjustment items
        homework4percentage = new Assignment(gradebook, "homework4percentage", null, now);
        homework4percentage.setIsExtraCredit(true);
        homework5percentage = new Assignment(gradebook, "homework5percentage", null, now);
        homework5percentage.setIsExtraCredit(true);
    }
	
	public static double getTotalPointsPossible(Collection assignments) {
		double total = 0;
		for (Iterator iter = assignments.iterator(); iter.hasNext();) {
			Assignment a = ((Assignment)iter.next());
			if (!a.getUngraded() && (a.getIsExtraCredit()==null || !a.getIsExtraCredit()) && a.isCounted())
			{
				total += a.getPointsPossible();
			}
		}
		return total;
	}
	
	public static Double getTotalPointsEarned(Collection gradeRecords) {
		double total = 0;
		boolean hasScores = false;
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
			if (agr.getAssignment().getGradebook().getGrade_type()==GradebookService.GRADE_TYPE_PERCENTAGE)
			{
				if (agr.getAssignment().getIsExtraCredit()==null || !agr.getAssignment().getIsExtraCredit())
				{
					total += new Double(agr.getPointsEarned());
					hasScores = true;
				}
			}
			else
			{
				total += new Double(agr.getPointsEarned());
				hasScores = true;
			}
		}
		return hasScores ? new Double(total) : null;
	}
	
	public static Double getAdjustmentPointsEarned(Collection gradeRecords) {
		double total = 0;
		boolean hasScores = false;
		for (Iterator iter = gradeRecords.iterator(); iter.hasNext();) {
			AssignmentGradeRecord agr = (AssignmentGradeRecord)iter.next();
			if (agr.getAssignment().getGradebook().getGrade_type()==GradebookService.GRADE_TYPE_PERCENTAGE && agr.getAssignment().getIsExtraCredit()!=null && agr.getAssignment().getIsExtraCredit())
			{
				total += new Double(agr.getPointsEarned());
				hasScores = true;
			}
		}
		return hasScores ? new Double(total) : null;
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//  POINTS TESTS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
     * Tests the course grade auto-calculation for a points gradebook with positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsPositive() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "3"));

        // The grade records should total 92%: (810 + 18) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, 0);
        Assert.assertEquals(new Double(92), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsNegative() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "-15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-3"));

        // The grade records should total 88%: (810 + -18) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, 0);
        Assert.assertEquals(new Double(88), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsMixed() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-6"));

        // The grade records should total 91%: (810 + (15 + -6)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, 0);
        Assert.assertEquals(new Double(91), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a positive course grade adjustment, no adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsPositiveCourseGradeAdjustment() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));

        // The grade records should total 95%: (810 + 45) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 45, 0);
        Assert.assertEquals(new Double(95), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a negative course grade adjustment, no adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsNegativeCourseGradeAdjustment() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));

        // The grade records should total 85%: (810 + -45) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -45, 0);
        Assert.assertEquals(new Double(85), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a positive course grade adjustment and positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsPositiveCourseGradeAdjustmentWithPositiveAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "3"));

        // The grade records should total 97%: (810 + (18 + 45)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 45, 0);
        Assert.assertEquals(new Double(97), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a positive course grade adjustment and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsPositiveCourseGradeAdjustmentWithNegativeAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "-15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-3"));

        // The grade records should total 93%: (810 + (-18 + 45)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 45, 0);
        Assert.assertEquals(new Double(93), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a positive course grade adjustment and both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsPositiveCourseGradeAdjustmentWithMixedAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-3"));

        // The grade records should total 96%: (810 + (12 + 42)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 42, 0);
        Assert.assertEquals(new Double(96), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a negative course grade adjustment and positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsNegativeCourseGradeAdjustmentWithPositiveAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "3"));

        // The grade records should total 87%: (810 + (18 + -45)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -45, 0);
        Assert.assertEquals(new Double(87), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a negative course grade adjustment and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsNegativeCourseGradeAdjustmentWithNegativeAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "-15"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-3"));

        // The grade records should total 83%: (810 + (-18 + -45)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -45, 0);
        Assert.assertEquals(new Double(83), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a points gradebook with a negative course grade adjustment and both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPointsNegativeCourseGradeAdjustmentWithMixedAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_POINTS);
        cg.setGradebook(gradebook);
        
        assignmentsPoints = new HashSet();
        assignmentsPoints.add(homework1points);
        assignmentsPoints.add(homework2points);
        assignmentsPoints.add(homework3points);
        assignmentsPoints.add(homework4points);
        assignmentsPoints.add(homework5points);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1points, "studentId", "110"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2points, "studentId", "300"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3points, "studentId", "400"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4points, "studentId", "12"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5points, "studentId", "-3"));

        // The grade records should total 86%: (810 + (9 + -45)) / 900
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPoints), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -45, 0);
        Assert.assertEquals(new Double(86), cgr.getAutoCalculatedGrade());
    }
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//   PERCENTAGE TESTS
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentagePositive() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "0.03"));

        // The grade records should total 86%: 78% + 8%(adjustment items)
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(86), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageNegative() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "-0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 70%: 78% - 8%(adjustment items)
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(70), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageMixed() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 80%: 78% + (5% + -3%)
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 0, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(80), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a positive course grade adjustment
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentagePositiveCourseGradeAdjustment() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));

        // The grade records should total 83%: 78% + 5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 5, 0);
        Assert.assertEquals(new Double(83), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a negative course grade adjustment
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageNegativeCourseGradeAdjustment() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));

        // The grade records should total 73%: 78% + -5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -5, 0);
        Assert.assertEquals(new Double(73), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a positive course grade adjustment and positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentagePositiveCourseGradeAdjustmentWithPositiveAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "0.03"));

        // The grade records should total 91%: 78% + 8% + 5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(91), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a positive course grade adjustment and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentagePositiveCourseGradeAdjustmentWithNegativeAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "-0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 75%: 78% + -8% + 5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(75), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a positive course grade adjustment and both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentagePositiveCourseGradeAdjustmentWithMixedAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 85%: 78% + 2% + 5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), 5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(85), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a negative course grade adjustment and positive adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageNegativeCourseGradeAdjustmentWithPositiveAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "0.03"));

        // The grade records should total 81%: 78% + 8% + -5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(81), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a negative course grade adjustment and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageNegativeCourseGradeAdjustmentWithNegativeAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "-0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 65%: 78% + -8% + -5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(65), cgr.getAutoCalculatedGrade());
    }
    
    /**
     * Tests the course grade auto-calculation for a pecentage gradebook with a negative course grade adjustment and both positive and negative adjustment items
     *
     * @throws Exception
     */
    public void testCourseGradeCalculationPercentageNegativeCourseGradeAdjustmentWithMixedAdjustmentItems() throws Exception {
        CourseGrade cg = new CourseGrade();
        gradebook.setGrade_type(GradebookService.GRADE_TYPE_PERCENTAGE);
        cg.setGradebook(gradebook);
        
        assignmentsPercentage = new HashSet();
        assignmentsPercentage.add(homework1percentage);
        assignmentsPercentage.add(homework2percentage);
        assignmentsPercentage.add(homework3percentage);
        assignmentsPercentage.add(homework4percentage);
        assignmentsPercentage.add(homework5percentage);

        List studentGradeRecords = new ArrayList();
        studentGradeRecords.add(new AssignmentGradeRecord(homework1percentage, "studentId", "0.90"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework2percentage, "studentId", "0.80"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework3percentage, "studentId", "0.64"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework4percentage, "studentId", "0.05"));
        studentGradeRecords.add(new AssignmentGradeRecord(homework5percentage, "studentId", "-0.03"));

        // The grade records should total 75%: 78% + 2% + -5%
        CourseGradeRecord cgr = new CourseGradeRecord();
        cgr.setStudentId("studentId");
        cgr.setGradableObject(cg);
        cgr.initNonpersistentFields(getTotalPointsPossible(assignmentsPercentage), getTotalPointsEarned(studentGradeRecords), getTotalPointsEarned(studentGradeRecords), -5, getAdjustmentPointsEarned(studentGradeRecords));
        Assert.assertEquals(new Double(75), cgr.getAutoCalculatedGrade());
    }
}

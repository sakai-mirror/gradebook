package org.sakaiproject.tool.gradebook.test;

import junit.framework.TestCase;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.Grade;
import org.sakaiproject.service.gradebook.shared.InvalidDecimalGradeException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.GradebookException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeLengthException;
import org.sakaiproject.service.gradebook.shared.NegativeGradeException;
import org.sakaiproject.service.gradebook.shared.NonNumericGradeException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GradeValidationTest extends TestCase 
{
	private static Log log = LogFactory.getLog(GradeValidationTest.class);

	protected void setUp() throws Exception 
	{
  }

  public void testNullGrade() throws Exception 
  {
  	Grade g = new Grade(null, GradebookService.GRADE_TYPE_POINTS, false, false);
  	g = new Grade("", GradebookService.GRADE_TYPE_POINTS, false, false);
  	g = new Grade(null, GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  	g = new Grade("", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  	g = new Grade(null, GradebookService.GRADE_TYPE_LETTER, false, false);
  	g = new Grade("", GradebookService.GRADE_TYPE_LETTER, false, false);
  	g = new Grade(null, GradebookService.GRADE_TYPE_POINTS, true, false);
  	g = new Grade("", GradebookService.GRADE_TYPE_PERCENTAGE, true, false);
  }

  public void testPointGrade() throws Exception 
  {
  	try
  	{
  		Grade g = new Grade("85.555", GradebookService.GRADE_TYPE_POINTS, false, false);
  		fail();
  	}
  	catch(InvalidDecimalGradeException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	try
  	{
  		Grade g = new Grade("-0.5", GradebookService.GRADE_TYPE_POINTS, false, false);
  		fail();
  	}
  	catch(NegativeGradeException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	try
  	{
  		Grade g = new Grade("abc", GradebookService.GRADE_TYPE_POINTS, false, false);
  		fail();
  	}
  	catch(NonNumericGradeException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	
		Grade g = new Grade("85.55", GradebookService.GRADE_TYPE_POINTS, false, false);
		g = new Grade("85.5", GradebookService.GRADE_TYPE_POINTS, false, false);
		g = new Grade("0", GradebookService.GRADE_TYPE_POINTS, false, false);
		g = new Grade("-0.0", GradebookService.GRADE_TYPE_POINTS, false, false);
  }

  public void testPercentGrade() throws Exception 
  {
  	try
  	{
  		Grade g = new Grade("85.555", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  		fail();
  	}
  	catch(InvalidDecimalGradeException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	try
  	{
  		Grade g = new Grade("abcd", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  		fail();
  	}
  	catch(NonNumericGradeException nfe)
  	{
  		log.info(nfe.getMessage());
  	}
  	try
  	{
  		Grade g = new Grade("-0.5", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  		fail();
  	}
  	catch(NegativeGradeException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	
	Grade g = new Grade("85.55", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
	g = new Grade("85.5", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
	g = new Grade("0", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
	g = new Grade("-0.0", GradebookService.GRADE_TYPE_PERCENTAGE, false, false);
  }
  
  public void testUngrade() throws Exception 
  {
  	try
  	{
  		Grade g = new Grade("85.555555", GradebookService.GRADE_TYPE_PERCENTAGE, true, false);
  		fail();
  	}
  	catch(InvalidGradeLengthException ige)
  	{
  		log.info(ige.getMessage());
  	}
  	
  	Grade g = new Grade("85.55555", GradebookService.GRADE_TYPE_PERCENTAGE, true, false);
  	g = new Grade("-56", GradebookService.GRADE_TYPE_POINTS, true, null);
  }
  
  public void testInvalidGradeType() throws Exception
  {
  	try
  	{
  		Grade g = new Grade("10.0", 0, false, false);
  		fail();
  	}
  	catch(GradebookException ge)
  	{
  		log.info(ge.getMessage());
  	}
  }
}

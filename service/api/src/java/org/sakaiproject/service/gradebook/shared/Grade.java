/**********************************************************************************
*
* $URL$
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
*
* Licensed under the Educational Community License Version 1.0 (the "License");
* By obtaining, using and/or copying this Original Work, you agree that you have read,
* understand, and will comply with the terms and conditions of the Educational Community License.
* You may obtain a copy of the License at:
*
*      http://www.opensource.org/licenses/ecl1.php
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
* INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
* AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
* DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*
**********************************************************************************/
package org.sakaiproject.service.gradebook.shared;

import java.math.BigDecimal;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;

/**
 *  Immutable class Grade for easy grade validation.
 *
 */
public final class Grade
{
	private final String grade;
	private final int grade_type;
	private final boolean ungraded;
	
	/**
	 * The only constructor for Grade. 
	 * Make sure to catch InvalidGradeException and GradebookException when create an object of Grade.
	 * For more specific exception information, catch NegativeGradeException, InvalidDecimalGradeException,
	 * InvalidGradeLengthException, NonNumericGradeException
	 * 
	 * @param grade
	 * @param grade_type
	 * @param ungraded
	 * @throws NegativeGradeException
	 * @throws InvalidDecimalGradeException
	 * @throws InvalidGradeLengthException
	 * @throws NonNumericGradeException
	 * @throws InvalidGradeException a catch-all for invalid grade exceptions
	 * @throws GradebookException
	 */
	public Grade(String grade, int grade_type, boolean ungraded)
	throws NegativeGradeException, InvalidDecimalGradeException, InvalidGradeLengthException, NonNumericGradeException,
	InvalidGradeException, GradebookException
	{
		try
		{
			boolean validBoolean = isValid(grade, grade_type, ungraded);
			if(!validBoolean)
			{
				throw new InvalidGradeException("Invalid grade:" + grade + " for grade_type of:" + grade_type);
			}
			else
			{
				this.grade = grade;
				this.grade_type = grade_type;
				this.ungraded = ungraded;		
			}
		}
		catch(NegativeGradeException nge)
		{
			throw new NegativeGradeException(nge.getMessage());
		}
		catch(InvalidDecimalGradeException idge)
		{
			throw new InvalidDecimalGradeException(idge.getMessage());
		}
		catch(NonNumericGradeException nnge) {
			throw new NonNumericGradeException(nnge.getMessage());
		}
		catch(InvalidGradeLengthException igle) {
			throw new InvalidGradeLengthException(igle.getMessage());
		}
		catch(InvalidGradeException ige)
		{
			throw new InvalidGradeException(ige.getMessage());
		}
		catch(GradebookException ge)
		{
			throw ge;
		}
	}

	public String getGrade()
	{
		return grade;
	}

	public int getGrade_type()
	{
		return grade_type;
	}

	public boolean isUngraded()
	{
		return ungraded;
	}
	
	public boolean isValid(String grade, int grade_type, boolean ungraded)
	{
		if(grade == null || grade.trim().equals(""))
			return true;
		else
		{
			if(grade.length() > GradebookService.MAX_GRADE_LENGTH)
				throw new InvalidGradeLengthException("grade length is greater than " + GradebookService.MAX_GRADE_LENGTH + " for: " + grade + " of grade_type of: " + grade_type + ". ungraded is:" + ungraded);
			
			if(grade_type != GradebookService.GRADE_TYPE_POINTS && grade_type != GradebookService.GRADE_TYPE_PERCENTAGE && grade_type != GradebookService.GRADE_TYPE_LETTER)
			{
				throw new GradebookException("Gradebook grade_type is invalid: it has to be 1, 2 or 3. Refer to GradebookService.");
			}
			else if( !ungraded && (grade_type == GradebookService.GRADE_TYPE_POINTS || grade_type == GradebookService.GRADE_TYPE_PERCENTAGE))
			{
				try
				{
					Double gradeDouble = new Double(grade);
					double gradeValue = gradeDouble.doubleValue();
					
					// ensure grade is >= 0
					if(gradeValue < 0.0d)
					{
						throw new NegativeGradeException("Grade: " + gradeValue + " is not valid for a " +
								"calculating gradebook item. Grade must be >= 0.");
					}
					
					// make sure there are at most 2 decimal places
					BigDecimal bd = new BigDecimal(gradeValue);
					bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
					double roundedVal = bd.doubleValue();
					double diff = gradeValue - roundedVal;
					if(diff != 0) 
					{
						throw new InvalidDecimalGradeException("Grade: " + grade + " is not valid for a " +
								"calculating gradebook. Grade must have no more than 2 decimal places.");
					}
					return true;
				}
				catch(NumberFormatException nfe)
				{
					throw new NonNumericGradeException("grade:" + grade +  " is not a number. A numeric value is required for a calculating gb item.");
				}
			}
			else if(ungraded || grade_type == GradebookService.GRADE_TYPE_LETTER)
			{
				return true;
			}
			return false;
		}
	}
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		if(!(obj instanceof Grade))
			return false;
		Grade go = (Grade) obj;
		if (go.grade != null)
			return go.ungraded == this.ungraded && 
			go.grade_type == this.grade_type &&
			go.grade.equals(this.grade);
		else if (this.grade != null)
			return go.ungraded == this.ungraded && 
			go.grade_type == this.grade_type &&
			this.grade.equals(go.grade);
		else
			return false;
	}
	
	public int hashCode()
	{
		int result = 17 + this.grade_type;
		result = 37 * result + Boolean.valueOf(this.ungraded).hashCode();
		result = 67 * result + this.grade.hashCode();
		return result;
	}
	
	public String toString()
	{
		if(grade != null)
			return grade.toString() + ":" + grade_type + ":"+ ungraded;
		else
			return "Null grade:" + grade_type + ":"+ ungraded;
	}
}
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

import org.sakaiproject.service.gradebook.shared.GradebookService;

/**
 *  Immutable class Grade for easy grade validation.
 *
 */
public final class Grade
{
	private final String grade;
	private final int grade_type;
	private final boolean ungraded;
	
	public Grade(String grade, int grade_type, boolean ungraded)
	{
		this.grade = grade;
		this.grade_type = grade_type;
		this.ungraded = ungraded;
		
		if(isValid())
		{
			//TODO: throw FormatException
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
	
	public boolean isValid()
	{
		//TODO: incomplete validation
		if(grade_type == GradebookService.GRADE_TYPE_POINTS)
		{		
		}
		return false;
	}
	
	public boolean equals(Object obj)
	{
		if(obj == this)
			return true;
		if(!(obj instanceof Grade))
			return false;
		Grade go = (Grade) obj;
		return go.ungraded == this.ungraded && 
			go.grade_type == this.grade_type &&
			go.grade.equals(this.grade);
	}
	
	public int hashCode()
	{
		int result = 17 + Float.floatToIntBits(this.grade_type);
		result = 37 * result + Boolean.valueOf(this.ungraded).hashCode();
		result = 37 * result + this.grade.hashCode();
		return result;
	}
}
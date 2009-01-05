/***********************************************************************************
*
* Copyright (c) 2007 The Regents of the University of California, The MIT Corporation
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


package org.sakaiproject.service.gradebook.shared;



/**
 * indicates that a grade has more than the allowed number of characters.
 * this maximum length is set in {@link GradebookService.MAX_GRADE_LENGTH}
 * 
 */
public class InvalidGradeLengthException extends InvalidGradeException {

	private static final long serialVersionUID = 1L;

	public InvalidGradeLengthException(String message) {
        super(message);
    }

}

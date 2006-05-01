/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2006 The Regents of the University of California
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

import java.util.*;

/**
 */
public class GradeMappingDefinition {
	private String uid;
	private String name;
	private List grades;
	private List defaultBottomScores;

	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List getGrades() {
		return grades;
	}
	public void setGrades(List grades) {
		this.grades = grades;
	}
	public List getDefaultBottomScores() {
		return defaultBottomScores;
	}
	public void setDefaultBottomScores(List defaultBottomScores) {
		this.defaultBottomScores = defaultBottomScores;
	}

}

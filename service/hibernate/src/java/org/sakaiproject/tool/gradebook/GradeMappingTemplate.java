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

package org.sakaiproject.tool.gradebook;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GradeMappingTemplate implements Serializable, Comparable {
	private static final Log log = LogFactory.getLog(GradeMappingTemplate.class);

	private Long id;
	private int version;

	private String uid;
	private String name;
	private List grades;
	private List defaultBottomScores;
	private boolean unavailable;

    public int compareTo(Object o) {
        return getName().compareTo(((GradeMappingTemplate)o).getName());
    }
    public String toString() {
        return new ToStringBuilder(this).
            append(getName()).toString();
    }
	public List getDefaultBottomScores() {
		return defaultBottomScores;
	}
	public void setDefaultBottomScores(List defaultBottomScores) {
		this.defaultBottomScores = defaultBottomScores;
	}
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
	public boolean isUnavailable() {
		return unavailable;
	}
	public void setUnavailable(boolean unavailable) {
		this.unavailable = unavailable;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
}

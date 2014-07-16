/**
 * Copyright 2013 Apereo Foundation Licensed under the
 * Educational Community License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://opensource.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.sakaiproject.gradebook.entity;

public class Category {
	public String name;
	public String weight;
	public String drop_lowest;
	public String dropHighest;
	public String keepHighest;
	
	public Category(String name, String weight,String drop_lowest, String dropHighest,String keepHighest) {
		this.name=name;
		this.weight=weight;
		this.drop_lowest=drop_lowest;
		this.dropHighest=dropHighest;
		this.keepHighest=keepHighest;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getWeight() {
		return weight;
	}

	public void setWeight(String weight) {
		this.weight = weight;
	}

	public String getDrop_lowest() {
		return drop_lowest;
	}

	public void setDrop_lowest(String drop_lowest) {
		this.drop_lowest = drop_lowest;
	}

	public String getDropHighest() {
		return dropHighest;
	}

	public void setDropHighest(String dropHighest) {
		this.dropHighest = dropHighest;
	}

	public String getKeepHighest() {
		return keepHighest;
	}

	public void setKeepHighest(String keepHighest) {
		this.keepHighest = keepHighest;
	}

}

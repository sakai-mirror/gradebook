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

package org.sakaiproject.tool.gradebook.ui;

/**
 * The interface between backing beans that need control over paged
 * tables and the paging controller itself.
 */
public interface Paging {
	public int getFirstRow();
	public void setFirstRow(int firstRow);
	public int getMaxDisplayedRows();
	public void setMaxDisplayedRows(int maxDisplayedRows);
	public int getDataRows();
}




/**********************************************************************************
*
* $Id: UserDirectoryServiceSakai2Impl.java 732 2006-07-11 17:50:23Z josh $
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

package org.sakaiproject.tool.gradebook.facades.sakai2impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.service.gradebook.shared.UnknownUserException;
import org.sakaiproject.service.legacy.user.User;
import org.sakaiproject.tool.gradebook.facades.UserDirectoryService;

/**
 * Sakai2 implementation of the gradebook UserDirectoryService API.
 */
public class UserDirectoryServiceSakai2Impl implements UserDirectoryService {
    private static final Log log = LogFactory.getLog(UserDirectoryServiceSakai2Impl.class);

    public String getUserDisplayName(String userUid) throws UnknownUserException {
            User sakaiUser;
			try {
				sakaiUser = org.sakaiproject.service.legacy.user.cover.UserDirectoryService.getUser(userUid);
			} catch (IdUnusedException e) {
	            throw new UnknownUserException("Unknown uid: " + userUid);
			}
            return sakaiUser.getDisplayName();
    }
}

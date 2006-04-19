/**********************************************************************************
*
* $Id$
*
***********************************************************************************
*
* Copyright (c) 2005, 2006 The Regents of the University of California, The MIT Corporation
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

package org.sakaiproject.tool.gradebook.jsf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import org.sakaiproject.jsf.util.JsfTool;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.facades.Authn;
import org.sakaiproject.tool.gradebook.facades.Authz;
import org.sakaiproject.tool.gradebook.facades.ContextManagement;

/**
 * Computes the default dispatch path for the user's role-appropriate view
 *
 * @author <a href="mailto:jholtzman@berkeley.edu">Josh Holtzman</a>
 */
public class GradebookJsfTool extends JsfTool {
    private static Log logger = LogFactory.getLog(GradebookJsfTool.class);
	protected String computeDefaultTarget() {
        if(logger.isInfoEnabled()) logger.info("Entering gradebook... determining role appropriate view");

        ApplicationContext ac = (ApplicationContext)getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        Authn authnService = (Authn)ac.getBean("org_sakaiproject_tool_gradebook_facades_Authn");
        Authz authzService = (Authz)ac.getBean("org_sakaiproject_tool_gradebook_facades_Authz");
        GradebookService gradebookService = (GradebookService)ac.getBean("org.sakaiproject.service.gradebook.GradebookService");
        ContextManagement contextManagementService = (ContextManagement)ac.getBean("org_sakaiproject_tool_gradebook_facades_ContextManagement");

        String userUid = authnService.getUserUid();
        String gradebookUid = contextManagementService.getGradebookUid(null);

		// If the Gradebook doesn't exist, give up.
		if(!gradebookService.isGradebookDefined(gradebookUid)) {
			throw new RuntimeException("Gradebook " + gradebookUid + " doesn't exist");
		}

        String target;
        if(authzService.isUserAbleToGrade(gradebookUid)) {
            if(logger.isInfoEnabled()) logger.info("Sending user to the overview page");
            target = "/overview";
        } else if (authzService.isUserAbleToViewOwnGrades(gradebookUid)) {
            if(logger.isInfoEnabled()) logger.info("Sending user to the student view page");
            target = "/studentView";
        } else {
            // The role filter has not been invoked yet, so this could happen here
            throw new RuntimeException("User " + userUid + " attempted to access gradebook " + gradebookUid + " without any role");
        }
        if(logger.isInfoEnabled()) logger.info("target = " + target);
        return target;
    }
}



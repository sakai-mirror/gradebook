/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California, The MIT Corporation
 *
 *  Licensed under the Educational Community License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.opensource.org/licenses/ecl1.php
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/

package org.sakaiproject.component.gradebook;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;

import org.sakaiproject.entitybroker.EntityBroker;
import org.sakaiproject.service.gradebook.shared.GradebookFeedService;
import org.sakaiproject.tool.cover.ToolManager;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 *
 */
public class GradebookFeedServiceImpl implements GradebookFeedService {

	private static final Log logger = LogFactory.getLog(GradebookFeedServiceImpl.class);
	
	private EntityBroker entityBroker;
	
	public void init(){
		if(logger.isDebugEnabled()) logger.debug("init");
	}
	
	public void addGradebookEventToFeed(String feedText, Date publishDate, Collection<String> recipients) {
		  // add this saving event to the feed for post recipients
		  if (entityBroker.entityExists("/feed-entity") && recipients != null) {

			  String url = entityBroker.getEntityURL("/feed-entity/");
			  HttpClient httpClient= new HttpClient();
			  PostMethod postMethod = new PostMethod(url);
			  postMethod.addParameter("markup", feedText);

			  // use a date which is related to the current users locale
			  DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			  postMethod.addParameter("publishTimeAsString", df.format(new Date()));
			  postMethod.addParameter("context", ToolManager.getCurrentPlacement().getContext());
			  postMethod.addParameter("author", "sakai.gradebook.tool");
			  postMethod.addParameter("surrogateKey", "");

			  for (String recip : recipients) {
				  postMethod.addParameter("recipients", recip);
			  }

			  try {
				  httpClient.executeMethod(postMethod);
			  } catch (IOException ioe) {
				  logger.warn("Unable to post new grade message to feed");
			  }

		  }
	  }


    /**
     *
     * @param eventTrackingService
     */
    public void setEntityBroker(EntityBroker entityBroker) {
        this.entityBroker = entityBroker;
    }
}

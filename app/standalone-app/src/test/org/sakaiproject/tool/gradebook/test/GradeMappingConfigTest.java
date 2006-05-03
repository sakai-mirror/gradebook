/**********************************************************************************
*
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
package org.sakaiproject.tool.gradebook.test;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class GradeMappingConfigTest extends GradeMappingTest {
	private static Log log = LogFactory.getLog(GradeMappingConfigTest.class);

    protected String[] getConfigLocations() {
        String[] configLocations = {"spring-db.xml", "spring-beans.xml", "spring-facades.xml",

        // Here's the big difference from the normal GradebookTestBase.
        "spring-service-with-grade-mappings-test.xml",

        "spring-hib.xml",
        	"spring-beans-test.xml",
        	"spring-hib-test.xml",
        	"classpath*:org/sakaiproject/component/section/support/spring-integrationSupport.xml",
			"classpath*:org/sakaiproject/component/section/spring-beans.xml",
			"classpath*:org/sakaiproject/component/section/spring-services.xml",
        };
        return configLocations;
    }

}

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

import junit.framework.Assert;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.sakaiproject.service.gradebook.shared.GradeMappingDefinition;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.GradeMapping;
import org.sakaiproject.tool.gradebook.GradeMappingTemplate;
import org.sakaiproject.tool.gradebook.LetterGradePlusMinusMapping;
import org.sakaiproject.tool.gradebook.test.support.BackwardCompatabilityBusiness;

public class GradeMappingTest extends GradebookTestBase {
	private static Log log = LogFactory.getLog(GradeMappingTest.class);

	private BackwardCompatabilityBusiness backwardCompatabilityBusiness;

    protected void onSetUpInTransaction() throws Exception {
    	super.onSetUpInTransaction();
        backwardCompatabilityBusiness = (BackwardCompatabilityBusiness)applicationContext.getBean("backwardCompatabilityBusiness");
	}

    public void testSetGradeMappings() throws Exception {
    	Collection grades;
    	List defaultValues;
    	GradeMapping gradeMapping;

        // By default, we get Letter Grades as a default mapping,
        // and three possible mappings per gradebook.
        String gradebook1Name = "SetGradeMappingsTest1";
        gradebookService.addGradebook(gradebook1Name, gradebook1Name);
        Gradebook gradebook1 = gradebookManager.getGradebook(gradebook1Name);
        gradeMapping = gradebook1.getSelectedGradeMapping();
        GradeMapping oldStaticDefault = new LetterGradePlusMinusMapping();
        Assert.assertTrue(gradeMapping.getName().equals(oldStaticDefault.getName()));
log.warn("gradebook1.getGradeMappings()=" + gradebook1.getGradeMappings());
        Assert.assertTrue(gradebook1.getGradeMappings().size() == 3);

        // Now make LetterGradeMapping the default.
        gradebookService.setDefaultGradeMapping("LetterGradeMapping");
        String gradebook2Name = "SetGradeMappingsTest2";
        gradebookService.addGradebook(gradebook2Name, gradebook2Name);
        Gradebook gradebook2 = gradebookManager.getGradebook(gradebook2Name);
        gradeMapping = gradebook2.getSelectedGradeMapping();
        GradeMappingTemplate letterGradeMappingTemplate = gradeMapping.getGradeMappingTemplate();
        Assert.assertTrue(gradeMapping.getName().equals("Letter Grades"));
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(90)));

        // Now replace the LetterGradePlusMinusMapping with LoseWinScale,
        // and change the default values of LetterGradeMapping.
        List newMappings = new ArrayList();
        GradeMappingDefinition def = new GradeMappingDefinition();
        def.setUid("LoseWinScale");
        def.setName("Win, Lose, or Draw");
        def.setGrades(Arrays.asList(new Object[] {"Win", "Draw", "Lose"}));
        def.setDefaultBottomScores(Arrays.asList(new Object[] {new Double(80), new Double(40), new Double(0)}));
        newMappings.add(def);
        def = new GradeMappingDefinition();
        def.setUid(letterGradeMappingTemplate.getUid());
        def.setName(letterGradeMappingTemplate.getName());
        def.setGrades(new ArrayList(letterGradeMappingTemplate.getGrades()));
        List bottomScores = letterGradeMappingTemplate.getDefaultBottomScores();
        bottomScores.set(0, new Double(89));
        def.setDefaultBottomScores(bottomScores);
        newMappings.add(def);
        gradebookService.setAvailableGradeMappings(newMappings);

        // Make sure a new gradebook is as expected.
        String gradebook3Name = "SetGradeMappingsTest3";
        gradebookService.addGradebook(gradebook3Name, gradebook3Name);
        Gradebook gradebook3 = gradebookManager.getGradebook(gradebook3Name);
        gradeMapping = gradebook3.getSelectedGradeMapping();
log.warn("gradeMapping name=" + gradeMapping.getName());
		Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(89)));
		Assert.assertTrue(gradebook3.getGradeMappings().size() == 2);
		GradeMapping newGradeMapping = null;
		for (Iterator iter = gradebook3.getGradeMappings().iterator(); iter.hasNext() && (newGradeMapping == null); ) {
			GradeMapping gm = (GradeMapping)iter.next();
			if (!gm.getId().equals(gradeMapping.getId())) {
				newGradeMapping = gm;
			}
		}
		gradebook3.setSelectedGradeMapping(newGradeMapping);
		gradebookManager.updateGradebook(gradebook3);
		Assert.assertTrue(gradebook3.getSelectedGradeMapping().getName().equals("Win, Lose, or Draw"));

		// Make sure the old gradebook doesn't change until we tell it to.
		gradebook2 = gradebookManager.getGradebook(gradebook2Name);
        gradeMapping = gradebook2.getSelectedGradeMapping();
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(90)));
        gradeMapping.setDefaultValues();
        Assert.assertTrue(gradeMapping.getValue("A").equals(new Double(89)));
    }

    public void testBackwardCompatability() throws Exception {
        String gradebookName = "PreTemplateGB";
        backwardCompatabilityBusiness.addGradebook(gradebookName, gradebookName);
        Gradebook gradebook = gradebookManager.getGradebook(gradebookName);

		// Play the old songs.
        GradeMapping gradeMapping = gradebook.getSelectedGradeMapping();
        GradeMapping oldStaticDefault = new LetterGradePlusMinusMapping();
        Assert.assertTrue(gradeMapping.getName().equals(oldStaticDefault.getName()));
log.warn("gradebook.getGradeMappings()=" + gradebook.getGradeMappings());
        Assert.assertTrue(gradebook.getGradeMappings().size() == 3);
    }

}

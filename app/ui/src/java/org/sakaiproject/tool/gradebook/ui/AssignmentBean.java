/**********************************************************************************
*
* $Id: AssignmentBean.java  $
*
***********************************************************************************
*
* Copyright (c) 2005, 2006, 2007 The Regents of the University of California, The MIT Corporation
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

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.section.api.coursemanagement.EnrollmentRecord;
import org.sakaiproject.section.api.facade.Role;
import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.StaleObjectModificationException;
import org.sakaiproject.tool.cover.SessionManager;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.jsf.FacesUtil;
import org.sakaiproject.service.gradebook.shared.MultipleAssignmentSavingException;
import org.sakaiproject.service.gradebook.shared.GradebookService;

public class AssignmentBean extends GradebookDependentBean implements Serializable {
	private static final Log logger = LogFactory.getLog(AssignmentBean.class);

	private Long assignmentId;
    private Assignment assignment;
    private List categoriesSelectList;
    private List categoriesAdjustmentSelectList;
    private String assignmentCategory;
    private List gradeEntrySelectList;
    private boolean isBulkDisplay = false;
    private boolean releaseChange = true;
    private boolean countedChange = true;
    private boolean isNonCalc;
    private Category selectedCategory;
    private boolean selectedCategoryDropsScores;
    
    // added to support bulk gradebook item creation
    public List newBulkItems; 
    public int numTotalItems = 1;
    public List addItemSelectList;
    public List addBulkItemSelectList;
    public List newBulkGradebookItems;
    
    private Gradebook localGradebook;
    private String selectGradebookItem;
    public String selectBulkGradebookItem;
    public String itemTitleChange;
    public String pointsPossibleChange;
    public Date dueDateChange;
    public String gradeEntryType;
    public String categoryEntry;

    
    public static final String UNASSIGNED_CATEGORY = "unassigned";
    public static final String GB_POINTS_ENTRY = "Points";
    public static final String GB_PERCENTAGE_ENTRY = "Percentage";
    public static final String GB_NON_CALCULATING_ENTRY = "Non-calculating";
    public static final String GB_ADJUSTMENT_ENTRY = "Adjustment";
    private static final String GB_ADD_ASSIGNMENT_PAGE = "addAssignment";
    
    
    /** 
     * To add the proper number of blank gradebook item objects for bulk creation 
     */
    private static final int NUM_EXTRA_ASSIGNMENT_ENTRIES = 50;
    
    private static final String GB_ITEM_INDIVIDUAL = "individual";
    private static final String GB_ITEM_BULK = "bulk";
    private static final String GB_ADD_BULK_DEFAULT = "5";
    private static final int NUM_EXTRA_GRADEBOOK_ENTRIES = 50;
    
	protected void init() {
		if (logger.isDebugEnabled()) logger.debug("init assignment=" + assignment);

		if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new Assignment();
				assignment.setReleased(true);
			}
		}
		
		if (localGradebook == null)
		{
			localGradebook = getGradebook();
			selectGradebookItem = GB_ITEM_INDIVIDUAL;
			selectBulkGradebookItem = GB_ADD_BULK_DEFAULT;
		}
		
		gradeEntrySelectList = new ArrayList();
		
		if (localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_POINTS) 
		{
			gradeEntrySelectList.add(new SelectItem(GB_POINTS_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_points")));
			gradeEntrySelectList.add(new SelectItem(GB_NON_CALCULATING_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_noncalc")));
			gradeEntrySelectList.add(new SelectItem(GB_ADJUSTMENT_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_adjustment")));
			if (assignment.getUngraded()) {
				assignment.selectedGradeEntryValue = GB_NON_CALCULATING_ENTRY;
			}
			if (assignment.getIsExtraCredit()!=null) {
				if (assignment.getIsExtraCredit())
				{
					assignment.selectedGradeEntryValue = GB_ADJUSTMENT_ENTRY;
				}
			}
		}
		else if (localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_PERCENTAGE)
		{
			gradeEntrySelectList.add(new SelectItem(GB_PERCENTAGE_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_percentage")));
			gradeEntrySelectList.add(new SelectItem(GB_NON_CALCULATING_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_noncalc")));
			gradeEntrySelectList.add(new SelectItem(GB_ADJUSTMENT_ENTRY, FacesUtil.getLocalizedString("add_assignment_type_adjustment")));
			if (assignment.getUngraded()) {
				assignment.selectedGradeEntryValue = GB_NON_CALCULATING_ENTRY;
			}
			if (assignment.getIsExtraCredit()!=null) {
				if (assignment.getIsExtraCredit())
				{
					assignment.selectedGradeEntryValue = GB_ADJUSTMENT_ENTRY;
				}
			}
		}
		else if (localGradebook.getGrade_type()==GradebookService.GRADE_TYPE_LETTER)
		{
			assignment.selectedGradeEntryValue = GB_NON_CALCULATING_ENTRY;
		}
		
		addBulkItemSelectList = new ArrayList();
		for (int i = 1; i < NUM_EXTRA_GRADEBOOK_ENTRIES + 1; i++) {
			addBulkItemSelectList.add(new SelectItem(new Integer(i).toString(), new Integer(i).toString()));
		}
		
		Category assignCategory = assignment.getCategory();
		if (assignCategory != null) {
			assignmentCategory = assignCategory.getId().toString();
		}
		else {
			assignmentCategory = getLocalizedString("cat_unassigned");
		}
		
		categoriesSelectList = new ArrayList();
		categoriesAdjustmentSelectList = new ArrayList();

		// The first choice is always "Unassigned"
		categoriesSelectList.add(new SelectItem(UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		categoriesAdjustmentSelectList.add(new SelectItem(UNASSIGNED_CATEGORY, FacesUtil.getLocalizedString("cat_unassigned")));
		List gbCategories = getGradebookManager().getCategories(getGradebookId());
		if (gbCategories != null && gbCategories.size() > 0)
		{
			Iterator catIter = gbCategories.iterator();
			while (catIter.hasNext()) {
				Category cat = (Category) catIter.next();
				categoriesSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
				if (cat.getIsExtraCredit()!=null)
				{
					if (!cat.getIsExtraCredit())
					{
						categoriesAdjustmentSelectList.add(new SelectItem(cat.getId().toString(), cat.getName()));
					}
				}
			}
		}

		// To support bulk creation of assignments
		if (newBulkItems == null) {
			newBulkItems = new ArrayList();
		}

		// initialize the number of items to add dropdown
		addItemSelectList = new ArrayList();
		addItemSelectList.add(new SelectItem("0", ""));
		for (int i = 1; i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			addItemSelectList.add(new SelectItem(new Integer(i).toString(), new Integer(i).toString()));
		}

		for (int i = newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {			
			BulkAssignmentDecoratedBean item = getNewAssignment();
			
			if (i == 0) {
				item.setSaveThisItem("true");
			}
			
			newBulkItems.add(item);
		}
		
	}

	private BulkAssignmentDecoratedBean getNewAssignment() {
		Assignment assignment = new Assignment();
		assignment.setReleased(true);
		if(selectedCategory != null && selectedCategory.isDropScores()) {
		    assignment.setPointsPossible(selectedCategory.getPointValue());
		}
		BulkAssignmentDecoratedBean bulkAssignmentDecoBean = new BulkAssignmentDecoratedBean(assignment, getItemCategoryString(assignment));

		return bulkAssignmentDecoBean;
	}

	private String getItemCategoryString(Assignment assignment) {
		String assignmentCategory;
		Category assignCategory = assignment.getCategory();
		if (assignCategory != null) {
			assignmentCategory = assignCategory.getId().toString();
		}
		else {
			assignmentCategory = getLocalizedString("cat_unassigned");
		}
		
		return assignmentCategory;
	}
	
	public boolean isGradesExistInAssignment() {
		boolean gradesExist = getGradebookManager().isEnteredAssignmentScores(assignmentId);
		return gradesExist;
	}
	
	public boolean isNoncalcWithGrades() {
		if (getGradebook().getGrade_type()==GradebookService.GRADE_TYPE_LETTER)
		{
			return false;
		}
		else if (isGradesExistInAssignment() && assignment.getUngraded())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * Used to check if all gradebook items are valid before saving. Due to the way JSF works, had to turn off
	 * validators for bulk assignments so had to perform checks here.
	 * This is an all-or-nothing save, ie, either all items are OK and we save them all, or return to add page
	 * and highlight errors.
	 */
	public String saveNewAssignment() {
		String resultString = "overview";
		boolean saveAll = true;

		// keep list of new assignment names just in case
		// duplicates entered on screen
		List newAssignmentNameList = new ArrayList();
		
		// used to hold assignments that are OK since we 
		// need to determine if all are correct before saving
		List itemsToSave = new ArrayList();

		Iterator assignIter = newBulkItems.iterator();
		int i = 0;
		while (i < numTotalItems && assignIter.hasNext()) {
			BulkAssignmentDecoratedBean bulkAssignDecoBean = (BulkAssignmentDecoratedBean) assignIter.next();
			
			if (bulkAssignDecoBean.getBlnSaveThisItem()) {
				Assignment bulkAssignment = bulkAssignDecoBean.getAssignment();
			
				// Check for blank entry else check if duplicate within items to be
				// added or with item currently in gradebook.
				if ("".equals(bulkAssignment.getName().toString().trim())) {
					bulkAssignDecoBean.setBulkNoTitleError("blank");
					saveAll = false;
					resultString = "failure";
				}
				else if (newAssignmentNameList.contains(bulkAssignment.getName().trim()) ||
						 ! getGradebookManager().checkValidName(getGradebookId(), bulkAssignment)){
					bulkAssignDecoBean.setBulkNoTitleError("dup");
					saveAll = false;
					resultString = "failure";
				}
				else {
					bulkAssignDecoBean.setBulkNoTitleError("OK");
					newAssignmentNameList.add(bulkAssignment.getName().trim());
				}

				boolean adjustmentWithNoPoints = false; // used for some logic later
				
				// if ungraded, we don't care about points possible
				if (bulkAssignDecoBean.getSelectedGradeEntryValue().equals(GB_NON_CALCULATING_ENTRY)) {
					bulkAssignment.setUngraded(true);
				}
				if (bulkAssignDecoBean.getSelectedGradeEntryValue().equals(GB_ADJUSTMENT_ENTRY)) {
					bulkAssignment.setIsExtraCredit(true);
					bulkAssignment.setUngraded(false); // extra insurance
					if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
						adjustmentWithNoPoints = true;
					}
				}
				if (!bulkAssignment.getUngraded() && getGradebook().getGrade_type()!=GradebookService.GRADE_TYPE_LETTER)
				{
					// Check if points possible is blank else convert to double. Exception at else point
					// means non-numeric value entered.
					if (adjustmentWithNoPoints)
					{
						// if this is the case, we want to skip the rest of this stuff as its ok
						bulkAssignDecoBean.setBulkNoPointsError("OK");
					}
					else if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
						bulkAssignDecoBean.setBulkNoPointsError("blank");
						saveAll = false;
						resultString = "failure";
					}
					else {
						try {
							double dblPointsPossible = new Double(bulkAssignDecoBean.getPointsPossible()).doubleValue();
	
							// Added per SAK-13459: did not validate if point value was valid (> zero)
							if (dblPointsPossible > 0) {
								// No more than 2 decimal places can be entered.
								BigDecimal bd = new BigDecimal(dblPointsPossible);
								bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
								double roundedVal = bd.doubleValue();
								double diff = dblPointsPossible - roundedVal;
								if(diff != 0) {
									saveAll = false;
									resultString = "failure";
									bulkAssignDecoBean.setBulkNoPointsError("precision");
								}
								else {
									bulkAssignDecoBean.setBulkNoPointsError("OK");
									bulkAssignDecoBean.getAssignment().setPointsPossible(new Double(bulkAssignDecoBean.getPointsPossible()));
								}
							}
							else {
								saveAll = false;
								resultString = "failure";
								bulkAssignDecoBean.setBulkNoPointsError("invalid");
							}
						}
						catch (Exception e) {
							bulkAssignDecoBean.setBulkNoPointsError("NaN");
							saveAll = false;
							resultString = "failure";
						}
					}
				}
				else
				{
					// extra insurance to make sure these fields are blank
					bulkAssignDecoBean.getAssignment().setCounted(false);
					bulkAssignDecoBean.getAssignment().setPointsPossible(null);
				}
			
				if (saveAll) {
					bulkAssignDecoBean.getAssignment().setCategory(retrieveSelectedCategory(bulkAssignDecoBean.getCategory()));
			    	itemsToSave.add(bulkAssignDecoBean.getAssignment());
				}

				// Even if errors increment since we need to go back to add page
		    	i++;
			}
		}

		// Now ready to save, the only problem is due to duplicate names.
		if (saveAll) {
			try {
				getGradebookManager().createAssignments(getGradebookId(), itemsToSave);
				
				for (Iterator gbItemIter = itemsToSave.iterator(); gbItemIter.hasNext();) {
					FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", 
									new String[] {((Assignment) gbItemIter.next()).getName()}));
				}
			}
			catch (MultipleAssignmentSavingException e) {
				FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
				resultString = "failure";
			}
		}
		else {
			// There are errors so need to put an error message at top
			FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
		}
		
		return resultString;
	}
	
	public String saveNewBulkAssignment() {
		String resultString = "overview";
		
		boolean saveAll = true;
		
		// keep list of new assignment names just in case
		// duplicates entered on screen
		List newAssignmentNameList = new ArrayList();
		
		// used to hold assignments that are OK since we 
		// need to determine if all are correct before saving
		List itemsToSave = new ArrayList();

		Iterator assignIter = newBulkGradebookItems.iterator();
		int i = 0;
		int numTotalBulkItems = Integer.parseInt(selectBulkGradebookItem);
		while (i < numTotalBulkItems && assignIter.hasNext()) {
			BulkAssignmentDecoratedBean bulkAssignDecoBean = (BulkAssignmentDecoratedBean) assignIter.next();
			
			Assignment bulkAssignment = bulkAssignDecoBean.getAssignment();
			
			bulkAssignDecoBean.setSelectedGradeEntryValue(gradeEntryType);
			
			bulkAssignDecoBean.setCategory(categoryEntry);
		
			// Check for blank entry else check if duplicate within items to be
			// added or with item currently in gradebook.
			if ("".equals(bulkAssignment.getName().toString().trim())) {
				bulkAssignDecoBean.setBulkNoTitleError("blank");
				saveAll = false;
				resultString = "failure";
			}
			else if (newAssignmentNameList.contains(bulkAssignment.getName().trim()) ||
					 ! getGradebookManager().checkValidName(getGradebookId(), bulkAssignment)){
				bulkAssignDecoBean.setBulkNoTitleError("dup");
				saveAll = false;
				resultString = "failure";
			}
			else {
				bulkAssignDecoBean.setBulkNoTitleError("OK");
				newAssignmentNameList.add(bulkAssignment.getName().trim());
			}
			
			boolean adjustmentWithNoPoints = false; // used for some logic later

			// if ungraded, we don't care about points possible
			if (bulkAssignDecoBean.getSelectedGradeEntryValue().equals(GB_NON_CALCULATING_ENTRY)) {
				bulkAssignment.setUngraded(true);
			}
			if (bulkAssignDecoBean.getSelectedGradeEntryValue().equals(GB_ADJUSTMENT_ENTRY)) {
				bulkAssignment.setIsExtraCredit(true);
				bulkAssignment.setUngraded(false); // extra insurance
				if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
					adjustmentWithNoPoints = true;
				}
			}
			if (!bulkAssignment.getUngraded() && getGradebook().getGrade_type()!=GradebookService.GRADE_TYPE_LETTER)
			{
				// Check if points possible is blank else convert to double. Exception at else point
				// means non-numeric value entered.
				if (adjustmentWithNoPoints)
				{
					// if this is the case, we want to skip the rest of this stuff as its ok
					bulkAssignDecoBean.setBulkNoPointsError("OK");
				}
				else if (bulkAssignDecoBean.getPointsPossible() == null || ("".equals(bulkAssignDecoBean.getPointsPossible().trim()))) {
					bulkAssignDecoBean.setBulkNoPointsError("blank");
					saveAll = false;
					resultString = "failure";
				}
				else {
					try {
						double dblPointsPossible = new Double(bulkAssignDecoBean.getPointsPossible()).doubleValue();

						// Added per SAK-13459: did not validate if point value was valid (> zero)
						if (dblPointsPossible > 0) {
							// No more than 2 decimal places can be entered.
							BigDecimal bd = new BigDecimal(dblPointsPossible);
							bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP); // Two decimal places
							double roundedVal = bd.doubleValue();
							double diff = dblPointsPossible - roundedVal;
							if(diff != 0) {
								saveAll = false;
								resultString = "failure";
								bulkAssignDecoBean.setBulkNoPointsError("precision");
							}
							else {
								bulkAssignDecoBean.setBulkNoPointsError("OK");
								bulkAssignDecoBean.getAssignment().setPointsPossible(new Double(bulkAssignDecoBean.getPointsPossible()));
							}
						}
						else {
							saveAll = false;
							resultString = "failure";
							bulkAssignDecoBean.setBulkNoPointsError("invalid");
						}
					}
					catch (Exception e) {
						bulkAssignDecoBean.setBulkNoPointsError("NaN");
						saveAll = false;
						resultString = "failure";
					}
				}
			}
			else
			{
				// extra insurance to make sure these fields are blank
				bulkAssignDecoBean.getAssignment().setCounted(false);
				bulkAssignDecoBean.getAssignment().setPointsPossible(null);
			}
		
			if (saveAll) {
				bulkAssignDecoBean.getAssignment().setCategory(retrieveSelectedCategory(bulkAssignDecoBean.getCategory()));
		    	itemsToSave.add(bulkAssignDecoBean.getAssignment());
			}

			// Even if errors increment since we need to go back to add page
	    	i++;
		}

	// Now ready to save, the only problem is due to duplicate names.
	if (saveAll) {
		try {
			getGradebookManager().createAssignments(getGradebookId(), itemsToSave);
			
			for (Iterator gbItemIter = itemsToSave.iterator(); gbItemIter.hasNext();) {
				FacesUtil.addRedirectSafeMessage(getLocalizedString("add_assignment_save", 
								new String[] {((Assignment) gbItemIter.next()).getName()}));
			}
		}
		catch (MultipleAssignmentSavingException e) {
			FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
			resultString = "failure";
		}
	}
	else {
		// There are errors so need to put an error message at top
		FacesUtil.addErrorMessage(FacesUtil.getLocalizedString("validation_messages_present"));
	}
	
	return resultString;
}
	
	public String updateAssignment() {
		try {
			Category category = retrieveSelectedCategory();
			assignment.setCategory(category);
			
			Assignment originalAssignment = getGradebookManager().getAssignment(assignmentId);
			Double origPointsPossible = originalAssignment.getPointsPossible();
			Double newPointsPossible = assignment.getPointsPossible();
			boolean scoresEnteredForAssignment = getGradebookManager().isEnteredAssignmentScores(assignmentId);
			
			//boolean origUngraded = originalAssignment.getUngraded();
			//boolean newUngraded = assignment.getUngraded();
			
			boolean adjustmentWithNoPoints = false; // used for some logic later
			
			// if ungraded, we don't care about points possible
			if (assignment.getSelectedGradeEntryValue().equals(GB_NON_CALCULATING_ENTRY)) {
				assignment.setUngraded(true);
				assignment.setIsExtraCredit(false); // extra insurance
			}
			else if (assignment.getSelectedGradeEntryValue().equals(GB_ADJUSTMENT_ENTRY)) {
				assignment.setIsExtraCredit(true);
				assignment.setUngraded(false); // extra insurance
				if (getGradebook().getGrade_type()==GradebookService.GRADE_TYPE_PERCENTAGE)
				{
					// Adjustment item in a percent gradebook should never have points possible
					assignment.setPointsPossible(null);
				}
				if (assignment.getPointsPossible() == null || ("".equals(assignment.getPointsPossible()))) {
					adjustmentWithNoPoints = true;
				}
			}
			else
			{
				// back to a normal type of item so set these types to false
				assignment.setUngraded(false);
				assignment.setIsExtraCredit(false);
			}
			if (!assignment.getUngraded() && getGradebook().getGrade_type()!=GradebookService.GRADE_TYPE_LETTER)
			{
				if (!adjustmentWithNoPoints)
				{
					if (assignment.getPointsPossible() == null) {
						FacesUtil.addErrorMessage(getLocalizedString("add_assignment_no_points"));
						return "failure";
					}
					/* If grade entry by percentage or letter and the points possible has changed for this assignment,
					 * we need to convert all of the stored point values to retain the same value
					 */
					if ((getGradeEntryByPercent() || getGradeEntryByLetter()) && scoresEnteredForAssignment) {
						if (!newPointsPossible.equals(origPointsPossible)) {
							List enrollments = getSectionAwareness().getSiteMembersInRole(getGradebookUid(), Role.STUDENT);
					        List studentUids = new ArrayList();
					        for(Iterator iter = enrollments.iterator(); iter.hasNext();) {
					            studentUids.add(((EnrollmentRecord)iter.next()).getUser().getUserUid());
					        }
					        // commented this out... not sure what we want to do here
							//getGradebookManager().convertGradePointsForUpdatedTotalPoints(getGradebook(), originalAssignment, assignment.getPointsPossible(), studentUids);
						}
					}
				}
			}
			else
			{
				// extra insurance to make sure these fields are blank
				assignment.setCounted(false);
				assignment.setPointsPossible(null);
			}
			
			getGradebookManager().updateAssignment(assignment);
			
			if (getGradebook().getGrade_type()!=GradebookService.GRADE_TYPE_LETTER)
			{
				if (origPointsPossible == null)
					origPointsPossible = new Double(0); // prevents a null pointer from a gradebook switch from a letter to another type
				if ((!origPointsPossible.equals(newPointsPossible)) && scoresEnteredForAssignment) {
					if (getGradeEntryByPercent())
						FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_percentage", new String[] {assignment.getName()}));
					else if (getGradeEntryByLetter())
						FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_converted", new String[] {assignment.getName()}));
					else
						FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save_scored", new String[] {assignment.getName()}));
	
				} else {
					FacesUtil.addRedirectSafeMessage(getLocalizedString("edit_assignment_save", new String[] {assignment.getName()}));
				}
			}

		} catch (ConflictingAssignmentNameException e) {
			logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_name_conflict_failure"));
            return "failure";
		} catch (StaleObjectModificationException e) {
            logger.error(e);
            FacesUtil.addErrorMessage(getLocalizedString("edit_assignment_locking_failure"));
            return "failure";
		}
		
		return navigateBack();
	}
	
	/**
	 * Go to assignment details page. InstructorViewBean contains duplicate
	 * of this method, cannot migrate up to GradebookDependentBean since
	 * needs assignmentId, which is defined here.
	 */
	public String navigateBack() {
		String breadcrumbPage = getBreadcrumbPage();
		final Boolean middle = new Boolean((String) SessionManager.getCurrentToolSession().getAttribute("middle"));
		
		if (breadcrumbPage == null || middle) {
			AssignmentDetailsBean assignmentDetailsBean = (AssignmentDetailsBean)FacesUtil.resolveVariable("assignmentDetailsBean");
			assignmentDetailsBean.setAssignmentId(assignmentId);
			assignmentDetailsBean.setBreadcrumbPage(breadcrumbPage);
			
			breadcrumbPage = "assignmentDetails";
		}

		// wherever we go, we're not editing, and middle section
		// does not need to be shown.
		setNav(null, "false", null, "false", null);
		
		return breadcrumbPage;
	}

	/**
	 * View maintenance methods.
	 */
	public Long getAssignmentId() {
		if (logger.isDebugEnabled()) logger.debug("getAssignmentId " + assignmentId);
		return assignmentId;
	}
	public void setAssignmentId(Long assignmentId) {
		if (logger.isDebugEnabled()) logger.debug("setAssignmentId " + assignmentId);
		if (assignmentId != null) {
			this.assignmentId = assignmentId;
		}
	}

    public Assignment getAssignment() {
        if (logger.isDebugEnabled()) logger.debug("getAssignment " + assignment);
		if (assignment == null) {
			if (assignmentId != null) {
				assignment = getGradebookManager().getAssignment(assignmentId);
			}
			if (assignment == null) {
				// it is a new assignment
				assignment = new Assignment();
				assignment.setReleased(true);
			}
		}

        return assignment;
    }
    
    public List getCategoriesSelectList() {
    	return categoriesSelectList;
    }
    
    public List getCategoriesAdjustmentSelectList() {
		return categoriesAdjustmentSelectList;
	}
    
    public List getAddItemSelectList() {
		return addItemSelectList;
	}
	
	public String getAssignmentCategory() {
    	return assignmentCategory;
    }
    
    public void setAssignmentCategory(String assignmentCategory) {
    	this.assignmentCategory = assignmentCategory;
    }
	
    /**
     * getNewBulkItems
     * 
     * Generates and returns a List of blank Assignment objects.
     * Used to support bulk gradebook item creation.
     */
	public List getNewBulkItems() {
		if (newBulkItems == null) {
			newBulkItems = new ArrayList();
		}

		for (int i = newBulkItems.size(); i < NUM_EXTRA_ASSIGNMENT_ENTRIES; i++) {
			newBulkItems.add(getNewAssignment());
		}
		
		return newBulkItems;
	}

	public void setNewBulkItems(List newBulkItems) {
		this.newBulkItems = newBulkItems;
	}

	public int getNumTotalItems() {
		return numTotalItems;
	}

	public void setNumTotalItems(int numTotalItems) {
		this.numTotalItems = numTotalItems;
	}

	public Gradebook getLocalGradebook()
	{
		return getGradebook();
	}
    
    /**
     * Returns the Category associated with assignmentCategory
     * If unassigned or not found, returns null
     * 
     * added parameterized version to support bulk gradebook item creation
     */
    private Category retrieveSelectedCategory() 
    {
    	return retrieveSelectedCategory(assignmentCategory);
    }
       
    private Category retrieveSelectedCategory(String assignmentCategory) 
    {
    	Long catId = null;
    	Category category = null;
    	
		if (assignmentCategory != null && !assignmentCategory.equals(UNASSIGNED_CATEGORY)) {
			try {
				catId = new Long(assignmentCategory);
			}
			catch (Exception e) {
				catId = null;
			}
			
			if (catId != null)
			{
				// check to make sure there is a corresponding category
				category = getGradebookManager().getCategory(catId);
			}
		}
		
		return category;
    }    

    public boolean isSelectedCategoryDropsScores() {
        return selectedCategoryDropsScores;
    }

    public void setSelectedCategoryDropsScores(boolean selectedCategoryDropsScores) {
        this.selectedCategoryDropsScores = selectedCategoryDropsScores;
    }

    public Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    /**
     * For bulk assignments, need to set the proper classes as a string
     */
	public String getRowClasses()
	{
		StringBuffer rowClasses = new StringBuffer();
		
		if (newBulkItems == null) {
			newBulkItems = getNewBulkItems();
		}

		//if shown in UI, set class to 'bogus show' otherwise 'bogus hide'
		for (int i=0; i< newBulkItems.size(); i++){
			Object obj = newBulkItems.get(i);
			if(obj instanceof BulkAssignmentDecoratedBean){
				BulkAssignmentDecoratedBean assignment = (BulkAssignmentDecoratedBean) newBulkItems.get(i);
			
				if (i != 0) rowClasses.append(",");

				if (assignment.getBlnSaveThisItem() || i == 0) {
					rowClasses.append("show bogus");
				}
				else {
					rowClasses.append("hide bogus");					
				}
			}		
		}
		
		return rowClasses.toString();
	}
	
	/**
     * For bulk assignments, need to set the proper classes as a string
     */
	public String getBulkRowClasses()
	{
		StringBuffer bulkRowClasses = new StringBuffer();
		
		if (newBulkGradebookItems == null) {
			newBulkGradebookItems = getNewBulkGradebookItems();
		}

		//if shown in UI, set class to 'bogus show' otherwise 'bogus hide'
		for (int i=0; i< newBulkGradebookItems.size(); i++){
			Object obj = newBulkGradebookItems.get(i);
			if(obj instanceof BulkAssignmentDecoratedBean){
				BulkAssignmentDecoratedBean assignment = (BulkAssignmentDecoratedBean) newBulkGradebookItems.get(i);
			
				if (i != 0) bulkRowClasses.append(",");

				if (assignment.getBlnSaveThisItem() || i == 0) {
					bulkRowClasses.append("show bogus");
				}
				else {
					bulkRowClasses.append("hide bogus");					
				}
			}		
		}
		
		return bulkRowClasses.toString();
	}
	
	public boolean getIsBulkDisplay() {
		isBulkDisplay = selectGradebookItem.equals(GB_ITEM_BULK);
		return isBulkDisplay;
	}

	public void setBulkDisplay(boolean isBulkDisplay) {
		this.isBulkDisplay = isBulkDisplay;
	}
	
	public boolean getIsNonCalc() {
		return isNonCalc;
	}

	public void setIsNonCalc(boolean isNonCalc) {
		this.isNonCalc = isNonCalc;
	}
	
	public boolean getReleaseChange() {
		return releaseChange;
	}

	public void setReleaseChange(boolean releaseChange) {
		this.releaseChange = releaseChange;
	}
	
	public boolean getCountedChange() {
		return countedChange;
	}

	public void setCountedChange(boolean countedChange) {
		this.countedChange = countedChange;
	}
	
	public String addConfirm() {
		
		setNav("overview", "false", "true", "false", "addBulkGradebookItem");
		
		return "addBulkGradebookItem";
	}
	
	public String processGradeEntryChange(ValueChangeEvent vce)
	{ 
		String changeGradeEntry = (String) vce.getNewValue();
		if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))	
		{
			gradeEntryType = changeGradeEntry;
		} 
		
		/*if(vce.getNewValue().equals(GB_NON_CALCULATING_ENTRY) && !vce.getOldValue().equals(vce.getNewValue()))
		{
			setIsNonCalc(true);
		} else {
			setIsNonCalc(false);
		}*/
		if(vce.getNewValue().equals(GB_NON_CALCULATING_ENTRY))
		{
			setIsNonCalc(true);
		} else {
			setIsNonCalc(false);
		}
		return GB_ADD_ASSIGNMENT_PAGE;
	}
    
    public String processCategoryChange(ValueChangeEvent vce)
    { 
        String changeCategory = (String) vce.getNewValue(); 
        if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))  
        {
        	categoryEntry = changeCategory;
            List<Category> categories = getGradebookManager().getCategories(getGradebookId());
            if (categories != null && categories.size() > 0)
            {
                for (Category category : categories) {
                    if(changeCategory.equals(category.getId().toString())) {
                        selectedCategoryDropsScores = category.isDropScores();
                        selectedCategory = category;
                        break;
                    }
                }
            }
        }
        return GB_ADD_ASSIGNMENT_PAGE;
    }
    
    public String processItemTitleChange(ValueChangeEvent vce)
    { 
        String changeItemTitle = (String) vce.getNewValue(); 
        if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))  
        {
            itemTitleChange = changeItemTitle;
        }
        return GB_ADD_ASSIGNMENT_PAGE;
    }
	
	public String processPointsPossibleChange(ValueChangeEvent vce)
	{ 
		String changePointsPossible = (String) vce.getNewValue(); 
		if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))	
		{
			pointsPossibleChange = changePointsPossible;
		}
		return GB_ADD_ASSIGNMENT_PAGE;
	}
	
	public String processDueDateChange(ValueChangeEvent vce)
	{ 
		Date changeDueDate = (Date) vce.getNewValue(); 
		dueDateChange = changeDueDate;
		return GB_ADD_ASSIGNMENT_PAGE;
	}
	
	public String processReleaseChange(ValueChangeEvent vce)
	{ 
		boolean changeRelease = (Boolean) vce.getNewValue(); 
		if(vce.getOldValue().equals(true) && vce.getNewValue().equals(false) && !vce.getOldValue().equals(vce.getNewValue()))
		{
			setReleaseChange(false);
			setCountedChange(false);
		} else {
			setReleaseChange(true);
			setCountedChange(true);
		}
		return GB_ADD_ASSIGNMENT_PAGE;
	}
	
	public String processCountedChange(ValueChangeEvent vce)
	{ 
		boolean changeCounted = (Boolean) vce.getNewValue(); 
		if(vce.getOldValue().equals(true) && vce.getNewValue().equals(false) && !vce.getOldValue().equals(vce.getNewValue()))
		{
			setCountedChange(false);
		} else {
			setCountedChange(true);
		}
		return GB_ADD_ASSIGNMENT_PAGE;
	}
	
	public String processNumBulkItemValue(ValueChangeEvent vce)
	{ 
		String changeNumBulkItem = (String) vce.getNewValue(); 
		if(vce.getOldValue() != null && vce.getNewValue() != null && !vce.getOldValue().equals(vce.getNewValue()))	
		{
			selectBulkGradebookItem = changeNumBulkItem;
		}
		return GB_ADD_ASSIGNMENT_PAGE;
	}
	
	public List getGradeEntrySelectList() 
	{
    	return gradeEntrySelectList;
    }
	
	public List getAddBulkItemSelectList() 
	{
		return addBulkItemSelectList;
	}
	
	/**
     * getNewBulkGradebookItems
     * 
     * Generates and returns a List of blank Assignment objects.
     * Used to support bulk grade book item creation.
     */
	public List getNewBulkGradebookItems() {
		if (newBulkGradebookItems == null) {
			newBulkGradebookItems = new ArrayList();
		}
		
		int numToAdd = Integer.parseInt(selectBulkGradebookItem);
		
		for (int i = newBulkGradebookItems.size(); i < numToAdd; i++) {
			BulkAssignmentDecoratedBean a = getNewAssignment();
			if (itemTitleChange != null)
			{
				a.getAssignment().setName(itemTitleChange + i);
			}
			if (!isNonCalc)
			{
				if (pointsPossibleChange != null)
				{
				a.setPointsPossible(pointsPossibleChange);
				a.getAssignment().setPointsPossible(new Double(pointsPossibleChange));
				}
			}
			a.getAssignment().setDueDate(dueDateChange);
			a.getAssignment().setReleased(releaseChange);
			a.getAssignment().setCounted(countedChange);
			newBulkGradebookItems.add(a);
		}
		
		return newBulkGradebookItems;
	}

	public void setNewBulkGradebookItems(List newBulkGradebookItems) {
		this.newBulkGradebookItems = newBulkGradebookItems;
	}
	
	public String getitemTitleChange() {
		return itemTitleChange;
	}

	public void setItemTitleChange(String itemTitleChange) {
		this.itemTitleChange = itemTitleChange;
	}
	
	public String getPointsPossibleChange() {
		return pointsPossibleChange;
	}

	public void setPointsPossibleChange(String pointsPossibleChange) {
		this.pointsPossibleChange = pointsPossibleChange;
	}
	
	public Date getDueDateChange() {
		return dueDateChange;
	}

	public void setDueDateChange(Date dueDateChange) {
		this.dueDateChange = dueDateChange;
	}
	
	public String getSelectBulkGradebookItem()
	{
		return selectBulkGradebookItem;
	}

	public void setSelectBulkGradebookItem(String selectBulkGradebookItem)
	{
		this.selectBulkGradebookItem = selectBulkGradebookItem;
	}
	
	public String getSelectGradebookItem()
	{
		return selectGradebookItem;
	}

	public void setSelectGradebookItem(String selectGradebookItem)
	{
		this.selectGradebookItem = selectGradebookItem;
	}
	
	public String getGradeEntryType() {
		return gradeEntryType;
	}

	public void setGradeEntryType(String gradeEntryType) {
		this.gradeEntryType = gradeEntryType;
	}
	public String getCategoryEntry() {
		return categoryEntry;
	}

	public void setCategoryEntry(String categoryEntry) {
		this.categoryEntry = categoryEntry;
	}
}


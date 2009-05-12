package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import java.util.Map;

import org.sakaiproject.service.gradebook.shared.ConflictingAssignmentNameException;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;

public class GradebookItemBean {
	
	private static final String CANCEL = "cancel";
	private static final String SUBMIT = "submit";
	private static final String FAILURE = "failure";
	
	public static final Long CATEGORY_UNASSIGNED = -1L;
	
    
   
	/**
	 * this gradebook item will be graded normally
	 * (ie points or %)
	 */
	public static String GB_ITEM_TYPE_NORMAL = "normal";
    /**
     * this item will be graded as non-calculating
     */
	public static String GB_ITEM_TYPE_NON_CAL = "non-cal";
    /**
     * this item will be graded as an adjustment item
     */
	public static String GB_ITEM_TYPE_ADJ = "adj";
	
	public Boolean requireDueDate = false;
	
	private TargettedMessageList messages;
    public void setMessages(TargettedMessageList messages) {
    	this.messages = messages;
    }
		
    private Map<String, Assignment> OTPMap;
    private EntityBeanLocator assignmentEntityBeanLocator;
	@SuppressWarnings("unchecked")
	public void setAssignmentEntityBeanLocator(EntityBeanLocator entityBeanLocator) {
		this.OTPMap = entityBeanLocator.getDeliveredBeans();
		this.assignmentEntityBeanLocator = entityBeanLocator;
	}
	
	private GradebookManager gradebookManager;
    public void setGradebookManager(GradebookManager gradebookManager) {
    	this.gradebookManager = gradebookManager;
    }
    
    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
	
	private Long categoryId;
	/**
	 * we have to render two category select menus because, if the user sets
     * the gradebook item to be an adjustment item, it cannot be associated with
     * an adjustment category. this variable represents the selected category id
     * in the category menu that includes adjustment categories
	 * @param categoryId
	 */
	public void setCategoryId(Long categoryId){
		this.categoryId = categoryId;
	}
	
	private Long nonAdjCategoryId;
	/**
	 * we have to render two category select menus because, if the user sets
	 * the gradebook item to be an adjustment item, it cannot be associated with
	 * an adjustment category. this variable represents the selected category id
	 * in the category menu that excludes adjustment categories
	 * 
	 * @param nonAdjCategoryId
	 */
	public void setNonAdjCategoryId(Long nonAdjCategoryId) {
	    this.nonAdjCategoryId = nonAdjCategoryId;
	}
	
	private Long gradebookId;
	public void setGradebookId(Long gradebookId){
		this.gradebookId = gradebookId;
	}
	private String gbItemType;
	public void setGbItemType(String gbItemType) {
	    this.gbItemType = gbItemType;
	}
	
	// I had to set these as Strings instead of Doubles because the UI
	// kept trying to validate them and threw NumberFormatExceptions
	// when they were empty when the user hit "cancel" - not sure why

	/**
	 * This value will represent the points possible value in a "normal"
	 * grade entry scenario: ie the gradebook item will be graded
	 * by points or % and is not an adjustment item or a non-calc item
	 */
	private String normalPointsPossible;
	public void setNormalPointsPossible(String normalPointsPossible) {
	    this.normalPointsPossible = normalPointsPossible;
	}
	
	/**
	 * This value will represent the points possible value for an
	 * adjustment gb item. We can't bind to the single points possible
	 * value since there will be two input fields. depending on the
	 * grade entry type selected, only one is displayed to the user
	 */
	private String adjPointsPossible;
	public void setAdjPointsPossible(String adjPointsPossible) {
	    this.adjPointsPossible = adjPointsPossible;
	}
	
	public String processActionAddItem(){
		boolean errorFound = false;
		
		Gradebook gradebook = gradebookManager.getGradebook(this.gradebookId);
		
		if (!gradebookService.currentUserHasEditPerm(gradebook.getUid())) {
		    return AuthorizationFailedProducer.VIEW_ID;
		}
		
		for (String key : OTPMap.keySet()) {
			Assignment assignment = OTPMap.get(key);
			
			// if no item type was selected, default to non-cal. this indicates
			// that we didn't display the option at all
			if (gbItemType == null) {
			    gbItemType = GB_ITEM_TYPE_NON_CAL;
			}
			
			// set the grade entry selection
			if (gbItemType.equals(GB_ITEM_TYPE_NON_CAL)) {
			    assignment.setUngraded(true);
			    assignment.setIsExtraCredit(false);
			} else if (gbItemType.equals(GB_ITEM_TYPE_ADJ)) {
			    assignment.setUngraded(false);
			    assignment.setIsExtraCredit(true);
			} else {
			    assignment.setUngraded(false);
			    assignment.setIsExtraCredit(false);
			}
			
			//check for null name
			if (assignment.getName() == null || assignment.getName().equals("")) {
				messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.null_name"));
				errorFound = true;
			}
			
			if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_LETTER) {
				assignment.setUngraded(true);
			}
			
			if (assignment.getUngraded()) {
				// ungraded items have null points possible and are not counted
				assignment.setPointsPossible(null);
				assignment.setCounted(false);
			} else {
			   
			    String pointsAsString;
			    Double pointsPossible;

			    // let's see which points possible we should point to
			    if (assignment.getIsExtraCredit()) {
			        pointsAsString = adjPointsPossible;
			    } else  {
			        pointsAsString = normalPointsPossible;
			    }

			    if (pointsAsString == null) {
			        pointsPossible = null;
			    } else {
			        try {
			            pointsPossible = Double.parseDouble(pointsAsString);
			        } catch (NumberFormatException nfe) {
			            pointsPossible = null; // this will get caught later on b/c invalid
			        }
			    }

			    assignment.setPointsPossible(pointsPossible);

				//check for null points
				if (assignment.getPointsPossible() == null ||
						assignment.getPointsPossible().doubleValue() <= 0) {
					if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
						messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_rel_weight"));
					} else {
						messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_points"));
					}
					errorFound = true;
				}

				// check for more than 2 decimal places
				if (assignment.getPointsPossible() != null) {
					String pointsToSplit = assignment.getPointsPossible().toString();
					String[] decimalSplit = pointsAsString.split("\\.");
					if (decimalSplit.length == 2) {
						String decimal = decimalSplit[1];
						if (decimal != null && decimal.length() > 2) {
							messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.invalid_decimal"));
							errorFound = true;
						}
					}
				}
			}
			
			if (this.requireDueDate == null || this.requireDueDate == Boolean.FALSE) {
				assignment.setDueDate(null);				
			}
			
			if (assignment.isCounted() && !assignment.isReleased()) {
			    messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.counted_not_released"));
                errorFound = true;
			}
				
			if (errorFound) {
				return FAILURE;
			}
			
			Long selectedCategoryId = null;
			if (gradebook.getCategory_type() != GradebookService.CATEGORY_TYPE_NO_CATEGORY) {         
	            // if the item is "adjustment" we need to use the category id
	            // sent back as nonAdjCategoryId. otherwise, we use the categoryId          	            
	            if (gbItemType.equals(GB_ITEM_TYPE_ADJ)) {
	                selectedCategoryId = this.nonAdjCategoryId;
	            } else {
	                selectedCategoryId = this.categoryId;
	            }
	            
	            // set to null if category is unassigned
	            if (selectedCategoryId.equals(CATEGORY_UNASSIGNED)) {
	                selectedCategoryId = null;
	            }
	            
	            // double check that the points possible is set to the category setting if category has drop high/low
	            if (selectedCategoryId != null) {
	                Category category = gradebookManager.getCategory(selectedCategoryId);
	                if (category != null && category.isDropScores()) {
	                    assignment.setPointsPossible(category.getItemValue());
	                }
	            }
			}
			
			if (key.equals(EntityBeanLocator.NEW_PREFIX + "1")){
				//We have a new assignment object
				Long id = null;
				try {
					if (selectedCategoryId != null){
						if (assignment.getUngraded()) {
							id = gradebookManager.createUngradedAssignmentForCategory(this.gradebookId, selectedCategoryId, assignment.getName(), 
									assignment.getDueDate(), assignment.isNotCounted(), assignment.isReleased());
						} else {
							id = gradebookManager.createAssignmentForCategory(this.gradebookId, selectedCategoryId, assignment.getName(), 
									assignment.getPointsPossible(), assignment.getDueDate(), assignment.isNotCounted(), assignment.isReleased(), null);
						}
					} else {
						if (assignment.getUngraded()) {
							id = gradebookManager.createUngradedAssignment(this.gradebookId, assignment.getName(), 
									assignment.getDueDate(), assignment.isNotCounted(), assignment.isReleased());
						} else {
							id = gradebookManager.createAssignment(this.gradebookId, assignment.getName(), assignment.getPointsPossible(), 
								assignment.getDueDate(), assignment.isNotCounted(), assignment.isReleased(), assignment.getIsExtraCredit());
						}
					}
					assignment.setId(id);
					//new UIELBinding("Assignment." + key + ".id", id);
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.successful",
							new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_INFO));
				} catch (ConflictingAssignmentNameException e){
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.conflicting_name",
							new Object[] {assignment.getName() }, "Assignment." + key + ".name"));
					errorFound = true;
				}
			} else {
				//we are editing an existing object
				try {
				    if (selectedCategoryId != null) {
				        // we need to retrieve the category and add it to the
				        // assignment
				        Category cat = gradebookManager.getCategory(selectedCategoryId);
				        if (cat != null) {
				            assignment.setCategory(cat);
				        }
				    } else {
				        // this assignment does not have a category
				        assignment.setCategory(null);
				    }
				    
					gradebookManager.updateAssignment(assignment);
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.successful",
							new Object[] {assignment.getName() }, TargettedMessage.SEVERITY_INFO));
				} catch (ConflictingAssignmentNameException e){
					messages.addMessage(new TargettedMessage("gradebook.add-gradebook-item.conflicting_name",
							new Object[] {assignment.getName() }, "Assignment." + key + ".name"));
					errorFound = true;
				}
			}
		}
		if(errorFound) {
			return FAILURE;
		}
		
		return SUBMIT;
	}
	
	public String processActionCancel(){
		
		return CANCEL;
	}
	
	/**
	 * this is the fetchMethod for the EntityBeanLocator
	 * @param assignmentId
	 * @return
	 */
	public Assignment getAssignmentById(Long assignmentId){
		return gradebookManager.getAssignment(assignmentId);
	}
}
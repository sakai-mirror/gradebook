package org.sakaiproject.tool.gradebook.ui.helpers.producers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Category;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.beans.GradebookItemBean;
import org.sakaiproject.tool.gradebook.ui.helpers.params.GradebookItemViewParams;

import uk.org.ponder.beanutil.entity.EntityBeanLocator;
import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.rsf.components.UIBoundBoolean;
import uk.org.ponder.rsf.components.UICommand;
import uk.org.ponder.rsf.components.UIContainer;
import uk.org.ponder.rsf.components.UIELBinding;
import uk.org.ponder.rsf.components.UIForm;
import uk.org.ponder.rsf.components.UIInput;
import uk.org.ponder.rsf.components.UIMessage;
import uk.org.ponder.rsf.components.UIOutput;
import uk.org.ponder.rsf.components.UISelect;
import uk.org.ponder.rsf.components.UIVerbatim;
import uk.org.ponder.rsf.components.decorators.DecoratorList;
import uk.org.ponder.rsf.components.decorators.UIFreeAttributeDecorator;
import uk.org.ponder.rsf.evolvers.FormatAwareDateInputEvolver;
import uk.org.ponder.rsf.flow.ARIResult;
import uk.org.ponder.rsf.flow.ActionResultInterceptor;
import uk.org.ponder.rsf.util.RSFUtil;
import uk.org.ponder.rsf.view.ComponentChecker;
import uk.org.ponder.rsf.view.DefaultView;
import uk.org.ponder.rsf.view.ViewComponentProducer;
import uk.org.ponder.rsf.viewstate.RawViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParameters;
import uk.org.ponder.rsf.viewstate.ViewParamsReporter;

public class GradebookItemProducer implements ActionResultInterceptor,
ViewComponentProducer, ViewParamsReporter, DefaultView {

    public static final String VIEW_ID = "gradebookItem";
    public String getViewID() {
        return VIEW_ID;
    }

    private String reqStar = "<span class=\"reqStar\">*</span>";

    public ViewParameters getViewParameters() {
        return new GradebookItemViewParams();
    }
    
    private EntityBeanLocator assignmentBeanLocator; 
    public void setAssignmentBeanLocator(EntityBeanLocator assignmentBeanLocator) {
        this.assignmentBeanLocator = assignmentBeanLocator;
    }
    
    private MessageLocator messageLocator;
    public void setMessageLocator(MessageLocator messageLocator) {
        this.messageLocator = messageLocator;
    }
    
    private GradebookManager gradebookManager;
    public void setGradebookManager(GradebookManager gradebookManager) {
        this.gradebookManager = gradebookManager;
    }
    
    private GradebookService gradebookService;
    public void setGradebookService(GradebookService gradebookService) {
        this.gradebookService = gradebookService;
    }
    
	/*
	 * You can change the date input to accept time as well by uncommenting the lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_TIME_INPUT);
	 * and commenting out lines like this:
	 * dateevolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
	 * -AZ
	 * And vice versa - RWE
	 */
	private FormatAwareDateInputEvolver dateEvolver;
	public void setDateEvolver(FormatAwareDateInputEvolver dateEvolver) {
		this.dateEvolver = dateEvolver;
	}
    
    public void fillComponents(UIContainer tofill, ViewParameters viewparams, ComponentChecker checker) {
    	GradebookItemViewParams params = (GradebookItemViewParams) viewparams;
    	
    	if (params.contextId == null) {
    	    //TODO do something
    	    return;
    	}
    	
    	if (!gradebookService.currentUserHasEditPerm(params.contextId)) {
    	    UIMessage.make(tofill, "permissions_error", "gradebook.authorizationFailed.permissions_error");
            return;
    	}
    	
    	//Gradebook Info
    	Gradebook gradebook = gradebookManager.getGradebook(params.contextId);
    	Long gradebookId = gradebook.getId();
    	
    	String newItemName = params.name;
    	String newItemDueTime = params.dueDateTime;
    	Date newItemDueDate = null;
    	if (newItemDueTime != null && !"".equals(newItemDueTime.trim())) {
    		try {
    			Long time = Long.parseLong(newItemDueTime);
    			newItemDueDate = new Date(time.longValue());
    		} catch (NumberFormatException nfe) {
    			// something funky was passed here, so we won't try to pre-set the due date
    		}
    	}
    	
    	//OTP
    	String assignmentOTP = "Assignment.";
    	String OTPKey = "";
    	if (params.assignmentId != null) {
    		OTPKey += params.assignmentId.toString();
    	} else {
    		OTPKey += EntityBeanLocator.NEW_PREFIX + "1";
    	}
    	assignmentOTP += OTPKey;
    	
    	Boolean add = (params.assignmentId == null);
    	
        
        Assignment assignment = (Assignment) assignmentBeanLocator.locateBean(OTPKey);
        
        // double check that this assignment is affiliated with this gradebook
        if (!add && !assignment.getGradebook().getUid().equals(params.contextId)) {
            throw new IllegalArgumentException("The given assignment with id " + assignment.getId() + " does not belong to the given contextId:" + params.contextId);
        }
    	
        //set dateEvolver
        dateEvolver.setStyle(FormatAwareDateInputEvolver.DATE_INPUT);
        
        //Display None Decorator list
		Map<String, String> attrmap = new HashMap<String, String>();
		attrmap.put("style", "display:none");
		DecoratorList display_none_list =  new DecoratorList(new UIFreeAttributeDecorator(attrmap));
		
		//Setting up Dates
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.DAY_OF_YEAR, 7);
    	cal.set(Calendar.HOUR_OF_DAY, 17);
    	cal.set(Calendar.MINUTE, 0);
    	Date duedate = cal.getTime();

        
        if (add){
        	UIMessage.make(tofill, "heading", "gradebook.add-gradebook-item.heading_add");
        } else {
        	UIMessage.make(tofill, "heading", "gradebook.add-gradebook-item.heading_edit");
        }
        
        UIVerbatim.make(tofill, "instructions", messageLocator.getMessage("gradebook.add-gradebook-item.instructions",
        		new Object[]{ reqStar }));
        
        //Start Form
        UIForm form = UIForm.make(tofill, "form");
        
        UIVerbatim.make(form, "title_label", messageLocator.getMessage("gradebook.add-gradebook-item.title_label",
        		new Object[]{ reqStar }));
        
        // only display grade entry option if not a letter-grade gradebook
        if (gradebook.getGrade_type() != GradebookService.GRADE_TYPE_LETTER) {
        	UIOutput.make(tofill, "grade_entry_li");
        	
        	String[] entry_labels = new String[3];
            String[] entry_values = new String[3];
            
            // Points or Percent
            if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE) {
                entry_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.grade_entry.percent");
                
            } else {
                entry_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.grade_entry.points");
            }
            entry_values[0] = GradebookItemBean.GB_ITEM_TYPE_NORMAL;

            entry_labels[1] = messageLocator.getMessage("gradebook.add-gradebook-item.grade_entry.non-cal");
            entry_values[1] = GradebookItemBean.GB_ITEM_TYPE_NON_CAL;
            
            entry_labels[2] = messageLocator.getMessage("gradebook.add-gradebook-item.grade_entry.adj");
            entry_values[2] = GradebookItemBean.GB_ITEM_TYPE_ADJ;
            
            String entryValue;
            if (assignment.getUngraded()) {
                entryValue = GradebookItemBean.GB_ITEM_TYPE_NON_CAL;
            } else if (assignment.getIsExtraCredit() != null && assignment.getIsExtraCredit()) {
                entryValue = GradebookItemBean.GB_ITEM_TYPE_ADJ;
            } else {
                entryValue = GradebookItemBean.GB_ITEM_TYPE_NORMAL;
            }
            
            UISelect.make(form, "grade_entry", entry_values, entry_labels, "#{GradebookItemBean.gbItemType}", entryValue);
            form.parameters.add( new UIELBinding("#{GradebookItemBean.gbItemType}", entryValue));
        }
        
        // if this is a new gradebook item, use the name parameter passed via the url
        if (add) {
            // add the name first as a UIELBinding to force it to save this value
            // if the user doesn't update the name field
            form.parameters.add( new UIELBinding(assignmentOTP + ".name", newItemName));
            UIInput.make(form, "title", assignmentOTP + ".name", newItemName);
        } else {
            UIInput.make(form, "title", assignmentOTP + ".name", assignment.getName());
        }
        
        // only display points possible info if not a letter grade gradebook
        if (gradebook.getGrade_type() != GradebookService.GRADE_TYPE_LETTER) {
            String pointsPossibleDisplay = "";
            if (assignment.getPointsPossible() != null) {
                pointsPossibleDisplay = assignment.getPointsPossible().toString();
            }
            // javascript will take care of which of these options is actually displayed
            
            // if this is a regular gb item, display normal points possible
            UIOutput.make(form, "points-possible");
            UIInput.make(form, "point", "#{GradebookItemBean.normalPointsPossible}", pointsPossibleDisplay);
            // otherwise, this is an adjustment item, so display different option
            // but we don't display at all unless it is a points gb
            if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
                UIOutput.make(form, "adjustment-points-possible");
                UIInput.make(form, "adj_point", "#{GradebookItemBean.adjPointsPossible}", pointsPossibleDisplay);
            }
            
            if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_POINTS) {
                UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.point_label",
                        new Object[]{ reqStar }));
            } else if (gradebook.getGrade_type() == GradebookService.GRADE_TYPE_PERCENTAGE){
                UIVerbatim.make(form, "point_label", messageLocator.getMessage("gradebook.add-gradebook-item.percentage_label",
                        new Object[]{ reqStar }));
            }
            
            if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
                UIOutput.make(form, "points_instruction", messageLocator.getMessage("gradebook.add-gradebook-item.adj_cat.instructions"));
                UIOutput.make(form, "adj_points_instruction", messageLocator.getMessage("gradebook.add-gradebook-item.adj_cat.instructions"));
            }
        }           

        if (add) {          
            // if a due date was passed in, set the due date
            if (newItemDueDate != null) {
            	assignment.setDueDate(newItemDueDate);
            }
        }
        
        
        Boolean require_due_date = (assignment.getDueDate() != null);
		UIBoundBoolean.make(form, "require_due_date", "#{GradebookItemBean.requireDueDate}", require_due_date);
		UIMessage.make(form, "require_due_date_label", "gradebook.add-gradebook-item.require_due_date");
		
		UIOutput require_due_container = UIOutput.make(form, "require_due_date_container");
		UIInput dueDateField = UIInput.make(form, "due_date:", assignmentOTP + ".dueDate");
		Date initDueDate = assignment.getDueDate() != null ? assignment.getDueDate() : duedate;
		dateEvolver.evolveDateInput(dueDateField, initDueDate);
		
		// add the due date as a UIELBinding to force it to save this value
        // if the user doesn't update the due date field
        form.parameters.add( new UIELBinding(assignmentOTP + ".dueDate", initDueDate));
        form.parameters.add( new UIELBinding("#{GradebookItemBean.requireDueDate}", require_due_date));
		
		if (!require_due_date){
			require_due_container.decorators = display_none_list;
		}
        
		if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_ONLY_CATEGORY ||
		        gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
		    
		    List<Category> categories = gradebookManager.getCategories(gradebookId);
		    
		    if (categories.size() > 0){

		        UIOutput.make(form, "category_li");

		        String[] category_labels = new String[categories.size() + 1];
		        String[] category_values = new String[categories.size() + 1];
		        category_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.category_unassigned");
		        category_values[0] = GradebookItemBean.CATEGORY_UNASSIGNED.toString();
		        int i=1;
		        for (Category cat : categories){
		            category_labels[i] = cat.getName();
		            category_values[i] = cat.getId().toString();
		            i++;
		        }

		        String categoryId = GradebookItemBean.CATEGORY_UNASSIGNED.toString(); // unassigned by default
		        if (assignment.getCategory() != null) {
		            categoryId = assignment.getCategory().getId().toString();
		        }

		        UISelect.make(form, "category", category_values, category_labels, "#{GradebookItemBean.categoryId}", categoryId);
		        
                // adjustment categories can't be included if the grade entry is set to "adjustment item", so
                // we need to render another drop down for categories that excludes adjustment items
                List<Category> nonAdjCategories = new ArrayList<Category>();
                for (Category cat : categories) {
                    if (cat.getIsExtraCredit() == null || !cat.getIsExtraCredit()) {
                        nonAdjCategories.add(cat);
                    }
                }
                
                if (!nonAdjCategories.isEmpty()) {
                    String[] non_adj_category_labels = new String[nonAdjCategories.size() + 1];
                    String[] non_adj_category_values = new String[nonAdjCategories.size() + 1];
                    non_adj_category_labels[0] = messageLocator.getMessage("gradebook.add-gradebook-item.category_unassigned");
                    non_adj_category_values[0] = GradebookItemBean.CATEGORY_UNASSIGNED.toString();
                    int non_adj_i=1;
                    for (Category cat : nonAdjCategories){
                        non_adj_category_labels[non_adj_i] = cat.getName();
                        non_adj_category_values[non_adj_i] = cat.getId().toString();
                        non_adj_i++;
                    }

                    UISelect.make(form, "nonAdjCategory", non_adj_category_values, non_adj_category_labels, "#{GradebookItemBean.nonAdjCategoryId}", categoryId);
                }

		        if (gradebook.getCategory_type() == GradebookService.CATEGORY_TYPE_WEIGHTED_CATEGORY) {
 
		            // if we have categories with weighting, we need to be conscious of categories that
		            // have drop high/low settings.  this means that the point value is uneditable. let's
		            // put affected categories in a hidden select so we can use javascript for this display
		            List<Category> catWithPointRestrictions = new ArrayList<Category>();
		            for (Category cat : categories) {
		                if (cat.isDropScores()) {
		                    catWithPointRestrictions.add(cat);
		                }
		            }
		            
		            // create the hidden select menu
		            if (!catWithPointRestrictions.isEmpty()) {
                        String[] weighted_category_labels = new String[catWithPointRestrictions.size()];
                        String[] weighted_category_values = new String[catWithPointRestrictions.size()];
                        int index = 0;
                        // the label will be the category id and the value will be the category item value
                        for (Category resCat : catWithPointRestrictions) {
                            weighted_category_labels[index] = resCat.getId().toString();
                            if (resCat.getItemValue() != null) {
                                weighted_category_values[index] = resCat.getItemValue().toString();
                            } else {
                                weighted_category_values[index] = "";
                            }
                            index++;
                        }

		                UISelect.make(tofill, "categories_with_uneditable_points", weighted_category_values, weighted_category_labels, null);
		            }

		            // display informational message related to counting items in a weighted gb
		            UIOutput.make(form, "category_instruction", messageLocator.getMessage("gradebook.add-gradebook-item.category_instruction"));

		        }
		    }
		}
        
        UIBoundBoolean.make(form, "release", assignmentOTP + ".released", assignment.isReleased());
        
        // only display "counted" if not letter grade gradebook
        if (gradebook.getGrade_type() != GradebookService.GRADE_TYPE_LETTER) {
        	UIOutput.make(form, "counted");
        	UIBoundBoolean.make(form, "course_grade", assignmentOTP + ".counted", assignment.isCounted());
        }
        
        form.parameters.add( new UIELBinding("#{GradebookItemBean.gradebookId}", gradebookId));
        
        //RSFUtil.addResultingViewBinding(form, "assignmentId", assignmentOTP + ".id");
        RSFUtil.addResultingViewBinding(form, "name", assignmentOTP + ".name");
        RSFUtil.addResultingViewBinding(form, "requireDueDate", "#{GradebookItemBean.requireDueDate}");
        RSFUtil.addResultingViewBinding(form, "dueDate", assignmentOTP + ".dueDate");
        
        //Action Buttons
        if (add){
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.add_item"), "#{GradebookItemBean.processActionAddItem}");
        } else {
        	UICommand.make(form, "add_item", UIMessage.make("gradebook.add-gradebook-item.edit_item"), "#{GradebookItemBean.processActionAddItem}");
        }
        UICommand.make(form, "cancel", UIMessage.make("gradebook.add-gradebook-item.cancel"), "#{GradebookItemBean.processActionCancel}");
    }

	public void interceptActionResult(ARIResult result,
			ViewParameters incoming, Object actionReturn) {
		if (incoming instanceof GradebookItemViewParams) {
			GradebookItemViewParams params = (GradebookItemViewParams) incoming;
			if (params.finishURL != null && actionReturn.equals("cancel")) {
				result.resultingView = new RawViewParameters(params.finishURL);
				result.propagateBeans = ARIResult.FLOW_END;
			}
			else if (params.finishURL != null && actionReturn.equals("submit")) {
				//tack on name and due date of newly created item
				String name = ((GradebookItemViewParams)result.resultingView).name;
				String gbItemDueTime = "";
				boolean requireDueDate = ((GradebookItemViewParams)result.resultingView).requireDueDate;
				if (requireDueDate) {
				    Date dueDate = ((GradebookItemViewParams)result.resultingView).dueDate;
				    if (dueDate != null) {
				        gbItemDueTime = dueDate.getTime() + "";
				    }
				}
				result.resultingView = new RawViewParameters(params.finishURL + "?gbItemName=" + name + "&gbItemDueTime=" + gbItemDueTime);
			}
		}
	}
}
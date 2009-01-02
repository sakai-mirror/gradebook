package org.sakaiproject.tool.gradebook.ui.helpers.beans;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.service.gradebook.shared.GradebookService;
import org.sakaiproject.service.gradebook.shared.InvalidDecimalGradeException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeException;
import org.sakaiproject.service.gradebook.shared.InvalidGradeLengthException;
import org.sakaiproject.service.gradebook.shared.NegativeGradeException;
import org.sakaiproject.service.gradebook.shared.NonNumericGradeException;
import org.sakaiproject.tool.gradebook.Assignment;
import org.sakaiproject.tool.gradebook.Gradebook;
import org.sakaiproject.tool.gradebook.business.GradebookManager;
import org.sakaiproject.tool.gradebook.ui.helpers.producers.AuthorizationFailedProducer;

import uk.org.ponder.messageutil.MessageLocator;
import uk.org.ponder.messageutil.TargettedMessage;
import uk.org.ponder.messageutil.TargettedMessageList;


public class AssignmentGradeRecordBean {
		private static final Log log = LogFactory.getLog(AssignmentGradeRecordBean.class);
	
		private static final String CANCEL = "cancel";
		private static final String SUBMIT = "submit";
		private static final String FAILURE = "failure";
		
		
		private TargettedMessageList messages;
	    public void setMessages(TargettedMessageList messages) {
	    	this.messages = messages;
	    }

		private MessageLocator messageLocator;
		public void setMessageLocator (MessageLocator messageLocator) {
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

	    private Long gradebookId;
	    public void setGradebookId(Long gradebookId){
	        this.gradebookId = gradebookId;
	    }

	    private Long assignmentId;
	    public void setAssignmentId(Long assignmentId){
	        this.assignmentId = assignmentId;
	    }

	    private String studentId;
	    public void setStudentId(String studentId){
	        this.studentId = studentId;
	    }

	    private String commentText;
	    public void setCommentText(String commentText){
	        this.commentText = commentText;
	    }

	    private String enteredGrade;
	    public void setEnteredGrade(String enteredGrade) {
	        this.enteredGrade = enteredGrade;
	    }

	    public String processActionSubmitGrade(){
	        if (this.assignmentId == null || this.studentId == null || this.gradebookId == null){
	            return FAILURE;
	        }

	        Gradebook gradebook = gradebookManager.getGradebook(this.gradebookId);
	        Assignment assignment = gradebookManager.getAssignment(this.assignmentId);

	        if (!gradebookService.isUserAbleToGradeItemForStudent(gradebook.getUid(), this.assignmentId, this.studentId)) {
	            return AuthorizationFailedProducer.VIEW_ID;
	        }
	        
	        try {
	        	gradebookService.validateGrade(assignment.getId(), enteredGrade);
	        } catch (NonNumericGradeException nnge) {
	        	if (log.isDebugEnabled()) log.debug("Caught non-numeric grade in grading helper: " + enteredGrade);
	        	messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.error.non-numeric",
	        			new Object[] {}, TargettedMessage.SEVERITY_ERROR));
	        	return FAILURE;
	        } catch (InvalidDecimalGradeException idge) {
	        	if (log.isDebugEnabled()) log.debug("Caught grade with invalid decimal places in grading helper: " + enteredGrade);
	        	messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.error.decimal",
	        			new Object[] {}, TargettedMessage.SEVERITY_ERROR));
	        	return FAILURE;
	        } catch (NegativeGradeException nge) {
	        	if (log.isDebugEnabled()) log.debug("Caught negative grade in grading helper: " + enteredGrade);
	        	messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.error.negative",
	        			new Object[] {}, TargettedMessage.SEVERITY_ERROR));
	        	return FAILURE;
	        } catch (InvalidGradeLengthException igle) {
	        	if (log.isDebugEnabled()) log.debug("Caught grade with invalid length in grading helper: " + enteredGrade);
	        	messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.error.length",
	        			new Object[] {GradebookService.MAX_GRADE_LENGTH}, TargettedMessage.SEVERITY_ERROR));
	        	return FAILURE;
	        } catch (InvalidGradeException ige) {
	        	log.warn("Unknown grade entry error identified in the grading helper for grade: " + enteredGrade);
	        	messages.addMessage(new TargettedMessage("gradebook.grade-gradebook-item.error.generic",
	        			new Object[] {}, TargettedMessage.SEVERITY_ERROR));
	        	return FAILURE;
	        }

	        gradebookService.saveGradeAndCommentForStudent(gradebook.getUid(), 
	        		assignment.getId(), studentId, enteredGrade, commentText);

	        return SUBMIT;
		}
		
		public String processActionCancel(){
			
			return CANCEL;
		}

}

<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
	  <t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
		<%@include file="/inc/appMenu.jspf"%>
		
		<%@include file="/inc/breadcrumb.jspf"%>
	  </t:aliasBean>

	  <sakai:flowState bean="#{addAssignmentBean}" />
	  
		<p class="instruction"><h:outputText value="#{msgs.add_assignment_bulk_instruction}" /></p>
		
		<%@include file="/inc/globalMessages.jspf"%>
		
		<p class="instruction">
			<h:outputText value="* " styleClass="reqStarInline"/>
			<h:outputText value="#{msgs.flag_required}"/>
		</p>
		<h4><h:outputText value="#{msgs.add_assignment_bulk_title}"/></h4>
		
		<h:panelGrid cellpadding="0" cellspacing="5" columns="2" 
    		columnClasses="itemSummaryLite itemName, itemSummaryLite shorttext" 
  			styleClass="itemSummaryLite">
			<h:outputLabel for="selectCategory" id="categoryLabel" value="#{msgs.add_assignment_category}" rendered="#{addAssignmentBean.categoriesEnabled}" />
			<h:panelGroup rendered="#{addAssignmentBean.categoriesEnabled}">
				<h:selectOneMenu id="selectCategory" value="#{addAssignmentBean.categoryEntry}" valueChangeListener="#{addAssignmentBean.processCategoryChangeInAddBulkGradebookItem}" onchange="this.form.submit();">
					<f:selectItems value="#{(addAssignmentBean.isAdjustment) ? addAssignmentBean.categoriesAdjustmentSelectList : addAssignmentBean.categoriesSelectList}" />
				</h:selectOneMenu>
				
				<f:verbatim><div class="instruction"></f:verbatim>
					<h:outputText id="nonCalCategoryInstructionText" value="#{msgs.add_assignment_category_info}" rendered="#{addAssignmentBean.weightingEnabled}"/>
				<f:verbatim></div></f:verbatim>			
			</h:panelGroup>
			<h:outputLabel for="selectGradeEntry" id="gradeEntryLabel" value="#{msgs.add_assignment_type}" rendered="#{!bean.assignment.externallyMaintained && addAssignmentBean.localGradebook.grade_type != 3}" />
			<h:panelGroup rendered="#{!bean.assignment.externallyMaintained && addAssignmentBean.localGradebook.grade_type != 3}">
				<h:selectOneMenu id="selectGradeEntry" value="#{addAssignmentBean.gradeEntryType}"
					valueChangeListener="#{addAssignmentBean.processGradeEntryChange}"
					onchange="this.form.submit();">
					<f:selectItems value="#{addAssignmentBean.gradeEntrySelectList}" />
					</h:selectOneMenu>
				</h:panelGroup>
		
		</h:panelGrid>
		
		<t:dataTable cellpadding="0" cellspacing="0"
			id="bulkNewAssignmentsTable"
			value="#{addAssignmentBean.newBulkGradebookItems}"
			var="gbItem"
			columnClasses="left_nowrap,left_nowrap,left_nowrap,center,center"
			styleClass="listHier lines nolines"
			rowClasses="#{addAssignmentBean.bulkRowClasses}"
			rowIndexVar="rowIndex">
			
			<h:column id="_title">
				<f:facet name="header">
					<%--<t:commandSortHeader columnName="name" propertyName="name" immediate="true" arrow="true">--%>
		    		<h:outputText value="#{msgs.add_assignment_bulk_header_title}" />
		    		<%--</t:commandSortHeader>--%>
		    	</f:facet>
		    	<h:outputText value="* " styleClass="reqStarInline"/>
		    	<h:inputText id="title" value="#{gbItem.assignment.name}" />    	
		    	<h:outputText id="noTitleErrMsg"  value="#{msgs.add_assignment_bulk_no_title}" 
					styleClass="alertMessageInline" rendered="#{gbItem.bulkNoTitleError == 'blank'}" />
				<h:outputText id="dupTitleErrMsg" value="#{msgs.add_assignment_bulk_duplicate_name}"
					styleClass="alertMessageInline" rendered="#{gbItem.bulkNoTitleError == 'dup'}" />
			</h:column>
			
			<h:column rendered="#{!addAssignmentBean.isNonCalc && addAssignmentBean.localGradebook.grade_type != 3 && !(addAssignmentBean.isAdjustment && addAssignmentBean.localGradebook.grade_type == 2)}">
				<f:facet name="header">
					<h:outputLabel for="points" id="pointsLabel" value="#{(addAssignmentBean.localGradebook.grade_type == 1) ? ((addAssignmentBean.isAdjustment) ? msgs.add_assignment_header_adjustment : msgs.add_assignment_bulk_header_points) : msgs.add_assignment_bulk_header_relative_weight}"/>
				</f:facet>
				<h:outputText value="* " styleClass="reqStarInline"  rendered="#{!gbItem.assignment.category.dropScores}"/>
				<h:inputText id="points" size="5" value="#{gbItem.assignment.pointsPossible}" rendered="#{!gbItem.assignment.category.dropScores || (gbItem.assignment.category.dropScores && addAssignmentBean.isAdjustment)}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.NONTRAILING_DOUBLE" />
					<f:validateDoubleRange minimum="0.01" />
					<f:validator validatorId="org.sakaiproject.gradebook.jsf.validator.ASSIGNMENT_GRADE_DOUBLE"/>
				</h:inputText>
				<h:outputText id="pointsDropScores" value="#{gbItem.assignment.pointsPossible}"
						rendered="#{gbItem.assignment.category.dropScores && !addAssignmentBean.isAdjustment}" />
				<h:outputText id="blankPtsErrMsg"  value="#{msgs.add_assignment_bulk_no_points}" styleClass="alertMessageInline"
					rendered="#{gbItem.bulkNoPointsError == 'blank'" />
				<h:message for="points" styleClass="alertMessageInline"/>
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_header_due_date}"/>
				</f:facet>
				<t:inputCalendar id="dueDate" size="8" value="#{gbItem.assignment.dueDate}" renderAsPopup="true" renderPopupButtonAsImage="true"
					popupTodayString="#{msgs.date_entry_today_is}" popupWeekString="#{msgs.date_entry_week_header}" >
				</t:inputCalendar>
				<h:message for="dueDate" styleClass="alertMessageInline" />
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_release}" escape="false"/>
				</f:facet>
        		<h:selectBooleanCheckbox id="released" value="#{gbItem.assignment.released}"
        			onclick="assignmentReleased((this.form.name + ':bulkNewAssignmentsTable:' + #{rowIndex}), true);"
        			onkeypress="return submitOnEnter(event, 'gbForm:saveButton');" />
			</h:column>
			
			<h:column rendered="#{addAssignmentBean.localGradebook.grade_type != 3 && !addAssignmentBean.isNonCalc}">
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_include_in_cum}" escape="false"/>
				</f:facet>
				<h:selectBooleanCheckbox id="countAssignment" value="#{gbItem.assignment.counted}"
					rendered="#{addAssignmentBean.localGradebook.grade_type != 3}" />			
			</h:column>
		
		</t:dataTable>
		
		<div class="act calendarPadding">
		<h:commandButton
			id="saveButton"
			styleClass="active"
			value="#{msgs.add_assignment_save_button}"
			action="#{addAssignmentBean.saveNewBulkAssignment}"/>
		<h:commandButton
			value="#{msgs.add_assignment_cancel}"
			action="overview" 
			immediate="true"/>
		</div>
		
	  </h:form>
	</div>
	
	
	
</f:view>

<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
	  <t:aliasBean alias="#{bean}" value="#{addAssignmentBean}">
		<%@include file="/inc/appMenu.jspf"%>
		
		<%@include file="/inc/breadcrumb.jspf"%>
	  </t:aliasBean>

	  <sakai:flowState bean="#{addAssignmentBean}" />
	  
		<%--<h2><h:outputText value="#{msgs.appmenu_overview}"/></h2>--%>
		
		<p class="instruction"><h:outputText value="#{msgs.add_assignment_bulk_instruction}" /></p>
		
		<%@include file="/inc/globalMessages.jspf"%>
		
		<h4><h:outputText value="#{msgs.add_assignment_bulk_title}"/></h4>
		
		<h:panelGrid cellpadding="0" cellspacing="5" columns="2" 
    		columnClasses="itemSummaryLite itemName, itemSummaryLite shorttext" 
  			styleClass="itemSummaryLite">
			<h:outputLabel value="#{msgs.add_assignment_type}" rendered="#{!bean.assignment.externallyMaintained && addAssignmentBean.localGradebook.grade_type != 3}" />
			<h:panelGroup rendered="#{!bean.assignment.externallyMaintained && addAssignmentBean.localGradebook.grade_type != 3}">
				<h:selectOneMenu id="selectGradeEntry" value="#{addAssignmentBean.assignment.selectedGradeEntryValue}"
					valueChangeListener="#{addAssignmentBean.processGradeEntryChange}" onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
					<f:selectItems value="#{addAssignmentBean.gradeEntrySelectList}" />
					</h:selectOneMenu>
				</h:panelGroup>
		
			<h:outputLabel for="selectCategory" id="categoryLabel" value="#{msgs.add_assignment_category}" rendered="#{addAssignmentBean.categoriesEnabled}" />
			<h:panelGroup rendered="#{addAssignmentBean.categoriesEnabled}">
				<h:selectOneMenu id="selectCategory" value="#{addAssignmentBean.assignmentCategory}"
					onclick="assignmentNonCalc((this.form.name));"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
					<f:selectItems value="#{addAssignmentBean.categoriesSelectList}" />
				</h:selectOneMenu>
				<f:verbatim><div class="instruction"></f:verbatim>
					<h:outputText id="nonCalCategoryInstructionText" value="#{msgs.add_assignment_category_info}" rendered="#{addAssignmentBean.weightingEnabled}"/>
				<f:verbatim></div></f:verbatim>			
			</h:panelGroup>
		</h:panelGrid>
		
		<t:dataTable cellpadding="0" cellspacing="0"
			id="bulkNewAssignmentsTable"
			value="#{addAssignmentBean.newBulkGradebookItems}"
			var="gbItem"
			columnClasses="left,left,left,center,center"
			styleClass="listHier lines nolines"
			rowClasses="#{addAssignmentBean.bulkRowClasses}"
			rowIndexVar="rowIndex">
			
			<h:column id="_title">
				<f:facet name="header">
					<%--<t:commandSortHeader columnName="name" propertyName="name" immediate="true" arrow="true">--%>
		    		<h:outputText value="#{msgs.add_assignment_title}" />
		    		<%--<h:outputText value="#{msgs.add_assignment_footnote_symbol1}" />
		    		</t:commandSortHeader>--%>
		    	</f:facet>
		    	<h:inputText id="title" value="#{addAssignmentBean.itemTitleChange} #{rowIndex}"/>
		    	<%--
	    		<h:outputText id="noTitleErrMsg"  value="#{msgs.add_assignment_no_title}" 
					styleClass="alertMessageInline" rendered="#{gbItem.bulkNoTitleError == 'blank'}" />
				<h:outputText id="noTitleErrMsgH"  value="#{msgs.add_assignment_no_title}" 
					styleClass="alertMessageInline errHide" rendered="#{gbItem.bulkNoTitleError != 'blank'}" />
				--%>
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:outputText id="pointsLabel" value="#{(addAssignmentBean.localGradebook.grade_type == 1) ? msgs.add_assignment_header_points : msgs.add_assignment_header_relative_weight}"
							rendered="#{!bean.assignment.externallyMaintained && addAssignmentBean.localGradebook.grade_type != 3}"/>
					<%--<h:outputText value="#{msgs.add_assignment_footnote_symbol1}" />--%>
				</f:facet>
				<h:inputText id="points" value="#{addAssignmentBean.pointsPossibleChange}" rendered="#{!addAssignmentBean.isNonCalc && addAssignmentBean.localGradebook.grade_type != 3}"/>
				<%--
				<h:outputText id="blankPtsErrMsg"  value="#{msgs.add_assignment_no_points}" styleClass="alertMessageInline"
					rendered="#{gbItem.bulkNoPointsError == 'blank'}" />
				<h:outputText id="blankPtsErrMsgH"  value="#{msgs.add_assignment_no_points}" styleClass="alertMessageInline errHide"
					rendered="#{gbItem.bulkNoPointsError != 'blank'}" />
				<h:outputText id="nanPtsErrMsg" value="#{msgs.add_assignment_nan_points}" styleClass="alertMessageInline"
					rendered="#{gbItem.bulkNoPointsError == 'NaN'}" />
				<h:outputText id="nanPtsErrMsgH" value="#{msgs.add_assignment_nan_points}" styleClass="alertMessageInline errHide"
					rendered="#{gbItem.bulkNoPointsError != 'NaN'}" />
				<h:outputText id="invalidPtsErrMsg" value="#{msgs.add_assignment_invalid_points}" styleClass="alertMessageInline"
					rendered="#{gbItem.bulkNoPointsError == 'invalid'}" />
				<h:outputText id="invalidPtsErrMsgH" value="#{msgs.add_assignment_invalid_points}" styleClass="alertMessageInline errHide"
					rendered="#{gbItem.bulkNoPointsError != 'invalid'}" />
				<h:outputText id="precisionPtsErrMsg" value="#{msgs.add_assignment_invalid_precision_points}" styleClass="alertMessageInline"
					rendered="#{gbItem.bulkNoPointsError == 'precision'}" />
				<h:outputText id="precisionPtsErrMsgH" value="#{msgs.add_assignment_invalid_precision_points}" styleClass="alertMessageInline errHide"
					rendered="#{gbItem.bulkNoPointsError != 'precision'}" />
				--%>
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_header_due_date}"/>
				</f:facet>
				<t:inputCalendar id="dueDate" value="#{addAssignmentBean.dueDateChange}" renderAsPopup="true" renderPopupButtonAsImage="true"
					popupTodayString="#{msgs.date_entry_today_is}" popupWeekString="#{msgs.date_entry_week_header}" />
				<h:message for="dueDate" styleClass="alertMessageInline" />
			</h:column>
			
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_release}" escape="false"/>
				</f:facet>
        		<h:selectBooleanCheckbox id="released" value="#{addAssignmentBean.releaseChange}"
        			onclick="assignmentReleased((this.form.name + ':bulkNewAssignmentsTable:' + #{rowIndex}), true);"
        				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');" />
				
			</h:column>
			
			<h:column rendered="#{addAssignmentBean.localGradebook.grade_type != 3}">
				<f:facet name="header">
					<h:outputText value="#{msgs.add_assignment_include_in_cum}" escape="false"/>
				</f:facet>
				<h:selectBooleanCheckbox id="countAssignment" value="#{addAssignmentBean.countedChange}"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');" rendered="#{addAssignmentBean.localGradebook.grade_type != 3}" />
				
			</h:column>
		
		</t:dataTable>
		
		<div class="act calendarPadding">
		<h:commandButton
			id="saveButton"
			styleClass="active"
			action="addAssignmentBean.saveNewBulkAssignment"
			value="#{msgs.add_assignment_save}"/>
		<h:commandButton
			id="clearButton"
			value="#{msgs.add_assignment_cancel}"
			action="overview"
			immediate="true"/>
		</div>
		
	  </h:form>
	</div>
	
	
	
</f:view>

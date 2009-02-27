<f:view>
  <div class="portletBody">
	<h:form id="gbForm">
		<t:aliasBean alias="#{bean}" value="#{feedbackOptionsBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<sakai:flowState bean="#{feedbackOptionsBean}" />

		<h2><h:outputText value="#{msgs.feedback_options_page_title}"/></h2>

		<div class="instruction"><h:outputText value="#{msgs.feedback_options_instruction}" escape="false"/></div>

		<div class="indnt1">

<!-- Grade Display -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_display}"/></h4>
		<h:panelGrid columns="5" columnClasses="prefixedCheckbox">
			<%/*  Moved to gradebook setup page
			<h:selectBooleanCheckbox id="displayAssignmentGrades" value="#{feedbackOptionsBean.localGradebook.assignmentsDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayAssignmentGrades" value="#{msgs.feedback_options_grade_display_assignment_grades}" />*/%>

			<h:selectBooleanCheckbox id="displayCourseGrades" value="#{feedbackOptionsBean.localGradebook.courseGradeDisplayed}"
				onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_course_grades}" />
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_course_grades_2}" rendered="#{feedbackOptionsBean.localGradebook.grade_type==3}" />
			<h:commandLink action="#{feedbackOptionsBean.navigateToCourseGrades}" immediate="true" rendered="#{feedbackOptionsBean.localGradebook.grade_type==3}">
				<h:outputText value="#{msgs.appmenu_course_grades}"/>
				<f:param name="pageName" value="courseGradeDetails" />
			</h:commandLink>
			<h:outputLabel for="displayCourseGrades" value="#{msgs.feedback_options_grade_display_course_grades_3}" rendered="#{feedbackOptionsBean.localGradebook.grade_type==3}" />
		</h:panelGrid>

<!-- Grade Conversion -->
		<h4><h:outputText value="#{msgs.feedback_options_grade_conversion}" rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}"/></h4>
		<h:panelGrid cellpadding="0" cellspacing="0"
			columns="2"
			columnClasses="itemName"
			styleClass="itemSummary"
		 rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}">

			<h:outputText value="#{msgs.feedback_options_grade_type}"  rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}" />

			<h:panelGroup>
				<h:selectOneMenu id="selectGradeType" value="#{feedbackOptionsBean.selectedGradeMappingId}" rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}">
					<f:selectItems value="#{feedbackOptionsBean.gradeMappingsSelectItems}"/>
				</h:selectOneMenu>
				<f:verbatim> </f:verbatim>
				<h:commandButton actionListener="#{feedbackOptionsBean.changeGradeType}" value="#{msgs.feedback_options_change_grade_type}" rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}" />
			</h:panelGroup>
		</h:panelGrid>

		<%@include file="/inc/globalMessages.jspf"%>
		
<%-- /* Per SAK-15061, error message doesn't need to show up. *?
		<h:panelGroup rendered="#{!feedbackOptionsBean.isExistingConflictScale}" styleClass="validation">
		  <h:outputText value="#{msgs.feedback_options_existing_conflict1}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
	  	<h:outputLink value="http://kb.iu.edu/data/aitz.html" rendered="#{!feedbackOptionsBean.isExistingConflictScale}" target="support_window1">
	  		<h:outputText value="#{msgs.feedback_options_existing_conflict2}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		  </h:outputLink>
   		  <h:outputText value=" " rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		  <h:outputText value="#{msgs.feedback_options_existing_conflict3}" rendered="#{!feedbackOptionsBean.isExistingConflictScale}"/>
		</h:panelGroup>
--%>	
<%-- /*this error message doesn't need to show up for non-calc gradebook
		<h:panelGroup rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}" styleClass="validation">
		  <h:outputText value="#{msgs.feedback_options_cannot_change_percentage1}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  <h:outputLink value="http://kb.iu.edu/data/aitz.html" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}" target="support_window2">
		  	<h:outputText value="#{msgs.feedback_options_cannot_change_percentage2}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  </h:outputLink>
  		  <h:outputText value=" " rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		  <h:outputText value="#{msgs.feedback_options_cannot_change_percentage3}" rendered="#{!feedbackOptionsBean.isValidWithLetterGrade}"/>
		</h:panelGroup>	
--%>
<!-- RESET TO DEFAULTS LINK -->
		<p>
		<h:commandLink actionListener="#{feedbackOptionsBean.resetMappingValues}" rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}">
			<h:outputText value="#{msgs.feedback_options_reset_mapping_values}" rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}" />
		</h:commandLink>
		</p>

<!-- GRADE MAPPING TABLE -->
		<t:dataTable cellpadding="0" cellspacing="0"
			id="mappingTable"
			value="#{feedbackOptionsBean.gradeRows}"
			var="gradeRow"
			columnClasses="shorttext"
			styleClass="listHier narrowTable"
    	    rendered="#{feedbackOptionsBean.localGradebook.grade_type!=3}">
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_grade_header}"/>
				</f:facet>
				<h:outputText value="#{gradeRow.grade}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.feedback_options_percent_header}"/>
				</f:facet>
				<h:outputText value="#{gradeRow.mappingValue}"
					rendered="#{!gradeRow.gradeEditable}"/>
				<h:inputText id="mappingValue" value="#{gradeRow.mappingValue}"
					rendered="#{gradeRow.gradeEditable}"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');"/>
				<h:message for="mappingValue" styleClass="validationEmbedded" />
			</h:column>
		</t:dataTable>

		</div> <!-- END INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.feedback_options_submit}"
				action="#{feedbackOptionsBean.save}" >
				<f:param name="pageName" value="gradebookSetup" />
			</h:commandButton>
			<h:commandButton
				value="#{msgs.feedback_options_cancel}"
				action="#{feedbackOptionsBean.cancel}"
				immediate="true" >
				<f:param name="pageName" value="gradebookSetup" />
			</h:commandButton>
		</p>

	</h:form>
  </div>
</f:view>

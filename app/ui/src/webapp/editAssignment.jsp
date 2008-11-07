<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

		<sakai:flowState bean="#{editAssignmentBean}" />

		<t:aliasBean alias="#{bean}" value="#{editAssignmentBean}">
			<%@include file="/inc/appMenu.jspf"%>

			<%@include file="/inc/breadcrumb.jspf" %>
		</t:aliasBean>

		<div class="indnt1">

		<p class="instruction"><h:outputText value="#{msgs.edit_assignment_instruction}" /></p>

		<t:aliasBean alias="#{bean}" value="#{editAssignmentBean}">
			<%@include file="/inc/assignmentEditing.jspf"%>
		</t:aliasBean>

		</div>

		<p class="act calendarPadding">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.edit_assignment_save_changes}"
				action="#{editAssignmentBean.updateAssignment}"/>
			<h:commandButton
				value="#{msgs.add_assignment_cancel}"
				action="#{editAssignmentBean.navigateBack}"
				immediate="true"/>
		</p>
	  </h:form>
	</div>
</f:view>

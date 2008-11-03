<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">
	  
	  	<sakai:flowState bean="#{deleteAllGradesBean}" />
	  
	<h2><h:outputText value="#{msgs.gb_delete_all_grades_title}"/></h2>
	<div class="indnt2">
	<h:outputText styleClass="alertMessage" value="#{msgs.delete_all_grades_warning}"/>
		
	<%@include file="/inc/globalMessages.jspf"%>
	<p>
	<h:outputLabel styleClass="instruction" for="delete_all_grades" value="#{msgs.delete_all_saved_grades}" />
	<h:selectBooleanCheckbox id="delete_all_grades" value="#{deleteAllGradesBean.delete}" />
	<div class="act calendarPadding">
				<h:commandButton
					value="#{msgs.gb_delete_all_grades_cancel}"
					action="#{deleteAllGradesBean.processCancelDeleteAllGrades}" immediate="true"/>
				<h:commandButton
					value="#{msgs.gb_delete_all_grades_export}"
					action="#{deleteAllGradesBean.processExportGradebook}" />
				<h:commandButton
					value="#{msgs.gb_delete_all_grades_delete}"
					action="#{deleteAllGradesBean.processDeleteGrades}" styleClass="indnt10"/>	
	</div>	


	</div>
	
	</h:form>
	</div>
</f:view>
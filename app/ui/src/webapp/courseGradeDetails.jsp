<link href="dhtmlpopup/dhtmlPopup.css" rel="stylesheet" type="text/css" />
<script src="dhtmlpopup/dhtmlPopup.js" type="text/javascript"></script>
<f:view>
	<div class="portletBody">
	  <h:form id="gbForm">

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/appMenu.jspf"%>
		</t:aliasBean>

		<!-- Course Grade Summary -->
		<sakai:flowState bean="#{courseGradeDetailsBean}" />
		
		<h2><h:outputText value="#{msgs.course_grade_details_title}"/></h2>
		<p class="instruction">
			<h:outputText value="#{msgs.course_grade_details_null_msg}  " rendered="#{courseGradeDetailsBean.userAbleToGradeAll && !overviewBean.isLetterGrade}"/>
			<h:outputText value="#{msgs.course_grade_details_null_msg_ta_view} " rendered="#{!courseGradeDetailsBean.userAbleToGradeAll && !overviewBean.isLetterGrade}"/>
		</p>

		<h4><h:outputText value="#{msgs.course_grade_details_page_title}" rendered="#{!overviewBean.isLetterGrade}"/></h4>
		<div class="indnt1">
		<h:panelGrid cellpadding="0" cellspacing="0" columns="2"
			columnClasses="itemName"
			styleClass="itemSummary">
			<h:outputText id="pointsLabel" value="#{msgs.course_grade_details_points}" rendered="#{!courseGradeDetailsBean.weightingEnabled && !overviewBean.isLetterGrade && !courseGradeDetailsBean.gradeEntryByPercent}"/>
			<h:outputText id="points" value="#{courseGradeDetailsBean.totalPoints}" rendered="#{!courseGradeDetailsBean.weightingEnabled && !overviewBean.isLetterGrade && !courseGradeDetailsBean.gradeEntryByPercent}">
				<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
			</h:outputText>
			
			<h:outputText id="courseGradeLabel" value="#{msgs.avg_course_grade_name}" rendered="#{courseGradeDetailsBean.userAbleToGradeAll && !overviewBean.isLetterGrade}" />
			<h:panelGroup rendered="#{courseGradeDetailsBean.userAbleToGradeAll && !overviewBean.isLetterGrade}">
				<h:outputText id="letterGrade" value="#{courseGradeDetailsBean.averageCourseGrade} " />
				<h:outputText id="cumScore" value="#{courseGradeDetailsBean.courseGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.CLASS_AVG_CONVERTER" />
				</h:outputText>
			</h:panelGroup>	

		</h:panelGrid>
		</div>

		<h4><h:outputText value="#{msgs.assignment_details_grading_table}"/></h4>
		<div class="indnt1">

		<%@include file="/inc/globalMessages.jspf"%>
		
		<div class="instruction"><h:outputText value="#{msgs.course_grade_details_instruction}" escape="false" rendered="#{!overviewBean.isLetterGrade}"/></div>
		<div class="instruction"><h:outputText value="#{msgs.course_grade_details_instruction_noncalc}" escape="false" rendered="#{overviewBean.isLetterGrade}"/></div>

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/filterPaging.jspf"%>
		</t:aliasBean>

		<!-- Grading Table -->
		<t:dataTable cellpadding="0" cellspacing="0"
			id="gradingTable"
			value="#{courseGradeDetailsBean.scoreRows}"
			var="scoreRow"
			rowIndexVar="scoreRowIndex"
			sortColumn="#{courseGradeDetailsBean.sortColumn}"
            sortAscending="#{courseGradeDetailsBean.sortAscending}"
            columnClasses="left,left,left,left,left,left"
			styleClass="listHier">
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentSortName" propertyName="studentSortName" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_name}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.sortName}"/>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="studentDisplayId" propertyName="studentDisplayId" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_student_id}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.enrollment.user.displayId}"/>
			</h:column>
			<h:column rendered="#{!courseGradeDetailsBean.weightingEnabled && !overviewBean.isLetterGrade && !courseGradeDetailsBean.gradeEntryByPercent}">
				<f:facet name="header">
		            <t:commandSortHeader columnName="pointsEarned" propertyName="pointsEarned" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.assignment_details_points}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:outputText value="#{scoreRow.courseGradeRecord.pointsEarned}" rendered="#{scoreRow.calculatedLetterGrade != null && !overviewBean.isLetterGrade}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>
				
				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{scoreRow.calculatedLetterGrade == null && !overviewBean.isLetterGrade}"/>
			</h:column>
			<h:column rendered="#{!overviewBean.isLetterGrade}">
				<f:facet name="header">
		            <t:commandSortHeader columnName="adjustmentScore" propertyName="adjustmentScore" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{(courseGradeDetailsBean.gradeEntryByPercent) ? msgs.course_grade_details_adjustment_percentage : (courseGradeDetailsBean.weightingEnabled) ? msgs.course_grade_details_adjustment_percentage : msgs.course_grade_details_adjustment_points}" />
		            </t:commandSortHeader>
				</f:facet>
				<h:inputText rendered="#{scoreRow.userCanGrade}"
					id="AdjustmentScore"
					value="#{scoreRow.adjustmentScore}"
					size="4"
					onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
				</h:inputText>
				<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.adjustmentScore != null}" value="#{scoreRow.adjustmentScore}">
					<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.POINTS" />
				</h:outputText>
				<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.adjustmentScore == null}" value="#{msgs.score_null_placeholder}" />
			</h:column>
			<h:column>
				<h:message for="AdjustmentScore" styleClass="validationEmbedded" />
			</h:column>
			<h:column rendered="#{!overviewBean.isLetterGrade}">
				<f:facet name="header">
		            <t:commandSortHeader columnName="autoCalc" propertyName="autoCalc" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_calculated_grade}"/>
		            </t:commandSortHeader>
				</f:facet>
				<h:panelGroup rendered="#{scoreRow.calculatedLetterGrade !=  null && !overviewBean.isLetterGrade}">
					<h:outputFormat value="#{msgs.course_grade_details_grade_display}" >
						<f:param value="#{scoreRow.calculatedLetterGrade}" />
						<f:param value="#{scoreRow.calculatedPercentGrade}" />
					</h:outputFormat>
				</h:panelGroup>

				<h:outputText value="#{msgs.score_null_placeholder}" rendered="#{scoreRow.calculatedLetterGrade == null && !overviewBean.isLetterGrade}"/>

			</h:column>
			<h:column>
				<f:facet name="header">
					<h:outputText value="#{msgs.course_grade_details_log}" styleClass="tier0"/>
				</f:facet>
				<h:outputLink value="#"
					rendered="#{not empty scoreRow.eventRows}"
					onclick="javascript:dhtmlPopupToggle('#{scoreRowIndex}', event);return false;">
					<h:graphicImage value="images/log.png" alt="Show log"/>
				</h:outputLink>
			</h:column>
			<h:column>
				<f:facet name="header">
		            <t:commandSortHeader columnName="override" propertyName="override" arrow="true" immediate="false" actionListener="#{courseGradeDetailsBean.sort}">
						<h:outputText value="#{msgs.course_grade_details_grade}" rendered="#{!overviewBean.isLetterGrade}"/>
						<h:outputText value="#{msgs.course_grade_details_calculated_grade}" rendered="#{overviewBean.isLetterGrade}"/>
		            </t:commandSortHeader>
				</f:facet>
				<t:div styleClass="shorttext">
					<h:inputText rendered="#{scoreRow.userCanGrade}"
						id="Grade"
						value="#{scoreRow.enteredGrade}"
						size="4"
						onkeypress="return submitOnEnter(event, 'gbForm:saveButton');">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.COURSE_GRADE" />
					</h:inputText>
					<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.enteredGrade != null}" value="#{scoreRow.enteredGrade}">
						<f:converter converterId="org.sakaiproject.gradebook.jsf.converter.COURSE_GRADE" />
					</h:outputText>
					<h:outputText rendered="#{!scoreRow.userCanGrade && scoreRow.enteredGrade == null}" value="#{msgs.score_null_placeholder}" />

				</t:div>
			</h:column>
			<h:column>
				<h:message for="Grade" styleClass="validationEmbedded" />
			</h:column>
		</t:dataTable>

		<t:aliasBean alias="#{bean}" value="#{courseGradeDetailsBean}">
			<%@include file="/inc/gradingEventLogs.jspf"%>
		</t:aliasBean>

		<p class="instruction">
			<h:outputText value="#{msgs.course_grade_details_no_enrollments}" rendered="#{courseGradeDetailsBean.emptyEnrollments}" />
		</p>

		</div> <!-- END OF INDNT1 -->

		<p class="act">
			<h:commandButton
				id="saveButton"
				styleClass="active"
				value="#{msgs.assignment_details_submit}"
				actionListener="#{courseGradeDetailsBean.processUpdateGrades}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				/>
			<h:commandButton
				value="#{msgs.assignment_details_cancel}"
				action="courseGradeDetails"
				immediate="true"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				disabled="#{courseGradeDetailsBean.allStudentsViewOnly}"
				/>

			<h:commandButton
				value="#{msgs.course_grade_details_calculate_course_grade}"
				action="calculateCourseGrades"
				rendered="#{courseGradeDetailsBean.userAbleToGradeAll && !overviewBean.isLetterGrade}"
				style="margin-left: 5em;"
			/>
			<h:commandButton
				value="#{msgs.course_grade_details_export_course_grades}"
				actionListener="#{courseGradeDetailsBean.exportCsv}"
				rendered="#{!courseGradeDetailsBean.emptyEnrollments}"
				/>
		</p>

	  </h:form>
	</div>
</f:view>

<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
    <div class="portletBody">
        <h:form id="form">

            <%@include file="/inc/appMenu.jspf"%>

            <h2><h:outputText value="Preview Spreadsheet"/></h2>

            <div class="instruction"><h:outputText value="instructions here" escape="false"/></div>
            <p class="instruction"><h:outputText value="instructions here"/></p>
            <%@include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="Verify Upload"/></h4>

            <t:selectOneRadio id="assignment" layout="spread" converter="javax.faces.Integer">
                <f:selectItems  value="#{spreadsheetUploadPreviewBean.assignmentColumnSelectItems}" />
            </t:selectOneRadio>

            <t:dataTable id="table1" value="#{spreadsheetUploadPreviewBean.studentRows}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">

                <t:column styleClass="left">
                    <f:facet name="header">
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:columns value="#{spreadsheetPreviewBean.assignmentList}" var="colIndex" >
                    <f:facet name="header">
                        <h:panelGrid>
                            <t:outputText value="#{spreadsheetUploadPreview.assignmentHeader[colIndex]}" />
                        </h:panelGrid>
                    </f:facet>
                    <h:outputText value="#{row.rowcontent[colIndex + 1]}" />
                </t:columns>
            </t:dataTable>
            <f:verbatim><br></f:verbatim>
            <p>
                <h:commandButton
                        id="saveButton"
                        styleClass="active"
                        value="Ok"
                        action="#{spreadsheetUploadPreviewBean.saveFile}"/>


                <h:commandButton
                        value="Back"
                        action="spreadsheetUpload" immediate="true"/>
            </p>
        </h:form>
    </div>
</f:view>
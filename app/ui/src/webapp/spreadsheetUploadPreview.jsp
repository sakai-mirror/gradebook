<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
    <div class="portletBody">
        <h:form id="form">
            <%@include file="/inc/appMenu.jspf"%>
            <h2><h:outputText value="#{msgs.upload_preview_page_title}"/></h2>

            <div class="instruction">
                <h:outputText value="#{msgs.upload_preview_instructions}" escape="false"/>
                <h:outputText value="#{spreadsheetUploadPreviewBean.columnCount}" escape="false"/>
                <h:outputText value="#{spreadsheetUploadPreviewBean.rowCount}" escape="false"/>
            </div>
            <p class="instruction">
                <h:outputText value="#{msgs.upload_preview_additional_text}" escape="false"/>
            </p>
            <%@include file="/inc/globalMessages.jspf"%>
            <t:dataTable id="table1" value="#{spreadsheetUploadPreviewBean.studentRows}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">
                <t:column styleClass="left">
                    <f:facet name="header">
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:columns value="#{spreadsheetPreviewBean.assignmentList}" var="colIndex" >
                    <f:facet name="header">
                        <h:panelGrid>
                            <t:outputText value="#{spreadsheetUploadPreviewBean.assignmentHeaders[colIndex]}" />
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
                        value="#{msgs.upload_preview_ok}"
                        action="#{spreadsheetUploadPreviewBean.saveFile}"/>


                <h:commandButton
                        value="#{msgs.upload_preview_back}"
                        action="spreadsheetUpload" immediate="true"/>
            </p>
        </h:form>
    </div>
</f:view>
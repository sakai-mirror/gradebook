<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>

<f:view>
    <div class="portletBody">
        <h:form id="form">

            <%@include file="/inc/appMenu.jspf"%>

            <h2><h:outputText value="Preview Spreadsheet"/></h2>

            <div class="instruction"><h:outputText value="instructions here" escape="false"/></div>
            <p class="instruction"><h:outputText value="instructions here"/></p>
            <%@include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="Preview Spreadsheet"/></h4>

            <t:selectOneRadio id="assignment" layout="spread" converter="javax.faces.Integer">
                <f:selectItems  value="#{spreadsheetPreviewBean.assignmentColumnSelectItems}" />
            </t:selectOneRadio>

            <t:dataTable id="table1" value="#{spreadsheetPreviewBean.studentRows}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">

                <t:column styleClass="left">
                    <f:facet name="header">
                        <t:outputText value="Student ID"/>
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:column styleClass="left" >
                    <f:facet name="header">
                        <t:outputText value="Student Name"/>
                    </f:facet>
                    <h:outputText value="#{row.userDisplayName}"/>
                </t:column>

                <t:columns value="#{spreadsheetPreviewBean.assignmentList}" var="colIndex" >
                    <f:facet name="header">
                        <h:panelGrid>
                            <t:radio for=":form:assignment" index="#{colIndex}" />
                        </h:panelGrid>
                    </f:facet>
                    <h:outputText value="#{row.rowcontent[colIndex + 1]}" />
                </t:columns>
            </t:dataTable>

            <f:verbatim><br></f:verbatim>
            <p>
                <h:commandButton
                        id="importButton"
                        styleClass="active"
                        value="Import Selected"
                        action="#{spreadsheetPreviewBean.processFile}"/>

                <h:commandButton
                        value="Cancel"
                        action="spreadsheetListing" immediate="true"/>

                <t:outputText value="#{param.spreadsheetId}"/>
            </p>
        </h:form>
    </div>
</f:view>
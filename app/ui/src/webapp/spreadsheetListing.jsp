
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="gbForm">
            <%@include file="/inc/appMenu.jspf"%>
            <h2><h:outputText value="File Uploads"/></h2>
            <div class="instruction">
                <h:outputText value="instructions here" escape="false"/>
            </div>

            <h:panelGroup rendered="#{overviewBean.userAbleToEditAssessments}">
                <f:verbatim><p></f:verbatim>
                <h:commandLink action="spreadsheetUpload" immediate="true">
                    <h:outputText value="upload new spreadsheet"/>
                </h:commandLink>
                <f:verbatim></p></f:verbatim>
        </h:panelGroup>
        <%@include file="/inc/globalMessages.jspf"%>
        <h4><h:outputText value="Loading Dock"/></h4>

        <t:dataTable id="table1" value="#{spreadsheetListingBean.spreadSheets}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">

            <t:column>
                <h:outputText value="#{rowIndex + 1}"/>
            </t:column>

            <t:column>
                <f:facet name="header">
                    <h:outputText value="Title"/>
                </f:facet>
                <h:outputText value="#{row.title}"/>
            </t:column>
            <t:column>
                <f:facet name="header">
                    <h:outputText value="creator"/>
                </f:facet>
                <h:outputText value="#{row.userId}"/>
            </t:column>

            <t:column>
                <f:facet name="header">
                    <h:outputText value="modified by"/>
                </f:facet>
                <h:outputText value="#{row.userId}"/>
            </t:column>
            <t:column>
                <f:facet name="header">
                    <h:outputText value="last modified"/>
                </f:facet>
                <h:outputText value="#{row.date}">
                    <f:convertDateTime pattern="d MMM yyyy  H:m:s"/>
                </h:outputText>
            </t:column>

            <t:column>
                <h:commandLink action="#{spreadsheetListingBean.viewItem}">
                    <h:outputText value="view"/>
                    <f:param name="spreadsheetId" value="#{rowIndex}"/>
                </h:commandLink>
            </t:column>
            <t:column>
                <h:commandLink action="#{spreadsheetListingBean.deleteItem}">
                    <h:outputText value="delete"/>
                    <f:param name="spreadsheetId" value="#{rowIndex}"/>
                </h:commandLink>
            </t:column>
        </t:dataTable>
    </h:form>
</div>
</f:view>
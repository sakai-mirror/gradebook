
<%@ taglib uri="http://myfaces.apache.org/tomahawk" prefix="t"%>
<f:view>
    <div class="portletBody">
        <h:form id="gbForm">
            <%@include file="/inc/appMenu.jspf"%>
            <h2><h:outputText value="#{msgs.loading_dock_page_title}"/></h2>
            <div class="instruction">
                <h:outputText value="#{msgs.loading_dock_instructions}" escape="false"/>
            </div>

            <h:panelGroup rendered="#{overviewBean.userAbleToEditAssessments}">
                <h:commandLink action="spreadsheetUpload" immediate="true">
                    <h:outputText value="#{msgs.loading_dock_upload_link_text}"/>
                </h:commandLink>
            </h:panelGroup>
            <p/>
            <p/>
            <%@include file="/inc/globalMessages.jspf"%>
            <h4><h:outputText value="#{msgs.loading_dock_table_header}"/></h4>
            <t:dataTable id="table1" value="#{spreadsheetListingBean.spreadSheets}" var="row" rowIndexVar="rowIndex" styleClass="listHier" columnClasses="center">

                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_title}"/>
                    </f:facet>
                    <h:outputText value="#{row.title}"/>
                </t:column>
                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_creator}"/>
                    </f:facet>
                    <h:outputText value="#{row.displayName}"/>
                </t:column>

                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_modifiedby}"/>
                    </f:facet>
                    <h:outputText value="#{row.userId}"/>
                </t:column>
                <t:column>
                    <f:facet name="header">
                        <h:outputText value="#{msgs.loading_dock_table_lastmodified}"/>
                    </f:facet>
                    <h:outputText value="#{row.date}">
                        <f:convertDateTime pattern="d MMM yyyy  H:m:s"/>
                    </h:outputText>
                </t:column>

                <t:column>
                    <h:commandLink action="#{spreadsheetListingBean.viewItem}">
                        <h:outputText value="#{msgs.loading_dock_table_view}"/>
                        <f:param name="spreadsheetId" value="#{rowIndex}"/>
                    </h:commandLink>
                </t:column>
                <t:column>
                    <h:commandLink action="#{spreadsheetListingBean.deleteItem}">
                        <h:outputText value="#{msgs.loading_dock_table_delete}"/>
                        <f:param name="spreadsheetId" value="#{rowIndex}"/>
                    </h:commandLink>
                </t:column>
            </t:dataTable>
        </h:form>
    </div>
</f:view>
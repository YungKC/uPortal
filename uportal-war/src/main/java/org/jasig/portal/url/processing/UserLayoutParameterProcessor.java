/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.url.processing;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IStylesheetUserPreferencesService;
import org.jasig.portal.layout.IUserLayout;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.TransientUserLayoutManagerWrapper;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.url.IPortletRequestInfo;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This helper class processes HttpServletRequests for parameters relating to
 * user layout actions, propagating appropriate events to the user's layout
 * manager, preferences, and channel manager.
 * 
 * This class results from factoring the processUserLayoutParameters method out
 * of UserInstance in an effort to make UserInstance smaller and more literate.
 */
@Service("userLayoutRequestParameterProcessor")
public class UserLayoutParameterProcessor implements IRequestParameterProcessor {
    protected final Log logger = LogFactory.getLog(getClass());
    
    private IUserInstanceManager userInstanceManager;
    private IPortalUrlProvider portalUrlProvider;
    private IPortletWindowRegistry portletWindowRegistry;
    private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    
    @Autowired
    public void setStylesheetUserPreferencesService(IStylesheetUserPreferencesService stylesheetUserPreferencesService) {
        this.stylesheetUserPreferencesService = stylesheetUserPreferencesService;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }
    
    @Autowired
    public void setPortalUrlProvider(IPortalUrlProvider portalUrlProvider) {
        this.portalUrlProvider = portalUrlProvider;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Override
    public boolean processParameters(HttpServletRequest request, HttpServletResponse response) {
        final IPortalRequestInfo portalRequestInfo = this.portalUrlProvider.getPortalRequestInfo(request);
        
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        
        final IPerson person = userInstance.getPerson();
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        
        
        portalRequestInfo.getTargetedLayoutNodeId();
        final UrlState urlState = portalRequestInfo.getUrlState();
        switch (urlState) {
            case MAX:
                final IPortletRequestInfo portletRequestInfo = portalRequestInfo.getPortletRequestInfo();
                
                if (portletRequestInfo != null) {
                    final IPortletWindowId targetWindowId = portletRequestInfo.getTargetWindowId();
                    final IPortletEntity portletEntity = this.portletWindowRegistry.getParentPortletEntity(request, targetWindowId);
                    
                    final String channelSubscribeId = portletEntity.getChannelSubscribeId();
                    
                    final IStylesheetUserPreferences structureStylesheetUserPreferences = this.stylesheetUserPreferencesService.getStructureStylesheetUserPreferences(request);
                    structureStylesheetUserPreferences.setStylesheetParameter("userLayoutRoot", channelSubscribeId);
                    this.stylesheetUserPreferencesService.updateStylesheetUserPreferences(request, structureStylesheetUserPreferences);
                    
                    if (userLayoutManager instanceof TransientUserLayoutManagerWrapper) {
                        // get wrapper implementation for focusing
                        final TransientUserLayoutManagerWrapper transientUserLayoutManagerWrapper = (TransientUserLayoutManagerWrapper) userLayoutManager;
                        // .. and now set it as the focused id
                        transientUserLayoutManagerWrapper.setFocusedId(channelSubscribeId);
                    }
                    
                    //If portletRequestInfo was null just fall through to NORMAL state
                    break;
                }
                
            case NORMAL:
            default:
                final IStylesheetUserPreferences structureStylesheetUserPreferences = this.stylesheetUserPreferencesService.getStructureStylesheetUserPreferences(request);
                final String tabId = portalRequestInfo.getTargetedLayoutNodeId();
                if (tabId != null) {
                    structureStylesheetUserPreferences.setStylesheetParameter("focusedTabID", tabId);
                }
                structureStylesheetUserPreferences.setStylesheetParameter("userLayoutRoot", IUserLayout.ROOT_NODE_NAME);
                this.stylesheetUserPreferencesService.updateStylesheetUserPreferences(request, structureStylesheetUserPreferences);
            break;
        }
        
        //TODO after portlet processing is complete set minimized theme flags by subscribeId
        //TODO how are we going to track portlet minimization, what will the authoritative source of that info be? PortletWindow objects?

        return true;
    }
    
    protected String findTabIndex(IUserLayoutManager userLayoutManager, String tabId) {
        final String rootFolderId = userLayoutManager.getRootFolderId();
        final Enumeration<String> rootsChildren = userLayoutManager.getChildIds(rootFolderId);
        
        int tabIndex = 0;
        for (String topNodeId = rootsChildren.nextElement(); rootsChildren.hasMoreElements(); topNodeId = rootsChildren.nextElement()) {
            tabIndex++;
            if (tabId.equals(topNodeId)) {
                return Integer.toString(tabIndex);
            }
        }
        
        return "none";
    }
}

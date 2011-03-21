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

package org.jasig.portal.io.xml.ssd;

import javax.xml.namespace.QName;

import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.layout.om.IStylesheetDescriptor;

/**
 * Describes the {@link IStylesheetDescriptor} for import and export.
 * 
 * TODO probably just need to move this data into an XML file to make editing it easier?
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class StylesheetDescriptorPortalDataType implements IPortalDataType {
    public static final StylesheetDescriptorPortalDataType INSTANCE = new StylesheetDescriptorPortalDataType();
    
    private static final QName PORTLET_DEFINITION_NAME = new QName("https://source.jasig.org/schemas/uportal/io/stylesheet-descriptor", "stylesheet-descriptor");
    
    @Override
    public String getTitle() {
        return "Stylesheet Descriptor";
    }

    @Override
    public QName getName() {
        return PORTLET_DEFINITION_NAME;
    }

    @Override
    public String getDescription() {
        return "Stylesheet Descriptors published in the portal";
    }
}
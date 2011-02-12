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

package org.jasig.portal.portlet.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision: 337 $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaTestApplicationContext.xml")
public class JpaPortletDaoTest extends BaseJpaDaoTest {
    private IPortletTypeDao jpaChannelTypeDao;
    private IPortletDefinitionDao jpaPortletDefinitionDao;
    private IPortletEntityDao jpaPortletEntityDao;

    @Autowired
    public void setJpaPortletEntityDao(final IPortletEntityDao jpaPortletEntityDao) {
        this.jpaPortletEntityDao = jpaPortletEntityDao;
    }
    @Autowired
    public void setJpaPortletDefinitionDao(final IPortletDefinitionDao dao) {
        this.jpaPortletDefinitionDao = dao;
    }
    @Autowired
    public void setJpaChannelTypeDao(IPortletTypeDao jpaChannelTypeDao) {
        this.jpaChannelTypeDao = jpaChannelTypeDao;
    }

    @Before
    public void onSetUp() throws Exception {
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (final IPortletDefinition portletDefinition : jpaPortletDefinitionDao.getPortletDefinitions()) {
                    jpaPortletDefinitionDao.deletePortletDefinition(portletDefinition);
                }
                
                for (final IPortletType portletType : jpaChannelTypeDao.getPortletTypes()) {
                    jpaChannelTypeDao.deletePortletType(portletType);
                }
                
                return null;
            }
        });
    }

    @Test
    public void testNoopOperations() throws Exception {
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletDefinitionId portletDefinitionId = new PortletDefinitionIdImpl(1);
                final IPortletDefinition nullPortDef1 = jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
                assertNull(nullPortDef1);
                
                final IPortletEntity nullPortEnt1 = jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
                assertNull(nullPortEnt1);
                
                final Set<IPortletEntity> portEnts = jpaPortletEntityDao.getPortletEntities(new PortletDefinitionIdImpl(1));
                assertEquals(Collections.emptySet(), portEnts);
                
                return null;
            }
        });
    }

    @Test
    public void testAllDefinitionDaoMethods() throws Exception {
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletType channelType = jpaChannelTypeDao.createPortletType("BaseType", "foobar");
                
                //Create a definition
                final IPortletDefinition chanDef1 = jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
                
                //Try all of the retrieval options
                final IPortletDefinition portDef1a = jpaPortletDefinitionDao.getPortletDefinition(chanDef1.getPortletDefinitionId());
                assertEquals(chanDef1, portDef1a);
                
                //Create a second definition with the same app/portlet
                IPortletDefinition chanDef2 = jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname2", "Test Portlet 2", "Test Portlet 2 Title", "/uPortal", "portletName2", true);
        
        
                // Add some preferences
                final IPortletPreferences prefs2 = chanDef2.getPortletPreferences();
                final List<IPortletPreference> prefsList2 = prefs2.getPortletPreferences();
                prefsList2.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
                prefsList2.add(new PortletPreferenceImpl("prefName2", true, "val3", "val4"));
                
                jpaPortletDefinitionDao.updatePortletDefinition(chanDef2);
                
                
                // Check prefs, remove one and another
                final IPortletDefinition portDef3 = jpaPortletDefinitionDao.getPortletDefinition(chanDef2.getPortletDefinitionId());
                final IPortletPreferences prefs3 = portDef3.getPortletPreferences();
                final List<IPortletPreference> prefsList3 = prefs3.getPortletPreferences();
                
                final List<IPortletPreference> expectedPrefsList3 = new ArrayList<IPortletPreference>();
                expectedPrefsList3.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
                expectedPrefsList3.add(new PortletPreferenceImpl("prefName2", true, "val3", "val4"));
                
                assertEquals(expectedPrefsList3, prefsList3);
                
                
                prefsList3.remove(1);
                prefsList3.add(new PortletPreferenceImpl("prefName3", false, "val5", "val6"));
                
                jpaPortletDefinitionDao.updatePortletDefinition(portDef3);
                
        
                // Check prefs
                final IPortletDefinition portDef4 = jpaPortletDefinitionDao.getPortletDefinition(portDef3.getPortletDefinitionId());
                final IPortletPreferences prefs4 = portDef4.getPortletPreferences();
                final List<IPortletPreference> prefsList4 = prefs4.getPortletPreferences();
                
                final List<IPortletPreference> expectedPrefsList4 = new ArrayList<IPortletPreference>();
                expectedPrefsList4.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
                expectedPrefsList4.add(new PortletPreferenceImpl("prefName3", false, "val5", "val6"));
                
                assertEquals(expectedPrefsList4, prefsList4);
                
                return null;
            }
        });
    }
    
    @Test
    public void testAllEntityDaoMethods() throws Exception {
        final IPortletDefinitionId portletDefinitionId = execute(new Callable<IPortletDefinitionId>() {
            @Override
            public IPortletDefinitionId call() throws Exception {

                final IPortletType channelType = jpaChannelTypeDao.createPortletType("BaseType", "foobar");
                
                //Create a definition
                final IPortletDefinition chanDef1 = jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
                return chanDef1.getPortletDefinitionId();
            }
        });
        
        
        final IPortletEntityId portletEntityId = execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                IPortletEntity portEnt1 = jpaPortletEntityDao.createPortletEntity(portletDefinitionId, "chanSub1", 1);
                
                return portEnt1.getPortletEntityId();
            }
        });
                

        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception { 
                final IPortletEntity portEnt1a = jpaPortletEntityDao.getPortletEntity(portletEntityId);
                assertNotNull(portEnt1a);
                
                final IPortletEntity portEnt1b = jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
                assertEquals(portEnt1a, portEnt1b);
                
                final IPortletEntity portEnt1c = jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
                assertEquals(portEnt1b, portEnt1c);
                
                final Set<IPortletEntity> portletEntities1 = jpaPortletEntityDao.getPortletEntities(portletDefinitionId);
                assertEquals(Collections.singleton(portEnt1a), portletEntities1);
                
                final Set<IPortletEntity> portletEntitiesByUser = jpaPortletEntityDao.getPortletEntitiesForUser(1);
                assertEquals(Collections.singleton(portEnt1a), portletEntitiesByUser);
                
                return null;
            }
        });
                
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception { 
                //Add entity and preferences
                final IPortletDefinition portDef1 = jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
                portDef1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("defpref1", false, "dpv1", "dpv2"));
                jpaPortletDefinitionDao.updatePortletDefinition(portDef1);
                
                final IPortletEntity portEnt1 = jpaPortletEntityDao.getPortletEntity(portletEntityId);
                portEnt1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("entpref1", false, "epv1", "epv2"));
                jpaPortletEntityDao.updatePortletEntity(portEnt1);
                
                return null;
            }
        });
                
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception { 
                //Delete whole tree
                final IPortletDefinition portDef2 = jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
                jpaPortletDefinitionDao.deletePortletDefinition(portDef2);
                
                return null;
            }
        });
                
        execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception { 
                //Verify it is gone
                final Set<IPortletEntity> portletEntities2 = jpaPortletEntityDao.getPortletEntities(portletDefinitionId);
                assertEquals(Collections.emptySet(), portletEntities2);
                
                return null;
            }
        });
    }
    
    public static class Util {
        public static <T> Set<T> unmodifiableSet(T... o) {
            return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(o)));
        }
    }
}

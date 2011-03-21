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

package org.jasig.portal.layout.dao.jpa;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

import org.jasig.portal.IUserProfile;
import org.jasig.portal.layout.dao.IStylesheetDescriptorDao;
import org.jasig.portal.layout.dao.IStylesheetUserPreferencesDao;
import org.jasig.portal.layout.om.IOutputPropertyDescriptor;
import org.jasig.portal.layout.om.IStylesheetData.Scope;
import org.jasig.portal.layout.om.IStylesheetDescriptor;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.portlet.dao.jpa.BaseJpaDaoTest;
import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaStylesheetDaoTestContext.xml")
public class JpaStylesheetDaoTest extends BaseJpaDaoTest {
    private IStylesheetDescriptorDao stylesheetDescriptorDao;
    private IStylesheetUserPreferencesDao stylesheetUserPreferencesDao;

    @Autowired
    public void setStylesheetDescriptorDao(IStylesheetDescriptorDao stylesheetDescriptorDao) {
        this.stylesheetDescriptorDao = stylesheetDescriptorDao;
    }
    
    @Autowired
    public void setStylesheetUserPreferencesDao(IStylesheetUserPreferencesDao stylesheetUserPreferencesDao) {
        this.stylesheetUserPreferencesDao = stylesheetUserPreferencesDao;
    }

    @Before
    public void onSetUp() throws Exception {
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (final IStylesheetDescriptor stylesheetDescriptor : stylesheetDescriptorDao.getStylesheetDescriptors()) {
                    stylesheetDescriptorDao.deleteStylesheetDescriptor(stylesheetDescriptor);
                }
                
                for (final IStylesheetUserPreferences stylesheetUserPreferences : stylesheetUserPreferencesDao.getStylesheetUserPreferences()) {
                    stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(stylesheetUserPreferences);
                }
                
                return null;
            }
        });
    }

    @Test
    public void testStylesheetDescriptorDao() throws Exception {
        final String ssName1 = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.createStylesheetDescriptor("columns", "classpath:/layout/struct/columns.xsl");
                
                assertNotSame(-1, stylesheetDescriptor.getId());
                
                return stylesheetDescriptor.getName();
            }
        });
        
        final String ssName2 = this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.createStylesheetDescriptor("lists", "classpath:/layout/struct/lists.xsl");
                
                assertNotSame(-1, stylesheetDescriptor.getId());
                
                return stylesheetDescriptor.getName();
            }
        });
        
        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptorByName(ssName1);
                assertNotNull(stylesheetDescriptor);
                
                final Collection<IOutputPropertyDescriptor> outputPropertyDescriptors = stylesheetDescriptor.getOutputPropertyDescriptors();
                assertEquals(0, outputPropertyDescriptors.size());
                stylesheetDescriptor.setOutputPropertyDescriptor(new OutputPropertyDescriptorImpl("propA", Scope.PERSISTENT));
                
                stylesheetDescriptorDao.updateStylesheetDescriptor(stylesheetDescriptor);
                
                return null;
            }
        });
        
        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptorByName(ssName2);
                assertNotNull(stylesheetDescriptor);
                
                final Collection<IOutputPropertyDescriptor> outputPropertyDescriptors = stylesheetDescriptor.getOutputPropertyDescriptors();
                assertEquals(0, outputPropertyDescriptors.size());
                stylesheetDescriptor.setOutputPropertyDescriptor(new OutputPropertyDescriptorImpl("propA", Scope.SESSION));
                
                stylesheetDescriptorDao.updateStylesheetDescriptor(stylesheetDescriptor);
                
                return null;
            }
        });
        
        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptorByName(ssName1);
                assertNotNull(stylesheetDescriptor);
                
                final Collection<IOutputPropertyDescriptor> outputPropertyDescriptors = stylesheetDescriptor.getOutputPropertyDescriptors();
                assertEquals(1, outputPropertyDescriptors.size());
                stylesheetDescriptor.setOutputPropertyDescriptor(new OutputPropertyDescriptorImpl("propA", Scope.REQUEST));
                
                stylesheetDescriptorDao.updateStylesheetDescriptor(stylesheetDescriptor);
                
                return null;
            }
        });
        
        this.execute(new Callable<String>() {
            @Override
            public String call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptorByName(ssName1);
                assertNotNull(stylesheetDescriptor);
                
                final Collection<IOutputPropertyDescriptor> outputPropertyDescriptors = stylesheetDescriptor.getOutputPropertyDescriptors();
                assertEquals(1, outputPropertyDescriptors.size());
                                
                return null;
            }
        });
    }
    

    @Test
    public void testStylesheetUserPreferencesDao() throws Exception {
        final long ssdId = this.execute(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.createStylesheetDescriptor("columns", "classpath:/layout/struct/columns.xsl");
                
                final long id = stylesheetDescriptor.getId();
                assertNotSame(-1, id);
                
                return id;
            }
        });

        final IPerson person = mock(IPerson.class);
        when(person.getID()).thenReturn(1);
        
        
        final IUserProfile userProfile = mock(IUserProfile.class);
        when(userProfile.getProfileId()).thenReturn(1);
        
        final long supId = this.execute(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptor(ssdId);
                
                final IStylesheetUserPreferences stylesheetUserPreferences = stylesheetUserPreferencesDao.createStylesheetUserPreferences(stylesheetDescriptor, person, userProfile);
                
                assertNotNull(stylesheetUserPreferences);
                
                stylesheetUserPreferences.setStylesheetParameter("activeTab", "1");
                stylesheetUserPreferences.setOutputProperty("media", "xhtml");
                stylesheetUserPreferences.setLayoutAttribute("u1l1n1", "deletable", "false");
                
                stylesheetUserPreferencesDao.storeStylesheetUserPreferences(stylesheetUserPreferences);
                
                return stylesheetUserPreferences.getId();
            }
        });
        
        this.execute(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                final IStylesheetUserPreferences stylesheetUserPreferences = stylesheetUserPreferencesDao.getStylesheetUserPreferences(supId);
                
                assertNotNull(stylesheetUserPreferences);
                assertEquals(Collections.singletonMap("activeTab", "1"), stylesheetUserPreferences.getStylesheetParameters());
                assertEquals(Collections.singletonMap("media", "xhtml"), stylesheetUserPreferences.getOutputProperties());
                assertEquals(Collections.singletonMap("deletable", "false"), stylesheetUserPreferences.getLayoutAttributes("u1l1n1"));
                
                return null;
            }
        });
        
        this.execute(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                final IStylesheetDescriptor stylesheetDescriptor = stylesheetDescriptorDao.getStylesheetDescriptor(ssdId);
                
                final IStylesheetUserPreferences stylesheetUserPreferences = stylesheetUserPreferencesDao.getStylesheetUserPreferences(stylesheetDescriptor, person, userProfile);
                
                assertNotNull(stylesheetUserPreferences);
                assertEquals(Collections.singletonMap("activeTab", "1"), stylesheetUserPreferences.getStylesheetParameters());
                assertEquals(Collections.singletonMap("media", "xhtml"), stylesheetUserPreferences.getOutputProperties());
                assertEquals(Collections.singletonMap("deletable", "false"), stylesheetUserPreferences.getLayoutAttributes("u1l1n1"));
                
                stylesheetUserPreferencesDao.deleteStylesheetUserPreferences(stylesheetUserPreferences);
                
                return null;
            }
        });
    }
}

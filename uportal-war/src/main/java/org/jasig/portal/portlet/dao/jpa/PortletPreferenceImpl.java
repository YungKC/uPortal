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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.container.PortletPreference;
import org.apache.pluto.container.om.portlet.Preference;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.jasig.portal.portlet.om.IPortletPreference;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_PREF")
@SequenceGenerator(
        name="UP_PORTLET_PREF_GEN",
        sequenceName="UP_PORTLET_PREF_SEQ",
        allocationSize=10
    )
@TableGenerator(
        name="UP_PORTLET_PREF_GEN",
        pkColumnValue="UP_PORTLET_PREF",
        allocationSize=10
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletPreferenceImpl implements IPortletPreference {

    @Id
    @GeneratedValue(generator = "UP_PORTLET_PREF_GEN")
    @Column(name = "PORTLET_PREF_ID")
    private final long portletPreferenceId;
    
    @Column(name = "NAME")//, nullable = false)
    @Type(type = "nullSafeClob")
    private String name = null;
    
    @Column(name = "READ_ONLY", nullable = false)
    private boolean readOnly = false;
    
    @ElementCollection(fetch =FetchType.EAGER, targetClass = String.class)
    @JoinTable(
        name = "UP_PORTLET_PREF_VALUES",
        joinColumns = @JoinColumn(name = "PORTLET_PREF_ID")
    )
    @IndexColumn(name = "VALUE_ORDER")
    @Type(type = "nullSafeClob")
    @Column(name = "VALUE")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private List<String> values = null;
    
    
    public PortletPreferenceImpl() {
        this.portletPreferenceId = -1;
    }
    
    public PortletPreferenceImpl(PortletPreferenceImpl portletPreference) {
        this.portletPreferenceId = -1;
        this.name = portletPreference.getName();
        this.readOnly = portletPreference.isReadOnly();
        this.setValues(portletPreference.getValues());
    }
    
    public PortletPreferenceImpl(PortletPreference portletPreference) {
        this.portletPreferenceId = -1;
        this.name = portletPreference.getName();
        this.readOnly = portletPreference.isReadOnly();

        final String[] values = portletPreference.getValues();
        this.setValues(values);
    }
    public PortletPreferenceImpl(Preference preference) {
    	this.portletPreferenceId = -1;
    	this.name = preference.getName();
    	this.readOnly = preference.isReadOnly();
    	
    	this.setValues(preference.getValues().toArray(new String[]{}));
    }
    
    public PortletPreferenceImpl(String name, boolean readOnly, String... values) {
        this.portletPreferenceId = -1;
        this.name = name;
        this.readOnly = readOnly;
        this.setValues(values);
    }

    
    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#getValues()
     */
    @Override
    public String[] getValues() {
        if (this.values == null) {
            return null;
        }

        return this.values.toArray(new String[this.values.size()]);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#isReadOnly()
     */
    @Override
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    @Override
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#setValues(java.lang.String[])
     */
    @Override
    public void setValues(String[] values) {
        if (values == null) {
            this.values = null;
        }
        else if (this.values == null) {
            this.values = new ArrayList<String>(Arrays.asList(values));
        }
        else {
            this.values.clear();
            this.values.addAll(Arrays.asList(values));
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.pluto.container.PortletPreference#clone()
     */
    @Override
    public PortletPreference clone() {
        return new PortletPreferenceImpl(this);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletPreference)) {
            return false;
        }
        PortletPreferenceImpl rhs = (PortletPreferenceImpl) object;
        return new EqualsBuilder()
            .append(this.name, rhs.getName())
            .append(this.readOnly, rhs.isReadOnly())
            .append(this.getValues(), rhs.getValues())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1904185833, -1222355625)
            .append(this.name)
            .append(this.readOnly)
            .append(this.values)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletPreferenceId", this.portletPreferenceId)
            .append("name", this.name)
            .append("readOnly", this.readOnly)
            .append("values", this.values)
            .toString();
    }
}

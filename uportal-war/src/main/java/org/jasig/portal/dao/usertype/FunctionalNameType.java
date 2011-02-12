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

package org.jasig.portal.dao.usertype;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.regex.Pattern;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

/**
 * Uses a regular expression to validate strings coming to/from the database.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FunctionalNameType extends BaseUserType<String> {
    public static final Pattern INVALID_CHARS_PATTERN = Pattern.compile("[^\\w-]");
    public static final Pattern VALID_CHARS_PATTERN = Pattern.compile("[\\w-]");
    public static final Pattern VALID_FNAME_PATTERN = Pattern.compile("^[\\w-]+$");

    public FunctionalNameType() {
        super(VarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
    }

    @Override
    public String nullSafeGet(ResultSet rs, String[] names, Object owner) throws HibernateException, SQLException {
        final String value = super.nullSafeGet(rs, names, owner);
        
        if (value == null) {
            return null;
        }
        
        if (!VALID_FNAME_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Value from database '" + value + "' does not validate against pattern: " + VALID_FNAME_PATTERN.pattern());
        }

        return value;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
        if (value != null && !VALID_FNAME_PATTERN.matcher((String) value).matches()) {
            throw new IllegalArgumentException("Value being stored '" + value + "' does not validate against pattern: " + VALID_FNAME_PATTERN.pattern());
        }
        
        super.nullSafeSet(st, value, index);
    }
}

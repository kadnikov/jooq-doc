/*
 * Copyright 2014 Florian Müller & Jay Brown
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * This code is based on the Apache Chemistry OpenCMIS FileShare project
 * <http://chemistry.apache.org/java/developing/repositories/dev-repositories-fileshare.html>.
 *
 * It is part of a training exercise and not intended for production use!
 *
 */
package ru.doccloud.cmis.server.util;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.*;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.exceptions.*;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigInteger;
import java.util.*;

public final class FileBridgeUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileBridgeUtils.class);


    private FileBridgeUtils() {
    }

    /**
     * Returns the boolean value of the given value or the default value if the
     * given value is <code>null</code>.
     */
    public static boolean getBooleanParameter(Boolean value, boolean def) {

        return value == null ? def : value;
    }

    /**
     * Converts milliseconds into a {@link GregorianCalendar} object, setting
     * the timezone to GMT and cutting milliseconds off.
     */
    public static GregorianCalendar millisToCalendar(long millis) {
        GregorianCalendar result = new GregorianCalendar();
        result.setTimeZone(TimeZone.getTimeZone("GMT"));
        result.setTimeInMillis((long) (Math.ceil((double) millis / 1000) * 1000));

        return result;
    }






    /**
     * Splits a filter statement into a collection of properties. If
     * <code>filter</code> is <code>null</code>, empty or one of the properties
     * is '*' , an empty collection will be returned.
     */
    public static Set<String> splitFilter(String filter) {
        if (filter == null) {
            return null;
        }

        if (filter.trim().length() == 0) {
            return null;
        }

        Set<String> result = new HashSet<String>();
        for (String s : filter.split(",")) {
            s = s.trim();
            if (s.equals("*")) {
                return null;
            } else if (s.length() > 0) {
                result.add(s);
            }
        }

        // set a few base properties
        // query name == id (for base type properties)
        result.add(PropertyIds.OBJECT_ID);
        result.add(PropertyIds.OBJECT_TYPE_ID);
        result.add(PropertyIds.BASE_TYPE_ID);

        return result;
    }

    /**
     * Gets the type id from a set of properties.
     */
    public static String getObjectTypeId(Properties properties) {
        LOGGER.trace("entering  getObjectTypeId(properties={})", properties);
        PropertyData<?> typeProperty = properties.getProperties().get(PropertyIds.OBJECT_TYPE_ID);
        if (!(typeProperty instanceof PropertyId)) {
            throw new CmisInvalidArgumentException("Type Id must be set!");
        }

        String typeId = ((PropertyId) typeProperty).getFirstValue();
        if (typeId == null) {
            throw new CmisInvalidArgumentException("Type Id must be set!");
        }

        LOGGER.trace("leaving getStringProperty(): objectTypeId {}", typeId);
        return typeId;
    }

    /**
     * Returns the first value of an id property.
     */
    public static String getIdProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyId)) {
            return null;
        }

        return ((PropertyId) property).getFirstValue();
    }

    /**
     * Returns the first value of a string property.
     */
    public static String getStringProperty(Properties properties, String name) {
        LOGGER.trace("entering  getStringProperty(properties={}, name={})", properties, name);
        PropertyData<?> property = properties.getProperties().get(name);

        if (!(property instanceof PropertyString)) {
            return null;
        }

        LOGGER.trace("leaving getStringProperty(): found property {}", property);
        return ((PropertyString) property).getFirstValue();
    }

    /**
     * Returns the first value of a int property.
     */
    public static BigInteger getIntegerProperty(Properties properties, String name) {
        LOGGER.trace("entering  getIntegerProperty(properties={}, name={})", properties, name);
        PropertyData<?> property = properties.getProperties().get(name);


        if (!(property instanceof PropertyInteger)) {
            return null;
        }

        LOGGER.trace("leaving getIntegerProperty(): found property {}", property);
        return ((PropertyInteger) property).getFirstValue();
    }

    /**
     * Returns the first value of a datetime property.
     */
    public static GregorianCalendar getDateTimeProperty(Properties properties, String name) {
        PropertyData<?> property = properties.getProperties().get(name);
        if (!(property instanceof PropertyDateTime)) {
            return null;
        }

        return ((PropertyDateTime) property).getFirstValue();
    }

    public static void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, value));
    }

    public static void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
                                           GregorianCalendar value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }

    private static boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id, TypeDefinition type) {
        if ((properties == null) || (properties.getProperties() == null)) {
            throw new IllegalArgumentException("Properties must not be null!");
        }

        if (id == null) {
            throw new IllegalArgumentException("Id must not be null!");
        }

        if (type == null) {
            throw new IllegalArgumentException("Unknown type: " + typeId);
        }
        if (!type.getPropertyDefinitions().containsKey(id)) {
            throw new IllegalArgumentException("Unknown property: " + id);
        }

        String queryName = type.getPropertyDefinitions().get(id).getQueryName();

        if ((queryName != null) && (filter != null)) {
            if (!filter.contains(queryName)) {
                return false;
            } else {
                filter.remove(queryName);
            }
        }

        return true;
    }

    public static void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    /**
     * Checks a property set for an update.
     */
    public static void checkUpdateProperties(Properties properties, String typeId, TypeDefinition type) {
        // check properties
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check the name
        String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        if (name != null) {
            if (!isValidName(name)) {
                throw new CmisNameConstraintViolationException("Name is not valid!");
            }
        }

        // check type properties
        checkTypeProperties(properties, typeId, false, type);
    }

    /**
     * Checks if the property belong to the type and are settable.
     */
    private static void checkTypeProperties(Properties properties, String typeId, boolean isCreate, TypeDefinition type) {
        // check type
//        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        // check if all required properties are there
        for (PropertyData<?> prop : properties.getProperties().values()) {
            PropertyDefinition<?> propType = type.getPropertyDefinitions().get(prop.getId());

            // do we know that property?
            if (propType == null) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is unknown!");
            }

            // can it be set?
            if (propType.getUpdatability() == Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + prop.getId() + "' is readonly!");
            }

            if (!isCreate) {
                // can it be set?
                if (propType.getUpdatability() == Updatability.ONCREATE) {
                    throw new CmisConstraintException("Property '" + prop.getId() + "' cannot be updated!");
                }
            }
        }
    }


    /**
     * Checks a property set for a new object.
     */
    public static void checkNewProperties(Properties properties, BaseTypeId baseTypeId, TypeDefinition type) {
        // check properties
        if (properties == null || properties.getProperties() == null) {
            throw new CmisInvalidArgumentException("Properties must be set!");
        }

        // check the name
        String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        if (!isValidName(name)) {
            throw new CmisNameConstraintViolationException("Name is not valid!");
        }

        // check the type
        String typeId = getObjectTypeId(properties);
        if (typeId == null) {
            throw new CmisInvalidArgumentException("Type Id is not set!");
        }


        if (type.getBaseTypeId() != baseTypeId) {
            if (baseTypeId == BaseTypeId.CMIS_DOCUMENT) {
                throw new CmisInvalidArgumentException("Type is not a document type!");
            } else if (baseTypeId == BaseTypeId.CMIS_FOLDER) {
                throw new CmisInvalidArgumentException("Type is not a folder type!");
            } else {
                throw new CmisRuntimeException("A file system does not support a " + baseTypeId.value() + " type!");
            }
        }

        // check type properties
        checkTypeProperties(properties, typeId, true, type);

        // check if required properties are missing
        for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
            if (propDef.isRequired() && !properties.getProperties().containsKey(propDef.getId())
                    && propDef.getUpdatability() != Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
            }
        }
    }

    /**
     * Checks a property set for a copied document.
     */
    public static void checkCopyProperties(Properties properties, String sourceTypeId, TypeDefinition type) {
        // check properties
        if (properties == null || properties.getProperties() == null) {
            return;
        }

        String typeId = sourceTypeId;

        // check the name
        String name = FileBridgeUtils.getStringProperty(properties, PropertyIds.NAME);
        if (name != null) {
            if (!isValidName(name)) {
                throw new CmisNameConstraintViolationException("Name is not valid!");
            }
        }

        // check the type
        typeId = FileBridgeUtils.getObjectTypeId(properties);
        if (typeId == null) {
            typeId = sourceTypeId;
        }

//        TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
        if (type == null) {
            throw new CmisObjectNotFoundException("Type '" + typeId + "' is unknown!");
        }

        if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
            throw new CmisInvalidArgumentException("Target type must be a document type!");
        }

        // check type properties
        checkTypeProperties(properties, typeId, true, type);

        // check if required properties are missing
        for (PropertyDefinition<?> propDef : type.getPropertyDefinitions().values()) {
            if (propDef.isRequired() && !properties.getProperties().containsKey(propDef.getId())
                    && propDef.getUpdatability() != Updatability.READONLY) {
                throw new CmisConstraintException("Property '" + propDef.getId() + "' is required!");
            }
        }
    }

    /**
     * Checks if the given name is valid for a file system.
     *
     * @param name
     *            the name to check
     *
     * @return <code>true</code> if the name is valid, <code>false</code>
     *         otherwise
     */
    public static boolean isValidName(final String name) {
        return !(StringUtils.isBlank(name) || name.indexOf(File.separatorChar) != -1
                || name.indexOf(File.pathSeparatorChar) != -1);
    }

    public static void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }
        props.addProperty(new PropertyStringImpl(id, value));
    }

    public static void addPropertyIdList(PropertiesImpl props, String typeId, Set<String> filter, String id,
                                         List<String> value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }

    public static void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value, TypeDefinition type) {
        addPropertyBigInteger(props, typeId, filter, id, BigInteger.valueOf(value), type);
    }

    public static void addPropertyBigInteger(PropertiesImpl props, String typeId, Set<String> filter, String id,
                                             BigInteger value, TypeDefinition type) {
        if (!checkAddProperty(props, typeId, filter, id, type)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }
}
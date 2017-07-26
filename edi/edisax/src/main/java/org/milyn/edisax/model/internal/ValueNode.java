/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/

package org.milyn.edisax.model.internal;

import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.DecodeType;
import org.milyn.javabean.DataDecodeException;
import org.milyn.config.Configurable;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * ValueNode.
 *
 * @author bardl
 */
public class ValueNode extends MappingNode {

    protected Boolean required;
    private String dataType;
    private List<Map.Entry<String,String>> parameters;
    private Integer minLength;
    private Integer maxLength;
    private DataDecoder decoder;
    private Class<?> typeClass;
    private Properties decodeParams;
    private Integer maxOccurs;

    public ValueNode() {
	}
    
	public ValueNode(String xmltag, String namespace, Boolean required) {
		super(xmltag, namespace);
		minLength = 0;
		maxLength = 1;
        this.required = required;
    }

	public String getDataType() {
        return dataType;
    }
    
    public void setDataType(String dataType) {
        this.dataType = dataType;
        if(dataType != null) {
            decoder = DataDecoder.Factory.create(dataType);

            DecodeType decodeType = decoder.getClass().getAnnotation(DecodeType.class);
            if(decodeType != null) {
                typeClass = decodeType.value()[0];
            }
        }
    }

    public DataDecoder getDecoder() {
        return decoder;
    }

    public Class<?> getTypeClass() {
        return typeClass;
    }

    public List<Map.Entry<String,String>> getTypeParameters() {
        return parameters;
    }

    public void setDataTypeParameters(List<Map.Entry<String,String>> parameters) {
        this.parameters = parameters;

        if(decoder instanceof Configurable) {
            if(decoder == null) {
                throw new IllegalStateException("Illegal call to set parameters before 'dataType' has been configured on the " + getClass().getName());
            }

            decodeParams = new Properties();
            if(parameters != null) {
                for (Map.Entry<String,String> entry : parameters) {
                    decodeParams.setProperty(entry.getKey(), entry.getValue());
                }
            }
            ((Configurable)decoder).setConfiguration(decodeParams);
        }
    }

    public String getDataTypeParametersString() {
        if(parameters == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        for(Map.Entry<String,String> parameter : parameters) {
            if(builder.length() > 0) {
                builder.append(";");
            }
            builder.append(parameter.getKey());
            builder.append("=");
            builder.append(parameter.getValue());
        }

        return builder.toString();
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    public void isValidForType(String value) throws DataDecodeException {
        decoder.decode(value);
    }

    public boolean isRequired() {
return required != null && required;
}

    public void setRequired(Boolean value) {
this.required = value;
}

    public int getMaxOccurs() {
        if (maxOccurs == null) {
            return  1;
        } else {
            return maxOccurs;
        }
    }

    public void setMaxOccurs(Integer value) {
        this.maxOccurs = value;
    }
}

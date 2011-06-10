package org.milyn.edisax;

import org.milyn.config.Configurable;
import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DataDecoder;

import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Properties;

public enum EDITypeEnum {
    String("String", String.class),
	Numeric("Double", String.class),
	Decimal("Double", Double.class),
	Date("Date", Date.class),
	Time("Date", Date.class),
	Binary("Binary", String.class),
    Custom(null, null);

    public final static String CUSTOM_NAME = "Custom";

    private String typeAlias;
    private Class javaClass;

    EDITypeEnum(String typeAlias, Class javaClass) {
        this.typeAlias = typeAlias;
        this.javaClass = javaClass;
    }

    public String getTypeAlias() {
        return typeAlias;
    }

    public Class getJavaClass() {
        return javaClass;
    }

    public boolean validateType(String value, List<Map.Entry<String,String>> parameters) {
        DataDecoder decoder;

        // Get DataDecoder.
        if (parameters != null && parameters.size() > 0 &&  this.name().equals("Custom")) {
            decoder = DataDecoder.Factory.create(parameters.get(0).getValue());
        } else {
            decoder = DataDecoder.Factory.create(typeAlias);
        }

        // If DataDecoder implements Configurable set format.
        if (decoder instanceof Configurable && parameters != null) {
            Properties properties = getProperties(parameters);
            Configurable configurable = (Configurable)decoder;
            configurable.setConfiguration(properties);
        }
        try {
            decoder.decode(value);
        } catch (DataDecodeException e) {
            return false;
        }
        return true;
    }

    private Properties getProperties(List<Map.Entry<String, String>> parameters) {
        Properties properties = new Properties();
        for (Map.Entry<String,String> entry : parameters) {
            properties.setProperty(entry.getKey(), entry.getValue());
        }
        return properties;
    }

}

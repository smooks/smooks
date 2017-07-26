/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.ejc;

import org.milyn.config.Configurable;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.ContainerNode;
import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.MappingNode;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.model.internal.ValueNode;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.DataEncoder;
import org.milyn.javabean.decoders.DABigDecimalDecoder;
import org.milyn.javabean.pojogen.JClass;
import org.milyn.javabean.pojogen.JMethod;
import org.milyn.javabean.pojogen.JNamedType;
import org.milyn.javabean.pojogen.JType;
import org.milyn.smooks.edi.EDIWritable;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

/**
 * EDIWritable bean serialization class.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
class WriteMethod extends JMethod {

    private JClass jClass;
    private MappingNode mappingNode;
    private boolean appendFlush = false;
    private boolean trunacate;
    private DelimiterType terminatingDelimiter;

    WriteMethod(JClass jClass, MappingNode mappingNode) {
        super("write");
        addParameter(new JType(Writer.class), "writer");
        addParameter(new JType(Delimiters.class), "delimiters");
        getExceptions().add(new JType(IOException.class));
        jClass.getImplementTypes().add(new JType(EDIWritable.class));
        jClass.getMethods().add(this);
        this.jClass = jClass;
        this.mappingNode = mappingNode;
        this.trunacate = (mappingNode instanceof ContainerNode && ((ContainerNode)mappingNode).isTruncatable());

        if(trunacate) {
            jClass.getRawImports().add(new JType(StringWriter.class));
            jClass.getRawImports().add(new JType(List.class));
            jClass.getRawImports().add(new JType(ArrayList.class));
            jClass.getRawImports().add(new JType(EDIUtils.class));
            jClass.getRawImports().add(new JType(DelimiterType.class));
        }
    }

    public void writeObject(JNamedType property, DelimiterType delimiterType, BindingConfig bindingConfig, MappingNode mappingNode) {
        writeDelimiter(delimiterType);
        writeObject(property, bindingConfig, mappingNode);
    }

    public void writeFieldCollection(JNamedType property, DelimiterType delimiterType, BindingConfig bindingConfig, int maxOccurs) {
        boolean first = getBodyBuilder().length() == 0;
        appendToBody("\n        if(" + property.getName() + " != null && !" + property.getName() + ".isEmpty()) {");
        appendToBody("\n            for(" + property.getType().getGenericType().getSimpleName() + " item : " + property.getName() + ") {");
        if (first) {
            appendToBody("\n                if(!nodeTokens.isEmpty()) {");
            writeDelimiter(delimiterType, "\n                    ");
            appendToBody("\n                }");
        } else {
            writeDelimiter(delimiterType, "\n                ");
        }
        appendToBody("\n                item.write(nodeWriter, delimiters);");
        if(trunacate) {
            appendToBody("\n                nodeTokens.add(nodeWriter.toString());");
            appendToBody("\n                ((StringWriter)nodeWriter).getBuffer().setLength(0);");
        }
        appendToBody("\n            }");
        appendToBody("\n        }");
        if (first) {
            appendToBody("\n        for(int i = " + property.getName() + " == null ? 1 : " + property.getName() + ".size() + 1; i < " + maxOccurs + "; i++) {");
            writeDelimiter(delimiterType, "\n            ");
            appendToBody("\n        }");
        } else {
            appendToBody("\n        for(int i = " + property.getName() + " == null ? 0 : " + property.getName() + ".size(); i < " + maxOccurs + "; i++) {");
            writeDelimiter(delimiterType, "\n            ");
            appendToBody("\n        }");
        }
    }

    public void writeObject(JNamedType property, BindingConfig bindingConfig, MappingNode mappingNode) {
        appendToBody("\n        if(" + property.getName() + " != null) {");
        if(mappingNode instanceof Segment) {
            if(!((Segment) mappingNode).getFields().isEmpty() && (bindingConfig.getParent() == null || bodyLength() > 0)) {
                appendToBody("\n            nodeWriter.write(\"" + ((Segment)mappingNode).getSegcode() + "\");");
                appendToBody("\n            nodeWriter.write(delimiters.getField());");
            }
        }
        appendToBody("\n            " + property.getName() + ".write(nodeWriter, delimiters);");
        if(trunacate) {
            appendToBody("\n            nodeTokens.add(nodeWriter.toString());");
            appendToBody("\n            ((StringWriter)nodeWriter).getBuffer().setLength(0);");
        }
        appendToBody("\n        }");
    }

    public void writeValue(JNamedType property, ValueNode modelNode, DelimiterType delimiterType) {
        writeDelimiter(delimiterType);
        writeValue(property, modelNode);
    }

    public void writeValue(JNamedType property, ValueNode modelNode) {
        appendToBody("\n        if(" + property.getName() + " != null) {");

        DataDecoder dataDecoder = modelNode.getDecoder();
        if(dataDecoder instanceof DataEncoder) {
            String encoderName = property.getName() + "Encoder";
            Class<? extends DataDecoder> decoderClass = dataDecoder.getClass();

            // Add the property for the encoder instance...
            jClass.getProperties().add(new JNamedType(new JType(decoderClass), encoderName));

            // Create the encoder in the constructor...
            JMethod defaultConstructor = jClass.getDefaultConstructor();
            defaultConstructor.appendToBody("\n        " + encoderName + " = new " + decoderClass.getSimpleName() + "();");

            // Configure the encoder in the constructor (if needed)....
            if(dataDecoder instanceof Configurable) {
                Properties configuration = ((Configurable) dataDecoder).getConfiguration();

                if(configuration != null) {
                    Set<Map.Entry<Object, Object>> encoderConfig = configuration.entrySet();
                    String encoderPropertiesName = encoderName + "Properties";

                    jClass.getRawImports().add(new JType(Properties.class));
                    defaultConstructor.appendToBody("\n        Properties " + encoderPropertiesName + " = new Properties();");
                    for(Map.Entry<Object, Object> entry : encoderConfig) {
                        defaultConstructor.appendToBody("\n        " + encoderPropertiesName + ".setProperty(\"" + entry.getKey() + "\", \"" + entry.getValue() + "\");");
                    }
                    defaultConstructor.appendToBody("\n        " + encoderName + ".setConfiguration(" + encoderPropertiesName + ");");
                }
            }

            // Add the encoder encode instruction to te write method...
            if (decoderClass == DABigDecimalDecoder.class){
                appendToBody("\n            nodeWriter.write(delimiters.escape(" + encoderName + ".encode(" + property.getName() + ", delimiters)));");
            } else {
                appendToBody("\n            nodeWriter.write(delimiters.escape(" + encoderName + ".encode(" + property.getName() + ")));");
            }
        } else {
            appendToBody("\n            nodeWriter.write(delimiters.escape(" + property.getName() + ".toString()));");
        }

        if(trunacate) {
            appendToBody("\n            nodeTokens.add(nodeWriter.toString());");
            appendToBody("\n            ((StringWriter)nodeWriter).getBuffer().setLength(0);");
        }

        appendToBody("\n        }");
    }

    public void writeSegmentCollection(JNamedType property, SegmentGroup segmentGroup) {
        appendToBody("\n        if(" + property.getName() + " != null && !" + property.getName() + ".isEmpty()) {");
        appendToBody("\n            for(" + property.getType().getGenericType().getSimpleName() + " " + property.getName() + "Inst : " + property.getName() + ") {");

        if(segmentGroup instanceof Segment && !((Segment) segmentGroup).getFields().isEmpty()) {
            appendToBody("\n                nodeWriter.write(\"" + segmentGroup.getSegcode() + "\");");
            appendToBody("\n                nodeWriter.write(delimiters.getField());");
            if(trunacate) {
                appendToBody("\n                nodeTokens.add(nodeWriter.toString());");
                appendToBody("\n                ((StringWriter)nodeWriter).getBuffer().setLength(0);");
            }
        }

        appendToBody("\n                " + property.getName() + "Inst.write(nodeWriter, delimiters);");
        appendToBody("\n            }");
        appendToBody("\n        }");
    }

    @Override
    public String getBody() {
        StringBuilder builder = new StringBuilder();

        if(trunacate) {
            builder.append("\n        Writer nodeWriter = new StringWriter();\n");
            builder.append("\n        List<String> nodeTokens = new ArrayList<String>();\n");
        } else {
            builder.append("\n        Writer nodeWriter = writer;\n");
        }

        builder.append(super.getBody());

        if(trunacate) {
            builder.append("\n        nodeTokens.add(nodeWriter.toString());");
            if(mappingNode instanceof Segment) {
                builder.append("\n        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.FIELD, delimiters));");
            } else if(mappingNode instanceof Field) {
                builder.append("\n        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.COMPONENT, delimiters));");
            } else if(mappingNode instanceof Component) {
                builder.append("\n        writer.write(EDIUtils.concatAndTruncate(nodeTokens, DelimiterType.SUBCOMPONENT, delimiters));");
            }
        }

        if(terminatingDelimiter != null) {
            writeDelimiter(terminatingDelimiter, "writer", builder);
        }
        if(appendFlush) {
            builder.append("\n        writer.flush();");
        }

        return builder.toString();
    }

    public void writeDelimiter(DelimiterType delimiterType) {
        writeDelimiter(delimiterType, "\n        ");
    }

    public void writeDelimiter(DelimiterType delimiterType, String indent) {
        writeDelimiter(delimiterType, "nodeWriter", getBodyBuilder(), indent);
    }

    private void writeDelimiter(DelimiterType delimiterType, String writerVariableName, StringBuilder builder) {
        writeDelimiter(delimiterType, writerVariableName, builder, "\n        ");
    }

    private void writeDelimiter(DelimiterType delimiterType, String writerVariableName, StringBuilder builder, String indent) {
        if(bodyLength() == 0) {
            return;
        }

        switch (delimiterType) {
            case SEGMENT:
                builder.append(indent + writerVariableName + ".write(delimiters.getSegmentDelimiter());");
                break;
            case FIELD:
                builder.append(indent + writerVariableName + ".write(delimiters.getField());");
                break;
            case FIELD_REPEAT:
                builder.append(indent + writerVariableName + ".write(delimiters.getFieldRepeat());");
                break;
            case COMPONENT:
                builder.append(indent + writerVariableName + ".write(delimiters.getComponent());");
                break;
            case SUB_COMPONENT:
                builder.append(indent + writerVariableName + ".write(delimiters.getSubComponent());");
                break;
            case DECIMAL_SEPARATOR:
                builder.append(indent + writerVariableName + ".write(delimiters.getDecimalSeparator());");
                break;
            default:
                throw new UnsupportedOperationException("Unsupported '" + DelimiterType.class.getName() + "' enum conversion.  Enum '" + delimiterType + "' not specified in switch statement.");
        }
    }

    public void addFlush() {
        appendFlush = true;
    }

    public void addTerminatingDelimiter(DelimiterType delimiterType) {
        terminatingDelimiter = delimiterType;
    }
}

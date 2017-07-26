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
package org.milyn.ejc;

import org.apache.commons.logging.Log;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.DelimiterType;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.MappingNode;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.model.internal.SubComponent;
import org.milyn.edisax.model.internal.ValueNode;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.util.IllegalNameException;
import org.milyn.javabean.pojogen.JClass;
import org.milyn.javabean.pojogen.JMethod;
import org.milyn.javabean.pojogen.JNamedType;
import org.milyn.javabean.pojogen.JType;
import org.milyn.smooks.edi.EDIMessage;

import java.util.*;

/**
 * Compiles a {@link ClassModel} from an {@link Edimap}.
 *
 * @author bardl
 */
public class ClassModelCompiler {

    private static Log LOG = EJCLogFactory.getLog(ClassModelCompiler.class);

    private Map<MappingNode, JClass> injectedCommonTypes = new HashMap<MappingNode, JClass>();
    private Map<MappingNode, JClass> createdClassesByNode = new HashMap<MappingNode, JClass>();

    private ClassModel model;
    private Stack<String> nodeStack = new Stack<String>();
    private boolean addEDIMessageAnnotation;

    public ClassModelCompiler(Map<MappingNode, JClass> commonTypes, boolean addEDIMessageAnnotation) {
        if(commonTypes != null) {
            injectedCommonTypes.putAll(commonTypes);
        }
        this.addEDIMessageAnnotation = addEDIMessageAnnotation;
    }

    public ClassModel compile(Edimap edimap, String classPackage) throws IllegalNameException {
        model = new ClassModel();

        model.setEdimap(edimap);

        SegmentGroup segmentGroup = edimap.getSegments();

        pushNode(segmentGroup);

        JClass rootClass = new JClass(classPackage, EDIUtils.encodeClassName(segmentGroup.getJavaName()), getCurrentClassId()).setSerializable();
        BindingConfig rootBeanConfig = new BindingConfig(getCurrentClassId(), getCurrentNodePath(), rootClass, null, null);

        //Insert root class into classModel and its' corresponding xmltag-value.
        model.addCreatedClass(rootClass);
        model.setRootBeanConfig(rootBeanConfig);

        LOG.debug("Added root class [" + rootClass + "] to ClassModel.");

        addWriteMethod(rootBeanConfig);
        processSegmentGroups(segmentGroup.getSegments(), rootBeanConfig);

        LOG.debug("Finished parsing edi-configuration. All segments are added to ClassModel.");
        LOG.debug("ClassModel contains " + model.getCreatedClasses().size() + " classes.");

        // Attach the createdClassesByNode map... so we can use them if they
        // are common classes in a model set...
        model.setClassesByNode(createdClassesByNode);
        model.setReferencedClasses(injectedCommonTypes.values());

        popNode();

        if(addEDIMessageAnnotation) {
            model.getRootBeanConfig().getBeanClass().getAnnotationTypes().add(new JType(EDIMessage.class));
        }

        return model;
    }

    /**********************************************************************************************************
     * Private Methods
     **********************************************************************************************************/

    /**
     * Process all SegmentGroups in List and insert info into the {@link org.milyn.ejc.ClassModel}.
     * @param segmentGroups the SegmentsGroups to process.
     * @param parentBinding the JClass 'owning' the SegmentGroups.
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private void processSegmentGroups(List<SegmentGroup> segmentGroups, BindingConfig parentBinding) throws IllegalNameException {
        WriteMethod writeMethod = null;

        for (SegmentGroup segmentGroup : segmentGroups) {
            BindingConfig childBeanConfig = processSegmentGroup(segmentGroup, parentBinding);

            writeMethod = parentBinding.getWriteMethod();

            // Add Write Method details for the property just added...
            if(writeMethod != null) {
                if(isCollection(childBeanConfig.getPropertyOnParent())) {
                    writeMethod.writeSegmentCollection(childBeanConfig.getPropertyOnParent(), segmentGroup);
                } else {
                    writeMethod.writeObject(childBeanConfig.getPropertyOnParent(), parentBinding, segmentGroup);
                }
            }

            addWireBindings(parentBinding, childBeanConfig);
        }
    }

    private void addWireBindings(BindingConfig parentBinding, BindingConfig childBeanConfig) {
        if(isCollection(childBeanConfig.getPropertyOnParent())) {
            BindingConfig collectionBinding = new BindingConfig(childBeanConfig.getBeanId() + "_List", parentBinding.getCreateOnElement(), ArrayList.class, parentBinding, childBeanConfig.getPropertyOnParent());

            // Wire the List binding into the parent binding and wire the child binding into the list binding...
            parentBinding.getWireBindings().add(collectionBinding);
            collectionBinding.getWireBindings().add(childBeanConfig);

            // And zap the propertyOnParent config because you don't wire onto a property on a collection...
            childBeanConfig.setPropertyOnParent(null);
        } else {
            parentBinding.getWireBindings().add(childBeanConfig);
        }
    }

    /**
     *
     * Process the {@link org.milyn.edisax.model.internal.SegmentGroup} in List and insert info into the {@link org.milyn.ejc.ClassModel}.
     * @param segmentGroup the {@link org.milyn.edisax.model.internal.SegmentGroup} to process.
     * @param parent the JClass 'owning' the {@link org.milyn.edisax.model.internal.SegmentGroup}.
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private BindingConfig processSegmentGroup(SegmentGroup segmentGroup, BindingConfig parent) throws IllegalNameException {
        LOG.debug("Parsing SegmentGroup " + segmentGroup.getXmltag());

        if(segmentGroup.getJavaName() == null) {
            throw new EJCException("The <segmentGroup> element can optionally omit the 'xmltag' attribute.  However, this attribute must be present for EJC to work properly.  It is omitted from one of the <segmentGroup> elements in this configuration.");
        }

        pushNode(segmentGroup);

        BindingConfig segGroupBinding = createChildAndConnectWithParent(parent, segmentGroup, segmentGroup.getMaxOccurs(), null);

        if (segmentGroup instanceof Segment) {
            Segment segment = (Segment) segmentGroup;
            processFields(segment.getFields(), segGroupBinding);
        }

        processSegmentGroups(segmentGroup.getSegments(), segGroupBinding);

        popNode();

        return segGroupBinding;
    }

    /**
     * Process all {@link org.milyn.edisax.model.internal.Field} in List and insert info into the {@link org.milyn.ejc.ClassModel}.
     * @param fields the {@link org.milyn.edisax.model.internal.Field} to process.
     * @param parent the JClass 'owning' the {@link org.milyn.edisax.model.internal.Field}.
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private void processFields(List<Field> fields, BindingConfig parent) throws IllegalNameException {
        Set<String> names = new HashSet<String>();
        for (Field field : fields) {
            LOG.debug("Parsing field " + field.getXmltag());
            pushNode(field);

            boolean added = names.add(field.getXmltag());
            if (!added) {
                throw new EJCException("The <field> element can have the same 'xmltag' attribute. However, this attribute must unique for EJC to work properly. Current path: " + getCurrentNodePath());
            }

            if (field.getComponents() != null && field.getComponents().size() > 0) {
                //Add class type.
                BindingConfig childBinding = createChildAndConnectWithParent(parent, field, field.getMaxOccurs(), DelimiterType.FIELD);

                addWireBindings(parent, childBinding);

                // Now add the components to the field...
                processComponents(field.getComponents(), childBinding);
            } else {
                // Add primitive type.
                createAndAddSimpleType(field, parent, field.getMaxOccurs(), DelimiterType.FIELD);
            }

            popNode();
        }

        collapseSingleFieldSegmentBinding(parent);

        if(parent.getWriteMethod() != null) {
            parent.getWriteMethod().addTerminatingDelimiter(DelimiterType.SEGMENT);
            parent.getWriteMethod().addFlush();
        }
    }

    private void collapseSingleFieldSegmentBinding(BindingConfig parent) {
        if(parent.getValueBindings().isEmpty() && parent.getWireBindings().size() == 1) {
            BindingConfig child = parent.getWireBindings().get(0);
            if (parent.getBeanClass() != null && child.getBeanClass() != null) {
                String parentClassName = parent.getBeanClass().getSkeletonClass().getName();
                String childClassName = child.getBeanClass().getSkeletonClass().getName();

                if (parentClassName.equals(childClassName)) {
                    // This is a segment with just one field, having the same name
                    // as the segment itself.  Need to collapse the child
                    // up into the parent...
                    parent.setBeanClass(child.getBeanClass());
                    parent.setValueBindings(child.getValueBindings());
                    parent.setWireBindings(child.getWireBindings());
                }
            }
        }
    }

    /**
     * Creates a {@link org.milyn.javabean.pojogen.JNamedType} given a {@link org.milyn.edisax.model.internal.ValueNode}.
     * When {@link org.milyn.edisax.model.internal.ValueNode} contains no type information String-type is used as default.
     * The new {@link org.milyn.javabean.pojogen.JNamedType} is inserted into parent and the xmltag- and
     * typeParameters-value is inserted into classModel.
     * @param valueNode the {@link ValueNode} to process.
     * @param parent the {@link JClass} 'owning' the valueNode.
     * @param maxOccurs
     * @param delimiterType Node delimiter type.  @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private JNamedType createAndAddSimpleType(ValueNode valueNode, BindingConfig parent, int maxOccurs, DelimiterType delimiterType) throws IllegalNameException {
        JType jtype;
        JNamedType childToParentProperty;

        if (valueNode.getDataType() != null && !valueNode.getDataType().equals("")) {
            jtype = new JType(valueNode.getTypeClass());
        } else {
            // Default type when no specific type is given.
            jtype = new JType(String.class);
        }

        if (maxOccurs > 1 || maxOccurs == -1) {
            jtype = new JType(ArrayList.class, jtype.getType());
        }

        String propertyName = EDIUtils.encodeAttributeName(jtype, valueNode.getJavaName());
        childToParentProperty = new JNamedType(jtype, propertyName);

        JClass parentBeanClass = parent.getBeanClass();
        if(!parentBeanClass.isFinalized() && !parentBeanClass.hasProperty(propertyName) && model.isClassCreator(parentBeanClass)) {
            parentBeanClass.addBeanProperty(childToParentProperty);
            getWriteMethod(parent).writeValue(childToParentProperty, valueNode, delimiterType);
        }

        if (isCollection(childToParentProperty)) {
            String currentClassId = getCurrentClassId();
            BindingConfig collectionBinding = new BindingConfig(currentClassId + "_List", parent.getCreateOnElement(), ArrayList.class, parent, childToParentProperty);

            // Wire the List binding into the parent binding and wire the child binding into the list binding...
            parent.getWireBindings().add(collectionBinding);

            BindingConfig childBinding = new BindingConfig(currentClassId, getCurrentNodePath(), jtype.getGenericType(), collectionBinding, null);
            collectionBinding.getWireBindings().add(childBinding);
        } else {
            ValueNodeInfo childBinding = new ValueNodeInfo(childToParentProperty, getCurrentNodePath(), valueNode);
            parent.getValueBindings().add(childBinding);
        }

        return childToParentProperty;
    }

    /**
     * Process all {@link org.milyn.edisax.model.internal.Component} in List and insert info into the {@link org.milyn.ejc.ClassModel}.
     * @param components the {@link org.milyn.edisax.model.internal.Component} to process.
     * @param parent the JClass 'owning' the {@link org.milyn.edisax.model.internal.Component}.
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private void processComponents(List<Component> components, BindingConfig parent) throws IllegalNameException {
        Set<String> names = new HashSet<String>();
        for (Component component : components) {
            LOG.debug("Parsing component " + component.getXmltag());
            pushNode(component);

            boolean added = names.add(component.getXmltag());
            if (!added) {
                throw new EJCException("The <component> element can have the same 'xmltag' attribute. However, this attribute must unique for EJC to work properly. Current path: " + getCurrentNodePath());
            }

            if (component.getSubComponents() != null && component.getSubComponents().size() > 0) {
                //Add class type.
                BindingConfig childBeanConfig = createChildAndConnectWithParent(parent, component, component.getMaxOccurs(), DelimiterType.COMPONENT);

                addWireBindings(parent, childBeanConfig);

                processSubComponents(component.getSubComponents(), childBeanConfig);
            } else {
                //Add primitive type.
                createAndAddSimpleType(component, parent, component.getMaxOccurs(), DelimiterType.COMPONENT);
            }

            popNode();
        }
    }

    /**
     * Process all {@link org.milyn.edisax.model.internal.SubComponent} in List and insert info into the {@link org.milyn.ejc.ClassModel}.
     * @param subComponents the {@link org.milyn.edisax.model.internal.SubComponent} to process.
     * @param parent the JClass 'owning' the {@link org.milyn.edisax.model.internal.SubComponent}.
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private void processSubComponents(List<SubComponent> subComponents, BindingConfig parent) throws IllegalNameException {
        for (SubComponent subComponent : subComponents) {

            pushNode(subComponent);

            //Add primitive type.
            createAndAddSimpleType(subComponent, parent, 1, DelimiterType.SUB_COMPONENT);

            popNode();
        }
    }

    private void pushNode(MappingNode node) {
        nodeStack.push(node.getXmltag());
    }

    private void popNode() {
        nodeStack.pop();
    }

    /**
     * Creates a new {@link org.milyn.javabean.pojogen.JClass} C and inserts the class as a property in parent.
     * If C occurs several times, i.e. maxOccurs > 1, then C exists in a {@link java.util.List} in parent.
     * The new {@link org.milyn.javabean.pojogen.JClass} is inserted into classModel along with xmltag-value
     * found in the {@link org.milyn.edisax.model.internal.MappingNode}.
     * @param parentBinding The parentBinding BindingConfig.
     * @param mappingNode the {@link org.milyn.edisax.model.internal.MappingNode} to process.
     * @param maxOccurs the number of times {@link org.milyn.edisax.model.internal.MappingNode} can occur.
     * @param delimiterType
     * @return the created {@link org.milyn.javabean.pojogen.JClass}
     * @throws IllegalNameException when name found in a xmltag-attribute is a java keyword.
     */
    private BindingConfig createChildAndConnectWithParent(BindingConfig parentBinding, MappingNode mappingNode, int maxOccurs, DelimiterType delimiterType) throws IllegalNameException {
        JClass child = getCommonType(mappingNode);
        boolean addClassToModel = false;

        if(child == null) {
            String packageName = parentBinding.getBeanClass().getPackageName();
            String className = EDIUtils.encodeClassName(mappingNode.getJavaName());

            if(mappingNode instanceof Field) {
                packageName += ".field";
            } else if(mappingNode instanceof Component) {
                packageName += ".component";
            } else if(mappingNode instanceof SubComponent) {
                packageName += ".subcomponent";
            }

            child = new JClass(packageName, className, getCurrentClassId()).setSerializable();
            addClassToModel = true;
            LOG.debug("Created class " + child.getClassName() + ".");
        }

        JType jtype;
        if (maxOccurs > 1 || maxOccurs == -1) {
            jtype = new JType(List.class, child.getSkeletonClass());
        } else {
            jtype = new JType(child.getSkeletonClass());
        }

        String propertyName = EDIUtils.encodeAttributeName(jtype, mappingNode.getJavaName());
        JNamedType childProperty = new JNamedType(jtype, propertyName);

        BindingConfig childBeanConfig = new BindingConfig(getCurrentClassId(), getCurrentNodePath(), child, parentBinding, childProperty);
        childBeanConfig.setMappingNode(mappingNode);

        JClass parentBeanClass = parentBinding.getBeanClass();
        if(!parentBeanClass.isFinalized() && !parentBeanClass.hasProperty(propertyName) && model.isClassCreator(parentBeanClass)) {
            parentBeanClass.addBeanProperty(childProperty);
            if(delimiterType != null) {
                if (isCollection(childProperty)) {
                    getWriteMethod(parentBinding).writeFieldCollection(childProperty, delimiterType, parentBinding, maxOccurs);
                } else {
                    getWriteMethod(parentBinding).writeObject(childProperty, delimiterType, parentBinding, mappingNode);
                }
            }
        }
        if(addClassToModel) {
            model.addCreatedClass(child);
            createdClassesByNode.put(mappingNode, child);
            childBeanConfig.setWriteMethod(new WriteMethod(child, mappingNode));
        }

        return childBeanConfig;
    }

    private String getCurrentClassId() {
        return getCurrentNodePath().replace('/', '.');
    }

    private String getCurrentNodePath() {
        StringBuilder builder = new StringBuilder();

        for(String nodePathElement : nodeStack) {
            if(builder.length() > 0) {
                builder.append('/');
            }
            builder.append(nodePathElement);
        }

        return builder.toString();
    }

    private JClass getCommonType(MappingNode mappingNode) {
        String nodeTypeRef = mappingNode.getNodeTypeRef();

        if(nodeTypeRef != null) {
            int colonIndex = nodeTypeRef.indexOf(':');

            if(colonIndex != -1) {
                nodeTypeRef = nodeTypeRef.substring(colonIndex + 1);
            }

            JClass commonType = getCommonType(mappingNode, nodeTypeRef, createdClassesByNode);
            if(commonType == null) {
                commonType = getCommonType(mappingNode, nodeTypeRef, injectedCommonTypes);
            }
            return commonType;
        } else {
            JClass commonType = createdClassesByNode.get(mappingNode);
            if(commonType == null) {
                commonType = injectedCommonTypes.get(mappingNode);
            }
            return commonType;
        }
    }

    private JClass getCommonType(MappingNode mappingNode, String nodeTypeRef, Map<MappingNode, JClass> typeSet) {
        Set<Map.Entry<MappingNode, JClass>> commonTypes = typeSet.entrySet();

        for(Map.Entry<MappingNode, JClass> typeEntry : commonTypes) {
            MappingNode entryMappingNode = typeEntry.getKey();
            String entryNodeTypeRef = entryMappingNode.getNodeTypeRef();

            if(entryMappingNode instanceof Segment) {
                if(nodeTypeRef.equals(((Segment)entryMappingNode).getSegcode())) {
                    return typeEntry.getValue();
                }
            } else if(entryNodeTypeRef != null && entryMappingNode.getClass() == mappingNode.getClass()) {
                // Must be the same node type exactly...
                if(nodeTypeRef.equals(entryNodeTypeRef)) {
                    return typeEntry.getValue();
                }
            }
        }
        return null;
    }

    /**********************************************************************************************************
     * Private Helper Methods
     **********************************************************************************************************/

    private WriteMethod getWriteMethod(BindingConfig bindingConfig) {
        for(JMethod method : bindingConfig.getBeanClass().getMethods()) {
            if(method instanceof WriteMethod) {
                return (WriteMethod) method;
            }
        }

        JClass beanClass = bindingConfig.getBeanClass();
        WriteMethod writeMethod = new WriteMethod(beanClass, bindingConfig.getMappingNode());

        bindingConfig.setWriteMethod(writeMethod);

        return writeMethod;
    }

    private WriteMethod addWriteMethod(BindingConfig bindingConfig) {
        return getWriteMethod(bindingConfig);
    }

    private static boolean isCollection(JNamedType property) {
        if(property != null && Collection.class.isAssignableFrom(property.getType().getType())) {
            return true;
        }

        return false;
    }
}

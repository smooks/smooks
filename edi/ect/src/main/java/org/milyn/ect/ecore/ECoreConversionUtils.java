package org.milyn.ect.ecore;

import static org.milyn.ect.ecore.SmooksMetadata.ANNOTATION_TYPE_KEY;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EModelElement;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.ExtendedMetaData;
import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.MappingNode;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.milyn.edisax.model.internal.ValueNode;

/**
 * Utility class that convert EDI model to ECore model elements
 * 
 * @author zubairov
 * 
 */
public class ECoreConversionUtils {

	/**
	 * Supported data types for conversion
	 * 
	 */
	private static final EDataType ETYPES[] = { XMLTypePackage.Literals.STRING,
			XMLTypePackage.Literals.LONG, XMLTypePackage.Literals.DECIMAL,
			XMLTypePackage.Literals.FLOAT };

	private static ExtendedMetaData metadata = ExtendedMetaData.INSTANCE;
	
	private static final Log log = LogFactory.getLog(ECoreConversionUtils.class);

	/**
	 * Converting {@link Segment} to {@link EClass}
	 * 
	 * @param segment
	 * @return
	 */
	public static EClass segmentToEClass(Segment segment) {
		EClass clazz = segmentGroupToEClass(segment);
		annotate(clazz, SmooksMetadata.SEGCODE, segment.getSegcode());
		annotate(clazz, "segcodePattern", segment.getSegcodePattern()
				.toString());
		annotate(clazz, "truncable", String.valueOf(segment.isTruncatable()));
		annotate(clazz, "ignoreUnmappedFields",
				String.valueOf(segment.isIgnoreUnmappedFields()));
		annotate(clazz, "description", segment.getDescription());
		annotate(clazz, SmooksMetadata.ANNOTATION_TYPE_KEY,
				SmooksMetadata.SEGMENT_TYPE);
		return clazz;
	}

	/**
	 * This method transforms {@link Edimap} to {@link EPackage} where classes
	 * related to this {@link Edimap} will be stored
	 * 
	 * @param mapModel
	 * @return
	 */
	public static EPackage mappingModelToEPackage(Edimap mapModel) {
		final EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		Description desc = mapModel.getDescription();
		pkg.setName(desc.getName().toLowerCase());
		pkg.setNsPrefix(desc.getName().toLowerCase());
		pkg.setNsURI(desc.getNamespace());
		if (mapModel.getSrc() != null) {
			annotate(pkg, "src", mapModel.getSrc().toASCIIString());
		}
		annotate(pkg, "description.name", mapModel.getDescription().getName());
		annotate(pkg, "description.version", mapModel.getDescription()
				.getVersion());
		annotate(pkg, "delimeters.segment", mapModel.getDelimiters()
				.getSegment());
		annotate(pkg, "delimeters.component", mapModel.getDelimiters()
				.getComponent());
		annotate(pkg, "delimeters.field", mapModel.getDelimiters().getField());
		annotate(pkg, "delimeters.fieldRepeat", mapModel.getDelimiters()
				.getFieldRepeat());
		annotate(pkg, "delimeters.escape", mapModel.getDelimiters().getEscape());
		annotate(pkg, "delimeters.ignoreCLRF",
				String.valueOf(mapModel.getDelimiters().ignoreCRLF()));
		return pkg;
	}

	/**
	 * Converts {@link Segment} to {@link EReference}
	 * 
	 * @param segment
	 * @param refClass
	 * @return
	 */
	public static EReference segmentToEReference(Segment segment,
			EClass refClass) {
		EReference reference = segmentGroupToEReference(segment, refClass);
		annotate(reference, SmooksMetadata.ANNOTATION_TYPE_KEY,
				SmooksMetadata.SEGMENT_TYPE);
		annotate(reference, SmooksMetadata.SEGCODE, segment.getSegcode());
		String name = segment.getSegcode();
		char lastChar = reference.getName().charAt(reference.getName().length() - 1);
		if (Character.isDigit(lastChar)) {
			name = name + lastChar;
		}
		reference.setName(name);
		return reference;
	}

	/**
	 * Converting {@link SegmentGroup} to {@link EClass}
	 * 
	 * @param grp
	 * @return
	 */
	public static EClass segmentGroupToEClass(SegmentGroup grp) {
		EClass clazz = EcoreFactory.eINSTANCE.createEClass();
		clazz.setName(toJavaName(grp.getXmltag(), true));
		addMappingInformation(clazz, grp);
		return clazz;
	}

	private static void addMappingInformation(EClass clazz, MappingNode node) {
		if (node.getDocumentation() != null) {
			annotate(clazz, "documentation", node.getDocumentation());
		}
		metadata.setName(clazz, clazz.getName());
		metadata.setContentKind(clazz, ExtendedMetaData.ELEMENT_ONLY_CONTENT);
	}

	private static void addMappingInformation(EStructuralFeature ref,
			MappingNode node) {
		metadata.setName(ref, node.getXmltag());
		metadata.setFeatureKind(ref, ExtendedMetaData.ELEMENT_FEATURE);
		setTargetNamespace(ref);
	}

	/**
	 * Annotate given {@link EModelElement} with smooks anntation with given key
	 * and value
	 * 
	 * @param element
	 * @param key
	 * @param value
	 */
	private static void annotate(EModelElement element, String key, String value) {
		if (!StringUtils.isEmpty(value)) {
			EAnnotation annotation = element
					.getEAnnotation(SmooksMetadata.ANNOTATION_TYPE);
			if (annotation == null) {
				annotation = EcoreFactory.eINSTANCE.createEAnnotation();
				annotation.setSource(SmooksMetadata.ANNOTATION_TYPE);
				element.getEAnnotations().add(annotation);
			}
			annotation.getDetails().put(key, value);
		}
	}

	/**
	 * Convert {@link SegmentGroup} into {@link EReference} to the given
	 * {@link EClass}
	 * 
	 * @param grp
	 * @param refClass
	 * @return
	 */
	public static EReference segmentGroupToEReference(SegmentGroup grp,
			EClass refClass) {
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setContainment(true);
		reference.setName(toJavaName(grp.getXmltag(), false));
		reference.setEType(refClass);
		reference.setLowerBound(grp.getMinOccurs());
		reference.setUpperBound(grp.getMaxOccurs());
		addMappingInformation(reference, grp);
		annotate(reference, "minOccurs", String.valueOf(grp.getMinOccurs()));
		annotate(reference, "maxOccurs", String.valueOf(grp.getMaxOccurs()));
		annotate(reference, SmooksMetadata.ANNOTATION_TYPE_KEY,
				SmooksMetadata.SEGMENT_GROUP_TYPE);
		annotate(reference, SmooksMetadata.SEGCODE, grp.getSegcode());
		return reference;
	}

	/**
	 * Converting a {@link Field} to {@link EAttribute} Works only for
	 * {@link Field} where {@link Field#getComponents()} is empty
	 * 
	 * @param field
	 * @return
	 */
	public static EAttribute fieldToEAttribute(Field field) {
		if (!field.getComponents().isEmpty()) {
			throw new IllegalArgumentException(
					"Can't convert field with components to "
							+ "EAttribute, use fieldToEReference");
		}
		EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
		attr.setName(toJavaName(field.getXmltag(), false));
		attr.setLowerBound(field.isRequired() ? 1 : 0);
		attr.setUpperBound(1);
		if (field.getTypeClass() != null) {
			attr.setEType(toEType(field.getTypeClass()));
		} else {
			log.warn("Field " + field.getXmltag()
					+ " has no type! Setting it's type to String");
			attr.setEType(XMLTypePackage.Literals.STRING);
		}
		addMappingInformation(attr, field);
		annotateField(field, attr);
		return attr;
	}

	/**
	 * Add field specific annotations
	 * 
	 * @param field
	 * @param attr
	 */
	private static void annotateField(Field field, EModelElement attr) {
		annotate(attr, "truncable", String.valueOf(field.isTruncatable()));
		annotate(attr, "required", String.valueOf(field.isRequired()));
		annotate(attr, SmooksMetadata.ANNOTATION_TYPE_KEY,
				SmooksMetadata.FIELD_TYPE);
		annotateValueNode(attr, field);
	}

	/**
	 * This method creates a new {@link EReference} to the {@link Field} that
	 * contains multiple {@link Component}.
	 * 
	 * For that purpose new {@link EClass} will be created and
	 * {@link EReference} will refer to it
	 * 
	 * @param field
	 * @param classes
	 * @return
	 */
	public static EReference fieldToEReference(Field field,
			Map<String, EClass> classes) {
		EClass newClass = fieldToEClass(field);
		if (!classes.containsKey(newClass.getName())) {
			classes.put(newClass.getName(), newClass);
		} else {
			newClass = classes.get(newClass.getName());
		}
		for (Component component : field.getComponents()) {
			EStructuralFeature attribute = componentToEAttribute(component);
			if (newClass.getEStructuralFeature(attribute.getName()) == null) {
				newClass.getEStructuralFeatures().add(attribute);
			}
		}
		EReference result = EcoreFactory.eINSTANCE.createEReference();
		result.setContainment(true);
		result.setName(toJavaName(field.getXmltag(), false));
		result.setLowerBound(field.isRequired() ? 1 : 0);
		result.setUpperBound(1);
		result.setEType(newClass);
		annotateField(field, result);
		addMappingInformation(result, field);
		return result;
	}

	/**
	 * Converts {@link Component} to {@link EAttribute}
	 * 
	 * @param component
	 * @return
	 */
	private static EStructuralFeature componentToEAttribute(Component component) {
		if (!component.getSubComponents().isEmpty()) {
			throw new IllegalArgumentException(
					"Sub-components are not supported yet for component "
							+ component.getXmltag());
		}
		EAttribute result = EcoreFactory.eINSTANCE.createEAttribute();
		result.setName(toJavaName(component.getXmltag(), false));
		result.setLowerBound(component.isRequired() ? 1 : 0);
		result.setUpperBound(1);
		result.setEType(toEType(component.getTypeClass()));
		annotate(result, "truncable", String.valueOf(component.isTruncatable()));
		annotate(result, "required", String.valueOf(component.isRequired()));
		annotate(result, ANNOTATION_TYPE_KEY, SmooksMetadata.COMPONENT_TYPE);
		annotateValueNode(result, component);
		addMappingInformation(result, component);
		return result;
	}

	private static EClassifier toEType(Class<?> typeClass) {
		if (typeClass == null) {
			typeClass = String.class;
		}
		for (EDataType type : ETYPES) {
			if (type.getInstanceClass() == typeClass) {
				return type;
			}
		}
		throw new IllegalArgumentException("Type for type class " + typeClass
				+ " is not supported");
	}

	/**
	 * Creating a new {@link EClass} based on the information from {@link Field}
	 * used in case we have a complex {@link Field} and we need to create a
	 * class for it.
	 * 
	 * @param field
	 * @return
	 */
	private static EClass fieldToEClass(Field field) {
		String classifierName = toJavaName(field.getXmltag(), true);
		if (field.getNodeTypeRef() != null) {
			classifierName += "_" + field.getNodeTypeRef();
		}
		EClass newClass = EcoreFactory.eINSTANCE.createEClass();
		newClass.setName(classifierName);
		addMappingInformation(newClass, field);
		annotate(newClass, ANNOTATION_TYPE_KEY, SmooksMetadata.FIELD_TYPE);
		annotateValueNode(newClass, field);
		return newClass;
	}

	private static void annotateValueNode(EModelElement element,
			ValueNode valueNode) {
		annotate(element, "datatype", valueNode.getDataType());
		annotate(element, "maxLength", String.valueOf(valueNode.getMaxLength()));
		annotate(element, "minLength", String.valueOf(valueNode.getMinLength()));
		if (valueNode.getDecoder() != null) {
			annotate(element, "decoder", valueNode.getDecoder().getClass()
					.getCanonicalName());
		} else {
			annotate(element, "decoder", "");
		}
	}

	/**
	 * Convert tricky names to JavaNames with CamelCase etc
	 * 
	 * @param name
	 * @return
	 */
	public static String toJavaName(String name, boolean className) {
		name = name.replaceAll("__", "_");
		StringBuilder result = new StringBuilder();
		boolean cap = className;
		for (int i = 0; i < name.length(); i++) {
			char ch = name.charAt(i);
			if ('_' == ch || '.' == ch) {
				cap = true;
			} else {
				if (cap) {
					result.append(Character.toUpperCase(ch));
					cap = false;
				} else {
					result.append(Character.toLowerCase(ch));
				}
			}
		}
		return result.toString();
	}

	/**
	 * Creates a droot root class
	 * 
	 * @param rootClass
	 * @return
	 */
	public static EClass createDocumentRoot(EClass rootClass) {
		EClass clazz = EcoreFactory.eINSTANCE.createEClass();
		clazz.setName("DocumentRoot");
		metadata.setDocumentRoot(clazz);
		if (rootClass != null) {
			EReference reference = EcoreFactory.eINSTANCE.createEReference();
			clazz.getEStructuralFeatures().add(reference);
			reference.setEType(rootClass);
			reference.setName(metadata.getName(rootClass));
			metadata.setFeatureKind(reference, ExtendedMetaData.ELEMENT_FEATURE);
			setTargetNamespace(reference);
			reference.setContainment(true);
		}
		return clazz;
	}

	private static void setTargetNamespace(EStructuralFeature element) {
		EAnnotation eAnnotation = element
				.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
		eAnnotation.getDetails().put("namespace", "##targetNamespace");
	}
}

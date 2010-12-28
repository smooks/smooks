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
package org.milyn.routing.basic;

import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.*;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;
import org.milyn.javabean.repository.BeanRepository;
import org.milyn.util.CollectionsUtil;
import org.milyn.xml.XmlUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;
import org.milyn.cdr.annotation.ConfigParam;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

/**
 * Basic message fragment serializer.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class FragmentSerializer implements SAXVisitBefore, SAXVisitAfter, DOMVisitBefore, DOMVisitAfter, Producer, VisitLifecycleCleanable {

    private String bindTo;
    private boolean omitXMLDeclaration;
	private boolean childContentOnly;
    private boolean retain;
    
    /**
     * Set the bind-to beanId for the serialized fragment.
	 * @param bindTo The bind-to beanId for the serialized fragment.
	 * @return this instance.
	 */
    @ConfigParam
	public FragmentSerializer setBindTo(String bindTo) {
		this.bindTo = bindTo;
		return this;
	}
    
    /**
     * Omit the XML Declaration from the serialized fragments.
	 * @param omitXMLDeclaration True if the XML declaration is to be omitted, otherwise false.
	 * @return this instance.
	 */
    @ConfigParam(defaultVal = "false")
	public FragmentSerializer setOmitXMLDeclaration(boolean omitXMLDeclaration) {
		this.omitXMLDeclaration = omitXMLDeclaration;
		return this;
	}

    /**
     * Set whether or not the child content only should be serialized.
     * <p/>
     * This variable is, by default, false.
     * 
	 * @param childContentOnly True if the child content only (exclude 
	 * the targeted element itself), otherwise false.
	 * @return this instance.
	 */
    @ConfigParam(defaultVal = "false")
	public FragmentSerializer setChildContentOnly(boolean childContentOnly) {
		this.childContentOnly = childContentOnly;
		return this;
	}

    /**
     * Retain the fragment bean in the {@link BeanContext} after it's creating fragment
     * has been processed.
     *
	 * @param retain True if the fragment bean is to be retained in the {@link org.milyn.javabean.context.BeanContext},
     * otherwise false.
	 * @return this instance.
	 */
    @ConfigParam(defaultVal = "false")
    public FragmentSerializer setRetain(boolean retain) {
        this.retain = retain;
        return this;
    }

    public Set<? extends Object> getProducts() {
		return CollectionsUtil.toSet(bindTo);
	}

	@SuppressWarnings("unchecked")
	public void visitBefore(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException, IOException {
    	Map<String, SAXSerializer> fragmentSerializers = (Map<String, SAXSerializer>) executionContext.getAttribute(FragmentSerializer.class);
    	
    	if(fragmentSerializers == null) {
    		fragmentSerializers = new HashMap<String, SAXSerializer>();
        	executionContext.setAttribute(FragmentSerializer.class, fragmentSerializers);
    	}
    	
    	SAXSerializer serializer = new SAXSerializer();
    	fragmentSerializers.put(bindTo, serializer);
    	
        if(!omitXMLDeclaration) {
        	serializer.fragmentWriter.write("<?xml version=\"1.0\"?>\n");
        }
    	
    	// Now add a dynamic visitor...
        DynamicSAXElementVisitorList.addDynamicVisitor(serializer, executionContext);

        notifyStartBean(new Fragment(saxElement), executionContext);
    }

    @SuppressWarnings("unchecked")
	public void visitAfter(SAXElement saxElement, ExecutionContext executionContext) throws SmooksException, IOException {
    	Map<String, SAXSerializer> fragmentSerializers = (Map<String, SAXSerializer>) executionContext.getAttribute(FragmentSerializer.class);
    	SAXSerializer serializer = fragmentSerializers.get(bindTo);

    	try {
    		executionContext.getBeanContext().addBean(bindTo, serializer.fragmentWriter.toString().trim(), new Fragment(saxElement));
    	} finally {
            DynamicSAXElementVisitorList.removeDynamicVisitor(serializer, executionContext);
    	}
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        notifyStartBean(new Fragment(element), executionContext);
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
		String serializedFragment;

        if(childContentOnly) {
        	serializedFragment = XmlUtil.serialize(element.getChildNodes(), false);
        } else {
        	serializedFragment = XmlUtil.serialize(element, true);
        }

        if(!omitXMLDeclaration) {
        	serializedFragment = "<?xml version=\"1.0\"?>\n" + serializedFragment;
        }

        executionContext.getBeanContext().addBean(bindTo, serializedFragment, new Fragment(element));
	}

    private void notifyStartBean(Fragment source, ExecutionContext executionContext) {
        BeanContext beanContext = executionContext.getBeanContext();

        beanContext.notifyObservers(new BeanContextLifecycleEvent(executionContext,
                source, BeanLifecycle.START_FRAGMENT, beanContext.getBeanId(bindTo), ""));
    }

    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        BeanContext beanContext = executionContext.getBeanContext();
        BeanId beanId = beanContext.getBeanId(bindTo);
        Object bean = beanContext.getBean(beanId);

        beanContext.notifyObservers(new BeanContextLifecycleEvent(executionContext, fragment, BeanLifecycle.END_FRAGMENT, beanId, bean));
        if(!retain) {
            executionContext.getBeanContext().removeBean(beanId, null);
        }
    }

    private class SAXSerializer implements SAXElementVisitor {
		
    	int depth = 0;
    	StringWriter fragmentWriter = new StringWriter();
    	Set<String> declaredPrefixes = new HashSet<String>();

		public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
			addNamepsaceDecls(element);			
	        if(childContentOnly) {
	        	// Print child content only, so only print the start if the depth is greater
	        	// than 1...
	        	if(depth > 0) {
	        		SAXElementWriterUtil.writeStartElement(element, fragmentWriter, true);
	        	}
	        } else {
	        	// Printing all of the element, so just print the start element...
	        	SAXElementWriterUtil.writeStartElement(element, fragmentWriter, true);
	        }
	        depth++;
		}

		public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
	    	// The child element will look after itself.
	    }
	    
		public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
	    	SAXElementWriterUtil.writeText(text, fragmentWriter);
	    }

		public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
	        depth--;
	        if(childContentOnly) {
	        	// Print child content only, so only print the empty element if the depth is greater
	        	// than 1...
	        	if(depth > 0) {
	        		SAXElementWriterUtil.writeEndElement(element, fragmentWriter);
	        	}
	        } else {
	        	// Printing all of the elements, so just print the end of the element...
	        	SAXElementWriterUtil.writeEndElement(element, fragmentWriter);
	        }
		}		

		private void addNamepsaceDecls(SAXElement element) {
			// This code is here to ensure that, for SAX, we add namespace declarations to the 
			// serialized fragments.
			
			// Check the attribute namespaces...
			addAttrNamespaceDecls(element);
			
			// Check that the element namespace has been declared..
			QName qName = element.getName();
			addNamespace(qName.getPrefix(), qName.getNamespaceURI(), element);
		}

		private void addAttrNamespaceDecls(SAXElement element) {
			Attributes attributes = element.getAttributes();
			int attrCount = attributes.getLength();
			
			for (int i = 0; i < attrCount; i++) {
				String uri = attributes.getURI(i);

				if(uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
					declaredPrefixes.add(attributes.getLocalName(i));					
				} else {					
					String qName = attributes.getQName(i);				
					if(qName != null) {
						int indexOf = qName.indexOf(':');
						if(indexOf != -1) {
							String prefix = qName.substring(0, indexOf);
							addNamespace(prefix, uri, element);
						}
					}
				}
				
			}
		}

		private void addNamespace(String prefix, String namespaceURI, SAXElement element) {
            if (prefix == null || namespaceURI == null) {
                // No namespace.  Ignore...
                return;
            } else  if(prefix.equals(XMLConstants.DEFAULT_NS_PREFIX) && namespaceURI.equals(XMLConstants.NULL_NS_URI)) {
                // No namespace.  Ignore...
                return;
            } else if(declaredPrefixes.contains(prefix)) {
				// Already declared (earlier)...
				return;
			} else {
				String prefixNS = element.getAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix);
				if(prefixNS != null && prefixNS.length() != 0) {
					// Already declared (on the element)...
					return;
				}
			}
			
			Attributes attributes = element.getAttributes();
	        AttributesImpl attributesCopy = new AttributesImpl();
	        attributesCopy.setAttributes(attributes);
	        
	        if(prefix.length() > 0) {
	        	attributesCopy.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, "xmlns:" + prefix, null, namespaceURI);
	        } else {
	        	attributesCopy.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, "xmlns", null, namespaceURI);
	        }
	        element.setAttributes(attributesCopy);
	        declaredPrefixes.add(prefix);
		}
	}
}

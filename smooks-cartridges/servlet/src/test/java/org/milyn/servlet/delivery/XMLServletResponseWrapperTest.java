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

package org.milyn.servlet.delivery;

import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.delivery.dom.MockContentDeliveryConfig;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.dom.DOMVisitBefore;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.xml.DomUtils;
import org.milyn.servlet.http.HeaderAction;
import org.w3c.dom.Element;

import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockServletOutputStream;

import junit.framework.TestCase;

public class XMLServletResponseWrapperTest extends TestCase {

	private MockExecutionContext mockCR;
	private MockHttpServletResponse mockSR;
	
	protected void setUp() throws Exception {
		mockCR = new MockExecutionContext();
		mockSR = new MockHttpServletResponse() {
			public String getCharacterEncoding() {
				return "UTF-8";
			}
			public void setIntHeader(String arg0, int arg1) {
			}
		};
	}

	/*
	 * Test method for 'org.milyn.delivery.response.XMLServletResponseWrapper.XMLServletResponseWrapper(ExecutionContext, HttpServletResponse)'
	 */
	public void test_initHeaderActions() {
		// Make sure it constructs without configured header actions
		new XMLServletResponseWrapper(mockCR, mockSR);
		
		// Set the header actions.
		addHeaderAction("add", "header-x", "value-x", (MockContentDeliveryConfig) mockCR.deliveryConfig);
		addHeaderAction("remove", "header-y", "value-y", (MockContentDeliveryConfig) mockCR.deliveryConfig);
		
		// Now, construct it again - with header actions
		new XMLServletResponseWrapper(mockCR, mockSR);
	}

	public void test_deliverResponse_OutputStream() {
		addHeaderAction("add", "header-x", "value-x", (MockContentDeliveryConfig) mockCR.deliveryConfig);
		addHeaderAction("remove", "header-y", "value-y", (MockContentDeliveryConfig) mockCR.deliveryConfig);
		addProcessingUnit("W", new MyTestTU("x", true), (MockContentDeliveryConfig) mockCR.deliveryConfig);
		addProcessingUnit("Y", new MyTestTU("z", false), (MockContentDeliveryConfig) mockCR.deliveryConfig);

		XMLServletResponseWrapper wrapper = new XMLServletResponseWrapper(mockCR, mockSR);
		MockServletOutputStream mockOS = new MockServletOutputStream();

		try {
			mockSR.setContentType("text/html; charset=ISO-88591");
			mockSR.setupOutputStream(mockOS);
			
			ServletOutputStream os = wrapper.getOutputStream();
			os.print("<W>");
			os.write("sometext ".getBytes());
			os.print(1);
			os.print(" ");
			os.print(true);
			os.print(" ");
			os.print('c');
			os.print(" ");
			os = wrapper.getOutputStream();
			os.print(1.1);
			os.print(" ");
			os.print(10L);
			os.print(" ");
			os.print("<y/></W>");
			os.close();
			
			wrapper.deliverResponse();
			wrapper.close();
			
			// expect "w" elements to be renamed to "x" and "y" elements to be renamed to "z". 
			assertEquals("Wrong SmooksDOMFilter delivery response.", "<x>sometext 1 true c 1.1 10 <z></z></x>", mockOS.getContents());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void test_deliverResponse_PrintWriter() {
		addHeaderAction("add", "Content-Type", "text/html", (MockContentDeliveryConfig) mockCR.deliveryConfig);
		addHeaderAction("remove", "Content-Length", "100", (MockContentDeliveryConfig) mockCR.deliveryConfig);

		XMLServletResponseWrapper wrapper = new XMLServletResponseWrapper(mockCR, mockSR);
		MockServletOutputStream mockOS = new MockServletOutputStream();

		try {
			mockSR.setContentType("text/html; charset=ISO-88591");
			mockSR.setupOutputStream(mockOS);
			
			PrintWriter pw = wrapper.getWriter();
			pw.write("<x>".toCharArray());
			pw.write("sometext".toCharArray(), 0, 8);
			pw.write("<a/>");
			pw = wrapper.getWriter();
			pw.write("</x>", 0, 4);
			
			wrapper.deliverResponse();
			wrapper.close();
			
			assertEquals("Wrong SmooksDOMFilter delivery response.", "<x>sometext<a></a></x>", mockOS.getContents());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class MyTestTU implements DOMElementVisitor {
		private String newName;
		private boolean visitBefore;
		public MyTestTU(String newName, boolean visitBefore) {
			this.newName = newName;
			this.visitBefore = visitBefore;
		}
        public void visitBefore(Element element, ExecutionContext executionContext) {
            if(visitBefore) {
                DomUtils.renameElement(element, newName, true, true);
            }
        }
		public void visitAfter(Element element, ExecutionContext executionContext) {
            if(!visitBefore) {
                DomUtils.renameElement(element, newName, true, true);
            }
        }
        public void setConfiguration(SmooksResourceConfiguration resourceConfig) throws SmooksConfigurationException {
        }
    }

    private void addHeaderAction(String action, String headerName, String headerValue, MockContentDeliveryConfig deliveryConfig) {
        SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration("X", "X", "X");
        
        resourceConfig.setParameter("action", action);
        resourceConfig.setParameter("header-name", headerName);
        resourceConfig.setParameter("header-value", headerValue);
        
        deliveryConfig.addObject("http-response-header", Configurator.configure(new HeaderAction(), resourceConfig));
    }

    private void addProcessingUnit(String targetElement, DOMElementVisitor processingUnit, MockContentDeliveryConfig deliveryConfig) {
        // Ignoring assembly units for now!!

        if(processingUnit instanceof DOMVisitBefore) {
            deliveryConfig.processingBefores.addMapping(targetElement, new SmooksResourceConfiguration(targetElement, processingUnit.getClass().getName()), processingUnit);
        }
        if(processingUnit instanceof DOMVisitAfter) {
            deliveryConfig.processingAfters.addMapping(targetElement, new SmooksResourceConfiguration(targetElement, processingUnit.getClass().getName()), processingUnit);
        }
    }
}

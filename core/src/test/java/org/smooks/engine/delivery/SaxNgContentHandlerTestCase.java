/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.engine.delivery;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.io.Sink;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.engine.delivery.sax.ng.SaxNgContentHandler;
import org.smooks.io.source.StreamSource;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.smooks.engine.delivery.SaxNgContentHandlerTestCase.SAXMatchers.isSaxElementWithQName;
import static org.smooks.engine.delivery.SaxNgContentHandlerTestCase.SAXMatchers.isSaxFragmentWithQName;

/**
 * Test for {@link SaxNgContentHandler}.
 *
 * @author Michael Kr&uuml;ske
 */
public class SaxNgContentHandlerTestCase {
    private static final String SIMPLE_SAMPLE_XML = "SAXHandlerTest.xml";

    private static final String URN_SIMPLE = "urn:simple";
    private static final String URN_FIRST = "urn:first";
    private static final String URN_SECOND = "urn:second";

    private static final QName QNAME_FOR_FIRST_SAMPLE = new QName(URN_FIRST, "sample");
    private static final QName QNAME_FOR_SECOND_SAMPLE = new QName(URN_SECOND, "sample");

    /**
     * Parse a simple XML file containing two tags only distinguished by their namespaces.
     *
     * This test checks that the {@see PostFragmentLifecycle#onPostFragment(Fragment, ExecutionContext)} are only
     * called for elements matching the full qualified tag name, including the namespace.
     * Additionally, it will also ensure this for {@see SAXVisitBefore#visitBefore(SAXElement, ExecutionContext)}.
     *
     * Test for MILYN-648 Only execute clean-up handlers targeted at element.
     *
     * @throws SmooksException if parsing fails
     */
    @Test
    public void executeLifeCycleCleanup_onlyForTargetElements() throws SmooksException {
        // given
        final VisitBeforeAndPostFragmentLifecycle firstMock = mock(VisitBeforeAndPostFragmentLifecycle.class);
        final VisitBeforeAndPostFragmentLifecycle secondMock = mock(VisitBeforeAndPostFragmentLifecycle.class);

        final Smooks smooks = createSmooks();
        smooks.addVisitor(firstMock, "simple:simple/first:sample");
        smooks.addVisitor(secondMock, "simple:simple/second:sample");

        // when
        smooks.filterSource(smooks.createExecutionContext(), createSource(), mock(Sink.class));

        // then
        verify(firstMock).visitBefore(
                argThat(isSaxElementWithQName(QNAME_FOR_FIRST_SAMPLE)),
                any(ExecutionContext.class));
		verify(firstMock, never()).visitBefore(
                argThat(isSaxElementWithQName(QNAME_FOR_SECOND_SAMPLE)),
                any(ExecutionContext.class));

        verify(firstMock).onPostFragment(
                argThat(isSaxFragmentWithQName(QNAME_FOR_FIRST_SAMPLE)),
                any(ExecutionContext.class));
        verify(firstMock, never()).onPostFragment(
                argThat(isSaxFragmentWithQName(QNAME_FOR_SECOND_SAMPLE)),
                any(ExecutionContext.class));

        verify(secondMock).visitBefore(
                argThat(isSaxElementWithQName(QNAME_FOR_SECOND_SAMPLE)),
                any(ExecutionContext.class));
        verify(secondMock, never()).visitBefore(
                argThat(isSaxElementWithQName(QNAME_FOR_FIRST_SAMPLE)),
                any(ExecutionContext.class));

        verify(secondMock).onPostFragment(
                argThat(isSaxFragmentWithQName(QNAME_FOR_SECOND_SAMPLE)),
                any(ExecutionContext.class));
        verify(secondMock, never()).onPostFragment(
                argThat(isSaxFragmentWithQName(QNAME_FOR_FIRST_SAMPLE)),
                any(ExecutionContext.class));

        verifyNoMoreInteractions(firstMock, secondMock);
    }

    private StreamSource createSource() {
        final InputStream inputStream = SaxNgContentHandlerTestCase.class
                .getResourceAsStream(SIMPLE_SAMPLE_XML);
        StreamSource source = new StreamSource(inputStream);
        return source;
    }

    private Smooks createSmooks() {
        final Smooks smooks = new Smooks();
        smooks.setNamespaces(createNamespacesMap());
        return smooks;
    }

    private Properties createNamespacesMap() {
        Properties namespaces = new Properties();
        namespaces.put("simple", URN_SIMPLE);
        namespaces.put("first", URN_FIRST);
        namespaces.put("second", URN_SECOND);
        return namespaces;
    }

    private interface VisitBeforeAndPostFragmentLifecycle extends BeforeVisitor, PostFragmentLifecycle {
    }

    static class SAXMatchers {

        private SAXMatchers() {
        }

        @Factory
        public static <T> Matcher<Element> isSaxElementWithQName(final QName qname) {
            return new IsElement(qname);
        }

        @Factory
        public static <T> Matcher<Fragment> isSaxFragmentWithQName(final QName qname) {
            return new IsSaxFragment(qname);
        }

    }

    private static class IsElement extends TypeSafeMatcher<Element> {
		private final QName qname;

        public IsElement(final QName qname) {
            if (qname == null) throw new IllegalArgumentException("qname must not be null.");
            this.qname = qname;
        }

        @Override
        public boolean matchesSafely(Element element) {
            return qname.getNamespaceURI().equals(element.getNamespaceURI()) && qname.getLocalPart().equals(element.getLocalName());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is a SAXElement with the qualified name " + qname);
        }
    }

    private static class IsSaxFragment extends TypeSafeMatcher<Fragment> {
		private final QName qname;

        public IsSaxFragment(final QName qname) {
            if (qname == null) throw new IllegalArgumentException("qname must not be null.");
            this.qname = qname;
        }

        @Override
        public boolean matchesSafely(Fragment fragment) {
            return qname.getNamespaceURI().equals(((Element) fragment.unwrap()).getNamespaceURI()) && qname.getLocalPart().equals(((Element) fragment.unwrap()).getLocalName());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("is a SAX fragment with the qualified name " + qname);
        }
    }
}

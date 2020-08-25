/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.profile;

import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

/**
 * Default device profile XML configuration digester. <p/> The profile
 * configuration is supplied in a well defined XML format. This XML format is
 * defined in <a href="doc-files/device-profile-1.0.txt">device-profile-1.0.dtd</a>
 * and a sample XML is defined in <a
 * href="doc-files/device-profile-sample-1.0.txt">device-profile-sample.xml</a>.
 * <p/> Uses XPath to parse the XML and construct the ProfileStore instance.
 * 
 * @author tfennelly
 */
public class DefaultProfileConfigDigester implements ProfileConfigDigester {

	/**
	 * Parse the device profile configuration stream.
	 * 
	 * @param input
	 *            The input stream instance.
	 * @return ProfileStore instance.
	 */
	public ProfileStore parse(InputStream input) throws SAXException,
			IOException {
		DefaultProfileStore store = new DefaultProfileStore();
		Document profileDoc = null;
		int count = 0;
		int profileIndex = 1;
		String profileSelector = null;

		if (input == null) {
			throw new IllegalArgumentException(
					"null 'stream' exception in method call.");
		}
		profileDoc = XmlUtil.parseStream(input, XmlUtil.VALIDATION_TYPE.DTD, true);

		// While there are device profile definitions.
		profileSelector = "/device-profiles/device-profile[" + profileIndex
				+ "]";
		while (!XmlUtil.getString(profileDoc, profileSelector).equals("")) {
			String name = XmlUtil.getString(profileDoc, profileSelector
					+ "/@name");
			String list = XmlUtil.getString(profileDoc, profileSelector
					+ "/@list");
			StringTokenizer tokenizer = null;

			if (name == null) {
				throw new SAXException(
						"<device-profile> mandatory attribute 'name' no present.");
			}
			name = name.trim();
			if (name.equals("")) {
				throw new SAXException(
						"<device-profile> attribute 'name' contains invalid empty value.");
			}
			if (list == null) {
				throw new SAXException(
						"<device-profile> mandatory attribute 'list' no present.");
			}
			list = list.trim();
			if (list.equals("")) {
				throw new SAXException(
						"<device-profile> attribute 'list' contains invalid empty value.");
			}
			tokenizer = new StringTokenizer(list, ",|;");
			if (tokenizer.countTokens() == 0) {
				throw new SAXException(
						"<device-profile> attribute 'list' contains invalid value ["
								+ list + "].");
			}

			while (tokenizer.hasMoreTokens()) {
				String profileMember = tokenizer.nextToken();
				DefaultProfileSet profileSet = null;

				try {
					profileSet = (DefaultProfileSet) store
							.getProfileSet(profileMember);
				} catch (UnknownProfileMemberException unknown) {
					profileSet = new DefaultProfileSet(profileMember);
					try {
						store.addProfileSet(profileSet);
					} catch (IllegalArgumentException invalid) {
						SAXException saxE = new SAXException(
								"<device-profile> attribute 'list' contains invalid device-name token value ["
										+ profileMember + "].", invalid);
						throw saxE;
					}
				}
				profileSet.addProfile(new BasicProfile(name));
			}

			count++;
			profileIndex++;
			profileSelector = "/device-profiles/device-profile[" + profileIndex
					+ "]";
		}

		if (count == 0) {
			throw new SAXException("No <device-profile> elements defined.");
		}

		store.expandProfiles();

		return store;
	}
}

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

package org.milyn.profile;

import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
			;

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
										+ profileMember + "].");
						saxE.initCause(invalid);
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

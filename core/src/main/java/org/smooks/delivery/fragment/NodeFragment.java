/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.fragment;

import org.w3c.dom.Node;
import org.w3c.dom.UserDataHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class NodeFragment implements Fragment {

    public static final String RESERVATIONS_USER_DATA_KEY = "reservations";
    public static final String ID_USER_DATA_KEY = "id";

    private final Node node;
    private final boolean isReservationInheritable;

    static class CopyUserDataHandler implements UserDataHandler {
        CopyUserDataHandler() {
            
        }
        
        @Override
        public void handle(final short operation, final String key, final Object data, final Node src, final Node dst) {
            dst.setUserData(key, data, new CopyUserDataHandler());
        }
    }

    static class Reservation {
        private final Object token;
        private final boolean inheritable;

        Reservation(final Object token, final boolean inheritable) {
            this.token = token;
            this.inheritable = inheritable;
        }

        public Object getToken() {
            return token;
        }

        public boolean isInheritable() {
            return inheritable;
        }
    }
    
    public NodeFragment(final Node node) {
        this(node, false);
    }

    public NodeFragment(final Node node, final boolean isReservationInheritable) {
        this.node = node;
        this.isReservationInheritable = isReservationInheritable;

        final CopyUserDataHandler copyUserDataHandler = new CopyUserDataHandler();
        if (node.getUserData(ID_USER_DATA_KEY) == null) {
            node.setUserData(ID_USER_DATA_KEY, String.valueOf(Math.abs(ThreadLocalRandom.current().nextLong())), copyUserDataHandler);
        }

        Map<Long, Reservation> reservations = (Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY);
        if (reservations == null) {
            reservations = new HashMap<>();
            node.setUserData(RESERVATIONS_USER_DATA_KEY, reservations, copyUserDataHandler);
        } else {
            reservations = new HashMap<>();
        }

        Node parentNode = node.getParentNode();
        while (parentNode != null) {
            final Map<Long, Reservation> parentNodeReservations = (Map<Long, Reservation>) parentNode.getUserData(RESERVATIONS_USER_DATA_KEY);
            if (parentNodeReservations != null) {
                reservations.putAll(parentNodeReservations.entrySet().stream().filter(e -> e.getValue().isInheritable()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
            parentNode = parentNode.getParentNode();
        }
    }

    @Override
    public String getId() {
        return (String) node.getUserData(ID_USER_DATA_KEY);
    }

    @Override
    public Object unwrap() {
        return node;
    }

    @Override
    public boolean reserve(final long id, final Object token) {
        return ((Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY)).
                computeIfAbsent(id, key -> new Reservation(token, isReservationInheritable)).
                getToken().equals(token);
    }

    @Override
    public boolean release(final long id, final Object token) {
        final Map<Long, Reservation> reservedTokens = (Map<Long, Reservation>) node.getUserData(RESERVATIONS_USER_DATA_KEY);
        final Object reservedToken = reservedTokens.getOrDefault(id, new Reservation(token, isReservationInheritable)).getToken();
        if (reservedToken.equals(token)) {
            reservedTokens.remove(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return node.getNodeName();
    }
}

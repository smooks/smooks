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
package org.smooks.engine.converter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQueries;
import java.util.Date;

public abstract class StringToDateLocaleAwareConverter<T> extends DateLocaleAwareTypeConverter<String, T> {
    
    @Override
    public T convert(final String value) {
        final TemporalAccessor temporalAccessor = dateTimeFormatter.parse(value.trim());
        final Date date;
        if (temporalAccessor.query(TemporalQueries.localDate()) != null && temporalAccessor.query(TemporalQueries.localTime()) != null && temporalAccessor.query(TemporalQueries.zoneId()) != null) {
            date = Date.from(LocalDateTime.from(temporalAccessor).atZone(temporalAccessor.query(TemporalQueries.zoneId())).toInstant());
        } else if (temporalAccessor.query(TemporalQueries.localDate()) != null && temporalAccessor.query(TemporalQueries.localTime()) != null) {
            date = Date.from(LocalDateTime.from(temporalAccessor).atZone(zoneId).toInstant());
        } else if (temporalAccessor.query(TemporalQueries.localDate()) != null) {
            date = Date.from(LocalDate.from(temporalAccessor).atStartOfDay(zoneId).toInstant());
        } else if (temporalAccessor.query(TemporalQueries.localTime()) != null && temporalAccessor.query(TemporalQueries.zoneId()) != null) {
            date = Date.from(LocalTime.from(temporalAccessor).atDate(Instant.ofEpochMilli(0L).atZone(temporalAccessor.query(TemporalQueries.zoneId())).toLocalDate()).atZone(temporalAccessor.query(TemporalQueries.zoneId())).toInstant());
        } else if (temporalAccessor.query(TemporalQueries.localTime()) != null) {
            date = Date.from(LocalTime.from(temporalAccessor).atDate(Instant.ofEpochMilli(0L).atZone(zoneId).toLocalDate()).atZone(zoneId).toInstant());
        } else {
            date = null;
        }
        
        return doConvert(date);
    }

    protected abstract T doConvert(Date date);
}

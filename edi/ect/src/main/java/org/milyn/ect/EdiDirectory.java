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

package org.milyn.ect;

import org.milyn.edisax.model.internal.Edimap;

import java.util.ArrayList;
import java.util.List;

/**
 * EDI directory model.
 * <p/>
 * Contains the mapping models for all message in an EDI directory/specification e.g.
 * for UN/EDIFACT it contains EDI mapping models for all the messages in a
 * UN/EDIFACT directory specification zip file.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EdiDirectory {

    private Edimap commonModel = null;
    private List<Edimap> messageModels = new ArrayList<Edimap>();

    /**
     * Public constructor.
     * @param commonModel The "common" model.  Contains common types used across
     * (and imported by) the messages in the directory.
     * @param messageModels The individual message models.
     */
    public EdiDirectory(Edimap commonModel, List<Edimap> messageModels) {
        this.commonModel = commonModel;
        this.messageModels = messageModels;
    }

    /**
     * Get the common model.
     * @return The common model.
     */
    public Edimap getCommonModel() {
        return commonModel;
    }

    /**
     * Get the message models.
     * <p/>
     * This list does not contain the {@link #getCommonModel() common} model.
     *
     * @return The message models.
     */
    public List<Edimap> getMessageModels() {
        return messageModels;
    }
}

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.events;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.events.UIEventImpl;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.views.AbstractView;

/**
 * An implementation of the DOM Level 2 <code>MouseEvent</code> interface.
 * 
 * @xerces.internal 
 * 
 * @version $Id$
 */
public class MouseEventImpl 
    extends UIEventImpl 
    implements MouseEvent {
    
    private int fScreenX;
    private int fScreenY;
    private int fClientX;
    private int fClientY;
    private boolean fCtrlKey;
    private boolean fAltKey;
    private boolean fShiftKey;
    private boolean fMetaKey;
    private short fButton;
    private EventTarget fRelatedTarget;

    public int getScreenX() {
        return fScreenX;
    }

    public int getScreenY() {
        return fScreenY;
    }

    public int getClientX() {
        return fClientX;
    }

    public int getClientY() {
        return fClientY;
    }

    public boolean getCtrlKey() {
        return fCtrlKey;
    }
    
    public boolean getAltKey() {
        return fAltKey;
    }

    public boolean getShiftKey() {
        return fShiftKey;
    }

    public boolean getMetaKey() {
        return fMetaKey;
    }

    public short getButton() {
        return fButton;
    }

    public EventTarget getRelatedTarget() {
        return fRelatedTarget;
    }

    public void initMouseEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, 
            int detailArg, int screenXArg, int screenYArg, int clientXArg, int clientYArg, 
            boolean ctrlKeyArg, boolean altKeyArg, boolean shiftKeyArg, boolean metaKeyArg, 
            short buttonArg, EventTarget relatedTargetArg) {
        fScreenX = screenXArg;
        fScreenY = screenYArg;
        fClientX = clientXArg;
        fClientY = clientYArg;
        fCtrlKey = ctrlKeyArg;
        fAltKey = altKeyArg;
        fShiftKey = shiftKeyArg;
        fMetaKey = metaKeyArg;
        fButton = buttonArg;
        fRelatedTarget = relatedTargetArg;
        super.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, detailArg);
    }
}

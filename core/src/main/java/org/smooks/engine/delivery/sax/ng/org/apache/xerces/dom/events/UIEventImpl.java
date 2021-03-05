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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.dom.events.EventImpl;
import org.w3c.dom.events.UIEvent;
import org.w3c.dom.views.AbstractView;

/**
 * An implementation of the DOM Level 2 <code>UIEvent</code> interface.
 * 
 * @xerces.internal 
 * 
 * @version $Id$
 */
public class UIEventImpl 
    extends EventImpl 
    implements UIEvent {
    
    private AbstractView fView;
    private int fDetail;
    
    public AbstractView getView() {
        return fView;
    }

    public int getDetail() {
        return fDetail;
    }

    public void initUIEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, 
            AbstractView viewArg, int detailArg) {
        fView = viewArg;
        fDetail = detailArg;
        super.initEvent(typeArg, canBubbleArg, cancelableArg);
    }
}

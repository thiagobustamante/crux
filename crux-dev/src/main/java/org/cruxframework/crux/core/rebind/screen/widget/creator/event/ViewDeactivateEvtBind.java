/*
 * Copyright 2011 cruxframework.org.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.cruxframework.crux.core.rebind.screen.widget.creator.event;

import org.cruxframework.crux.core.client.screen.views.ViewDeactivateEvent;
import org.cruxframework.crux.core.client.screen.views.ViewDeactivateHandler;
import org.cruxframework.crux.core.rebind.AbstractProxyCreator.SourcePrinter;
import org.cruxframework.crux.core.rebind.screen.widget.EvtProcessor;
import org.cruxframework.crux.core.rebind.screen.widget.WidgetCreator;

/**
 * Helper Class for View load events binding
 * @author Thiago Bustamante
 */
public class ViewDeactivateEvtBind extends EvtProcessor
{
	public ViewDeactivateEvtBind(WidgetCreator<?> widgetCreator)
    {
	    super(widgetCreator);
    }

	private static final String EVENT_NAME = "onViewDeactivate";

	/**
	 * @see org.cruxframework.crux.core.rebind.screen.widget.EvtProcessor#getEventName()
	 */
	public String getEventName()
	{
		return EVENT_NAME;
	}
	
	@Override
    public Class<?> getEventClass()
    {
	    return ViewDeactivateEvent.class;
    }

	@Override
    public Class<?> getEventHandlerClass()
    {
	    return ViewDeactivateHandler.class;
    }	

	@Override
    public void processEvent(SourcePrinter out, String eventValue, String parentVariable, String widgetId)
    {
		out.println(parentVariable+".add"+getEventHandlerClass().getSimpleName()+"(new "+getEventHandlerClass().getCanonicalName()+"(){");
		out.println("public void onDeactivate("+getEventClass().getCanonicalName()+" event){");
		printEvtCall(out, eventValue, "event");
		out.println("}");
		out.println("});");
		//TODO adicionar tratamento de erro e logar a widgetId onde deu o erro aqui.
    }
	
}

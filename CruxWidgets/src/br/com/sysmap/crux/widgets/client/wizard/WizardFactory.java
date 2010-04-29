/*
 * Copyright 2009 Sysmap Solutions Software e Consultoria Ltda.
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
package br.com.sysmap.crux.widgets.client.wizard;

import br.com.sysmap.crux.core.client.declarative.DeclarativeFactory;
import br.com.sysmap.crux.core.client.declarative.TagAttributeDeclaration;
import br.com.sysmap.crux.core.client.declarative.TagAttributesDeclaration;
import br.com.sysmap.crux.core.client.declarative.TagChild;
import br.com.sysmap.crux.core.client.declarative.TagChildAttributes;
import br.com.sysmap.crux.core.client.declarative.TagChildren;
import br.com.sysmap.crux.core.client.declarative.TagEvent;
import br.com.sysmap.crux.core.client.declarative.TagEventDeclaration;
import br.com.sysmap.crux.core.client.declarative.TagEvents;
import br.com.sysmap.crux.core.client.declarative.TagEventsDeclaration;
import br.com.sysmap.crux.core.client.event.Event;
import br.com.sysmap.crux.core.client.event.Events;
import br.com.sysmap.crux.core.client.event.bind.EvtBind;
import br.com.sysmap.crux.core.client.screen.InterfaceConfigException;
import br.com.sysmap.crux.core.client.screen.ScreenFactory;
import br.com.sysmap.crux.core.client.screen.WidgetFactory;
import br.com.sysmap.crux.core.client.screen.children.AllChildProcessor;
import br.com.sysmap.crux.core.client.screen.children.ChoiceChildProcessor;
import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor;
import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessorContext;
import br.com.sysmap.crux.core.client.screen.children.WidgetChildProcessor.AnyWidget;
import br.com.sysmap.crux.core.client.utils.StringUtils;
import br.com.sysmap.crux.widgets.client.event.CancelEvtBind;
import br.com.sysmap.crux.widgets.client.event.FinishEvtBind;
import br.com.sysmap.crux.widgets.client.wizard.Wizard.ControlPosition;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Thiago da Rosa de Bustamante - <code>tr_bustamante@yahoo.com.br</code>
 *
 */
@DeclarativeFactory(id="wizard", library="widgets")
public class WizardFactory extends WidgetFactory<Wizard>
{
	@Override
    public Wizard instantiateWidget(Element element, String widgetId) throws InterfaceConfigException
    {
	    return new Wizard();
    }

	@Override
	@TagEvents({
		@TagEvent(FinishEvtBind.class),
		@TagEvent(CancelEvtBind.class)
	})
	public void processEvents(WidgetFactoryContext<Wizard> context) throws InterfaceConfigException
	{
	    super.processEvents(context);
	}

	@Override
	public void postProcess(WidgetFactoryContext<Wizard> context) throws InterfaceConfigException
	{
	    context.getWidget().first();
	}
	
	
	@Override
	@TagChildren({
		@TagChild(WizardChildrenProcessor.class)
	})
	public void processChildren(WidgetFactoryContext<Wizard> context) throws InterfaceConfigException {}
	
	public static class WizardChildrenProcessor extends AllChildProcessor<Wizard>
	{
		@Override
		@TagChildren({
			@TagChild(NavigationBarProcessor.class),
			@TagChild(StepsProcessor.class),
			@TagChild(ControlBarProcessor.class)
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException {}
	}
	
	@TagChildAttributes(tagName="navigationBar", minOccurs="0")
	public static class NavigationBarProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagAttributesDeclaration({
			@TagAttributeDeclaration(value="position", type=ControlPosition.class, defaultValue="north")
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException 
		{
			String positionAttr = context.getChildElement().getAttribute("_position");
			ControlPosition position = ControlPosition.north;
			if (!StringUtils.isEmpty(positionAttr))
			{
				position = ControlPosition.valueOf(positionAttr);
			}
			context.getRootWidget().setNavigationBar(new WizardNavigationBar(), position);
		}
	}
	
	@TagChildAttributes(tagName="steps")
	public static class StepsProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagChildren({
			@TagChild(WizardStepsProcessor.class)
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException {}
	}

	@TagChildAttributes(maxOccurs="unbounded")
	public static class WizardStepsProcessor extends ChoiceChildProcessor<Wizard>
	{
		@Override
		@TagChildren({
			@TagChild(WidgetStepProcessor.class),
			@TagChild(PageStepProcessor.class)
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException {}
	}
	
	@TagChildAttributes(tagName="widget")
	public static class WidgetStepProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagChildren({
			@TagChild(WidgetProcessor.class),
			@TagChild(CommandsProcessor.class)
		})
		@TagAttributesDeclaration({
			@TagAttributeDeclaration(value="id", required=true),
			@TagAttributeDeclaration(value="label", required=true, supportsI18N=true),
		})
		@TagEventsDeclaration({
			@TagEventDeclaration("onEnter"),
			@TagEventDeclaration("onLeave")
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException 
		{
			context.setAttribute("stepId", context.getChildElement().getAttribute("id"));
			context.setAttribute("stepLabel", context.getChildElement().getAttribute("_label"));
			context.setAttribute("stepOnEnter", context.getChildElement().getAttribute("_onEnter"));
			context.setAttribute("stepOnLeave", context.getChildElement().getAttribute("_onLeave"));
		}
	}
	
	@TagChildAttributes(tagName="commands", minOccurs="0")
	public static class CommandsProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagChildren({
			@TagChild(WizardCommandsProcessor.class),
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException {}
	}
	
	@TagChildAttributes(tagName="command", maxOccurs="unbounded")
	public static class WizardCommandsProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagAttributesDeclaration({
			@TagAttributeDeclaration(value="id", required=true),
			@TagAttributeDeclaration(value="label", required=true, supportsI18N=true),
			@TagAttributeDeclaration(value="order", required=true, type=Integer.class),
			@TagAttributeDeclaration(value="onCommand", required=true)
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException 
		{
			String id = context.getChildElement().getAttribute("id");
			String label = ScreenFactory.getInstance().getDeclaredMessage(context.getChildElement().getAttribute("_label"));
			int order = Integer.parseInt(context.getChildElement().getAttribute("_order"));
			
			final Event commandEvent = EvtBind.getWidgetEvent(context.getChildElement(), "onCommand");
			
			WizardCommandHandler handler = new WizardCommandHandler()
			{
				public void onCommand(WizardCommandEvent event)
				{
					Events.callEvent(commandEvent, event);
				}
			};
			
			WidgetStep widgetStep = context.getRootWidget().getWidgetStep((String)context.getAttribute("stepId"));
			widgetStep.addCommand(id, label, handler, order);
		}
	}
	
	@TagChildAttributes(type=AnyWidget.class)
	public static class WidgetProcessor extends WidgetChildProcessor<Wizard> 
	{
		@Override
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException
		{
			Widget childWidget = createChildWidget(context.getChildElement(), context.getChildElement().getId());
			
			String id = (String)context.getAttribute("stepId");
			String label = (String)context.getAttribute("stepLabel");
			
			WidgetStep widgetStep = context.getRootWidget().addWidgetStep(id, label, childWidget);
			
			String onEnter = (String)context.getAttribute("stepOnEnter");
			final Event onEnterEvent = Events.getEvent("onEnter", onEnter);
			if (onEnterEvent != null)
			{
				widgetStep.addEnterHandler(new EnterHandler()
				{
					public void onEnter(EnterEvent event)
					{
						Events.callEvent(onEnterEvent, event);
					}
				});
			}
			
			String onLeave = (String)context.getAttribute("stepOnLeave");
			final Event onLeaveEvent = Events.getEvent("onLeave", onLeave);
			if (onLeaveEvent != null)
			{
				widgetStep.addLeaveHandler(new LeaveHandler()
				{
					public void onLeave(LeaveEvent event)
					{
						Events.callEvent(onLeaveEvent, event);
					}
				});
			}
		}
	}

	@TagChildAttributes(tagName="page")
	public static class PageStepProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagAttributesDeclaration({
			@TagAttributeDeclaration(value="id", required=true),
			@TagAttributeDeclaration(value="label", required=true, supportsI18N=true),
			@TagAttributeDeclaration(value="url", required=true)
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException
		{
			String id = context.getChildElement().getAttribute("id");
			String label = ScreenFactory.getInstance().getDeclaredMessage(context.getChildElement().getAttribute("_label"));
			String url = context.getChildElement().getAttribute("_url");
			context.getRootWidget().addPageStep(id, label, url);
		}
	}
	
	@TagChildAttributes(tagName="controlBar", minOccurs="0")
	public static class ControlBarProcessor extends WidgetChildProcessor<Wizard>
	{
		@Override
		@TagAttributesDeclaration({
			@TagAttributeDeclaration(value="position", type=ControlPosition.class, defaultValue="south"),
			@TagAttributeDeclaration(value="vertical", type=Boolean.class, defaultValue="false"),
			@TagAttributeDeclaration(value="spacing", type=Integer.class),
			@TagAttributeDeclaration(value="buttonsWidth", type=String.class),
			@TagAttributeDeclaration(value="buttonsHeight", type=String.class),
			@TagAttributeDeclaration(value="buttonsStyle", type=String.class),
			@TagAttributeDeclaration(value="previousLabel", type=String.class, supportsI18N=true),
			@TagAttributeDeclaration(value="nextLabel", type=String.class, supportsI18N=true),
			@TagAttributeDeclaration(value="cancelLabel", type=String.class, supportsI18N=true),
			@TagAttributeDeclaration(value="finishLabel", type=String.class, supportsI18N=true),
			@TagAttributeDeclaration(value="previousOrder", type=Integer.class, defaultValue="0"),
			@TagAttributeDeclaration(value="nextOrder", type=Integer.class, defaultValue="1"),
			@TagAttributeDeclaration(value="cancelOrder", type=Integer.class, defaultValue="2"),
			@TagAttributeDeclaration(value="finishOrder", type=Integer.class, defaultValue="3")
		})
		public void processChildren(WidgetChildProcessorContext<Wizard> context) throws InterfaceConfigException 
		{
			String positionAttr = context.getChildElement().getAttribute("_position");
			ControlPosition position = ControlPosition.south;
			if (!StringUtils.isEmpty(positionAttr))
			{
				position = ControlPosition.valueOf(positionAttr);
			}
			
			String verticalAttr = context.getChildElement().getAttribute("_vertical");
			boolean vertical = false;
			if (!StringUtils.isEmpty(verticalAttr))
			{
				vertical = Boolean.parseBoolean(verticalAttr);
			}
			context.getRootWidget().setControlBar(new WizardControlBar(vertical), position);
			WizardControlBar controlBar = context.getRootWidget().getControlBar();
			
			processControlBarAttributes(context, controlBar);
		}

		private void processControlBarAttributes(WidgetChildProcessorContext<Wizard> context,
                WizardControlBar controlBar)
        {
	        String spacing = context.getChildElement().getAttribute("_spacing");
			if (!StringUtils.isEmpty(spacing))
			{
				controlBar.setSpacing(Integer.parseInt(spacing));
			}
			
			String buttonsWidth = context.getChildElement().getAttribute("_buttonsWidth");
			if (!StringUtils.isEmpty(buttonsWidth))
			{
				controlBar.setButtonsWidth(buttonsWidth);
			}

			String buttonsHeight = context.getChildElement().getAttribute("_buttonsHeight");
			if (!StringUtils.isEmpty(buttonsHeight))
			{
				controlBar.setButtonsHeight(buttonsHeight);
			}

			String buttonsStyle = context.getChildElement().getAttribute("_buttonsStyle");
			if (!StringUtils.isEmpty(buttonsStyle))
			{
				controlBar.setButtonsStyle(buttonsStyle);
			}

			String previousLabel = context.getChildElement().getAttribute("_previousLabel");
			if (!StringUtils.isEmpty(previousLabel))
			{
				controlBar.setPreviousLabel(ScreenFactory.getInstance().getDeclaredMessage(previousLabel));
			}

			String nextLabel = context.getChildElement().getAttribute("_nextLabel");
			if (!StringUtils.isEmpty(nextLabel))
			{
				controlBar.setNextLabel(ScreenFactory.getInstance().getDeclaredMessage(nextLabel));
			}

			String cancelLabel = context.getChildElement().getAttribute("_cancelLabel");
			if (!StringUtils.isEmpty(cancelLabel))
			{
				controlBar.setCancelLabel(ScreenFactory.getInstance().getDeclaredMessage(cancelLabel));
			}

			String finishLabel = context.getChildElement().getAttribute("_finishLabel");
			if (!StringUtils.isEmpty(finishLabel))
			{
				controlBar.setFinishLabel(ScreenFactory.getInstance().getDeclaredMessage(finishLabel));
			}

			String previousOrder = context.getChildElement().getAttribute("_previousOrder");
			if (!StringUtils.isEmpty(previousOrder))
			{
				controlBar.setPreviousOrder(Integer.parseInt(previousOrder));
			}

			String nextOrder = context.getChildElement().getAttribute("_nextOrder");
			if (!StringUtils.isEmpty(nextOrder))
			{
				controlBar.setNextOrder(Integer.parseInt(nextOrder));
			}

			String cancelOrder = context.getChildElement().getAttribute("_cancelOrder");
			if (!StringUtils.isEmpty(cancelOrder))
			{
				controlBar.setCancelOrder(Integer.parseInt(cancelOrder));
			}

			String finishOrder = context.getChildElement().getAttribute("_finishOrder");
			if (!StringUtils.isEmpty(finishOrder))
			{
				controlBar.setFinishOrder(Integer.parseInt(finishOrder));
			}
        }
	}

}
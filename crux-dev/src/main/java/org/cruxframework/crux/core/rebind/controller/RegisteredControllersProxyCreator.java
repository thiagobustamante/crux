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
package org.cruxframework.crux.core.rebind.controller;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.cruxframework.crux.core.client.Crux;
import org.cruxframework.crux.core.client.collection.FastMap;
import org.cruxframework.crux.core.client.controller.Controller;
import org.cruxframework.crux.core.client.controller.RegisteredControllers;
import org.cruxframework.crux.core.client.screen.DeviceAdaptive.Device;
import org.cruxframework.crux.core.client.utils.EscapeUtils;
import org.cruxframework.crux.core.client.utils.StringUtils;
import org.cruxframework.crux.core.rebind.AbstractInterfaceWrapperProxyCreator;
import org.cruxframework.crux.core.rebind.CruxGeneratorException;
import org.cruxframework.crux.core.rebind.context.RebindContext;
import org.cruxframework.crux.core.rebind.context.scanner.ResourceNotFoundException;
import org.cruxframework.crux.core.rebind.ioc.IocContainerRebind;
import org.cruxframework.crux.core.rebind.screen.View;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.generator.NameFactory;

/**
 * Generates a RegisteredControllers class. 
 * 
 * 
 * @author Thiago da Rosa de Bustamante
 *
 */
public class RegisteredControllersProxyCreator extends AbstractInterfaceWrapperProxyCreator
{
	private Map<String, String> controllerClassNames = new HashMap<String, String>();
	private Device device;
	private String iocContainerClassName;
	private IocContainerRebind iocContainerRebind;
	private NameFactory nameFactory;
	private final View view;

	/**
	 * Constructor
	 * @param logger
	 * @param context
	 */
	public RegisteredControllersProxyCreator(RebindContext context, View view, String iocContainerClassName, String device)
    {
	    super(context, context.getGeneratorContext().getTypeOracle().findType(RegisteredControllers.class.getCanonicalName()), false);
		this.view = view;
		this.iocContainerClassName = iocContainerClassName;
		this.device = Device.valueOf(device);
		this.nameFactory = new NameFactory();
		this.iocContainerRebind = new IocContainerRebind(context, view, device);
    }

	@Override
	public String getProxySimpleName()
	{
		String className = view.getId() + "_" + device.toString(); 
		className = className.replaceAll("[\\W]", "_");
		return "RegisteredControllers_"+className;
	}

	/**
	 * 
	 * @param sourceWriter
	 * @throws CruxGeneratorException
	 */
	@Override
	protected void generateProxyContructor(SourcePrinter sourceWriter) throws CruxGeneratorException
	{
		sourceWriter.println("public "+getProxySimpleName()+"("+org.cruxframework.crux.core.client.screen.views.View.class.getCanonicalName()+" view, " +
				iocContainerClassName+" iocContainer){");
		sourceWriter.println("this.view = view;");
		sourceWriter.println("this.iocContainer = iocContainer;");
		for (String controller : controllerClassNames.keySet()) 
		{
			JClassType controllerClass = getControllerClass(controller);
			if (!isControllerLazy(controllerClass))
			{
				String controllerVar = createController(sourceWriter, controller);
				sourceWriter.println("controllers.put(\""+controller+"\", "+controllerVar+");");
			}
		}
		sourceWriter.println("}");
    }	

	/**
	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateProxyFields(com.google.gwt.user.rebind.SourceWriter)
	 */
	@Override
    protected void generateProxyFields(SourcePrinter srcWriter) throws CruxGeneratorException
    {
		srcWriter.println("private FastMap<Object> controllers = new FastMap<Object>();");
		srcWriter.println("private "+org.cruxframework.crux.core.client.screen.views.View.class.getCanonicalName()+" view;");
		srcWriter.println("private "+iocContainerClassName+" iocContainer;");
    }

	/**
	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateProxyMethods(com.google.gwt.user.rebind.SourceWriter)
	 */
	@Override
    protected void generateProxyMethods(SourcePrinter sourceWriter) throws CruxGeneratorException
    {
		generateGetControllertMethod(sourceWriter);
    }

	/**
	 * @see org.cruxframework.crux.core.rebind.AbstractProxyCreator#generateSubTypes(com.google.gwt.user.rebind.SourceWriter)
	 */
	@Override
	protected void generateSubTypes(SourcePrinter srcWriter) throws CruxGeneratorException
	{
		Set<String> usedWidgets = new HashSet<String>();
		generateControllersForView(view);
		Iterator<org.cruxframework.crux.core.rebind.screen.Widget> screenWidgets = view.iterateWidgets();
		while (screenWidgets.hasNext())
		{
			String widgetType = screenWidgets.next().getType();
			usedWidgets.add(widgetType);
		}
		generateControllersForWidgets(usedWidgets);
	}	
	
	/**
	 * @return
	 */
    @Override
	protected String[] getImports()
    {
	    String[] imports = new String[] {
    		GWT.class.getCanonicalName(), 
    		org.cruxframework.crux.core.client.screen.Screen.class.getCanonicalName(),
    		RunAsyncCallback.class.getCanonicalName(),
    		Crux.class.getCanonicalName(), 
    		FastMap.class.getCanonicalName(),
    		StringUtils.class.getCanonicalName()
		};
	    return imports;
    }	
	
	/**
	 * 
	 * @param sourceWriter
	 * @param controller
	 * @return
	 */
	private String createController(SourcePrinter sourceWriter, String controller)
	{
		String controllerClassName = controllerClassNames.get(controller);
		String controllerVar = nameFactory.createName("__cont");
		sourceWriter.println(controllerClassName+" "+controllerVar+"  = new "+controllerClassName+"(this.view);");
		JClassType controllerClass = getControllerClass(controller);
		iocContainerRebind.injectFieldsAndMethods(sourceWriter, controllerClass, controllerVar, "iocContainer", view, device);
		return controllerVar;
	}

	/**
	 * Generate the block to include controller object.
	 * @param controller
	 * @param module
	 */
	private void generateControllerBlock(String controller)
	{
		try
		{
			JClassType controllerClass = getControllerClass(controller);
			if (!controllerClassNames.containsKey(controller) && controllerClass!= null)
			{
				String genClass = new ControllerProxyCreator(context, controllerClass).create();
				controllerClassNames.put(controller, genClass);
			}
		}
		catch (Throwable e) 
		{
			throw new CruxGeneratorException("Error for register client event handler. Controller: ["+controller+"].", e);
		}
	}

	/**
	 * Generate wrapper classes for event handling.
	 * @param view
	 */
	private void generateControllersForView(View view)
	{
		Iterator<String> controllers = view.iterateControllers();
		
		while (controllers.hasNext())
		{
			String controller = controllers.next();
			generateControllerBlock(controller);
		}		

		controllers = context.getControllers().iterateGlobalControllers();
		
		while (controllers.hasNext())
		{
			String controller = controllers.next();
			JClassType controllerClass = getControllerClass(controller);
			if (controllerClass != null)
			{
				generateControllerBlock(controller);
			}
		}		
	}
	
	/**
	 * @param usedWidgets
	 */
	private void generateControllersForWidgets(Set<String> usedWidgets)
	{
		
		Iterator<String> widgets = usedWidgets.iterator();
		while (widgets.hasNext())
		{
			Iterator<String> controllers = context.getControllers().iterateWidgetControllers(widgets.next());
			if (controllers != null)
			{
				while (controllers.hasNext())
				{
					String controller = controllers.next();
					JClassType controllerClass = getControllerClass(controller);
					if (controllerClass != null)
					{
						generateControllerBlock(controller);
					}
				}
			}		
		}
	}

	/**
	 * @param sourceWriter
	 */
	private void generateGetControllertMethod(SourcePrinter sourceWriter)
	{
		sourceWriter.println("public <T> T getController(String controller){");
		sourceWriter.println("T ret = (T)controllers.get(controller);");
		sourceWriter.println("if (ret == null){");
		
		boolean first = true;
		for (String controller : controllerClassNames.keySet())
        {
			JClassType controllerClass = getControllerClass(controller);
			if (isControllerLazy(controllerClass))
			{
				if (!first)
				{
					sourceWriter.print("else ");
				}
				first = false;
				sourceWriter.println("if ("+StringUtils.class.getCanonicalName()+".unsafeEquals(controller, "+EscapeUtils.quote(controller)+")){");

				String controllerVar = createController(sourceWriter, controller);
				if (isControllerStatefull(controllerClass))
				{
					sourceWriter.println("controllers.put("+EscapeUtils.quote(controller)+", "+controllerVar+");");
				}
				else
				{
					sourceWriter.println("ret = (T) "+controllerVar+";");
				}

				sourceWriter.println("}");
			}
        }
		
		sourceWriter.println("if (ret == null){");
		sourceWriter.println("ret = (T)controllers.get(controller);");
		sourceWriter.println("}");
		
		sourceWriter.println("}");
		
		sourceWriter.println("return ret;");

		sourceWriter.println("}");
	}

	/**
	 * @param controller
	 * @return
	 */
	private JClassType getControllerClass(String controller) 
	{
		TypeOracle typeOracle = context.getGeneratorContext().getTypeOracle();
		assert (typeOracle != null);

		try
        {
	        return typeOracle.findType(context.getControllers().getController(controller, device));
        }
        catch (ResourceNotFoundException e)
        {
        	throw new CruxGeneratorException("Can not found the controller ["+controller+"]. Check your classpath and the inherit modules", e);
        }
	}
	
	/**
	 * @param controllerClass
	 * @return true if this controller can be loaded in lazy mode
	 * @throws CruxGeneratorException 
	 */
	private boolean isControllerLazy(JClassType controllerClass) throws CruxGeneratorException
    {
		Controller controllerAnnot = controllerClass.getAnnotation(Controller.class);
        return (controllerAnnot == null || controllerAnnot.lazy());
    }

	/**
	 * @param controllerClass
	 * @return true if this controller can be loaded in lazy mode
	 * @throws CruxGeneratorException 
	 */
	private boolean isControllerStatefull(JClassType controllerClass) throws CruxGeneratorException
    {
		Controller controllerAnnot = controllerClass.getAnnotation(Controller.class);
        return (controllerAnnot == null || controllerAnnot.stateful());
    }	
}

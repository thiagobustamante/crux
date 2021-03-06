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
package org.cruxframework.crux.core.rebind.datasource;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.cruxframework.crux.core.client.Legacy;
import org.cruxframework.crux.core.client.datasource.DataSourceExcpetion;
import org.cruxframework.crux.core.client.datasource.DataSourceRecord;
import org.cruxframework.crux.core.client.datasource.RegisteredDataSources;
import org.cruxframework.crux.core.client.formatter.HasFormatter;
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
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.dev.generator.NameFactory;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * Generates a RegisteredControllers class.  
 * 
 * @author Thiago da Rosa de Bustamante
 *
 */
public class RegisteredDataSourcesProxyCreator extends AbstractInterfaceWrapperProxyCreator
{
	private Map<String, String> dataSourcesClassNames = new HashMap<String, String>();
	private Device device;
	private String iocContainerClassName;
	private IocContainerRebind iocContainerRebind;
	private NameFactory nameFactory;
	private final View view;

	public RegisteredDataSourcesProxyCreator(RebindContext context, View view, String iocContainerClassName, String device)
    {
	    super(context, context.getGeneratorContext().getTypeOracle().findType(RegisteredDataSources.class.getCanonicalName()), false);
		this.view = view;
		this.iocContainerClassName = iocContainerClassName;
		this.device = Device.valueOf(device);
		this.nameFactory = new NameFactory();
		this.iocContainerRebind = new IocContainerRebind(context, view, device);
    }

	@Override
	public String getProxySimpleName()
	{
		String className = view.getId()+"_"+device.toString(); 
		className = className.replaceAll("[\\W]", "_");
		return "RegisteredDataSources_"+className;
	}

	@Override
	protected void generateProxyContructor(SourcePrinter sourceWriter) throws CruxGeneratorException
	{
		sourceWriter.println("public "+getProxySimpleName()+"("+org.cruxframework.crux.core.client.screen.views.View.class.getCanonicalName()+" view, " +
				iocContainerClassName+" iocContainer){");
		sourceWriter.println("this.view = view;");
		sourceWriter.println("this.iocContainer = iocContainer;");
		sourceWriter.println("}");
    }

	@Override
	protected void generateProxyFields(SourcePrinter srcWriter) throws CruxGeneratorException
	{
		srcWriter.println("private "+org.cruxframework.crux.core.client.screen.views.View.class.getCanonicalName()+" view;");
		srcWriter.println("private "+iocContainerClassName+" iocContainer;");
	}
	
	@Override
    protected void generateProxyMethods(SourcePrinter sourceWriter) throws CruxGeneratorException
    {
		generateGetDataSourceMethod(sourceWriter);
    }
	
	@Override
    protected void generateSubTypes(SourcePrinter srcWriter) throws CruxGeneratorException
    {
		generateDataSourcesForView(srcWriter, view);
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
    		Widget.class.getCanonicalName(),
    		HasValue.class.getCanonicalName(), 
    		HasText.class.getCanonicalName(),
    		HasFormatter.class.getCanonicalName(),
    		DataSourceExcpetion.class.getCanonicalName(),
    		DataSourceRecord.class.getCanonicalName(),
    		StringUtils.class.getCanonicalName()
		};
	    return imports;
    }
	
	/**
	 * 
	 * @param sourceWriter
	 * @param datasource
	 * @return
	 */
	@Deprecated
	@Legacy
	private String createDataSource(SourcePrinter sourceWriter, String dataSource)
	{
		String datasourceClassName = dataSourcesClassNames.get(dataSource);
		String dsVar = nameFactory.createName("__dat");
		sourceWriter.println(datasourceClassName+" "+dsVar+"  = new "+datasourceClassName+"(this.view);");
		JClassType datasourceClass;
        try
        {
	        datasourceClass = context.getGeneratorContext().getTypeOracle().findType(context.getDataSources().getDataSource(dataSource, device));
        }
        catch (ResourceNotFoundException e)
        {
			throw new CruxGeneratorException("Can not found the datasource ["+datasourceClassName+"]. Check your classpath and the inherit modules");
        }
		if (datasourceClass == null)
		{
			throw new CruxGeneratorException("Can not found the datasource ["+datasourceClassName+"]. Check your classpath and the inherit modules");
		}
		iocContainerRebind.injectFieldsAndMethods(sourceWriter, datasourceClass, dsVar, "iocContainer", view, device);
		return dsVar;
	}
	
	
	/**
	 * 
	 * @param sourceWriter
	 * @param dataSource
	 */
	@Deprecated
	@Legacy
	private void generateDataSourceClassBlock(SourcePrinter sourceWriter, String dataSource)
	{
		if (!dataSourcesClassNames.containsKey(dataSource) && context.getDataSources().hasDataSource(dataSource))
		{
			try
            {
	            JClassType dataSourceClass = baseIntf.getOracle().getType(context.getDataSources().getDataSource(dataSource, device));
	            String genClass = new DataSourceProxyCreator(context, dataSourceClass).create(); 
	            dataSourcesClassNames.put(dataSource, genClass);
            }
            catch (NotFoundException | ResourceNotFoundException e)
            {
            	throw new CruxGeneratorException("Can not generate datasource class", e);
            }
		}
	}
	
	/**
	 * 
	 * @param sourceWriter
	 * @param view
	 */
	private void generateDataSourcesForView(SourcePrinter sourceWriter, View view)
	{
		Iterator<String> dataSources = view.iterateDataSources();
		
		while (dataSources.hasNext())
		{
			String dataSource = dataSources.next();
			generateDataSourceClassBlock(sourceWriter, dataSource);
		}		
	}
	
	/**
	 * 
	 * @param logger
	 * @param sourceWriter
	 * @param implClassName
	 * @param dataSourcesClassNames
	 */
	private void generateGetDataSourceMethod(SourcePrinter sourceWriter) 
	{
		sourceWriter.println("public DataSource<?> getDataSource(String id){");
		boolean first = true;
		sourceWriter.println("if(id==null){");
		sourceWriter.println("throw new DataSourceExcpetion("+EscapeUtils.quote("DataSource not found: ")+"+id);");
		sourceWriter.println("}");
		for (String dataSource : dataSourcesClassNames.keySet()) 
		{
			if (!first)
			{
				sourceWriter.print("else ");
			}
			else
			{
				first = false;
			}
			sourceWriter.println("if(StringUtils.unsafeEquals(\""+dataSource+"\",id)){");
			String datasourceVar = createDataSource(sourceWriter, dataSource);
			sourceWriter.println("return "+datasourceVar+";");
			sourceWriter.println("}");
		}
		sourceWriter.println("throw new DataSourceExcpetion("+EscapeUtils.quote("DataSource not found: ")+"+id);");
		sourceWriter.println("}");
	}
}

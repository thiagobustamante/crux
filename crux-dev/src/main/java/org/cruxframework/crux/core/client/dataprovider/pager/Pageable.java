/*
 * Copyright 2014 cruxframework.org.
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
package org.cruxframework.crux.core.client.dataprovider.pager;

import org.cruxframework.crux.core.client.dataprovider.HasPagedDataProvider;
import org.cruxframework.crux.core.client.dataprovider.PagedDataProvider;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Define a base interface for classes that are capable to be paged by a {@link Pager}.  
 * @author Thiago da Rosa de Bustamante
 */
public interface Pageable<T> extends HasPagedDataProvider<PagedDataProvider<T>>, IsWidget
{
	/**
	 * Sets the pager
	 * @param pager
	 */
	void setPager(PageablePager<T> pager);
}

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
package org.cruxframework.crux.core.client.dataprovider;


/**
 * Interface to be implemented by classes that are capable of using PagedDataProviders.  
 * @author Thiago da Rosa de Bustamante
 */
public interface HasPagedDataProvider<T extends PagedDataProvider<?>> extends HasDataProvider<T>
{
	/**
	 * Moves the dataProvider's cursor to the next page 
	 */
	void nextPage();
	
	/**
	 * Moves the dataProvider's cursor to the previous page 
	 */	
	void previousPage();
	
	/**
	 * Return the total number of pages
	 * @return number of pages, -1 if unknown.
	 */
	int getPageCount();
	
	/**
	 * Moves the dataProvider's cursor to an arbitrary page
	 * @param page page number
	 */
	void goToPage(int page);
	
	/**
	 * Checks if dataProvider data is already available
	 * @param page
	 */
	boolean isDataLoaded();	

	/**
	 * Retrieve the page size of the dataProvider
	 * @return page size
	 */
	int getPageSize();

	/**
	 * Set the page size of the dataProvider
	 * @param pageSize page size
	 */
	void setPageSize(int pageSize);
	
	/**
	 * @return the currentPage
	 */
	int getCurrentPage();

	/**
	 * @return true if has more pages
	 */
	boolean hasNextPage();

	/**
	 * @return true if has previous pages
	 */
	boolean hasPreviousPage();
	
	
	/**
	 * Moves the dataProvider's cursor to the first page
	 */
	void firstPage();
	
	/**
	 * Moves the dataProvider's cursor to the last page
	 */
	void lastPage();
}

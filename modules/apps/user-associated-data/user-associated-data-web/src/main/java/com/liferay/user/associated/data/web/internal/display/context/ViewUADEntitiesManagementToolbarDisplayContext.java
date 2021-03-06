/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.user.associated.data.web.internal.display.context;

import com.liferay.frontend.taglib.clay.servlet.taglib.display.context.SearchContainerManagementToolbarDisplayContext;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItem;
import com.liferay.frontend.taglib.clay.servlet.taglib.util.DropdownItemList;
import com.liferay.petra.string.StringBundler;
import com.liferay.portal.kernel.dao.search.SearchContainer;
import com.liferay.portal.kernel.language.LanguageUtil;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.util.ParamUtil;
import com.liferay.portal.kernel.util.Validator;
import com.liferay.user.associated.data.web.internal.display.UADEntity;

import java.util.List;
import java.util.Map;

import javax.portlet.PortletURL;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Drew Brokke
 */
public class ViewUADEntitiesManagementToolbarDisplayContext
	extends SearchContainerManagementToolbarDisplayContext {

	public ViewUADEntitiesManagementToolbarDisplayContext(
		LiferayPortletRequest liferayPortletRequest,
		LiferayPortletResponse liferayPortletResponse,
		HttpServletRequest request,
		SearchContainer<UADEntity> searchContainer) {

		super(
			liferayPortletRequest, liferayPortletResponse, request,
			searchContainer);
	}

	@Override
	public List<DropdownItem> getActionDropdownItems() {
		return new DropdownItemList() {
			{
				add(
					dropdownItem -> {
						dropdownItem.setHref(
							StringBundler.concat(
								"javascript:", getNamespace(),
								"doAnonymizeMultiple();"));
						dropdownItem.setLabel(
							LanguageUtil.get(request, "anonymize"));
					});

				add(
					dropdownItem -> {
						dropdownItem.setHref(
							StringBundler.concat(
								"javascript:", getNamespace(),
								"doDeleteMultiple();"));
						dropdownItem.setLabel(
							LanguageUtil.get(request, "delete"));
					});
			}
		};
	}

	@Override
	public String getClearResultsURL() {
		PortletURL portletURL = getPortletURL();

		portletURL.setParameter("keywords", (String)null);

		return portletURL.toString();
	}

	@Override
	public String getInfoPanelId() {
		return "infoPanelId";
	}

	@Override
	public String getSearchActionURL() {
		PortletURL portletURL = getPortletURL();

		return portletURL.toString();
	}

	@Override
	public Boolean isShowSearch() {
		return true;
	}

	@Override
	protected Map<String, String> getOrderByEntriesMap() {
		return searchContainer.getOrderableHeaders();
	}

	@Override
	protected PortletURL getPortletURL() {
		PortletURL portletURL = searchContainer.getIteratorURL();

		String[] parameterNames =
			{"keywords", "orderByCol", "orderByType", "cur", "delta"};

		for (String parameterName : parameterNames) {
			String value = ParamUtil.getString(request, parameterName);

			if (Validator.isNotNull(value)) {
				portletURL.setParameter(parameterName, (String)null);
				portletURL.setParameter(parameterName, value);
			}
		}

		return portletURL;
	}

}
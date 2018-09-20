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

package com.liferay.source.formatter.checks;

import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.source.formatter.checks.util.GradleSourceUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Peter Shin
 */
public class GradleTestDependencyVersionCheck extends BaseFileCheck {

	@Override
	public boolean isPortalCheck() {
		return true;
	}

	public void setAllowedDependencyNames(String allowedDependencyNames) {
		Collections.addAll(
			_allowedDependencyNames, StringUtil.split(allowedDependencyNames));
	}

	@Override
	protected String doProcess(
		String fileName, String absolutePath, String content) {

		if (!absolutePath.contains("/modules/apps/") &&
			!absolutePath.contains("/modules/private/apps/")) {

			return content;
		}

		for (String dependencies : _getDependenciesBlocks(content)) {
			content = _formatDependencies(fileName, content, dependencies);
		}

		return content;
	}

	private String _formatDependencies(
		String fileName, String content, String dependencies) {

		int x = dependencies.indexOf("\n");
		int y = dependencies.lastIndexOf("\n");

		if (x == y) {
			return content;
		}

		dependencies = dependencies.substring(x, y + 1);

		StringBundler sb = new StringBundler();

		for (String line : StringUtil.splitLines(dependencies)) {
			String configuration = GradleSourceUtil.getConfiguration(
				StringUtil.trim(line));

			if (!StringUtil.startsWith(configuration, "test")) {
				sb.append(line);
				sb.append("\n");

				continue;
			}

			String dependencyName = _getDependencyName(line);

			if (dependencyName.equals("com.liferay.portal.impl") ||
				dependencyName.equals("com.liferay.portal.kernel") ||
				dependencyName.equals("com.liferay.portal.test") ||
				dependencyName.equals("com.liferay.portal.test.integration") ||
				dependencyName.equals("com.liferay.util.bridges") ||
				dependencyName.equals("com.liferay.util.java") ||
				dependencyName.equals("com.liferay.util.taglib")) {

				String dependencyVersion = _getDependencyVersion(line);

				if (dependencyVersion.equals("default")) {
					sb.append(line);
				}
				else {
					String newLine = StringUtil.replaceFirst(
						line, "version: \"" + dependencyVersion + "\"",
						"version: \"default\"");

					sb.append(newLine);
				}

				sb.append("\n");

				continue;
			}

			if (dependencyName.startsWith("com.liferay.") &&
				!line.contains("project(\"") &&
				!_allowedDependencyNames.contains(dependencyName)) {

				int lineNumber = getLineNumber(content, content.indexOf(line));

				addMessage(
					fileName,
					"Use a project dependency instead of a module dependency",
					lineNumber);
			}

			sb.append(line);
			sb.append("\n");
		}

		return StringUtil.replace(
			content, StringUtil.trim(dependencies),
			StringUtil.trim(sb.toString()));
	}

	private List<String> _getDependenciesBlocks(String content) {
		List<String> dependenciesBlocks = new ArrayList<>();

		Matcher matcher = _dependenciesPattern.matcher(content);

		while (matcher.find()) {
			int y = matcher.start();

			while (true) {
				y = content.indexOf("}", y + 1);

				if (y == -1) {
					return dependenciesBlocks;
				}

				String dependencies = content.substring(
					matcher.start(2), y + 1);

				int level = getLevel(dependencies, "{", "}");

				if (level == 0) {
					if (!dependencies.contains("}\n")) {
						dependenciesBlocks.add(dependencies);
					}

					break;
				}
			}
		}

		return dependenciesBlocks;
	}

	private String _getDependencyName(String dependency) {
		Matcher matcher = _dependencyNamePattern.matcher(dependency);

		if (!matcher.find()) {
			return StringPool.BLANK;
		}

		return matcher.group(1);
	}

	private String _getDependencyVersion(String dependency) {
		Matcher matcher = _dependencyVersionPattern.matcher(dependency);

		if (!matcher.find()) {
			return StringPool.BLANK;
		}

		return matcher.group(1);
	}

	private static final Pattern _dependenciesPattern = Pattern.compile(
		"(\n|\\A)(\t*)dependencies \\{\n");
	private static final Pattern _dependencyNamePattern = Pattern.compile(
		".*, name: \"([^\"]*)\".*");
	private static final Pattern _dependencyVersionPattern = Pattern.compile(
		".*, version: \"([^\"]*)\".*");

	private final List<String> _allowedDependencyNames = new ArrayList<>();

}
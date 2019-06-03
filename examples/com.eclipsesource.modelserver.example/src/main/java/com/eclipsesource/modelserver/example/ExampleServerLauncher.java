/********************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package com.eclipsesource.modelserver.example;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import com.eclipsesource.modelserver.emf.ModelServer;
import com.eclipsesource.modelserver.emf.launch.ModelServerLauncher;
import com.eclipsesource.modelserver.example.util.ResourceUtil;
import com.google.common.collect.Lists;

public class ExampleServerLauncher {
	private static String WORKSPACE_ROOT = ".temp/workspace";
	private static Logger LOG = Logger.getLogger(ExampleServerLauncher.class);

	public static void main(String[] args) throws IOException {
		BasicConfigurator.configure();

		final ModelServerLauncher launcher = new ModelServerLauncher();
		final File workspaceRoot = new File(WORKSPACE_ROOT);
		if (!setupTempTestWorkspace(workspaceRoot)) {
			LOG.error("Could not setup test workspace");
			System.exit(0);
		}
		launcher.setWorkspaceRoot(workspaceRoot.getAbsolutePath());
		launcher.setModules(Lists.newArrayList(new ExampleModelServerModule()));
		launcher.start();

		final ModelServer server = launcher.getInjector().getInstance(ModelServer.class);
		server.initialize();
		server.loadModel("SuperBrewer3000.xmi");
	}

	private static boolean setupTempTestWorkspace(File workspaceRoot) throws IOException {
		if (workspaceRoot.exists()) {
			FileUtils.deleteDirectory(workspaceRoot);
		}
		workspaceRoot.mkdirs();
		boolean result = ResourceUtil.copyFromResource("workspace/Coffe.ecore", new File(workspaceRoot, "Coffe.ecore"));
		result = result && ResourceUtil.copyFromResource("workspace/SuperBrewer3000.xmi",
				new File(workspaceRoot, "SuperBrewer3000.xmi"));
		result = result && ResourceUtil.copyFromResource("workspace/SuperBrewer3000.json",
				new File(workspaceRoot, "SuperBrewer3000.json"));
		return result;
	}

}

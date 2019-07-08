/*******************************************************************************
 * Copyright (c) 2019 EclipseSource and others.
 *
 *   This program and the accompanying materials are made available under the
 *   terms of the Eclipse Public License v. 2.0 which is available at
 *   http://www.eclipse.org/legal/epl-2.0.
 *
 *   This Source Code may also be made available under the following Secondary
 *   Licenses when the conditions for such availability set forth in the Eclipse
 *   Public License v. 2.0 are satisfied: GNU General Public License, version 2
 *   with the GNU Classpath Exception which is available at
 *   https://www.gnu.org/software/classpath/license.html.
 *
 *   SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 *******************************************************************************/
package com.eclipsesource.modelserver.emf.di;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.eclipsesource.modelserver.common.AppEntryPoint;
import com.eclipsesource.modelserver.common.EntryPointType;
import com.eclipsesource.modelserver.common.Routing;
import com.eclipsesource.modelserver.emf.ResourceManager;
import com.eclipsesource.modelserver.emf.common.ModelController;
import com.eclipsesource.modelserver.emf.common.ModelServerRouting;
import com.eclipsesource.modelserver.emf.common.SchemaController;
import com.eclipsesource.modelserver.emf.configuration.EPackageConfiguration;
import com.eclipsesource.modelserver.emf.configuration.EcorePackageConfiguration;
import com.eclipsesource.modelserver.emf.configuration.ServerConfiguration;
import com.eclipsesource.modelserver.emf.launch.ModelServerEntryPoint;
import com.eclipsesource.modelserver.emf.launch.ModelServerStartup;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;

import io.javalin.Javalin;

public class ModelServerModule extends AbstractModule {

	private Javalin app;
	private static final Logger LOG = Logger.getLogger(ModelServerModule.class);
	protected Multibinder<EPackageConfiguration> ePackageConfigurationBinder;
	protected ArrayList<Class<? extends EPackageConfiguration>> ePackageConfigurations;

	protected ModelServerModule() {
		ePackageConfigurations = Lists.newArrayList(EcorePackageConfiguration.class);
	}

	private ModelServerModule(Javalin app) {
		this();
		this.app = app;
	}

	public static ModelServerModule create() {
		return new ModelServerModule(Javalin.create(config -> {
			config.enableCorsForAllOrigins();
			config.requestLogger((ctx, ms) -> {
				LOG.info(ctx.method() + " " + ctx.path() + " took " + ms + " ms");
			});
		}));
	}

	@Override
	protected void configure() {
		bind(ServerConfiguration.class).in(Singleton.class);
		ePackageConfigurationBinder = Multibinder.newSetBinder(binder(), EPackageConfiguration.class);
		bind(ResourceManager.class).in(Singleton.class);
		ePackageConfigurations.forEach(c -> ePackageConfigurationBinder.addBinding().to(c));

		bind(Javalin.class).toInstance(this.app);
		bind(ModelServerStartup.class).in(Singleton.class);
		bind(ModelController.class).in(Singleton.class);
		bind(SchemaController.class).in(Singleton.class);
		Multibinder.newSetBinder(binder(), Routing.class).addBinding().to(ModelServerRouting.class).in(Singleton.class);
		MapBinder.newMapBinder(binder(), EntryPointType.class, AppEntryPoint.class).addBinding(EntryPointType.REST)
				.to(ModelServerEntryPoint.class);
	}

	public void addEPackageConfigurations(Collection<Class<? extends EPackageConfiguration>> configs) {
		configs.forEach(c -> ePackageConfigurations.add(c));
	}
}

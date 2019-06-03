package com.eclipsesource.modelserver.example;

import java.util.Collection;

import com.eclipsesource.modelserver.emf.configuration.EPackageConfiguration;
import com.eclipsesource.modelserver.emf.di.EMFModelServerModule;

public class ExampleModelServerModule extends EMFModelServerModule {

	@Override
	protected Collection<Class<? extends EPackageConfiguration>> bindEPackageConfigurations() {
		Collection<Class<? extends EPackageConfiguration>> configs = super.bindEPackageConfigurations();
		configs.add(CoffeeConfiguration.class);
		return configs;
	}

}

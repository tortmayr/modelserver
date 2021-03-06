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
package com.eclipsesource.modelserver.emf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eclipsesource.modelserver.emf.di.ModelServerModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class ResourceManagerTest extends AbstractResourceTest {

	private static ResourceManager resourceManager;

	@BeforeClass
	public static void beforeClass() {
		Injector injector = Guice.createInjector(ModelServerModule.create());
		resourceManager = injector.getInstance(ResourceManager.class);
	}

	@Test
	public void testLoadModel_castToExactType() {
		String resourceURI = toFullPath("Test1.ecore");
		Optional<EPackage> result = resourceManager.loadModel(resourceURI, resourceSet, EPackage.class);
		assertNotNull(result);
		assertTrue(result.isPresent());
		assertEquals("test1", result.get().getName());
	}

	@Test
	public void testLoadModel_castToSupertype() {
		String resourceURI = toFullPath("Test1.ecore");
		Optional<EObject> result = resourceManager.loadModel(resourceURI, resourceSet, EObject.class);
		assertNotNull(result);
		assertTrue(result.isPresent());
	}

	@Test
	public void testLoadModel_invalidCast() {
		String resourceURI = toFullPath("Test1.ecore");
		Optional<EClass> result = resourceManager.loadModel(resourceURI, resourceSet, EClass.class);

		assertNotNull(result);
		assertFalse(result.isPresent());
	}

	@Test
	public void testLoadModel_fromJson() throws IOException {
		Resource expectedResource = loadResource("Test1.ecore");
		Optional<Resource> result = resourceManager.loadResource(toFullPath("Test1.json"), resourceSet);
		assertTrue(result.isPresent());
		assertTrue(EcoreUtil.equals(expectedResource.getContents(), result.get().getContents()));

	}
}

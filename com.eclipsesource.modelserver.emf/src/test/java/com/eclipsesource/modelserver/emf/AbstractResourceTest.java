package com.eclipsesource.modelserver.emf;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emfjson.jackson.resource.JsonResourceFactory;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractResourceTest {
	public static final String RESOURCE_PATH = "src/test/resources/";
	protected ResourceSetImpl resourceSet;

	@Before
	public void initializeResourceSet() {
		resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json",
				new JsonResourceFactory(EmfJsonConverter.setupDefaultMapper()));
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	}

	protected Resource loadResource(String file) throws IOException {
		Resource resource = resourceSet.createResource(URI.createFileURI(toFullPath(file)));
		resource.load(Collections.EMPTY_MAP);
		return resource;
	}

	protected String toFullPath(String file) {
		return RESOURCE_PATH + file;
	}

	@After
	public void tearDownResourceSet() {
		if (resourceSet != null) {
			resourceSet.getResources().stream().forEach(Resource::unload);
		}
	}

}

package com.eclipsesource.modelserver.emf.configuration;

import java.util.Collection;
import java.util.Optional;

import org.eclipse.emf.ecore.resource.Resource;

public interface EPackageConfiguration {
	/**
	 * Unique identifier for the EPackage (typically the NS-URI)
	 *
	 * @return Id as String
	 */
	String getId();

	/**
	 * Getter for a collection of file extensions that are related to this language
	 *
	 * @return Collection of file extensions.
	 */
	Collection<String> getFileExtensions();

	/**
	 * Initializes and registers the ePackage. Typically this is is achieved by
	 * calling eINSTANCE.eClass() of the corresponding EPackage implementation.
	 *
	 */
	void registerEPackage();

	/**
	 * Optional ResourceFactory that is needed to load this EPackage from a resource
	 * with a certain extension
	 *
	 * @param extension the file extension
	 * @return ResourceFactory that is needed for this EPackage or empty optional if
	 *         the default XMI Factory can be used
	 */
	default Optional<Resource.Factory> getResourceFactory(String extension) {
		return Optional.empty();
	}
}
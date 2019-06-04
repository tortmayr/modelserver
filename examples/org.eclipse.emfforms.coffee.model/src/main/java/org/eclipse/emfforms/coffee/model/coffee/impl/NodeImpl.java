/**
 * Copyright (c) 2011-2018 EclipseSource Muenchen GmbH and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * EclipseSource Munich - initial API and implementation
 */
package org.eclipse.emfforms.coffee.model.coffee.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emfforms.coffee.model.coffee.CoffeePackage;
import org.eclipse.emfforms.coffee.model.coffee.Node;

/**
 * <!-- begin-user-doc --> An implementation of the model object
 * '<em><b>Node</b></em>'. <!-- end-user-doc -->
 *
 * @generated
 */
public abstract class NodeImpl extends MinimalEObjectImpl.Container implements Node {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	protected NodeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CoffeePackage.Literals.NODE;
	}

} // NodeImpl

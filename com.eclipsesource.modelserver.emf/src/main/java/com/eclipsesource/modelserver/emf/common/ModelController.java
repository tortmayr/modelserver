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
package com.eclipsesource.modelserver.emf.common;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import com.eclipsesource.modelserver.command.CCommand;
import com.eclipsesource.modelserver.common.codecs.DecodingException;
import com.eclipsesource.modelserver.common.codecs.EMFJsonConverter;
import com.eclipsesource.modelserver.common.codecs.EncodingException;
import com.eclipsesource.modelserver.emf.common.codecs.Codecs;
import com.eclipsesource.modelserver.emf.common.codecs.JsonCodec;
import com.eclipsesource.modelserver.jsonschema.Json;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.plugin.json.JavalinJackson;

public class ModelController {

	private static final Logger LOG = Logger.getLogger(ModelController.class.getSimpleName());

	private ModelRepository modelRepository;
	private SessionController sessionController;
	private Codecs codecs;

	@Inject public ModelController(ModelRepository modelRepository, SessionController sessionController) {
		JavalinJackson.configure(EMFJsonConverter.setupDefaultMapper());
		codecs = new Codecs();
		this.modelRepository = modelRepository;
		this.sessionController = sessionController;
	}

	public void create(Context ctx, String modeluri) {
		getContents(readResource(ctx, modeluri)).ifPresentOrElse(
				eObject -> {
					this.modelRepository.addModel(modeluri, eObject);
					try {
						final JsonNode encoded = codecs.encode(ctx, eObject);
						ctx.json(JsonResponse.data(encoded));
						this.sessionController.modelChanged(modeluri);
					} catch (EncodingException ex) {
						handleEncodingError(ctx, ex);
					}
				},
				() -> handleError(ctx, 400, "Create new model failed")
		);
	}

	public void delete(Context ctx, String modeluri) {
		if (this.modelRepository.hasModel(modeluri)) {
			this.modelRepository.removeModel(modeluri);
			ctx.json(JsonResponse.confirm("Model '" + modeluri + "' successfully deleted"));
			this.sessionController.modelDeleted(modeluri);
		} else {
			handleError(ctx, 404, "Model '" + modeluri + "' not found, cannot be deleted!");
		}
	}

	public void getAll(Context ctx) {
		final Map<URI, EObject> allModels = this.modelRepository.getAllModels();
		try {
			Map<URI, JsonNode> encodedEntries = Maps.newLinkedHashMap();
			for (Map.Entry<URI, EObject> entry : allModels.entrySet()) {
				final JsonNode encoded = codecs.encode(ctx, entry.getValue());
				encodedEntries.put(entry.getKey(), encoded);
			}
			ctx.json(JsonResponse.data(JsonCodec.encode(encodedEntries)));
		} catch (EncodingException ex) {
			handleEncodingError(ctx, ex);
		}
	}

	public void getOne(Context ctx, String modeluri) {
		this.modelRepository.getModel(modeluri).ifPresentOrElse(
			model -> {
				if (model == null) {
					ctx.json(JsonResponse.data(Json.text("")));
				} else {
					try {
						ctx.json(JsonResponse.data(codecs.encode(ctx, model)));
					} catch (EncodingException ex) {
						handleEncodingError(ctx, ex);
					}
				}
			},
			() -> handleError(ctx, 404, "Model '" + modeluri + "' not found!")
		);
	}

	public void update(Context ctx, String modeluri) {
		Optional<Resource> resource = readResource(ctx, "temp$update.json");
		getContents(resource).ifPresentOrElse(
			eObject -> {
				modelRepository.updateModel(modeluri, eObject);
				try {
					ctx.json(JsonResponse.data(codecs.encode(ctx, eObject)));
				} catch (EncodingException e) {
					handleEncodingError(ctx, e);
				} finally {
					// Ditch the temporary resource
					resource.get().getResourceSet().getResources().remove(resource.get());
				}
				sessionController.modelChanged(modeluri);
			},
			() -> handleError(ctx, 400, "Update model failed")
		);
	}

	public Handler modelUrisHandler = ctx -> ctx.json(JsonResponse.data(JsonCodec.encode(this.modelRepository.getAllModelUris())));

	private Optional<Resource> readResource(Context ctx, String modelURI) {
		try {
			JsonNode json = JavalinJackson.getObjectMapper().readTree(ctx.body());
			if (!json.has("data")) {
				handleError(ctx, 400, "Empty JSON");
				return Optional.empty();
			}
			JsonNode jsonDataNode =  json.get("data");
			String jsonData = !jsonDataNode.asText().isEmpty() ? jsonDataNode.asText() : jsonDataNode.toString();
			if (jsonData.equals("{}")) {
				handleError(ctx, 400, "Empty JSON");
				return Optional.empty();
			}
			return codecs.decode(ctx, modelRepository.getResourceSet(), modelURI, jsonData);
		} catch (DecodingException | IOException e) {
			handleError(ctx, 400, "Invalid JSON");
		}
		return Optional.empty();
	}
	
	/**
	 * Get the first {@link EObject} in a {@code resource}'s {@link Resource#getContents() contents}, or
	 * an {@linkplain Optional#empty() empty optional} if the {@code resource} is empty.
	 * 
	 * @param resource an optional resource from which to get its optional content
	 * @return the optional content, or empty if no resource or no content in the resource
	 */
	private Optional<EObject> getContents(Optional<Resource> resource) {
		return resource.map(r -> r.getContents().isEmpty() ? null : r.getContents().get(0));
	}

	public void executeCommand(Context ctx, String modeluri) {
		this.modelRepository.getModel(modeluri).ifPresentOrElse(
				model -> {
					if (model == null) {
						handleError(ctx, 404, String.format("Model '%s' not found!", modeluri));
					} else {
							String commandURI = "command$1.command";
							Optional<Resource> resource = readResource(ctx, commandURI);
							getContents(resource).filter(CCommand.class::isInstance)//
									.map(CCommand.class::cast) //
									.ifPresent(cmd -> {
										try {
											modelRepository.updateModel(modeluri, cmd);
											sessionController.modelChanged(modeluri, cmd);
											ctx.json(JsonResponse.success());
										} catch (DecodingException e) {
											handleDecodingError(ctx, e);
										} finally {
											// Ditch the temporary resource
											resource.get().getResourceSet().getResources().remove(resource.get());
										}
									});
							ctx.json(JsonResponse.confirm("Model '" + modeluri + "' successfully updated"));

					}
				},
				() -> handleError(ctx, 404, String.format("Model '%s' not found!", modeluri))
			);
	}
	
	private void handleEncodingError(Context context, EncodingException ex) {
		handleError(context, 500, "An error occurred during data encoding", ex);
	}
	
	private void handleDecodingError(Context context, DecodingException ex) {
		handleError(context, 500, "An error occurred during data decoding", ex);
	}

	private void handleError(Context ctx, int statusCode, String errorMsg) {
		LOG.error(errorMsg);
		ctx.status(statusCode).json(JsonResponse.error(errorMsg));
	}

	private void handleError(Context ctx, int statusCode, String errorMsg, Exception e) {
		LOG.error(errorMsg, e);
		ctx.status(statusCode).json(JsonResponse.error(errorMsg));
	}
}

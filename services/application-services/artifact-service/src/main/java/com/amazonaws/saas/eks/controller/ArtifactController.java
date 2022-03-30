/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.amazonaws.saas.eks.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.saas.eks.auth.TokenManager;
import com.amazonaws.saas.eks.model.Artifact;
import com.amazonaws.saas.eks.service.ArtifactService;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class ArtifactController {
	private static final Logger logger = LogManager.getLogger(ArtifactController.class);

	@Autowired
	private ArtifactService artifactService;

	@Autowired
	private TokenManager tokenManager;

	/**
	 * Method to retrieve all artifacts for a tenant
	 * 
	 * @param request
	 * @return List<Artifact>
	 */
	@GetMapping(value = "{companyName}/artifact/api/artifacts", produces = { MediaType.APPLICATION_JSON_VALUE })
	public List<Artifact> getArtifacts(HttpServletRequest request) {
		logger.info("Return artifacts");
		String tenantId = null;
		List<Artifact> artifacts = null;

		try {
			tenantId = tokenManager.getTenantId(request);
			
			if (tenantId != null && !tenantId.isEmpty()) {
				artifacts =  artifactService.getArtifacts(tenantId);
				return artifacts;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-get artifacts failed: ", e);
			return null;
		}

		return artifacts;
	}

	/**
	 * Method to get Artifact by id for a tenant
	 * 
	 * @param artifactId
	 * @param request
	 * @return Artifact
	 */
	@GetMapping(value = "{companyName}/artifact/api/artifact/{artifactId}", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Artifact getArtifactById(@PathVariable("artifactId") String artifactId, HttpServletRequest request) {
		String tenantId = null;
		Artifact artifact = null;
		
		try {
			tenantId = tokenManager.getTenantId(request);
			
			if (tenantId != null && !tenantId.isEmpty()) {
				artifact = artifactService.getArtifactById(artifactId, tenantId);
				return artifact;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-get artifact by ID failed: ", e);
			return null;
		}

		return artifact;
	}

	/**
	 * Method to save an artifact for a tenant
	 * 
	 * @param artifact
	 * @param request
	 * @return Artifact
	 */
	@PostMapping(value = "{companyName}/artifact/api/artifact", produces = { MediaType.APPLICATION_JSON_VALUE })
	public Artifact saveArtifact(@RequestBody Artifact artifact, HttpServletRequest request) {
		String tenantId = null;
		Artifact newArtifact = null;
		
		try {
			tenantId = tokenManager.getTenantId(request);
			if (tenantId != null && !tenantId.isEmpty()) {
				newArtifact = artifactService.save(artifact, tenantId);
				return newArtifact;
			}
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-save artifact failed: ", e);
			return null;
		}

		return newArtifact;
	}

	@RequestMapping("{companyName}/artifact/health/artifact")
	public String health() {
		return "\"Artifact service is up!\"";
	}

}
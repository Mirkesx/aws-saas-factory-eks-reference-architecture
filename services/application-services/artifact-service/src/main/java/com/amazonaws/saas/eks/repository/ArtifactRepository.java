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
package com.amazonaws.saas.eks.repository;

import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.saas.eks.model.Artifact;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedScanList;

@Repository
public class ArtifactRepository {
	private static final Logger logger = LogManager.getLogger(ArtifactRepository.class);

	/**
	 * Method to get all artifacts for a tenant
	 * @param tenantId
	 * @return List<Artifact>
	 */
	public List<Artifact> getArtifacts(String tenantId) {
		PaginatedScanList<Artifact> results = null;
		DynamoDBMapper mapper = dynamoDBMapper(tenantId);
		
		try {
			results = mapper.scan(Artifact.class, new DynamoDBScanExpression());
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Get Artifacts failed " + e.getMessage());
		}

		return results;
	}

	/**
	 * Method to save an artifact for a tenant
	 * @param artifact
	 * @param tenantId
	 * @return Artifact
	 */
	public Artifact save(Artifact artifact, String tenantId) {
		try {
			DynamoDBMapper mapper = dynamoDBMapper(tenantId);
			mapper.save(artifact);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Save Artifact failed " + e.getMessage());
		}
		
		return artifact;
	}

	/**
	 * Method to get artifact by Id for a tenant
	 * @param artifactId
	 * @param tenantId
	 * @return Artifact
	 */
	public Artifact getArtifactById(String artifactId, String tenantId) {
		DynamoDBMapper mapper = dynamoDBMapper(tenantId);
		Artifact artifact = null;
		
		DynamoDBMapperConfig config = DynamoDBMapperConfig.builder()
				.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.CONSISTENT).build();
		try {
			artifact = mapper.load(Artifact.class, artifactId, config);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Get Artifact By Id failed " + e.getMessage());
		}
		
		return artifact;
	}

	/**
	 * Method to delete a tenant's artifact
	 * @param artifact
	 * @param tenantId
	 */
	public void delete(Artifact artifact, String tenantId) {
		try {
			DynamoDBMapper mapper = dynamoDBMapper(tenantId);
			mapper.delete(artifact);
		} catch (Exception e) {
			logger.error("TenantId: " + tenantId + "-Delete Artifact failed " + e.getMessage());
		}
	}
	
	/**
	 * Method to retrieve DynamoDBMapper and access to the tenant's Artifact table
	 * @param tenantId
	 * @return DynamoDBMapper
	 */
	public DynamoDBMapper dynamoDBMapper(String tenantId) {
		String tableName = "Artifact-" + tenantId;
		DynamoDBMapperConfig dbMapperConfig = new DynamoDBMapperConfig.Builder()
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName)).build();

		AmazonDynamoDBClient dynamoClient = getAmazonDynamoDBLocalClient(tenantId);
		return new DynamoDBMapper(dynamoClient, dbMapperConfig);
	}

	/**
	 * Helper method for DynamoDBMapper
	 * @param tenantId
	 * @return AmazonDynamoDBClient
	 */
	private AmazonDynamoDBClient getAmazonDynamoDBLocalClient(String tenantId) {
		return (AmazonDynamoDBClient) AmazonDynamoDBClientBuilder.standard()
				// .withCredentials(WebIdentityTokenCredentialsProvider.builder().roleSessionName("ddb-query").build())
				.withCredentials(new DefaultAWSCredentialsProviderChain()).build();
	}

}

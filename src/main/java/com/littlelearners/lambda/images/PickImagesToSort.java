package com.littlelearners.lambda.images;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.littlelearners.common.utils.CommonUtils;

public class PickImagesToSort {
	
	static StringBuilder baseUrl;

    public ImageResponse handleRequest(Map<String,String> input, Context context) {
    	context.getLogger().log("Input: " + input);
    	
        
        baseUrl = new StringBuilder();
        
        String region = System.getenv("AWS_REGION")!=null?System.getenv("AWS_REGION"):input.get("region");
        String bucket_name = System.getenv("BUCKET_NAME")!=null?System.getenv("BUCKET_NAME"):input.get("bucketname");
        String prefix = System.getenv("PREFIX")!=null?System.getenv("PREFIX"):input.get("prefix");
        String sortType = System.getenv("SORTTYPE")!=null?System.getenv("SORTTYPE"):input.get("sortType");
        List<String> subPrefixes = input.get("subPrefixes")!=null?Arrays.asList(input.get("subPrefixes").split(",")):null;
        
        int numberOfObjects = Integer.valueOf(input.get("count"));
        
        baseUrl.append("https://");
        baseUrl.append(bucket_name);
        baseUrl.append(".s3.amazonaws.com/");
        
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
        
        ImageResponse response;
        try {
	        if(sortType.equalsIgnoreCase("IDENTICAL")) {
	        	 response = getImageUrlsForIdenticalSort(s3, context, bucket_name,prefix, numberOfObjects);
	        }else if(sortType.equalsIgnoreCase("NON-IDENTICAL")) {
	        	 response = getImageUrlsForNonIdenticalSort(s3, context, bucket_name,prefix, subPrefixes, numberOfObjects);
	        }else if(sortType.equalsIgnoreCase("CATEGORY")){
	        	 response = getImageUrlsForCategorySort(s3, context, bucket_name, prefix, subPrefixes, numberOfObjects);
	        }else {
	        	return null;
	        }
	        return response;
        }	
        catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    
    ImageResponse getImageUrlsForIdenticalSort(AmazonS3 s3, Context context, String bucket_name, 
    										   String prefix, int numberOfObjects) {
    	ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
    			.withBucketName(bucket_name)
    			.withPrefix(prefix + "/");
    	ListObjectsV2Result result = s3.listObjectsV2(listObjectsRequest);
    	List<S3ObjectSummary> objects = result.getObjectSummaries();
    	try {
    		List<String> objectNames = CommonUtils.pickRandom(objects, numberOfObjects);
    		objectNames = objectNames.stream()
    				.map(i->baseUrl+i)
    				.collect(Collectors.toList());
    		return new ImageResponse(objectNames);
    	}catch(Exception e) {
    		context.getLogger().log(e.getMessage());
    		return null;
    	}
    }
    
    ImageResponse getImageUrlsForNonIdenticalSort(AmazonS3 s3, Context context, String bucket_name, 
			   String prefix, List<String> subPrefixes, int numberOfObjects) {
    	try {
    		int limitPerSet = numberOfObjects/subPrefixes.size();
    		List<String> finalObjectNamesList = new ArrayList<>();
    		subPrefixes.forEach(subPrefix->{
    			
    			StringBuilder fullPrefix = new StringBuilder(prefix + "/" + subPrefix + "/");
    			ListObjectsV2Request listCPRequest = new ListObjectsV2Request()
											    	    			.withBucketName(bucket_name)
											    	    			.withPrefix(fullPrefix.toString());
											  
    	    	ListObjectsV2Result subPrefixesResult = s3.listObjectsV2(listCPRequest);
    	    	List<S3ObjectSummary> all = subPrefixesResult.getObjectSummaries();
    	    	List<S3ObjectSummary> availableSubPrefixes = all.stream().filter(os -> os.getKey()
    	    												 			 .codePoints()
    	    												 			 .filter(ch -> ch == '/')
    	    												 			 .count()== 3)
    	    													.filter(os -> os.getKey().endsWith("/"))
    	    												 	.collect(Collectors.toList());
    	    	
    	    	//Pick one of the prefixes randomly
    	    	Random rand = new Random();
    	        String aRandomSubPrefix = availableSubPrefixes.get(rand.nextInt(availableSubPrefixes.size())).getKey();
    	        
    	        ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
											    	        		.withBucketName(bucket_name)
											    	        		.withPrefix(aRandomSubPrefix);

    	        ListObjectsV2Result result = s3.listObjectsV2(listObjectsRequest);
    	        List<S3ObjectSummary> objects = result.getObjectSummaries();

    	        List<String> objectNames = CommonUtils.pickRandom(objects, limitPerSet);
    	        objectNames = objectNames.stream()
    	        		.map(i->baseUrl+i)
    	        		.collect(Collectors.toList());
    	        finalObjectNamesList.addAll(objectNames);
    		});
    		Collections.shuffle(finalObjectNamesList);
    		return new ImageResponse(finalObjectNamesList);
    	}catch(Exception e) {
    		context.getLogger().log(e.getMessage());
    		return null;
    	}
    }
    
    ImageResponse getImageUrlsForCategorySort(AmazonS3 s3, Context context, String bucket_name, 
			   String prefix, List<String> subPrefixes, int numberOfObjects) {
    	try {
    		int limitPerSet = numberOfObjects/subPrefixes.size();
    		List<String> finalObjectNamesList = new ArrayList<>();
    		subPrefixes.forEach(subPrefix->{
    			
    			String fullPrefix = prefix + "/" + subPrefix + "/";
    			ListObjectsV2Request listObjectsRequest = new ListObjectsV2Request()
											    	    			.withBucketName(bucket_name)
											    	    			.withPrefix(fullPrefix);
											  
    	    	ListObjectsV2Result result = s3.listObjectsV2(listObjectsRequest);
    	    	List<S3ObjectSummary> objects = result.getObjectSummaries();
    	    	
    	    	List<String> objectNames = CommonUtils.pickRandom(objects, limitPerSet);
    	    	objectNames = objectNames.stream()
    	    				.map(i->baseUrl+i)
    	    				.collect(Collectors.toList());
    	    	finalObjectNamesList.addAll(objectNames);
    		});
    		Collections.shuffle(finalObjectNamesList);
    		return new ImageResponse(finalObjectNamesList);
    	}catch(Exception e) {
    		context.getLogger().log(e.getMessage());
    		return null;
    	}
    }
}
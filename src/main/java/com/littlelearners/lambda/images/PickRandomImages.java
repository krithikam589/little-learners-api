package com.littlelearners.lambda.images;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.littlelearners.common.utils.CommonUtils;

public class PickRandomImages{
	
    public ImageResponse handleRequest(Map<String,String> input, Context context) {
        context.getLogger().log("Input: " + input);
        
        StringBuilder baseUrl = new StringBuilder();
        
        String region = System.getenv("AWS_REGION")!=null?System.getenv("AWS_REGION"):input.get("region");
        String bucket_name = System.getenv("BUCKET_NAME")!=null?System.getenv("BUCKET_NAME"):input.get("bucketname");
        String prefix = System.getenv("PREFIX")!=null?System.getenv("PREFIX"):input.get("prefix");
        
        
        baseUrl.append("https://");
        baseUrl.append(bucket_name);
        baseUrl.append(".s3.amazonaws.com/");
        
        int numberOfObjects = Integer.valueOf(input.get("count"));
        final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
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

}

package com.littlelearners.common.utils;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class CommonUtils {
	public static List<String> pickRandom(List<S3ObjectSummary> list, int n) {
	    if (n > list.size()) {
	        throw new IllegalArgumentException("not enough elements");
	    }
	    List<S3ObjectSummary> filteredList = list.stream()
	    										.filter(a-> !a.getKey().endsWith("/"))
	    										.collect(Collectors.toList());
	    Random random = new Random();
	    return IntStream
	            .generate(() -> random.nextInt(filteredList.size()))
	            .distinct()
	            .limit(n)
	            .mapToObj(filteredList::get)
	            .map(o->o.getKey())
	            .collect(Collectors.toList());
	}
}

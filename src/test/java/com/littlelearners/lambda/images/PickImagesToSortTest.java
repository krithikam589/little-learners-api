package com.littlelearners.lambda.images;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */

public class PickImagesToSortTest {

    private final String CONTENT_TYPE = "image/jpeg";
    private Map<String,String> input;

    
    @Before
    public void setUp() throws IOException {
        input = new HashMap<>();
        // TODO: set up your sample input object here.
        input =  Stream.of(new String[][]{
                    {"bucketname", "littlelearnersimages"},
                    {"count", "10"},
                    {"region", "us-east-1"},
                    {"prefix","sort"},
                    {"subPrefixes","fruits,vegetables"},
                    {"sortType","NON-IDENTICAL"}
        		}).collect(Collectors.toMap(p -> p[0], p -> p[1]));

    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testPickImagesToSort() {
        PickImagesToSort handler = new PickImagesToSort();
        Context ctx = createContext();

        ImageResponse output = handler.handleRequest(input, ctx);
        System.out.println(input);
        output.getImageUrls().forEach(p->System.out.println(p));

        // TODO: validate output here if needed.
        Assert.assertThat(output.getImageUrls().toString(), CoreMatchers.containsString(","));

    }
}

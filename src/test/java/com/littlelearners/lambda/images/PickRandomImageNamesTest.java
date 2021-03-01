package com.littlelearners.lambda.images;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.amazonaws.services.lambda.runtime.Context;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
public class PickRandomImageNamesTest {

    private static Map<String,String> input;
    
    @BeforeClass
    public static void createInput() throws IOException {
        // TODO: set up your sample input object here.
        input =  Stream.of(new String[][]{
                    {"bucketname", "littlelearnersimages"},
                    {"count", "4"},
                    {"region", "us-east-1"},
                    {"prefix","nouns"}
        		}).collect(Collectors.toMap(p -> p[0], p -> p[1]));
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        // TODO: customize your context here if needed.
        ctx.setFunctionName("Your Function Name");

        return ctx;
    }

    @Test
    public void testPickRandomImageNames() {
        PickRandomImages handler = new PickRandomImages();
        Context ctx = createContext();

        System.out.println(input);
        ImageResponse output = handler.handleRequest(input, ctx);
        System.out.println(output.getImageUrls());

        // TODO: validate output here if needed.
        Assert.assertThat(output.getImageUrls().toString(), CoreMatchers.containsString(","));
    }
}

package com.zebrunner.carina.demo;

import com.amazonaws.services.s3.model.S3Object;
import com.zebrunner.carina.amazon.AmazonS3Manager;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URL;

public class SimpleTest {
    @Test(invocationCount = 100, threadPoolSize = 10)
    public void testgeneratePreSignURL() {
        URL presigned = AmazonS3Manager.getInstance()
                .generatePreSignUrl("zbr-test", "carinademoexample.apk", 60_000);
        System.out.println(presigned);
    }

    @Test(invocationCount = 1000, threadPoolSize = 10)
    public void testGetDirectLink() {
        String link = AmazonS3Manager.getInstance()
                .getDirectLink("s3://zbr-test/carinademoexample.apk");
        System.out.println(link);
    }

    @Test(invocationCount = 100, threadPoolSize = 10)
    public void getS3Object() throws IOException {
        S3Object s3Object = AmazonS3Manager.getInstance().get("zbr-test","carinademoexample.apk");
        String key = s3Object.getKey();
        System.out.println(key);
    }
}

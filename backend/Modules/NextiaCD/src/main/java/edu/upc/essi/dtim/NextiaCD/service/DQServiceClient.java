package edu.upc.essi.dtim.NextiaCD.service;

import okhttp3.*;
import java.io.File;
import java.io.IOException;
import java.time.Duration;

public class DQServiceClient {

    private final String endpointUrl;
    private final OkHttpClient client;

    public DQServiceClient(String endpointUrl) {
        this.endpointUrl = endpointUrl;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofSeconds(30))       // reasonable connection time
                .readTimeout(Duration.ofMinutes(10))          // allow 10 minutes for response
                .writeTimeout(Duration.ofMinutes(5))          // allow large file uploads
                .callTimeout(Duration.ofMinutes(15))          // full time end-to-end
                .build();
    }

    public String sendCsvFile(File csvFile) throws IOException {
        MediaType mediaType = MediaType.parse("text/csv");

        RequestBody fileBody = RequestBody.create(csvFile, mediaType);

        MultipartBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", csvFile.getName(), fileBody)
                .build();

        Request request = new Request.Builder()
                .url(endpointUrl)
                .post(requestBody)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected HTTP code: " + response.code());
            }
            return response.body().string();
        }
    }

    // main method for testing
    public static void main(String[] args) throws IOException {
        DQServiceClient client = new DQServiceClient("http://localhost:5050/discover");
        String result = client.sendCsvFile(new File("/Users/anbipa/Desktop/DTIM/Cyclops/DQRuleDiscovery/data/input.csv"));
        System.out.println(result);
    }


}




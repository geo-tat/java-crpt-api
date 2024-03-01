package ru.selsup.homework;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Getter
@Setter
public class CrptApi {

    private final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    Duration duration;
    private final Gson gson = new GsonBuilder().create();
    private final Semaphore semaphore;
    private final HttpClient httpClient;
    private final ExecutorService executor;
    int requestLimit;
    private int requestCount = 0;

    private Instant lastRequestTime = Instant.MIN;

    public CrptApi(Duration duration, int requestLimit) {
        this.requestLimit = requestLimit;
        this.duration = duration;
        this.httpClient = HttpClient.newHttpClient();
        this.executor = Executors.newFixedThreadPool(requestLimit);
        this.semaphore = new Semaphore(requestLimit);

    }


    public void createDocument(Document document, String signature) {
        try {
            semaphore.acquire();
            executor.submit(() -> {
                Instant now = Instant.now();
                synchronized (this) {
                    if (Duration.between(lastRequestTime, now).compareTo(duration) >= 0) {
                        requestCount = 0;
                        lastRequestTime = now;
                    }
                    requestCount++;
                }

                String jsonBody = gson.toJson(document);

                try {
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(URL))
                            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                            .header("Content-Type", "application/json")
                            .build();
                    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                    System.out.println("Response code:" + response.statusCode());
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Getter
    @Setter
    static class Document {

        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        private String production_date;
        private String production_type;
        private List<Product> products;
        private String reg_date;
        private String reg_number;

    }

    @Getter
    @Setter
    private static class Product {
        private String certificate_document;
        private String certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        private String production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    @Getter
    @Setter
    private static class Description {
        private String participantInn;
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(Duration.ofSeconds(20),5);
        Document document = new Document();
        Description description = new Description();
        Product product = new Product();

        description.setParticipantInn("string");
        document.setDescription(description);

        document.setDoc_id("string");
        document.setDoc_status("string");
        document.setDoc_type("LP_INTRODUCE_GOODS");
        document.setImportRequest(true);
        document.setOwner_inn("string");
        document.setParticipant_inn("string");
        document.setProducer_inn("string");
        document.setProduction_date("2020-01-23");
        document.setProduction_type("string");

        List<Product> products = new ArrayList<>();

        product.setCertificate_document("string");
        product.setCertificate_document_date("2020-01-23");
        product.setCertificate_document_number("string");
        product.setOwner_inn("string");
        product.setProducer_inn("string");
        product.setProduction_date("2020-01-23");
        product.setTnved_code("string");
        product.setUit_code("string");
        product.setUitu_code("string");
        products.add(product);
        document.setProducts(products);
        document.setReg_date("2020-01-23");
        document.setReg_number("string");

        crptApi.createDocument(document,"signature");

    }
}

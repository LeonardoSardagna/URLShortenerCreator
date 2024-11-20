package com.urlshortener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

//RequestHandler é usada para criar funções Lambda na AWS que processam eventos de entrada e retornam uma saída.
public class Main implements RequestHandler<Map<String, Object>, Map<String, String>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final S3Client s3Client = S3Client.builder().build();
    private static String BUCKET_NAME = "shorturl-aws-bucket";

    @Override
    public Map<String, String> handleRequest(Map<String, Object> input, Context context) {
        // input.get("body") deve conter um JSON no formato:
        // {"originalUrl": "www.youtube.com/@loianegroner", "expirationTime": "1763479279"}
        String bodyRequest = input.get("body").toString();
        Map<String, String> bodyMap = converteBody(bodyRequest);

        String originalURL = bodyMap.get("originalUrl");
        String expirationTime = bodyMap.get("expirationTime");
        long timeEspirationParsed = Long.parseLong(expirationTime);

        String shortUrlCode = UUID.randomUUID().toString().substring(0, 8);

        UrlData urlData = new UrlData(originalURL, timeEspirationParsed);
        Map<String, String> response = new HashMap<>();
        String keyBucket = shortUrlCode + ".json";
        String urlDataJson;

        //conexão com s3 para salvar os dados
        try {
            //criar um request para enviar dados para o s3
            urlDataJson = objectMapper.writeValueAsString(urlData);
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(keyBucket)
                    .build();
            //envia p request + conteúdo do arquivo
            s3Client.putObject(request, RequestBody.fromString(urlDataJson));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error saving data to s3 " + e.getMessage(), e);
        }

        response.put("code", shortUrlCode);

        return response;
    }

    private Map<String, String> converteBody(String valueToMapping) {
        try {
            return objectMapper.readValue(valueToMapping, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error parsing JSON body: " + e.getMessage(), e);
        }
    }
}
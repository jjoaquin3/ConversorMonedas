package com.jjoaquin3.util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Optional;

public class ApiClient
{

    private final HttpClient httpClient;

    public ApiClient()
    {
        this.httpClient = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofSeconds(16))
                .build();
    }

    public Optional<String> get(String uri)
    {
        HttpRequest request = HttpRequest
                .newBuilder()
                .GET()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .build();

        try
        {
            HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
            if (response.statusCode() == 200)
                return Optional.of(response.body());
            else
            {
                System.err.println("Error: " + response.statusCode() + " - " + response.body());
                return Optional.empty();
            }
        } catch (IOException | InterruptedException e)
        {
            System.err.println("Exception: " + e.getMessage());
            return Optional.empty();
        }
    }
}
package com.jjoaquin3.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjoaquin3.model.CurrencyResponseDTO;
import com.jjoaquin3.util.ApiClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ServiceCurrency
{
    private final ApiClient API_CLIENT;
    private final ObjectMapper OBJECT_MAPPER;
    private final String API_KEY;

    public ServiceCurrency(ApiClient apiClient, ObjectMapper objectMapper)
    {
        this.API_CLIENT = apiClient;
        this.OBJECT_MAPPER = objectMapper;
        this.API_KEY = loadApiKey();
    }

    public CurrencyResponseDTO getExchangeRate(String baseCode, String targetCode, Double amount)
    {
        String formattedAmount = String.format("%.0f", amount);
        String apiUrl = "https://v6.exchangerate-api.com/v6/" + API_KEY + "/pair/" + baseCode + "/" + targetCode + "/" + formattedAmount;

        try
        {
            String jsonResponse = API_CLIENT.get(apiUrl).orElseThrow(() -> new RuntimeException("Error al obtener la respuesta de la API"));
            return OBJECT_MAPPER.readValue(jsonResponse, CurrencyResponseDTO.class);
        }
        catch (Exception e)
        {
            System.err.println("Excepción al obtener la tasa de cambio: " + e.getMessage());
            return null;
        }
    }

    private String loadApiKey()
    {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties"))
        {
            if (input == null)
            {
                throw new RuntimeException("No se puede encontrar el archivo de configuración: config.properties");
            }

            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty("api.key");
        }
        catch (IOException ex)
        {
            throw new RuntimeException("Error al cargar la clave API desde config.properties", ex);
        }
    }
}

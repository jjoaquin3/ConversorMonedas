package com.jjoaquin3.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jjoaquin3.model.CurrencyResponseDTO;
import com.jjoaquin3.service.ServiceCurrency;
import com.jjoaquin3.util.ApiClient;

import java.text.DecimalFormat;
import java.util.Scanner;

public class CurrencyEndpoint
{
    private static CurrencyEndpoint instance;
    private final Scanner SCANNER;
    private final ServiceCurrency SERVICE_CURRENCY;

    private CurrencyEndpoint()
    {
        this.SCANNER = new Scanner(System.in);
        this.SERVICE_CURRENCY = new ServiceCurrency(new ApiClient(), new ObjectMapper());
    }

    public static CurrencyEndpoint getInstance()
    {
        if (instance == null) instance = new CurrencyEndpoint();
        return instance;
    }

    public void showMenu()
    {
        int option;
        while (true)
        {
            try
            {
                System.out.println("**********************************************");
                System.out.println("Bienvenid@ al Conversor de Monedas");
                System.out.println("**********************************************");
                System.out.println("Elige una opcion:");
                System.out.println("1. Dólar            => Peso Argentino");
                System.out.println("2. Peso Argentino   => Dólar");
                System.out.println("3. Dólar            => Real Brasileño");
                System.out.println("4. Real Brasileño   => Dólar");
                System.out.println("5. Dólar            => Peso Colombiano");
                System.out.println("6. Peso Colombiano  => Dólar");
                System.out.println("7. Salir");
                System.out.println("**********************************************");
                System.out.print("Opción: ");

                option = Integer.parseInt(SCANNER.nextLine());
                if (option >= 1 && option <= 6) processConversion(option);
                else if (option == 7) break;
                else System.out.println("Opción no válida. Por favor elija una opción del 1 al 7.");

                System.out.println();
                System.out.println("Presiona Enter para cargar el menú...");
                SCANNER.nextLine();
            }
            catch (Exception e)
            {
                System.out.println("Error: " + e.getMessage());
                SCANNER.nextLine();
            }
        }
        System.out.println("Gracias por utilizar nuestro servicio, ten un buen día c:");
        SCANNER.close();
    }

    private void processConversion(int option)
    {
        try
        {
            System.out.print("Ingrese el valor que desea convertir: ");
            double amount = Double.parseDouble(SCANNER.nextLine());
            String fromCurrency = "";
            String toCurrency = switch (option)
            {
                case 1 ->
                {
                    fromCurrency = "USD";
                    yield "ARS";
                }
                case 2 ->
                {
                    fromCurrency = "ARS";
                    yield "USD";
                }
                case 3 ->
                {
                    fromCurrency = "USD";
                    yield "BRL";
                }
                case 4 ->
                {
                    fromCurrency = "BRL";
                    yield "USD";
                }
                case 5 ->
                {
                    fromCurrency = "USD";
                    yield "COP";
                }
                case 6 ->
                {
                    fromCurrency = "COP";
                    yield "USD";
                }
                default -> "";
            };

            CurrencyResponseDTO responseDTO = SERVICE_CURRENCY.getExchangeRate(fromCurrency, toCurrency, amount);
            if (responseDTO != null)
            {
                Double convertedAmount = responseDTO.getConversion_result();
                DecimalFormat df = new DecimalFormat("#,##0.00");
                String formattedAmount = df.format(amount);
                String formattedConvertedAmount = df.format(convertedAmount);

                System.out.printf("El valor de %s %s corresponde al valor final de => %s %s%n", formattedAmount, fromCurrency, formattedConvertedAmount, toCurrency);
            }
            else System.out.println("No se pudó realizar la conversión.");
        }
        catch (Exception e)
        {
            System.out.println("Error durante la conversión: " + e.getMessage());
        }
    }
}
# Conversor de Monedas
![GitHub](https://img.shields.io/github/license/dropbox/dropbox-sdk-java)
![version](https://img.shields.io/badge/version-1.0.1-blue)

Proyecto para realizar solicitudes a una API de tasas de cambio, a manipular datos JSON y finalmente mostrar las monedas de interés común. 

## Indice

El proyecto consta de los siguiente pasos y están disponibles en la sección adyacente:

1. [Requerimientos](#requerimientos)
2. [Estructura Proyecto](#estructura-proyecto)
3. [Filtrado](#filtrado)
4. [Consumo API](#consumo-api)
5. [Manejo Respuestas](#manejo-respuestas)
6. [Endpoint](#endpoint)
7. [Consideraciones](#consideraciones)

## Requerimientos

El proyecto nace como un forma de demostrar la capacidades de elegir las librerias, la organización y bien implementar una solución para la obtención de información para la conversion de monedas.

| Recursos                    | Información                                   |
|-----------------------------|-----------------------------------------------|
| Eclipse Temurin JDK 17      | https://adoptium.net/es/temurin/releases/     |
| Maven Project 4.0.0         | https://maven.apache.org/            |
| Project lombok 1.18.30      | https://projectlombok.org/setup/maven         |
| ModelMapper 3.2.0           | https://modelmapper.org         |
| Jackson Project Home 2.17.2 | https://github.com/FasterXML/jackson        |
| GitHub                      | https://github.com/jjoaquin3/ConversorMonedas |

Estos serian los principales, las otras libreria se pueden encontrar en pom.xml del API

## Estructura Proyecto

Estructura principal del proyecto
```plaintext
├── endpoint
│   └── CurrencyEndpoint 
├── model
│   └── CurrencyResponseDTO
├── service
│   └── ServiceCurrency
├── util
│   └── ApiClient
└── Main.java
```
- endpoint: Divide la comunciación entre el usuario y la logica de negocio.
- model: Represtanción de las entidades tanto para request, reponse o bien entidades, en este caso solo se uso para response.
- service: Capa logica que gestion realiza las operaciones en este caso seria usar los parámetros que envia el EndPoint y usar el ApiCliente para consultar la API de https://www.exchangerate-api.com/.
- util: Tiene los auxiliares, en este caso un ApiCliente para el consumo de APIs.

Archivos  extras
```plaintext
├── resources
    └── config.properties
```

* Para usar la API de https://www.exchangerate-api.com/ es imperativo un API KEY, este se puede obtener gratis y colocarla en config.properties

config.properties, sustituir xxxxxx por el propio API KEY
```sh
api.key=xxxxxx
```

## Filtrado

En la versión actual se limitó a ciertas monedas a travez de comunicación en un menú:
```sh
**********************************************
Bienvenid@ al Conversor de Monedas
**********************************************
Elige una opcion:
1. Dólar            => Peso Argentino
2. Peso Argentino   => Dólar
3. Dólar            => Real Brasileño
4. Real Brasileño   => Dólar
5. Dólar            => Peso Colombiano
6. Peso Colombiano  => Dólar
7. Salir
**********************************************
Opción: 
```

Posterior la lectura de un monto:
```sh
Ingrese el valor que desea convertir: 
```

Se realizara la conversión y nos solicita "enter" para volver a mostrar el menú:
```sh
El valor de 2,500.00 USD corresponde al valor final de => 2,301,250.00 ARS

Presiona Enter para cargar el menú...
```

Finalmente si se selecciona la opcion 7, un mensaje de despedia y cierre.
```sh
Opción: 7
Gracias por utilizar nuestro servicio, ten un buen día c:
```

## Consumo API

#### **ApiClient.java**

Establece una clase custom para el consumo a travez del verbo GET.

Con esta clase se puede consumir esta URL https://v6.exchangerate-api.com/v6/YOUR-API-KEY/pair/EUR/GBP, donde se establece el API KEY, la moneda origen, moneda destino, y monto a convertir.

```java
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
```

## Manejo Respuestas

Explorando la documentación (https://www.exchangerate-api.com/docs/pair-conversion-requests) API de ExchangeRate-API para nuestro requerimiento las posibles respuestas son. 

#### **Success**
```json
{
  "result": "success",
  "documentation": "https://www.exchangerate-api.com/docs",
  "terms_of_use": "https://www.exchangerate-api.com/terms",
  "time_last_update_unix": 1585267200,
  "time_last_update_utc": "Fri, 27 Mar 2020 00:00:00 +0000",
  "time_next_update_unix": 1585270800,
  "time_next_update_utc": "Sat, 28 Mar 2020 01:00:00 +0000",
  "base_code": "EUR",
  "target_code": "GBP",
  "conversion_rate": 0.8412
}
```

#### **Error**
```json
{
  "result": "error",
  "error-type": "unknown-code"
}
```

#### **CurrencyResponseDTO**
Nos permite mapear las respuestas del API de ExchangeRate a un DTO general con ModelMapper.

```java
@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyResponseDTO
{
    private String base_code;
    private String target_code;
    private Double conversion_rate;
    private Double conversion_result;
    private String result;

    @JsonAlias("e1rror-type")
    private String error_type;
}

```

#### **ServiceCurrency**
Hacemos uso de ApiCliente luego de construir la URL, pero con ObjectMapper mapeamos el JSON de la respuesta a nuestro DTO.

```Java
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
```

## Endpoint

#### **CurrencyEndpoint**

Permite comunicación entre el Cliente, Sistema cargando el menu y el manejo de instancias del services y propia con Singleton.
Principalmente el siguiente metodo:

```java
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
```

## Consideraciones

- Configurar bien el resources.properties colocando el API KEY gestionado en https://www.exchangerate-api.com/ 
- Al principio al moento de ingresar cantidades grandes estas automaticamente se convertian en notación decimal lo cual el API no puede identicar bien los parametros por lo que se realizó formateo con una mascara con DecimalFormat:

``` Java
Double convertedAmount = responseDTO.getConversion_result();
DecimalFormat df = new DecimalFormat("#,##0.00");
String formattedAmount = df.format(amount);
String formattedConvertedAmount = df.format(convertedAmount);
System.out.printf("El valor de %s %s corresponde al valor final de => %s %s%n", formattedAmount, fromCurrency, formattedConvertedAmount, toCurrency);        
```

Fin c:


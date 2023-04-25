package com.azureprojectconsultingfunction;

import java.sql.*;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
// import org.springframework.cloud.function.adapter.azure.AzureSpringBootRequestHandler;
import com.azureprojectconsultingfunction.Model.Product;


/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("GetLastVisitedProduct")
    @FixedDelayRetry(maxRetryCount = 3, delayInterval = "00:00:05")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                route = "product/{visitorId}/{sessionId}/{eventType}",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
                 @BindingName("visitorId") String visitorId,
                 @BindingName("sessionId") String sessionId,
                 @BindingName("eventType") String eventType,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        // final String visitorId = request.getQueryParameters().get("visitorId ");
        // final String sessionId = request.getQueryParameters().get("sessionId ");
        // final String name = request.getBody().orElse(query);

        String connectionUrl = System.getenv("connection_string");
        // String connectionUrl = "jdbc:sqlserver://23shophsalbsig.database.windows.net:1433;database=shop23hsalbsig;user=shophsalbsig@23shophsalbsig;password=12345shop.;encrypt=true;trustServerCertificate=false;hostNameInCertificate=*.database.windows.net;loginTimeout=30;";
        ResultSet resultSet =  null;

        List<Product> productList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            PreparedStatement  statement;
            String sqlQuery = "SELECT TOP 3 p.* FROM Event e LEFT JOIN Product p ON p.product_id = e.product_id WHERE e.session_id <> ?  AND e.visitor_id = ? AND event_type=? ORDER BY e.created_date DESC";
            
            statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, sessionId);
            statement.setString(2, visitorId);
            statement.setString(3, eventType);
            resultSet = statement.executeQuery();

            while(resultSet.next()) {
               Product product = new Product();
                product.setProductId(resultSet.getString("product_id"));
                product.setName(resultSet.getString("product_name"));
                product.setDescription(resultSet.getString("product_description"));
                productList.add(product);
            }
        } catch (SQLException e) {
            // TODO: handle exception
            e.printStackTrace();
        }

        try {
           ObjectMapper mapper = new ObjectMapper();
           String jsonResponse = mapper.writeValueAsString(productList);

           return request.createResponseBuilder(HttpStatus.OK)
                   .header("Content-Type", "application/json")
                   .body(jsonResponse)
                   .build();
       } catch (Exception e) {
           return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Error: " + e.getMessage())
                   .build();
       }

        // final String jsonDocument = "{\"visitorId\":\"" + visitorId + "\", " + 
        //                                 "\"description\": \"" + sessionId + "\"}";
        // if (visitorId == null) {
        //     return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a name on the query string or in the request body").build();
        // } else {
        //     return request.createResponseBuilder(HttpStatus.OK).body(jsonDocument).build();
        // }
    }
}

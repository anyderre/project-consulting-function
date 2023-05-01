package com.azureprojectconsultingfunction;

import java.sql.*;
import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;
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

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        

        String connectionUrl = System.getenv("connection_string");
        ResultSet resultSet =  null;

        List<Product> productList = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(connectionUrl)) {
            PreparedStatement  statement;
            String sqlQuery = "SELECT TOP 3 p.* FROM events e LEFT JOIN Product p ON p.productId = e.productId WHERE e.sessionId <> ?  AND e.visitorId = ? AND eventType=? ORDER BY e.dateTime DESC";
            
            statement = connection.prepareStatement(sqlQuery);
            statement.setString(1, sessionId);
            statement.setString(2, visitorId);
            statement.setString(3, eventType);
            resultSet = statement.executeQuery();

            while(resultSet.next()) {
               Product product = new Product();
                product.setProductId(resultSet.getString("productId"));
                product.setBrand(resultSet.getString("brand"));
                product.setDescription(resultSet.getString("description"));
                product.setSku(resultSet.getString("sku"));
                product.setPrice(resultSet.getInt("price"));
                product.setCurrency(resultSet.getString("currency"));
                product.setAvailability(resultSet.getString("availability"));
                product.setItem_condition(resultSet.getString("item_condition"));
                product.setGame_platform(resultSet.getString("game_platform"));
                product.setOperating_systems(resultSet.getString("operating_systems"));
                product.setImages(resultSet.getString("images"));
                product.setCategory(resultSet.getString("category"));
                product.setPublisher(resultSet.getString("publisher"));
                product.setTitle(resultSet.getString("title"));
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
    }
}

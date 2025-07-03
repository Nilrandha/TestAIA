package aiatest.function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("HttpAIA")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a " + request.getHttpMethod().name() + " request.");
        String url = System.getenv("DB_SETTINGS");
        try {
            Optional<String> requestBody = request.getBody();
            if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
                return errorResponse(request, "Empty request body");
            }

            JsonNode jsonNode = objectMapper.readTree(requestBody.get());
            HttpMethod method = request.getHttpMethod();

            if (method == HttpMethod.POST) {
                // POST: Insert new record
                String firstName = jsonNode.path("firstName").asText(null);
                String lastName = jsonNode.path("lastName").asText(null);
                String city = jsonNode.path("city").asText(null);
                Integer age = jsonNode.has("age") && jsonNode.get("age").canConvertToInt() ? jsonNode.get("age").asInt() : null;

                if (firstName == null || lastName == null || city == null || age == null ||
                        firstName.isEmpty() || lastName.isEmpty() || city.isEmpty() ||
                        !firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
                    return errorResponse(request, "Invalid or missing data for insert");
                }

                try (Connection conn = DriverManager.getConnection(url)) {
                    String sql = "INSERT INTO users (first_name, last_name, city, age) VALUES (?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, city);
                    stmt.setInt(4, age);
                    stmt.executeUpdate();
                    context.getLogger().info("User inserted successfully");
                }

                return successResponse(request, "User inserted successfully");

            } else if (method == HttpMethod.PUT) {
                // PUT: Update existing record
                int id = jsonNode.path("id").asInt(-1);
                String firstName = jsonNode.path("firstName").asText(null);
                String lastName = jsonNode.path("lastName").asText(null);
                String city = jsonNode.path("city").asText(null);
                Integer age = jsonNode.has("age") && jsonNode.get("age").canConvertToInt() ? jsonNode.get("age").asInt() : null;

                if (id <= 0 || firstName == null || lastName == null || city == null || age == null ||
                        firstName.isEmpty() || lastName.isEmpty() || city.isEmpty() ||
                        !firstName.matches("[a-zA-Z]+") || !lastName.matches("[a-zA-Z]+")) {
                    return errorResponse(request, "Invalid or missing data for update");
                }

                try (Connection conn = DriverManager.getConnection(url)) {
                    String sql = "UPDATE users SET firstname=?, last_name=?, city=?, age=? WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstName);
                    stmt.setString(2, lastName);
                    stmt.setString(3, city);
                    stmt.setInt(4, age);
                    stmt.setInt(5, id);
                    int rows = stmt.executeUpdate();
                    if (rows == 0) {
                        return errorResponse(request, "No user found with given id");
                    }
                }

                return successResponse(request, "User updated successfully");

            } else if (method == HttpMethod.DELETE) {
                // DELETE: Remove record by id
                int id = jsonNode.path("id").asInt(-1);
                if (id <= 0) {
                    return errorResponse(request, "Invalid or missing id for delete");
                }

                try (Connection conn = DriverManager.getConnection(url)) {
                    String sql = "DELETE FROM users WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, id);
                    int rows = stmt.executeUpdate();
                    if (rows == 0) {
                        return errorResponse(request, "No user found with given id");
                    }
                }

                return successResponse(request, "User deleted successfully");
            }

            return errorResponse(request, "Unsupported HTTP method");

        } catch (Exception e) {
            context.getLogger().severe("Error processing request: " + e.getMessage());
            context.getLogger().info("DB Error:" +e.getMessage());
            return errorResponse(request, "Exception occurred");
        }
    }

    private HttpResponseMessage successResponse(HttpRequestMessage<Optional<String>> request, String message) {
        ResponseEntity responseEntity = new ResponseEntity(0, message);
        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }

    private HttpResponseMessage errorResponse(HttpRequestMessage<Optional<String>> request, String message) {
        ResponseEntity responseEntity = new ResponseEntity(-1, message);
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }

}

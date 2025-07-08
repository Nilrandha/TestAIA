package aiatest.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Optional;

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

public class FunctionUpdate {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @FunctionName("updateUser")
    public HttpResponseMessage updateUser(
        @HttpTrigger(name = "req", methods = {HttpMethod.PUT}, authLevel = AuthorizationLevel.ANONYMOUS)
        HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context) {

        context.getLogger().info("Processing PUT request...");

        try {
            Optional<String> requestBody = request.getBody();
            if (!requestBody.isPresent() || requestBody.get().isEmpty()) {
                return errorResponse(request, "Empty request body");
            }

            JsonNode jsonNode = objectMapper.readTree(requestBody.get());
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

            try (Connection conn = DatabaseUtil.getConnection()) {
                String sql = "UPDATE users SET first_name=?, last_name=?, city=?, age=? WHERE id=?";
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

            ResponseEntity responseEntity = new ResponseEntity(0, "User updated successfully", firstName, lastName);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseEntity)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error updating user: " + e.getMessage());
            return errorResponse(request, "Exception occurred while updating user");
        }
    }

    private HttpResponseMessage errorResponse(HttpRequestMessage<Optional<String>> request, String message) {
        ResponseEntity responseEntity = new ResponseEntity(-1, message, null, null);
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(responseEntity)
                .build();
    }
}

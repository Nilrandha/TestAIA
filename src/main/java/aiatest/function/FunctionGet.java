package aiatest.function;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Optional;

public class FunctionGet {
    @FunctionName("getRecord")
    public HttpResponseMessage runGet(
            @HttpTrigger(name = "req", methods = {HttpMethod.GET}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Processing GET request...");

        //String url = "jdbc:mysql://localhost:3306/azuredb?user=root&password=Malee%402000";
        String url = System.getenv("DB_SETTINGS");

        try {
            String idParam = request.getQueryParameters().get("id");

            try (Connection conn = DriverManager.getConnection(url)) {
                String sql;
                PreparedStatement stmt;

                if (idParam != null) {
                    // Fetch single record by id
                    sql = "SELECT id, first_name, last_name, city, age FROM users WHERE id = ?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, Integer.parseInt(idParam));
                } else {
                    // Fetch all records
                    sql = "SELECT id, first_name, last_name, city, age FROM users";
                    stmt = conn.prepareStatement(sql);
                }

                var rs = stmt.executeQuery();

                StringBuilder resultJson = new StringBuilder();
                resultJson.append("[");

                boolean first = true;
                while (rs.next()) {
                    if (!first) resultJson.append(",");
                    resultJson.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"firstName\":\"").append(rs.getString("first_name")).append("\",")
                            .append("\"lastName\":\"").append(rs.getString("last_name")).append("\",")
                            .append("\"city\":\"").append(rs.getString("city")).append("\",")
                            .append("\"age\":").append(rs.getInt("age"))
                            .append("}");
                    first = false;
                }

                resultJson.append("]");

                if (first) { // no records found
                    return errorResponse(request, "No matching records found");
                }

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(resultJson.toString())
                        .build();

            }

        } catch (Exception e) {
            context.getLogger().severe("Error: " + e.getMessage());
            return errorResponse(request, "Error fetching record(s)");
        }
    }

    private HttpResponseMessage errorResponse(HttpRequestMessage<Optional<String>> request, String message) {
        ResponseEntity response = new ResponseEntity(-1, message, null, null);
        return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(response)
                .build();
    }
}

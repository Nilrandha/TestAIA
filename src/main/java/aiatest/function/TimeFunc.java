package aiatest.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class TimeFunc {

    @FunctionName("TimerExample")
    public void run(
        @TimerTrigger(
            name = "timerInfo",
            schedule = "0 */1 * * * *" // Every 5 minutes
        ) String timerInfo,final ExecutionContext context
    ) {
        try {
           SqlServerConnector ss  = new SqlServerConnector();
           ss.callSqlServer();
        } catch (Exception e) {
            context.getLogger().severe("Error calling SQL Server: " + e.getMessage());
        }
    }
    
   
}
package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import utils.PropertyUtils;

public class RetryListener implements IRetryAnalyzer {

    int failedIteration = 0;
    int maxRetryLimit = Integer.parseInt(PropertyUtils.getValue("maxTry"));

    @Override
    public boolean retry(ITestResult iTestResult) {

        // Check If Failed Retry For Max Times
        if (failedIteration < maxRetryLimit) {

            // Increasing Iteration to check
            failedIteration++;
            return true;

        } else {

            return false;
        }

    }

}

//Tao noi dung step cho attachment

package utils;

public class StepContext {

    private static final ThreadLocal<String> currentStep = new ThreadLocal<>();

    public static void setStep(String stepName) {
        currentStep.set(stepName);
    }

    public static String getStep() {
        return currentStep.get();
    }

    public static void clear() {
        currentStep.remove();
    }
}
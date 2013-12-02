package org.milyn.lang;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class LangUtil {

    public static final double JAVA_VERSION;

    private LangUtil() {
    }

    public static double getJavaVersion() {
        return JAVA_VERSION;
    }

    static {
        String versionSysProp = System.getProperty("java.version");
        StringBuilder versionBuilder = new StringBuilder();

        boolean gotDot = false;
        for (int i = 0; i < versionSysProp.length(); i++) {
            char c = versionSysProp.charAt(i);

            if (Character.isDigit(c)) {
                versionBuilder.append(c);
            } else if (c == '.' && !gotDot) {
                versionBuilder.append(c);
                gotDot = true;
            } else {
                break;
            }
        }

        JAVA_VERSION = Double.parseDouble (versionBuilder.toString());
    }
}

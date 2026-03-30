import java.util.Properties;

class Test {
    public static void printSystemProperties() {
        System.out.println("Printing System Properties Using");
        Properties properties = System.getProperties();
        System.out.println(properties);
    }

    public static void main(String[] args) {
        System.out.println("Java program started...");
        printSystemProperties();
    }
}

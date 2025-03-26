import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        String[] commands = {"exit", "echo", "type"};
            List<String> builtins = Arrays.asList(commands);

        while(true) {
            System.out.print("$ ");
            String input = scanner.nextLine();
            String[] str = input.split(" ");
            String command = str[0];
            StringBuilder parameter = new StringBuilder();

            // if parameter words length bigger than 1, add all it to string
            if(str.length > 2) {
                for(int i = 1; i < str.length; i++) {
                    if(i < str.length-1) {
                        parameter.append(str[i]).append(" ");
                    } else {
                        parameter.append(str[i]);
                    }
                }
            } else if(str.length > 1) {
                parameter = new StringBuilder(str[1]);
            }

            switch(command) {
                case "exit":
                    if(parameter.toString().equals("0")) {
                        System.exit(0);
                    } else {
                        System.out.println(input + ": command not found");
                    }
                    break;
                case "echo":
                    System.out.println(parameter);
                    break;
                case "type":
                    if(parameter.toString().equals(builtins.get(0)) ||
                            parameter.toString().equals(builtins.get(1)) ||
                            parameter.toString().equals(builtins.get(2))) {
                        System.out.println(parameter + " is a shell builtin");
                    } else {
                        String path = getPath(parameter.toString());
                        if(path != null) {
                            System.out.println(parameter + " is " + path);
                        } else {
                            System.out.println(parameter + ": not found");
                        }
                    }
                    break;
                default:
                    String fullPath = getPath(command);
                    if (fullPath != null) {
                        List<String> run = new ArrayList<>();
                        run.add(command);
                        for (int i = 1; i < str.length; i++) {
                            run.add(str[i]);
                        }
                        ProcessBuilder pb = new ProcessBuilder(run);
                        pb.directory(Path.of(getCommandDir(fullPath)).toFile());
                        pb.inheritIO();
                        try {
                            Process proc = pb.start();
                            proc.waitFor();
                        } catch (Exception e) {
                            System.err.println("Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println(input + ": command not found");
                    }
                    break;

            }
        }
    }
    private static String getCommandDir(String fullPath) {
        int lastSlash = fullPath.lastIndexOf('/');
        return (lastSlash == -1) ? "." : fullPath.substring(0, lastSlash);
    }
    /*
     * GetCommandDir extracts the directory from a full file path by finding the last '/' character; if found, it returns the substring from the beginning up to (but not including) that slash, which gives the directory path. If no '/' is present (meaning the file is in the current directory), it returns "." to indicate the current working directory.
     */


    private static String getPath(String parameter) {
        for (String path : System.getenv("PATH").split(":")) {
            Path fullPath = Path.of(path, parameter);
            if( Files.isRegularFile(fullPath)) {
                return fullPath.toString();
            }
        }
        return null;
    }

}
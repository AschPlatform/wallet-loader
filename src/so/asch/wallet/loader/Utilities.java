package so.asch.wallet.loader;

import javafx.scene.control.Alert;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Utilities {

    public static String getJarDir(Class clazz) {
        String executePath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        java.io.File file = new java.io.File(executePath);

        String dir = file.getParentFile().getAbsolutePath();
        try {
            dir = java.net.URLDecoder.decode (dir, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dir;
    }

    private static List<URL> getDirJarUrls(Path jarDir){
        return Arrays.stream(jarDir.toFile().listFiles())
                .filter(f->f.getName().endsWith("jar") || f.getName().endsWith(".zj"))
                .map(f-> fileToURL(f))
                .collect(Collectors.toList());
    }

    private static URL fileToURL(File file){
        try {
            return file.toURI().toURL();
        }
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

    private static void errorDialog(String error){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(LanguageContant.KEY_UPDATEBOX_ALERT_ERROR);
        alert.setHeaderText(error);

        alert.showAndWait();
    }

    public static Class<?> loadClass(Path jarPath, String className){
        try{
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            List<URL> urls = getDirJarUrls(jarPath);
            ClassLoader appLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]));

            Thread.currentThread().setContextClassLoader(appLoader);
            return appLoader.loadClass(className);
        }
        catch (Exception ex){
            ex.printStackTrace();
            return null;
        }
    }


    public static Process launchWallet(Path walletJarPath){
        try {
            String os = System.getProperty("os.name").toLowerCase();
            boolean isWindows = os.contains("win");
            boolean isMac = os.contains("mac") || os.contains("darwin");

            String command = isMac ?
                    "./jre/Contents/Home/bin/java -jar \"" + walletJarPath.toString() +"\"" :
                    ( isWindows ? "java -jar \"" + walletJarPath.toString() +"\"" :
                                  "java -jar "+ walletJarPath.toString() );

            return Runtime.getRuntime().exec(command);
        }
        catch (Exception ex){
            ex.printStackTrace();
            errorDialog(LanguageContant.KEY_UPDATEBOX_DIALOG_START_FAIL);
            return null;
        }
    }
}

package so.asch.wallet.loader;

import java.util.Arrays;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Program extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("upgrade.fxml"),
            LanguageContant.getResourceBundle());
        Scene loaderScene = new Scene(root);
        loaderScene.setFill(Color.TRANSPARENT);
        loaderScene.setUserAgentStylesheet(Program.class.getResource("style.css").toString());
        primaryStage.setScene(loaderScene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image(
                Program.class.getResourceAsStream("aschLogo.png")));
        primaryStage.show();
    }

    private static void generateAssembly(){
        try {
            WalletAssembly wa = WalletAssembly.fromDefaultFile().orElse(WalletAssembly.empty());
            wa.updateArtifacts();
            wa.saveToDefaultFile();

            System.out.print("generate'" + WalletAssembly.WALLET_ASSEMBLY_FILE + "'success");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args != null && Arrays.stream(args).anyMatch(arg->arg.toLowerCase().equals("-g"))){
            generateAssembly();
            System.exit(0);
            return;
        }

        launch(args);
    }
}

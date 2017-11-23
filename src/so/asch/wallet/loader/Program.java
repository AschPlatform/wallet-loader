package so.asch.wallet.loader;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Arrays;

public class Program extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        Parent root = FXMLLoader.load(getClass().getResource("upgrade.fxml"));
        Scene loaderScene = new Scene(root);
        loaderScene.setFill(Color.TRANSPARENT);
        loaderScene.setUserAgentStylesheet(Program.class.getResource("style.css").toString());
        primaryStage.setScene(loaderScene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.getIcons().add(new Image(
                Program.class.getResourceAsStream("aschLogo.png")));
        primaryStage.show();
    }

    protected static void generateAssembly(){
        try {
            WalletAssembly wa = WalletAssembly.fromDefaultFile().orElse(WalletAssembly.empty());
            wa.updateArtifacts();
            wa.saveToDefaultFile();

            System.out.print("生成'" + WalletAssembly.WALLET_ASSEMBLY_FILE + "'成功");
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

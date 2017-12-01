package so.asch.wallet.loader;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UpgradeController {
    private volatile DefaultUpgrader upgrader = new DefaultUpgrader();
    private ExecutorService upgradeService =  Executors.newSingleThreadExecutor();
    private Future upgradeFuture = null;

    @FXML protected Label labProgress;
    @FXML protected Label labPercentage;
    @FXML protected ProgressBar prgUpgrade;
    @FXML protected Button btnCancel;

    @FXML protected void initialize(){
        upgrader.setOnProgressChanged((step, progress)->
                Platform.runLater(()->updateProgress(step, progress))
        );

        btnCancel.setOnAction(e->cancelUpgrade());

        upgradeFuture = upgradeService.submit(()->upgradeAndLaunch());

        Platform.runLater(()->{
            btnCancel.getScene().getWindow().setOnHidden(e->{
                cancelUpgrade();
            });
        });
    }

    private void cancelUpgrade(){
        if (upgradeFuture != null && !upgradeFuture.isDone() ){
            upgradeFuture.cancel(true);
            upgradeService.shutdown();
        }
    }

    private void updateProgress(String step, double progress){
        labProgress.setText(step);
        prgUpgrade.setProgress(progress/100);
        labPercentage.setText(Double.toString((int)progress)+"%");
    }

    protected void upgradeAndLaunch(){
        try {
            upgrader.upgrade();
        }
        finally {
            launch();
        }
    }

    private void launch(){
        Platform.runLater(()-> updateProgress(LanguageContant.KEY_UPDATEBOX_LOADING_WALLET, 100));

        WalletAssembly wa = WalletAssembly.fromDefaultFile().orElse(WalletAssembly.empty());
        Path walletJarPath = Paths.get(Utilities.getJarDir(WalletAssembly.class));
        boolean isCurrentDir = null == wa.getDir() || "".equals(wa.getDir().trim()) || ".".equals(wa.getDir().trim());
        if (!isCurrentDir) {
            walletJarPath = walletJarPath.resolve(wa.getDir());
        }
        Path path = walletJarPath.resolve(wa.getEntryJar());
        Platform.runLater(()->lanuchWalletAndwait(path));
    }

    private void fadeOutClose(){
        Node root = btnCancel.getParent().getParent();
        FadeTransition ft = new FadeTransition(Duration.millis(300), root);
        ft.setFromValue(1);
        ft.setToValue(0.1);

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        ft.setOnFinished(e-> System.exit(0));

        ft.play();
    }

    private void showErrorMessage(String error){
        updateProgress(error, 100);
        labProgress.setTextFill(Color.ORANGE);
    }

    private void lanuchWalletAndwait(Path walletJarPath) {
        Process p = Utilities.launchWallet(walletJarPath);
        ExecutorService service = Executors.newSingleThreadExecutor();
        Future<Boolean> future = service.submit(()->{
            try {
                if (null == p) return false;
                int read = p.getInputStream().read();
                return read > 0;
            }
            catch (Exception ex){
                return false;
            }
        });

        try {
            int times = 10;
            String[] tmp = new String[]{".","..","..."};
            while( times -- >0 && !future.isDone()){
                String info = LanguageContant.KEY_UPDATEBOX_STARTING_WALLET + tmp[times % 3];
                Platform.runLater(()->updateProgress(info, 100));
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            future.cancel(true);
            service.shutdown();
            Platform.runLater(()->fadeOutClose());
        }
    }
}

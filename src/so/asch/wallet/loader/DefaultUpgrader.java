package so.asch.wallet.loader;

import com.alibaba.fastjson.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class DefaultUpgrader implements Upgrader {

    private double currentProgress = 0;
    private String currentStep = "";
    private String currentServerUrl= "";

    private WalletAssembly localAssembly;
    private BiConsumer<String, Double> onProgressChanged;
    private String tmpDir = System.getProperty("java.io.tmpdir");
    private boolean cancelUpgrade = false;


    public void setOnProgressChanged(BiConsumer<String, Double> onProgressChanged){
        this.onProgressChanged = onProgressChanged;
    }

    private void cancel(){
        this.cancelUpgrade = true;
    }

    private void checkCancel() throws Exception{
        if (cancelUpgrade){
            throw new InterruptedException();
        }
    }

    private void setCurrentServer(String serverUrl){
        this.currentServerUrl = serverUrl;
        if (currentServerUrl != null && !currentServerUrl.endsWith("/")){
            this.currentServerUrl += "/";
        }
    }

    private WalletAssembly getLocalAssembly(){
        if (null == localAssembly){
            localAssembly = WalletAssembly.fromDefaultFile().orElse(WalletAssembly.empty());
            try {
                localAssembly.updateArtifacts();
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }

        return localAssembly;
    }

    private Optional<WalletAssembly> getServerAssembly(){
        for(String serverUrl : getLocalAssembly().getServerUrls()) {
            setCurrentServer(serverUrl);
            try {
                String assemblyFileName = WalletAssembly.WALLET_ASSEMBLY_FILE;
                downloadArtifact(assemblyFileName, 0, false);
                byte[] buffer = Files.readAllBytes(getCacheArtifactPath(assemblyFileName));

                String json = (new String(buffer, Charset.defaultCharset()));
                WalletAssembly wa = JSONObject.parseObject(json, WalletAssembly.class);
                return Optional.of(wa);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return Optional.empty();
    }

    private List<Artifact> generateUpgradeList(){
        return getServerAssembly()
                .orElse(WalletAssembly.empty())
                .getArtifacts().stream()
                .filter(sa -> needUpgrade(sa))
                .collect(Collectors.toList());
}

    private boolean needUpgrade(Artifact serverArtifact){
        return getLocalAssembly().getArtifacts().stream()
                .noneMatch(la -> la.isSameVersion(serverArtifact));
    }

    private URI getServerArtifactURI(String artifactName){
        return URI.create(currentServerUrl).resolve(artifactName);
    }

    private Path getCacheArtifactPath(String artifactName){
        return Paths.get(tmpDir, artifactName);
    }

    private Path getLocalArtifactPath(String artifactName){
        return Paths.get(Utilities.getJarDir(WalletAssembly.class), getLocalAssembly().getDir(), artifactName);
    }

    private void downloadArtifact( String artifactName, long totalArtifactsSize, boolean reportProgress) throws Exception{

        try (InputStream input = openDownloadStream(getServerArtifactURI(artifactName));
             OutputStream output = Files.newOutputStream(getCacheArtifactPath(artifactName))) {

            byte[] buf = new byte[1024*128];

            int read;
            while ((read = input.read(buf)) > -1) {
                checkCancel();
                output.write(buf, 0, read);
                if (reportProgress) {
                    increaseProgress((double) read / totalArtifactsSize * 90);
                }
            }
        }
    }

    private boolean downloadArtifacts(List<Artifact> artifacts){
        long totalSize = 0L;
        for (Artifact a: artifacts) {
            totalSize+= a.getSize();
        }

        try {
            for (Artifact a : artifacts) {
                setCurrentStep("正在下载"+a.getName());
                downloadArtifact(a.getName(), totalSize, true);
                setCurrentStep(a.getName()+"下载成功");
            }

            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private InputStream openDownloadStream(URI uri) throws IOException {
        if (uri.getScheme().equals("file")) return Files.newInputStream(new File(uri.getPath()).toPath());

        URLConnection connection = uri.toURL().openConnection();
        if (uri.getUserInfo() != null) {
            byte[] payload = uri.getUserInfo().getBytes(StandardCharsets.UTF_8);
            String encoded = Base64.getEncoder().encodeToString(payload);
            connection.setRequestProperty("Authorization", String.format("Basic %s", encoded));
        }
        return connection.getInputStream();
    }


    private boolean replaceArtifacts(List<String> artifacts){
        try {
            setCurrentStep("正在应用更新");
            int count = 0;
            for (String a : artifacts) {
                Files.copy(getCacheArtifactPath(a), getLocalArtifactPath(a), StandardCopyOption.REPLACE_EXISTING);
                setCurrentProgress(95 + ((double)++count / artifacts.size()));
            }

            return true;
        }
        catch (Exception ex){
            ex.printStackTrace();
            return false;
        }
    }

    private void setCurrentProgress(double totalPercentage) {
        currentProgress = totalPercentage;
        if(onProgressChanged != null){
           onProgressChanged.accept(currentStep, Math.max(Math.min(100, totalPercentage),0));
        }
    }

    private void increaseProgress(double percentage){
        setCurrentProgress(currentProgress + percentage);
    }

    private void setCurrentStep(String step){
        currentStep = step;
        if(onProgressChanged != null){
            onProgressChanged.accept(step, currentProgress);
        }
    }

    private void upgradeFailed(String message){
        setCurrentProgress(100);
        setCurrentStep(message);
    }

    @Override
    public boolean checkUpgradable() {
        setCurrentStep("正在检查更新");
        return generateUpgradeList().size() > 0;
    }


    @Override
    public boolean upgrade() {
        List<Artifact> upgradeList = generateUpgradeList();
        if (upgradeList == null || upgradeList.size() == 0){
            setCurrentStep("检查完毕，无需更新");
            return true;
        }
        else  if (!downloadArtifacts(upgradeList)){
            upgradeFailed("下载更新失败");
            return false;
        }

        List<String> artifacts = upgradeList.stream().map(a->a.getName()).collect(Collectors.toList());
        artifacts.add(WalletAssembly.WALLET_ASSEMBLY_FILE);
        if (artifacts.size()> 0 && !replaceArtifacts(artifacts)){
            upgradeFailed("应用更新文件失败");
            return false;
        }

        return true;
    }

}

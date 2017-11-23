package so.asch.wallet.loader;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;

import javax.rmi.CORBA.Util;
import javax.swing.text.html.Option;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;

public class WalletAssembly {
    private final static String DEFAULT_UPGRADE_SERVER1 = "http://101.200.123.124:8080/upgrade/";
    private final static String DEFAULT_UPGRADE_SERVER2 = "http://101.200.84.232:8080/upgrade/";
    public final static String WALLET_ASSEMBLY_FILE ="assembly.info";

    private Long timestamp;
    private String entryJar;
    private String dir;
    private List<String> serverUrls = new ArrayList<>() ;
    private List<Artifact> artifacts = new ArrayList<>();


    public WalletAssembly(){}

    public WalletAssembly(Long timestamp, String entryJar, String dir) {
        this.timestamp = timestamp;
        this.entryJar = entryJar;
        this.dir = dir;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public void setEntryJar(String entryJar) {
        this.entryJar = entryJar;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public void setServerUrls(List<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public void setArtifacts(List<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public String getEntryJar() {
        return entryJar;
    }

    public String getDir() {
        return dir;
    }

    public List<String> getServerUrls() {
        return serverUrls;
    }

    public List<Artifact> getArtifacts() {
        return artifacts;
    }

    public void saveToFile(Path path) throws IOException{
        String json = JSONObject.toJSONString(this);
        Files.write(path, json.getBytes(Charset.defaultCharset()));
    }

    public void saveToDefaultFile() throws IOException{
        saveToFile(Paths.get(Utilities.getJarDir(WalletAssembly.class), WALLET_ASSEMBLY_FILE));
    }

    protected static WalletAssembly empty(){
        WalletAssembly wa = new WalletAssembly(new Date().getTime(), "asch-wallet.jar", "." );
        wa.getServerUrls().add(DEFAULT_UPGRADE_SERVER1);
        wa.getServerUrls().add( DEFAULT_UPGRADE_SERVER2);

        return wa;
    }


    public static void updateArtifacts(WalletAssembly wa, String artifactsDir, String... includedExtensions) throws IOException{
        List<File> files = new ArrayList<>();

        Set<String> extensions = new HashSet<>();
        extensions.add("jar");
        Arrays.asList(includedExtensions == null ? new String[0] : includedExtensions).forEach(ext->{
            if (!extensions.contains(ext)){
                extensions.add(ext);
            }
        });

        wa.getArtifacts().clear();

        Arrays.stream(new File(artifactsDir).listFiles())
                .filter(f-> extensions.stream().anyMatch(e-> f.getName().endsWith(e)))
                .forEach(f->wa.getArtifacts().add( Artifact.fromFile(f)));
    }

    public WalletAssembly updateArtifacts() throws IOException {
        updateArtifacts(this, this.getDir());
        return this;
    }

    public static WalletAssembly fromJson(String json){
        WalletAssembly wa = JSON.parseObject(json, WalletAssembly.class);
        List<String> serverUrls = wa.getServerUrls();
        if (serverUrls.size() == 0){
            serverUrls.add(DEFAULT_UPGRADE_SERVER1);
            serverUrls.add(DEFAULT_UPGRADE_SERVER2);
        }

        return wa;
    }

    public static Optional<WalletAssembly> fromDefaultFile(){
        try {
            Path defaultFilePath = Paths.get(Utilities.getJarDir(WalletAssembly.class), WALLET_ASSEMBLY_FILE);
            return Optional.of(fromFile(defaultFilePath));
        }
        catch (Exception ex){
            return Optional.empty();
        }
    }

    public static WalletAssembly fromFile(Path filePath)throws IOException{
        byte[] bytes = Files.readAllBytes(filePath);
        return fromJson(new String(bytes, Charset.defaultCharset()));
    }
}

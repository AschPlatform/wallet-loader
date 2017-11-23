package so.asch.wallet.loader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.Base64;

public class Artifact{

    static{
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        md5 = m;
    }

    private static final MessageDigest md5;
    private String name;
    private Long buildTime;
    private Long size;
    private String checksum;
    private boolean zipped;

    public void setName(String name) {
        this.name = name;
    }

    public void setBuildTime(Long buildTime) {
        this.buildTime = buildTime;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public void setZipped(boolean zipped) {
        this.zipped = zipped;
    }

    public String getName() {
        return name;
    }

    public Long getBuildTime() {
        return buildTime;
    }

    public String getChecksum() {
        return checksum;
    }

    public boolean isZipped() {
        return zipped;
    }

    public Long getSize() {
        return size;
    }

    public Artifact(){}

    public Artifact(String name, Long buildTime, Long size, String checksum, boolean zipped) {
        this.name = name;
        this.buildTime = buildTime;
        this.size = size;
        this.checksum = checksum;
        this.zipped = zipped;
    }


    public boolean isSameVersion(Artifact another){
        return another != null &&
                this.getName().equals(another.getName()) &&
                //this.getBuildTime().equals(another.getBuildTime()) &&
                this.getChecksum().equals(another.getChecksum());
    }

    protected static String calcChecksum(File file) throws IOException {

        try (InputStream is = Files.newInputStream(file.toPath(), StandardOpenOption.READ );
             DigestInputStream dis = new DigestInputStream(is, md5)) {

            byte[] buffer = new byte[65536];
            while(dis.read(buffer) > 0);
        }

        byte[] digest = md5.digest();
        return Base64.getEncoder().encodeToString(digest);
    }

    public static Artifact fromFile(File file){
        try{
            Path path = file.toPath();
            return new Artifact(file.getName(), Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toMillis(),
                    Files.size(path), calcChecksum(file), false);
        }
        catch (IOException ex){
            ex.printStackTrace();
            return null;
        }
    }
}

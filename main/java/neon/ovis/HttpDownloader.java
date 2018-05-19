package neon.ovis;

import java.io.IOException;

public class HttpDownloader {
    public static void dl(final String[] args)
    {
        try
        {
            String fileURL = args[0];
            String saveDir = args[1];
            HttpDownloadUtility.downloadFile(fileURL, saveDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

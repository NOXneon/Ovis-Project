package neon.ovis;

import java.io.IOException;

public class HttpDownloader {
    public static void dl(final String[] args)
    {
        final String fileURL = args[0];
        final String saveDir = args[1];

        try
        {
            HttpDownloadUtility.downloadFile(fileURL, saveDir);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

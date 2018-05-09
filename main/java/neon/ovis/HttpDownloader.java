package neon.ovis;

import java.io.IOException;

public class HttpDownloader {
    public static void main(final String[] args) throws java.io.FileNotFoundException
    {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try
                {
                    //String fileURL = "http://gobierno.euitio.uniovi.es/grado/gd/?y=17-18&t=S2&uo=dsUO257514";
                    String fileURL = args[0];

                    //String saveDir = "C://Users/wafig/Desktop";
                    String saveDir = args[1];
                    HttpDownloadUtility.downloadFile(fileURL, saveDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
}

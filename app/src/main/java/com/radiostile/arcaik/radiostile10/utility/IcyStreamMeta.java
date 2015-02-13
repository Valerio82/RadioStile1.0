package com.radiostile.arcaik.radiostile10.utility;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IcyStreamMeta {
	private static final String TAG = "RP - IcyStreamMeta";
    private URL streamUrl;
    private Map<String, String> metadata;
    private boolean isError;

    public IcyStreamMeta(URL streamUrl) {
        setStreamUrl(streamUrl);

        isError = false;
    }

    public URL getStreamUrl() {
        return streamUrl;
    }

    /**
     * Get stream title
     * 
     * @return String
     * @throws java.io.IOException
     */
    public String getStreamTitle() throws IOException {
        Map<String, String> data = getMetadata();

        if (data == null) {
            return "Artista Sconosciuto - Titolo Sconosciuto";
        }

        if (!data.containsKey("StreamTitle"))
            return "Artista sconosciuto - Titolo sconosciuto";//Modifica fatta da me

        return data.get("StreamTitle").trim();
    }

    /**
     * Get artist using stream's title
     * 
     * @return String
     * @throws java.io.IOException
     */
    public String getArtist() throws IOException {
        Map<String, String> data = getMetadata();

        if (!data.containsKey("StreamTitle"))
            return "Artista sconosciuto - Titolo sconosciuto";//Modifica fatta da me

        String streamTitle = data.get("StreamTitle");
        String title = streamTitle.substring(0, streamTitle.indexOf("-"));
        return title.trim();
    }

    /**
     * Get title using stream's title
     * 
     * @return String
     * @throws java.io.IOException
     */
    public String getTitle() throws IOException {
        Map<String, String> data = getMetadata();

        if (!data.containsKey("StreamTitle"))
            return "Artista sconosciuto - Titolo sconosciuto";

        String streamTitle = data.get("StreamTitle");
        String artist = streamTitle.substring(streamTitle.indexOf("-") + 1);
        return artist.trim();
    }

    public void refreshMeta() throws IOException {
        retreiveMetadata();
    }

    private Map<String, String> getMetadata() throws IOException {
        if (metadata == null) {
            refreshMeta();
        }

        return metadata;
    }

    private void retreiveMetadata() throws IOException {
        URLConnection con = streamUrl.openConnection();
        con.setRequestProperty("Icy-MetaData", "1");
        con.setRequestProperty("Connection", "close");
        con.connect();

        int metaDataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        if (headers.containsKey("icy-metaint")) {
            // Headers are sent via HTTP
            metaDataOffset = Integer.parseInt(headers.get("icy-metaint").get(0));
        }
        // else: There would be a way to get it from the stream content, but it is inefficient, and unneeded for our
        // streams

        // In case no data was sent
        if (metaDataOffset == 0) {
            isError = true;
            Log.e(TAG, "retreiveMetadata - no offset");
            return;
        }

        // Read metadata
        int b;
        int count = 0;
        int metaDataLength = 4080; // 4080 is the max length
        boolean inData = false;
        StringBuilder metaData = new StringBuilder();
        // Stream position should be either at the beginning or right after headers
        while ((b = stream.read()) != -1) {
            count++;

            // Length of the metadata
            if (count == metaDataOffset + 1) {
                metaDataLength = b * 16;
            }

            if (count > metaDataOffset + 1 && count < (metaDataOffset + metaDataLength)) {
                inData = true;
            } else {
                inData = false;
            }
            if (inData) {
                if (b != 0) {
                    metaData.append((char) b);
                }
            }
            if (count > (metaDataOffset + metaDataLength)) {
                break;
            }

        }

        // Set the data
        metadata = IcyStreamMeta.parseMetadata(metaData.toString());

        // Close
        stream.close();
    }

    public boolean isError() {
        return isError;
    }

    private void setStreamUrl(URL streamUrl) {
        this.metadata = null;
        this.streamUrl = streamUrl;
        this.isError = false;
    }

    private static Map<String, String> parseMetadata(String metaString) {
        Map<String, String> metadata = new HashMap<String, String>();
        String[] metaParts = metaString.split(";");
        Pattern p = Pattern.compile("^([a-zA-Z]+)=\\'([^\\']*)\\'$");
        Matcher m;
        for (int i = 0; i < metaParts.length; i++) {
            m = p.matcher(metaParts[i]);
            if (m.find()) {
                metadata.put(((String) m.group(1)).trim(), ((String) m.group(2)).trim());
            }
        }

        return metadata;
    }
}

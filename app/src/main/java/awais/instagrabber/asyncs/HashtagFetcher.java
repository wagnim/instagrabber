package awais.instagrabber.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

import awais.instagrabber.BuildConfig;
import awais.instagrabber.interfaces.FetchListener;
import awais.instagrabber.models.HashtagModel;
import awais.instagrabber.utils.Constants;
import awais.instagrabber.utils.Utils;
import awaisomereport.LogCollector;

import static awais.instagrabber.utils.Utils.logCollector;

public final class HashtagFetcher extends AsyncTask<Void, Void, HashtagModel> {
    private final FetchListener<HashtagModel> fetchListener;
    private final String hashtag;

    public HashtagFetcher(String hashtag, FetchListener<HashtagModel> fetchListener) {
        this.hashtag = hashtag;
        this.fetchListener = fetchListener;
    }

    @Nullable
    @Override
    protected HashtagModel doInBackground(final Void... voids) {
        HashtagModel result = null;

        try {
            final HttpURLConnection conn = (HttpURLConnection) new URL("https://www.instagram.com/explore/tags/" + hashtag + "/?__a=1").openConnection();
            conn.setUseCaches(true);
            conn.connect();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                final JSONObject user = new JSONObject(Utils.readFromConnection(conn)).getJSONObject("graphql").getJSONObject(Constants.EXTRAS_HASHTAG);

                final JSONObject timelineMedia = user.getJSONObject("edge_hashtag_to_media");
                if (timelineMedia.has("edges")) {
                    final JSONArray edges = timelineMedia.getJSONArray("edges");
                }

                result = new HashtagModel(
                        user.getString(Constants.EXTRAS_ID),
                        user.getString("name"),
                        user.getString("profile_pic_url"),
                        timelineMedia.getLong("count"),
                        user.optBoolean("is_following"));
            }

            conn.disconnect();
        } catch (final Exception e) {
            if (logCollector != null)
                logCollector.appendException(e, LogCollector.LogFile.ASYNC_HASHTAG_FETCHER, "doInBackground");
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }

        return result;
    }

    @Override
    protected void onPostExecute(final HashtagModel result) {
        if (fetchListener != null) fetchListener.onResult(result);
    }
}

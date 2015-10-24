package com.example.android.news.app;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.news.app.data.NewsContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//import android.app.Fragment;


/**
 * A placeholder fragment containing a simple view.
 */
public class ListFragment extends Fragment {

    ArrayAdapter<String> mNewsAdapter;
    String[] mNewsDetailStr;

    public ListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.listfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateNews();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Create some dummy data for the ListView.  Here's a sample weekly forecast
        String[] data = {
                "title - Wed, 14 Oct 2015 03:42:00 -0700 - content - link"
        };
        List<String> dummyNews = new ArrayList<String>(Arrays.asList(data));


        // The ArrayAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mNewsAdapter =
                new ArrayAdapter<String>(
                        getActivity(), // The current context (this activity)
                        R.layout.list_item_forecast, // The name of the layout ID.
                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
                        new ArrayList<String>());

        View rootView =  inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mNewsAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                String detailNews = mNewsDetailStr[position];
                String listIndex = String.valueOf(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, listIndex);
                startActivity(intent);
            }
        });

        return rootView;
    }

    private void updateNews() {
        FetchNewsTask newsTask = new FetchNewsTask(getContext());
        newsTask.execute();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateNews();
    }

    public class FetchNewsTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchNewsTask.class.getSimpleName();
        private final Context mContext;

        public FetchNewsTask(Context context) {
            mContext = context;
        }

        int clearNews() {
            int result = mContext.getContentResolver().delete(NewsContract.NewsEntry.CONTENT_URI, null, null);
            return result;
        }

        long insertNews(String listindex, String link, String date, String title, String content) {
            long newsId;

            // Now that the content provider is set up, inserting rows of data is pretty simple.
            // First create a ContentValues object to hold the data you want to insert.
            ContentValues newsValues = new ContentValues();

            // Then add the data, along with the corresponding name of the data type,
            // so the content provider knows what kind of value is being inserted.
            newsValues.put(NewsContract.NewsEntry.COLUMN_LISTINDEX, listindex);
            newsValues.put(NewsContract.NewsEntry.COLUMN_LINK, link);
            newsValues.put(NewsContract.NewsEntry.COLUMN_DATE, date);
            newsValues.put(NewsContract.NewsEntry.COLUMN_TITLE, title);
            newsValues.put(NewsContract.NewsEntry.COLUMN_CONTENT, content);

            // Finally, insert location data into the database.
            Uri insertedUri = mContext.getContentResolver().insert(
                    NewsContract.NewsEntry.CONTENT_URI,
                    newsValues
            );

            // The resulting URI contains the ID for the row.  Extract the locationId from the Uri.
            newsId = ContentUris.parseId(insertedUri);

            return newsId;
        }

        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         */
        private String[] getNewsDataFromJson(String newsJsonStr, int numNews)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_RESPONSE = "responseData";
            final String OWM_FEED = "feed";
            final String OWM_LIST = "entries";
            final String OWM_TITLE = "title";
            final String OWM_LINK = "link";
            final String OWM_CONTENT = "contentSnippet";
            final String OWM_DATE = "publishedDate";

            JSONObject newsJson = new JSONObject(newsJsonStr);
            JSONObject resJson = newsJson.getJSONObject(OWM_RESPONSE);
            JSONObject feedJson = resJson.getJSONObject(OWM_FEED);
            JSONArray newsArray = feedJson.getJSONArray(OWM_LIST);

            String[] resultStrs = new String[numNews];
            mNewsDetailStr = new String[numNews];

            clearNews();
            for(int i = 0; i < newsArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String listindex;
                String title;
                String link;
                String content;
                String date;

                listindex = String.valueOf(i);

                // Get the JSON object representing the day
                JSONObject elementNews = newsArray.getJSONObject(i);

                // description is in a child array called "weather", which is 1 element long.
                title = elementNews.getString(OWM_TITLE);
                link = elementNews.getString(OWM_LINK);
                content = elementNews.getString(OWM_CONTENT);
                date = elementNews.getString(OWM_DATE);

                resultStrs[i] = title + " - " + date;
//                mNewsDetailStr[i] = content + " - " + date + " - " + link;

                insertNews( listindex, link, date, title, content );
            }
            return resultStrs;
        }

        @Override
        protected String[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String newsJsonStr = null;

            String version = "1.0";
            String query = "http://rss.itmedia.co.jp/rss/2.0/news_bursts.xml";
            int numNews = 8;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String NEWS_BASE_URL =
                        "http://ajax.googleapis.com/ajax/services/feed/load?";
                final String QUERY_PARAM = "q";
                final String VERSION_PARAM = "v";
                final String NEWS_PARAM = "num";

                Uri builtUri = Uri.parse(NEWS_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, query)
                        .appendQueryParameter(VERSION_PARAM, version)
                        .appendQueryParameter(NEWS_PARAM, Integer.toString(numNews))
                        .build();

                URL url = new URL(builtUri.toString());


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                newsJsonStr = buffer.toString();

            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try {
                return getNewsDataFromJson(newsJsonStr, numNews);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if (result != null) {
                mNewsAdapter.clear();
                for(String elementNewsStr : result) {
                    mNewsAdapter.add(elementNewsStr);
                }
                // New data is back from the server.  Hooray!
            }
        }
    }

}

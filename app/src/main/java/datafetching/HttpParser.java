package datafetching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import workinginfoclasses.NewsHeadLine;
import workinginfoclasses.ParcelableGroupInfo;
import workinginfoclasses.ParcelableNewsHeadLine;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import news.bbc.bbc_reader.ExpandableNewsListFragment;
import news.bbc.bbc_reader.NewsHeadlinesFragment;

public final class HttpParser {
    private List<NewsHeadLine> lsAllNews = new ArrayList<NewsHeadLine>();
    private ArrayList<ParcelableGroupInfo> columns = new ArrayList<ParcelableGroupInfo>();
    private Context context;
    private HttpTransaction transaction;

    public HttpParser(Context context, HttpTransaction transaction) {
        this.context = context;
        this.transaction = transaction;
    }

    public boolean ParseJSONFile(final ArrayList<String> inputFiles, final String dirPath) {
        ArrayList<String> picsLinksArr = new ArrayList<String>();
        Boolean readingResult = true;

        //Read and parse all files available
        for (String path : inputFiles) {
            try {
                FileReader fileReader = new FileReader(dirPath + path);
                BufferedReader bufferedReder = new BufferedReader(fileReader);

                StringBuilder builder = new StringBuilder();
                String line = null;

                while((line = bufferedReder.readLine()) != null) {
                    builder.append(line);
                }

                //Parse JSON
                JSONArray array = new JSONArray(builder.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    NewsHeadLine headLine = new NewsHeadLine(null,
                            obj.get("title").toString(), obj.get("description").toString(), obj.get("link").toString());
                    this.lsAllNews.add(headLine);
                    picsLinksArr.add(obj.get("media").toString());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
                readingResult = false;
                break;
            }
        }

        //Well, we have only three items.
        //Latter will be compared to more items.
        //For now server has only three of them
        //Also this will prevent from adding more items into groups
        if (columns.size() < 3) {
            columns.add(new ParcelableGroupInfo(
                    "Main Page", "/main_latest"));
            columns.add(new ParcelableGroupInfo(
                    "World", "/world_latest"));
            columns.add(new ParcelableGroupInfo(
                    "Science", "/science_latest"));
        }

        Intent intentColumns = new Intent(ExpandableNewsListFragment.GET_COLUMNS_HEADERS);
        intentColumns.putParcelableArrayListExtra(ExpandableNewsListFragment.COLUMNS_INCOME, columns);
        this.context.sendBroadcast(intentColumns);

        this.transaction.executeBitmapLoading(picsLinksArr);

        return readingResult;
    }

    public void insertBitmaps(Bitmap[] bitmaps) {
        int width = 0;
        int height = 0;
        float aspectRatio = 0.0f;

        for(int i = 0; i < bitmaps.length; i++) {
            if (bitmaps[i] != null) {
                width = bitmaps[i].getWidth();
                height = bitmaps[i].getHeight();
                aspectRatio = (float) width / (float) height;
                this.lsAllNews.get(i).setBitmap(Bitmap.createScaledBitmap(bitmaps[i], 100, (int) (100 / aspectRatio), true));

                try {
                    FileOutputStream out = new FileOutputStream(new File(this.transaction.getCacheFile().getAbsolutePath(),
                            Integer.toString(i) + ".png"));
                    bitmaps[i].compress(Bitmap.CompressFormat.PNG, 100, out);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.lsAllNews.get(i).setBitmap(null);
            }
        }

        //Let's send the size of the array first to the NewsHeadlinesFragment
        //Then let's send news in small chunks
        //It is receiver responsibility to check
        //How much it should receive before drawing UI

        Intent intent = new Intent(NewsHeadlinesFragment.PUBLISHER_ACTION_NUMBER);
        intent.putExtra(NewsHeadlinesFragment.TOP_NEWS_NUMBER, String.valueOf(this.lsAllNews.size()));
        this.context.sendBroadcast(intent);

        //Only restricted amount of information can be send with intent
        //Let's send it by small chunks of 10 and collect on the other side

        intent.removeExtra(NewsHeadlinesFragment.TOP_NEWS_NUMBER);
        intent.setAction(NewsHeadlinesFragment.PUBLISHER_ACTION);

        for(int i = 0; i < this.lsAllNews.size(); i++) {
            ParcelableNewsHeadLine parcelableHeadline =
                    new ParcelableNewsHeadLine(this.lsAllNews.get(i));
            intent.removeExtra(NewsHeadlinesFragment.TOP_NEWS_KEY_ARRAY);
            intent.putExtra(NewsHeadlinesFragment.TOP_NEWS_KEY_ARRAY, parcelableHeadline);
            this.context.sendBroadcast(intent);
        }
    }

    //We need this method only for back compatibility
    public void sendFileNewsURL(final String path) {
        Intent intent = new Intent(NewsHeadlinesFragment.SHOW_SINGLE_NEWS_ACTION);
        try {
            URL url = new File(path).toURI().toURL();
            intent.putExtra(NewsHeadlinesFragment.SHOW_SINGLE_NEWS_ACTION_KEY, url.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        HttpParser.this.context.sendBroadcast(intent);
    }

    public void ParseGroupNewsExecute(final String... params) {
        new ParseGroupNewsExecuteAsyncTask().execute(params);
    }

    private class ParseGroupNewsExecuteAsyncTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            this.progress = ProgressDialog.show(context, "", "Loading. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected String[] doInBackground(String... params) {
            ArrayList<String> picsLinksArr = new ArrayList<String>();
            HttpParser.this.lsAllNews.clear();

            try {
                FileReader fileReader = new FileReader(params[0]);
                BufferedReader bufferedReder = new BufferedReader(fileReader);

                StringBuilder builder = new StringBuilder();
                String line = null;

                while((line = bufferedReder.readLine()) != null) {
                    builder.append(line);
                }

                //Parse JSON
                JSONArray array = new JSONArray(builder.toString());
                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    NewsHeadLine headLine = new NewsHeadLine(null,
                            obj.get("title").toString(), obj.get("description").toString(), obj.get("link").toString());
                    headLine.setCategory(params[1]);
                    HttpParser.this.lsAllNews.add(headLine);
                    picsLinksArr.add(obj.get("media").toString());
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return picsLinksArr.toArray(new String[0]);
        }

        @Override
        public void onPostExecute(String[] links)
        {
            HttpParser.this.transaction.executeBitmapLoadingGroup(links);
            this.progress.dismiss();
        }
    }

    public void insertBitmapsGroups(Bitmap[] bitmaps) {
        ArrayList<ParcelableNewsHeadLine> ls = new ArrayList<ParcelableNewsHeadLine>();
        int width = 0;
        int height = 0;
        float aspectRatio = 0.0f;

        for(int i = 0; i < bitmaps.length; i++) {
            width = bitmaps[i].getWidth();
            height = bitmaps[i].getHeight();
            aspectRatio = (float)width / (float)height;
            this.lsAllNews.get(i).setBitmap(Bitmap.createScaledBitmap(bitmaps[i], 100, (int)(100 / aspectRatio), true));

            try {
                FileOutputStream out = new FileOutputStream(new File(this.transaction.getCacheFile().getAbsolutePath(),
                        i + ".png"));
                bitmaps[i].compress(Bitmap.CompressFormat.PNG, 100, out);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //Let's send the size of the array first to the NewsHeadlinesFragment
        //Then let's send news in small chunks
        //It is receiver responsibility to check
        //How much it should receive before drawing UI

        Intent intent = new Intent(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE_NUMBER);
        intent.putExtra(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE_NEWS_NUMBER, String.valueOf(this.lsAllNews.size()));
        this.context.sendBroadcast(intent);

        //Only restricted amount of information can be send with intent
        //Let's send it by small chunks of 10 and collect on the other side

        intent.removeExtra(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE_NEWS_NUMBER);
        intent.setAction(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE);

        for(int i = 0; i < this.lsAllNews.size(); i++) {
            ParcelableNewsHeadLine parcelableHeadline =
                    new ParcelableNewsHeadLine(this.lsAllNews.get(i));
            intent.removeExtra(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE_KEY_1);
            intent.putExtra(ExpandableNewsListFragment.ON_NEWS_HEADERS_RECEIVE_KEY_1, parcelableHeadline);
            this.context.sendBroadcast(intent);
        }
    }
}

package datafetching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import news.bbc.bbc_reader.MainBBCNewsReader;
import news.bbc.bbc_reader.R;

public final class HttpTransaction
{
    private static final String CRITICAL_ERROR = "critical_error";

    private ProgressDialog progressDialog;
    private final static String LOADING_STR = "Loading. Please, wait...";
    private final static String EXCHANGE_COURSES_URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";
    private File cacheFile;
    private LoadUrlPage lUrl;
    private Context context;
    private HttpParser parser;
    private XmlParser xmlParser;

    public HttpTransaction(Context context) {
        this.context = context;
        this.parser = new HttpParser(this.context, this);
        this.xmlParser = new XmlParser(this.context);

        if(android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            this.cacheFile = new File(android.os.Environment.getExternalStorageDirectory(), "BBCCache/Cache");	//Hidden directory(Not for now)
        } else {
            this.cacheFile = this.context.getCacheDir();
        }

        if(!this.cacheFile.exists()) {
            this.cacheFile.mkdirs();
        }
    }

    public void LoadRequest(String... addressString) {
        ConnectivityManager connManager = (ConnectivityManager)
                this.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInf = connManager.getActiveNetworkInfo();
        if(networkInf != null && networkInf.isConnected()) {
            lUrl = new LoadUrlPage();
            this.lUrl.execute(addressString);
        } else {
            this.ShowWarningDialog("There is no active networks", true);
        }
    }

    public void executeBitmapLoading(ArrayList<String> params) {
        String[] links = params.toArray(new String[0]);
        new LoadBitmap().execute(links);
    }

    private class LoadBitmap extends AsyncTask<String, Void, Bitmap[]> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            this.progress = ProgressDialog.show(context, "",
                    "Loading pictures. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected Bitmap[] doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection connection;
            Bitmap[] bitmaps = new Bitmap[params.length];
            int width = 0;
            int height = 0;
            float aspectRatio = 0.0f;

            for(int i = 0; i < params.length; i++) {
                try {
                    if (params[i] != null) {
                        URL url = new URL(params[i]);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setDoInput(true);
                        connection.connect();

                        int response = connection.getResponseCode();
                        if (response != 200) {
                            HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), true);
                            connection.disconnect();
                            return null;
                        }

                        inputStream = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //Bitmap is scaled to save memory
                        width = bitmap.getWidth();
                        height = bitmap.getHeight();
                        aspectRatio = (float) width / (float) height;
                        bitmaps[i] = Bitmap.createScaledBitmap(bitmap, 350, (int) (350 / aspectRatio), true);
                        inputStream.close();
                        connection.disconnect();
                    } else {
                        //Will guaranty the exact same size of the array as original news array
                        bitmaps[i] = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps) {
            HttpTransaction.this.parser.insertBitmaps(bitmaps);
            this.progress.dismiss();
        }
    }

    //String = params; Void = progress; ArrayList<String> = result
    private class LoadUrlPage extends AsyncTask<String, Void, ArrayList<String>> {
        @Override
        protected void onPreExecute() {
            HttpTransaction.this.progressDialog = ProgressDialog.show(context, "",
                    LOADING_STR, true);
            HttpTransaction.this.progressDialog.setCancelable(false);
        }

        @Override
        protected ArrayList<String> doInBackground(String... urlString) {
            try {
                // The number of available channels presented on server side
                // TO DO: make it fetch them all
                final int magic_number = 3;
                ArrayList<String> lsUrls = new ArrayList<>();
                // Construct an url. Here all three type of news will be requested
                // Since on main page we want to display only news presented in
                // server's database, we need only first three values passed from the main activity
                // They are constants for now. TO DO: it is possible to make a separate request to the server
                // and fetch needed addresses from it.
                for (int i = 1; i <= magic_number; i++) {
                    String res = HttpTransaction.this.DownloadJSON(urlString[0] + urlString[i]);
                    //Well, we do not want to get a bunch of alert messages here.
                    //Break the loop
                    if (res.equals(CRITICAL_ERROR))
                        break;

                    try {
                        if (HttpTransaction.this.cacheFile.canWrite() && res != null) {
                            FileOutputStream fos = new FileOutputStream(new File(HttpTransaction.this.cacheFile, urlString[i]));
                            OutputStreamWriter out = new OutputStreamWriter(fos);
                            out.write(res);
                            out.flush();
                            out.close();
                            lsUrls.add(urlString[i]);
                        } else {
                            HttpTransaction.this.ShowWarningDialog("Cache cannot be written", true);
                            break;
                        }
                    } catch (IOException e) {
                        HttpTransaction.this.ShowWarningDialog(e.getLocalizedMessage(), true);
                        e.printStackTrace();
                        break;
                    }
                }
                return lsUrls;
            } catch(Exception e) {
                HttpTransaction.this.progressDialog.dismiss();
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(final ArrayList<String> lsPaths) {
            if (lsPaths != null && !lsPaths.isEmpty()) {
                HttpTransaction.this.ParseFiles(lsPaths);
            }
            HttpTransaction.this.progressDialog.dismiss();
        }
    }

    private String DownloadJSON(final String urlString) {
        InputStream inputStream = null;
        HttpURLConnection connection = null;

        try {
            URL url = new URL(urlString);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("content-type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setDoInput(true);
            connection.connect();

            int response = connection.getResponseCode();
            //On read and update responses from server
            if(response != 200 && response != 201 && response != 202) {
                HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), false);
                connection.disconnect();
                return null;
            }

            inputStream = connection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder total = new StringBuilder();
            String line = null;

            while((line = br.readLine()) != null) {
                total.append(line);
            }

            inputStream.close();
            connection.disconnect();

            return total.toString();
        } catch(IOException e) {
            HttpTransaction.this.progressDialog.dismiss();
            HttpTransaction.this.ShowWarningDialog(e.getLocalizedMessage(), true);
            e.printStackTrace();
            return CRITICAL_ERROR;
        }
    }

    private void ParseFiles(final ArrayList<String> lsPaths) {
        Boolean result = this.parser.ParseJSONFile(lsPaths, this.cacheFile.getAbsolutePath());

        if (!result) {
            ShowWarningDialog("Error occured during file reading. Application will be closed", true);
        }
    }

    private void ShowWarningDialog(final String error, final boolean isCritical) {
        Activity activity = (Activity) this.context;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(HttpTransaction.this.context);
                builder.setTitle("Error").setIcon(R.drawable.dialog_warning)
                        .setNegativeButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                dialog.dismiss();
                                if(isCritical) {
                                    ((MainBBCNewsReader)HttpTransaction.this.context).finish();
                                }
                            }
                        }).setCancelable(false).setMessage(error);

                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public File getCacheFile()
    {
        return this.cacheFile;
    }

    public void executeNewsFetching(final String header, final String ref) {
        String title = header;
        if(title.contains("Other news: ")) {
            title = title.replace("Other news: ", "");
        }

        File file = new File(this.cacheFile, title.replaceAll("[-+.^:,~]","") + ".html");
        if(file.exists()) {
            this.parser.sendFileNewsURL(file.getAbsolutePath());
        } else {
            new NewsDownloadingAsyncTask().execute(ref, title);
        }
    }

    public class NewsDownloadingAsyncTask extends AsyncTask<String, Void, String> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            this.progress = ProgressDialog.show(HttpTransaction.this.context, "", "News is being loaded. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection connection = null;

            String fileName = params[1].replaceAll("[-+.^:,~]","");

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoInput(true);
                connection.connect();

                int response = connection.getResponseCode();
                if(response != 200) {
                    HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), true);
                    connection.disconnect();
                    return null;
                }

                inputStream = connection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line = null;

                while((line = br.readLine()) != null) {
                    total.append(line);
                }

                inputStream.close();
                connection.disconnect();

                try {
                    if(HttpTransaction.this.cacheFile.canWrite()) {
                        File file = new File(HttpTransaction.this.cacheFile, fileName + ".html");
                        FileOutputStream fos = new FileOutputStream(file);
                        OutputStreamWriter out = new OutputStreamWriter(fos);
                        out.write(total.toString());
                        out.flush();
                        out.close();
                    } else {
                        HttpTransaction.this.ShowWarningDialog("Cache cannot be written", true);
                    }
                } catch(IOException e) {
                    HttpTransaction.this.ShowWarningDialog(e.getLocalizedMessage(), true);
                }

            } catch (Exception e) {
                connection.disconnect();
                HttpTransaction.this.ShowWarningDialog(e.getMessage(), true);
            }

            return (new File(HttpTransaction.this.cacheFile,  fileName + ".html").getAbsolutePath());
        }

        @Override
        protected void onPostExecute(String path) {
            HttpTransaction.this.parser.sendFileNewsURL(path);
            this.progress.dismiss();
        }
    }

    public void GroupNewsExecute(String... grInfo) {
        if(new File(this.cacheFile, grInfo[0]).exists()) {
            HttpTransaction.this.parser.ParseGroupNewsExecute(new File(this.cacheFile, grInfo[0]).getAbsolutePath(), grInfo[1]);
        } else {
            new GroupNewsDownloadingAsyncTask().execute(grInfo[0], grInfo[1], grInfo[2]);
        }
    }

    private class GroupNewsDownloadingAsyncTask extends AsyncTask<String, Void, String[]> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            this.progress = ProgressDialog.show(context, "", "Loading. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected String[] doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(params[2] + params[0]);
                connection = (HttpURLConnection)url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11");
                connection.setRequestProperty("Accept", "*/*");
                connection.setDoInput(true);
                connection.connect();

                int response = connection.getResponseCode();
                if(response != 201 && response != 202) {
                    connection.disconnect();
                    HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), true);
                }

                inputStream = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line = null;

                while((line = br.readLine()) != null) {
                    total.append(line);
                }

                inputStream.close();
                connection.disconnect();
                try {
                    if(HttpTransaction.this.cacheFile.canWrite()) {
                        FileOutputStream fos = new FileOutputStream(new File(HttpTransaction.this.cacheFile, params[0]));
                        OutputStreamWriter out = new OutputStreamWriter(fos);
                        out.write(total.toString());
                        out.flush();
                        out.close();
                    } else {
                        HttpTransaction.this.ShowWarningDialog("Cache cannot be written", true);
                    }
                } catch(IOException e) {
                    HttpTransaction.this.ShowWarningDialog(e.getLocalizedMessage(), true);
                }
            } catch (Exception e) {
                connection.disconnect();
                HttpTransaction.this.ShowWarningDialog(e.getMessage(), true);
                return null;
            }

            String[] retParams = {new File(HttpTransaction.this.cacheFile, params[0]).getAbsolutePath(), params[1]};

            return retParams;
        }

        @Override
        protected void onPostExecute(String... params) {
            HttpTransaction.this.parser.ParseGroupNewsExecute(params);
            this.progress.dismiss();
        }
    }

    public void executeBitmapLoadingGroup(String... params) {
        new LoadBitmapGroup().execute(params);
    }

    private class LoadBitmapGroup extends AsyncTask<String, Void, Bitmap[]> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            this.progress = ProgressDialog.show(context, "",
                    "Loading pictures. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected Bitmap[] doInBackground(String... params) {
            InputStream inputStream = null;
            HttpURLConnection connection;
            Bitmap[] bitmaps = new Bitmap[params.length];
            int width = 0;
            int height = 0;
            float aspectRatio = 0.0f;

            for(int i = 0; i < params.length; i++) {
                try {
                    if (params[i] != null) {
                        URL url = new URL(params[i]);
                        connection = (HttpURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setConnectTimeout(10000);
                        connection.setDoInput(true);
                        connection.connect();

                        int response = connection.getResponseCode();
                        if (response != 200) {
                            HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), true);
                            connection.disconnect();
                            return null;
                        }

                        inputStream = connection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        //Bitmap is scaled to save memory
                        width = bitmap.getWidth();
                        height = bitmap.getHeight();
                        aspectRatio = (float) width / (float) height;
                        bitmaps[i] = Bitmap.createScaledBitmap(bitmap, 350, (int) (350 / aspectRatio), true);
                        inputStream.close();
                        connection.disconnect();
                    } else {
                        //Will guaranty the exact same size of the array as original news array
                        bitmaps[i] = null;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(Bitmap[] bitmaps)
        {
            HttpTransaction.this.parser.insertBitmapsGroups(bitmaps);
            this.progress.dismiss();
        }
    }

    public void RemoveCacheDirectory()
    {
        File[] files = this.cacheFile.listFiles();
        String parent = this.cacheFile.getParent();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        this.cacheFile.delete();
        new File(parent).delete();
    }

    public void ExchangeCoursesLoadExecute()
    {
        File file = new File(HttpTransaction.this.cacheFile, "currency_exchange_courses.xml");
        if(file.exists())
        {
            this.xmlParser.ParseXmlaExecute(file);
        }
        else
        {
            new ExchangeCoursesLoadAsyncTask().execute(EXCHANGE_COURSES_URL);
        }
    }

    private class ExchangeCoursesLoadAsyncTask extends AsyncTask<String, Void, File>
    {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute()
        {
            this.progress = ProgressDialog.show(HttpTransaction.this.context, "", LOADING_STR);
            this.progress.setCancelable(false);
        }

        @Override
        protected File doInBackground(String... params)
        {
            HttpURLConnection connection;
            InputStream inputStream = null;

            try
            {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();

                int response = connection.getResponseCode();
                if(response != 200)
                {
                    connection.disconnect();
                    HttpTransaction.this.ShowWarningDialog(connection.getResponseMessage(), true);
                }

                inputStream = connection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder total = new StringBuilder();
                String line = null;

                while((line = br.readLine()) != null)
                {
                    total.append(line);
                }

                inputStream.close();
                connection.disconnect();

                try
                {
                    if(HttpTransaction.this.cacheFile.canWrite())
                    {
                        FileOutputStream fos = new FileOutputStream(new File(HttpTransaction.this.cacheFile, "currency_exchange_courses.xml"));
                        OutputStreamWriter out = new OutputStreamWriter(fos);
                        out.write(total.toString());
                        out.flush();
                        out.close();
                    }
                    else
                    {
                        HttpTransaction.this.ShowWarningDialog("Cache cannot be written", true);
                    }
                }
                catch(IOException e)
                {
                    HttpTransaction.this.ShowWarningDialog(e.getLocalizedMessage(), true);
                }

            }
            catch (Exception e)
            {
                HttpTransaction.this.ShowWarningDialog(e.getMessage(), true);
            }

            return new File(HttpTransaction.this.cacheFile, "currency_exchange_courses.xml");
        }

        @Override
        protected void onPostExecute(File file)
        {
            HttpTransaction.this.xmlParser.ParseXmlaExecute(file);
            this.progress.dismiss();
        }
    }
}

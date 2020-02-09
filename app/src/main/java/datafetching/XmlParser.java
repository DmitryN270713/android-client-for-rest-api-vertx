package datafetching;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import workinginfoclasses.ParcelableRate;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import news.bbc.bbc_reader.CurrencyExchangeFragment;

public final class XmlParser
{
    private Context context;
    private XmlPullParserFactory factory;
    private XmlPullParser parser;
    private final static String CUBE = "Cube";

    public XmlParser(Context context)
    {
        this.context = context;
    }

    public void ParseXmlaExecute(final File file)
    {
        new ParseXMLAsyncTask().execute(file);
    }

    public class ParseXMLAsyncTask extends AsyncTask<File, Void, ArrayList<ParcelableRate>>
    {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute()
        {
            this.progress = ProgressDialog.show(XmlParser.this.context, "", "Loading. Please, wait...");
            this.progress.setCancelable(false);
        }

        @Override
        protected ArrayList<ParcelableRate> doInBackground(File... params)
        {
            try
            {
                FileInputStream input = new FileInputStream(params[0]);
                InputStream inputStream = new BufferedInputStream(input);

                return XmlParser.this.parseXmlHelper(inputStream);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<ParcelableRate> ls)
        {
            Intent intent = new Intent(CurrencyExchangeFragment.CURRENCY_SENT);
            intent.putParcelableArrayListExtra(CurrencyExchangeFragment.CURRENCY_SENT_KEY, ls);
            XmlParser.this.context.sendBroadcast(intent);

            this.progress.dismiss();
        }
    }

    private ArrayList<ParcelableRate> parseXmlHelper(InputStream inputStream)
    {
        ArrayList<ParcelableRate> ls = new ArrayList<ParcelableRate>();

        try
        {
            this.factory = XmlPullParserFactory.newInstance();
            this.factory.setNamespaceAware(true);
            this.parser = this.factory.newPullParser();
            this.parser.setInput(inputStream, "UTF-8");

            int eventType = this.parser.getEventType();

            while(eventType != XmlPullParser.END_DOCUMENT)
            {
                if(eventType == XmlPullParser.START_TAG)
                {
                    String name = this.parser.getName();
                    if(name.equals(CUBE))
                    {
                        if(this.parser.getAttributeCount() == 2)
                        {
                            ls.add(new ParcelableRate(this.parser.getAttributeValue(0), Float.valueOf(this.parser.getAttributeValue(1))));
                        }
                    }
                }

                eventType = parser.next();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ls;
    }
}

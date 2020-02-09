package news.bbc.bbc_reader;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import workinginfoclasses.NewsHeadLine;

public class NewsAdapter extends ArrayAdapter<NewsHeadLine>
{
    private List<NewsHeadLine> headlines;
    private Context context;

    public NewsAdapter(Context context, int textViewResourceId, List<NewsHeadLine> list)
    {
        super(context, textViewResourceId, list);
        this.headlines = list;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View view = null;			//Since, two different views are used, as templates for the rows
        Bitmap img;
        String headline;
        String description;

        if(view == null)
        {
            LayoutInflater inflater =
                    (LayoutInflater)this.context.getSystemService
                            (Context.LAYOUT_INFLATER_SERVICE);

            if((position % 2) == 0 )
            {
                view = inflater.inflate(R.layout.news_headlines_left, null);
            }
            else
            {
                view = inflater.inflate(R.layout.news_headlines_right, null);
            }
        }

        NewsHeadLine nhLine = this.headlines.get(position);
        if(nhLine != null)
        {
            img = nhLine.getBitmap();
            headline = nhLine.getHeader();
            description = nhLine.getPreviewTxt();
            if((position % 2) == 0 )
            {
                ImageView imgView = (ImageView)view.findViewById(R.id.newsPhotoLeft);
                imgView.setImageBitmap(img);

                TextView headtv = (TextView)view.findViewById(R.id.headlineLeft);
                headtv.setText(headline);

                TextView descriptiontv = (TextView)view.findViewById(R.id.shortDescLeft);
                descriptiontv.setText(description);
            }
            else
            {
                ImageView imgView = (ImageView)view.findViewById(R.id.newsPhotoRight);
                imgView.setImageBitmap(img);

                TextView headtv = (TextView)view.findViewById(R.id.headlineRight);
                headtv.setText(headline);

                TextView descriptiontv = (TextView)view.findViewById(R.id.shortDescRight);
                descriptiontv.setText(description);
            }
        }

        return view;
    }
}

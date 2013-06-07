
package org.openclipart.app;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mcsoxford.rss.MediaThumbnail;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;
import org.openclipart.app.R;

import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {
    private static final String TAG = "Openclipart";

    private static final String Openclipart_URL = "http://openclipart.org";

    private static final String API_URL = Openclipart_URL + "/api/search/?query=water&page=4";

    private ArrayList<HashMap<String, Object>> listItem;

    private ImageView imageView1;

    private TextView aboutTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }

        aboutTxt = (TextView) findViewById(R.id.about);
        Spanned text = Html
                .fromHtml("<b>Openclipart:</b> by <a href=\"http://www.openclipart.org\">openclipart.org</a>");
        aboutTxt.setText(text);
        aboutTxt.setMovementMethod(LinkMovementMethod.getInstance());
        aboutTxt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                invokeBrowse();
            }
        });

        imageView1 = (ImageView) findViewById(R.id.imageView1);

        RSSReader reader = new RSSReader();
        String uri = API_URL;

        listItem = new ArrayList<HashMap<String, Object>>();
        try {
            // use reader.load to parse xml file.
            RSSFeed feed = reader.load(uri);
            TextView tv = (TextView) findViewById(R.id.textView1);
            tv.setText(feed.getDescription());
            Integer it = feed.getItems().size();

            for(int i = 0; i < it; i++) {
                RSSItem item = feed.getItems().get(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("itemtitle", item);
                map.put("itemcontent", "Desc:\n" + item.getDescription());
                // map.put("itemcontent", "");
                listItem.add(map);
            }
        } catch (RSSReaderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            reader.close();
        }
        ListView lv = (ListView) findViewById(R.id.listView1);

        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem, R.layout.item,
                new String[] {
                        "itemtitle", "itemcontent"
                }, new int[] {
                        R.id.title, R.id.content
                });
        lv.setAdapter(listItemAdapter);
        lv.setOnItemClickListener(this);
        reader.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        RSSItem item = (RSSItem) listItem.get(position).get("itemtitle");
        List<MediaThumbnail> thumbnails = item.getThumbnails();
        Log.d(TAG, "title = " + item.getTitle() + ", url = " + item.getLink().toString()
                + ", thumbnails.size = " + thumbnails.size() + ", category = "
                + item.getCategories().size());

        if(thumbnails.size() > 0) {
            MediaThumbnail thumbnail = thumbnails.get(0);
            if(thumbnails != null) {
                Uri uri = thumbnail.getUrl();
                Bitmap b = getHttpBitmap(uri.toString());
                imageView1.setImageDrawable(new BitmapDrawable(b));
            }
        }
    }

    public Bitmap getHttpBitmap(final String url) {
        URL myFileUrl = null;
        Bitmap bitmap = null;
        try {
            Log.d(TAG, url);
            myFileUrl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) myFileUrl.openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
            // BitmapFactory.Options op = new BitmapFactory.Options();
            // op.inSampleSize = 8;
            // bitmap = BitmapFactory.decodeStream(is, null, op);

            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            bitmap.recycle();

            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) myFileUrl.openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
                // BitmapFactory.Options op = new BitmapFactory.Options();
                // op.inSampleSize = 8;
                // bitmap = BitmapFactory.decodeStream(is, null, op);

                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return bitmap;
    }

    public void invokeBrowse() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Openclipart_URL));
        startActivity(intent);
    }
}

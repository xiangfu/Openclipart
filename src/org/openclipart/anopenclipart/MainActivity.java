
package org.openclipart.anopenclipart;

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

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class MainActivity extends Activity implements OnItemClickListener {
    private static final String TAG = "AnOpenclipart";

    private static final String API_URL = "http://openclipart.org/api/search/?query=water&page=4";

    private ArrayList<HashMap<String, Object>> listItem;

    private ImageView imageView1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView1 = (ImageView)findViewById(R.id.imageView1);
        
        RSSReader reader = new RSSReader();
        String uri = API_URL;

        listItem = new ArrayList<HashMap<String, Object>>();
        try {
            // 这是最核心的方法，reader.load会解析url上面的xml文件
            RSSFeed feed = reader.load(uri);
            TextView tv = (TextView) findViewById(R.id.textView1);
            tv.setText(feed.getDescription());
            Integer it = feed.getItems().size();
            // 将所有的解析到的数据加入到listItem中
            for(int i = 0; i < it; i++) {
                RSSItem item = feed.getItems().get(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("itemtitle", item);
                map.put("itemcontent", item.getDescription());
                // map.put("itemcontent", "");
                listItem.add(map);
            }
        } catch (RSSReaderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        ListView lv = (ListView) findViewById(R.id.listView1);
        // 构造一个Adapter
        SimpleAdapter listItemAdapter = new SimpleAdapter(this, listItem, R.layout.item,
                new String[] {
                        "itemtitle", "itemcontent"
                }, new int[] {
                        R.id.title, R.id.content
                });
        lv.setAdapter(listItemAdapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        RSSItem item = (RSSItem) listItem.get(position).get("itemtitle");
        List<MediaThumbnail> thumbnails = item.getThumbnails();
        Log.d(TAG, "title = " + item.getTitle()
                + ", url = " + item.getLink().toString()
                + ", thumbnails.size = " + thumbnails.size()
                + ", category = " + item.getCategories().size());
        
        if(thumbnails.size()>0) {
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
            HttpURLConnection conn = (HttpURLConnection) myFileUrl
                .openConnection();
            conn.setConnectTimeout(0);
            conn.setDoInput(true);
            conn.connect();
            BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
//            BitmapFactory.Options op = new BitmapFactory.Options();
//            op.inSampleSize = 8;
//            bitmap = BitmapFactory.decodeStream(is, null, op);
            
            
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
            bitmap.recycle();

            HttpURLConnection conn;
            try {
                conn = (HttpURLConnection) myFileUrl
                                          .openConnection();
                conn.setConnectTimeout(0);
                conn.setDoInput(true);
                conn.connect();
                BufferedInputStream is = new BufferedInputStream(conn.getInputStream());
//                BitmapFactory.Options op = new BitmapFactory.Options();
//                op.inSampleSize = 8;
//                bitmap = BitmapFactory.decodeStream(is, null, op);
                
                
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        return bitmap;
    }
}

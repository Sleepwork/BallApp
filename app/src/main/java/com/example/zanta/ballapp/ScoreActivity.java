package com.example.zanta.ballapp;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Xml;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zanta on 14/10/2017.
 */

public class ScoreActivity extends AppCompatActivity {

    private ArrayList<Map<String,String>> list = new ArrayList<Map<String,String>>();
    private ArrayAdapter<Map<String,String>> adapter;
    private Map<String, String> maplist;
    private static final int ADDACTIVITY_A_DEMARRER = 2;
    private MediaPlayer legendMusic;
    private ListView mySweetHeart;
    private File mFileOutPut;
    private Intent mIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mySweetHeart = (ListView) findViewById(R.id.list);
        //registerForContextMenu(mySweetHeart);
        mFileOutPut = new File(getBaseContext().getFilesDir(), "listScores.xml");
        Log.i("onCreated" , "drop the bass ");

        loadXml();
        mySweetHeart.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //on récupère la HashMap contenant les infos de notre item (titre, description, img)
                Map<String, String> myMap = new HashMap<String, String>();
                myMap = (Map<String, String>)list.get(position);
                Intent intent = new Intent(getBaseContext(), MapsActivity.class);
                intent.putExtra("lat", myMap.get("lat"));
                intent.putExtra("lng", myMap.get("lng"));
                startActivity(intent);
            }
        });
        sortList();

        legendMusic=MediaPlayer.create(this,R.raw.first_flight);
        legendMusic.setLooping(true);
        legendMusic.start();
        mIntent = getIntent();
        if(mIntent.getBooleanExtra("GameOver", false)){
            Intent intent = new Intent(this, AddScore.class);
            int score = mIntent.getIntExtra("score", -1);
            mIntent.removeExtra("GameOver");
            mIntent.removeExtra("score");
            Log.i("ScoreActivity", ""+score);
            intent.putExtra("score", score);
            startActivityForResult (intent, ADDACTIVITY_A_DEMARRER);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!legendMusic.isPlaying())
            legendMusic.start();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult (int requestCode,
                                     int resultCode, Intent data) {
        switch (requestCode) {
            case ADDACTIVITY_A_DEMARRER:
                switch (resultCode) {
                    case RESULT_OK:
                        maplist = new HashMap<String, String>();
                        maplist.put("ligne1", data.getStringExtra("pseudo"));
                        maplist.put("ligne2", data.getStringExtra("score"));
                        maplist.put("ligne3", ""+0);
                        maplist.put("lat", Double.toString(data.getDoubleExtra("lat", 0)));
                        maplist.put("lng", Double.toString(data.getDoubleExtra("lng", 0)));
                        Log.i("onActivityResult", maplist.get("lat") + "/" + maplist.get("lng"));
                        list.add(maplist);
                        sortList();

                        Toast toast = Toast.makeText(this, "Ajout validé", Toast.LENGTH_LONG);
                        toast.show();

                        data.removeExtra("pseudo");
                        data.removeExtra("score");
                        data.removeExtra("lat");
                        data.removeExtra("lng");
                        break;
                    case RESULT_CANCELED:
                        Toast toast2 = Toast.makeText(this, "Ajout annulé ou Localisation impossible", Toast.LENGTH_LONG);
                        toast2.show();;
                        break;
                }
        }
    }

    public void saveXml(){

        try {
                Log.i("saveXml", "FileOutput");
                FileOutputStream fileOutputStream = new FileOutputStream(mFileOutPut);
                XmlSerializer xmlSerializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();
                xmlSerializer.setOutput(writer);

                //Start Document
                xmlSerializer.startDocument("UTF-8", true);
                xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                Log.i("saveXml", "Open Tag <resources>");
                xmlSerializer.startTag("", "resources");

                for (int i = 0; i < adapter.getCount(); i++) {
                    Map<String, String> map = adapter.getItem(i);
                    xmlSerializer.startTag("", "score");
                    xmlSerializer.attribute("", "pseudo", map.get("ligne1"));
                    xmlSerializer.attribute("", "value", map.get("ligne2"));
                    xmlSerializer.attribute("", "ranking", map.get("ligne3"));
                    xmlSerializer.attribute("", "lat", map.get("lat"));
                    xmlSerializer.attribute("", "lng", map.get("lng"));
                    xmlSerializer.endTag("", "score");
                }

                Log.i("saveXml", "End Tag <resources>");
                xmlSerializer.endTag("", "resources");
                xmlSerializer.endDocument();
                xmlSerializer.flush();
                String dataWrite = writer.toString();
                fileOutputStream.write(dataWrite.getBytes());
                fileOutputStream.close();
                Log.i("saveXml", "fileOuput close");
                Log.i("saveXml", dataWrite);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("saveXml", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("saveXml", e.getMessage());
            }

    }

    public void loadXml(){
        Log.i("loadXml", "Existence:  " +mFileOutPut.exists());
        if(!mFileOutPut.exists()) {
            try {
                XmlPullParser xmlPullParser = getResources().getXml(R.xml.scores);
                //Log.i("onCreate" ,getResources().getXml(R.xml.scores).getNamespace());
                while (xmlPullParser.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (xmlPullParser.getEventType() == XmlPullParser.START_TAG) {
                        if (xmlPullParser.getName().equals("score")) {
                            maplist = new HashMap<String, String>();
                            maplist.put("ligne1", xmlPullParser.getAttributeValue(0));
                            maplist.put("ligne2", xmlPullParser.getAttributeValue(1));
                            maplist.put("ligne3", xmlPullParser.getAttributeValue(2));
                            maplist.put("lat", xmlPullParser.getAttributeValue(3));
                            maplist.put("lng", xmlPullParser.getAttributeValue(4));
                            list.add(maplist);
                        }
                    }
                    xmlPullParser.next();
                }

            } catch (Exception e) {
                Log.i("loadXml", "Erreur = " + e.getMessage());
            }

            String[] from = {"ligne1", "ligne2", "ligne3"};
            int[] to = {R.id.tvPseudo, R.id.tvScore, R.id.tvRanking};

            adapter = new MyArrayAdapter(this, list, from, to);
            mySweetHeart.setAdapter(adapter);


        } else {
            try {
                Log.i("loadXml", "FileInput open");
                InputStream inputStream = new FileInputStream(mFileOutPut);
                XmlPullParser parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(inputStream, null);
                while (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                    if (parser.getEventType() == XmlPullParser.START_TAG) {
                        if (parser.getName().equals("score")) {
                            maplist = new HashMap<String, String>();
                            maplist.put("ligne1", parser.getAttributeValue(0));
                            maplist.put("ligne2", parser.getAttributeValue(1));
                            maplist.put("ligne3", parser.getAttributeValue(2));
                            maplist.put("lat", parser.getAttributeValue(3));
                            maplist.put("lng", parser.getAttributeValue(4));
                            list.add(maplist);
                        }
                    }
                    parser.next();
                }
                inputStream.close();
                Log.i("loadXml", "FileIntput close");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.i("loadXml", e.getMessage());
            } catch (XmlPullParserException e) {
                e.printStackTrace();
                Log.i("loadXml", e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.i("loadXml", e.getMessage());
            }

            String[] from = {"ligne1", "ligne2", "ligne3"};
            int[] to = {R.id.tvPseudo, R.id.tvScore, R.id.tvRanking};

            adapter = new MyArrayAdapter(this, list, from, to);
            mySweetHeart.setAdapter(adapter);
        }
    }

    public void sortList() {
        ArrayList<Map<String, String>> calls = new ArrayList<Map<String, String>>();
        calls.addAll(list);

        // Descending Order
        Collections.sort(calls, new Comparator<Map<String, String>>() {

            @Override
            public int compare(Map<String, String> m1, Map<String, String> m2) {
                return (int)(Integer.parseInt(m2.get("ligne2")) - Integer.parseInt(m1.get("ligne2")));
            }
        });

        int i=1;
        for(Map<String, String> myMap : calls){

            myMap.remove("ligne3");
            myMap.put("ligne3", ""+i);
            Log.i("sortList : ", myMap.get("ligne3") + " - " + myMap.get("ligne1"));
            i++;
        }
        list = calls;
        adapter.clear();
        adapter.addAll(calls);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy(){
        Log.i("onDestroy", "here");
        saveXml();
        legendMusic.stop();
        legendMusic.release();
        super.onDestroy();
    }

}

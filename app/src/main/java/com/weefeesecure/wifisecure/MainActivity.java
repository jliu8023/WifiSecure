package com.weefeesecure.wifisecure;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private TextView mOut;
    private InfoThread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //AsyncTask to display String given on TextView
    private class DisplayTask extends AsyncTask< String, Void, String> {
        protected String doInBackground(String... givenString){
            return givenString[0];
        }

        protected void onPostExecute( String outString ){
            mOut = (TextView) findViewById(R.id.results);
            mOut.append(outString);
        }
    }

    public void startButton(View v){
        Button button = (Button) v;
        //Referencing EditText and TextView
        mOut = (TextView) findViewById(R.id.results);
        //Enable scrolling in TextView for more results
        mOut.setMovementMethod(new ScrollingMovementMethod());

        //Starting thread
        mThread = new InfoThread();
        mThread.start();
    }

    private class InfoThread extends Thread{

        private boolean mRunning = false;

        void Info (){
            if ( ! Thread.interrupted() && mRunning ){
                String display= "\n Hello World" ;
                if ( !Thread.interrupted() ){
                    //Start Monte Carlo polling
                    new DisplayTask().execute(display);
                }

            }
        }
        //Override run() with Monte Carlo task
        @Override
        public void run(){
            mRunning = true;
            Info();
        }

        public void close() {
            mRunning = false;
        }
    }

}

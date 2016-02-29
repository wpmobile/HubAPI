package com.worldpay.hubtest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.worldpay.hub.Hub;
import com.worldpay.hub.MePOS.MePOSHub;


public class MainActivity extends ActionBarActivity implements HubProvider
{
    protected Hub mHub;
    protected Prober mProber;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Hub getHub()
    {
        return mHub;
    }

    @Override
    public void setHub(Hub hub)
    {
        mHub = hub;
    }

    @Override
    public void setProber(Prober prober)
    {
        mProber = prober;
    }

    @Override
    public void probe(MePOSHub hub)
    {
        if(mProber != null)
            mProber.probe(hub);
    }
}

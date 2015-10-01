package com.worldpay.hubtest;

import com.worldpay.hub.Hub;
import com.worldpay.hub.MePOS.MePOSHub;

/**
 * Created by Micro2 on 01/10/2015.
 */
public interface HubProvider
{
    public Hub getHub();
    public void setHub(Hub hub);

    public interface Prober
    {
        public void probe(MePOSHub hub);
    };

    public void setProber(Prober prober);
    public void probe(MePOSHub hub);
}

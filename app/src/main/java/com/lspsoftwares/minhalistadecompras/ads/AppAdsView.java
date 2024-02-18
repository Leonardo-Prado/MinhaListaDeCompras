package com.lspsoftwares.minhalistadecompras.ads;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
public class AppAdsView {
    private AdView view;
    private AdRequest request;

    public AppAdsView(AdView view){
        this.view = view;
        this.request = new AdRequest.Builder().build();
        view.loadAd(request);
    }

    public AdView getView() {
        return view;
    }

    public void setView(AdView view) {
        this.view = view;
    }

    public AdRequest getRequest() {
        return request;
    }

    public void setRequest(AdRequest request) {
        this.request = request;
    }
}

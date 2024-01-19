package com.mollosradix.deals.Fragments;

import static android.content.ContentValues.TAG;

import static it.skrape.core.ParserKt.htmlDocument;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.gargoylesoftware.htmlunit.javascript.host.fetch.Response;
import com.mollosradix.deals.R;

import it.skrape.fetcher.BrowserFetcherKt;
import it.skrape.fetcher.Request;
import it.skrape.fetcher.Result;
import it.skrape.fetcher.BrowserFetcher;
import it.skrape.fetcher.HttpFetcher;

public class HotDeals extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_hot_deals, container, false);
    }
}
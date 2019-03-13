package com.example.nfcreaderapp.record;

import com.google.common.collect.BiMap;

public class UriRecord implements ParsedNdefRecord {

    private static final String TAG = "UriRecord";
    private static final String RECORD_TYPE = "UriRecord";

    private static final BiMap <Byte, String> URI_PREFIX_MAP = null;

    @Override
    public String str() {
        return null;
    }
}

package com.example.dongja94.sampletmap;

import com.skp.Tmap.TMapPOIItem;

/**
 * Created by dongja94 on 2016-03-03.
 */
public class POIItem {
    TMapPOIItem poiitem;

    public POIItem(TMapPOIItem poiitem) {
        this.poiitem = poiitem;
    }

    @Override
    public String toString() {
        return poiitem.getPOIName();
    }
}

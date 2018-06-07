package com.fmwrn.indoors;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

import com.customlbs.library.model.Zone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LocationUtil {

    public static boolean containsZone(List<Zone> zones, Zone zone) {
        for (Zone everyZone: zones) {
            if (everyZone != null && everyZone.getId()== zone.getId()) {
                return true;
            }
        }
        return false;
    }

    public static void removeZone(List<Zone> zones, Zone zone) {
        for (Zone everyZone: zones) {
            if (everyZone != null && everyZone.getId()== zone.getId()) {
                zones.remove(everyZone);
            }
        }
    }

    public static String listZones(List<Zone> zones) {
        ArrayList<String> list = new ArrayList<>();
        for (Zone everyZone: zones) {
            list.add(everyZone.getName());
        }
        return Arrays.toString(list.toArray());
    }

    public static void showToast(Context context, CharSequence text, int duration) {
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }
}

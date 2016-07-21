package com.incirrd.bildiricin.jsonlib;

import android.text.format.Time;

public class FestCountdownTimer {

    private long intervalMillis;

    public FestCountdownTimer(int second, int minute, int hour, int monthDay, int month, int year) {

        Time futureTime = new Time();

        // Gelecekteki zaman�n bilgileri
        futureTime.set(second, minute, hour, monthDay, month, year);
        futureTime.normalize(true);
        long futureMillis = futureTime.toMillis(true);

        Time timeNow = new Time(); 

        // Su anki zaman
        timeNow.setToNow();
        timeNow.normalize(true);
        long nowMillis = timeNow.toMillis(true);

        // Gelecek zamandan �imdiki zaman� ��kartt�m
        intervalMillis = futureMillis - nowMillis;
    }

    public long getIntervalMillis() {
        return intervalMillis;
    }
}
package com.example.eatitv2shipper.common;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;



public class MarkerAnimation {
    public static void animateMarkertoGB(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator)
    {
        LatLng startPosition = marker.getPosition();
        Handler handler = new Handler();
        long start = SystemClock.uptimeMillis();
        Interpolator interpolator = new AccelerateDecelerateInterpolator();
        float durationInMs = 3000; //3s

        handler.post(new Runnable() {
            long elapsed;
            float t,v;
            @Override
            public void run() {
                elapsed = SystemClock.uptimeMillis() - start;
                t = elapsed/durationInMs;
                v = interpolator.getInterpolation(t);

                marker.setPosition(latLngInterpolator.interpolate(v,
                        startPosition,
                        finalPosition));

                //Repeat till progress is complete
                if (t<1)
                {
                    //Post again 16ms lateer
                    handler.postDelayed(this,16);
                }
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void animateMarkertoHC(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator)
    {
        LatLng startLocation = marker.getPosition();

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.addUpdateListener(animation -> {
            float v = valueAnimator.getAnimatedFraction();
            LatLng newPosition = latLngInterpolator.interpolate(v,startLocation,finalPosition);
            marker.setPosition(newPosition);
        });

        valueAnimator.setFloatValues(0,1);
        valueAnimator.setDuration(3000);
        valueAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static void animateMarkertoICS(final Marker marker,
                                         LatLng finalPosition,
                                         LatLngInterpolator latLngInterpolator)
    {
        TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
            @Override
            public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
                return latLngInterpolator.interpolate(fraction,startValue,endValue);
            }
        };
        Property<Marker,LatLng> property = Property.of(Marker.class,LatLng.class,"position");
        ObjectAnimator animator = ObjectAnimator.ofObject(marker,property,typeEvaluator,finalPosition);
        animator.setDuration(3000);
        animator.start();
    }
}

package com.rohith.autobotserver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;

import static android.content.Context.SENSOR_SERVICE;

public class Navigation implements LocationListener, SensorEventListener {
    public static final int FORWARD = 2, LEFT = 3, RIGHT = 4;
    private LocationManager locationManager;
    MainActivity context;
    Location loc, dLoc = new Location("");
    SensorManager sManager;
    float rotation, bearing;
    boolean canF = true, canR = true, canL = true;
    DatabaseReference pf = MainActivity.mDatabase.getReference("todo/t");

    public Navigation(MainActivity c) {
        context = c;
        locationManager = (LocationManager) c.getSystemService(Context.LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (c.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return ;
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0,   0, this);
        sManager = (SensorManager) c.getSystemService(SENSOR_SERVICE);
        //sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),SensorManager.SENSOR_DELAY_FASTEST);
        sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),SensorManager.SENSOR_DELAY_NORMAL);
        //sManager.registerListener(this, sManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),SensorManager.SENSOR_DELAY_NORMAL);
        System.out.println("Nav Started");
        DatabaseReference lat = MainActivity.mDatabase.getReference("dloc/lat");
        DatabaseReference lon = MainActivity.mDatabase.getReference("dloc/lon");
        DatabaseReference cf = MainActivity.mDatabase.getReference("possib/f");
        DatabaseReference cl = MainActivity.mDatabase.getReference("possib/l");
        DatabaseReference cr = MainActivity.mDatabase.getReference("possib/r");
        DatabaseReference upd = MainActivity.mDatabase.getReference("c/i");

        upd.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!context.sendText.getText().toString().equals("z")){
                    if (loc != null)
                        System.out.print(Angle() + ", " + rotation + ", " + loc.bearingTo(dLoc) + ", ");
                    try {
                        byte b = possibDir();
                        System.out.println((char) b);
                        if (context.btOutStream != null) {
                            context.btOutStream.write(b);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        lon.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dLoc.setLongitude((double)dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        lat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dLoc.setLatitude((double)dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        lon.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dLoc.setLongitude((double)dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                canF = "True".equals(dataSnapshot.getValue());
                System.out.println("F = " + canF);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cl.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                canL = "True".equals(dataSnapshot.getValue());
                System.out.println("L = " + canL);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        cr.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                canR = "True".equals(dataSnapshot.getValue());
                System.out.println("R = " + canR);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        loc = location;
        DatabaseReference lat = MainActivity.mDatabase.getReference("cloc/lat");
        lat.setValue(loc.getLatitude());
        DatabaseReference lon = MainActivity.mDatabase.getReference("cloc/lon");
        lon.setValue(loc.getLongitude());
        //System.out.println(loc.bearingTo(dLoc));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
    ////////////////////////////////////////////////////////////
    @Override
    public void onSensorChanged(SensorEvent event) {
        rotation = VecToRot(RotVec(event.values, new float[]{0, 1, 0}))[0];
        float rot[] = RotVec(event.values, new float[]{0, 1, 0});
        if (rot[1] > 0) rotation = -rotation;
        //rotation += 90;
        if (!context.sendText.getText().toString().equals("z")){
            if (loc != null)
                System.out.print(Angle() + ", " + rotation + ", " + loc.bearingTo(dLoc) + ", ");
            try {
                byte b = possibDir();
                System.out.println((char) b);
                if (context.btOutStream != null) {
                    context.btOutStream.write(b);
                    pf.setValue(b+"");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public double Angle(){
        if (loc != null)
            return angle(rotation, loc.bearingTo(dLoc));
        else
            return 0;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    static float[] Hpdt(float[] A, float[] B){
        float a1=A[3], a2=B[3];
        float b1=A[0], b2=B[0];
        float c1=A[1], c2=B[1];
        float d1=A[2], d2=B[2];
        return new float[]{
                a1*b2 + b1*a2 + c1*d2 - d1*c2,
                a1*c2 - b1*d2 + c1*a2 + d1*b2,
                a1*d2 + b1*c2 - c1*b2 + d1*a2,
                a1*a2 - b1*b2 - c1*c2 - d1*d2
        };
    }
    static float[] RotVec(float[] uquar, float[] vec){
        float[] r1 = new float[]{-uquar[0],-uquar[1],-uquar[2],uquar[3]};
        float[] p = new float[]{vec[0],vec[1],vec[2],0};
        float[] ret = Hpdt(Hpdt(uquar,p),r1);
        return new float[]{(ret[0]),(ret[1]),(ret[2])};
    }
    static float[] VecToRot(float[] vec){
        float mag = (float)Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
        return new float[]{(float)rad2deg(Math.acos(vec[0]/mag)), (float)rad2deg(Math.acos(vec[1]/mag)), (float)rad2deg(Math.acos(vec[2]/mag))};
    }
    double[] quat2euler(float[] q){
        /*euler-angles*/
        double[] ret = new double[3];

        double psi = Math.atan2( -2.*(q[2]*q[3] - q[0]*q[1]) , q[0]*q[0] - q[1]*q[1]- q[2]*q[2] + q[3]*q[3]);
        double theta = Math.asin( 2.*(q[1]*q[3] + q[0]*q[2]));
        double phi = Math.atan2( 2.*(-q[1]*q[2] + q[0]*q[3]) , q[0]*q[0] + q[1]*q[1] - q[2]*q[2] - q[3]*q[3]);
        /*save var. by simply pushing them back into the array and return*/
        ret[0] = rad2deg(psi);
        ret[1] = rad2deg(theta);
        ret[2] = rad2deg(phi);
        return ret;
    }
    double angle(double a1, double a2){
        /*if (a1 < 0 && a2 > 0) {
            a1 += 180;
            a1 = -a1;
        }
        if (a1 > 0 && a2 < 0){
            a2 += 180;
            a2 = -a2;
        }*/
        return a1 - a2;
    }
    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    static double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
    public byte possibDir(){
        if (loc != null){
            double dist = loc.distanceTo(dLoc);
            if (dist < 10) return (byte)'S';
        }
        boolean canf = canF, canr = canR, canl = canL;
        if (!canf){
            canr = true;
            canl = true;
        }
        double ang = Angle();
        if (Math.abs(ang) < 10){
            if (canf) return (byte)'F'; else {
                if (ang > 0) return (byte)'L'; else return (byte)'R';
            }
        }
        else if (ang < 0){
            System.out.println("\n"+ang);
            if (canr) return (byte)'R'; else {
                if (canf) return (byte)'F'; else return (byte)'L';
            }
        }
        ang = 180 - ang;
        if (ang > 0){

            if (canl) return (byte)'L'; else {
                if (canf) return (byte)'F'; else return (byte)'R';
            }
        } else if (ang < 0){
            System.out.println("\n"+ang);
            if (canr) return (byte)'R'; else {
                if (canf) return (byte)'F'; else return (byte)'L';
            }
        }
        return 0;
    }
}

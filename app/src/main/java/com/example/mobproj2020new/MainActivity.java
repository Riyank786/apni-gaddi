package com.example.mobproj2020new;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.api.Distribution;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity{

    private static CircleImageView btnSettings;
    private static String image;
    private static String uid;
    private List<Route> myOfferedRidesInfoList = new ArrayList<>();
    private List<Route> myBookedRidesInfoList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_pick_up_or_transportation);

        Log.d("TAG", "onCreate: ");
        btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setImageResource(R.drawable.ic_settings_icon_foreground); 

        FirebaseAuth.AuthStateListener als = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    Log.d("TAG", "onAuthStateChanged: true");
                    AppUser.init();
                    CheckProfileCreated();
                }
                else
                {
                    Log.d("TAG", "onAuthStateChanged: false");
                    FirebaseHelper.loggedIn = false;
                    setImageSettings(MainActivity.this);
                }
            }
        };
        FirebaseAuth.getInstance().addAuthStateListener(als);

    }

    private void CheckProfileCreated()
    {
        DatabaseHandler db= new DatabaseHandler();
        db.checkProfileCreated(getApplicationContext());
    }

    public static void setImageSettings(final Context context) {
        Log.d("TAG", "menn????n = setImageSettings: ");
        if (FirebaseHelper.loggedIn) {
            uid = FirebaseAuth.getInstance().getUid();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference("profpics/" + uid);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    try {
                        if (task.getResult() != null) {
                            image = String.valueOf(task.getResult());
                            Log.d("TAG", "onComplete: " + image);
                            Picasso.with(context).load(image).into(btnSettings);
                        }
                    } catch(Exception e) {

                    }
                }
            });


        }
        else{
                btnSettings.setImageResource(R.drawable.ic_settings_icon_foreground);
        }
    }

    //-----------Applications settings button------------//
    final String[] itemListLoggedOut = {"Log In"};
    final String[] itemListLoggedIn = {"My Profile", "My rating", "Sign Out"};
    public void AppSettings(View v) {
        if(FirebaseHelper.loggedIn){
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setItems(itemListLoggedIn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            Log.d("SWAG", "onClick: My Profile");
                            if(FirebaseHelper.loggedIn) {
                                DatabaseHandler db = new DatabaseHandler();
                                Log.d("UID", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                db.GoToProfile(MainActivity.this, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            } else {
                                Log.d("SWAG", "onClick: My Profile Else not logged in");
                                FirebaseHelper.GoToLogin(getApplicationContext());
                            }
                            break;
                        case 1:
                            //Log.d("SWAG", "onClick: My rating");
                            if(FirebaseHelper.loggedIn)
                            {
                                Intent ratingIntent = new Intent(getApplicationContext(), RatingActivity.class);
                                startActivity(ratingIntent);
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "You are not signed in", Toast.LENGTH_LONG).show();
                            }
                            break;
                        case 2:
                            //Log.d("SWAG", "onClick: Sign Out");
                            if(FirebaseHelper.loggedIn) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                Toast.makeText(getApplicationContext(), "Signed out", Toast.LENGTH_LONG).show();
                                startActivity(intent);
                                break;
                            }
                    }
                }
            });
            AlertDialog settingsDialog = builder.create();
            settingsDialog.show();
            settingsDialog.getWindow().setDimAmount(0);
            settingsDialog.getWindow().setLayout(420, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            settingsDialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
            settingsDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.settings_dialog_background));
        }
        else{
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setItems(itemListLoggedOut, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case 0:
                            Log.d("SWAG", "onClick: ");
                            FirebaseHelper.GoToLogin(getApplicationContext());
                            break;
                    }
                }
            });
            AlertDialog settingsDialog = builder.create();
            settingsDialog.show();
            settingsDialog.getWindow().setDimAmount(0);
            settingsDialog.getWindow().setLayout(420, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            settingsDialog.getWindow().setGravity(Gravity.TOP | Gravity.RIGHT);
            settingsDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.settings_dialog_background));
        }
    }

    //----------------Button BookedTrips----------------//
    public void SelectBookedTrips(View v){
        if(FirebaseHelper.loggedIn) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            TextView titleTV = new TextView(this);
            titleTV.setText("Booked trips");
            titleTV.setTextSize(24);
            Typeface face = ResourcesCompat.getFont(this, R.font.montserrat_medium);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            params.setMarginStart(24);
            titleTV.setLayoutParams(params);
            titleTV.setTypeface(face);
            titleTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            titleTV.setTextColor(Color.WHITE);
            builder.setCustomTitle(titleTV);

            myBookedRidesInfoList.clear();
            final RideInfoListAdapter ridesAdapter = new RideInfoListAdapter(this, myBookedRidesInfoList);


            new DatabaseHandler().GetBookedRides(ridesAdapter, myBookedRidesInfoList);

            builder.setAdapter(ridesAdapter, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    final Route r = myBookedRidesInfoList.get(which);
                    Toast.makeText(MainActivity.this, r.getEndAddress() + " ride chosen ", Toast.LENGTH_LONG).show();

                    final DocumentReference userDoc = FirebaseFirestore.getInstance().collection("users").document(r.getUid());
                    userDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                DocumentSnapshot doc = task.getResult();
                                if(doc.exists()) {
                                    User u = doc.toObject(User.class);
                                    Intent i = new Intent(MainActivity.this, BookedTripsInfoActivity.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    i.putExtra("MYKEY1",r);
                                    i.putExtra("MYKEY2", u);
                                    startActivity(i);
                                }
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Error getting ride data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog);
        }
        else {
            FirebaseHelper.GoToLogin(getApplicationContext());
        }
    }

    //---------------Button OfferTrips---------------//
    public void SelectOfferedTrips(View v){
        if(FirebaseHelper.loggedIn)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            TextView titleTV = new TextView(this);
            titleTV.setText("Offered trips");
            titleTV.setTextSize(24);
            Typeface face = ResourcesCompat.getFont(this, R.font.montserrat_medium);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            params.setMarginStart(24);
            titleTV.setLayoutParams(params);
            titleTV.setTypeface(face);
            titleTV.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            titleTV.setTextColor(Color.WHITE);
            builder.setCustomTitle(titleTV);

            myOfferedRidesInfoList.clear();
            final RideInfoListAdapter ridesAdapter = new RideInfoListAdapter(this, myOfferedRidesInfoList);

            new DatabaseHandler().GetOfferedRides(ridesAdapter, myOfferedRidesInfoList);


            builder.setAdapter(ridesAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(MainActivity.this, myOfferedRidesInfoList.get(which).getEndAddress() + " ride chosen ", Toast.LENGTH_LONG).show();

                    /////////////////Offered rides view\\\\\\\\\\\\\\\\\\
                    Intent i = new Intent(MainActivity.this, OfferedTripsInfoActivity.class);
                    i.putExtra("MYKEY", myOfferedRidesInfoList.get(which));
                    startActivity(i);
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.background_dialog);
        }
        else{
            FirebaseHelper.GoToLogin(getApplicationContext());
        }
    }

    //-------------Button Get A Ride----------------//
    public void SelectGetARide(View v){
        Intent GetARideIntent = new Intent(MainActivity.this, GetRideActivity.class);
        startActivity(GetARideIntent);
    }

    //------------Button Offer A Ride--------------//
    public void SelectOfferARide(View v){
        Intent intent = new Intent(MainActivity.this, RouteActivity.class);
        startActivity(intent);
    }

    //Exit app with pressing back putton on your phone
    @Override
    public void onBackPressed() {
        finishAffinity();
        finish();
    }
}
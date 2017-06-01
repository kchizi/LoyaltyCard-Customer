package card.loyalty.loyaltycardcustomer;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

import card.loyalty.loyaltycardcustomer.fragments.CardsRecyclerFragment;
import card.loyalty.loyaltycardcustomer.fragments.QrFragment;

public class CustomerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "CustomerActivity";

    // Navigation Drawer Objects
    private NavigationView nView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;

    // User Info TextViews
    private TextView userNameView;
    private TextView emailView;

    // Firebase Authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);

        // Navigation Drawer
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        nView = (NavigationView) findViewById(R.id.nav_view);
        nView.setNavigationItemSelectedListener(this);

        // Firebase Authentication Initialisation
        mFirebaseAuth = FirebaseAuth.getInstance();

        // Firebase UI Authentication
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged: is signed_in:" + user.getUid());

                    // User Info TextViews
                    userNameView = (TextView) nView.getHeaderView(0).findViewById(R.id.username_view);
                    emailView = (TextView) nView.getHeaderView(0).findViewById(R.id.email_view);
                    userNameView.setText(user.getDisplayName());
                    emailView.setText(user.getEmail());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged: is signed_out");
                }
            }
        };


        // Set QrFragment as the initial fragment
        if (savedInstanceState == null) {
            Fragment frag = new QrFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.content, frag, QrFragment.FRAGMENT_TAG)
                    .commit();
        }

    }

    // On resuming activity
    @Override
    protected void onResume() {

        super.onResume();

        // Add the firebase auth state listener
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    // On pausing activity
    @Override
    protected void onPause() {

        super.onPause();

        // Remove the firebase auth state listener
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    // When back is pressed the drawer must close if open
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    // Called when a navigation drawer item is selected
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        // Get fragment manager
        FragmentManager manager = getSupportFragmentManager();

        // Fragment tag. This must be set when item is selected
        String tag = "";

        // Declare variable for current fragment
        Fragment current = null;

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            // check if fragment exists already
            tag = QrFragment.FRAGMENT_TAG;
            current = manager.findFragmentByTag(tag);
            if (current == null) {
                // create if doesn't exist
                current = new QrFragment();
            }

        } else if (id == R.id.nav_myCards) {

            // check if fragment exists already
            tag = CardsRecyclerFragment.FRAGMENT_TAG;
            current = manager.findFragmentByTag(tag);
            if (current == null) {
                // create if doesn't exist
                current = new CardsRecyclerFragment();
            }

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_signout) {
            // Firebase sign out
            AuthUI.getInstance().signOut(this);

        } else if (id == R.id.nav_test) {
            // tests activity
            Intent intent = new Intent(this, TestsActivity.class);
            startActivity(intent);

        }

        // Return if no fragment was selected
        if (current == null) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        // Fragment manager transaction to replace currently displayed fragment with new currently selected fragment
        manager.beginTransaction()
                .replace(R.id.content, current, tag)
                .addToBackStack(tag)
                .commit();
        drawerLayout.closeDrawer(GravityCompat.START);

        return true;
    }

}

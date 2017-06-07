package card.loyalty.loyaltycardcustomer;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.firebase.ui.auth.AuthUI;

import card.loyalty.loyaltycardcustomer.adapters.PromotionsRecyclerAdapter;
import card.loyalty.loyaltycardcustomer.fragments.CardsRecyclerFragment;
import card.loyalty.loyaltycardcustomer.fragments.PromotionsFragment;
import card.loyalty.loyaltycardcustomer.fragments.QrFragment;

public class CustomerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        // TODO check if launched from notification with Extra describing fragment to launch

        // Set QrFragment as the initial fragment
        if (savedInstanceState == null) {
            Fragment frag = new QrFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction()
                    .replace(R.id.content, frag, QrFragment.FRAGMENT_TAG)
                    .commit();
        }

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

        } else if (id == R.id.nav_promotions) {

            tag = PromotionsFragment.FRAGMENT_TAG;
            current = manager.findFragmentByTag(tag);
            if (current == null) {
                // create if doesn't exist
                current = new PromotionsFragment();
            }

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
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        }

        // Fragment manager transaction to replace currently displayed fragment with new currently selected fragment
        manager.beginTransaction()
                .replace(R.id.content, current, tag)
                .addToBackStack(tag)
                .commit();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

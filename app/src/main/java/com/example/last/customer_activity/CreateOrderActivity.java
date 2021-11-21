package com.example.last.customer_activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Locale;

import com.example.last.AuthAbstractActivity;
import com.example.last.R;
import com.example.last.adapters.MenuItemRecyclerAdapter;
import com.example.last.helper.FirebaseKeylist;
import com.example.last.models.MenuItem;
import com.example.last.models.Order;
import com.example.last.models.Restaurant;

public class CreateOrderActivity extends AuthAbstractActivity implements View.OnClickListener{
    private Restaurant m_Restaurant;
    private LatLng m_DeliveryLocationLatLng;
    private TextView m_RestaurantShortView;
    private TextView m_RestaurantAddressView;
    private MenuItemRecyclerAdapter m_Adapter;
    private Button m_OrderButton;
    private LinearLayoutManager m_LayoutManager;
    private RecyclerView m_MenuItemRecyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_order);
        //get views and variable assignments
        m_Restaurant = (Restaurant) this.getIntent().getParcelableExtra(Restaurant.RESTAURANT_KEY);
        m_DeliveryLocationLatLng = getIntent().getParcelableExtra(LatLng.class.toString());
        m_RestaurantShortView = (TextView) findViewById(R.id.restaurantTextView);
        m_RestaurantAddressView = (TextView) findViewById(R.id.restaurantAddressView);
        m_OrderButton = (Button) findViewById(R.id.createOrderButton);
        m_MenuItemRecyclerView = (RecyclerView) findViewById(R.id.menuItemRecyclerView);
        m_LayoutManager = new LinearLayoutManager(this);
        m_Adapter = new MenuItemRecyclerAdapter(m_Restaurant);

        //recyclerview
        m_MenuItemRecyclerView.setHasFixedSize(true);
        m_MenuItemRecyclerView.setLayoutManager(m_LayoutManager);
        m_Adapter = new MenuItemRecyclerAdapter(m_Restaurant);
        m_MenuItemRecyclerView.setAdapter(m_Adapter);

        //labels and textviews
        m_RestaurantShortView.setText(m_Restaurant.toString());
        m_RestaurantAddressView.setText(m_Restaurant.getAddress());

        //buttons
        m_OrderButton.setEnabled(false);
        m_OrderButton.setOnClickListener(this);
    }

    @Override
    public void onLogin() {
        super.onLogin();
        m_OrderButton.setEnabled(true);
    }

    @Override
    public void onNotLoggedIn() {
        super.onNotLoggedIn();
        m_OrderButton.setEnabled(false);
    }

    @Override
    protected void setTitle(String available_order) {

    }

    @Override
    public void onClick(View v) {
        String destination = null;
        try {
            // get the destination address through the Geocoder service
            Address destinationAddress = new Geocoder(this, Locale.getDefault())
                    .getFromLocation(m_DeliveryLocationLatLng.latitude, m_DeliveryLocationLatLng.longitude, 1).get(0);
            destination = destinationAddress.getAddressLine(0);
        }
        catch (IOException e){
            String errorString = getString(R.string.err_location_svc_unavailable);
            Toast toast2 = Toast.makeText(v.getContext(), errorString, Toast.LENGTH_SHORT);
            toast2.show();
            Log.e("Create Order:", errorString, e);
        }
        if (destination != null) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null){
                //if user is not signed in
                onNotLoggedIn();
            }
            else {
                String userID = user.getUid();
                // create a new order object
                Order theOrder = new Order(m_Restaurant, destination, userID,
                        m_DeliveryLocationLatLng.latitude, m_DeliveryLocationLatLng.longitude);
                // retrieve the menuitems from the adapter's ViewHolders
                for (MenuItem menuItem : m_Adapter.getMenuItemList()) {
                    theOrder.addItem(menuItem);
                }
                DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference(Order.ORDER_KEY);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference(FirebaseKeylist.USER_PROFILE);

                //store order
                String orderKey = orderRef.push().getKey();
                theOrder.setFbKey(orderKey);
                orderRef.child(orderKey).setValue(theOrder);

                //update user profile for having an active order
                userRef.child(userID).child(FirebaseKeylist.USER_PROFILE_ACTIVE_ORDER).setValue(orderKey);

                //we're done!
                String toastStr = "Order created.";
                Toast toast = Toast.makeText(v.getContext(), toastStr, Toast.LENGTH_SHORT);
                toast.show();
                Intent i = new Intent(this, CustomerDeliveryStatusMapActivity.class);
                i.putExtra(Order.ORDER_KEY, theOrder);
                Log.e("HELLOTHERE", orderKey);
                startActivity(i);
            }
        }
    }
}

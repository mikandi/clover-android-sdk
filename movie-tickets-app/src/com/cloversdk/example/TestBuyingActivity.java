package com.cloversdk.example;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.clover.sdk.Clover;
import com.clover.sdk.CloverOrder;
import com.clover.sdk.OrderListener;
import com.clover.sdk.OrderRequest;

public class TestBuyingActivity extends Activity {

  Clover cloverSDK;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    // Initialize the Clover SDK by passing in the merchant ID
    cloverSDK = Clover.init(this, "58129cb7-1a1f-4cf3-8699-13f33bbaa6ae");

    // Populate the First Purchase Info if available. This is used for pre-populating the
    // web overlay only and saves the user from typing this information
    cloverSDK.createFirstPurchaseInfo().setFullName("Nagesh").setEmail("nagesh@example.com");

    // Next create the OrderRequest via a builder.
    final OrderRequest order = cloverSDK.createOrderRequestBuilder()
        .setAmount("0.50").setTitle("Movie Ticket")
        .setPermissions(new String[] {"full_name", "email_address"})
        .setClientOrderId("my_client_id")
        .build();


    final Button buyButton = (Button) findViewById(R.id.button2);
    buyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        // authorizeOrder call on the Clover instance makes a call to the Clover App
        // or a web overlay and returns one of the callbacks of an OrderListener
        cloverSDK.authorizeOrder(TestBuyingActivity.this, order, new OrderListener() {
          @Override
          public void onOrderAuthorized(CloverOrder order) {
            if (order != null) {
              buyButton.setText("Purchased!");
              buyButton.setEnabled(false);
              AlertDialog.Builder builder = new AlertDialog.Builder(TestBuyingActivity.this);
              builder.setTitle("Purchased!");

              String message = String.format("OrderId: %s\nName: %s \nEmail: %s\n", order.id, order.permissions.full_name, order.permissions.email_address.email);

              builder.setMessage(message);
              builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
              });
              builder.show();
            }
          }

          @Override
          public void onCancel() {
            Toast.makeText(TestBuyingActivity.this, "Cancelled by user", Toast.LENGTH_LONG).show();
          }

          @Override
          public void onFailure(Throwable th) {
            Toast.makeText(TestBuyingActivity.this, th.getMessage(), Toast.LENGTH_LONG).show();
          }
        });
      }
    });
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    cloverSDK.onResult(requestCode, resultCode, data);
  }
}
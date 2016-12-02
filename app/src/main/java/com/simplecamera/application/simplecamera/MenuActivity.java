package com.simplecamera.application.simplecamera;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.Toast;

/**
 *   An Activity class extending the AppCompatActivity class
 *   which provides the main menu of the "Treat with CARE" application
 *   and several UI elements, namely the ImageView "previewImage"
 *   which displays the last image to be captured, and the two
 *   Buttons "takePictureButton" and "analyzePictureButton" which
 *   allow the user to open CameraActivity to capture an image using
 *   Android's Camera API and analyze a captured image using Bitmap
 *   operations, respectively.
 *
 *   In detail, the "analyzePictureButton" creates an AlertDialog to
 *   display image analysis options to the user using a single-choice item list.
 *   Depending on which item is selected from the list (given that the user
 *   selects the "positiveButton" after making their selection), the onClick
 *   method for the "positiveButton" will call the appropriate image
 *   analysis method to transform the Bitmap representation of the most
 *   recently captured image and display it to the local ImageView object,
 *   "previewImage".
 *
 *   MenuActivity begins activates the start of CameraActivity in the
 *   onClick method for "takePictureButton", where the CameraActivity is set up
 *   through an Intent, and then the startActivityForResult method is called
 *   on that Intent. The onActivityResult method, when invoked, receives the
 *   path of an image file and reads it from memory into the local Bitmap,
 *   "theImage" and displays it to the ImageView "previewImage". On startup, the
 *   default image that is displayed is "startImage", which is hidden once
 *   onActivityResult is invoked for the first time. "previewImage" is displayed at
 *   a 90 degree angle so that the image is viewed in the correct orientation,
 *   as it is stored in landscape orientation in CameraActivity, and
 *   it is an expensive operation to adjust it before storing it. There is almost certainly
 *   a more efficient way to rotate the image and display it properly, but I
 *   think that for the purposes of this application, rotating the image would
 *   serve no computational benefit at the expense of efficiency.
 *
 *   @author Natalie Wong
 *   @version 1.0
 *   @since May 11, 2016
 */

public class MenuActivity extends AppCompatActivity
{
    private final static int MENUACTIVITY_REQUESTCODE = 42;
    private ImageView startImage, previewImage;
    private Bitmap theImage;
    private BitmapManager mapMan;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        final Context context = this;
        startImage = (ImageView)findViewById(R.id.startImage);
        previewImage = (ImageView)findViewById(R.id.previewImage);
        Button newPictureButton = (Button)findViewById(R.id.newPictureButton);
        Button analyzePictureButton = (Button)findViewById(R.id.analyzePictureButton);

        newPictureButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Toast.makeText(getApplicationContext(), "Launching camera...", Toast.LENGTH_LONG).show();

                Intent switchActivities = new Intent(getApplicationContext(), CameraActivity.class);
                startActivityForResult(switchActivities, MENUACTIVITY_REQUESTCODE);
            }
        });

        analyzePictureButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (theImage == null)
                {
                    Toast.makeText(getApplicationContext(), "This feature is unavailable.", Toast.LENGTH_LONG).show();
                    return;
                }

                AlertDialog.Builder bobTheBuilder = new AlertDialog.Builder(context, R.style.AlertTheme);
                bobTheBuilder.setTitle("Analyze Picture")
                        .setSingleChoiceItems(R.array.options, 0 ,null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which) { dialog.dismiss(); }
                        })
                        .setPositiveButton("Submit", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int which)
                            {
                                int selectedItem = ((AlertDialog)dialog).getListView().getCheckedItemPosition();

                                if (selectedItem == 0)
                                    previewImage.setImageBitmap(theImage);
                                else if (selectedItem == 1)
                                    previewImage.setImageBitmap(mapMan.turnGray());
                                else if (selectedItem == 2)
                                    previewImage.setImageBitmap(mapMan.turnBinary(119));
                                else if (selectedItem == 3)
                                    previewImage.setImageBitmap(mapMan.convertToLAB());
                                else if (selectedItem == 4)
                                    previewImage.setImageBitmap(mapMan.extractRG());
                                else
                                    previewImage.setImageBitmap(mapMan.extractBY());

                                dialog.dismiss();
                            }
                        }).show();
            }

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent activityData)
    {
        if (requestCode == MENUACTIVITY_REQUESTCODE && resultCode == Activity.RESULT_OK)
        {
            String fileName = activityData.getStringExtra("filename key");

            if (theImage != null)
                theImage.recycle();

            theImage = BitmapFactory.decodeFile(fileName);

            if (mapMan == null)
                mapMan = new BitmapManager(theImage);
            else
                mapMan.setBitmap(theImage);

            if (startImage.getVisibility() != ImageView.INVISIBLE)
                startImage.setVisibility(ImageView.INVISIBLE);

            previewImage.setImageBitmap(theImage);
            previewImage.setRotation(90);
        }
    }
}

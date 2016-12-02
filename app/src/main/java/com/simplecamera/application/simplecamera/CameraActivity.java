package com.simplecamera.application.simplecamera;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;;
import android.content.Intent;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 *   An Activity class extending the AppCompatActivity class and
 *   implementing the SurfaceHolder.Callback interface which
 *   provides the most elementary user interface for image capture
 *   making use of the autofocus feature built in to the native camera,
 *   requiring that camera hardware is native to the phone on which
 *   the application is installed and a useable sdcard is installed.
 *
 *   The start of this Activity is designed to be initiated by a call to
 *   startActivityForResult() in a managing Activity class. When a picture is
 *   taken, it is stored into the sdcard and the directory to that specific
 *   location is returned to the managing Activity for retrieval.
 *
 *   @author Natalie Wong
 *   @version 1.0
 *   @since May 11, 2016
 */

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback
{
    /**
     *   The private Camera object to provide the
     *   means to capture images from the device's
     *   physical camera hardware.
     */

    private Camera theCamera;

    /**
     *   The private SurfaceHolder object for
     *   displaying the live camera preview to
     *   the user.
     */

    private SurfaceHolder theHolder;

    /**
     *   The private Camera.PictureCallback object to
     *   process the onPictureTaken() method for storing
     *   a captured image and finishing this Activity.
     */

    private Camera.PictureCallback jpegCallback;

    /**
     *  Generates the user interface, initializes OnClickListeners
     *  and onClick methods for Button objects, and defines the
     *  onPictureTaken() method for the jpegCallback when this Activity
     *  is started.
     *
     *  @param savedInstanceState
     *      If this Activity has previously been started and terminated,
     *      this Bundle will contain useful data most recently stored in
     *      onSaveInstantState(Bundle), and will be null otherwise.
     */

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        SurfaceView theView = (SurfaceView) findViewById(R.id.theView);
        theHolder = theView.getHolder();

        theHolder.addCallback(this);
        theHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        jpegCallback = new PictureCallback()
        {
            public void onPictureTaken(byte[] pictureData, Camera localCamera)
            {

                FileOutputStream out = null;
                String fileName = String.format("/sdcard/SimpleCamera/%d.jpg", System.currentTimeMillis());

                File targetDir = new File("/sdcard/SimpleCamera");
                if (!targetDir.isDirectory())
                    if (!targetDir.mkdir())
                    {
                        Toast.makeText(getApplicationContext(), "Unable to take picture.", Toast.LENGTH_LONG).show();
                        setResult(RESULT_CANCELED);

                        finish();
                    }

                try
                {
                    out = new FileOutputStream(fileName);
                    out.write(pictureData);
                    out.close();

                    Toast.makeText(getApplicationContext(), "Picture taken successfully.", Toast.LENGTH_LONG).show();

                    Intent activityData = new Intent();
                    activityData.putExtra("filename key", fileName);

                    setResult(RESULT_OK, activityData);
                }

                catch (FileNotFoundException e)
                {
                    Toast.makeText(getApplicationContext(), "Unable to take picture.", Toast.LENGTH_LONG).show();

                    setResult(RESULT_CANCELED);
                }

                catch (IOException e)
                {
                    Toast.makeText(getApplicationContext(), "Unable to take picture.", Toast.LENGTH_LONG).show();

                    setResult(RESULT_CANCELED);
                }

                finish();
            }

        };

        Button takePictureButton = (Button) findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                theCamera.takePicture(null, null, jpegCallback);
            }
        });
    }

    /**
     *   Generates the display surface when a SurfaceHolder object is created.
     *
     *   @param holder
     *       The SurfaceHolder object which is being created.
     */

    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            if (theCamera != null)
            {
                theCamera.release();
                theCamera = null;
            }

            theCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        }
        catch (RuntimeException e)
        {
            Toast.makeText(getApplicationContext(), "Unable to connect to camera.", Toast.LENGTH_LONG).show();
            return;
        }

        theCamera.setParameters(getLowResolutionParams(theCamera));
        theCamera.setDisplayOrientation(90);

        try
        {
            theCamera.setPreviewDisplay(theHolder);
            theCamera.startPreview();
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Unable to start camera preview.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     *   Destroys the surface of a SurfaceHolder object when it is being
     *   destroyed.
     *
     *   @param holder
     *       The SurfaceHolder object which is being destroyed.
     */

    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (theCamera != null)
        {
            theCamera.stopPreview();
            theCamera.release();
            theCamera = null;
        }
    }

    /**
     *   Handles changes in the properties of the surface of a
     *   SurfaceHolder object.
     *
     *   @param holder
     *       The SurfaceHolder object whose surface needs readjustment.
     *
     *   @param format
     *       The new surface PixelFormat.
     *
     *   @param width
     *       The new surface width.
     *
     *   @param height
     *       The new surface height.
     */

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (theHolder.getSurface() == null) return;

        theCamera.stopPreview();

        try
        {
            theCamera.setPreviewDisplay(theHolder);
            theCamera.startPreview();
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Unable to start camera preview.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     *   Handles the event where the user leaves,
     *   but does not terminate this Activity and unlocks
     *   the camera being used.
     */

    protected void onPause()
    {
        super.onPause();

        if (theCamera != null)
        {
            theCamera.release();
            theCamera = null;
        }
    }

    /**
     *   Handles the event where the user resumes
     *   the Activity which was previously paused and
     *   restarts and relocks the camera.
     */

    protected void onResume()
    {
        super.onResume();

        if (theCamera == null)
        {
            try
            {
                theCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            catch (RuntimeException e)
            {
                Toast.makeText(getApplicationContext(), "Unable to connect to camera.", Toast.LENGTH_LONG).show();
                return;
            }

            theCamera.setDisplayOrientation(90);

            try
            {
                theCamera.setPreviewDisplay(theHolder);
                theCamera.startPreview();
            }
            catch (Exception e)
            {
                Toast.makeText(getApplicationContext(), "Unable to start camera preview.", Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     *   Terminates this Activity and returns to the calling
     *   Activity safely when the user presses the back button.
     */

    public void onBackPressed()
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     *   Retrieves the Camera.Parameters for the lowest camera
     *   resolution on the camera.
     *
     *   @param localCamera
     *       The Camera object whose support sizes should be
     *       inspected.
     *
     *   @return
     *       A Camera.Parameters object containing the parameters
     *       for the lowest supported camera resolution.
     */

    private Camera.Parameters getLowResolutionParams(Camera localCamera)
    {
        Camera.Parameters params = localCamera.getParameters();
        List<Camera.Size> supportedSizes = params.getSupportedPictureSizes();
        Camera.Size lowResolution = supportedSizes.get(0);

        for (int i = 1; i < supportedSizes.size(); i++)
            if (lowResolution.height > supportedSizes.get(i).height)
                lowResolution = supportedSizes.get(i);

        params.setPictureSize(lowResolution.width, lowResolution.height);

        return params;
    }
}

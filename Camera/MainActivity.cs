using System;
using Android.App;
using Android.Content;
using Android.Hardware;
using Android.Os;
using Android.Util;
using Android.View;
using Android.Widget;
using Dot42.Manifest;
using Java.Io;
using Java.Text;
using Java.Util;
using Android.Bluetooth;
using Bitmap = Android.Graphics.Bitmap;
using Environment = Android.Os.Environment;
using TextureView = Android.View.TextureView;

[assembly: Application("Bluetooth Live Camera", Icon = "Icon")]

[assembly: UsesFeature("android.hardware.camera")]
[assembly: UsesFeature("android.bluetooth")]
[assembly: UsesPermission(Android.Manifest.Permission.CAMERA)]
[assembly: UsesPermission(Android.Manifest.Permission.BLUETOOTH_ADMIN)]
[assembly: UsesPermission(Android.Manifest.Permission.WRITE_EXTERNAL_STORAGE)]

namespace SimpleCamera
{
    /// <summary>
    /// Demonstrates how to preview the camera and take pictures with it.
    /// 
    /// This sample was inspired by: http://stackoverflow.com/questions/10913682/how-to-capture-and-save-an-image-using-custom-camera-in-android
    /// </summary>
    [Activity(ScreenOrientation = ScreenOrientations.Landscape)]
    public class MainActivity : Activity
    {
    	private static UUID MY_UUID = UUID.FromString("00001101-0000-1000-8000-00805F9B34FB");
    	private const string address ="00:1B:10:00:2A:EC";
    
        protected override void OnCreate(Bundle savedInstance)
        {
            base.OnCreate(savedInstance);
            var main = new RelativeLayout(this);
            SetContentView(main);

            var camera = GetCamera();
            var captureButton = new Button(this);
            captureButton.SetWidth(100);
            captureButton.SetX((480/2)-(50));
            //captureButton.SetY(290);
            captureButton.SetText("Connect");
            
            if (camera != null)
            {
                var pre = new Preview(this, camera);
                
                main.AddView(pre);
                
                //previewButton.Visibility = View.INVISIBLE;
                captureButton.Click += (s, x) => {
                	var stream = openConnection();
                	//stream.Write(54);
                };
                
            }
            else
            {
                main.AddView(new TextView(this) { Text = "No camera found" });
                captureButton.Visibility = View.INVISIBLE;
            }
            main.AddView(captureButton);
            
        }

        /// <summary>
        /// Open the camera or return null on errors.
        /// </summary>
        private static Camera GetCamera()
        {
            try
            {
                return Camera.Open();
            }
            catch
            {
                return null;
            }
        }
        public static bool connected = false;
        public class Preview : TextureView, TextureView.ISurfaceTextureListener {
        	
        	Android.Content.Context context;
        	Camera camera;
        	public Preview(Android.Content.Context con, Camera cam) : base(con){context = con; camera = cam; this.SetSurfaceTextureListener(this);}
			
        	public void OnSurfaceTextureAvailable(Android.Graphics.SurfaceTexture surface, int width, int height)
			{
        		camera.SetPreviewTexture(surface);
        		camera.StartPreview();
			}
        	
			public void OnSurfaceTextureSizeChanged(Android.Graphics.SurfaceTexture surface, int width, int height)
			{}
        	
			public bool OnSurfaceTextureDestroyed(Android.Graphics.SurfaceTexture surface)
			{
				camera.StopPreview();
				camera.Release();
				return true;
			}
        	
			public void OnSurfaceTextureUpdated(Android.Graphics.SurfaceTexture surface)
			{
				if (connected){
					
				}
			}
        }
        
        private OutputStream openConnection(){
        	BluetoothAdapter btAdapter = BluetoothAdapter.GetDefaultAdapter();
        	var device = btAdapter.GetRemoteDevice(address);
        	//var btSocket = device.CreateRfcommSocketToServiceRecord(MY_UUID);
        	/*btAdapter.CancelDiscovery();
        	btSocket.Connect();
        	return btSocket.GetOutputStream();*/
        	return null;
        }
        private void CheckBTState(BluetoothAdapter btAdapter) {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if (btAdapter==null) {
        }else{
        if(!btAdapter.IsEnabled()) {
        //Prompt user to turn on Bluetooth
		
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        StartActivityForResult(enableBtIntent, 1);
            }
        }
    }
    }
}

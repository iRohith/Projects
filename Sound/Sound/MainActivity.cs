/*
 * Created by SharpDevelop.
 * User: ROHITH
 * Date: 10/17/2018
 * Time: 6:58 AM
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Threading;
using Android;
using Android.App;
using Android.Media;
using Android.Os;
using Android.Widget;
using Dot42;
using Dot42.Manifest;
using Java.Io;
using Java.Net;

[assembly: Application("Sound")]
[assembly: UsesPermission(Manifest.Permission.INTERNET)]

namespace Sound
{
    [Activity]
    public class MainActivity : Activity
    {
    	ServerSocket mServer;
		Socket mClient;
		BufferedInputStream inStream;
		BufferedOutputStream outStream;
		AudioTrack player;
		Thread Running;
		TextView text;
		const int FREQUENCY = 44100;
		byte[] buffer = new byte[1024*1024];
		
        protected override void OnCreate(Bundle savedInstance) 
        {
            base.OnCreate(savedInstance);
            RelativeLayout layout = new RelativeLayout(this);
            text = new TextView(this);
            layout.AddView(text);
            SetContentView(layout);
            StrictMode.SetThreadPolicy(new StrictMode.ThreadPolicy.Builder().PermitAll().Build());
            
            player = new AudioTrack(AudioManager.STREAM_MUSIC, FREQUENCY, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT, FREQUENCY, AudioTrack.MODE_STREAM);
            Running = new Thread(initConnection);
            Running.Start();
        }
        public void initConnection(){
        start:
        	// initialize server socket
			try
			{
				StrictMode.SetThreadPolicy(new StrictMode.ThreadPolicy.Builder().PermitAll().Build());

				mServer = new ServerSocket(38300);
				mServer.SetSoTimeout(1000 * 1000);

				//attempt to accept a connection
				mClient = mServer.Accept();

				inStream = new BufferedInputStream(mClient.GetInputStream());
				outStream = new BufferedOutputStream(mClient.GetOutputStream());
				
				player.Play();
				Message("Connected");
				while (true) {
					int len = (inStream.Read() << 24) | (inStream.Read() << 16) | (inStream.Read() << 8) | (inStream.Read() << 0);
					if (len == 59874){
						close();
						break;
					}
					if (len <= 0) continue;
					var dat = readFullBytes(inStream, len);
					player.Write(dat, 0, dat.Length);
					/*var read = inStream.Read(buffer, 0, buffer.Length);
					if (read > 0) player.Write(buffer, 0, read);
					Array.Clear(buffer, 0, buffer.Length);*/
				}
			}
			catch
			{
				goto start;
			}
        }
        static byte[] readFullBytes(InputStream @in, int length)
		{
			byte[] buf = new byte[length];
			readFully(@in, buf);
			return buf;
		}
		static void readFully(InputStream @in, byte[] buf)
		{
			readFully(@in, buf, 0, buf.Length);
		}
		static void readFully(InputStream @in, byte[] b, int off, int len)
		{
			int n = 0;
			while (n < len)
			{
				int count = @in.Read(b, off + n, len - n);
				n += count;
			}
		}
			
		override
		public void OnBackPressed()
		{
			close();
			// TODO: Implement this method
			base.OnBackPressed();
		}
		
		override
		protected void OnDestroy()
		{
			// TODO: Implement this method
			close();
			base.OnDestroy();
		}
		void close()
		{
			try
			{
				Running.Stop();
				mServer.Close();
				inStream.Close();
				outStream.Close();
				mClient.Close();
			}
			catch (Exception ee)
			{
				Message(ee.StackTrace);
			}
		}
		void Message(string msg)
		{
			RunOnUiThread(()=>
					{
						text.SetText(msg);
					}
				);
		}
   }
}

/*
 * Created by SharpDevelop.
 * User: ROHITH
 * Date: 3/13/2019
 * Time: 3:48 PM
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;
using NAudio.Lame;
using NAudio.Wave;

namespace SoundStream
{
	class Program
	{
		public static void Main(string[] args)
		{
			Console.WriteLine("Hello World!");
			// TODO: Implement Functionality Here
			WebHandler.Start();
			SoundHandler.Start();
			Console.Write("Press any key to continue . . . ");
			Console.ReadKey(true);
			
		}
	}
	public static class WebHandler {
		static HttpListener http;
		public static bool BytesAvailable = false;
		
		public static void Start(){
			var thread = new Thread(init);
			thread.Start();
			Console.WriteLine("WebHandler started");
		}
		static void init(){
			http = new HttpListener();
			http.Prefixes.Add("http://192.168.0.3:49665/stream/");
			http.Start();
			
			var response = http.GetContext().Response;
			Console.WriteLine("Connected");
			response.KeepAlive = true;
			response.ContentType = "audio/mpeg";
			response.SendChunked = true;
			response.AddHeader("Accept-Ranges", "bytes");
			//response.ContentLength64 = long.MaxValue;
			//if (!String.IsNullOrEmpty(Request.Headers["Range"])) {
                //response.StatusCode = 206;
                //response.AddHeader("Content-Range", String.Format(" bytes {0}-{1}/{2}", 0, (1024*1024*100)-1, (1024*1024*100)));
            //}
            
			var outs = response.OutputStream;
			while (true){
				SpinWait.SpinUntil(()=>BytesAvailable);
				Console.WriteLine("BytesAvaileble");
				try {
					if (SoundHandler.buffer.Length == 0) { BytesAvailable = false; continue; }
					outs.Write(SoundHandler.buffer, 0, SoundHandler.buffer.Length);
					outs.Flush();
					Console.WriteLine("Written bytes = " + SoundHandler.buffer.Length);
				} catch (Exception e) {
					Console.WriteLine(e.StackTrace);
				}
				BytesAvailable = false;
			}
			
		}
	}
		public static class SoundHandler
		{
			public static void Start(){
				init();
				waveIn.StartRecording();
			}
			public static void Stop(){
				waveIn.StopRecording();
			}
			
			static IWaveIn waveIn;
			static LameMP3FileWriter lame;
			public static byte[] buffer;
			static readonly MemoryStream MyStream = new MemoryStream(0);
			
			static void init()
			{	
			   waveIn = new WasapiLoopbackCapture();
			   waveIn.DataAvailable += onDataAvailable;
			   waveIn.RecordingStopped += (sender, e) => waveIn.Dispose();
			   
			   lame = new LameMP3FileWriter(MyStream, waveIn.WaveFormat, 128);
			}
			static long sum(byte[] arr){
				long ret = 0;
				for (int i = 0; i < arr.Length; i++) ret += arr[i];
				return ret;
			}
			static void onDataAvailable(object sender, WaveInEventArgs e)
		    {
				if (sum(e.Buffer) < 10) return;
				Console.WriteLine("Data Availeble");
				lame.Write(e.Buffer, 0, e.BytesRecorded);
				buffer = MyStream.ToArray();
				MyStream.SetLength(0);
				WebHandler.BytesAvailable = true;
		    }
	    }

}
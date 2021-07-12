using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using System.Threading;
using System.Windows.Forms;

namespace Screen_Cast
{
	class Program
	{
		static Socket output;
		const string ADB = "C:\\Program Files (x86)\\Minimal ADB and Fastboot\\adb.exe";
/*
		static byte[] Capture(int width, int height, int x, int y, bool crop, bool rotate)
		{
			var stream = new MemoryStream();
			var bitmap = crop ? new Bitmap(width, height) : new Bitmap(1024, 768);
			var graphics = Graphics.FromImage(bitmap);
			graphics.CopyFromScreen(x, y, 0, 0, new Size(crop ? width : 1024, crop ? height : 768));
			if (crop) {
				if (rotate) bitmap.RotateFlip(RotateFlipType.Rotate90FlipNone);
				var map = new Bitmap(bitmap, rotate ? height : width, rotate ? width : height);
				map.Save(stream, ImageFormat.Jpeg);
				map.Dispose();
			} else {
				if (rotate) bitmap.RotateFlip(RotateFlipType.Rotate90FlipNone);
				var final = new Bitmap(bitmap, rotate ? height : width, rotate ? width : height);
				final.Save(stream, ImageFormat.Jpeg);
			}
			graphics.Dispose();
			bitmap.Dispose();
			graphics = null;
			bitmap = null;
			GC.GetTotalMemory(true);
			return stream.ToArray();
		}
		*/
		static readonly Bitmap bitmap = new Bitmap(1024, 768);
		static readonly Graphics graphics = Graphics.FromImage(bitmap);
			
		static byte[] Capture()
		{
			var stream = new MemoryStream();
			//bitmap.RotateFlip(RotateFlipType.Rotate180FlipNone);
			graphics.CopyFromScreen(0, 0, 0, 0, new Size(1024, 768));
			var mouse = Control.MousePosition;
			graphics.FillEllipse(Brushes.Chocolate, new RectangleF(mouse.X - 8, mouse.Y - 8, 16, 16));
			//var final = new Bitmap(bitmap, rotate ? height : width, rotate ? width : height);
			//final.Save(stream, ImageFormat.Jpeg);
			//var final = new Bitmap(bitmap);
			bitmap.RotateFlip(RotateFlipType.Rotate90FlipNone);
			bitmap.Save(stream, ImageFormat.Jpeg);
			bitmap.RotateFlip(RotateFlipType.Rotate90FlipY);
			//final.Dispose();
			//final = null;
			/*graphics.Dispose();
			bitmap.Dispose();
			final.Dispose();
			final = null;
			graphics = null;
			bitmap = null;*/
			var ret = stream.ToArray();
			stream.Dispose();
			stream = null;
			GC.GetTotalMemory(true);
			return ret;
		}
		static void UsbConnect()
		{
			while (true) {
				var process = Process.Start(ADB, "forward tcp:38300 tcp:38300");
				process.WaitForExit();
				var processes = Process.GetProcessesByName("adb");
				if (processes != null && processes.Length != 0)
					break;
			}
			var inp = new Socket(SocketType.Stream, ProtocolType.IP);
			Console.WriteLine("Waiting for connections");
			inp.Connect("localhost", 38300);
			inp.Send(new byte[]{ 62 });
			output = inp;
			Console.WriteLine("Success");
		}
		static void Main(string[] args)
		{
			
			KillADB();
			Process.GetCurrentProcess().PriorityClass = ProcessPriorityClass.BelowNormal;
			UsbConnect();
			while (true) {
				if (output == null) {
					return;
				}
				try {
					var bytes = Capture();
					var size = ToBytes(bytes.Length);
					output.Send(size);
					output.Send(bytes);
				} catch {
					KillADB();
					Environment.Exit(0);
				}
				Thread.Sleep(30);
			}
		}
		static void KillADB(){
			var adb = Process.GetProcessesByName("adb");
			if (adb == null || adb.Length == 0) return;
			foreach (var v in adb) v.Kill();
		}
		static byte[] ToBytes(int v)
		{
			var bits = new byte[4];
			bits[0] = (byte)((v >> 24) & 255);
			bits[1] = (byte)((v >> 16) & 255);
			bits[2] = (byte)((v >> 8) & 255);
			bits[3] = (byte)((v >> 0) & 255);
			return bits;
		}
		static int FromBytes(IList<byte> bits)
		{
			return ((bits[0] << 24) + (bits[1] << 16) + (bits[2] << 8) + (bits[3] << 0));
		}
	}
}
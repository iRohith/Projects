/*
 * Created by SharpDevelop.
 * User: ROHITH
 * Date: 1/9/2017
 * Time: 5:17 PM
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Diagnostics;
using System.Net.Sockets;
using System.Runtime.InteropServices;
using InTheHand.Net.Sockets;

namespace MouseControl
{
	static class VirtualMouse{
		
		[DllImport("user32.dll")]
		static extern void mouse_event(int dwFlags, int dx, int dy, int dwData, int dwExtraInfo);
		
		const int MOVE = 0x0001;
		const int LEFTDOWN = 0x0002;
		const int LEFTUP = 0x0004;
		const int RIGHTDOWN = 0x0008;
		const int RIGHTUP = 0x00010;
		const int MIDDLEDOWN = 0x0020;
		const int MIDDLEUP = 0x0040;
		const int ABSOLUTE = 0x8000;
		
		public static void Move(int xDelta, int yDelta){
			mouse_event(MOVE, xDelta, yDelta, 0, 0);
		}
		public static void MoveTo(int x, int y){
			int initX = System.Windows.Forms.Control.MousePosition.X, initY = System.Windows.Forms.Control.MousePosition.Y;
			mouse_event(MOVE, initX-x, initY-y, 0, 0);
		}
		public static void LeftClick(){
			mouse_event(LEFTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
			mouse_event(LEFTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}
		public static void LeftDown(){
			mouse_event(LEFTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}
		public static void LeftUp(){
			mouse_event(LEFTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}
		public static void RightClick(){
			mouse_event(RIGHTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
			mouse_event(RIGHTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}
		public static void RightDown(){
			mouse_event(RIGHTDOWN, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}
		public static void RightUp(){
			mouse_event(RIGHTUP, System.Windows.Forms.Control.MousePosition.X, System.Windows.Forms.Control.MousePosition.Y, 0, 0);
		}

	}
	class Program
	{
		const int MOUSE_RIGHT_CLICK = 2, MOUSE_LEFT_CLICK = 3, MOUSE_TRANSLATE = 1, MOUSE_RIGHT_DOWN = 4, MOUSE_LEFT_DOWN = 5, MOUSE_RIGHT_UP = 6, MOUSE_LEFT_UP = 7, NOTHING = 0;
		public static void Main(string[] args)
		{
			var bc = new BluetoothClient();
			var id = new Guid("{00001101-0000-1000-8000-00805f9b34fb}");
			try {
				var bl = new BluetoothListener(id);
				bl.Start();
				
				while (true) {
					Trace.WriteLine("Waiting for connection...");
					Console.WriteLine("Waiting for connection...");
					Socket input;
					if ((input = bl.AcceptSocket()) != null) {
						Trace.WriteLine("Success");
						Console.WriteLine("Success");
					}
					int task;
					bool cont = true;
					while (cont) {
						try {
							task = ReadByte(input);
							switch (task) {
								case MOUSE_LEFT_CLICK:
									{
										VirtualMouse.LeftClick();
										break;
									}
								case MOUSE_RIGHT_CLICK:
									{
										VirtualMouse.RightClick();
										break;
									}
								case MOUSE_RIGHT_DOWN:
									{
										VirtualMouse.RightDown();
										break;
									}
								case MOUSE_LEFT_DOWN:
									{
										VirtualMouse.LeftDown();
										break;
									}
								case MOUSE_RIGHT_UP:
									{
										VirtualMouse.RightUp();
										break;
									}
								case MOUSE_LEFT_UP:
									{
										VirtualMouse.LeftUp();
										break;
									}
								case MOUSE_TRANSLATE:
									{
										int transX = (int)(((double)(ReadByte(input) - 255)) / 1.651612903225806);
										int transY = (int)(((double)(ReadByte(input) - 255)) / 1.651612903225806);
										VirtualMouse.Move(transX, transY);
										break;
									}
								case NOTHING:
									{
										cont = false;
										break;
									}
							}
						} catch {
							break;
						}
					}
				}
			} catch {
				return;
			}
		}
		static int ReadByte(Socket soc){
			var b = new byte[1];
			soc.Receive(b);
			return b[0];
		}
	}
}
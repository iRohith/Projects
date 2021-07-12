/*
 * Created by SharpDevelop.
 * User: RAINBOW
 * Date: 1/13/2017
 * Time: 5:49 PM
 * 
 * To change this template use Tools | Options | Coding | Edit Standard Headers.
 */
using System;
using System.Drawing;
using System.Windows.Forms;
using InTheHand.Net.Bluetooth;
using System.Net.Sockets;

namespace Bluetooth_Camera
{
	/// <summary>
	/// Description of Camera.
	/// </summary>
	public partial class Camera : Form
	{
		public Camera()
		{
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();
			
			//
			// TODO: Add constructor code after the InitializeComponent() call.
			//
		}
		public static void Main(string[] args){
			BluetoothRadio br = BluetoothRadio.PrimaryRadio;
			Console.WriteLine(br.Name);
			Guid guid = new Guid("{00001101-0000-1000-8000-00805F9B34FB}");
			var bl = br.StackFactory.CreateBluetoothListener(guid);
			bl.Start(50);
			Console.WriteLine("Listening...Address : "+br.LocalAddress);
			var socket = bl.AcceptBluetoothClient();
			Console.WriteLine("Accepted");
			byte[] bytes = new byte[5];
			socket.Connect(Receive(bytes);
			Console.WriteLine(bytes[0]);
		}
	}
}

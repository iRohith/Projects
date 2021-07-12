using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Drawing.Imaging;
using Bluetooth_Live_Camera.Resources;

namespace Bluetooth_Live_Camera
{
    public partial class Form1 : Form
    {
        static int WIDTH = 128, Height = 96;
        Socket socket = null;
        byte[] bytes = new byte[WIDTH * Height * 3], tempByte = new byte[1];

        public Form1()
        {
            InitializeComponent();
            
            socket = new Socket(SocketType.Stream, ProtocolType.IP);
            socket.Connect("127.78.78.78", 888);
            socket.ReceiveBufferSize = WIDTH * Height * 3;
            
            System.Windows.Forms.Timer timer = new System.Windows.Forms.Timer();
            timer.Interval = 1;
            timer.Tick += new EventHandler(task);
            timer.Start();
            
        }

        private void Form1_Load(object sender, EventArgs e)
        {

        }

        private void task(object obj, EventArgs args) 
        {
            for (int i = 0; i < WIDTH * Height * 3; i++ )
            {
                socket.Receive(tempByte, tempByte.Length, SocketFlags.None);
                bytes[i] = tempByte[0];
            }
            this.BackgroundImage = Image.FromHbitmap(ImageDecoder.decode(bytes, WIDTH, Height).GetHbitmap());
        }
    }
}

using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Drawing;
using System.IO;

namespace Bluetooth_Live_Camera.Resources
{
    class ImageDecoder
    {
        public static Bitmap decode(byte[] ImageData, int width, int height) {
            Bitmap image = new Bitmap(width, height);
            MemoryStream data = new MemoryStream(ImageData);
            for (int i = 0; i < width; i++)
            {
                for (int j = 0; j < height; j++)
                {
                    image.SetPixel(i, j, Color.FromArgb(data.ReadByte(), data.ReadByte(), data.ReadByte()));
                }
            }
            return image;
        }
        
    }
}

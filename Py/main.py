import cv2
import numpy as np
from screencap import screencap
import pytesseract

from NaiveScrcpyClient import *

'''with screencap(True) as cap:
    key = None
    while cap.running and key != ord('q') and key != 27:
        img = cap()
        cv2.imshow('Image', img)
        key = cv2.waitKey(30)
        cap.set_coords(key)

    cv2.destroyAllWindows()'''

config = {
        "max_size": 2000,
        "bit_rate": 2 ** 30,
        "crop": "-",
        "adb_path": "adb",
        "adb_port": 61550,
        "lib_path": "lib",
        "buff_size": 0x10000,
        "deque_length": 5
    }

def draw_circle(event,x,y,flags,param):
    if event == cv2.EVENT_LBUTTONDBLCLK:
        print(x,y)

cv2.namedWindow('image')
cv2.setMouseCallback('image',draw_circle)

def get_roi(img, hei, thresh=0.8):
    siz = img.shape[1] * hei * 255
    for i in range(0, img.shape[0] - hei):
        nImg = img[i:i+hei]
        if np.sum(nImg/siz) > thresh: return nImg
    return img

client = NaiveScrcpyClient(config)
ret = client.start_loop()
while True:
    try:
        img = client.get_screen_frame()
        if img is not None:
            #928X2000
            img = get_roi(cv2.cvtColor(img[510:1530, 720:820], cv2.COLOR_BGR2GRAY), 30)
            thresh = cv2.threshold(img, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]
            data = pytesseract.image_to_string(thresh, lang='eng',config='--psm 6 -c tessedit_char_whitelist=0123456789.')
            print(data)
            cv2.imshow("image", img)
        c = cv2.waitKey(30)
        if c in [27, 13]:
            break
    except KeyboardInterrupt:
        break

cv2.destroyAllWindows()
client.stop_loop()